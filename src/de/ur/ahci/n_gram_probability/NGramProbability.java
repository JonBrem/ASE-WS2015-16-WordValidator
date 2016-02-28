package de.ur.ahci.n_gram_probability;

import de.ur.ahci.FileUtils;

import java.io.*;
import java.util.*;

/**
 * An NGramProbability defines how likely it is for a certain string with N characters to occur.
 * The version we use uses 3 letters (since smaller character chains are not likely to be important
 * (not based on evidence)).
 */
public class NGramProbability {

    /**
     * @param fromSavedFile
     * Should be set to true when re-using the same probability as before.
     * This is distributed with the nGramProbability built from the top articles in the German Wikipedia and the
     * file for these is created.<br>
     * If it is set to false, the nGramProbability will be created by reading text from files and counting
     * how often every chain of 3 characters occurs (which takes way longer; should only be done once, when the
     * list of files or some configuration changes).
     * @return
     * A ready-to-use NGramProbability
     */
    public static NGramProbability buildProbability(boolean fromSavedFile) {
        NGramProbability prob;
        if(fromSavedFile) {
            prob = new NGramProbability(3);
            prob.readFromFile("ngramProbability_german.txt");
        } else {
            prob = buildProbabilityFromFilesInFolder("wiki_texts");
        }
        return prob;
    }

    /**
     * The nGramProbability will be created by reading text from files and counting
     * how often every chain of 3 characters occurs (which takes way longer than building it from a "dump" created using
     * the "dumpToFile" method; should only be done once, when the
     * list of files or some configuration changes).
     * @param folderName
     * Every file in this folder will be added to the NGramProbability
     * @return
     * A ready-to-use NGramProbability.
     */
    private static NGramProbability buildProbabilityFromFilesInFolder(String folderName) {
        NGramProbability probability = new NGramProbability(3);

        File folder = new File(folderName);
        for (File file : folder.listFiles()) {
            FileUtils.forEveryLineInFile(file.getAbsolutePath(), s -> addLineToProbability(probability, s));
        }
        return probability;
    }

    /**
     * Modifies the line according to some rules (removes every non-alphanumeric & non-whitespace character);
     * takes every word from the line and adds its nGrams to the NGramProbability
     * @param probability
     * the NGramProbability that is being built
     * @param line
     * Any string (not null)
     */
    private static void addLineToProbability(NGramProbability probability, String line) {
        line = line.replaceAll("\\[[a-zA-Z0-9 ]+\\]", "");
        line = line.toLowerCase();
        line = line.replaceAll("[^a-zäöüß\\d\\s]", " ");

        for(String word : line.split(" ")) {
            if(word.length() < 3) continue;
            probability.readWords(word);
        }
    }

    private int nGramLength;
    private Map<String, Integer> numOccurences;
    private long total;

    /**
     * An NGramProbability defines how likely it is for a certain string with N characters to occur.
     * The version we use uses 3 letters (since smaller character chains are not likely to be important
     * (not based on evidence)).
     */
    public NGramProbability(int nGramLength) {
        this.nGramLength = nGramLength;
        this.numOccurences = new HashMap<>();
        total = 0;
    }

    /**
     * Adds the NGrams in the word to this NGramProbability
     * @param words
     * any number of Strings
     */
    public void readWords(String... words) {
        for(String word : words) {
            parseWord(word);
        }
    }

    /**
     * Adds the NGrams in the word to this NGramProbability
     * @param words
     * any number of Strings
     */
    public void readWords(Collection<String> words) {
        words.forEach(this::parseWord);
    }

    /**
     * Adds all the NGrams in the word to this NGramProbability (case-insensitive / lowercase)
     * @param word
     * Any String (not null)
     */
    private void parseWord(String word) {
        word = word.toLowerCase();
        if(word.length() >= nGramLength) {
            for(int i = 0; i < word.length() - nGramLength + 1; i++) {
                String nGram = word.substring(i, i + nGramLength);

                if(!numOccurences.containsKey(nGram)) {
                    numOccurences.put(nGram, 1);
                } else {
                    numOccurences.put(nGram, numOccurences.get(nGram) + 1);
                }
                total++;
            }
        }
    }

    /**
     * Returns how often the specified nGram was found in the Collection that this NGramProbability was based on divided by
     * the number of NGrams it is based on.
     * @param ngram any String of the length of this NGramProbability (others won't be found and will return 0)
     * @return The probability of the nGram in the Collection that this NGramProbability was based on.
     */
    public double getProbability(String ngram) {
        if(numOccurences.containsKey(ngram)) {
            return numOccurences.get(ngram) / (double) total;
        } else {
            return 0;
        }
    }

    /**
     * Returns how often the specified nGram was found in the Collection that this NGramProbability was based on.
     * @param ngram any String of the length of this NGramProbability (others won't be found and will return 0)
     * @return The probability of the nGram in the Collection that this NGramProbability was based on.
     */
    public int getNumOccurences(String ngram) {
        if(numOccurences.containsKey(ngram)) {
            return numOccurences.get(ngram);
        } else {
            return 0;
        }
    }

    /**
     * Sorts the NGrams in the Collection alphabetically and prints them and their likelihood to the file
     * so that it can be loaded faster in the future.
     * @param file Path to output file.
     */
    public void dumpToFile(String file) {
        class StringAndInt {
            public String string;
            public int num;

            public StringAndInt(String str, int n) {
                this.string = str;
                this.num = n;
            }
        }

        try {
            List<StringAndInt> list = new ArrayList<>();

            for(String key : numOccurences.keySet()) {
                list.add(new StringAndInt(key, numOccurences.get(key)));
            }

            Collections.sort(list, (stringAndInt, t1) -> stringAndInt.string.compareTo(t1.string));

            FileWriter writer = new FileWriter(file);
            for(StringAndInt stringAndInt : list) {
                writer.write(stringAndInt.string + "\t" + stringAndInt.num + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Quickly loads the NGramProbability from a file, likely created by the {@link #dumpToFile(String)} method.
     * @param file Path to a saved file.
     */
    public void readFromFile(String file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                numOccurences.put(parts[0], Integer.parseInt(parts[1]));
                total += Integer.parseInt(parts[1]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the total number of NGrams in the Collection.
     * @return the total number of NGrams in the Collection.
     */
    public long getTotal() {
        return total;
    }
    /**
     * Returns the average probability of the NGrams in the Collection.
     * @return the average probability of the NGrams in the Collection.
     */
    public double averageProbability() {
        return (double) total / numOccurences.keySet().size();
    }
}
