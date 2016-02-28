import de.ur.ahci.FileUtils;
import de.ur.ahci.n_gram_probability.NGramProbabilityBoost;
import de.ur.ahci.model.StringProbability;
import de.ur.ahci.WordValidator;
import de.ur.ahci.n_gram_probability.NGramProbability;
import de.ur.ahci.model.Frame;
import de.ur.ahci.offline_dictionary.CaseInsensitiveDictionary;
import de.ur.ahci.offline_dictionary.OfflineDictionaryBoost;
import de.ur.ahci.string_similarity.NeedlemanWunschSimilarity;
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
        List<Frame> frameList = getFrameListFromFile("output_1.json");

        WordValidator validator = new WordValidator(frameList, new NeedlemanWunschSimilarity());
        validator.run();
        List<StringProbability> probabilities = validator.getStringProbabilities();

        applyNGramProbabilityBoost(probabilities);
        applyOfflineDictionaryBoost(probabilities);

        filterAndJoinWords(probabilities);

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

    private static void filterAndJoinWords(List<StringProbability> probabilities) {

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
        for(String key : frameWrapper.keySet()) { // only one key, e.g. "frame5"
            JSONObject frameJson = frameWrapper.getJSONObject(key);
            Frame frame = new Frame();
            frame.setNumber(Integer.parseInt(key.substring(5)));

            for(String recognitionKey : frameJson.keySet()) {
                JSONObject recognition = frameJson.getJSONObject(recognitionKey);
                frame.addWord(recognition.getString("text"));
            }

            frameList.add(frame);
        }
    }



}
