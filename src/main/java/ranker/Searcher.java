package ranker;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import parser.MiniDictionary;
import parser.NamedEntitiesSearcher;
import parser.Parse;
import parser.cDocument;
import rw.DatamuseAPI;
import rw.Query;

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
    private String postingPath;
    private List<String> docsThatReturned;
    private final int DOCS_RETURN_NUMBER = 50;

    public Searcher(Query q, String outputPath, String postingPath, boolean useStemming, boolean useSemantics) {
        this.query = q;
        this.outputPath = outputPath;
        this.postingPath = postingPath;
        this.enableStemming = useStemming;
        this.enableSemantics = useSemantics;
        docsThatReturned = new ArrayList<>();
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

    @Override
    public LinkedList<String> call() {
        try {
            return getQueryResults();
        } catch(IOException ignored) {
        }
        return null;
    }

    /**
     * Searcher
     * parseQueryAndReturnRank
     * synForQuery
     * autoComplete(term)
     */

    public static ObservableList<ShowResultRecord> getRecord() {
        ObservableList<ShowResultRecord> showDataRecords = FXCollections.observableArrayList();
        //TODO

        return showDataRecords;
    }

    private String[] getAllDocsOptionalForRetrival(String wordPostingData) {
        wordPostingData = wordPostingData.substring(wordPostingData.indexOf("~")+1);
        return split(wordPostingData, "|");
    }

    /**
     * Returns the file into we need to write the term and it's posting.
     *
     * @param term - the term for whom we wish to discover the relevant posting.
     * @return - a-z,0-9 or according to the prefix of given term.
     */
    private char returnDestinatoinPostFile(String term) {
        char prefix = term.toLowerCase().charAt(0);
        return !Character.isDigit(prefix) && !Character.isLetter(prefix) ? '`' : prefix;
    }

    private void returnOnlyFifty(List bigList) {
        while(bigList.size() > DOCS_RETURN_NUMBER) bigList.remove(DOCS_RETURN_NUMBER);
        List<Object> list = new ArrayList();
        for(int i = 0; i < bigList.size() && i < DOCS_RETURN_NUMBER; i++) list.add(bigList.get(i));
        IntStream.range(0, list.size()).forEach(i -> docsThatReturned.add(list.get(i).toString().split("=")[0]));
    }

    //Write the results to the file selected by the user
    public void writeResultsToDisk(String path) throws IOException {
        String addToFile = docsThatReturned.stream().map(s -> "240 "+"0 "+s+" 1 "+"42.38 mt"+"\n").collect(Collectors.joining());
        /////////\n
        BufferedWriter test = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+"\\Results.txt")));
        test.write(addToFile);
        test.flush();
        test.close();
    }

    private LinkedList<String> getQueryResults() throws IOException {
        NamedEntitiesSearcher ner = null;
        Parse parser;
        String queryAfterSem;
        parser = new Parse(new cDocument("", "", "", "", query.getQueryText()), enableStemming, ner);
        MiniDictionary md = parser.parse();
        if (enableSemantics) {
            queryAfterSem = query.getQueryText()+" "+useSemantics(md.dictionary).toString();
            parser.parse();
        }


        HashMap<String, Integer> wordsCountInQuery = null;

        Ranker ranker = null;

        return null;
    }

    //Allows the use of a semantic connection for the query
    public StringBuilder useSemantics(HashMap<String, LinkedList<Integer>> queryAfterParse) throws IOException {
        StringBuilder allSynonyms = new StringBuilder();
        for(HashMap.Entry<String, LinkedList<Integer>> entry : queryAfterParse.entrySet())
            allSynonyms.append(DatamuseAPI.synonyms(entry.getKey()));
        return allSynonyms;
    }


    public class ShowResultRecord {
        private StringProperty documentProperty;
        private StringProperty topFiveProperty;
        private IntegerProperty scoreProperty;

        ShowResultRecord(String term, String count, Integer rank) {
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
}
