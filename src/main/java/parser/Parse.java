package parser;

import model.Model;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Callable;

public class Parse implements Callable<MiniDictionary>, IParse {

    HashMap<String, String> monthsData;
    HashMap<String, String> wordsRulesData;
    NamedEntitiesSearcher ner;
    private LinkedList<String> wordList;
    private cDocument corpus_doc;
    private Stemmer ps;
    private boolean stm;


    public Parse(cDocument corpus_doc, boolean stm) {
        this.corpus_doc = corpus_doc;
        this.stm = stm;
        this.ps = new Stemmer();
    }

    public MiniDictionary call() {
        return parse();
    }

    public MiniDictionary parse() {
        //split the <Text> label to list of terms
        wordList = stringToList(StringUtils.split(corpus_doc.getDocText(), " ~;!?=#&^*+\\|:\"(){}[]<>\n\r\t"));
        //list of next words from the current term
        LinkedList<String> nextWord = new LinkedList<>();
        //the mini dictionary that will be filled according to the terms
        MiniDictionary miniDic = new MiniDictionary(corpus_doc.getDocNum(), corpus_doc.getDocTitle());
        //the index of the

        //FRACTION, RANGES,
        /* Stanford CoreNLP 3.9.2 provides a set of human language technology tools. */
        //TODO
        /* ------------------------------------------------------------------------- */

//        CoreDocument doc = new CoreDocument(corpus_doc.getDocText());
//        ner.pipeline().annotate(doc);
//        for(CoreEntityMention em : doc.entityMentions())
//            miniDic.addWord(em.text().toUpperCase(), 0);

        //TODO
        /* ------------------------------------------------------------------------- */

//        initMonthsData();
//        nextWordsRules();
        int index = 0;
        while(!wordList.isEmpty()) {
            boolean doStemIfTermWasNotManipulated = false;
            String term = wordList.remove();
            if (isNumber(term)) { //if current term is a number
                nextWord.add(nextWord());
                assert nextWord.peekFirst() != null;
                if (nextWord.peekFirst() != null) {
                    if (nextWord.peekFirst().contains("-") && isRangeNumbers(nextWord.peekFirst()) && checkIsFraction(Objects.requireNonNull(nextWord.peekFirst()).substring(0, nextWord.peekFirst().indexOf("-"))) && !wordList.isEmpty()) {
                        nextWord.addFirst(wordList.pollFirst());
                        term += " "+nextWord.pollLast();
                        if (nextWord.peekFirst() != null && checkIsFraction(nextWord.peekFirst())) {
                            term += " "+nextWord.pollLast();
                        }
                    } else if (isMonth(nextWord.peekFirst()) != -1 && isInteger(term)) {  //if it is rule Hei - it is a Month term
                        term = handleMonth(nextWord, term);
                    } else {
                        assert nextWord.peekFirst() != null;
                        if (nextWord.peekFirst().equalsIgnoreCase("Dollars")) {  //if it is rule Dalet - it is a Dollar term
                            nextWord.pollFirst();
                            term = handleDollar(Double.parseDouble(term.replace(",", "")), term.contains(","));
                        } else if (nextWord.peekFirst().equals("%")) { // if it is rule Gimel - it is a percent term
                            term = handlePercent(term, nextWord.pollFirst());
                        } else if (nextWord.peekFirst().equalsIgnoreCase("centimeter") || nextWord.peekFirst().equalsIgnoreCase("meter")) {
                            term = handleDistance(term, Objects.requireNonNull(nextWord.pollFirst()));
                        } else if (nextWord.peekFirst().equals("Ton") || nextWord.peekFirst().equals("Gram")) {
                            term = handleWeight(term, Objects.requireNonNull(nextWord.pollFirst()));
                        } else {
                            term = handleNumber(Double.parseDouble(term.replace(",", "")));
                            if (!(term.charAt(term.length()-1) > 'A' && term.charAt(term.length()-1) < 'Z')) { //if a number returned is smaller than 1000
                                if (nextWord.peekFirst() != null && Objects.equals(nextWord.peekFirst(), "T")) {
                                    term = numberValue(Double.parseDouble(term) * 1000);
                                    nextWord.pollFirst();
                                    nextWord.addFirst("B");
                                }
                                if (Objects.requireNonNull(nextWord.peekFirst()).length() == 1)
                                    term += nextWord.pollFirst();

                                if (!nextWord.isEmpty() && nextWord.peekFirst().equals(""))
                                    nextWord.clear();

                                term = dealWithDollars(nextWord, term);
                            }
                        }
                    }
                }
            } else if (term.length() >= 1 && isNumber(term.substring(1))) {
                term = startWithDollar(term);


            } else if (term.length() >= 1 && isNumber(term.substring(0, term.length()-1))) {
                if (!term.substring(0, term.length()-1).equals("%")) {
                    nextWord.addFirst(nextWord());
                    if (term.substring(term.length()-1).equals("m") && Objects.requireNonNull(nextWord.peekFirst()).equals("Dollars"))
                        term = numberValue(Double.parseDouble(term.substring(0, term.length()-1).replace(",", "")))+" M "+nextWord.pollFirst();

                }
            } else if (term.length() >= 2 && isNumber(term.substring(0, term.length()-2)) && term.substring(term.length()-2).equals("bn")) {
                nextWord.addFirst(nextWord());
                if (Objects.requireNonNull(nextWord.peekFirst()).equals("Dollars"))
                    term = numberValue(Double.parseDouble(term.substring(0, term.length()-2).replace(",", "")) * 1000)+" M "+nextWord.pollFirst();


            } else if (isMonth(term) != -1) { // rule Vav - month year rule
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.poll());
                    if (isNumber(Objects.requireNonNull(nextWord.peekFirst()))) {
                        term = handleMonthYear(term, nextWord.pollFirst());
                    }
                }
            } else if (term.equalsIgnoreCase("between")) {
                term = handleRange(nextWord, term);
            } else if (term.contains("-") && isRangeNumbers(term)) {
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.pollFirst());
                    if (checkIsFraction(Objects.requireNonNull(nextWord.peekFirst())))
                        term += " "+nextWord.pollFirst();
                }
            } else if (term.contains("-")) {
                term = handleRangesSign(term);
            } else if (stm) {
                doStemIfTermWasNotManipulated = true;
            }
            index = lastCheckAndAdd(nextWord, miniDic, index, doStemIfTermWasNotManipulated, term);
        }
        return miniDic;
    }

    private String handleRange(LinkedList<String> nextWord, String term) {
        if (wordList.isEmpty())
            return term;
        nextWord.addFirst(wordList.poll());
        if ((isNumber(Objects.requireNonNull(nextWord.peekFirst())) || checkIsFraction(Objects.requireNonNull(nextWord.peekFirst()))) && !wordList.isEmpty()) {
            nextWord.addFirst(wordList.pollFirst());
            if (checkIsFraction(Objects.requireNonNull(nextWord.peekFirst())) && !wordList.isEmpty())
                nextWord.addFirst(wordList.pollFirst());

            if (Objects.requireNonNull(nextWord.peekFirst()).equalsIgnoreCase("and") && !wordList.isEmpty()) {
                nextWord.addFirst(wordList.pollFirst());
                if (isNumber(Objects.requireNonNull(nextWord.peekFirst())) || checkIsFraction(Objects.requireNonNull(nextWord.peekFirst()))) {
                    StringBuilder termBuilder = new StringBuilder(term);
                    while(!nextWord.isEmpty())
                        termBuilder.append(" ").append(nextWord.pollLast());
                    term = termBuilder.toString();
                    if (!wordList.isEmpty()) {
                        nextWord.addFirst(wordList.pollFirst());
                        if (checkIsFraction(Objects.requireNonNull(nextWord.peekFirst())) && !wordList.isEmpty())
                            term += " "+nextWord.pollFirst();

                    }
                }
            }

        }
        return term;
    }

    private String handleMonth(LinkedList<String> nextWord, String term) {
        String save = nextWord.pollFirst();
        term = handleMonthDay(save, term);
        if (!wordList.isEmpty()) {
            nextWord.add(wordList.pollFirst());
            if (nextWord.peekFirst() != null && nextWord.peekFirst() != null && isNumber(nextWord.peekFirst()) && Objects.requireNonNull(nextWord.peekFirst()).length() == 4) {
                nextWord.addFirst(save);
            }
        }
        return term;
    }

    private String startWithDollar(String term) {
        if (term.charAt(0) == '$') { //rule Dalet - dollar sign at the beginning of a number
            try {
                term = handleDollar(Double.parseDouble(term.substring(1).replace(",", "")), term.contains(","));
            } catch(NumberFormatException e) {
                e.getCause();
            }
        }
        return term;
    }

    private String dealWithDollars(LinkedList<String> nextWord, String term) {
        if (!wordList.isEmpty()) {
            nextWord.addLast(wordList.poll());
            if (checkIsFraction(Objects.requireNonNull(nextWord.peekFirst()))) { //rule Alef 2 - fraction rule
                term += " "+nextWord.pollFirst();
                nextWord.addFirst(nextWord());
                if (Objects.equals(nextWord.peekFirst(), "Dollars"))
                    term += " "+nextWord.pollFirst();
            } else if (!wordList.isEmpty() && Objects.equals(nextWord.peekFirst(), "U.S")) {
                nextWord.addFirst(wordList.poll());
                if (Objects.requireNonNull(nextWord.peekFirst()).equalsIgnoreCase("dollars")) {
                    nextWord.clear();
                    double d;
                    if (Character.isLetter(term.charAt(term.length()-1)))
                        d = Double.parseDouble(term.substring(0, term.length()-1));
                    else
                        d = Double.parseDouble(term);
                    if (term.charAt(term.length()-1) == 'M')
                        d *= 1000000;
                    else if (term.charAt(term.length()-1) == 'B') {
                        d *= 1000000000;
                    }
                    term = handleDollar(d, term.contains(","));
                }
            }
        }
        return term;
    }

    private int lastCheckAndAdd(LinkedList<String> nextWord, MiniDictionary miniDic, int index, boolean doStemIfTermWasNotManipulated, String term) {
        while(!nextWord.isEmpty()) {
            String s = nextWord.pollLast();
            if (s != null && !s.equals(""))
                wordList.addFirst(s);
        }

        if (!Model.stopWords.contains(term.toLowerCase())) {
            if (doStemIfTermWasNotManipulated)
                term = ps.stemTerm(term);
            miniDic.addWord(term, index);
            index++;
        }
        return index;
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

    private String handleRangesSign(String term) {
        int idx = term.indexOf('-');
        if (idx != -1) {
            if (term.lastIndexOf('-') == idx+1)
                term = term.replaceFirst("-", "");
        }
        return term;
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

    private String handleDistance(String term, String unit) {
        switch(unit) {
            case "cm":
                term = numberValue(Double.parseDouble(term.replace(",", "")) / 1000);
                break;
            case "m":
                term = numberValue(Double.parseDouble(term.replace(",", "")) / 100);
        }
        return term+" km";
    }

    private String handlePercent(String term, String percentSign) {
        return term+percentSign;
    }

    private String handleMonthDay(String month, String day) {
        int monthNum = isMonth(month);
        int dayNum;
        dayNum = Integer.parseInt(day);
        if (dayNum < 10)
            day = "0"+day;
        String newTerm = monthNum+"-"+day;
        if (monthNum < 9)
            newTerm = "0"+newTerm;

        return newTerm;
    }

    private String handleMonthYear(String month, String year) {
        int monthNum = isMonth(month);
        String newTerm = year+"-";
        if (monthNum < 9)
            newTerm += "0";
        return newTerm+monthNum;
    }

    private String handleDollar(double number, boolean containsComma) {
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
        switch(ans) {
            case "":
                return containsComma ? addCommas(numberValue(number))+" Dollars" : numberValue(number)+" Dollars";
        }
        return numberValue(number)+" "+ans+" Dollars";
    }

    public String handleNumber(double number) {
        String ans = "";
        int multi = 1000;
        if (number > multi) {
            multi *= 1000;
            if (number > multi) {
                multi *= 1000;
                if (number > multi) {
                    ans = "B";
                } else {
                    ans = "M";
                    multi /= 1000;
                }
                number = (number / multi);
            } else {
                ans = "K";
                multi /= 1000;
                number = number / multi;
            }
        }
        return numberValue(number)+ans;
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

    private String numberValue(Double d) {
        if (isInteger(d))
            return ""+d.intValue();
        return ""+d;
    }

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
            } else if (queuePeek.equalsIgnoreCase("Minutes")) {
                wordList.remove();
                nextWord = "Min";
            } else if (queuePeek.equalsIgnoreCase("Seconds")) {
                wordList.remove();
                nextWord = "Sec";
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
            } else if (isMonth(queuePeek) != -1) {
                wordList.remove();
                nextWord = queuePeek;
            } else if (queuePeek.contains("-")) {
                wordList.remove();
                nextWord = queuePeek;
            }
        }
        return nextWord;
    }

    private boolean checkIsFraction(String nextWord) {
        int idx = nextWord.indexOf('/');
        if (idx != -1)
            return isNumber(nextWord.substring(0, idx)) && isNumber(nextWord.substring(idx+1));
        return false;
    }

    private boolean isInteger(double word) {
        return word == Math.floor(word) && !Double.isInfinite(word);
    }

    private boolean isInteger(String word) {
        try {
            Integer.parseInt(word);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    private int isMonth(String month) {
        for(int i = 0; i < shortMonth.length; i++)
            if (month.equalsIgnoreCase(shortMonth[i]) || month.equalsIgnoreCase(longMonth[i]))
                return i+1;
        return -1;
    }

    private boolean isNumber(String word) {
        try {
            Double.parseDouble(word.replace(",", ""));
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    private boolean isRangeNumbers(String range) {
        int idx = range.indexOf('-');
        if (idx != -1) {
            if (checkIsFraction(range.substring(0, idx)))
                return isNumber(range.substring(idx+1)) || checkIsFraction(range.substring(idx+1));
            else if (isNumber(range.substring(0, idx)))
                return isNumber(range.substring(idx+1));

        }
        return false;
    }

    private static String[] shortMonth = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static String[] longMonth = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    private void nextWordsRules() {
        wordsRulesData = new HashMap<String, String>() {{
            put("Thousand", "K");
            put("Thousands", "K");
            put("Million", "M");
            put("Millions", "M");
            put("billion", "B");
            put("billions", "B");
            put("trillion", "T");
            put("trillions", "T");
            put("Tons", "Tons");
            put("Ton", "Tons");
            put("Grams", "gr");
            put("Gram", "gr");
            put("gr", "gr");
            put("kilogram", "kg");
            put("Kilogram", "kg");
            put("kilograms", "kg");
            put("Kilograms", "kg");
            put("kg", "kg");
            put("kgs", "kg");
            put("percent", "%");
            put("percentage", "%");
            put("%", "%");
            put("Dollars", "Dollars");
            put("Dollar", "Dollars");
            put("$", "Dollars");
            put("centimeter", "cm");
            put("centimeters", "cm");
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
        }};
    }
}
