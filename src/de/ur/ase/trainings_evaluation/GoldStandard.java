package de.ur.ase.trainings_evaluation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GoldStandard {

    public static GoldStandard fromFile(String goldStandardFile) {
        GoldStandard goldStandard = new GoldStandard();
        JSONArray arr = new JSONArray(TrainingEvaluation.readFile(goldStandardFile));

        buildFromArray(goldStandard, arr);

        return goldStandard;
    }

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

    public GoldStandard() {
        frameData = new HashMap<>();
    }

    public Set<String> getWordsForFrame(String frame) {
        return frameData.get(frame);
    }

    public Set<String> getFrameIDs() {
        return frameData.keySet();
    }

    private void add(String key, Set<String> words) {
        this.frameData.put(key, words);
    }
}
