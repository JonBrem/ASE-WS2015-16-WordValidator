package de.ur.ahci.string_similarity;

import de.ur.ahci.StringSimilarityCalculator;

public class AdaptedNeedlemanWunschSimilarity implements StringSimilarityCalculator {

    private static double INDEL = 1;

    @Override
    public double getSimilarity(String word1, String word2) {

        AdaptedNeedlemanWunschMatrix matrix = new AdaptedNeedlemanWunschMatrix(word1, word2);
        matrix.run();
        int longerWordLength = word1.length();
        if(longerWordLength < word2.length()) longerWordLength = word2.length();
        return matrix.getDifference() / longerWordLength;

    }


    private static class AdaptedNeedlemanWunschMatrix {
        private double[][] matrix;
        private String word1, word2;

        public AdaptedNeedlemanWunschMatrix(String word1, String word2) {
            this.word1 = word1;
            this.word2 = word2;
            matrix = new double[word1.length() + 1][word2.length() + 1];
            for(int rowNum = 0; rowNum < word1.length() + 1; rowNum++) {
                matrix[rowNum][0] = rowNum * INDEL;
            }
            for(int colNum = 0; colNum < word2.length() + 1; colNum++) {
                matrix[0][colNum] = colNum * INDEL;
            }
        }

        public void run() {
            for(int rowNum = 1; rowNum < word1.length() + 1; rowNum++) {
                for(int colNum = 1; colNum < word2.length() + 1; colNum++) {
                    double topLeft = matrix[rowNum - 1][colNum - 1];
                    topLeft += (1 - degreeOfMatch(word1.charAt(rowNum - 1), word2.charAt(colNum - 1)));

                    double left = matrix[rowNum][colNum - 1] + INDEL;
                    double top = matrix[rowNum - 1][colNum] + INDEL;

                    matrix[rowNum][colNum] = min(topLeft, top, left);
                }
            }
        }

        private double degreeOfMatch(char c1, char c2) {
            if(c1 == c2) return 1;
            if(checkCharsAre(c1, c2, 'i', 'l')) return 0.9;
            if(checkCharsAre(c1, c2, 'I', 'l')) return 0.9;
            if(checkCharsAre(c1, c2, 'r', 'n')) return 0.8;
            if(checkCharsAre(c1, c2, 'q', 'g')) return 0.7;
            if(checkCharsAre(c1, c2, 'y', 'g')) return 0.6;
            if(checkCharsAre(c1, c2, 'y', 'q')) return 0.6;
            if(checkCharsAre(c1, c2, 'j', 'k')) return 0.3;


            return 0;
        }

        private boolean checkCharsAre(char c1, char c2, char val1, char val2) {
            if(c1 == val1 && c2 == val2) return true;
            if(c1 == val2 && c2 == val1) return true;
            return false;
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
                    System.out.print(sameLength(round(matrix[rowNum][colNum]) + ""));
                }
                System.out.println();
            }
        }

        private double round(double v) {
            return ((double) ((int) (v * 1000))) / 1000.0;
        }

        private String sameLength(String s) {
            while(s.length() < 10) s = " " + s;
            if(s.length() > 10) s = s.substring(0, 10);
            return s;
        }


        public double getDifference() {
            return matrix[word1.length()][word2.length()];
        }
    }


    public static void main(String... args) {
        AdaptedNeedlemanWunschMatrix test = new AdaptedNeedlemanWunschMatrix("i", "l");
        test.run();
        test.dump();
    }

}
