package de.ur.ase.trainings_evaluation;

import de.ur.ase.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The GoldStandard contains the actual words that appear in each Frame,
 * as detected and verified by the both of us.
 */
public class GoldStandard {

    /**
     * Creates a Gold Standard object from the specified file.
     */
    public static GoldStandard fromFile(String goldStandardFile) {
        GoldStandard goldStandard = new GoldStandard();
        JSONArray arr = new JSONArray(FileUtils.getFileContents(goldStandardFile));

        buildFromArray(goldStandard, arr);

        return goldStandard;
    }

    /**
     * Puts the JSONArray data in the GoldStandard.
     */
    private static void buildFromArray(GoldStandard goldStandard, JSONArray arr) {
        for(int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String frame = obj.getString("frame");
            JSONArray words = obj.getJSONArray("text");

            Set<String> wordSet = new HashSet<>();

            for(int j = 0; j < words.length(); j++) {
                wordSet.add(words.getString(j));
            }

            goldStandard.add(frame, wordSet);
        }
    }

    private Map<String, Set<String>> frameData;

    /**
     * The GoldStandard contains the actual words that appear in each Frame,
     * as detected and verified by the both of us.
     */
    public GoldStandard() {
        frameData = new HashMap<>();
    }

    /**
     * @return the words in the frame, specified by its key in a map (e.g. frame_450)
     */
    public Set<String> getWordsForFrame(String frame) {
        return frameData.get(frame);
    }

    /**
     * @return a collection of identifiers of the frames that were used in the training.
     */
    public Set<String> getFrameIDs() {
        return frameData.keySet();
    }

    /**
     * Adds the string to the set.
     */
    private void add(String key, Set<String> words) {
        this.frameData.put(key, words);
    }
}
