package model;

import indexer.DocumentIndex;
import indexer.InvertedIndex;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import parser.MiniDictionary;
import parser.NamedEntitiesSearcher;
import parser.Parse;
import parser.cDocument;
import ranker.Ranker;
import ranker.Searcher;
import rw.Query;
import rw.ReadFile;
import rw.WriteFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.System.exit;

public class Model extends Observable implements IModel {
    private static Model singleton = null;
    public static HashSet<String> stopWords;
    public static InvertedIndex invertedIndex;
    public static HashMap<String, DocumentIndex> documentDictionary;
    private Searcher mySearcher;
    private Ranker myRanker;
    private AtomicInteger numOfPostings = new AtomicInteger(0);
    public HashMap<String, LinkedList<String>> m_results;
    private final static Logger LOGGER = LogManager.getLogger(Model.class.getName());

    //singleton
    public static Model getInstance() {
        if (singleton == null)
            singleton = new Model();
        return singleton;
    }

    @Override
    public void startIndexing(String pathOfDocs, String destinationPath, boolean useStemming) {
        LOGGER.log(Level.INFO, "Start Parsing and Indexing");

        String[] paths = pathAreValid(pathOfDocs, destinationPath); // checks if the paths entered are valid
        if (paths == null) return;
        int[] results = new int[0];
        double startEngine = System.currentTimeMillis();
        stopWords = ReadFile.initStopWordsSet(pathOfDocs+"/stop_words.txt");
        invertedIndex = new InvertedIndex();
        documentDictionary = new HashMap<>();

        try {
            results = indexMainLogic(invertedIndex, paths[0], paths[1], useStemming);
            WriteFile.writeDictionariesToDisk(destinationPath, useStemming);
        } catch(Exception e) {
            String[] update = {"Fail", "Indexing failed"};
            LOGGER.log(Level.ERROR, "Indexing failed");
            setChanged();
            notifyObservers(update);
            e.printStackTrace();
            exit(0);
        }
        final double RUNTIME = Double.parseDouble(String.format(Locale.US, "%.2f", (System.currentTimeMillis()-startEngine) / 60000));
        double[] totalResults = new double[]{results[0], results[1], RUNTIME};
        LOGGER.log(Level.INFO, "PROCESS DONE :: END INDEXING in "+RUNTIME+" minutes");
        setChanged();
        notifyObservers(totalResults);
    }

    @Override
    public void startBoogleSearch(String postingPath, String queries, String outLocation, boolean stem, boolean semantic, boolean offline) {
        LOGGER.log(Level.INFO, "Start Searching");
        String[] paths = pathAreValid(postingPath, outLocation); // checks if the paths entered are valid
        if (paths == null) return;
        double startEngine = System.currentTimeMillis();
        try {
            HashMap<String, LinkedList<String>> results = m_results = boogleMainLogic(paths[0], queries, paths[1], stem, semantic, offline);
            resultsToObservableList(results);
        } catch(Exception e) {
            String[] update = {"Fail", "Boogle failed"};
            LOGGER.log(Level.ERROR, "Boogle failed");
            setChanged();
            notifyObservers(update);
            e.printStackTrace();
            exit(0);
        }
        final double RUNTIME = Double.parseDouble(String.format(Locale.US, "%.2f", (System.currentTimeMillis()-startEngine)));
        double[] totalResults = new double[]{RUNTIME};
        LOGGER.log(Level.INFO, "PROCESS DONE :: END Searching"+" in "+RUNTIME+" ms");
        setChanged();
        notifyObservers(totalResults);
    }

