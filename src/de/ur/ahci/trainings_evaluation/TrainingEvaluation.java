package de.ur.ahci.trainings_evaluation;

import de.ur.ahci.string_similarity.NeedlemanWunschSimilarity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TrainingEvaluation {

    public static void main(String... args) {
        new TrainingEvaluation("training_gold.json", "training_files", "times.txt", new SimilarityCalculator() {
            @Override
            public float calculateSimilarity(Set<String> recognizedSet, Set<String> goldSet) {
                if(recognizedSet == null) return 0;

                Set<String> lookedAt = new HashSet<>();

                float similarity = 0;

                for(String actualWord : goldSet) {
                    if(recognizedSet.contains(actualWord)) {
                        similarity += 1;
                        lookedAt.add(actualWord);
                    } else {
                        float v = getHighestWordSimilarity(recognizedSet, actualWord, lookedAt);
                        if(v > 0) {
                            similarity += Math.pow(v, 3);
                        }
                    }
                }

                for(String word : recognizedSet) {
                    if(!lookedAt.contains(word)) {
                        similarity -= 0.05;
                    }
                }

                if(similarity < 0) similarity = 0;
                return similarity / goldSet.size();
            }

            private float getHighestWordSimilarity(Set<String> recognizedSet, String actualWord, Set<String> lookedAt) {
                float highestSimilarity = -1 * Float.MAX_VALUE;
                String bestMatch = null;

                for(String word : recognizedSet) {
                    float similarity = getSimilarity(word, actualWord);
                    if(similarity > highestSimilarity) {
                        bestMatch = word;
                        highestSimilarity = similarity;
                    }
                }

                if(highestSimilarity == 0) {
                    return 0;
                }

                lookedAt.add(bestMatch);
                return highestSimilarity;
            }

            private float getSimilarity(String word, String actualWord) {
                double similarity = 1.0 - new NeedlemanWunschSimilarity(1, 0, 1).getSimilarity(word, actualWord);
                return (float) similarity;
            }
        }).run();
    }

    private GoldStandard goldStandard;
    private TimeData timeData;
    private String trainingFilesFolder;

    private SimilarityCalculator similarityCalculator;
    private List<Result> results;


    public TrainingEvaluation(String goldStandardFile, String trainingFilesFolder, String timeDataFile, SimilarityCalculator similarityCalculator) {
        this.goldStandard = GoldStandard.fromFile(goldStandardFile);
        this.trainingFilesFolder = trainingFilesFolder;
        this.timeData = new TimeData(timeDataFile);
        this.similarityCalculator = similarityCalculator;
        this.results = new ArrayList<>();
    }

    public void run() {
        File folder = new File(trainingFilesFolder);
        for(File trainingFile : folder.listFiles()) {
            parseFile(trainingFile);
        }

        Collections.sort(results, new Comparator<Result>() {
            @Override
            public int compare(Result r1, Result r2) {
                int scoreCompare = Float.compare(r1.getScore(), r2.getScore());
                return scoreCompare == 0? Float.compare(r2.getTime(), r1.getTime()) : scoreCompare;
            }
        });

        Collections.reverse(results);

        for(Result r : results) {
            System.out.println(r.getConfiguration() + "\t" + r.getScore() + "\t" + r.getTime());
        }
    }

    private void parseFile(File file) {
        IterationData iterationData = new IterationData(new JSONObject(readFile(file.getAbsolutePath())));

        for(String frameId : goldStandard.getFrameIDs()) {
            forEveryFrameInGoldStandard(iterationData.getDataForFrame(frameId), goldStandard.getWordsForFrame(frameId), iterationData);
        }

        String configuration = file.getName();
        configuration = configuration.substring(0, configuration.indexOf("."));
        Result result = new Result(configuration, iterationData.getAverageValue(), timeData.getTimeFor(configuration));
        results.add(result);
    }

    private void forEveryFrameInGoldStandard(Set<String> dataForFrame, Set<String> goldStandard, IterationData iterationData) {
        iterationData.addValue(similarityCalculator.calculateSimilarity(dataForFrame, goldStandard));
    }

    public interface SimilarityCalculator {
        float calculateSimilarity(Set<String> recognizedSet, Set<String> goldSet);
    }

    /**
     * Utility method for the training evaluation module.
     * @param fileName
     * @return
     */
    public static String readFile(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            StringBuilder b = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                b.append(line).append("\n");
            }

            reader.close();
            return b.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
