package snitch.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import snitch.prometheus.QueryResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
public class PdfUtils {

    public static boolean buildPdf(ArrayList<QueryResult> data, OutputStream outputStream) {

        Document document = null;
        PdfWriter writer = null;
        boolean response = false;

        try {
            //instantiate document and writer
            document = new Document();
            writer = PdfWriter.getInstance(document, outputStream);

            //open document
            document.open();

            //add image for each query
            int width = 300;
            int height = 300;

            for(QueryResult result:data){
                JFreeChart chart = createChart(getPlotDataset(result), result.podName);
                BufferedImage bufferedImage = chart.createBufferedImage(width, height);
                Image image = Image.getInstance(writer, bufferedImage, 1.0f);
                document.add(image);
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

        return response;
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
                "Millicores",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        XYPlot plot = chart.getXYPlot();

        var renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        chart.setTitle(new TextTitle(name,
                        new Font("Serif", java.awt.Font.BOLD, 18)
                )
        );

        return chart;
    }

}
