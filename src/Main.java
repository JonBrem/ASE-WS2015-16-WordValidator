import de.ur.ahci.WordValidator;
import de.ur.ahci.build_probabilities.NGramProbability;
import de.ur.ahci.model.Frame;
import de.ur.ahci.string_similarity.AdaptedNeedlemanWunschSimilarity;
import de.ur.ahci.string_similarity.EqualitySimilarity;
import de.ur.ahci.string_similarity.LevenshteinSimilarity;
import de.ur.ahci.string_similarity.NeedlemanWunschSimilarity;
import de.ur.ahci.training_text.TextReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) {
        List<Frame> frameList = getFrameListFromFile("output_1.json");
        new WordValidator(frameList, new NeedlemanWunschSimilarity()).run();

    }

    private static List<Frame> getFrameListFromFile(String fileName) {
        List<Frame> frameList = new ArrayList<>();

        String fileContents = getFileContents(fileName);
        JSONObject object = new JSONObject(fileContents);
        JSONArray recognitionData = object.getJSONArray("data");

        recognitionData.forEach(frameWrapper -> addFramesToList((JSONObject) frameWrapper, frameList));

        return frameList;
    }

    private static void addFramesToList(JSONObject frameWrapper, List<Frame> frameList) {
        for(String key : frameWrapper.keySet()) { // only one key, e.g. "frame5"
            JSONObject frameJson = frameWrapper.getJSONObject(key);
            Frame frame = new Frame();
            frame.setNumber(Integer.parseInt(key.substring(5)));

            for(String recognitionKey : frameJson.keySet()) {
                JSONObject recognition = frameJson.getJSONObject(recognitionKey);
                frame.addWord(recognition.getString("text"));
            }

            frameList.add(frame);
        }
    }

    private static String getFileContents(String fileName) {
        StringBuilder fileContents = new StringBuilder();
        forEveryLineInFile(fileName, line -> fileContents.append(line).append("\n"));
        return fileContents.toString();
    }

    private static void forEveryLineInFile(String fileName, Consumer<String> forEveryLine) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while((line = reader.readLine()) != null) {
                forEveryLine.accept(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static NGramProbability buildProbability() {
        NGramProbability probability = new NGramProbability(3);

        File folder = new File("wiki_texts");
        for (File file : folder.listFiles()) {
            forEveryLineInFile(file.getAbsolutePath(), s -> addLineToProbability(probability, s));
        }

        return probability;
    }

    private static void addLineToProbability(NGramProbability probability, String line) {
        line = line.replaceAll("\\[[a-zA-Z0-9 ]+\\]", "");
        line = line.toLowerCase();
        line = line.replaceAll("[^a-zäöüß\\d\\s:]", " ");

        for(String word : line.split(" ")) {
            if(word.length() < 3) continue;
            probability.readWords(word);
        }
    }

    private static class JSONFileReader {

        public static List<Frame> getFrames(String json) {
            List<Frame> frames = new ArrayList<>();

            return frames;
        }

    }
}
