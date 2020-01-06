package ranker.algoritems;

import parser.MiniDictionary;

import java.util.Comparator;

/**
 * comparator class for comparing doc ranks
 */

public class DocRankComparator implements Comparator<MiniDictionary> {

    @Override
    public int compare(MiniDictionary o1, MiniDictionary o2) {

        if (o1.getRank() > o2.getRank())
            return -1;
        else if (o1.getRank() == o2.getRank())
            return 0;

        return 1;
    }
}