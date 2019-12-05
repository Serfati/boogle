import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NERPipelineDemo {

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        String[] replacements = "~;!?=#&^*+\\|:\"(){}[]<>\n\t".split("");
        FileReader r = new FileReader("/home/serfati/Desktop/Boogle/LICENSE");
        //Reader r = new StringReader("/home/serfati/Desktop/Boogle/LICENSE");
        PTBTokenizer<Word> tokenizer = PTBTokenizer.newPTBTokenizer(r);
        PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>((r), new CoreLabelTokenFactory(), "");
        while(ptbt.hasNext()) {
            CoreLabel label = ptbt.next();
            System.out.print(label+"||");
        }

//        while (tokenizer.hasNext() ) {
//            Word label = tokenizer.next();
//            if (!stringContainsItemFromList(label.word(),replacements)){
//                System.out.println(label.word());
//            }
//        }
        long endTime = System.nanoTime()-startTime;
        System.out.printf("\nTime Complexity :%s%n", endTime * Math.pow(10, -9));


    }

    private static boolean stringContainsItemFromList(String inputStr, String[] items) {
        if (inputStr.equalsIgnoreCase(".") || inputStr.equalsIgnoreCase(","))
            return true;
        return Arrays.stream(items).parallel().anyMatch(inputStr::contains);
    }

    public static String[] tokenize(String sentence) {
        String[] replacements = "'`~;!?=#&^*+\\|:\"(){}[]<>\n\t".split("");

        Reader r = new StringReader(sentence);
        PTBTokenizer<Word> tokenizer = PTBTokenizer.newPTBTokenizer(r);
        List<String> l = new ArrayList<>();
        while(tokenizer.hasNext()) {
            Word w = tokenizer.next();
            l.add(w.word());
        }


        String[] tok = new String[l.size()+1];
        int i = 1;
        for(String s : l)
            tok[i++] = s;
        return tok;
    }
}