    HashMap<String, LinkedList<String>> boogleMainLogic(String postingPath, String queryField, String outLocation, boolean stem, boolean semantic, boolean offline) {
        Random r = new Random();
        LinkedList<Query> queriesList = new LinkedList<>();
        if (queryField.endsWith(".txt")) queriesList = ReadFile.readQueries(new File(queryField));
        else queriesList.add(new Query(""+Math.abs(r.nextInt(899)+100), queryField, ""));

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        HashMap<String, LinkedList<String>> queriesResults = new HashMap<>();
        LinkedList<Pair<String, Future<LinkedList<String>>>> queryFuture;
        try {
            queryFuture = queriesList.stream().map(q -> new Pair<>(q.getQueryNum(), pool.submit(new Searcher(q, outLocation, postingPath, stem, semantic, offline)))).collect(Collectors.toCollection(LinkedList::new));
            for(Pair<String, Future<LinkedList<String>>> f : queryFuture)
                queriesResults.put(f.getKey(), mySearcher.returnOnlyFifty(f.getValue().get()));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return queriesResults;
    }

    @Override
    public int[] indexMainLogic(InvertedIndex invertedIndex, String corpusPath, String destinationPath, boolean stem) throws Exception {
        LOGGER.log(Level.INFO, "Start manager Method :: runnable");
        int numOfDocs = 0;
        int tempPostingValue = 650;
        ReadFile rf = new ReadFile();
        NamedEntitiesSearcher ner = new NamedEntitiesSearcher();
        ProgressBar pb = new ProgressBar("Parse & Index", tempPostingValue).start();

        int i = 0;
        while(i < tempPostingValue) {
            if (i == tempPostingValue / 2)
                LOGGER.log(Level.INFO, "mainLogicUnit Method :: DONE HALF WAY");
            //-------------------------ReadFile------------------------//
            LinkedList<cDocument> l =
                    rf.readFiles(corpusPath, i, tempPostingValue);
            //--------------------Thread Pool 8 cores-----------------//
            ExecutorService threadPool =
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);
            //-------------------------Parsing------------------------//
            ConcurrentLinkedDeque<Future<MiniDictionary>> futureMiniDicList =
                    l.stream().map(cd -> threadPool.submit(new Parse(cd, stem, ner)))
                            .collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
            //-------------------------Arrange------------------------//
            ConcurrentLinkedDeque<MiniDictionary> dicList = new ConcurrentLinkedDeque<>();
            for(Iterator<Future<MiniDictionary>> iterator = futureMiniDicList.iterator(); iterator.hasNext(); ) {
                Future<MiniDictionary> futureList = iterator.next();
                dicList.add(futureList.get());
                numOfDocs++;
            }
            //-------------------------Indexing------------------------//
            InvertedIndex index = new InvertedIndex(dicList); // tempPost on Memory
            Future<HashMap<String, Pair<Integer, StringBuilder>>> futureTemporaryPosting = threadPool.submit(index); // runnable build tempPost
            HashMap<String, Pair<Integer, StringBuilder>> temporaryPosting = futureTemporaryPosting.get(); // get it from future
            //-------------------------WriteFile------------------------//
            //Thread t1 = new Thread(() -> WriteFile.writeTempPosting(destinationPath, numOfPostings.getAndIncrement(), temporaryPosting));
            WriteFile.writeTempPosting(destinationPath, numOfPostings.getAndIncrement(), temporaryPosting);
            //-------------------------Insert Data to II------------------------//
            insertData(dicList, invertedIndex);
            setPrimaryWords(dicList);
            threadPool.shutdown();
            i++;
            pb.stepBy(1);
            pb.setExtraMessage("Reading...");
        }
        pb.stop();

        LOGGER.log(Level.INFO, "Start merge Method :: single");
        mergePosting(invertedIndex, destinationPath, stem);

        return new int[]{numOfDocs, invertedIndex.getNumOfUniqueTerms()};
    }

