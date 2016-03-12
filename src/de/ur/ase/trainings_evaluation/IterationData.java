package de.ur.ase.trainings_evaluation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Data for one Iteration (i.e., one configuration of the text recognition tool) of the evaluation +
 * evaluation results for that iteration.
 */
public class IterationData {

    private List<Float> matches;

    private Map<String, Set<String>> recognitionData;

    /**
     * Data for one Iteration (i.e., one configuration of the text recognition tool) of the evaluation +
     * evaluation results for that iteration.
     *
     * @param jsonObject
     * JSON created from the file the text recognition tool created
     */
    public IterationData(JSONObject jsonObject) {
        this.matches = new ArrayList<>();
        this.recognitionData = new HashMap<>();

        JSONArray dataArray = jsonObject.getJSONArray("data");
        for(int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObject = dataArray.getJSONObject(i);
            for(String frame : dataObject.keySet()) {
                addToRecognitionData(frame, dataObject.getJSONObject(frame));
            }
        }
    }

    /**
     * @return returns the text that was recognized with the current configuration for the given frame
     */
    public Set<String> getDataForFrame(String frameId) {
        return recognitionData.get(frameId);
    }

    /**
     * Adds the set of words contained in the JSONObject to {@link #recognitionData} with the key
     * specified by the String
     */
    private void addToRecognitionData(String frame, JSONObject frameData) {
        Set<String> words = new HashSet<>();

        for(String recognitionKey : frameData.keySet()) {
            words.add(frameData.getJSONObject(recognitionKey).getString("text"));
        }

        recognitionData.put(frame, words);
    }

    /**
     * @return
     * the average quality or recall (or what you want to call the "success rate of the recognition") for the given
     * Iteration
     */
    public float getAverageValue() {
        if(matches.size() == 0) return -1;

        float average = 0;
        for(Float f : matches) average += f;
        return average / matches.size();
    }

    /**
     * adds the value to
     */
    public void addValue(float v) {
        matches.add(v);
    }
}
