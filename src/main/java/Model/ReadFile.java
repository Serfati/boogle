
package Model;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.print.Doc;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue

        ;
public class ReadFile {
    private File corpus;
    private int numOfParsedDocs; //number of documents to parse until writing to disk.
    private Parser parser;
    private Queue<Doc> documents;
    private HashSet<String> languages;
    private int numOfDocuments; //number of documents in corpus

    public ReadFile() {
        numOfParsedDocs = 0;//counts number of docs parsed so when arriving threshold, temporal dictionary will be cleared.
        documents = new LinkedList<>();
        languages = new HashSet<>();
        numOfDocuments = 0;
    }


    //INIT
    public void readFile () throws Exception{
        File[] filesList=null;
        Elements listOfDocs=null;
        Doc currentDoc = null;
        File[] corpusFiles=corpus.listFiles();
        for(int i=0;i<corpusFiles.length;i++){
            File fileEntry=corpusFiles[i];
            if(fileEntry.isDirectory()) {
                filesList = fileEntry.listFiles();
                //for(int m=0;m<filesList.length;m++)
                listOfDocs = extractSubDocuments(filesList[0].getAbsolutePath()); //filesList[i]
                if (listOfDocs != null)
                    for (int j = 0; j < listOfDocs.size(); j++) {
                        Element document = listOfDocs.get(j);
                        if (document != null) {
                            //extract document details
                            currentDoc =  extractDocumentDetails(document);
                            if(document.select("text").size() > 0) {
                                //extract the text included between the tags <text></text>
                                parser.setCurrentDoc(currentDoc);
                                //parse also the title if exists
                                String text = document.getElementsByTag("text").toString();
                                if (text.length() > 7)
                                    text = text.substring(7);
                                if(currentDoc.getTitle()!=null)
                                    parser.parse(currentDoc.getTitle()+" "+text);
                                else
                                    parser.parse(document.getElementsByTag("text").toString());
                                numOfParsedDocs++;
                            }

                            documents.add(currentDoc);
                            //arriving threshold
                            if (numOfParsedDocs == 500) {
                                parser.clearTemporalPostings();
                                writeDocumentsListToDisk(parser.getPathForWriting());
                                numOfParsedDocs = 0;
                            }
                        }
                    }
            }
        }
        numOfParsedDocs=0;
        parser.clearTemporalPostings();
        writeDocumentsListToDisk(parser.getPathForWriting());
    }





    /**
     * A function that gets a document and returns a Doc object with the details from the document
     * @param document - the given document
     * @return new Doc with name, date, title and city
     */
    private Doc extractDocumentDetails(Element document) {return null;}


    public void setParser(Parser parser)
    {
        this.parser=parser;
    }

    public void setCorpus(String path){

        this.corpus =new File(path);
    }
    /**
     * returns the number of documents extracted during all process.
     * @return the number of documents which passed a parse process.
     */
    public int getNumberOfDocuments() {
        return numOfDocuments;
    }

    /**
     * sets the number of documents extracted to the given number.
     * @param numOfDocuments- the number of documents we wish to reassign to the field.
     */
    public void setNumOfDocuments(int numOfDocuments) {
        this.numOfDocuments = numOfDocuments;
    }

    /**
     * resets the structures of ReadFile
     */
    public void resetData(){
        documents = new LinkedList<>();
        languages = new HashSet<>();
        numOfDocuments = 0;
    }
}