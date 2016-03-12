package de.ur.ase.online_dictionary;

import de.ur.ase.offline_dictionary.Stopwords;
import de.ur.ase.model.StringProbability;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

/**
 * The OnlineLookup class sees if the recognised words are included in online dictionaries.
 * If they are, they are boosted even higher or their spelling may be corrected or they may be
 * turned into their base form.
 */
public class OnlineLookup {

    public static final String CANOO_URL = "http://www.canoo.net/services/Controller?service=canooNet&input=";
    public static final String BING_URL = "http://www.bing.com/search?q=";

    public static boolean PERFORM_BING_SEARCH = true;
    public static boolean SEARCH_FOR_SYNONYMS_AND_HYPERNYMS = false;

    /**
     * Main routine of this class.
     * @param words
     * the word candidate at this point
     * @param totalNumRecognitions
     * how many words the text recognition tool found in total
     * @param onFinishCallback
     * since this takes a long time, it is done in another thread (which is not that important for a command line
     * tool, but it doesn't hurt) and needs a callback
     */
    public void lookupOnline(Set<Set<StringProbability>> words, int totalNumRecognitions, Consumer<List<StringProbability>> onFinishCallback) {
        new LookupThread(words, onFinishCallback, totalNumRecognitions).start();
    }

    /**
     * The actual logic is in here, since the online verification takes a long time.
     */
    private class LookupThread extends Thread {

        private Consumer<List<StringProbability>> onFinishCallback;
        private Set<Set<StringProbability>> words;
        private int totalNumRecognitions;

        private Set<String> lookedUp;

        /**
         * The actual logic is in here, since the online verification takes a long time.
         */
        public LookupThread(Set<Set<StringProbability>> words, Consumer<List<StringProbability>> onFinishCallback, int totalNumRecognitions) {
            this.words = words;
            this.onFinishCallback = onFinishCallback;
            this.lookedUp = new HashSet<>();
            this.totalNumRecognitions = totalNumRecognitions;
        }

        @Override
        public void run() {
            List<StringProbability> finalWords = new ArrayList<>();

            for(Set<StringProbability> wordAlternatives : words) {
                forEveryWordSet(finalWords, wordAlternatives);
            }

            onFinishCallback.accept(finalWords);
        }

        /**
         * Decomposition method that is called for every word set (i.e., Strings that are likely to refer to the same word)
         * , hence the name.
         * @param finalWords
         * words will be added to this list after they are verified.
         * @param wordAlternatives
         * the word set
         */
        private void forEveryWordSet(List<StringProbability> finalWords, Set<StringProbability> wordAlternatives) {
            List<StringProbability> asList = new ArrayList<>(wordAlternatives);
            Collections.sort(asList, (sp1, sp2) -> Double.compare(sp1.getProbability(), sp2.getProbability()));
            Collections.reverse(asList);

            for(StringProbability word : wordAlternatives) {
                List<StringProbability> wordsFoundForWord = performCanooSearch(word, true);

                if(wordsFoundForWord != null) {
                    finalWords.addAll(wordsFoundForWord);
                }

                bePoliteToCanoo();
            }
        }

