package de.ur.ase.trainings_evaluation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class IterationData {

    private List<Float> matches;

    private Map<String, Set<String>> recognitionData;

    public IterationData(JSONObject jsonObject) {
        this.matches = new ArrayList<>();
        this.recognitionData = new HashMap<>();

        setupRecognitionData(jsonObject);
    }

    public Set<String> getDataForFrame(String frameId) {
        return recognitionData.get(frameId);
    }

    private void setupRecognitionData(JSONObject jsonObject) {
        JSONArray dataArray = jsonObject.getJSONArray("data");
        for(int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            for(String frame : dataObject.keySet()) {
                addToRecognitionData(frame, dataObject.getJSONObject(frame));
            }
        }
    }

    private void addToRecognitionData(String frame, JSONObject frameData) {
        Set<String> words = new HashSet<>();

        for(String recognitionKey : frameData.keySet()) {
            words.add(frameData.getJSONObject(recognitionKey).getString("text"));
        }

        recognitionData.put(frame, words);
    }

    public float getAverageValue() {
        if(matches.size() == 0) return -1;

        float average = 0;
        for(Float f : matches) average += f;
        return average / matches.size();
    }

    public void addValue(float v) {
        matches.add(v);
    }
}
