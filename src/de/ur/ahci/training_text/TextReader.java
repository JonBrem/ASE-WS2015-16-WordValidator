package de.ur.ahci.training_text;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TextReader {

    public static List<String> readText(String file) throws IOException {
        List<String> words = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(new File(file)));

        String line;
        while((line = reader.readLine()) != null) {
            words.addAll(getWordsFromLine(line));
        }

        return words;
    }

    private static Collection<? extends String> getWordsFromLine(String line) {
        String[] words = line.split(" ");
        return Arrays.asList(words);
    }

}
