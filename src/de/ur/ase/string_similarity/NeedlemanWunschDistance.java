package de.ur.ase.string_similarity;

/**
 * One of the three DistanceCalculator that were actually implemented
 * (one was just "strings are equal? -> distance = 0, else -> 1" and just for testing).
 * Read up on the NeedlemanWunsch algorithm online (e.g. <a href="https://en.wikipedia.org/wiki/Needleman%E2%80%93Wunsch_algorithmhttps://de.wikipedia.org/wiki/Levenshtein-Distanz">wikipedia</a>)
 * to find out more!
 */
public class NeedlemanWunschDistance implements StringDistanceCalculator {

    private static double INDEL = 2;
    private static double MATCH = 0;
    private static double MISMATCH = 1;

    private double indel;
    private double match;
    private double mismatch;

    /**
     * One of the three DistanceCalculator that were actually implemented
     * (one was just "strings are equal? -> distance = 0, else -> 1" and just for testing).
     * Read up on the NeedlemanWunsch algorithm online (e.g. <a href="https://en.wikipedia.org/wiki/Needleman%E2%80%93Wunsch_algorithmhttps://de.wikipedia.org/wiki/Levenshtein-Distanz">wikipedia</a>)
     * to find out more!
     *
     * Same as {@link NeedlemanWunschDistance(double, double, double)} constructor using the values
     * {@link #INDEL}, {@link #MATCH} and {@link #MISMATCH}.
     */
    public NeedlemanWunschDistance() {
        this.indel = INDEL;
        this.match = MATCH;
        this.mismatch = MISMATCH;
    }

    /**
     * One of the three DistanceCalculator that were actually implemented
     * (one was just "strings are equal? -> distance = 0, else -> 1" and just for testing).
     * Read up on the NeedlemanWunsch algorithm online (e.g. <a href="https://en.wikipedia.org/wiki/Needleman%E2%80%93Wunsch_algorithmhttps://de.wikipedia.org/wiki/Levenshtein-Distanz">wikipedia</a>)
     * to find out more!
     * That also explains what the parameters mean.
     */
    public NeedlemanWunschDistance(double indel, double match, double mismatch) {
        this.indel = indel;
        this.match = match;
        this.mismatch = mismatch;
    }

    /**
     * Calculates the Needleman Wunsch String distance between the two words.
     *
     * @param word1
     * one of the two words.
     * @param word2
     * the other word.
     * @return
     * The Levenshtein Distance between the words, in relation to the longer word's length (0 == equal, 1 == same length,
     * all characters different (just an example), 2 == completely different)
     */
    @Override
    public double getDistance(String word1, String word2) {
        NeedlemanWunschMatrix matrix = new NeedlemanWunschMatrix(word1, word2, indel, match, mismatch);
        matrix.run();
        int longerWordLength = word1.length();
        if(longerWordLength < word2.length()) longerWordLength = word2.length();
        return matrix.getDifference() / longerWordLength;
    }

    /**
     * Helper class to calculate the Needleman-Wunsch-Character-Chain-Similarity between two chains
     */
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

    /**
     * For testing & debugging (not important for the deployed version)
     */
    public static void main(String... args) {
        NeedlemanWunschMatrix test = new NeedlemanWunschMatrix("gattaca", "gattacaii", INDEL, MATCH, MISMATCH);
        test.run();
        test.dump();

        System.out.println(new NeedlemanWunschDistance().getDistance("WEINBERG", "WNBERG"));
    }

}
