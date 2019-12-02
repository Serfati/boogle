package Model.Structures;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class NamedEntitiesSearcher {
    private static StanfordCoreNLP pipeline;

    public NamedEntitiesSearcher() {
        buildPipeLine();
    }

    //build pipe line
    public static void buildPipeLine() {
        Properties props = new Properties();
        props.setProperty("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
        props.setProperty("ner.rulesOnly", "true");
        props.setProperty("ner.statisticalOnly", "true");
        props.setProperty("ner.applyNumericClassifiers", "false");
        props.setProperty("ner.applyFineGrained", "false");
        props.setProperty("ner.buildEntityMentions", "true");
        props.setProperty("ner.combinationMode", "NORMAL");
        props.setProperty("ner.useSUTime", "true");
        props.setProperty("sutime.markTimeRanges", "false");
        props.setProperty("sutime.markTimeRanges", "false");
        props.setProperty("maxAdditionalKnownLCWords", "0");
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        pipeline = new StanfordCoreNLP(props);
    }

    public StanfordCoreNLP pipeline() {
        return pipeline;
    }
}