package de.ur.ase;

import de.ur.ase.join_filter.FilterWords;
import de.ur.ase.join_filter.JoinWords;
import de.ur.ase.n_gram_probability.NGramProbabilityBoost;
import de.ur.ase.model.StringProbability;
import de.ur.ase.n_gram_probability.NGramProbability;
import de.ur.ase.model.Frame;
import de.ur.ase.offline_dictionary.CaseInsensitiveDictionary;
import de.ur.ase.offline_dictionary.OfflineDictionaryBoost;
import de.ur.ase.offline_dictionary.Stopwords;
import de.ur.ase.online_dictionary.OnlineLookup;
import de.ur.ase.string_similarity.NeedlemanWunschDistance;
import de.ur.ase.string_similarity.StringDistanceCalculator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Entry point & routine that is executed when the program is started from the command line
 * (that is, the version we "distribute" enters here).
 */
public class Main {

    /**
     * Entry point & routine that is executed when the program is started from the command line
     * (that is, the version we "distribute" enters here).
     */
    public static void main(String... args) {
        String pathForDictionaries, filePath;

        if (args.length == 0) {
            pathForDictionaries = "";
            filePath = "save_good/output_1001.json";
        } else {
            pathForDictionaries = args[0] + "/";
            filePath = args[1];
        }

        // methods that long are bad style, but it is a step-by-step routine & I din't want to further group any of the steps

        configureOnlineVerification(args);

        List<Frame> frameList = getFrameListFromFile(filePath);
        int totalNumRecognitions = getTotalNumRecognitions(frameList);
        StringDistanceCalculator distanceCalculator = new NeedlemanWunschDistance();

        WordValidator validator = new WordValidator(frameList, distanceCalculator);
        validator.run();
        List<StringProbability> probabilities = validator.getStringProbabilities();

        removeStopwords(probabilities);

        applyNGramProbabilityBoost(probabilities, pathForDictionaries);
        applyOfflineDictionaryBoost(probabilities, pathForDictionaries);


        Set<Set<StringProbability>> mostLikelyWords = filterAndJoinWords(probabilities, distanceCalculator, frameList.size(), totalNumRecognitions);
        verifyOnline(mostLikelyWords, args.length >= 3 ? args[2] : null, totalNumRecognitions);
    }

    /**
     * If the 4th param is set, this will decide what to do with it.
     * If it is
     * <ul>
     *     <li>0: a bing search will be performed and the program will look for synonyms and hypernyms.</li>
     *     <li>1: a bing search will be performed, the program will not look for synonyms and hypernyms. (this is what happens when the param is not set)</li>
     *     <li>2: no bing search will be performed and the program will look for synonyms and hypernyms.</li>
     *     <li>3: no bing search will be performed, the program will not look for synonyms and hypernyms.</li>
     * </ul>
     * @param args
     * args[3] will be used; must be parsable and should be 0 <= args[3] <= 3
     */
    private static void configureOnlineVerification(String[] args) {
        if(args.length >= 4) {
            switch(Integer.parseInt(args[3])) {
                case 1:
                    OnlineLookup.PERFORM_BING_SEARCH = true;
                    OnlineLookup.SEARCH_FOR_SYNONYMS_AND_HYPERNYMS = false;
                    break;
                case 2:
                    OnlineLookup.PERFORM_BING_SEARCH = false;
                    OnlineLookup.SEARCH_FOR_SYNONYMS_AND_HYPERNYMS = true;
                    break;
                case 3:
                    OnlineLookup.PERFORM_BING_SEARCH = false;
                    OnlineLookup.SEARCH_FOR_SYNONYMS_AND_HYPERNYMS = false;
                    break;
                default:
                case 0:
                    OnlineLookup.PERFORM_BING_SEARCH = true;
                    OnlineLookup.SEARCH_FOR_SYNONYMS_AND_HYPERNYMS = true;
                    break;
            }
        }
    }

    /**
     * Removes the stop words contained in the {@link Stopwords} class.
     * @param stringProbabilities the words so far.
     */
    private static void removeStopwords(List<StringProbability> stringProbabilities) {
        Iterator<StringProbability> iter = stringProbabilities.iterator();
        while(iter.hasNext()) {
            if(Stopwords.isStopword(iter.next().getString())) iter.remove();
        }
    }

    /**
     * Returns the number of words the detector found across all the frames.
     * @param frameList FrameList, probably from a JSON detection file.
     * @return the number of words in all the frames.
     */
    private static int getTotalNumRecognitions(List<Frame> frameList) {
        int totalNumRecognitions = 0;
        for(Frame f : frameList) {
            totalNumRecognitions += f.getWords().size();
        }
        return totalNumRecognitions;
    }

