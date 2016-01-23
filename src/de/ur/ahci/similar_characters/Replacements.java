package de.ur.ahci.similar_characters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Replacements {

    public static List<Replacement> replacements;

    static {
        replacements = new ArrayList<>();

        replacements.add(new Replacement("M", "ITI", 1f));

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
            Matcher m = p.matcher(s);
            if(m.find()) {
                List<SimilarString> matches = new ArrayList<>();
                do {
                    matches.add(new SimilarString(s.substring(0, m.start()) + s2 + s.substring(m.end(), s.length()), probability));
                } while(m.find());
                return matches;
            } else {
                return null;
            }
        }


    }

}
