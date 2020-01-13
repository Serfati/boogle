package ui;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.datatable.DataTable;
import javafx.collections.ObservableList;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import ranker.ResultDisplay;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class ListToPDF {

    public void doPrintToPdf(ObservableList<ResultDisplay> list, String saveLoc1, Orientation orientation) {
        File saveLoc = null;
        try {
            if (saveLoc1 == null) return;
            if (!saveLoc1.endsWith(".pdf")) saveLoc = new File(saveLoc1+".pdf");
            //Initialize Document
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            //Create a landscape page
            if (orientation == Orientation.LANDSCAPE) {
                page.setMediaBox(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            } else {
                page.setMediaBox(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
            }

            doc.addPage(page);
            //Initialize table
            float margin = 10;
            float tableWidth = page.getMediaBox().getWidth()-(2 * margin);
            float yStartNewPage = page.getMediaBox().getHeight()-(2 * margin);
            float yStart = yStartNewPage;
            float bottomMargin = 0;

            BaseTable dataTable = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true,
                    true);
            DataTable t = new DataTable(dataTable, page);
            t.addListToTable(Collections.singletonList(list), DataTable.HASHEADER);
            dataTable.draw();
            doc.save(saveLoc);
            doc.close();

        } catch(IOException ex) {
            AlertMaker.showErrorMessage("Error occurred during PDF export", ex.getMessage());
        }
    }

    public enum Orientation {
        PORTRAIT, LANDSCAPE
    }

}