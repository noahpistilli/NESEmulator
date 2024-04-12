package com.wiilink24.emu.ui;

import com.wiilink24.emu.Emulator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class MemoryChart {
    public MemoryChart() {
        JDialog dialog = new JDialog(Emulator.getUI(), "Scrollable Graph Pane Example", true);

        // Create a sample dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Sample Data");
        for (int i = 0; i < 10; i++) {
            series.add(i, Math.sin(Math.toRadians(i)));
        }
        dataset.addSeries(series);

        // Create a chart based on the dataset
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Scrollable Graph", "X-axis", "Y-axis", dataset
        );

        // Customize the appearance of the chart if needed
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);

        // Create a chart panel and set its preferred size
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(16 * 40, 300)); // Assuming 40 pixels per data point

        // Create a JScrollPane and set the chart panel as its view
        JScrollPane scrollPane = new JScrollPane(chartPanel);

        // Add the JScrollPane to the JFrame
        dialog.add(scrollPane);
        dialog.setSize(200, 400);
        dialog.setVisible(true);
    }
}
