package architectureTest.GUI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Chart extends JFrame {

    private JFreeChart chart;

    public JFreeChart getJfreeChart() {
        return chart;
    }

    public Chart(String architecture, List<Integer> xValues, List<Double> sortTimes, List<Double> processTimes, List<Double> clientTimes, String xAxis) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries sortSeries = createSeries(xValues, sortTimes, "Sort times");
        XYSeries processSeries = createSeries(xValues, processTimes, "Process times");
        XYSeries clientSeries = createSeries(xValues, clientTimes, "Client times");
        dataset.addSeries(sortSeries);
        dataset.addSeries(processSeries);
        dataset.addSeries(clientSeries);

        chart = createChart(architecture, dataset, xAxis);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);
        add(chartPanel);
        pack();
        setTitle("Array sorting times");
        setLocationRelativeTo(null);
    }

    private XYSeries createSeries(List<Integer> xValues, List<Double> yValues, String name) {
        XYSeries series = new XYSeries(name);
        for (int i = 0; i < xValues.size(); i++) {
            series.add(xValues.get(i), yValues.get(i));
        }
        return series;
    }

    private JFreeChart createChart(String architecture, XYDataset dataset, String xAxis) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                architecture,
                xAxis,
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);
        chart.getLegend().setFrame(BlockBorder.NONE);
        return chart;
    }

}
