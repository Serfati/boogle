package Parser;

import Engine.Stemmer;
import Structures.MiniDictionary;
import Structures.cDocument;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.math.NumberUtils.isNumber;
import static org.apache.commons.lang3.StringUtils.replace;

@SuppressWarnings({"UNUSED", "MismatchedQueryAndUpdateOfCollection", "FieldCanBeLocal"})
public class Parse implements IParse, Runnable {


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
    private static HashSet<String> stopWordSet;
    private BlockingQueue<cDocument> sourceDocumentsQueue;


    private HashMap<String, String> replacements;
    private LinkedList<String> wordList;
    private HashSet<Character> delimiters;
    private HashMap<String, Double> weights;
    private HashMap<String, String> nums;
    private HashMap<String, String> monthsData;

    /**
     * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
     * format is unknown. You can simply extend DateUtil with more formats if needed.
     * @param dateString The date string to determine the SimpleDateFormat pattern for.
     * @return The matching SimpleDateFormat pattern, or null if format is unknown.
     */
    private static String determineDateFormat(String dateString) {
        for(String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp))
                return DATE_FORMAT_REGEXPS.get(regexp);
        }
        return null; // Unknown format.
    }

    @Override
    public boolean isDone() {
        return false;
    }

    public void setDone(boolean done) {
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

    private String handleWeight(String term, String unit) {
        Double parseTerm= Double.parseDouble(term);
        parseTerm= parseTerm* weights.get(unit);
        String ans= ""+parseTerm+" Kg";
        return ans;
    }

    private String cleanTerm(String term) {
        return "0";
    }

    public String handleLetters(String text, String DocID) {
        return "";
    }

    public void reset() {
    }

    /**
     * goes over all the terms of label <TEXT>  and parses them according to the rules of the work
     *
     * @return all the data about the terms and the doc
     * @since Nov 13
     */
    public void parse(String text) {

        MiniDictionary entitiesDiscoveredInDoc = MiniDictionary;

        LinkedList<String> nextWord = new LinkedList<>();

        wordList = stringToList(StringUtils.split(currentCDocument.getDocText(), " ~;!?=#&^*+\\|:\"(){}[]<>\n\r\t"));

        String lang = currentCDocument.getDocLang();
        int index = 0;
        while(!wordList.isEmpty()) {
            boolean doStemIfTermWasNotManipulated = false;
            String term = wordList.remove();
            cleanTerm(term);
            if(isNumber(term)) {
                nextWord.add(nextWord());
                if (nextWord.peekFirst().contains("-") && isRangeNumbers(term + "" + nextWord.poll()) && !wordList.isEmpty()) {
                    nextWord.addFirst(wordList.pollFirst());

                    term += " " + nextWord.pollLast();

                    if (checkIfFracture(term)) {
                        term += " " + nextWord.pollLast();
                    }
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
                }
                else if ( isInteger(Double.parseDouble(nextWord.peekFirst()))){

                }

            }else{


            }
            if (term.length()>=1 && isNumber(term.substring(1))) {
                if (term.charAt(0) == '$') {
                    term = handleDollar(term.substring(1).replace(",", ""), term.contains(","));
                }
            }
            else if (term.length() >= 1 && isNumber(term.substring(0, term.length() - 1))) {
                if (!term.substring(0, term.length() - 1).equals("%")) {
                    nextWord.addFirst(nextWord());
                    if (term.substring(term.length() - 1).equals("m") && nextWord.peekFirst().equals("Dollars"))
                        term = numberValue(Double.parseDouble(term.substring(0, term.length() - 1).replace(",",""))) + " M " + nextWord.pollFirst();
                }
            }



            else if (isMonth(term) ){
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.poll());
                    if (isNumber(nextWord.peekFirst())) {
                        term = handleMonthYear(term, nextWord.pollFirst());
                    }
                }
            }
            else if (term.equalsIgnoreCase("between")) {

            }

            if(useStemming && !stopWordSet.contains(term)) {
                stemmer.add(term);
                minidic.add(term);
            }



        }
    }

    private String numberValue(Double d) {
        if (isInteger(d))
            return ""+d.intValue();
        return ""+d;
    }
    private boolean isInteger(double word) {
        return word == Math.floor(word) && !Double.isInfinite(word);
    }
//            else if (term.length() >= 2 && isNumber(term.substring(0, term.length() - 2))) {
////                nextWord.addFirst(nextWord());
////                if (nextWord.peekFirst().equals("Dollars"))
////                    term = numberValue(term.substring(0, term.length() - 2).replace(",","") * 1000) + " M " + nextWord.pollFirst();

    //            }
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
                return false;
            }
        }
        return false;
    }

    public void run() {
        try {
            parse(currentCDocument.getDocText());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String checkKorMorB(String number) {
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

    private double threeDigit(String check){
        String ans="";
        String s= ans.substring(0,1);
        double correct;
        while(s.equals(".")){
            ans=ans+s;
            check=check.substring(1);
            s=ans.substring(0,1);
        }
        ans=ans+s;
        check=check.substring(1);
        if(check.length()<=3){
            ans=ans+check;
            correct= Double.parseDouble(ans);
        } else{
            ans=ans+check.substring(0,3);
            correct=Double.parseDouble(ans);
        }
        return  correct;
    }
    public void initReplacements() {
        replacements = new HashMap<String, String>() {{
            put(",", "");
            put("th", "");
            put("$", "");
            put("%", "");
            put(":", "");
        }};
    }

    private void initDelimiters() {
        delimiters = new HashSet<Character>() {{
            add('.');
            add(',');
            add(':');
            add('!');
            add('\"');
            add('#');
            add('(');
            add(')');
            add('[');
            add('@');
            add('+');
            add(']');
            add('|');
            add(';');
            add('?');
            add('&');
            add('\'');
            add('*');
            add('-');
            add('}');
            add('`');
            add('/');
            add(' ');
            add('\n');
            add('{');
            add('~');
        }};
    }
    private void convertWeightToKG (){
        weights= new HashMap<String, Double>(){{
            put ("Kilogram", 1.0);
            put ("Kg", 1.0);
            put ("kg", 1.0);
            put ("KG", 1.0);
            put ("KILOGRAM", 1.0);
            put ("GRAM", 0.001);
            put ("Gr", 0.001);
            put ("gr",0.001);
            put ("GR", 0.001);
            put ("gram", 0.001);
            put ("Gram", 0.001);
            put ("Ton", 1000.0);
            put ("ton", 1000.0);
            put ("TON", 1000.0);
            put ("T", 1000.0);
        }};
    }

    private void convertNum (){
        nums= new HashMap<String, String>(){{
            put ("Thousand", "K");
            put ("thousand", "K");
            put ("Million", "M");
            put ("million", "M");
            put ("Billion", "B");
            put ("billion", "B");
        }};
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
            } else if (queuePeek.contains("-")) {
                wordList.remove();
                nextWord = queuePeek;
            }
        }
        return nextWord;
    }

    public void loadStopWordsList(String pathOfStopWords) throws IOException {

        File f = new File(pathOfStopWords);
        StringBuilder allText = new StringBuilder();
        FileReader fileReader = new FileReader(f);
        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while((line = bufferedReader.readLine()) != null)
                allText.append(line).append("\n");
            String[] stopWords = allText.toString().split("\n");
            Collections.addAll(stopWordSet, stopWords);
        } catch(IOException e) {
            System.out.println("stopwords file not found in the specified path. running without stopwords");
        }
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
