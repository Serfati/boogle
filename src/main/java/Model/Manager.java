package Model;


import Model.Engine.Indexer;
import Model.Engine.InvertedIndex;
import Model.IO.ReadFile;
import Model.IO.WriteFile;
import Model.Parser.MiniDictionary;
import Model.Parser.Parse;
import Model.Parser.cDocument;
import com.sun.corba.se.impl.orbutil.concurrent.Mutex;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Manager {
    private AtomicInteger numOfPostings = new AtomicInteger(0);
    public static HashMap<String,double[]> vectors;
    public static Mutex m = new Mutex();

    /**
     * returns 50 or less results for a query
     * @param queryResults the full results
     * @return 50 relevant queries
     */
    private LinkedList<String> getLimited(LinkedList<String> queryResults) {
        LinkedList<String> limited = new LinkedList<>();
        for (int i = 0; i < 50 && !queryResults.isEmpty(); i++) {
            limited.add(queryResults.pollFirst());
        }
        return limited;
    }

    /**
     * This function manages the index process by separating it to a few bunches
     * @param documentDictionary - the document dictionary
     * @param invertedIndex - the inverted index
     * @param corpusPath - the path of the corpus
     * @param destinationPath - the path where the postings will be written
     * @param stem - if stemming should be done
     * @return returns data about the current run [num of documents, number of unique terms]
     * @throws Exception .
     */
    double[] manage( HashMap<String, cDocument> documentDictionary, InvertedIndex invertedIndex, String corpusPath, String destinationPath, boolean stem) throws Exception {

        int numOfDocs = 0;
        int numOfTempPostings = 900;
        LinkedList<Thread> tmpPostingThread = new LinkedList<>();

        int i = 0;
        while (i < numOfTempPostings) {
            //read number of files
            LinkedList<cDocument> l = ReadFile.readFiles(corpusPath, i, numOfTempPostings);

            //gather all the corpus documents together and parse them
            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            ConcurrentLinkedDeque<Future<MiniDictionary>> futureMiniDicList = l.stream().map(cd -> pool.submit(new Parse(cd, stem))).collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
            //each document is being submitted to a new thread to be parsed

            ConcurrentLinkedDeque<MiniDictionary> miniDicList = new ConcurrentLinkedDeque<>();
            for (Future<MiniDictionary> fMiniDic : futureMiniDicList) {
                miniDicList.add(fMiniDic.get());
                numOfDocs++; //counts all the documents we
            }

            //index together all the documents we've parsed in this iteration
            Indexer index = new Indexer(miniDicList);
            Future<HashMap<String, Pair<Integer, StringBuilder>>> futureTemporaryPosting = pool.submit(index);
            HashMap<String, Pair<Integer, StringBuilder>> temporaryPosting = futureTemporaryPosting.get();

            // Write the posting to the disk, then get the "link" of each word in list from the "WriteFile"
            Thread t1 = new Thread(() -> WriteFile.writeTempPosting(destinationPath, numOfPostings.getAndIncrement(), temporaryPosting));
            t1.start();
            tmpPostingThread.add(t1);

            setPrimaryWords(miniDicList);

            pool.shutdown();
            i++;
        }

        //wait for all the temp postings to be written
        for (Thread t : tmpPostingThread)
            t.join();

        //merge all the temp postings
        mergePostings(invertedIndex, destinationPath, stem);

        return new double[]{numOfDocs, invertedIndex.getNumOfUniqueTerms()};
    }

    /**
     * set the 5 most recent words in a doc
     * @param miniDicList miniDictionary list
     */
    private void setPrimaryWords(ConcurrentLinkedDeque<MiniDictionary> miniDicList) {
        for (MiniDictionary mini:miniDicList){
            mini.setPrimaryWords();
        }
    }

    /**
     * this function merges the temp postings to postings according to the letter of words
     * @param invertedIndex - the inverted index
     * @param tempPostingPath - the path of the temp postings
     * @param stem - if should be stemmed
     */
    private void mergePostings(InvertedIndex invertedIndex, String tempPostingPath,boolean stem){
        //save buffers for each temp file
        LinkedList<BufferedReader> bufferedReaderList = initiateBufferedReaderList(tempPostingPath);
        //download all the first sentences of each file
        String[] firstSentenceOfFile = initiateMergingArray(bufferedReaderList);
        char postingNum = '`';
        HashMap<String, StringBuilder> writeToPosting = new HashMap<>();
        //separate the name of the file to be with stem or not
        String fileName = tempPostingPath+"\\finalPostingStem";
        if (!stem)
            fileName= tempPostingPath+"\\finalPosting";
        do {
            int numOfAppearances = 0;
            StringBuilder finalPostingLine = new StringBuilder(); //current posting line
            String minTerm = ""+(char)127;
            String[] saveSentences = new String[firstSentenceOfFile.length];
            //go throw all the lines currently in the array to merge them together if a certain term exists in more than 1 file
            int i = 0;
            while (i < firstSentenceOfFile.length) {
                if (firstSentenceOfFile[i] != null && !firstSentenceOfFile[i].equals("")) {
                    String[] termAndData = firstSentenceOfFile[i].split("~");
                    int result = termAndData[0].compareToIgnoreCase(minTerm);
                    if (result == 0) { // if it is the same term add his posting data to the old term
                        if (Character.isLowerCase(termAndData[0].charAt(0)))
                            finalPostingLine.replace(0, termAndData[0].length(), termAndData[0].toLowerCase());
                        finalPostingLine.append(termAndData[2]);
                        firstSentenceOfFile[i] = null;
                        saveSentences[i] = termAndData[0] + "~" + termAndData[1] + "~" + termAndData[2];
                        numOfAppearances += Integer.parseInt(termAndData[1]);
                    } else if (result < 0) { // if it is more lexi smaller than min term than it is time to take care of it
                        minTerm = termAndData[0];
                        finalPostingLine.delete(0, finalPostingLine.length());
                        finalPostingLine.append(termAndData[0]).append("~").append(termAndData[2]);
                        firstSentenceOfFile[i] = null;
                        saveSentences[i] = termAndData[0] + "~" + termAndData[1] + "~" + termAndData[2];
                        numOfAppearances = Integer.parseInt(termAndData[1]);
                    }
                }
                i++;
            }
            //restore all the lines that were deleted (because they weren't the minimal term)
            restoreSentence(bufferedReaderList,minTerm,firstSentenceOfFile,saveSentences);
            finalPostingLine.append("\t").append(numOfAppearances);
            if(minTerm.toLowerCase().charAt(0)>postingNum) { //write the current posting to the disk once a term with higher first letter has riched
                writeFinalPosting(writeToPosting,invertedIndex,fileName,postingNum);
                writeToPosting = new HashMap<>();
                postingNum++;
            }
            //merge terms that appeared in different case
            lookForSameTerm(finalPostingLine.toString().split("~")[0],finalPostingLine,writeToPosting);
        } while(readingIsDone(firstSentenceOfFile) && postingNum<'z'+1);
        writeFinalPosting(writeToPosting,invertedIndex,fileName,'z');

        invertedIndex.deleteEntriesOfIrrelevant();
        closeAllFiles(bufferedReaderList);
        deleteTempFiles(tempPostingPath);
    }

    /**
     * write the data of a current letter to the disk
     * @param writeToPosting - what should be written
     * @param invertedIndex - the inverted index
     * @param fileName - the file name as it should be written
     * @param postingNum - the header to the posting num indicating what letter is it
     */
    private void writeFinalPosting(HashMap<String, StringBuilder> writeToPosting, InvertedIndex invertedIndex, String fileName, char postingNum) {
        List<String> keys = new LinkedList<String>(writeToPosting.keySet());
        int k = 0;
        //set the pointers in the inverted index for each term
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
            String word0 = iterator.next();
            String toNum = writeToPosting.get(word0).toString().split("\t")[1];
            int num = Integer.parseInt(toNum);
            invertedIndex.setPointer(word0, k++);
            invertedIndex.setNumOfAppearance(word0, num);
        }
        final HashMap<String, StringBuilder> sendToThread = new HashMap<>(writeToPosting);
        String file = fileName + "_"+ postingNum + ".txt";
        new Thread(()->WriteFile.writeFinalPosting(file, sendToThread)).start();
    }

    /**
     * closes all the buffered readers of the temp postings
     * @param bufferedReaderList the list of the files
     */
    private void closeAllFiles(LinkedList<BufferedReader> bufferedReaderList) {
        bufferedReaderList.forEach(bf -> {
            try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * searches if a term has appeared in a different case
     * @param minTerm the term
     * @param finalPostingLine term's final posting
     * @param writeToPosting - the collection of the final posting
     */
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
            } else replace = writeToPosting.remove(minTerm.toUpperCase()).toString();
            //update the posting with old and new data
            String[] separatePostingAndNumOld = replace.split("\t");
            String[] separatePostingAndNumNew = finalPostingLine.toString().split("\t");
            int numOfAppearance = Integer.parseInt(separatePostingAndNumOld[1]) + Integer.parseInt(separatePostingAndNumNew[1]);
            String oldPosting = separatePostingAndNumOld[0].substring(separatePostingAndNumOld[0].indexOf("~") + 1);
            String newPosting = separatePostingAndNumNew[0].substring(separatePostingAndNumNew[0].indexOf("~") + 1);
            StringBuilder allTogether = new StringBuilder(minTerm + "~" + oldPosting + newPosting + "\t" + numOfAppearance);
            writeToPosting.put(minTerm, allTogether);
        }
        else if (option3 || option2) { //if term appeared in lower case
            if(option2)
                minTerm = minTerm.toLowerCase();
            //update the posting with old and new data
            replace = writeToPosting.get(minTerm).toString();
            String[] separatePostingAndNumOld = replace.split("\t");
            String[] separatePostingAndNumNew = finalPostingLine.toString().split("\t");
            int numOfAppearance = Integer.parseInt(separatePostingAndNumOld[1]) + Integer.parseInt(separatePostingAndNumNew[1]);
            String oldPosting = separatePostingAndNumOld[0].substring(separatePostingAndNumOld[0].indexOf("~") + 1);
            String newPosting = separatePostingAndNumNew[0].substring(separatePostingAndNumNew[0].indexOf("~") + 1);
            StringBuilder allTogether = new StringBuilder(minTerm + "~" + oldPosting + newPosting + "\t" + numOfAppearance);
            writeToPosting.replace(minTerm, allTogether);
        }
        else {
            writeToPosting.put(minTerm, finalPostingLine);
        }
    }

    /**
     * restores the lines that were deleted during merge because a smaller case has appeared
     * @param bufferedReaderList bf list
     * @param minTerm the current term
     * @param firstSentenceOfFile array of current lines
     * @param saveSentences the array that saves all the lines
     */
    private void restoreSentence(LinkedList<BufferedReader> bufferedReaderList,String minTerm,String[] firstSentenceOfFile, String[] saveSentences){
        IntStream.range(0, saveSentences.length).filter(i -> saveSentences[i] != null).forEachOrdered(i -> {
            String[] termAndData = saveSentences[i].split("~");
            firstSentenceOfFile[i] = termAndData[0].compareToIgnoreCase(minTerm) != 0 ? termAndData[0] + "~" + termAndData[1] + "~" + termAndData[2] : getNextSentence(bufferedReaderList.get(i));
        });
    }

    /**
     * checks if the reading from the files is done
     * @param firstSentenceOfFile lines from the
     * @return returns true if reading should continue, or false if reading is done
     */
    private boolean readingIsDone(String[] firstSentenceOfFile) {
        return Arrays.stream(firstSentenceOfFile).anyMatch(Objects::nonNull);
    }

    /**
     * retrieves the next sentence of a file
     * @param bf the buffered reader of the file
     * @return returns the next sentence or null if there is no further reading
     */
    private String getNextSentence(BufferedReader bf){
        String line;
        try {
            if ((line= bf.readLine())!=null) return line;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * initiates an array of all first sentences in all files
     * @param bufferedReaderList the list of buffers
     * @return an array filled with lines
     */
    private String[] initiateMergingArray(LinkedList<BufferedReader> bufferedReaderList){
        String[] firstSentenceOfFile = new String[bufferedReaderList.size()];
        int i = 0;
        for (BufferedReader bf: bufferedReaderList) {
            String line = getNextSentence(bf);
            if(line!= null) firstSentenceOfFile[i] = line;
            i++;
        }
        return firstSentenceOfFile;
    }

    /**
     * initiates the buffered readers of all files
     * @param tempPostingPath the path of the temp postings
     * @return returns a list of all buffers
     */
    private LinkedList<BufferedReader> initiateBufferedReaderList(String tempPostingPath){
        File dirSource = new File(tempPostingPath);
        File[] directoryListing = dirSource.listFiles();
        LinkedList<BufferedReader> bufferedReaderList = new LinkedList<>();
        if (directoryListing != null && dirSource.isDirectory())
            Arrays.stream(directoryListing).filter(file -> file.getName().startsWith("posting")).forEach(file -> {
                try {
                    bufferedReaderList.add(new BufferedReader(new FileReader(file)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        return bufferedReaderList;
    }

    /**
     * deletes all the temporary postings
     * @param destPath the path of the temp postings
     */
    private void deleteTempFiles(String destPath){
        File dirSource = new File(destPath);
        File[] directoryListing = dirSource.listFiles();
        if (directoryListing != null && dirSource.isDirectory())
            Arrays.stream(directoryListing).filter(file -> file.getName().startsWith("posting")).forEach(File::delete);
    }
}