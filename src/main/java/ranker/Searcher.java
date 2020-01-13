package ranker;

import model.Model;
import parser.MiniDictionary;
import parser.NamedEntitiesSearcher;
import parser.Parse;
import parser.cDocument;
import rw.Query;
import rw.ReadFile;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Searcher implements Callable<LinkedList<String>> {
    Query query;
    String outputPath;
    boolean enableStemming;
    boolean enableSemantics;
    private static SemanticHandler sh;
    private List<String> docsThatReturned;
    final String postingPath;
    final int DOCS_RETURN_NUMBER = 50;

    public Searcher(Query q, String outputPath, String postingPath, boolean offline, boolean useStemming, boolean useSemantics) {
        this.query = q;
        this.outputPath = outputPath;
        this.postingPath = postingPath;
        this.enableStemming = useStemming;
        this.enableSemantics = useSemantics;
        docsThatReturned = new ArrayList<>();
        sh = new SemanticHandler(offline);
    }

    @Override
    public LinkedList<String> call() {
        try {
            return mainLogic();
        } catch(IOException ignored) {
        }
        return null;
    }

    private LinkedList<String> mainLogic() throws IOException {
        NamedEntitiesSearcher ner = null;
        String queryAfterSem = query.getQueryText();
        LinkedList<String> argsAsLinkedList = new LinkedList<>(Arrays.asList(query.getQueryText().split(" ")));
        if (enableSemantics) queryAfterSem = query.getQueryText()+" "+sh.getTwoBestMatches(argsAsLinkedList);
        CaseInsensitiveMap wordsPosting;
        Parse p = new Parse(new cDocument("", "", "", "", queryAfterSem+" "+query.getQueryDesc()), enableStemming, ner);
        MiniDictionary md = p.parse(true);
        HashMap<String, Integer> wordsCountInQuery = md.countAppearances(); //count word in the query
        wordsPosting = getPosting(wordsCountInQuery.keySet());
        Ranker ranker = new Ranker(wordsCountInQuery);
        HashMap<String, Double> score = new HashMap<>();

        //for each word go throw its posting with relevant documents
        for(String word : wordsCountInQuery.keySet()) {
            if (wordsCountInQuery != null && wordsPosting.get(word) != null)
                if (!wordsPosting.get(word).equals("")) {
                    String postingLine = wordsPosting.get(word);
                    String[] split = postingLine.split("\\|");
                    double idf = getIDF(split.length-1);
                    double weight = 1;
                    if (word.contains("-"))
                        weight = 1.15;
                    for(String aSplit : split) {
                        String[] splitLine = aSplit.split(",");
                        String docName = splitLine[0];
                        if (splitLine.length > 1) {
                            int tf = Integer.parseInt(splitLine[1]);
                            double totalRank = ranker.GetRank(score, wordsPosting.keySet(), docName, word, tf, idf);
                            addToScore(score, docName, totalRank);
                        }
                    }
                }
        }
        return sortByScore(score);
    }

    /**
     * Searcher
     * parseQueryAndReturnRank
     * synForQuery
     * autoComplete(term)
     */


    //Write the results to the file selected by the user
    public void trecEval(String path) throws IOException {
        String addToFile = docsThatReturned.stream().map(s -> "240 "+"0 "+s+" 1 "+"42.38 mt"+"\n").collect(Collectors.joining());
        /////////\n
        BufferedWriter test = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+"/terc_eval_res.txt")));
        test.write(addToFile);
        test.flush();
        test.close();
    }

    private LinkedList<String> sortByScore(HashMap<String, Double> score) {
        List<Map.Entry<String, Double>> list = new ArrayList<>(score.entrySet());
        list.sort(Map.Entry.comparingByValue());

        LinkedList<String> result = new LinkedList<>();
        for(Map.Entry<String, Double> entry : list) {
            result.add(entry.getKey());
        }
        return result;
    }

    private void addToScore(HashMap<String, Double> score, String docName, double newScore) {
        if (newScore != 0) {
            Double d = score.get(docName);
            if (d != null)
                newScore += d;
            score.put(docName, newScore);
        }
    }

    private CaseInsensitiveMap getPosting(Set<String> query) {
        CaseInsensitiveMap words = new CaseInsensitiveMap();
        HashMap<Character, LinkedList<Integer>> allCharactersTogether = new HashMap<>();
        for(String word : query) {
            char letter;
            if (!Character.isLetter(word.charAt(0)))
                letter = '`';
            else
                letter = Character.toLowerCase(word.charAt(0));
            String lineNumber = getPostingLineNumber(word);
            if (!lineNumber.equals("")) {
                if (allCharactersTogether.containsKey(letter))
                    allCharactersTogether.get(letter).add(Integer.parseInt(lineNumber));
                else {
                    LinkedList<Integer> lettersLines = new LinkedList<>();
                    lettersLines.add(Integer.parseInt(lineNumber));
                    allCharactersTogether.put(letter, lettersLines);
                }
            } else
                words.put(word, "");
        }
        for(Character letter : allCharactersTogether.keySet()) {
            LinkedList<String> postings = ReadFile.readPostingLineAtIndex(postingPath, Character.toLowerCase(letter), allCharactersTogether.get(letter), enableStemming);
            for(String posting : postings) {
                String[] wordAndRestOfPosting = posting.split("~");
                words.put(wordAndRestOfPosting[0], wordAndRestOfPosting[1]);
            }
        }
        return words;
    }

    private String getPostingLineNumber(String word) {
        String lineNumber = Model.invertedIndex.getPostingLink(word.toLowerCase());
        if (lineNumber.equals(""))
            lineNumber = Model.invertedIndex.getPostingLink(word.toUpperCase());
        return lineNumber;
    }

    private Double getIDF(int length) {
        double docInCorpusCount = Model.documentDictionary.keySet().size();
        return Math.log10((docInCorpusCount+1) / length);
    }


    public class CaseInsensitiveMap extends HashMap<String, String> {
        @Override
        public String put(String key, String value) {
            return super.put(key.toLowerCase(), value);
        }

        public String get(String key) {
            return super.get(key.toLowerCase());
        }
    }
}


