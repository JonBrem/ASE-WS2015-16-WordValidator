package de.ur.ase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Utility class containing some methods for parsing files that are used in multiple places.
 */
public class FileUtils {

    /**
     * Returns the contents of the file as a String.
     * @param fileName the file's path
     */
    public static String getFileContents(String fileName) {
        StringBuilder fileContents = new StringBuilder();
        forEveryLineInFile(fileName, line -> fileContents.append(line).append("\n"));
        return fileContents.toString();
    }

    /**
     * Calls the consumer for every line in the specified file.
     */
    public static void forEveryLineInFile(String fileName, Consumer<String> forEveryLine) {
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


}
