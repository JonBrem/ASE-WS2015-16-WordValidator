package de.ur.ahci.similar_characters;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;

public class ReplacementsTest {

    @Test
    public void replacementWorks() {
        Replacements.Replacement replacement = new Replacements.Replacement("abc", "def", 0.7f);
        List<SimilarString> similarStrings =  replacement.replaces("aaabccc");
        assertEquals(similarStrings.get(0).getString(), "aadefcc");
    }

    @Test
    public void noReplacement() {
        Replacements.Replacement replacement = new Replacements.Replacement("abc", "abx", 0.7f);
        assertEquals(replacement.replaces("aadefcc"), null);
    }

    // modify & run this for real output with the actual data

//    @Test
//    public void multiReplacementWorks() {
//        List<SimilarString> similarStrings = Replacements.getReplacedStrings("abcdiefghi");
//        for(SimilarString s : similarStrings) {
//            System.out.println(s.getString() + "\t" + s.getSimilarity());
//        }
//
//    }

}
