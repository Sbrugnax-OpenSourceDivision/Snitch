package snitch.mails;

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import snitch.prometheus.beans.QueryBean;
import snitch.prometheus.beans.QueryResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PdfUtils {

    public static void buildPdf(QueryBean queryBean, HashMap<String, ArrayList<QueryResult>> data, OutputStream outputStream) {

        Document document = null;
        PdfWriter writer = null;
        boolean response = false;

        // Plot settings
        int width = 400;
        int height = 300;

        try {
            //instantiate document and writer
            document = new Document();
            writer = PdfWriter.getInstance(document, outputStream);

            //open document
            document.open();
            document.addTitle("Snitch recap: "+ queryBean.getQueryName());

            for(Map.Entry<String, ArrayList<QueryResult>> line : data.entrySet()){

                document.add(new Phrase(line.getKey()));

                if(line.getValue().get(0).query.type.equals(QueryBean.Type.table)){
                    document.add(getTableFromData(line.getValue()));
                }
                else{
                    for(QueryResult result: line.getValue()){
                        JFreeChart chart = createChart(getPlotDataset(result), result.podName);
                        BufferedImage bufferedImage = chart.createBufferedImage(width, height);
                        Image image = Image.getInstance(writer, bufferedImage, 1.0f);
                        document.add(image);
                    }
                }
            }

            //release resources
            document.close();
            document = null;

            writer.close();
            writer = null;
            response = true;

        } catch(DocumentException | IOException de) {
            System.out.println("Impossibile creare documento PDF");
        } finally {
            //release resources
            if(null != document) {
                try { document.close(); }
                catch(Exception ignored) { }
            }

            if(null != writer) {
                try { writer.close(); }
                catch(Exception ex) { }
            }
        }

    }

    private static PdfPTable getTableFromData(ArrayList<QueryResult> data) throws BadElementException, IOException {

        PdfPTable result = new PdfPTable(2);

        // Prima colonna
        PdfPCell cell = new PdfPCell(new Phrase("Nome Pod"));
        result.addCell(cell);

        cell = new PdfPCell(new Phrase("Status"));
        result.addCell(cell);

        for(QueryResult q: data){

            // Nome pod
            cell = new PdfPCell(new Phrase(q.podName));
            result.addCell(cell);

            if(q.values.get(0) == 1)
                try {
                    try {
                        cell = new PdfPCell(Image.getInstance("/snitch/resources/true.png"));
                    } catch (IOException | BadElementException e){
                        cell = new PdfPCell(Image.getInstance("kubernetes/snitch/resources/true.png"));
                    }
                }
                catch (IOException | BadElementException e){
                   cell = new PdfPCell(new Phrase("TRUE"));
                }

            else
                try{
                    try {
                        cell = new PdfPCell(Image.getInstance("/snitch/resources/dead.png"));
                    } catch (IOException | BadElementException e){
                        cell = new PdfPCell(Image.getInstance("kubernetes/snitch/resources/dead.png"));
                    }
                }
                catch (IOException | BadElementException e){
                    cell = new PdfPCell(new Phrase("FALSE"));
                }

            cell.setColspan(2);

            result.addCell(cell);
        }

        return result;
    }

    private static XYDataset getPlotDataset(QueryResult data){

        XYSeriesCollection result = new XYSeriesCollection();
        XYSeries series = new XYSeries("Random");
        for (int i = 0; i < data.timestamps.size(); i++) {
            series.add(data.timestamps.get(i),data.values.get(i));
        }
        result.addSeries(series);
        return result;
    }

    private static JFreeChart createChart(XYDataset dataset, String name) {

        JFreeChart chart = ChartFactory.createXYLineChart(
                name,
                "Age",
                "Ram ",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        XYPlot plot = chart.getXYPlot();

        var renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(1.5f));

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        chart.setTitle(new TextTitle(name));

        return chart;
    }

}
