package Model.Parser;

import Model.MyModel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.process.PTBTokenizer;
import org.apache.log4j.Logger;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.math.NumberUtils.isNumber;

/**
 * Callable.
 * Takes Documents, tokenizes and parses them.
 */

public class Parse implements IParse, Callable<MiniDictionary> {

    private final Logger logger = Logger.getLogger(Parse.class);
    private Stemmer stemmer;
    private Boolean useStemming;
    private cDocument currentCDocument;

    private BlockingQueue<cDocument> sourceDocumentsQueue;
    PTBTokenizer<Word> tokenizer;
    private LinkedList<String> wordList;
    HashMap<String, String> monthsData;


    public Parse(cDocument corpus_doc, boolean stm) {
        this.currentCDocument = corpus_doc;
        this.useStemming = stm;
        this.stemmer = new Stemmer();
    }

    public MiniDictionary call() {
        try {
            return parse();
        } catch(Exception e) {
            logger.error("PARSE :: DOC FAILD : a thread prosses - call()");
        }
        return null;
    }


    private String numberValue(Double d) {
        if (isInteger(d))
            return ""+d.intValue();
        return ""+d;
    }

    private boolean isInteger(double word) {
        return word == Math.floor(word) && !Double.isInfinite(word);
    }

    private String handleDollar(String price, boolean containsComma) {
        double number = Double.parseDouble(price);
        String ans = "";
        int multi = 1000000;
        if (number >= multi) {
            ans = "M";
            number /= multi;
        }
        String nextWord = nextWord();
        if (nextWord.equals("M"))
            ans = "M";
        else if (nextWord.equals("B")) {
            number *= 1000;
            ans = "M";
        }
        if (ans.equals("")) {
            if (containsComma)
                return addCommas(numberValue(number))+" Dollars";
            else
                return numberValue(number)+" Dollars";
        }
        return numberValue(number)+" "+ans+" Dollars";
    }

    private String addCommas(String number) {
        String saveFraction = "";
        if (number.indexOf('.') != -1) {
            saveFraction = number.substring(number.indexOf('.'));
            number = number.substring(0, number.indexOf('.'));
        }
        for(int i = number.length()-3; i > 0; i -= 3) {
            number = number.substring(0, i)+","+number.substring(i);
        }
        return number+saveFraction;
    }

    private LinkedList<String> stringToList(String[] split) {
        LinkedList<String> wordsList = new LinkedList<>();
        for(String word : split) {
            if (!word.equals(""))
                wordsList.add(word);
        }
        return wordsList;
    }

    /**
     * goes over all the terms of label <TEXT>  and parses them according to the rules of the work
     *
     * @return all the data about the terms and the doc
     * @since Nov 13
     */

