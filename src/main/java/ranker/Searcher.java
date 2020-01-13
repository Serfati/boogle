package ranker;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import java.util.stream.IntStream;

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

    /**
     * This method proved a quicker implemtion of the str.split() method.
     * Used to improve performances.
     */
    public static String[] split(String str, String delimter) {
        ArrayList<String> splittedData = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(str, delimter);
        while(tokenizer.hasMoreTokens()) {
            splittedData.add(tokenizer.nextToken());
        }
        String[] splitResult = new String[splittedData.size()];
        return splittedData.toArray(splitResult);
    }

    public static Object getRecord() {
        return null;
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
        Parse parser;
        String queryAfterSem;
        LinkedList<String> argsAsLinkedList = new LinkedList<>(Arrays.asList(query.getQueryText().split(" ")));
        if (enableSemantics) queryAfterSem = query.getQueryText()+" "+sh.getTwoBestMatches(argsAsLinkedList);
        parser = new Parse(new cDocument("", "", "", "", query.getQueryText()), enableStemming, ner);
        MiniDictionary md = parser.parse();
        HashMap<String, Integer> wordsCountInQuery = md.countAppearances(); //count word in the query
        CaseInsensitiveMap wordsPosting = getPosting(wordsCountInQuery.keySet());

        //objects for the iteration
        Ranker ranker = new Ranker(wordsCountInQuery, 200);
        HashMap<String, Double> score = new HashMap<>();

        //for each word go throw its posting with relevant documents
        for(String word : wordsCountInQuery.keySet()) {
            if (!wordsPosting.get(word).equals("")) {


                addToScore(score, "docName", 0.0);
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

    private CaseInsensitiveMap getPosting(Set<String> query) {
        CaseInsensitiveMap words = new CaseInsensitiveMap();
        HashMap<Character, LinkedList<Integer>> allCharactersTogether = new HashMap<>();
        for(String word : query) {
            char letter = !Character.isLetter(word.charAt(0)) ? '`' : Character.toLowerCase(word.charAt(0));
            String lineNumber = Model.invertedIndex.getPostingLink(word.toLowerCase());
            if (lineNumber.equals(""))
                lineNumber = Model.invertedIndex.getPostingLink(word.toUpperCase());
            if (!lineNumber.equals("")) if (allCharactersTogether.containsKey(letter))
                allCharactersTogether.get(letter).add(Integer.parseInt(lineNumber));
            else {
                LinkedList<Integer> lettersLines = new LinkedList<>();
                lettersLines.add(Integer.parseInt(lineNumber));
                allCharactersTogether.put(letter, lettersLines);
            }
            else
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


    public LinkedList<String> returnOnlyFifty(LinkedList bigList) {
        while(bigList.size() > DOCS_RETURN_NUMBER) bigList.remove(DOCS_RETURN_NUMBER);
        List<Object> list = new ArrayList();
        for(int i = 0; i < bigList.size() && i < DOCS_RETURN_NUMBER; i++) list.add(bigList.get(i));
        IntStream.range(0, list.size()).forEach(i -> docsThatReturned.add(list.get(i).toString().split("=")[0]));
        return (LinkedList<String>) docsThatReturned;
    }

    //Write the results to the file selected by the user
    public void writeResultsToDisk(String path) throws IOException {
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
        return list.stream().map(Map.Entry::getKey).collect(Collectors.toCollection(LinkedList::new));
    }

    private void addToScore(HashMap<String, Double> score, String docName, double newScore) {
        if (newScore != 0) {
            Double d = score.get(docName);
            if (d != null)
                newScore += d;
            score.put(docName, newScore);
        }
    }

    private Double getIDF(int length) {
        double docInCorpusCount = Model.documentDictionary.keySet().size();
        return Math.log10((docInCorpusCount+1) / length);
    }

    public static class ShowResultRecord {
        private StringProperty documentProperty;
        private StringProperty topFiveProperty;
        private IntegerProperty scoreProperty;

        public ShowResultRecord(String term, String count, Integer rank) {
            this.documentProperty = new SimpleStringProperty(term);
            this.topFiveProperty = new SimpleStringProperty(count);
            this.scoreProperty = new SimpleIntegerProperty(rank);
        }

        public StringProperty getDocumentProperty() {
            return documentProperty;
        }

        public StringProperty getTopFiveProperty() {
            return topFiveProperty;
        }

        public IntegerProperty getScoreProperty() {
            return scoreProperty;
        }
    }

    public class CaseInsensitiveMap extends HashMap<String, String> {

        /**
         * put entries as lower case to ignore case
         *
         * @param key   key of entry
         * @param value value of entry
         * @return df
         */
        @Override
        public String put(String key, String value) {
            return super.put(key.toLowerCase(), value);
        }

        /**
         * return the String of the value of the given key
         *
         * @param key key in the map
         * @return value
         */
        public String get(String key) {
            return super.get(key.toLowerCase());
        }
    }


}
