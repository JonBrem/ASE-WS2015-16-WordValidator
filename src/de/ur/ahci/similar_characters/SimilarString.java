package de.ur.ahci.similar_characters;

public class SimilarString {

    private String string;
    private float similarity;

    public SimilarString() {

    }

    public SimilarString(String string, float similarity) {
        this.string = string;
        this.similarity = similarity;
    }

    public float getSimilarity() {
        return similarity;
    }

    public SimilarString setSimilarity(float similarity) {
        this.similarity = similarity;
        return this;
    }

    public String getString() {
        return string;
    }

    public SimilarString setString(String string) {
        this.string = string;
        return this;
    }
}