    @Override
    public void loadDictionary(String path, boolean useStemming) {
        boolean foundInvertedIndex = false, foundDocumentDictionary = false;
        File dirSource = new File(path);
        File[] directoryListing = dirSource.listFiles();
        String[] update;
        if (directoryListing != null && dirSource.isDirectory()) {
            for(int i = 0, directoryListingLength = directoryListing.length; i < directoryListingLength; i++) {
                File file = directoryListing[i]; // search for the relevant file
                if ((file.getName().equals("SIF.txt") && useStemming) || (file.getName().equals("IF.txt")) && !useStemming) {
                    invertedIndex = new InvertedIndex(file);
                    foundInvertedIndex = true;
                }
                if ((file.getName().equals("DocDic_PS.txt") && useStemming) || (file.getName().equals("DocDic.txt")) && !useStemming)
                    foundDocumentDictionary = true;
            }
            if (!foundInvertedIndex || !foundDocumentDictionary) {
                invertedIndex = null;
                documentDictionary = null;
                update = new String[]{"Fail", "could not find one or more dictionaries"};
            } else
                update = new String[]{"Successful", "Dictionary was loaded successfully"};
        } else
            update = new String[]{"Fail", "destination path is illegal or unreachable"};

        setChanged();
        notifyObservers(update);
    }

    /**
     *  checks if paths are valid
     * @param pathOfDocs - path of the corpus
     * @param destinationPath - path to output
     * @return true if paths are valid
     */
    private String[] pathAreValid(String pathOfDocs, String destinationPath) {
        String pathOfStopWords = "";
        File dirSource = new File(pathOfDocs);
        File[] directoryListing = dirSource.listFiles();
        if (directoryListing != null && dirSource.isDirectory()) {
            for(int i = 0, directoryListingLength = directoryListing.length; i < directoryListingLength; i++) {
                File file = directoryListing[i];
                if (file.isFile() && file.getName().equalsIgnoreCase("stop_words.txt"))
                    pathOfStopWords = file.getAbsolutePath();
            }
            if (pathOfStopWords.equals("")) {
                String[] update = {"Fail", "contents of source path do not contain corpus folder or stop words file"};
                setChanged();
                notifyObservers(update);
                return null;
            }
        } else {
            String[] update = {"Fail", ""};
            setChanged();
            notifyObservers(update);
            return null;
        }
        File dirDest = new File(destinationPath);
        if (!dirDest.isDirectory()) {
            String[] update = {"Fail", "Destination"};
            setChanged();
            notifyObservers(update);
            return null;
        }
        return new String[]{pathOfDocs, destinationPath, pathOfStopWords};
    }

    private void insertData(ConcurrentLinkedDeque<MiniDictionary> miniDicList, InvertedIndex invertedIndex) {
        miniDicList.forEach(mini -> {
            DocumentIndex cur = new DocumentIndex(mini.getName(), mini.getMaxFrequency(), mini.size(), mini.getMaxFreqWord(), mini.getDocLength(), mini.getTitle());
            documentDictionary.put(mini.getName(), cur);
            mini.dictionary.keySet().forEach(invertedIndex::addTerm);
        });
    }

