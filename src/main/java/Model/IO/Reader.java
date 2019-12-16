package Model.IO;

import Model.Parser.cDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Callable;

public class Reader implements Callable<LinkedList<cDocument>> {
    File file;

    Reader(File file) {
        this.file = file;
    }

    @Override
    public LinkedList<cDocument> call() {
        File[] directoryListing = file.listFiles();
        LinkedList<cDocument> fileList = new LinkedList<>();
        if (directoryListing != null && file.isDirectory())
            Arrays.stream(directoryListing).map(this::read).forEach(fileList::addAll);
        return fileList;
    }

    private LinkedList<cDocument> read(File fileToSeparate) {
        LinkedList<cDocument> docList = new LinkedList<>();
        try {
            Document document = Jsoup.parse(new String(Files.readAllBytes(Objects.requireNonNull(file.listFiles())[0].toPath())));
            assert document != null;
            Elements docElements = document.getElementsByTag("DOC");
            for(Element element : docElements) {
                Elements IDElement = element.getElementsByTag("DOCNO");
                Elements DateElement = element.getElementsByTag("DATE1");
                Elements TitleElement = element.getElementsByTag("TI");
                Elements TextElement = element.getElementsByTag("TEXT");
                cDocument newDoc = new cDocument(fileToSeparate.getName(), IDElement.text(), DateElement.text(), TitleElement.text(), TextElement.text());
                docList.add(newDoc);
            }
            return docList;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return docList;
    }
}
