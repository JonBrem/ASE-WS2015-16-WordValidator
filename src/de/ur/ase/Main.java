package de.ur.ase;

import de.ur.ase.join_filter.FilterWords;
import de.ur.ase.join_filter.JoinWords;
import de.ur.ase.n_gram_probability.NGramProbabilityBoost;
import de.ur.ase.model.StringProbability;
import de.ur.ase.n_gram_probability.NGramProbability;
import de.ur.ase.model.Frame;
import de.ur.ase.offline_dictionary.CaseInsensitiveDictionary;
import de.ur.ase.offline_dictionary.OfflineDictionaryBoost;
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

        if(args.length == 0) {
            pathForDictionaries = "";
            filePath = "output_14.json";
        } else {
            pathForDictionaries = args[0] + "/";
            filePath = args[1];
        }

        List<Frame> frameList = getFrameListFromFile(filePath);
        int totalNumRecognitions = getTotalNumRecognitions(frameList);
        StringDistanceCalculator similarityCalculator = new NeedlemanWunschDistance();

        WordValidator validator = new WordValidator(frameList, similarityCalculator);
        validator.run();
        List<StringProbability> probabilities = validator.getStringProbabilities();

        removeStopwords(probabilities);

        applyNGramProbabilityBoost(probabilities, pathForDictionaries);
        applyOfflineDictionaryBoost(probabilities, pathForDictionaries);

        Set<Set<StringProbability>> mostLikelyWords = filterAndJoinWords(probabilities, similarityCalculator, frameList.size(), totalNumRecognitions);

        verifyOnline(mostLikelyWords, args.length == 3? args[2] : null, totalNumRecognitions);
    }

    private static void removeStopwords(List<StringProbability> probabilities) {
        Iterator<StringProbability> iter = probabilities.iterator();
        while(iter.hasNext()) {
            if(Stopwords.isStopword(iter.next().string)) iter.remove();
        }
    }

    private static int getTotalNumRecognitions(List<Frame> frameList) {
        int totalNumRecognitions = 0;
        for(Frame f : frameList) {
            totalNumRecognitions += f.getWords().size();
        }
        return totalNumRecognitions;
    }

    private static void dumpWordsToWriter(Collection<String> probabilities, OutputStreamWriter out) {
        probabilities.forEach(str -> {
            try {
                out.write(str + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void verifyOnline(Set<Set<StringProbability>> probabilities, String file, int totalNumRecognitions) {
        new OnlineLookup().lookupOnline(probabilities, totalNumRecognitions, strings -> {
            if(file != null) {
                try {
                    FileWriter out =  new FileWriter(new File(file));
                    dumpWordsToWriter(strings, out);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                dumpWordsToStream(strings, System.out);
            }
        });
    }

    private static void dumpWordsToStream(Collection<String> strings, PrintStream out) {
        strings.forEach(out::println);
    }

    private static Set<Set<StringProbability>> filterAndJoinWords(List<StringProbability> probabilities,
                                                                  StringDistanceCalculator similarityCalculator, int numFrames, int totalNumRecognitions) {
        JoinWords joinWords = new JoinWords();
        FilterWords filterWords = new FilterWords();
        return filterWords.filterWords(joinWords.joinWords(probabilities, similarityCalculator), numFrames, totalNumRecognitions);
    }


    private static void applyOfflineDictionaryBoost(List<StringProbability> probabilities, String pathForDictionaries) {
        CaseInsensitiveDictionary dictionary = new CaseInsensitiveDictionary();
        dictionary.buildFromFile(pathForDictionaries + "german.dic");
        OfflineDictionaryBoost offlineDictionaryBoost = new OfflineDictionaryBoost(dictionary);
        offlineDictionaryBoost.boostAll(probabilities);
    }

    private static void applyNGramProbabilityBoost(List<StringProbability> probabilities, String pathForDictionaries) {
        NGramProbabilityBoost nGramProbabilityBoost = new NGramProbabilityBoost(NGramProbability.buildProbability(true, pathForDictionaries));
        nGramProbabilityBoost.boostAll(probabilities);
    }

    private static List<Frame> getFrameListFromFile(String fileName) {
        List<Frame> frameList = new ArrayList<>();

        String fileContents = FileUtils.getFileContents(fileName);
        JSONObject object = new JSONObject(fileContents);
        JSONArray recognitionData = object.getJSONArray("data");

        for(int i = 0; i < recognitionData.length(); i++) {
            JSONObject frameWrapper = (JSONObject) recognitionData.get(i);
            addFramesToList(frameWrapper, frameList);
        }

//        recognitionData.forEach(frameWrapper -> addFramesToList((JSONObject) frameWrapper, frameList));

        return frameList;
    }


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



}
