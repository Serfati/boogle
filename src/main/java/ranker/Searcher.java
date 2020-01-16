package ranker;

import model.Model;
import parser.MiniDictionary;
import parser.Parse;
import parser.cDocument;
import rw.ReadFile;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;

public class Searcher implements Callable<LinkedList<String>> {
    private String postingPath;
    private boolean enableStemming;
    private boolean enableSemantics;
    private Query query;
    private SemanticHandler semanticHandler;

    public Searcher(String postingPath, boolean stem, boolean semantics, Query q, boolean offline) {
        this.postingPath = postingPath;
        this.enableStemming = stem;
        this.enableSemantics = semantics;
        this.query = q;
        if (semantics)
            semanticHandler = new SemanticHandler(offline);
    }

    /**
     * adds the new score to the score of the document
     *
     * @param score    scores of documents
     * @param docName  document name
     * @param newScore new score be added
     */
    static void addToScore(HashMap<String, Double> score, String docName, double newScore) {
        if (newScore != 0) {
            Double d = score.get(docName);
            if (d != null)
                newScore += d;
            score.put(docName, newScore);
        }
    }

    @Override
    public LinkedList<String> call() {
        return getQueryResults();
    }

    /**
     * Search for relevant documents for the given query
     *
     * @return relevant documents
     */
    private LinkedList<String> getQueryResults() {
        LinkedList<String> queryToList = Arrays.stream(query.getQueryText().split(" ")).filter(word -> !word.equals("")).collect(Collectors.toCollection(LinkedList::new));
        //parse query
        String queryToRun = enableSemantics ? query.getQueryText()+" "+query.getQueryDesc()+semanticHandler.getTwoBestMatches(queryToList) : query.getQueryText()+" "+query.getQueryDesc();
        Parse p = new Parse(new cDocument("", "", "", "", queryToRun), enableStemming);
        MiniDictionary md = p.parse(true);
        HashMap<String, Integer> wordsCountInQuery = md.countAppearances(); //count word in the query
        //prepare for calculation
        CaseInsensitiveMap wordsPosting = getPosting(wordsCountInQuery.keySet());
        //objects for the iteration
        Ranker ranker = new Ranker(wordsCountInQuery);
        HashMap<String, Double> score = new HashMap<>();
        //for each word go throw its posting with relevant documents
        for(String word : wordsCountInQuery.keySet())
            if (!wordsPosting.get(word).equals("")) {
                String postingLine = wordsPosting.get(word);
                String[] split = postingLine.split("\\|");
                double docInCorpusCount = Model.documentDictionary.keySet().size();
                double idf = Math.log10((docInCorpusCount+1) / split.length-1);
                double weight = 1;
                if (word.contains("-"))
                    weight = 1.15;
                for(String aSplit : split) {
                    String[] splitLine = aSplit.split(",");
                    String docName = splitLine[0];
                    if (splitLine.length > 1) {
                        int tf = Integer.parseInt(splitLine[1]);
                        double BM25 = weight * ranker.BM25Algorithm(word, docName, tf, idf);
                        addToScore(score, docName, BM25);
                        ranker.titleAlgorithm(score, docName, wordsPosting.keySet());
                    }
                }
            }
        ranker.containingAlgorithm(score, wordsCountInQuery.keySet());
        //sort the scores of documents from higher to lower
        List<Map.Entry<String, Double>> list = new ArrayList<>(score.entrySet());
        list.sort(reverseOrder(Map.Entry.comparingByValue()));
        return list.stream().map(Map.Entry::getKey).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * gets the posting of all query words
     *
     * @param query query words
     * @return the postings of the words
     */
    private CaseInsensitiveMap getPosting(Set<String> query) {
        CaseInsensitiveMap words = new CaseInsensitiveMap();
        HashMap<Character, LinkedList<Integer>> allCharactersTogether = new HashMap<>();
        //get first letters
        for(String word : query) {
            char letter = !Character.isLetter(word.charAt(0)) ? '`' : Character.toLowerCase(word.charAt(0));
            //get line number
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
        // get all posting lines
        allCharactersTogether.keySet().stream().map(letter -> ReadFile.readPostingLineAtIndex(postingPath, Character.toLowerCase(letter), allCharactersTogether.get(letter), enableStemming)).forEach(postings -> postings.stream().map(posting -> posting.split("~")).forEach(wordAndRestOfPosting -> words.put(wordAndRestOfPosting[0], wordAndRestOfPosting[1])));
        return words;
    }

    /* extends hash map to get insensitive keys (for lower and upper case string)*/
    public class CaseInsensitiveMap extends HashMap<String, String> {
        /* put entries as lower case to ignore case*/
        @Override
        public String put(String key, String value) {
            return super.put(key.toLowerCase(), value);
        }

        /* return the String of the value of the given key*/
        public String get(String key) {
            return super.get(key.toLowerCase());
        }
    }
}
