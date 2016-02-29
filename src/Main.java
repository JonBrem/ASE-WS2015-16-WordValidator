import de.ur.ahci.FileUtils;
import de.ur.ahci.n_gram_probability.NGramProbabilityBoost;
import de.ur.ahci.model.StringProbability;
import de.ur.ahci.WordValidator;
import de.ur.ahci.n_gram_probability.NGramProbability;
import de.ur.ahci.model.Frame;
import de.ur.ahci.offline_dictionary.CaseInsensitiveDictionary;
import de.ur.ahci.offline_dictionary.OfflineDictionaryBoost;
import de.ur.ahci.string_similarity.NeedlemanWunschSimilarity;
import de.ur.ahci.string_similarity.StringSimilarityCalculator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entry point & routine that is executed when the program is started from the command line
 * (that is, the version we "distribute" enters here).
 */
public class Main {

    /**
     * Entry point & routine that is executed when the program is started from the command line
     * (that is, the version we "distribute" enters here).
     */
    public static void main(String[] args) {
        List<Frame> frameList = getFrameListFromFile("output_9.json");
        StringSimilarityCalculator similarityCalculator = new NeedlemanWunschSimilarity();

        WordValidator validator = new WordValidator(frameList, similarityCalculator);
        validator.run();
        List<StringProbability> probabilities = validator.getStringProbabilities();

        applyNGramProbabilityBoost(probabilities);
        applyOfflineDictionaryBoost(probabilities);

//        filterAndJoinWords(probabilities, similarityCalculator);

        findSimilarAndSuperCategoriesOnline(probabilities);

        dumpWordsToConsole(probabilities);
    }

    private static void findSimilarAndSuperCategoriesOnline(List<StringProbability> probabilities) {

    }

    private static void dumpWordsToConsole(List<StringProbability> probabilities) {
        Collections.sort(probabilities, (sp1, sp2) -> Double.compare(sp1.probability, sp2.probability));
        probabilities.forEach(pro -> {
            System.out.println(pro.string + "\t" + pro.probability);
        });
    }

    private static void filterAndJoinWords(List<StringProbability> probabilities, StringSimilarityCalculator similarityCalculator) {
        joinWords(probabilities, similarityCalculator);
        filterWords(probabilities);
    }

    private static void filterWords(List<StringProbability> probabilities) {

    }

    private static void joinWords(List<StringProbability> probabilities, StringSimilarityCalculator similarityCalculator) {
        int index = 0;
        while(index < probabilities.size()) {

            // set up loop
            boolean removeCurrent = false;
            int joinCurrent = -1;
            double strongestMatchProbability = 0;
            double strongestMatchStrength = Double.MAX_VALUE;

            List<Integer> joinToCurrentIndices = new ArrayList<>(); // these will be removed & their likelihoods will be added to the current word
            StringProbability current = probabilities.get(index);
            String currentWord = current.string.toLowerCase();

            // decide if other words will be joined to this word or vice versa
            for(int i = index + 1; i < probabilities.size(); i++) {
                StringProbability other = probabilities.get(i);
                String otherWord = other.string.toLowerCase();

                if(isPartOf(currentWord, otherWord)) {
                    if(other.probability >= 1.2 * current.probability && other.probability > strongestMatchProbability) {
                        strongestMatchStrength = 0;
                        strongestMatchProbability = other.probability;
                        removeCurrent = true;
                        joinCurrent = i;
                    }
                } else if (isPartOf(otherWord, currentWord)) {
                    if(current.probability >= 1.2 * other.probability) {
                        joinToCurrentIndices.add(i);
                    }
                } else {
                    double similarity = getSimilarity(currentWord, otherWord, similarityCalculator);
                    if(similarity < 0.25) {
                        if(other.probability >= 1.2 * current.probability && similarity <= strongestMatchStrength && other.probability > strongestMatchProbability) {
                            strongestMatchStrength = similarity;
                            strongestMatchProbability = other.probability;
                            removeCurrent = true;
                            joinCurrent = i;
                        } else if (current.probability >= 1.2 * other.probability) {
                            joinToCurrentIndices.add(i);
                        }
                    }
                }
            }

            // enact possible joins
            if(removeCurrent) {
//                System.out.println("Removing " + currentWord + ", joining with " + probabilities.get(joinCurrent).string);
//                probabilities.get(joinCurrent).probability += current.probability;
                probabilities.remove(index);
            } else {
                for(int i = 0; i < joinToCurrentIndices.size(); i++) {
//                    current.probability += probabilities.get(i).probability;
                    probabilities.remove(i);
                }
                index++;
            }
        }
    }

    private static double getSimilarity(String current, String other, StringSimilarityCalculator similarityCalculator) {
        return similarityCalculator.getSimilarity(current, other);
    }

    private static boolean isPartOf(String w1, String w2) {
        return w2.contains(w1);
    }

    private static void applyOfflineDictionaryBoost(List<StringProbability> probabilities) {
        CaseInsensitiveDictionary dictionary = new CaseInsensitiveDictionary();
        dictionary.buildFromFile("german.dic");
        OfflineDictionaryBoost offlineDictionaryBoost = new OfflineDictionaryBoost(dictionary);
        offlineDictionaryBoost.boostAll(probabilities);
    }

    private static void applyNGramProbabilityBoost(List<StringProbability> probabilities) {
        NGramProbabilityBoost nGramProbabilityBoost = new NGramProbabilityBoost(NGramProbability.buildProbability(true));
        nGramProbabilityBoost.boostAll(probabilities);
    }

    private static List<Frame> getFrameListFromFile(String fileName) {
        List<Frame> frameList = new ArrayList<>();

        String fileContents = FileUtils.getFileContents(fileName);
        JSONObject object = new JSONObject(fileContents);
        JSONArray recognitionData = object.getJSONArray("data");

        recognitionData.forEach(frameWrapper -> addFramesToList((JSONObject) frameWrapper, frameList));

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