        /**
         * Performs a canoo search for a word.
         *
         * @param tryToCorrectIfNoResult
         * true: if canoo has no entry for the term, Bing will be consulted to see if there is a spelling error in the word.
         * If Bing suggests there is, the method might be called again (kind of recursively, but only once) with the
         * corrected version.
         * @return
         * A list of words that were found while searching for the word, e.g. the unaltered version of the word,
         * a "grammatical base form" of the word or a spelling corrected version of it.
         */
        private List<StringProbability> performCanooSearch(StringProbability word, boolean tryToCorrectIfNoResult) {
            if(lookedUp.contains(word.getString())) return null; // might be the case if this is a word that another word was corrected to by Bing
            try {
                URL url = new URL(CANOO_URL + word.getString());
                Document doc = Jsoup.parse(url, 5000);

                Elements headline = doc.getElementsByClass("Headline");
                lookedUp.add(word.getString());
                if(headline.size() > 0) {
                    if(noCanooEntryFor(headline)) {
                        if(PERFORM_BING_SEARCH) {
                            if (tryToCorrectIfNoResult) return tryToCorrect(word);
                        }
                        else if(wordIsVeryLikely(word)) return Collections.singletonList(word);
                        else return null;
                    } else {
                        return getResultsFromCanooPage(doc, word);
                    }
                } else {
                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Simply based on "experience"/looking at the data.
         * If we found no result, the word might still be in the video
         * (some abbreviation, like a hashtag, an unusual last name, or whatever might still be an actual word in the video)
         */
        private boolean wordIsVeryLikely(StringProbability word) {
            return word.getProbability() >= totalNumRecognitions * 0.75;
        }

        /**
         * Performs a bing search for the word.
         * If Bing suggests another word (or, even if it doesn't, the only results concern an altered
         * version of the string), this word might be more likely to have been in the video.
         */
        private List<StringProbability> tryToCorrect(StringProbability word) {
            BingSearchResult correction = performBingSearch(word.getString());

            if(correction != null && correction.hasCorrectedQuery() && !Stopwords.isStopword(correction.correctedQuery) && !lookedUp.contains(correction.correctedQuery)) {
                word.setProbability(word.getProbability() * 2);

                bePoliteToCanoo();
                word.setString(correction.correctedQuery);
                List<StringProbability> correctedResults = performCanooSearch(word, false); // must be false, otherwise: lots of recursion!!
                if (correctedResults == null) return Collections.singletonList(new StringProbability(correction.correctedQuery, word.getProbability()));
                return correctedResults;
            } else if (correction != null && correction.commonString != null) {
                word.setProbability(word.getProbability() * 2);
                return Collections.singletonList(new StringProbability(correction.commonString, word.getProbability()));
            } else if (wordIsVeryLikely(word)){ // word is very likely --> just take it, probably still better than doing nothing
                return Collections.singletonList(word);
            } else {
                return null;
            }
        }

    }

    /**
     * One of the most important qualities for all crawlers: don't call the server too often with too little
     * time in between. Also, canoo will not allow further queries if we use it faster than that.
     */
    private void bePoliteToCanoo() {
        try {
            Thread.sleep(5000 + ((long) (Math.random() * 500)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses the canoo results page; if the words are not (in some form) nouns, verbs, or adjectives, they will be
     * ignored. Looks for any base form of the words. (may look for synonyms and hypernyms, depending on the value
     * of {@link #SEARCH_FOR_SYNONYMS_AND_HYPERNYMS}).
     */
    private List<StringProbability> getResultsFromCanooPage(Document doc, StringProbability originalWord) {
        Elements wordFormElements = doc.getElementsByClass("Indent2first");

        boolean nounOrVerbOrAdjective = false;
        for(Element element : wordFormElements) {
            String text = element.text();
            if(text.contains("Nomen")) nounOrVerbOrAdjective = true;
            else if (text.contains("Verb")) nounOrVerbOrAdjective = true;
            else if (text.contains("Adjektiv")) nounOrVerbOrAdjective = true;

            if(text.contains("geo") || text.contains("Name")) return Arrays.asList(originalWord);
        }
        if(!nounOrVerbOrAdjective) return null;

        String baseForm = getBaseWord(doc, originalWord.getString());

        List<StringProbability> wordsToReturn = new ArrayList<>();
        wordsToReturn.add(new StringProbability(baseForm, originalWord.getProbability() * 2)); // because we found something!! actual word = great!

        if(SEARCH_FOR_SYNONYMS_AND_HYPERNYMS) addSynonymsAndHypernyms(doc, wordsToReturn, originalWord.getProbability());

        return wordsToReturn;
    }

    /**
     * Canoo suggests those, but without providing context or doing some massive language analysis
     * (presumably; I know too little about this to know if that would actually even help), using them as tags increases
     * the amount of "trash" in our word list and they are long enough as is (so this is disabled by default).
     */
    private void addSynonymsAndHypernyms(Document doc, List<StringProbability> wordsToReturn, double originalProbability) {
        Elements tableRows = doc.getElementsByTag("tr");
        for(Element tableRow : tableRows) {
            Elements tableData = tableRow.getElementsByTag("td");
            if(tableData.size() == 2) {
                if((tableData.get(0).hasClass("wordnetSynonym") || tableData.get(0).hasClass("wordnetHypernym")) &&
                        tableData.get(1).getElementsByTag("a").size() > 0) {
                    String alternativeTermsString = tableData.get(1).text();
                    String[] alternatives = alternativeTermsString.split(", ");

                    for(String a : alternatives) {
                        wordsToReturn.add(new StringProbability(a, originalProbability));
                    }

//                    breakAtNextEmptyRow = true;
                }
            }
        }
    }

    /**
     * Returns the base form of a word (e.g. "laufen" for "laufe")
     */
    private String getBaseWord(Document doc, String originalWord) {
        String getBaseWord = originalWord;
        Elements wordNetForms = doc.getElementsByClass("wordnetForm");
        for(Element wordNetForm : wordNetForms) {
            getBaseWord = wordNetForm.ownText();
            break; // the first one should be the best hit; there is a lot of uncertainty in this assumption,
            // but it's still our best guess...
        }
        return getBaseWord;
    }

    /**
     * Searches for the word in Bing; might return null.
     * If the search if successful, spelling errors may have been found.
     */
    private BingSearchResult performBingSearch(String string) {
        // @todo: lots of mentions of this very String on the results page? --> add "as is" if there is no correction!
        try {
            URL url = new URL(BING_URL + string);
            Document bingSite = Jsoup.parse(url, 5000);
            BingSearchResult bingSearchResult = new BingSearchResult();
            bingSearchResult.parseSite(bingSite);
            return bingSearchResult;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks whether or not canoo has an entry for the word that we were looking for.
     */
    private boolean noCanooEntryFor(Elements headlines) {
        boolean noPositiveHeadline = true;
        for(Element headline : headlines) {
            String headlineText = headline.text();
            if(!(headlineText.toLowerCase().contains("keine eintr") ||
                    headlineText.toLowerCase().contains("der gesuchte begriff ist nicht") ||
                    headlineText.toLowerCase().contains("zu viele anfr")))  {
                noPositiveHeadline = false;
                break;
            }
        }
        return noPositiveHeadline;
    }

    // mostly a "separate" class for decomposition purposes...
    /**
     * BingSearchResults can parse and contain the results from a Bing HTML page. we don't use any of
     * MS's search APIs but just parse a Bing page.
     */
    private class BingSearchResult {

        private String correctedQuery;
        private String commonString;

        /**
         * Parses the bing Site, looks for spelling corrected version of the word we were looking for.
         */
        public void parseSite(Document bingSite) {
            Element correction = bingSite.getElementById("sp_requery");
            if(correction != null) {
                setCorrectedQuery(correction);
            } else {
                lookForCommonString(bingSite);
            }
        }

        /**
         * If Bing had no suggestion for the actual word, they may still have searched for an alternative
         * word than what we were looking for and just left out the "hey, did you mean this...?" message.
         * <br>
         * Therefore, this method checks if one of the highlighted words appears very often on the page.
         */
        private void lookForCommonString(Document bingSite) {
            List<StringCount> stringCounts = new ArrayList<>();

            Elements searchResults = bingSite.getElementsByClass("b_algo");
            for(Element searchResult : searchResults) {
                Elements strongElements = searchResult.getElementsByTag("strong");
                for(Element strongElement : strongElements) {
                    forEveryHighlightedText(stringCounts, strongElement);
                }
            }

            Collections.sort(stringCounts, (o1, o2) -> Integer.compare(o1.count, o2.count));
            Collections.reverse(stringCounts);

            if(stringCounts.size() > 0 && stringCounts.get(0).count >= 2) commonString = stringCounts.get(0).string;
        }

        /**
         * Decomposition method that gets called for every highlighted text in a Bing results page.
         * Highlighted text = Bing saying: "this part matches your query!!"
         */
        private void forEveryHighlightedText(List<StringCount> stringCounts, Element strongElement) {
            String strongText = strongElement.text();

            boolean alreadyFound = false;
            for(StringCount stringCount : stringCounts) {
                if(strongText.toLowerCase().equals(stringCount.string.toLowerCase())) {
                    stringCount.count++;
                    alreadyFound = true;
                    break;
                }
            }
            if(!alreadyFound) {
                StringCount newStringCount = new StringCount();
                newStringCount.string = strongText;
                newStringCount.count = 1;
                stringCounts.add(newStringCount);
            }
        }

        /**
         * Reads the element's text and adds it as the corrected version of the string we were looking for.
         *
         * @param correction
         * verify that this is not null before calling the method!!
         */
        private void setCorrectedQuery(Element correction) {
            Elements correctionLink = correction.getElementsByTag("a");
            if(correctionLink.size() > 0) correctedQuery = correctionLink.get(0).text();
        }

        public boolean hasCorrectedQuery() {
            return correctedQuery != null;
        }
    }

    /**
     * Data storage class that contains a String and an Integer number.
     */
    private class StringCount {
        public String string;
        public int count;
    }

}