    /**
     * Method that calls the routine in {@link OnlineLookup}
     * @param probabilities
     * probabilities at this step in the evaluation process
     * @param file
     * Path of file to write results to. Pass null for Console output.
     * @param totalNumRecognitions
     * How many words are in all the frames (necessary for the OnlineLookup routine)
     */
    private static void verifyOnline(Set<Set<StringProbability>> probabilities, String file, int totalNumRecognitions) {
        new OnlineLookup().lookupOnline(probabilities, totalNumRecognitions, words -> {
            removeDuplicateWords(words);
            Collections.sort(words, (sp1, sp2) -> Double.compare(sp1.getProbability(), sp2.getProbability()));
            Collections.reverse(words);

            if(file != null) {
                try {
                    FileWriter out =  new FileWriter(new File(file));
                    dumpWordsToWriter(words, out);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                dumpWordsToStream(words, System.out);
            }
        });
    }

    private static void removeDuplicateWords(List<StringProbability> probabilities) {
        Set<Integer> toRemove = new HashSet<>();

        for(int i = 0; i < probabilities.size() - 1; i++) {
            for(int j = i + 1; j < probabilities.size(); j++) {
                if(probabilities.get(i).getString().toLowerCase().equals(probabilities.get(j).getString().toLowerCase())) {
                    if(!toRemove.contains(j)) {
                        toRemove.add(j);
                    }
                }
            }
        }

        List<Integer> asList = new ArrayList<>(toRemove);
        Collections.sort(asList);
        Collections.reverse(asList); // start with highest because removing from the end doesn't cause problems, removing earlier indices does.
        for (Integer i : asList) {
            probabilities.remove(i.intValue());
        }
    }

    /**
     * Applies the steps implemented in {@link JoinWords} and {@link FilterWords} to the list of words.
     *
     * @param probabilities
     * List of words in the current step.
     * @param distanceCalculator
     * Some DistanceCalculator, e.g. {@link NeedlemanWunschDistance}
     * @param totalNumRecognitions
     * Number of words in all the frames
     *
     * @return
     * The items in the probabilities list, possibly joined with other words (if they were alternatives) and possibly filtered
     * (if they were too unlikely)
     */
    private static Set<Set<StringProbability>> filterAndJoinWords(List<StringProbability> probabilities,
                                                                  StringDistanceCalculator distanceCalculator, int numFrames, int totalNumRecognitions) {
        JoinWords joinWords = new JoinWords();
        FilterWords filterWords = new FilterWords();
        return filterWords.filterWords(joinWords.joinWords(probabilities, distanceCalculator), numFrames, totalNumRecognitions, distanceCalculator);
    }

    /**
     * If a word appears in the list of german words we downloaded, we assume it is far more likely that it actually
     * was in a video. Mostly implemented in {@link CaseInsensitiveDictionary}
     *
     * @param probabilities
     * List of words in the current step.
     * @param pathForDictionaries
     * Path for the dictionary file(s)
     */
    private static void applyOfflineDictionaryBoost(List<StringProbability> probabilities, String pathForDictionaries) {
        CaseInsensitiveDictionary dictionary = new CaseInsensitiveDictionary();
        dictionary.buildFromFile(pathForDictionaries + "german.dic");
        OfflineDictionaryBoost offlineDictionaryBoost = new OfflineDictionaryBoost(dictionary);
        offlineDictionaryBoost.boostAll(probabilities);
    }

    /**
     * If the text recognition gives us a word like "sdfsjafdas" and reognizes it often or suggests many similar words,
     * it might actually have a high enough boost to be considered a viable candidate for a tag. This method looks at
     * distributions of characters in actual german words (from all the german wikipedia articles classified as 'excellent')
     * in order to decide whether or not a word is likely.
     *
     * @param probabilities
     * List of words in the current step.
     * @param pathForDictionaries
     * Path for the dictionary file(s)
     */
    private static void applyNGramProbabilityBoost(List<StringProbability> probabilities, String pathForDictionaries) {
        NGramProbabilityBoost nGramProbabilityBoost = new NGramProbabilityBoost(NGramProbability.buildProbability(true, pathForDictionaries));
        nGramProbabilityBoost.boostAll(probabilities);
    }

    /**
     * Parses the JSON file at the location specified by the parameter.
     * @param fileName
     * Path to a JSON file that the Text Recognition Tool outputs.
     * @return
     * List of Frames in the JSON file.
     */
    private static List<Frame> getFrameListFromFile(String fileName) {
        List<Frame> frameList = new ArrayList<>();

        String fileContents = FileUtils.getFileContents(fileName);
        JSONObject object = new JSONObject(fileContents);
        JSONArray recognitionData = object.getJSONArray("data");

//        for(int i = 0; i < recognitionData.length(); i++) {
//            JSONObject frameWrapper = (JSONObject) recognitionData.get(i);
//            addFramesToList(frameWrapper, frameList);
//        }

        recognitionData.forEach(frameWrapper -> addFramesToList((JSONObject) frameWrapper, frameList));

        return frameList;
    }


    /**
     * Add frame objets in the JSONObject to the list.
     * @param frameWrapper
     * Object that wraps the frame (look at any one of the recognition files to see what that means)
     * @param frameList
     * "output" parameter / will be modified by the method!
     */
    public static void addFramesToList(JSONObject frameWrapper, List<Frame> frameList) {
        for(String key : frameWrapper.keySet()) { // only one key, e.g. "frame_5"
            JSONObject frameJson = frameWrapper.getJSONObject(key);
            Frame frame = new Frame();
            frame.setNumber(Integer.parseInt(key.substring(6)));

            for(String recognitionKey : frameJson.keySet()) {
                JSONObject recognition = frameJson.getJSONObject(recognitionKey);
                frame.addWord(recognition.getString("text"));
            }

            frameList.add(frame);
        }
    }

    /**
     * Writes all the words to the OutputStreamWriter.
     * @param strings List of words
     * @param out where to output the words
     */
    private static void dumpWordsToWriter(List<StringProbability> strings, OutputStreamWriter out) {
        strings.forEach(str -> {
            try {
                out.write(str.getString() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Writes all the words to the PrintStream.
     * @param strings List of words
     * @param out where to output the words
     */
    private static void dumpWordsToStream(List<StringProbability> strings, PrintStream out) {
        strings.forEach(s -> out.println(s.getString()));
    }



}
