package de.ur.ase.trainings_evaluation;

import java.util.HashMap;
import java.util.Map;

public class TimeData {

    private Map<String, Float> timeData;

    public TimeData(String fileName) {
        timeData = new HashMap<>();
        String fileContents = TrainingEvaluation.readFile(fileName);

        for(String line : fileContents.split("\n")) {
            String[] parts = line.split("\t");
            String configuration = parts[0];
            float time = Float.parseFloat(parts[1]);

            timeData.put(configuration, time);
        }
    }

    public float getTimeFor(String configuration) {
        return timeData.get(configuration);
    }

}
