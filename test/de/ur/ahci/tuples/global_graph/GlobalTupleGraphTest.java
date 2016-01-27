package de.ur.ahci.tuples.global_graph;

import de.ur.ahci.build_probabilities.NGramProbability;
import de.ur.ahci.tuples.tuple_graph.TupleGraph;
import de.ur.ahci.tuples.tuple_graph.TupleGraphUtil;
import org.junit.Test;

import java.io.*;
import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class GlobalTupleGraphTest {

    @Test
    public void testGlobalGraph() {
        String[] strings = {
                "tvode","hvo","LUL","fl","hvo","OOOOI","LDL","JFABLAB","FabLah","Bayremhe","Yomettln","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","OIOIC","LUL","FabLab","Bayremhe","Yomettln","Soybaba","LA","OBERFRANKEN","AKTUELL","tvode","hvo","OBERFR","Ffafl","ober","LUL","IVO","LUL","fIV0","LUL","Vs","FahLah","Bayrellhe","Yomettm","Soybaba","FABEAB","OIERFRANKEN","AKTUELL","nro","rarO","fl","LUL","LUL","LUL","OOOII","rrc","WIMJ","BERFRHI","DID","LUL","IIU","LUL","IVO","AIL","LUL","FabLah","Bayremhe","Yomettm","Soybaba","FA7B","LA","OIERFRANKEN","AKTUELL","Fran","FABLAB","00001","rib","X1","FabLab","Bayremh","Yomettm","Soybaba","FABLAB","OIERFNANKEN","AKTUELL","LUL","tvode","hvo","FabLah","Bayrelthe","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","LUL","IND","FABLIAB","OBERFRANKEN","AKTUELL","FABLAB","SPLASH","SQUAD","Umm","LUL","LUL","Ken","Ffah","ober","IID","nu","xiv","LUL","IWD","IVO","LUL","FabLah","Bayremhe","Yomettm","Soybaba","FABZHAB","OIEIIFRANKEN","AKTUELL","IND","smxnwuml","fl","are","smsn","SIJIW","LUL","IND","FabLah","Bayrelthe","Yomettm","Soybaba","FABEJAB","OIERFRANKEN","AKTUELL","tvode","hvo","LUL","FabLah","Bayrelthe","Yomettm","Soybaba","FABLAB","OBERFRANKEN","AKTUELL","OOOOI","LUL","FabLah","Bayrelthe","Yomettm","Soybaba","OIEIIFRANKEN","AKTUELL","LUL","IND","tnp","Fran","OOOOI","LUL","IOOOI","FabLah","Bayrelthe","Yomettm","Soybaba","FXWLAB","OBERFRANKEN","AKTUELL","LUL","FabLah","Beyrelthe","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","FabLah","Bnyrelthe","Yomettln","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","FabLah","Bayremh","Yomettm","Soybaba","FABLAB","OEERFRANKEN","AKTUELL","LDL","FabLab","Bayremhe","Yomettm","Soybaba","FABLAB","OBERFRANKEN","AKTUELL","LUL","IOOII","IPD","flb","LanDC","rib","LDL","FabLah","Bayrelthe","Yomettln","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","smsn","SIIIW","FabLah","Bayrelthe","Yomettm","Soybaba","FABEAB","OIERFRANKEN","AKTUELL","hvo","FabLah","Bayrellhe","Yomettm","Soybaba","FABLIAB","OIERFRANKEN","AKTUELL","IIVO","IPD","LDL","TWO","LUL","Ilrg","xA1t","LUL","LUL","Fran","KEI1","ober","OBERFRBTIKEI1","Fran","ober","nu","IUD","xiv","FabLab","Bayremh","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","LUL","LUL","fl","LDL","LUL","FabLah","sayreuua","Yomettm","Soybaba","FABLAB","OEERFRANKEN","AKTUELL","D100","unto","fl","FTBJT","OBERFRBHKEI1","Ffah","ober","nah","FabLah","Buyrelthe","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","LUL","Kefl","FabLah","Bayrellhe","Yomettm","Soybaba","FABILAB","OBEIIFRANKEN","AKTUELL","FabLah","Bayrelthe","Yomettm","Soybaba","FABEIAB","OIEIIFRANKEN","AKTUELL","LUL","FABLAB","FABLAB","LUL","LUL","FabLah","Bayremhe","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","hulz","FabLahBayreI1he","Yomettm","Soybaba","FABLLAB","OIERFRANKEN","AKTUELL","ober","rlb","FabLah","Beyremhe","Yomettm","Soybaba","FABLAB","OBERFRANKEN","AKTUELL","LUL","FabLah","Bayrellhe","Yomettln","Soybaba","FABLIAB","OBERFRANKEN","AKTUELL","LUL","FABLAB","17al11lLl1E3I1tRELll1DLlCHES","OBERFRRHKEI1","Fran","ober","winr","Oi","IIVO","LUL","xVeT","UWM","rg","FabLah","Bayremhe","Yomettm","Soybaba","FABEJAB","OBERFRANKEN","AKTUELL","3PlAsII","OBERFRBHKEI1","Ken","Fran","ober","flb","FabLah","Bayrelthe","Yomettm","Soybaba","OEERFRANKEN","AKTUELL","LDL","LUL","LlEl1Lan","VII","fr0","IIVO","fl","LUL","Ffafl","LUL","jFABLAB","OIERFRANKEN","AKTUELL","rvo","LUL","nah","FabLah","Buyrellhe","Yomettm","Soybaba","FABHAB","OIERFRANKEN","AKTUELL","tvode","hvo","FABEIAB","LDL","Fran","ober","LUL","LUL","LUL","oaeRm3V1EKen","Fran","Ken","ober","FABLAB","SIHMII","IVO","LUL","LUL","LUL","OBERFRITIKEH","Fran","ober","IVO","IWD","IID","LUL","LDL","LDL","HTIKETI","Jo","LUL","FabLah","Bayrellhe","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","uy75","FABLAB","FabLah","Boyrelthe","Yomettm","Soybaba","FABAAB","OIERFRANKEN","AKTUELL","FABLAB","AKTUELL","LUL","llllunwnr","OBERFRBHKEI1","Fran","ober","LUL","SPUISH","IUD","fIVO","I19","FABLAB","AKTUELL","LUL","FabLah","Bnyrelthe","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","fjr","FabLahBayreI1he","Yomettm","Soybaba","FABEAB","OBEIIFRANKEN","AKTUELL","IID","FABUAB","IND","LDL","LUL","FRHFIKE","LUL","LUL","FABLIAB","FabLah","Bayrelthe","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","UUL","fl","IID","LUL","LUL","Ken","Fran","ober","FABLAB","LUL","IID","Lat","OBERF","lyOI","HES","OBERFREFIKEI1","rran","ober","oaoan","LUL","FabLah","Bayremhe","Yomettm","Soybaba","FABLAB","OIEIIFRANKEN","AKTUELL","lerl","Land","LDL","FABLVAB","LUL","IPD","IIVQ","LUL","LUL","mIILIenl","IPD","yfi3","hvo","FABLAB","IIVQ","LUL","FabLab","Bnyrellhe","Yomettm","Soybaba","FA","BLAB","OBERFRANKEN","AKTUELL","FabLah","Bnyrellhe","Yomettm","Soybaba","FABLAB","OBERFRANKEN","AKTUELL","rg","LUL","lllIu116Tnr","LDL","lllllumurbr","FabLahBayreI1he","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","In","nu","LUL","00A1t","LUL","IPD","Colo","90A1t","FABLAB","AKTUELL","OIIERFRANKEN","LUL","tvode","hvo","NER","FABLAB","IID","LUL","lllviuwrxr","UUL","FE","HTILIETIFREUDDLICHES","OBERFREFIKE","rran","ober","LDL","FabLah","Bayrellhe","Yomettm","Soybaba","FABLAB","OBERFRANKEN","AKTUELL","jFABLAB","OBERFRBHKEI1","Fran","ober","nrg","LUL","IND","SPLASH","nub","FabLah","Bayrelthe","Yomettm","Soybaba","FABEAB","OIEIIFRANKEN","AKTUELL","FABLAB","LUL","Ff","an","Illlhanlmr","LUL","FABDAB","FABLAB","rjr","LUL","LUL","SPMSM","SHUMJ","FabLab","Bayremhe","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","FabLah","Bayremhe","Yomettm","Soybaba","FABEAB","OBERFRANKEN","AKTUELL","ES","OBERFREDKEFI","Fran","KEN","ober","F3lTIILT8i1FRELlI1DLlCHE5","OBERFRRTIKEI1","Fran","ober","LDL","nonofnnan","FABLAB","8PlA8ll","SIJIMII","HKEI1","fur","IND","IND","FABLAB","OBERFRANKEN","FabLah","Bayrelthe","Yomettm","Soybaba","OIERFRANKEN","AKTUELL","FABLAB","1llIlM1lITnr","IVO","LUL","LDL","FabLab","Bnyrellhe","Yomettm","Soybaba","FABLAB","OIERFRANKEN","AKTUELL","rib","IUD"
        };

        setupGlobalTupleGraph(strings);

        List<StringProbability> probabilityList = GlobalTupleGraph.getProbableStrings(buildProbability());

        Iterator<StringProbability> iterator = probabilityList.iterator();
        while(iterator.hasNext()) {
            StringProbability p = iterator.next();
            if(p.getProbability() < 2) iterator.remove();
        }

        int index = 0;
        while(true) {
            List<Integer> removeIndices = new ArrayList<>();
            int addTo = -1;
            int longestAddTo = -1;

            StringProbability item = probabilityList.get(index);

            for(int i = 0; i < probabilityList.size(); i++) {
                if(i == index) continue;
                StringProbability another = probabilityList.get(i);

                if(another.getString().startsWith(item.getString()) && longestAddTo < another.getString().length()) {
                    addTo = i;
                    longestAddTo = another.getString().length();
                    break;
                } else if (item.getString().startsWith(another.getString())) {
                    removeIndices.add(i);
                }
            }

            if(addTo == -1) {
                Collections.sort(removeIndices);
                Collections.reverse(removeIndices);
                for(int removeIndex : removeIndices) {
                    item.setProbability(item.getProbability() + probabilityList.get(removeIndex).getProbability());
                    probabilityList.remove(removeIndex);
                }
                index++;
            } else {
                probabilityList.get(addTo)
                        .setProbability(probabilityList.get(addTo).getProbability() + probabilityList.get(index).getProbability());
                probabilityList.remove(index);
            }

            if(index >= probabilityList.size()) break;
        }
        Collections.sort(probabilityList);
        Collections.reverse(probabilityList);

        for(StringProbability p : probabilityList) System.out.println(p.getProbability() + "\t" + p.getString());
    }

    private void setupGlobalTupleGraph(String[] strings) {
        int total = strings.length;
        for (String string : strings) {
            if (string.length() < 3) continue;

            boolean containsNumber = false,
                    containsCharacter = false;
            for(char c : string.toCharArray()) {
                if(Character.isAlphabetic(c)) containsCharacter = true;
                if(Character.isDigit(c)) containsNumber = true;
            }
            if(containsCharacter && containsNumber) continue;

            TupleGraph t = TupleGraphUtil.constructGraph(string, 3);
            GlobalTupleGraph.add(t.start);
        }
    }

    private NGramProbability buildProbability() {
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

}
