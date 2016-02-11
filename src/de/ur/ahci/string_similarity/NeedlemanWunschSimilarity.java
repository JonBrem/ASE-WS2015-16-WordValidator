package de.ur.ahci.string_similarity;

import de.ur.ahci.StringSimilarityCalculator;

public class NeedlemanWunschSimilarity implements StringSimilarityCalculator {

    private static double INDEL = 2;
    private static double MATCH = 0;
    private static double MISMATCH = 1;

    private double indel;
    private double match;
    private double mismatch;

    public NeedlemanWunschSimilarity() {
        this.indel = INDEL;
        this.match = MATCH;
        this.mismatch = MISMATCH;
    }

    public NeedlemanWunschSimilarity(double indel, double match, double mismatch) {
        this.indel = indel;
        this.match = match;
        this.mismatch = mismatch;
    }

    @Override
    public double getSimilarity(String word1, String word2) {
        NeedlemanWunschMatrix matrix = new NeedlemanWunschMatrix(word1, word2, indel, match, mismatch);
        matrix.run();
        int longerWordLength = word1.length();
        if(longerWordLength < word2.length()) longerWordLength = word2.length();
        return matrix.getDifference() / longerWordLength;
    }


    private static class NeedlemanWunschMatrix {
        private final double indel;
        private final double match;
        private final double mismatch;
        private double[][] matrix;
        private String word1, word2;

        public NeedlemanWunschMatrix(String word1, String word2, double indel, double match, double mismatch) {
            this.indel = indel;
            this.match = match;
            this.mismatch = mismatch;
            this.word1 = word1;
            this.word2 = word2;
            matrix = new double[word1.length() + 1][word2.length() + 1];
            for(int rowNum = 0; rowNum < word1.length() + 1; rowNum++) {
                matrix[rowNum][0] = rowNum * indel;
            }
            for(int colNum = 0; colNum < word2.length() + 1; colNum++) {
                matrix[0][colNum] = colNum * indel;
            }
        }

        public void run() {
            for(int rowNum = 1; rowNum < word1.length() + 1; rowNum++) {
                for(int colNum = 1; colNum < word2.length() + 1; colNum++) {
                    double topLeft = matrix[rowNum - 1][colNum - 1];
                    if(word1.charAt(rowNum - 1) == word2.charAt(colNum - 1)) {
                        topLeft += match;
                    } else {
                        topLeft += mismatch;
                    }

                    double left = matrix[rowNum][colNum - 1] + indel;
                    double top = matrix[rowNum - 1][colNum] + indel;

                    matrix[rowNum][colNum] = min(topLeft, top, left);
                }
            }
        }

        public double getDifference() {
            return matrix[word1.length()][word2.length()];
        }

        private double min(double... values) {
            double min = values[0];
            for(int i = 1; i < values.length; i++) {
                if(min > values[i]) min = values[i];
            }
            return min;
        }

        public void dump() {
            System.out.print(sameLength("") + sameLength(""));
            for(int colNum = 0; colNum < word2.length(); colNum++) {
                System.out.print(sameLength(word2.charAt(colNum) + ""));
            }
            System.out.println();

            for(int rowNum = 0; rowNum < word1.length() + 1; rowNum++) {
                if(rowNum == 0) System.out.print(sameLength(""));
                else System.out.print(sameLength(word1.charAt(rowNum - 1) + ""));

                for(int colNum = 0; colNum < word2.length()  + 1; colNum++) {
                    System.out.print(sameLength(matrix[rowNum][colNum] + ""));
                }
                System.out.println();
            }
        }

        private String sameLength(String s) {
            while(s.length() < 10) s = " " + s;
            return s;
        }


    }


    public static void main(String... args) {
        NeedlemanWunschMatrix test = new NeedlemanWunschMatrix("gattaca", "gattacaii", INDEL, MATCH, MISMATCH);
        test.run();
        test.dump();

        System.out.println(new NeedlemanWunschSimilarity().getSimilarity("fpvn", "Christoph"));
    }

}
