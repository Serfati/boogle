package parser;

import model.Model;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Callable;


public class Parse implements Callable<MiniDictionary> {
    private LinkedList<String> wordList;
    private cDocument corpus_doc;
    private Stemmer ps;
    private boolean stm;
    private static String[] shortMonth = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static String[] longMonth = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};


    /**
     * a constructor that receives a document from the corpus and if the terms should be stemmed
     *
     * @param corpus_doc a document from the corpus
     * @param stm        if the terms should be stemmed
     */
    public Parse(cDocument corpus_doc, boolean stm) {
        this.corpus_doc = corpus_doc;
        this.stm = stm;
        this.ps = new Stemmer();
    }

    /**
     * returns a Mini dictionary that was retrieved from the parse
     *
     * @return a Mini dictionary that was retrieved from the parse
     */
    public MiniDictionary call() {
        return parse(false);
    }

    /**
     * goes over all the terms of label <TEXT>  and parses them according to the rules of the work
     *
     * @return a Mini Dictionary that contains all the data about the terms and the doc
     */
    public MiniDictionary parse(boolean isQuery) {
        //split the <Text> label to list of terms
        wordList = stringToList(StringUtils.split(corpus_doc.getDocText(), " ~;!?=#&^*+\\|:\"(){}[]<>\n\r\t"));
        //list of next words from the current term
        LinkedList<String> nextWord = new LinkedList<>();
        //the mini dictionary that will be filled according to the terms

        MiniDictionary miniDic = new MiniDictionary(corpus_doc.getDocNum(), corpus_doc.getDocTitle());
        //the index of the
        int index = 0;
        while(!wordList.isEmpty()) {
            boolean doStemIfTermWasNotManipulated = false;
            String term = wordList.remove();
            if (isNumber(term)) { //if current term is a number
                nextWord.add(nextWord());
                if (nextWord.peekFirst().contains("-") && isRangeNumbers(nextWord.peekFirst()) && isFraction(nextWord.peekFirst().substring(0, nextWord.peekFirst().indexOf("-"))) && !wordList.isEmpty()) {
                    nextWord.addFirst(wordList.pollFirst());
                    term += " "+nextWord.pollLast();
                    if (isFraction(nextWord.peekFirst())) {
                        term += " "+nextWord.pollLast();
                    }
                } else if (isMonth(nextWord.peekFirst()) != -1 && isInteger(term)) {  //if it is rule Hei - it is a Month term
                    String save = nextWord.pollFirst();
                    term = handleMonthDay(save, term);
                    if (!wordList.isEmpty()) {
                        nextWord.add(wordList.pollFirst());
                        if (nextWord.peekFirst() != null && isNumber(nextWord.peekFirst()) && nextWord.peekFirst().length() == 4) {
                            nextWord.addFirst(save);
                        }
                    }
                } else if (nextWord.peekFirst().equalsIgnoreCase("Dollars")) {  //if it is rule Dalet - it is a Dollar term
                    nextWord.pollFirst();
                    term = handleDollar(Double.parseDouble(term.replace(",", "")), term.contains(","));
                } else if (nextWord.peekFirst().equals("%")) { // if it is rule Gimel - it is a percent term
                    term = handlePercent(term, nextWord.pollFirst());
                } else if (nextWord.peekFirst().equalsIgnoreCase("Min") || nextWord.peekFirst().equalsIgnoreCase("Sec")) {
                    term = handleTime(term, Objects.requireNonNull(nextWord.pollFirst()));
                } else if (nextWord.peekFirst().equals("Ton") || nextWord.peekFirst().equals("Gram")) {
                    term = handleWeight(term, Objects.requireNonNull(nextWord.pollFirst()));
                } else {
                    term = handleNumber(Double.parseDouble(term.replace(",", "")));
                    if (!(term.charAt(term.length()-1) > 'A' && term.charAt(term.length()-1) < 'Z')) { //if a number returned is smaller than 1000
                        if (nextWord.peekFirst().equals("T")) {
                            term = numberValue(Double.parseDouble(term) * 1000);
                            nextWord.pollFirst();
                            nextWord.addFirst("B");
                        }
                        if (nextWord.peekFirst().length() == 1)
                            term += nextWord.pollFirst();

                        if (!nextWord.isEmpty() && nextWord.peekFirst().equals(""))
                            nextWord.clear();

                        if (!wordList.isEmpty()) {
                            nextWord.addLast(wordList.poll());
                            if (isFraction(nextWord.peekFirst())) { //rule Alef 2 - fraction rule
                                term += " "+nextWord.pollFirst();
                                nextWord.addFirst(nextWord());
                                if (nextWord.peekFirst().equals("Dollars"))
                                    term += " "+nextWord.pollFirst();
                            } else if (!wordList.isEmpty() && nextWord.peekFirst().equals("U.S")) {
                                nextWord.addFirst(wordList.poll());
                                if (nextWord.peekFirst().equalsIgnoreCase("dollars")) {
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
                    }
                }
            } else if (term.length() >= 1 && isNumber(term.substring(1))) {
                if (term.charAt(0) == '$') { //rule Dalet - dollar sign at the beginning of a number
                    try {
                        term = handleDollar(Double.parseDouble(term.substring(1).replace(",", "")), term.contains(","));
                    } catch(NumberFormatException e) {
                        e.getCause();
                    }
                }


            } else if (term.length() >= 1 && isNumber(term.substring(0, term.length()-1))) {
                if (!term.substring(0, term.length()-1).equals("%")) {
                    nextWord.addFirst(nextWord());
                    if (term.substring(term.length()-1).equals("m") && nextWord.peekFirst().equals("Dollars"))
                        term = numberValue(Double.parseDouble(term.substring(0, term.length()-1).replace(",", "")))+" M "+nextWord.pollFirst();

                }
            } else if (term.length() >= 2 && isNumber(term.substring(0, term.length()-2)) && term.substring(term.length()-2).equals("bn")) {
                nextWord.addFirst(nextWord());
                if (nextWord.peekFirst().equals("Dollars"))
                    term = numberValue(Double.parseDouble(term.substring(0, term.length()-2).replace(",", "")) * 1000)+" M "+nextWord.pollFirst();


            } else if (isMonth(term) != -1) { // rule Vav - month year rule
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.poll());
                    if (isNumber(nextWord.peekFirst())) {
                        term = handleMonthYear(term, nextWord.pollFirst());
                    }
                }
            } else if (term.equalsIgnoreCase("between")) {
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.poll());
                    if ((isNumber(nextWord.peekFirst()) || isFraction(nextWord.peekFirst())) && !wordList.isEmpty()) {
                        nextWord.addFirst(wordList.pollFirst());
                        if (isFraction(nextWord.peekFirst()) && !wordList.isEmpty())
                            nextWord.addFirst(wordList.pollFirst());

                        if (nextWord.peekFirst().equalsIgnoreCase("and") && !wordList.isEmpty()) {
                            nextWord.addFirst(wordList.pollFirst());
                            if (isNumber(nextWord.peekFirst()) || isFraction(nextWord.peekFirst())) {
                                while(!nextWord.isEmpty())
                                    term += " "+nextWord.pollLast();
                                if (!wordList.isEmpty()) {
                                    nextWord.addFirst(wordList.pollFirst());
                                    if (isFraction(nextWord.peekFirst()) && !wordList.isEmpty())
                                        term += " "+nextWord.pollFirst();

                                }
                            }
                        }

                    }
                }
            } else if (term.contains("-") && isRangeNumbers(term)) {
                if (!wordList.isEmpty()) {
                    nextWord.addFirst(wordList.pollFirst());
                    if (isFraction(nextWord.peekFirst()))
                        term += " "+nextWord.pollFirst();
                }
            } else if (term.contains("-")) {
                term = handleRangesSign(term);
            } else if (stm) {
                doStemIfTermWasNotManipulated = true;
            }


            while(!nextWord.isEmpty()) {
                String s = nextWord.pollLast();
                if (s != null && !s.equals(""))
                    wordList.addFirst(s);

            }

            if (isQuery || !Model.stopWords.contains(term.toLowerCase())) {
                if (doStemIfTermWasNotManipulated)
                    term = ps.stemTerm(term);
                miniDic.addWord(term, index);
                index++;
            }
        }
        return miniDic;
    }

    /**
     * removes unwanted letters from language
     *
     * @param lang the language
     * @return clean word
     */
    private String cleanLanguage(String lang) {
        String language = lang;
        int idx = lang.indexOf(" ");
        if (idx != -1)
            language = language.substring(0, idx);
        int i = language.length()-1;
        while(i >= 0 && !Character.isLetter(language.charAt(i))) {
            language = language.substring(0, i);
            i--;
        }
        while(language.length() > 0 && !Character.isLetter(language.charAt(0))) {
            language = language.substring(1);
        }
        return language;
    }

    /**
     * gets an array of String and returns it has a List
     * each term is cleaned from the unwanted signs
     *
     * @param split the array to separate
     * @return a list of the terms
     */
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
     * this function deletes all irrelevant signs from a term
     *
     * @param term the term to clean
     * @return a cleaned term
     */
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

    /**
     * deletes a double -- if exists
     *
     * @param term the term to be handled
     * @return the term without --
     */
    private String handleRangesSign(String term) {
        int idx = term.indexOf('-');
        if (idx != -1) {
            if (term.lastIndexOf('-') == idx+1)
                term = term.replaceFirst("-", "");
        }
        return term;
    }

    /**
     * transforms terms that contain Tons or Grams to Kilograms
     *
     * @param term the term to be changed
     * @param unit the unit of the number
     * @return the term after change as kilogram
     */
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

    /**
     * transforms terms that contain Seconds or Minutes to Hours
     *
     * @param term the term to be changed
     * @param unit the unit of the number
     * @return the term after change as hours
     */
    private String handleTime(String term, String unit) {
        switch(unit) {
            case "Sec":
                term = numberValue(Double.parseDouble(term.replace(",", "")) / 3600);
                break;
            case "Min":
                term = numberValue(Double.parseDouble(term.replace(",", "")) / 60);
        }
        return term+" Hours";
    }

    /**
     * adds to term the % sign
     *
     * @param term        the term
     * @param percentSign % sign
     * @return the term with percent sign
     */
    private String handlePercent(String term, String percentSign) {
        return term+percentSign;
    }

    /**
     * transforms a term that is written as DAY MONTH / MONTH DAY to mm-dd
     *
     * @param month the month
     * @param day   the day
     * @return mm-dd
     */
    private String handleMonthDay(String month, String day) {
        int monthNum = isMonth(month);
        int dayNum = 0;
        dayNum = Integer.parseInt(day);
        if (dayNum < 10)
            day = "0"+day;
        String newTerm = monthNum+"-"+day;
        if (monthNum < 9)
            newTerm = "0"+newTerm;

        return newTerm;
    }

    /**
     * transforms a MONTH YEAR to yyyy-mm
     *
     * @param month the month
     * @param year  the year
     * @return yyyy-mm
     */
    private String handleMonthYear(String month, String year) {
        int monthNum = isMonth(month);
        String newTerm = year+"-";
        if (monthNum < 9)
            newTerm += "0";
        return newTerm+monthNum;
    }

    /**
     * Rule DALET - changed number according to the rule
     *
     * @param number        the number to be changed
     * @param containsComma if the number contains comma
     * @return the number after rule
     */
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
        if (ans.equals("")) {
            if (containsComma)
                return addCommas(numberValue(number))+" Dollars";
            else
                return numberValue(number)+" Dollars";
        }
        return numberValue(number)+" "+ans+" Dollars";
    }

    /**
     * Rule ALEF - change numbers according to their size
     *
     * @param number - number to be changed
     * @return the number after changed
     */
    public String handleNumber(double number) {
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
     * adds commas to a number
     *
     * @param number number to add commas to
     * @return returns number as a String with commas
     */
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

    /**
     * checks if the number is int or double
     *
     * @param d number to be checked
     * @return returns a string with the correct number
     */
    private String numberValue(Double d) {
        if (isInteger(d))
            return ""+d.intValue();
        return ""+d;
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

    /**
     * Checks if the string given is a fraction of a number
     *
     * @param nextWord string to be checked
     * @return true if string is a fraction, false otherwise
     */
    private boolean isFraction(String nextWord) {
        int idx = nextWord.indexOf('/');
        if (idx != -1)
            return isNumber(nextWord.substring(0, idx)) && isNumber(nextWord.substring(idx+1));
        return false;
    }

    /**
     * Checks if a number is integer or double
     *
     * @param word number to be checked
     * @return returns true if it is integer, false it is double
     */
    private boolean isInteger(double word) {
        return word == Math.floor(word) && !Double.isInfinite(word);
    }

    /**
     * checks if a number is an integer or not
     *
     * @param word the number to be checked
     * @return true if it is integer
     */
    private boolean isInteger(String word) {
        try {
            Integer.parseInt(word);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a string is a month
     *
     * @param month - the string to be checked
     * @return true if it is a month, false otherwise
     */
    private int isMonth(String month) {
        for(int i = 0; i < shortMonth.length; i++)
            if (month.equalsIgnoreCase(shortMonth[i]) || month.equalsIgnoreCase(longMonth[i]))
                return i+1;
        return -1;
    }

    /**
     * Checks is a string is a number
     *
     * @param word - the string to be checked
     * @return returns true if it is a number, false otherwise
     */
    private boolean isNumber(String word) {
        try {
            Double.parseDouble(word.replace(",", ""));
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    /**
     * checks if a range term is a range of numbers
     *
     * @param range the range to be checked
     * @return tr
     */
    private boolean isRangeNumbers(String range) {
        int idx = range.indexOf('-');
        if (idx != -1) {
            if (isFraction(range.substring(0, idx)))
                return isNumber(range.substring(idx+1)) || isFraction(range.substring(idx+1));
            else if(isNumber(range.substring(0,idx)))
                return isNumber(range.substring(idx + 1));

        }
        return false;
    }
}
