package Model;

import Engine.DocumentIndex;
import Engine.InvertedIndex;
import IO.ReadFile;
import IO.WriteFile;
import Parser.MiniDictionary;
import Parser.Parse;
import Parser.cDocument;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Model extends Observable implements IModel {
    public static HashSet<String> stopWords;
    public static InvertedIndex invertedIndex;
    public static HashMap<String, DocumentIndex> documentDictionary;
    private AtomicInteger numOfPostings = new AtomicInteger(0);


    @Override
    public void startIndexing(String pathOfDocs, String destinationPath, boolean stm) {
        String[] paths = pathsAreValid(pathOfDocs, destinationPath); // checks if the paths entered are valid
        if (paths != null) {
            double start = System.currentTimeMillis();
            stopWords = ReadFile.initStopWordsSet(paths[2]);
            invertedIndex = new InvertedIndex();
            documentDictionary = new HashMap<>();
            double[] results = new double[0];
            try {
                results = manage(invertedIndex, paths[0], paths[1], stm);
                WriteFile.writeDictionariesToDisk(destinationPath, stm);
            } catch(Exception e) {
                String[] update = {"Fail", "Indexing failed"};
                setChanged();
                notifyObservers(update);
            }
            double[] totalResults = new double[]{results[0], results[1], (System.currentTimeMillis()-start) / 60000};
            setChanged();
            notifyObservers(totalResults);
        }
    }

    @Override
    public void startOver(String path) {
        File dir = new File(path);
        String[] update;
        if (dir.isDirectory()) {
            try {
                FileUtils.cleanDirectory(dir); //delete all the files in the directory
                update = new String[]{"Successful", "The folder is clean now"};
            } catch(IOException e) {
                e.printStackTrace();
                update = new String[]{"Fail", "Cleaning the folder was unsuccessful"};
            }
        } else {
            update = new String[]{"Fail", "Path given is not a directory or could not be reached"};
        }
        setChanged();
        notifyObservers(update);
    }

    @Override
    public void showDictionary() {
        if (invertedIndex == null) {
            String[] update = {"Fail", "Please load the dictionary first"};
            setChanged();
            notifyObservers(update);
        } else {
            ObservableList records = invertedIndex.getRecords();
            setChanged();
            notifyObservers(records);
        }
    }

    @Override
    public void loadDictionary(String path, boolean stem) {
        boolean foundInvertedIndex = false, foundDocumentDictionary = false;
        File dirSource = new File(path);
        File[] directoryListing = dirSource.listFiles();
        String[] update;
        if (directoryListing != null && dirSource.isDirectory()) {
            for(File file : directoryListing) { // search for the relevant file
                if ((file.getName().equals("StemInvertedFile.txt") && stem) || (file.getName().equals("InvertedFile.txt")) && !stem) {
                    invertedIndex = new InvertedIndex(file);
                    foundInvertedIndex = true;
                }
                if ((file.getName().equals("StemDocumentDictionary.txt") && stem) || (file.getName().equals("DocumentDictionary.txt")) && !stem) {
                    foundDocumentDictionary = true;
                }
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

    private String[] pathsAreValid(String pathOfDocs, String destinationPath) {
        String pathOfStopWords = "";
        File dirSource = new File(pathOfDocs);
        File[] directoryListing = dirSource.listFiles();
        if (directoryListing != null && dirSource.isDirectory()) {
            for(File file : directoryListing) {
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
            String[] update = {"Fail", "Source path is illegal or unreachable"};
            setChanged();
            notifyObservers(update);
            return null;
        }
        File dirDest = new File(destinationPath);
        if (!dirDest.isDirectory()) {
            String[] update = {"Fail", "Destination path is illegal or unreachable"};
            setChanged();
            notifyObservers(update);
            return null;
        }
        return new String[]{pathOfDocs, destinationPath, pathOfStopWords};
    }

    double[] manage(InvertedIndex invertedIndex, String corpusPath, String destinationPath, boolean stem) throws Exception {
        int numOfDocs = 0;
        int numOfTempPostings = 100;
        LinkedList<Thread> tmpPostingThread = new LinkedList<>();

        for(int i = 0; i < numOfTempPostings; i++) {

            ReadFile rf = new ReadFile();
            LinkedList<cDocument> l = rf.readFiles(corpusPath, i, numOfTempPostings);

            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            ConcurrentLinkedDeque<Future<MiniDictionary>> futureMiniDicList = l.stream().map(cd -> pool.submit(new Parse(cd, stem))).collect(Collectors.toCollection(ConcurrentLinkedDeque::new));


            ConcurrentLinkedDeque<MiniDictionary> miniDicList = new ConcurrentLinkedDeque<>();
            for(Future<MiniDictionary> fMiniDic : futureMiniDicList) {
                miniDicList.add(fMiniDic.get());
                numOfDocs++;
            }

            InvertedIndex index = new InvertedIndex(miniDicList);
            Future<HashMap<String, Pair<Integer, StringBuilder>>> futureTemporaryPosting = pool.submit(index);
            HashMap<String, Pair<Integer, StringBuilder>> temporaryPosting = futureTemporaryPosting.get();

            Thread t1 = new Thread(() -> WriteFile.writeTempPosting(destinationPath, numOfPostings.getAndIncrement(), temporaryPosting));
            t1.start();
            tmpPostingThread.add(t1);

            insertData(miniDicList, invertedIndex);
            pool.shutdown();
        }


        for(Thread t : tmpPostingThread)
            t.join();

        mergePostings(invertedIndex, destinationPath, stem);

        return new double[]{numOfDocs, invertedIndex.getNumOfUniqueTerms()};
    }

    private void insertData(ConcurrentLinkedDeque<MiniDictionary> miniDicList, InvertedIndex invertedIndex) {

        miniDicList.forEach(mini -> {
            DocumentIndex cur = new DocumentIndex(mini.getName(), mini.getMaxFrequency(), mini.size(), mini.getMaxFreqWord(), mini.getDocLength(), mini.getTitle());
            documentDictionary.put(mini.getName(), cur);
            mini.m_dictionary.keySet().forEach(invertedIndex::addTerm);
        });
    }

    private void mergePostings(InvertedIndex invertedIndex, String tempPostingPath, boolean stem) {
        //save buffers for each temp file
        LinkedList<BufferedReader> bufferedReaderList = initiateBufferedReaderList(tempPostingPath);
        //download all the first sentences of each file
        String[] firstSentenceOfFile = initiateMergingArray(bufferedReaderList);
        char postingNum = '`';
        HashMap<String, StringBuilder> writeToPosting = new HashMap<>();
        //separate the name of the file to be with stem or not
        String fileName = tempPostingPath+"/finalPostingStem";
        if (!stem)
            fileName = tempPostingPath+"/finalPosting";
        do {
            int numOfAppearances = 0;
            StringBuilder finalPostingLine = new StringBuilder(); //current posting line
            String minTerm = ""+(char) 127;
            String[] saveSentences = new String[firstSentenceOfFile.length];
            //go throw all the lines currently in the array to merge them together if a certain term exists in more than 1 file
            for(int i = 0; i < firstSentenceOfFile.length; i++) {
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
            }
            //restore all the lines that were deleted (because they weren't the minimal term)
            restoreSentence(bufferedReaderList, minTerm, firstSentenceOfFile, saveSentences);
            finalPostingLine.append("\t").append(numOfAppearances);
            if (minTerm.toLowerCase().charAt(0) > postingNum) { //write the current posting to the disk once a term with higher first letter has riched
                WriteFile.writeFinalPosting(writeToPosting, invertedIndex, fileName, postingNum);
                writeToPosting = new HashMap<>();
                postingNum++;
            }
            //merge terms that appeared in different case
            lookForSameTerm(finalPostingLine.toString().split("~")[0], finalPostingLine, writeToPosting);
        } while(Arrays.stream(firstSentenceOfFile).anyMatch(Objects::nonNull) && postingNum < 'z'+1);
        WriteFile.writeFinalPosting(writeToPosting, invertedIndex, fileName, 'z');

        invertedIndex.deleteEntriesOfIrrelevant();
        closeAllFiles(bufferedReaderList);
        deleteTempFiles(tempPostingPath);
    }

    private void closeAllFiles(LinkedList<BufferedReader> bufferedReaderList) {
        bufferedReaderList.forEach(bf -> {
            try {
                bf.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void lookForSameTerm(String minTerm, StringBuilder finalPostingLine, HashMap<String, StringBuilder> writeToPosting) {
        boolean option1 = writeToPosting.containsKey(minTerm.toUpperCase());
        boolean option2 = writeToPosting.containsKey(minTerm.toLowerCase());
        boolean option3 = writeToPosting.containsKey(minTerm);
        String replace;
        if (option1) { //if term appeared in upper case
            //remove the current appearance of the term
            if (Character.isLowerCase(minTerm.charAt(0))) {
                replace = writeToPosting.remove(minTerm.toUpperCase()).toString();
                minTerm = minTerm.toLowerCase();
            } else {
                replace = writeToPosting.remove(minTerm.toUpperCase()).toString();
            }
            //update the posting with old and new data
            writeToPosting.put(minTerm, separator(minTerm, finalPostingLine, replace));
        } else if (option3 || option2) { //if term appeared in lower case
            if (option2)
                minTerm = minTerm.toLowerCase();
            //update the posting with old and new data
            replace = writeToPosting.get(minTerm).toString();
            writeToPosting.replace(minTerm, separator(minTerm, finalPostingLine, replace));
        } else {
            writeToPosting.put(minTerm, finalPostingLine);
        }
    }

    private StringBuilder separator(String minTerm, StringBuilder finalPostingLine, String replace) {
        String[] separatePostingAndNumOld = replace.split("\t");
        String[] separatePostingAndNumNew = finalPostingLine.toString().split("\t");
        int numOfAppearance = Integer.parseInt(separatePostingAndNumOld[1])+Integer.parseInt(separatePostingAndNumNew[1]);
        String oldPosting = separatePostingAndNumOld[0].substring(separatePostingAndNumOld[0].indexOf("~")+1);
        String newPosting = separatePostingAndNumNew[0].substring(separatePostingAndNumNew[0].indexOf("~")+1);
        return new StringBuilder(minTerm+"~"+oldPosting+newPosting+"\t"+numOfAppearance);
    }

    private void restoreSentence(LinkedList<BufferedReader> bufferedReaderList, String minTerm, String[] firstSentenceOfFile, String[] saveSentences) {
        for(int i = 0; i < saveSentences.length; i++) {
            if (saveSentences[i] != null) {
                String[] termAndData = saveSentences[i].split("~");
                if (termAndData[0].compareToIgnoreCase(minTerm) != 0) {
                    firstSentenceOfFile[i] = termAndData[0]+"~"+termAndData[1]+"~"+termAndData[2];
                } else
                    firstSentenceOfFile[i] = getNextSentence(bufferedReaderList.get(i));
            }
        }
    }

    private String getNextSentence(BufferedReader bf) {
        String line = null;
        try {
            if ((line = bf.readLine()) != null) return line;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    private String[] initiateMergingArray(LinkedList<BufferedReader> bufferedReaderList) {
        String[] firstSentenceOfFile = new String[bufferedReaderList.size()];
        int i = 0;
        for(Iterator<BufferedReader> iterator = bufferedReaderList.iterator(); iterator.hasNext(); ) {
            BufferedReader bf = iterator.next();
            String line = getNextSentence(bf);
            if (line != null) firstSentenceOfFile[i] = line;
            i++;
        }
        return firstSentenceOfFile;
    }

    private LinkedList<BufferedReader> initiateBufferedReaderList(String tempPostingPath) {
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

    private void deleteTempFiles(String destPath) {
        File dirSource = new File(destPath);
        File[] directoryListing = dirSource.listFiles();
        if (directoryListing != null && dirSource.isDirectory())
            Arrays.stream(directoryListing).filter(file -> file.getName().startsWith("posting")).forEachOrdered(File::delete);
    }
}
