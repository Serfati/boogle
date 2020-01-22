package ranker;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;


/**
 * A class used to handle semantics.
 * given a word check for her vector in a glove file and calculate similarity between it and other words
 * bring back the  most relevant words
 */

public class SemanticHandler {
    public WordVectors wordVectors;
    public boolean useOffline;

    public SemanticHandler(boolean useOffline) {
        try {
            if (wordVectors == null && useOffline) {
                InputStream inputStream = new ClassPathResource("glove.6B.50d.txt").getInputStream();
                File file = new File("gloVe.txt");
                // commons-io
                FileUtils.copyInputStreamToFile(inputStream, file);
                if (inputStream != null) inputStream.close();
                wordVectors = useOffline ? WordVectorSerializer.readWord2VecModel(file) : null;
            }
        } catch(IOException ignored) {
        }
        this.useOffline = useOffline;
    }

    //-------------------------------------------------------------------//

    /**
     * this will return all the related words for a given query (2 for each word)
     *
     * @param originalQueryWords list of the query words
     * @return realted words for a given query
     */
    public String getTwoBestMatches(List<String> originalQueryWords) {
        StringBuilder allSynonyms = new StringBuilder();
        DatamuseAPI datamuseAPI = new DatamuseAPI();
        for(String entry : originalQueryWords)
            allSynonyms.append(useOffline ? new GloVe(wordVectors).synonyms(entry) : datamuseAPI.synonyms(entry));
        return allSynonyms.toString();
    }

    public class GloVe {
        public WordVectors wordVectors;

        public GloVe(WordVectors wv) {
            this.wordVectors = wv;
        }

        public String synonyms(String wordToSyn) {
            StringBuilder synonyms = new StringBuilder();
            synonyms.append(" ");
            Collection<String> syn = this.wordVectors.wordsNearest(wordToSyn, 2);
            syn.forEach(s -> synonyms.append(s).append(" "));
            return synonyms.toString();
        }
    }

    public class DatamuseAPI {
        private static final String URL = "https://api.datamuse.com/words?rel_syn=";

        /**
         * this function addresses the url given and gets the data from the web
         *
         * @param url the url to address to
         * @return JSONObject containing all data
         * @throws IOException .
         */
        public JSONObject post(String url) throws IOException {
            java.net.URL address = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) address.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            StringBuilder sb = new StringBuilder("{\"result\":");
            Scanner scan = new Scanner(address.openStream());
            while(scan.hasNext())
                sb.append(scan.nextLine());
            scan.close();
            sb.append("}");
            return new JSONObject(sb.toString());
        }

        public String synonyms(String wordToSyn) {
            StringBuilder backSynonyms = new StringBuilder();
            backSynonyms.append(" ");
            DatamuseAPI request = new DatamuseAPI();
            JSONObject details = null;
            try {
                details = request.post(URL+wordToSyn);
            } catch(IOException e) {
                e.printStackTrace();
            }
            JSONArray result = details.getJSONArray("result");

            for(int i = 0; i < result.length() && i < 2; i++) {
                JSONObject data = (JSONObject) result.get(i);
                String word = data.get("word").toString()+" ";
                backSynonyms.append(word);
            }
            return backSynonyms.toString();
        }
    }
}