    private boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).parallel().anyMatch(inputStr::contains);
    }

    public MiniDictionary parse() {
        long startTime = System.nanoTime();

        MiniDictionary miniDic = new MiniDictionary(currentCDocument.getDocId(), currentCDocument.getDocTitle(), currentCDocument.getDocLang());
        LinkedList<String> nextWord = new LinkedList<>();
        //FRACTION, RANGES,
        /* Stanford CoreNLP 3.9.2 provides a set of human language technology tools. */
        //TODO
        /* ------------------------------------------------------------------------- */
        NamedEntitiesSearcher ner = new NamedEntitiesSearcher();
        CoreDocument doc = new CoreDocument(currentCDocument.getDocText());
        ner.pipeline().annotate(doc);
        for(CoreEntityMention em : doc.entityMentions())
            miniDic.addWord(em.text(), 0);
        String[] replacements = "~;!?=#&^*+\\|:\"(){}[]<>\n\t".split("");
        Reader r = new StringReader(currentCDocument.getDocText());
        tokenizer = PTBTokenizer.newPTBTokenizer(r);
        while(tokenizer.hasNext()) {
            Word label = tokenizer.next();
            if (!stringContainsItemFromList(label.word(), replacements)) {
                wordList.add(label.word());
            }
        }
        //TODO
        /* ------------------------------------------------------------------------- */
        initMonthsData();
        int index = 0;
        while(!wordList.isEmpty()) {
            useStemming = false;
            StringBuilder term = new StringBuilder(wordList.remove());
            if (isNumber(term.toString())) { //if current term is a number
                nextWord.add(nextWord());
                if (Objects.requireNonNull(nextWord.peekFirst()).contains("-") && isRangeNumbers(nextWord.peekFirst()) && checkIfFracture(Objects.requireNonNull(nextWord.peekFirst()).substring(0, nextWord.peekFirst().indexOf("-"))) && !wordList.isEmpty()) {
                    nextWord.addFirst(wordList.pollFirst());
                    term.append(" ").append(nextWord.pollLast());
                    if (checkIfFracture(Objects.requireNonNull(nextWord.peekFirst()))) {
                        term.append(" ").append(nextWord.pollLast());
                    }
                } else if (monthsData.containsKey(nextWord.peekFirst()) && isInteger(Double.parseDouble(term.toString()))) {  // rule Hei -  Month term
                    String save = nextWord.pollFirst();

                    term.insert(0, save+"-");
                    if (!wordList.isEmpty()) {
                        nextWord.add(wordList.pollFirst());
                        if (nextWord.peekFirst() != null && isNumber(nextWord.peekFirst()) && Objects.requireNonNull(nextWord.peekFirst()).length() == 4) {
                            nextWord.addFirst(save);
                        }
                    }
                } else if (Objects.requireNonNull(nextWord.peekFirst()).equalsIgnoreCase("Dollars")) {  //if it is rule Dalet - it is a Dollar term
                    nextWord.pollFirst();
                    term = new StringBuilder(handleDollar(term.toString().replace(",", ""), term.toString().contains(",")));
                } else if (Objects.equals(nextWord.peekFirst(), "%")) { //  rule Gimel - percent term
                    term.append('%');
                } else if (Objects.equals(nextWord.peekFirst(), "Ton") || nextWord.peekFirst().equals("Gram")) {
                    //term = new StringBuilder(handleWeight(term.toString(), Objects.requireNonNull(nextWord.pollFirst())));
                } else {
                    term = new StringBuilder(handleNumber(term.toString().replace(",", "")));
                    if (!(term.charAt(term.length()-1) > 'A' && term.charAt(term.length()-1) < 'Z')) { //if a number returned is smaller than 1000
                        if (Objects.equals(nextWord.peekFirst(), "T")) {
                            term = new StringBuilder(numberValue(Double.parseDouble(term.toString()) * 1000));
                            nextWord.pollFirst();
                            nextWord.addFirst("B");
                        }
                        if (Objects.requireNonNull(nextWord.peekFirst()).length() == 1)
                            term.append(nextWord.pollFirst());

                        if (!nextWord.isEmpty() && nextWord.peekFirst().equals(""))
                            nextWord.clear();

                        if (!wordList.isEmpty()) {
                            nextWord.addLast(wordList.poll());
                            if (checkIfFracture(Objects.requireNonNull(nextWord.peekFirst()))) { //rule Alef 2 - fraction rule
                                term.append(" ").append(nextWord.pollFirst());
                                nextWord.addFirst(nextWord());
                                if (Objects.equals(nextWord.peekFirst(), "Dollars"))
                                    term.append(" ").append(nextWord.pollFirst());
                            } else if (!wordList.isEmpty() && Objects.equals(nextWord.peekFirst(), "U.S")) {
                                nextWord.addFirst(wordList.poll());
                                if (Objects.requireNonNull(nextWord.peekFirst()).equalsIgnoreCase("dollars")) {
                                    nextWord.clear();
                                    double d;
                                    if (Character.isLetter(term.charAt(term.length()-1)))
                                        d = Double.parseDouble(term.substring(0, term.length()-1));
                                    else
                                        d = Double.parseDouble(term.toString());
                                    if (term.charAt(term.length()-1) == 'M')
                                        d *= 1000000;
                                    else if (term.charAt(term.length()-1) == 'B') {
                                        d *= 1000000000;
                                    }
                                    term = new StringBuilder(handleDollar(""+d, term.toString().contains(",")));
                                }
                            }
                        }
                    }
                }
            } else if (term.length() >= 1 && isNumber(term.substring(1))) {
                if (term.charAt(0) == '$') { //rule Dalet - dollar sign at the beginning of a number
                    try {
                        term = new StringBuilder(handleDollar(term.substring(1).replace(",", ""), term.toString().contains(",")));
                    } catch(NumberFormatException e) {
                        e.getCause();
                    }
                }
            } else if (term.length() >= 1 && isNumber(term.substring(0, term.length()-1))) {
                if (!term.substring(0, term.length()-1).equals("%")) {
                    nextWord.addFirst(nextWord());
                    if (term.substring(term.length()-1).equals("m") && Objects.equals(nextWord.peekFirst(), "Dollars"))
                        term = new StringBuilder(numberValue(Double.parseDouble(term.substring(0, term.length()-1).replace(",", "")))+" M "+nextWord.pollFirst());

                }
            } else if (term.length() >= 2 && isNumber(term.substring(0, term.length()-2)) && term.substring(term.length()-2).equals("bn")) {
                nextWord.addFirst(nextWord());
                if (Objects.equals(nextWord.peekFirst(), "Dollars"))
                    term = new StringBuilder(numberValue(Double.parseDouble(term.substring(0, term.length()-2).replace(",", "")) * 1000)+" M "+nextWord.pollFirst());


            } else if (monthsData.containsKey(term.toString())) { // rule Vav - month year rule
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.poll());
                    if (isNumber(nextWord.peekFirst())) term.insert(0, nextWord.pollFirst()+"-");
                }
            } else if (term.toString().equalsIgnoreCase("between")) {
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.poll());
                    if ((isNumber(nextWord.peekFirst()) || checkIfFracture(Objects.requireNonNull(nextWord.peekFirst()))) && !wordList.isEmpty()) {
                        nextWord.addFirst(wordList.pollFirst());
                        if (checkIfFracture(Objects.requireNonNull(nextWord.peekFirst())) && !wordList.isEmpty())
                            nextWord.addFirst(wordList.pollFirst());

                        if (Objects.requireNonNull(nextWord.peekFirst()).equalsIgnoreCase("and") && !wordList.isEmpty()) {
                            nextWord.addFirst(wordList.pollFirst());
                            if (isNumber(nextWord.peekFirst()) || checkIfFracture(Objects.requireNonNull(nextWord.peekFirst()))) {
                                while(!nextWord.isEmpty())
                                    term.append(" ").append(nextWord.pollLast());
                                if (!wordList.isEmpty()) {
                                    nextWord.addFirst(wordList.pollFirst());
                                    assert nextWord.peekFirst() != null;
                                    if (checkIfFracture(nextWord.peekFirst()) && !wordList.isEmpty())
                                        term.append(" ").append(nextWord.pollFirst());
                                }
                            }
                        }
                    }
                }
            }

            while(!nextWord.isEmpty()) {
                String s = nextWord.pollLast();
                if (s != null && !s.equals(""))
                    wordList.addFirst(s);
            }

            if (!MyModel.stopWords.contains(term.toString().toLowerCase())) {
                if (useStemming) {
                    stemmer.getStemmer().setCurrent(term.toString());
                    if (stemmer.getStemmer().stem())
                        term = new StringBuilder(stemmer.getStemmer().getCurrent());
                }
                miniDic.addWord(term.toString(), index);
                index++;
            }
        }
        long endTime = System.nanoTime()-startTime;
        System.out.printf("Time Complexity of parser: %s%n sec", endTime * Math.pow(10, -9));
        return miniDic;
    }

    private boolean isRangeNumbers(String s) {
        return true;
    }

    private boolean checkIfFracture(String token) {
        if (token.contains("/")) {
            token = replace(token, ",", "");
            String[] check = split(token, "/");
            if (check.length < 2)
                return false;
            try {
                Integer.parseInt(check[0]);
                Integer.parseInt(check[1]);
                return true;
            } catch(NumberFormatException e) {
                logger.error("NumberFormatException in PARSE :: checkIfFracture ");
                return false;
            }
        }
        return false;
    }

    private String handleNumber(String number) {
        StringBuilder ans = new StringBuilder();
        ans.append(number);
        String check="";//check there are only 3 digits after the dot
        while(ans.toString().contains(","))
            ans.deleteCharAt(ans.indexOf(","));
        double num = Double.parseDouble(ans.toString());
        check= check+num;
        ans.delete(0, ans.length());
        if (num < 1000) {
            return number;
        } else if (num < 1000000) {
            num /= 1000;
            if(check.contains(".")){
                num=threeDigit(check);
            }
            ans.append(num).append("K");
        } else if (num < 1000000000) {
            num /= 1000000;
            if(check.contains(".")){
                num=threeDigit(check);
            }
            ans.append(num).append("M");
        } else {
            num /= 1000000000;
            if(check.contains(".")){
                num=threeDigit(check);
            }
            ans.append(num).append("B");
        }
        if (ans.toString().substring(ans.toString().indexOf("."), ans.toString().length()-1).equals(".0")) {
            ans.delete(ans.toString().length()-3, ans.toString().length()-1);
        }
        return ans.toString();
    }

    /**
     * Checks if the next word is one of certain rules given to the parser
     *
     * @return returns a string according to the rules
     */
    private String nextWord() {
        String nextWord = "";
        if (!wordList.isEmpty()) {
            String queuePeek = wordList.peek();
            if (queuePeek.equalsIgnoreCase("Thousand")) {
                wordList.remove();
                nextWord = "K";
            } else if (queuePeek.equalsIgnoreCase("Million")) {
                wordList.remove();
                nextWord = "M";
            } else if (queuePeek.equalsIgnoreCase("Billion")) {
                wordList.remove();
                nextWord = "B";
            } else if (queuePeek.equalsIgnoreCase("Trillion")) {
                wordList.remove();
                nextWord = "T";
            } else if (queuePeek.equalsIgnoreCase("Tons")) {
                wordList.remove();
                nextWord = "Ton";
            } else if (queuePeek.equalsIgnoreCase("Grams")) {
                wordList.remove();
                nextWord = "Gram";
            } else if (queuePeek.equalsIgnoreCase("percent") || queuePeek.equalsIgnoreCase("percentage")) {
                wordList.remove();
                nextWord = "%";
            } else if (queuePeek.equalsIgnoreCase("Dollars")) {
                wordList.remove();
                nextWord = "Dollars";
            } else if (monthsData.containsKey(queuePeek)) {
                wordList.remove();
                nextWord = queuePeek;
            } else if (queuePeek.contains("-")) {
                wordList.remove();
                nextWord = queuePeek;
            }
        }
        return nextWord;
    }

    private double threeDigit(String check) {
        StringBuilder ans = new StringBuilder();
        String s = ans.substring(0, 1);
        double correct;
        while(s.equals(".")) {
            ans.append(s);
            check = check.substring(1);
            s = ans.substring(0, 1);
        }
        ans.append(s);
        check = check.substring(1);
        if (check.length() <= 3) {
            ans.append(check);
            correct = Double.parseDouble(ans.toString());
        } else {
            ans.append(check, 0, 3);
            correct = Double.parseDouble(ans.toString());
        }
        return correct;
    }

    private void initMonthsData() {
        monthsData = new HashMap<String, String>() {{
            put("Jan", "01");
            put("Feb", "02");
            put("Mar", "03");
            put("Apr", "04");
            put("May", "05");
            put("Jun", "06");
            put("Jul", "0");
            put("Aug", "08");
            put("Sep", "09");
            put("Oct", "10");
            put("Nov", "11");
            put("Dec", "12");
            put("Sept", "09");
            put("January", "01");
            put("February", "02");
            put("March", "03");
            put("April", "04");
            put("June", "06");
            put("July", "07");
            put("August", "08");
            put("September", "09");
            put("October", "10");
            put("November", "11");
            put("December", "12");
            put("JANUARY", "01");
            put("FEBRUARY", "02");
            put("MARCH", "03");
            put("APRIL", "04");
            put("MAY", "05");
            put("JUNE", "06");
            put("JULY", "07");
            put("AUGUST", "08");
            put("SEPTEMBER", "09");
            put("OCTOBER", "10");
            put("NOVEMBER", "11");
            put("DECEMBER", "12");
        }};
    }
}
