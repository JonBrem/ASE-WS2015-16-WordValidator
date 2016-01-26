package de.ur.ahci.similar_characters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Replacements {

    public static List<Replacement> replacements;

    static {
        replacements = new ArrayList<>();

        replacements.add(new Replacement("M", "ITI", 0.8f));
        replacements.add(new Replacement("N", "I1", 0.4f));

        replacements.add(new Replacement("IE", "BE", 0.1f));

        replacements.add(new Replacement("eut", "elt", 0.05f));

        replacements.add(new Replacement("h", "b", 0.3f));

        replacements.add(new Replacement("m", "in", 0.3f));
        replacements.add(new Replacement("m", "rn", 0.7f));
        replacements.add(new Replacement("m", "ln", 0.2f));


        replacements.add(new Replacement("i", "l", 0.1f));
        replacements.add(new Replacement("l", "I", 0.9f));
        replacements.add(new Replacement("I", "l", 0.9f));
    }

    public static List<SimilarString> getReplacedStrings(String original) {
        List<SimilarString> similarStrings = new ArrayList<>();

        for(Replacement r : replacements) {
            List<SimilarString> similarForReplacement;
            if((similarForReplacement = r.replaces(original)) != null) {
                similarStrings.addAll(similarForReplacement);
            }
        }

        return similarStrings;
    }

    public static class Replacement {
        private String s1;
        private String s2;
        private float probability;
        private Pattern p;

        public Replacement(String s1, String s2, float probability) {
            this.s1 = s1;
            this.s2 = s2;
            this.probability = probability;
            this.p =  Pattern.compile(s1);
        }

        public List<SimilarString> replaces(String s) {
            if(s.length() < s1.length()) return new ArrayList<>();
            String newChars;
            String unChanged;
            if(s.length() == s1.length()) {
                newChars = s.substring(s.length() - s1.length());
                unChanged = "";
            } else {
                newChars = s.substring(s.length() - s1.length());
                unChanged = s.substring(0, newChars.length() + 1);
            }

            Matcher m = p.matcher(newChars); // so that only new combinations will be expanded!
            if(m.find()) {
                List<SimilarString> matches = new ArrayList<>();
                do {
                    matches.add(new SimilarString(unChanged + newChars.substring(0, m.start()) + s2 + newChars.substring(m.end(), newChars.length()), probability));
                } while(m.find());
                return matches;
            } else {
                return null;
            }
        }


    }

}
