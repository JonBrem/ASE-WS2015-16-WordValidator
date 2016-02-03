import de.ur.ahci.WordValidator;
import de.ur.ahci.build_probabilities.NGramProbability;
import de.ur.ahci.model.Frame;
import de.ur.ahci.training_text.TextReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) {
        new WordValidator().run();

    }


    private static NGramProbability buildProbability() {
        NGramProbability probability = new NGramProbability(3);

        File folder = new File("wiki_texts");
        for(File file : folder.listFiles()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while((line = reader.readLine()) != null) {
                    line = line.replaceAll("\\[[a-zA-Z0-9 ]+\\]", "");
                    line = line.toLowerCase();
                    line = line.replaceAll("[^a-zäöüß\\d\\s:]", " ");

                    for(String word : line.split(" ")) {
                        if(word.length() < 3) continue;
                        probability.readWords(word);
                    }

                }

                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return probability;
    }


    private static class JSONFileReader {

        public static List<Frame> getFrames(String json) {
            List<Frame> frames = new ArrayList<>();

            return frames;
        }

    }
}
