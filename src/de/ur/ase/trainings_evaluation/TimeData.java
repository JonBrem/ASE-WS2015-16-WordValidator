package de.ur.ase.trainings_evaluation;

import de.ur.ase.FileUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * The training times were stored in a file; this class contains the data in that file.
 */
public class TimeData {

    private Map<String, Float> timeData;

    /**
     * The training times were stored in a file; this class contains the data in that file.
     */
    public TimeData(String fileName) {
        timeData = new HashMap<>();
        String fileContents = FileUtils.getFileContents(fileName);

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