    @Override
    public void mergePosting(InvertedIndex invertedIndex, String tempPostingPath, boolean stem) throws IOException {
        //save buffers for each temp file
        LinkedList<BufferedReader> bufferedReaderList = initBufferedReaderList(tempPostingPath);
        //download all the first sentences of each file
        String[] firstSentenceOfFile = initiateMergeArray(bufferedReaderList);
        char postingNum = '`';
        HashMap<String, StringBuilder> writeToPosting = new HashMap<>();
        //separate the name of the file to be with stem or not
        String fileName = tempPostingPath+"/Stemmed";
        if (!stem)
            fileName = tempPostingPath+"/Unstemmed";
        do {
            int numOfAppearances = 0;
            StringBuilder finalPostingLine = new StringBuilder(); //current posting line
            String minTerm = ""+(char) 127;
            String[] saveSentences = new String[firstSentenceOfFile.length];
            //go throw all the lines currently in the array to merge them together if a certain term exists in more than 1 file
            int i = 0;
            if (i < firstSentenceOfFile.length) {
                do {
                    if (firstSentenceOfFile[i] != null && !firstSentenceOfFile[i].equals("")) {
                        String[] termAndData = firstSentenceOfFile[i].split("~");
                        int result = termAndData[0].compareToIgnoreCase(minTerm);
                        if (result == 0) { // if it is the same term add his posting data to the old term
                            if (Character.isLowerCase(termAndData[0].charAt(0)))
                                finalPostingLine.replace(0, termAndData[0].length(), termAndData[0].toLowerCase());
                            finalPostingLine.append(termAndData[2]);
                            firstSentenceOfFile[i] = null;
                            saveSentences[i] = termAndData[0]+"~"+termAndData[1]+"~"+termAndData[2];
                            numOfAppearances += Integer.parseInt(termAndData[1]);
                        } else if (result < 0) { // if it is more lexi smaller than min term than it is time to take care of it
                            minTerm = termAndData[0];
                            finalPostingLine.delete(0, finalPostingLine.length());
                            finalPostingLine.append(termAndData[0]).append("~").append(termAndData[2]);
                            firstSentenceOfFile[i] = null;
                            saveSentences[i] = termAndData[0]+"~"+termAndData[1]+"~"+termAndData[2];
                            numOfAppearances = Integer.parseInt(termAndData[1]);
                        }
                    }
                    i++;
                } while(i < firstSentenceOfFile.length);
            }
            //restore all the lines that were deleted (because they weren't the minimal term)
            restoreSentences(bufferedReaderList, minTerm, firstSentenceOfFile, saveSentences);
            finalPostingLine.append("\t").append(numOfAppearances);
            if (minTerm.toLowerCase().charAt(0) > postingNum) { //write the current posting to the disk once a term with higher first letter has riched
                writeFinalPostings(writeToPosting, invertedIndex, fileName, postingNum);
                writeToPosting = new HashMap<>();
                postingNum++;
            }
            //merge terms that appeared in different case
            lookForSameTerms(finalPostingLine.toString().split("~")[0], finalPostingLine, writeToPosting);
        } while(Arrays.stream(firstSentenceOfFile).anyMatch(Objects::nonNull) && postingNum < 'z'+1);
        HashMap<String, StringBuilder> finalWriteToPosting = writeToPosting;
        String finalFileName = fileName;
        writeFinalPostings(finalWriteToPosting, invertedIndex, finalFileName, 'z');

        invertedIndex.deleteEntriesOfIrrelevant();

        for(BufferedReader bufferedReader : bufferedReaderList)
            bufferedReader.close();

        File dirSource = new File(tempPostingPath);
        File[] directoryListing = dirSource.listFiles();
        if (directoryListing != null && dirSource.isDirectory())
            Arrays.stream(directoryListing).filter(file -> file.getName().startsWith("posting")).forEachOrdered(File::delete);
    }

    /**
     * searches for term in different case
     * @param term the term
     * @param finalPostingLine term's posting line
     * @param writeToPosting - the collection of the final posting
     */
    private void lookForSameTerms(String term, StringBuilder finalPostingLine, HashMap<String, StringBuilder> writeToPosting) {
        boolean upper = writeToPosting.containsKey(term.toUpperCase());
        boolean lower = writeToPosting.containsKey(term.toLowerCase());
        boolean normal = writeToPosting.containsKey(term);
        String replace;
        if (!upper) {
            if (!(!normal && !lower)) { //if term appeared in lower case
                if (lower)
                    term = term.toLowerCase();
                //update the posting with old and new data
                replace = writeToPosting.get(term).toString();
                writeToPosting.replace(term, separator(term,
                        finalPostingLine,
                        replace));
            } else writeToPosting.put(term, finalPostingLine);
        } else { //if term appeared in upper case
            //remove the current appearance of the term
            replace = writeToPosting.remove(term.toUpperCase()).toString();
            if (Character.isLowerCase(term.charAt(0))) term = term.toLowerCase();
            //update the posting with old and new data
            writeToPosting.put(term, separator(term, finalPostingLine, replace));
        }
    }

