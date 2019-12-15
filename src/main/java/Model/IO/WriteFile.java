package Model.IO;

import Model.Engine.DocDictionaryNode;
import Model.Engine.InvertedIndex;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class WriteFile {

    /**
     * writes a tmp posting to the disk
     * @param path the path where it should be written
     * @param postingNum the number of the temp posting
     * @param temporaryPosting an hash map that contains all the contents of the temp posting
     */
    public static void writeTempPosting(String path, int postingNum , HashMap<String, Pair<Integer,StringBuilder>> temporaryPosting) {
        //get all the info needed and write it to dest
        StringBuilder toWrite = new StringBuilder();
        LinkedList<String> sorted = new LinkedList<>(temporaryPosting.keySet());
        sorted.sort(new InvertedIndex.StringComparator());
        for (String s : sorted) {
            int shows = temporaryPosting.get(s).getKey();
            StringBuilder stringBuilder = temporaryPosting.get(s).getValue();
            toWrite.append(s).append("~").append(shows).append("~").append(stringBuilder).append("\n");
        }
        File dir = new File(path);
        File actualFile = new File(dir, "posting_" + postingNum + ".txt");
        write(actualFile, toWrite);
    }

    /**
     * writes the doc dictionary to the disk
     *
     * @param path               the path where it should be written
     * @param documentDictionary the document dictionary
     * @param stem               if terms were stemmed
     */
    public static void writeDocDictionary(String path, HashMap<String, DocDictionaryNode> documentDictionary, boolean stem) {
        StringBuilder toWrite = new StringBuilder();
        for(DocDictionaryNode cur : documentDictionary.values()) {
            toWrite.append(cur.toString());
        }
        File dir = new File(path);
        String fileName = "StemDocumentDictionary.txt";
        if (!stem)
            fileName = "DocumentDictionary.txt";
        File actualFile = new File(dir, fileName);
        write(actualFile, toWrite);
    }

    /**
     * writes the inverted index to the disk
     * @param path the path where it should be written
     * @param invertedIndex the inverted index
     * @param stem if terms were stemmed
     */
    public static void writeInvertedFile(String path, InvertedIndex invertedIndex, boolean stem) {
        String toWrite = invertedIndex.toString();
        File dir = new File(path);
        String fileName = "StemInvertedFile.txt";
        if (!stem)
            fileName= "InvertedFile.txt";
        File actualFile = new File(dir,fileName);
        write(actualFile,new StringBuilder(toWrite));
    }

    /**
     * writes a final posting to the disk
     * @param fileName the file name
     * @param finalPosting the entire final posting
     */
    public static void writeFinalPosting(String fileName, HashMap<String, StringBuilder> finalPosting) {
        StringBuilder ans = new StringBuilder();
        StringBuilder[] sbArray = finalPosting.values().toArray(new StringBuilder[0]);
        try {
            File file = new File(fileName);
            FileWriter fileWriter = new FileWriter(file, true);
            int i = 0;
            while(i <= sbArray.length / 1000) {
                for(int j = 0; j < 1000 && (j+i * 1000) < sbArray.length; j++)
                    ans.append(sbArray[j+i * 1000]).append("\n");
                fileWriter.write(ans.toString());
                ans.delete(0, ans.length());
                i++;
            }
            fileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write a certain string to the file
     * @param actualFile the file to be written to
     * @param toWrite what should be written
     */
    private static void write(File actualFile, StringBuilder toWrite){
        try {
            FileWriter fileWriter = new FileWriter(actualFile);
            fileWriter.write(toWrite.toString());
            fileWriter.close();
            toWrite.delete(0,toWrite.length());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
