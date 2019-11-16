package Parser;

import Engine.Stemmer;
import Structures.cDocument;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.math.NumberUtils.isNumber;
import static org.apache.commons.lang3.StringUtils.replace;

@SuppressWarnings({"UNUSED", "MismatchedQueryAndUpdateOfCollection", "FieldCanBeLocal"})
public class Parse implements IParse {
    Stemmer stemmer;

    private Boolean useStemming;
    private String pathToWrite;
    private cDocument currentCDocument;

    private static HashSet<String> stopWordSet;
    private HashMap<String, String> replacements;
    private HashMap<String, String> dates;
    private HashSet<Character> delimiters;
    private HashMap<String, Double> weights;
    private HashMap<String, String> nums;
    private LinkedList<String> nextWord = new LinkedList<>(); //list of next words from the current term

    /**
     * goes over all the terms of label <TEXT>  and parses them according to the rules of the work
     *
     * @return all the data about the terms and the doc
     * @since Nov 13
     */
    public void parse(String text) {

        Map<String, Double> entitiesDiscoveredInDoc = new HashMap<>();

        LinkedList<String> wordList = stringToList(StringUtils.split(currentCDocument.getDocText(), " ~;!?=#&^*+\\|:\"(){}[]<>\n\r\t"));

        String lang = currentCDocument.getDocLang();
        int index = 0;
        while(!wordList.isEmpty()) {
            boolean doStemIfTermWasNotManipulated = false;
            String term = wordList.remove();
            cleanTerm(term);
            if (isNumber(term))
                nextWord.add(nextWord());
        }
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

    private String nextWord() {
        String next =nextWord.peek() ;
        if (nums.containsKey(next))
            return nums.get(next);
        if(dates.containsKey(next))
            return dates.get(next);
        if(weights.containsKey(next))
            return ""+ weights.get(next);//not sure what to do here- because there's kind of a func for that, also in Dollars case
        return next;
    }

    public String getPathToWrite() {
        return pathToWrite;
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

    public void loadStopWordsList(String pathOfStopWords) throws IOException {
        File f = new File(pathOfStopWords);
        StringBuilder allText = new StringBuilder();
        FileReader fileReader = new FileReader(f);
        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while((line = bufferedReader.readLine()) != null)
                allText.append(line).append("\n");
        }
        String[] stopWords = allText.toString().split("\n");
        Collections.addAll(stopWordSet, stopWords);
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
    private void initMonthsData() {
        dates = new HashMap<String, String>() {{
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
