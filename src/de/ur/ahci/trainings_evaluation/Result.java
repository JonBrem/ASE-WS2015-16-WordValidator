package de.ur.ahci.trainings_evaluation;

public class Result {

    private String configuration;
    private float score;
    private float time;

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
