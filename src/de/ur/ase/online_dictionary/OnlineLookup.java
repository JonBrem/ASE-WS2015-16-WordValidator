package de.ur.ase.online_dictionary;

import de.ur.ase.Stopwords;
import de.ur.ase.model.StringProbability;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Collector;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class OnlineLookup {

    public static final String CANOO_URL = "http://www.canoo.net/services/Controller?service=canooNet&input=";
    public static final String BING_URL = "http://www.bing.com/search?q=";

    public void lookupOnline(Set<Set<StringProbability>> words, int totalNumRecognitions, Consumer<Set<String>> onFinishCallback) {
        new LookupThread(words, onFinishCallback, totalNumRecognitions).start();
    }

    private class LookupThread extends Thread {

        private Consumer<Set<String>> onFinishCallback;
        private Set<Set<StringProbability>> words;
        private int totalNumRecognitions;

        private Set<String> lookedUp;

        public LookupThread(Set<Set<StringProbability>> words, Consumer<Set<String>> onFinishCallback, int totalNumRecognitions) {
            this.words = words;
            this.onFinishCallback = onFinishCallback;
            this.lookedUp = new HashSet<>();
            this.totalNumRecognitions = totalNumRecognitions;
        }

        @Override
        public void run() {
            Set<String> finalWords = new HashSet<>();

            for(Set<StringProbability> wordAlternatives : words) {
                forEveryWordSet(finalWords, wordAlternatives);
            }

            onFinishCallback.accept(finalWords);
        }

        private void forEveryWordSet(Set<String> finalWords, Set<StringProbability> wordAlternatives) {
            List<StringProbability> asList = new ArrayList<>(wordAlternatives);
            Collections.sort(asList, (sp1, sp2) -> Double.compare(sp1.probability, sp2.probability));
            Collections.reverse(asList);

            for(StringProbability word : wordAlternatives) {
                List<String> wordsFoundForWord = performCanooSearch(word, true);

                if(wordsFoundForWord != null) {
                    finalWords.addAll(wordsFoundForWord);
                }

                try {
                    Thread.sleep(2500 + ((long) (Math.random() * 500)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private List<String> performCanooSearch(StringProbability word, boolean tryToCorrectIfNoResult) {
            if(lookedUp.contains(word.string)) return null; // might be the case if this is a word that another word was corrected to by Bing
            try {
                URL url = new URL(CANOO_URL + word.string);
                Document doc = Jsoup.parse(url, 5000);

                Elements headline = doc.getElementsByClass("Headline");
                lookedUp.add(word.string);
                if(headline.size() > 0) {
                    if(noCanooEntryFor(headline)) {
                        return tryToCorrect(word, tryToCorrectIfNoResult);
                    } else {
                        List<String> resultsFromCanooPage = getResultsFromCanooPage(doc, word.string);
                        return resultsFromCanooPage;
                    }
                } else {
                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private List<String> tryToCorrect(StringProbability word, boolean tryToCorrectIfNoResult) {
            if(!tryToCorrectIfNoResult) return null;

            BingSearchResult correction = performBingSearch(word.string);

            if(correction == null) {
                System.out.println("Correction for " + word.string + " was null");
            } else {
                System.out.println("Correction for " + word.string + ": ");
                System.out.println("\t" + correction.hasCorrectedQuery() + "\t" + correction.correctedQuery + "\t" + correction.commonString + "\t");
            }

            if(correction != null && correction.hasCorrectedQuery() && !Stopwords.isStopword(correction.correctedQuery) && !lookedUp.contains(correction.correctedQuery)) {
                word.string = correction.correctedQuery;
                List<String> correctedResults = performCanooSearch(word, false); // must be false, otherwise: lots of recursion!!
                if (correctedResults == null) return Collections.singletonList(correction.correctedQuery);
                return correctedResults;
            } else if (correction != null && correction.commonString != null) {
                return Collections.singletonList(correction.commonString);
            } else if (word.probability >= totalNumRecognitions * 0.75){ // word is very likely --> just take it, probably still better than doing nothing
                return Collections.singletonList(word.string);
            } else {
                return null;
            }
        }

    }

    private List<String> getResultsFromCanooPage(Document doc, String originalWord) {
        Elements wordFormElements = doc.getElementsByClass("Indent2first");

        boolean nounOrVerb = false;
        for(Element element : wordFormElements) {
            String text = element.text();
            if(text.contains("Nomen")) nounOrVerb = true;
            else if (text.contains("Verb")) nounOrVerb = true;

            if(text.contains("geo") || text.contains("Name")) return Arrays.asList(originalWord);
        }
        if(!nounOrVerb) return null;

        String baseForm = getBaseWord(doc, originalWord);

        List<String> wordsToReturn = new ArrayList<>();
        wordsToReturn.add(baseForm);

//        addSynonymsAndHypernyms(doc, wordsToReturn);

        return wordsToReturn;
    }

    private void addSynonymsAndHypernyms(Document doc, List<String> wordsToReturn) {
        Elements tableRows = doc.getElementsByTag("tr");
        boolean breakAtNextEmptyRow = false;
        for(Element tableRow : tableRows) {
            Elements tableData = tableRow.getElementsByTag("td");
            if(tableData.size() == 2) {
                if((tableData.get(0).hasClass("wordnetSynonym") || tableData.get(0).hasClass("wordnetHypernym")) &&
                        tableData.get(1).getElementsByTag("a").size() > 0) {
                    String alternativeTermsString = tableData.get(1).text();
                    String[] alternatives = alternativeTermsString.split(", ");
                    wordsToReturn.addAll(Arrays.asList(alternatives));

                    breakAtNextEmptyRow = true;
                }
            } else if (breakAtNextEmptyRow && tableData.size() < 2) break;
        }
    }

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

    private boolean noCanooEntryFor(Elements headlines) {
        boolean atLeastOnePositiveHeadline = false;
        for(Element headline : headlines) {
            String headlineText = headline.text();
            if(!(headlineText.toLowerCase().contains("keine eintr") ||
                    headlineText.toLowerCase().contains("der gesuchte begriff ist nicht") ||
                    headlineText.toLowerCase().contains("zu viele anfr")))  {
                atLeastOnePositiveHeadline = true;
                break;
            }
        }
        return !atLeastOnePositiveHeadline;
    }

    private class BingSearchResult {
        private String correctedQuery;
        private String commonString;

        public void parseSite(Document bingSite) {
            Element correction = bingSite.getElementById("sp_requery");
            if(correction != null) {
                setCorrectedQuery(correction);
            } else {
                lookForCommonString(bingSite);
            }
        }

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

        private void setCorrectedQuery(Element correction) {
            Elements correctionLink = correction.getElementsByTag("a");
            if(correctionLink.size() > 0) correctedQuery = correctionLink.get(0).text();
        }

        public boolean hasCorrectedQuery() {
            return correctedQuery != null;
        }
    }

    private class StringCount {
        public String string;
        public int count;
    }

}
