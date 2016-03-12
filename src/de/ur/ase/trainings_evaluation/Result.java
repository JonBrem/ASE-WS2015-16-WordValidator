package de.ur.ase.trainings_evaluation;

/**
 * Model class for the trainings evaluation. Contains results (i.e., recognition quality & time) for one
 * configuration.
 */
public class Result {

    private String configuration;
    private float score;
    private float time;

    /**
     * Model class for the trainings evaluation. Contains results (i.e., recognition quality & time) for one
     * configuration.
     */
    public Result(String configuration, float score, float time) {
        this.configuration = configuration;
        this.score = score;
        this.time = time;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }
}