    /**
     * init a buffer readers for all files
     * @param tempPostingPath path of temp postings
     * @return list with buffers
     */
    private LinkedList<BufferedReader> initBufferedReaderList(String tempPostingPath) {
        File dirSource = new File(tempPostingPath);
        File[] directoryListing = dirSource.listFiles();
        LinkedList<BufferedReader> bufferedReaderList = new LinkedList<>();
        if (directoryListing != null && dirSource.isDirectory())
            Arrays.stream(directoryListing).filter(file -> file.getName().startsWith("posting")).forEach(file -> {
                try {
                    bufferedReaderList.add(new BufferedReader(new FileReader(file)));
                } catch(FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        return bufferedReaderList;
    }

    private StringBuilder separator(String minTerm, StringBuilder finalPostingLine, String replace) {
        AtomicReferenceArray<String> separatePostingAndNumOld;
        separatePostingAndNumOld = new AtomicReferenceArray<>(replace.split("\t"));
        AtomicReferenceArray<String> separatePostingAndNumNew;
        separatePostingAndNumNew = new AtomicReferenceArray<>(finalPostingLine.toString().split("\t"));
        int numOfAppearance = Integer.parseInt(separatePostingAndNumOld.get(1))+Integer.parseInt(separatePostingAndNumNew.get(1));
        String oldPosting = separatePostingAndNumOld.get(0).substring(separatePostingAndNumOld.get(0).indexOf("~")+1);
        String newPosting = separatePostingAndNumNew.get(0).substring(separatePostingAndNumNew.get(0).indexOf("~")+1);
        return new StringBuilder(minTerm+"~"+oldPosting+newPosting+"\t"+numOfAppearance);
    }

    private void restoreSentences(LinkedList<BufferedReader> bufferedReaderList, String minTerm, String[] firstSentenceOfFile, String[] saveSentences) {
        IntStream.range(0, saveSentences.length).filter(i -> saveSentences[i] != null).forEach(i -> {
            String[] termAndData = saveSentences[i].split("~");
            firstSentenceOfFile[i] = termAndData[0].compareToIgnoreCase(minTerm) != 0 ? termAndData[0]+"~"+termAndData[1]+"~"+termAndData[2] : getNextSentences(bufferedReaderList.get(i));
        });
    }
    /**
     * returns the next sentence from @bf
     * @param bf a buffer reader of file
     * @return the next sentence
     */
    private String getNextSentences(BufferedReader bf) {
        String line = null;
        try {
            return (line = bf.readLine()) == null ? null : bf.readLine();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    /**
     * init array full all first sentences from all files
     * @param bufferedReaderList list of buffers
     * @return array with lines
     */
    private String[] initiateMergeArray(LinkedList<BufferedReader> bufferedReaderList) {
        String[] firstSentenceOfFile = new String[bufferedReaderList.size()];
        int i = 0;
        for(Iterator<BufferedReader> iterator = bufferedReaderList.iterator(); iterator.hasNext(); ) {
            BufferedReader bf = iterator.next();
            String line = null;
            try {
                if ((line = bf.readLine()) != null) line = bf.readLine();
            } catch(IOException e) {
                e.printStackTrace();
            }
            if (line != null) firstSentenceOfFile[i] = line;
            i++;
        }
        return firstSentenceOfFile;
    }

    /**
     * Takes the data of a current letter and write to the disk
     * @param writeToPosting - what should be written
     * @param invertedIndex - the inverted index
     * @param fileName - the file name as it should be written
     * @param postingNum - the header to the posting num indicating what letter is it
     */
    public void writeFinalPostings(HashMap<String, StringBuilder> writeToPosting, InvertedIndex invertedIndex, String fileName, char postingNum) {
        List<String> keys = new LinkedList<>(writeToPosting.keySet());
        int k = 0;
        Iterator<String> iterator = keys.iterator();
        if (iterator.hasNext()) {
            do {
                String word0 = iterator.next();
                String toNum = writeToPosting.get(word0).toString().split("\t")[1];
                invertedIndex.setPointer(word0, k++);
                invertedIndex.setNumOfAppearances(word0, Integer.parseInt(toNum));
            } while(iterator.hasNext());
        }
        final HashMap<String, StringBuilder> sendToThread = new HashMap<>(writeToPosting);
        String file = fileName+"_"+postingNum+".txt";
        new Thread(() -> WriteFile.writeFinalPosting(file, sendToThread)).start();
    }

    /**
     * change results to observable list
     *
     * @param results the result
     */
    private void resultsToObservableList(HashMap<String, LinkedList<String>> results) {
        ObservableList<Searcher.ShowResultRecord> observableResult = FXCollections.observableArrayList();
        for(Map.Entry<String, LinkedList<String>> entry : results.entrySet()) {
            if (documentDictionary.containsKey(entry.getKey())) {
                StringBuilder fives = documentDictionary.get(entry.getKey()).getFiveEntities();
                int dl = documentDictionary.get(entry.getKey()).getDocLength();
                observableResult.add(new Searcher.ShowResultRecord(entry.getKey(), fives.toString(), dl));
            }
        }
        setChanged();
        notifyObservers(observableResult);
    }

    public StringBuilder showFiveEntities(String docName) {
        if (documentDictionary.containsKey(docName)) {
            try {
                return documentDictionary.get(docName).getFiveEntities();
            } catch(Exception e) {
                System.out.println(docName);
            }
        }
        return new StringBuilder();
    }

    private void setPrimaryWords(ConcurrentLinkedDeque<MiniDictionary> miniDicList) {
        miniDicList.forEach(MiniDictionary::setPrimaryWords);
    }

    @Override
    public boolean writeRes(String dest) {
        FileWriter fileWriter;
        StringBuilder toWrite = results();
        try {
            if (m_results.size() > 0) {
                fileWriter = new FileWriter(dest+"/results.txt");
                fileWriter.write(toWrite.toString());
                fileWriter.close();
                toWrite.delete(0, toWrite.length());
            }
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    /**
     * returns a string builder with the results ready to be written to the disk
     *
     * @return a string builder ready to be written
     */
    private StringBuilder results() {
        StringBuilder res = new StringBuilder();
        ArrayList<String> queryIDs = new ArrayList<>(m_results.keySet());
        queryIDs.sort(String.CASE_INSENSITIVE_ORDER);
        if (m_results != null)
            for(String m : queryIDs) {
                for(String doc : m_results.get(m)) {
                    String line = m+" 0 "+doc+" 0 0 ah\n";
                    res.append(line);
                }
            }
        return res;
    }

    @Override
    public void reset(String path) {
        File dir = new File(path);
        String[] update;
        if (dir.isDirectory()) {
            try {
                FileUtils.cleanDirectory(new File(path)); //delete all the files in the directory
                update = new String[]{"Successful", "folder is clean"};
                LOGGER.log(Level.INFO, "folder is clean");
            } catch(IOException e) {
                e.printStackTrace();
                update = new String[]{"Fail", "Faild to clean folder"};
                LOGGER.log(Level.ERROR, "Faild to clean folder");
            }
        } else {
            update = new String[]{"Fail", "Not a directory"};
            LOGGER.log(Level.ERROR, "Path is NOT a directory");
        }
        setChanged();
        notifyObservers(update);
    }

    @Override
    public void showDictionary() {
        setChanged();
        notifyObservers(invertedIndex.getRecord());
    }

    @Override
    public void showData() {
        setChanged();
        notifyObservers(Searcher.getRecord());
    }
}