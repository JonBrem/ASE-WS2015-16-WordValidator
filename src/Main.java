import de.ur.ahci.build_probabilities.BuildNGrams;
import de.ur.ahci.training_text.TextReader;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        try {
            List<String> words = TextReader.readText("lorem_ipsum.txt");
            BuildNGrams nGrams2 = new BuildNGrams(2);
            nGrams2.readWords(words);
            nGrams2.debugDump();

            BuildNGrams nGrams3 = new BuildNGrams(3);
            nGrams3.readWords(words);
            nGrams3.debugDump();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //@todo main routine

        // get candidates for 2-,3-,4-grams (in the right order)
        // eg: O13ERF -> [O1, OB], [13], [BE, 3E], [ER], [EN], [RF, NF, RT, NT]

        // for every tuple: check if previous words have those in a similar order

        // if so: merge & increase likelihood

        // if not: add most likely tuple rows to list of possible words (with relative likelihood)
        // eg: OBERF [top (hopefully)] , OBERT, OBERF
        // BUUUT keep the tuples!! so similar things can be found & there can be corrections!!

        // longer words (if the addendums are SOMEWHAT likely) should be preferred because the endings get cut off / things get cut in the middle

    }
}
