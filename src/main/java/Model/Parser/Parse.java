package Model.Parser;

import Model.Engine.MiniDictionary;
import Model.Model;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Callable;

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
    private LinkedList<String> wordList;
    HashMap<String, String> monthsData;
    HashMap<String, String> wordsRulesData;
    NamedEntitiesSearcher ner;


    public Parse(cDocument corpus_doc, boolean stm) {
        this.currentCDocument = corpus_doc;
        this.useStemming = stm;
        this.stemmer = new Stemmer();
        this.ner = ner;
    }

    public MiniDictionary call() {
        return parse();
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
            word = cleanTerm(word);
            if (!word.equals(""))
                wordsList.add(word);
        }
        return wordsList;
    }

    public MiniDictionary parse() {
        wordList = stringToList(StringUtils.split(currentCDocument.getDocText(), " ~;!?=#&^*+\\|:\"(){}[]<>\n\r\t"));
        MiniDictionary miniDic = new MiniDictionary(currentCDocument.getFileName(), "TO");
        LinkedList<String> nextWord = new LinkedList<>();

        //FRACTION, RANGES,
        /* Stanford CoreNLP 3.9.2 provides a set of human language technology tools. */
        //TODO
        /* ------------------------------------------------------------------------- */

//        CoreDocument doc = new CoreDocument(currentCDocument.getDocText());
//        ner.pipeline().annotate(doc);
//        for(CoreEntityMention em : doc.entityMentions())
//            miniDic.addWord(em.text(), 0);

        //TODO
        /* ------------------------------------------------------------------------- */
        //list of next words from the current term
        initMonthsData();
        nextWordsRules();
        int index = 0;
        while(!wordList.isEmpty()) {
            useStemming = false;
            StringBuilder term = new StringBuilder(wordList.remove());
            if (isNumber(term.toString())) { //if current term is a number
                nextWord.add(nextWord());
                if (Objects.requireNonNull(nextWord.peekFirst()).contains("-") &&
                        isRangeNumbers(nextWord.peekFirst()) &&
                        checkIfFracture(Objects.requireNonNull(nextWord.peekFirst()).substring(0, nextWord.peekFirst().indexOf("-"))) && !wordList.isEmpty())
                {

                    nextWord.addFirst(wordList.pollFirst());
                    term.append(" ").append(nextWord.pollLast());
                    if (checkIfFracture(Objects.requireNonNull(nextWord.peekFirst())))
                        term.append(" ").append(nextWord.pollLast());
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
                    term = new StringBuilder(handleWeight(term.toString(), Objects.requireNonNull(nextWord.pollFirst())));
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
            } else if (term.toString().contains("-") && isRangeNumbers(term.toString())) {
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.pollFirst());
                    if (checkIfFracture(Objects.requireNonNull(nextWord.peekFirst())))
                        term.append(" ").append(nextWord.pollFirst());
                }
            } else if (term.toString().contains("-")) {
                int idx = term.toString().indexOf('-');
                if (idx != -1) {
                    if (term.toString().lastIndexOf('-') == idx+1)
                        term = new StringBuilder(term.toString().replaceFirst("-", ""));
                }
            }
            while(!nextWord.isEmpty()) {
                String s = nextWord.pollLast();
                if (s != null && !s.equals(""))
                    wordList.addFirst(s);
            }

            if (!Model.stopWords.contains(term.toString().toLowerCase())) {
                if (useStemming) {
                    stemmer.getStemmer().setCurrent(term.toString());
                    if (stemmer.getStemmer().stem())
                        term = new StringBuilder(stemmer.getStemmer().getCurrent());
                }
                miniDic.addWord(term.toString(), index);
                index++;

            }

        }

        //long endTime = System.nanoTime()-startTime;
        //System.out.printf("Time Complexity of parser: %s%n sec", endTime * Math.pow(10, -9));
        return miniDic;
    }

    private boolean isRangeNumbers(String range) {

        int idx = range.indexOf('-');
        if (idx != -1) if (checkIfFracture(range.substring(0, idx)))
            return isNumber(range.substring(idx+1)) || checkIfFracture(range.substring(idx+1));
        else if (isNumber(range.substring(0, idx)))
            return isNumber(range.substring(idx+1));
        return false;
    }

    private String handleWeight(String term, String unit) {
        switch(unit) {
            case "Ton":
                term = numberValue(Double.parseDouble(term.replace(",", "")) * 1000);
                break;
            case "Gram":
                term = numberValue(Double.parseDouble(term.replace(",", "")) / 1000);
        }
        return term+" Kilograms";
    }

    private boolean checkIfFracture(String nextWord) {

        int idx = nextWord.indexOf('/');
        if (idx != -1)
            return isNumber(nextWord.substring(0, idx)) && isNumber(nextWord.substring(idx+1));
        return false;
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

    public String handleNumber(String termNumber) {
        double number = Double.parseDouble(termNumber);
        String ans = "";
        int multi = 1000;
        if (number > multi) {//smaller than 1000
            multi *= 1000;
            if (number > multi) {
                multi *= 1000;
                if (number > multi) { // is billion or trillion
                    ans = "B";
                    number = (number / multi);
                } else { // is million
                    ans = "M";
                    multi /= 1000;
                    number = number / multi;
                }
            } else { // is thousand
                ans = "K";
                multi /= 1000;
                number = number / multi;
            }
        }
        return numberValue(number)+ans;
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
            if (wordsRulesData.containsKey(queuePeek)) {
                wordList.remove();
                nextWord = wordsRulesData.get(queuePeek);
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

    private void nextWordsRules() {
        wordsRulesData = new HashMap<String, String>() {{
            put("Thousand", "K");
            put("Thousands", "K");
            put("thousand", "K");
            put("thousands", "K");
            put("Million", "M");
            put("Millions", "M");
            put("million", "M");
            put("millions", "M");
            put("Billion", "B");
            put("Billions", "B");
            put("billion", "B");
            put("billions", "B");
            put("Trillion", "T");
            put("Trillions", "T");
            put("trillion", "T");
            put("trillions", "T");
            put("Minute", "Min");
            put("Minutes", "Min");
            put("minute", "Min");
            put("minutes", "Min");
            put("Second", "Sec");
            put("Seconds", "Sec");
            put("second", "Sec");
            put("seconds", "Sec");
            put("Tons", "Tons");
            put("Ton", "Tons");
            put("tons", "Tons");
            put("ton", "Tons");
            put("Grams", "gr");
            put("Gram", "gr");
            put("grams", "gr");
            put("gram", "gr");
            put("GRAM", "gr");
            put("GRAMS", "gr");
            put("gr", "gr");
            put("kilogram", "kg");
            put("Kilogram", "kg");
            put("kilograms", "kg");
            put("Kilograms", "kg");
            put("KILOGRAM", "kg");
            put("KILOGRAMS", "kg");
            put("kg", "kg");
            put("kgs", "kg");
            put("KG", "kg");
            put("KGS", "kg");
            put("percent", "%");
            put("percentage", "%");
            put("%", "%");
            put("Dollars", "Dollars");
            put("Dollar", "Dollars");
            put("DOLLARS", "Dollars");
            put("DOLLAR", "Dollars");
            put("$", "Dollars");
            put("centimeter", "cm");
            put("Centimeter", "cm");
            put("centimeters", "cm");
            put("Centimeters", "cm");
            put("cm", "cm");
            put("CM", "cm");
            put("meter", "m");
            put("Meter", "m");
            put("METER", "m");
            put("meters", "m");
            put("Meters", "m");
            put("METERS", "m");
            put("kilometer", "km");
            put("Kilometer", "km");
            put("kilometers", "km");
            put("Kilometers", "km");
            put("km", "km");
            put("KILOMETER", "km");
            put("KILOMETERS", "km");
            put("KM", "km");

        }};
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
