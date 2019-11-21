package Parser;

import Model.MyModel;
import Structures.MiniDictionary;
import Structures.cDocument;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.math.NumberUtils.isNumber;
import static org.apache.commons.lang3.StringUtils.replace;

public class Parse implements IParse, Callable<MiniDictionary> {

    private static Logger logger = LogManager.getLogger(Parse.class);
    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
        put("^\\d{8}$", "yyyyMMdd");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
    }};
    private Stemmer stemmer;
    private Boolean useStemming;
    private cDocument currentCDocument;

    private BlockingQueue<cDocument> sourceDocumentsQueue;
    private LinkedList<String> wordList;
    private HashMap<String, Double> weights;
    private HashMap<String, String> monthsData;


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
        return new MiniDictionary();
    }

    public static String determineDateFormat(String dateString) {
        for(String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp))
                return DATE_FORMAT_REGEXPS.get(regexp);
        }
        return null; // Unknown format.
    }

    private boolean isMonth(String term) {
        return monthsData.containsKey(term);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public void reset() {

    }

    private String cleanTerm(String term) {
        if (!term.equals("")) {
            if (!(term.charAt(term.length()-1) == '%')) {
                int i = term.length()-1;
                while(i >= 0 && !Character.isLetterOrDigit(term.charAt(i))) {
                    term = term.substring(0, i);
                    i--;
                }
            }
            if (term.length() > 1 && !(term.charAt(0) == '$') && !isNumber(term)) {
                while(term.length() > 0 && !Character.isLetterOrDigit(term.charAt(0))) {
                    term = term.substring(1);
                }
            }
        }
        return term;
    }

    public String handleLetters(String text, String DocID) {
        return "";
    }

    private String handlePercent(String term, String percentSign) {
        return term+" %";
    }

    private String handleMonthYear(String month, String year) {
        return year+"-"+month;
    }

    private String handleMonthDay(String day, String month) {
        return month+"-"+day;
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
        Double number = Double.parseDouble(price);
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
            word = cleanTerm(word);

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
    public MiniDictionary parse() {

        MiniDictionary miniDic = new MiniDictionary(currentCDocument.getDocId(), currentCDocument.getDocOrgin(), currentCDocument.getDocTitle(), currentCDocument.getDocLang());

        LinkedList<String> nextWord = new LinkedList<>();

        wordList = stringToList(StringUtils.split(currentCDocument.getDocText(), " ~;!?=#&^*+\\|:\"(){}[]<>\n\r\t"));

        String lang = currentCDocument.getDocLang();
        int index = 0;
        while(!wordList.isEmpty()) {
            boolean doStemIfTermWasNotManipulated = false;
            String term = wordList.remove();
            cleanTerm(term);
            if (isNumber(term)) {
                nextWord.add(nextWord());
                if (nextWord.peekFirst().contains("-") && isRangeNumbers(term+""+nextWord.poll()) && !wordList.isEmpty()) {
                    nextWord.addFirst(wordList.pollFirst());

                    term += " "+nextWord.pollLast();

                    if (checkIfFracture(term)) {
                        term += " "+nextWord.pollLast();
                    }

                    //handleMonthDay
                } else if (isMonth(nextWord.peekFirst()) && isInteger(Double.parseDouble(term)) && !wordList.isEmpty()) {
                    String day = nextWord.pollFirst();
                    term = handleMonthDay(day, term);

                    if (!wordList.isEmpty()) {
                        nextWord.add(wordList.pollFirst());
                        if (nextWord.peekFirst() != null && isNumber(nextWord.peekFirst()))
                            nextWord.addFirst(day);
                    }

                } else if (nextWord.peekFirst().equalsIgnoreCase("Dollars")) {
                    nextWord.pollFirst();
                    term = handleDollar(term, term.contains(","));
                } else if (nextWord.peekFirst().equalsIgnoreCase("percent") || nextWord.peekFirst().equalsIgnoreCase("percentage") || nextWord.peekFirst().equals("%")) {
                    term += "%";
                } else if (nextWord.peekFirst().equalsIgnoreCase("Ton") || nextWord.peekFirst().equalsIgnoreCase("Gram")) {
                    term = handleWeight(term, nextWord.pollFirst());
                } else if (isInteger(Double.parseDouble(nextWord.peekFirst()))) {

                }

            } else {


            }
            // START WITH $ SIGN
            if (term.length() >= 1 && isNumber(term.substring(1))) {
                if (term.charAt(0) == '$') {
                    term = handleDollar(term.substring(1).replace(",", ""), term.contains(","));
                }
            } else if (term.length() >= 1 && isNumber(term.substring(0, term.length()-1))) {
                if (!term.substring(0, term.length()-1).equals("%")) {
                    nextWord.addFirst(nextWord());
                    if (term.substring(term.length()-1).equals("m") && nextWord.peekFirst().equals("Dollars"))
                        term = numberValue(Double.parseDouble(term.substring(0, term.length()-1).replace(",", "")))+" M "+nextWord.pollFirst();
                }
            }
            //handleMonthYear
            else if (isMonth(term)) {
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.poll());
                    if (isNumber(nextWord.peekFirst())) {
                        term = handleMonthYear(term, nextWord.pollFirst());
                    }
                }
            }

            //HANDLE RANGES (BETWEEN AND)
            else if (term.equalsIgnoreCase("between")) {

            }


            while(!nextWord.isEmpty()) {
                String s = nextWord.pollLast();
                if (s != null && !s.equals(""))
                    wordList.addFirst(s);

            }

            if (!MyModel.stopWordSet.contains(term.toLowerCase())) {
                if (doStemIfTermWasNotManipulated) {
                    stemmer.getStemmer().setCurrent(term);
                    if (stemmer.getStemmer().stem())
                        term = stemmer.getStemmer().getCurrent();
                }
                //miniDic.addWord(term);
            }
        }
        return miniDic;
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

    private String handleWeight(String term, String unit) {
        Double parseTerm = Double.parseDouble(term);
        parseTerm = parseTerm * weights.get(unit);
        return ""+parseTerm+" Kg";
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
