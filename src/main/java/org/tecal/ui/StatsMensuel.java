package org.tecal.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Week;
import org.jfree.data.time.TimeSeriesCollection;
import org.tecal.scheduler.data.SQL_DATA;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
import java.util.List;
import java.util.ArrayList;

public class StatsMensuel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private TimeSeriesCollection dataset;
    private ChartPanel chartPanel;
    Statement mStatement ;
    XYSeries series ;
    JFreeChart chart ;

    public StatsMensuel() {
        // Créer les champs de date
        JPanel datePanel = createDatePanel();
        try {
            mStatement = SQL_DATA.getInstance().getStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        series = new XYSeries("Durée d'Occupation");
        // Créer le dataset à partir de la base de données
        createDataset();

        // Créer le graphique
        chart = ChartFactory.createTimeSeriesChart(
                "temps anodisation par semaine", // Titre du graphique
                "Date",                           // Axe des X
                "heures",                         // Axe des Y
                dataset,                          // Dataset
                true,                             // Inclure une légende
                true,                             // Info tooltips
                false                             // URLs
        );

        // Appliquer le lissage avec un renderer
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(true);
        
        

        // Ajouter le renderer
        plot.setRenderer(renderer);

        // Ajouter le graphique dans un JPanel
        chartPanel = new ChartPanel(chart);
        //chartPanel.setPreferredSize(new Dimension(600, 600));

        // Agencer les composants
        this.setLayout(new BorderLayout());
        this.add(datePanel, BorderLayout.NORTH); // Ajouter le panel de dates au dessus
        this.add(chartPanel, BorderLayout.CENTER); // Ajouter le graphique au centre
    }

    private JPanel createDatePanel() {
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Centrer les composants

        // Créer les sélecteurs de date
        startDateChooser = new JDateChooser();
        endDateChooser = new JDateChooser();
        startDateChooser.setPreferredSize(new java.awt.Dimension(100, 20));
        endDateChooser.setPreferredSize(new java.awt.Dimension(100, 20));

        // Ajouter les champs de date au panel
        datePanel.add(new JLabel("Date de début :"));
        datePanel.add(startDateChooser);
        datePanel.add(new JLabel("Date de fin :"));
        datePanel.add(endDateChooser);

        // Définir les dates par défaut : startDateChooser = 1 jour avant la date actuelle, endDateChooser = date du jour
        Date today = new Date();
        Date oneYearBefore = new Date(today.getTime() - 24L * 3600 * 1000*365); // Un an avant la date actuelle
        startDateChooser.setDate(oneYearBefore);
        endDateChooser.setDate(today);

        // Ajouter un bouton pour régénérer la requête
        JButton regenerateButton = new JButton("MAJ");
        regenerateButton.addActionListener(e -> createDataset());

        datePanel.add(regenerateButton);

        return datePanel;
    }

   
    private void createDataset() {
        TimeSeries rawSeries = new TimeSeries("Temps en anodisation ");
        TimeSeries smoothedSeries = new TimeSeries("Durée lissée");

        // Récupérer les nouvelles dates
        Date startDate = startDateChooser.getDate();
        Date endDate = endDateChooser.getDate();

        // Convertir les dates au format SQL (yyyy-MM-dd)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateDebut = sdf.format(startDate);
        String dateFin = sdf.format(endDate);

        // Initialiser la requête SQL
        String query = String.format("""
                DECLARE @DateDebut DATE = '%s'; 
                DECLARE @DateFin DATE = '%s';

                SELECT 
                    DATEPART(YEAR, DateEntreePoste) AS Year,
                    DATEPART(WEEK, DateEntreePoste) AS WeekNumber,
                    SUM(DATEDIFF(HOUR, DateEntreePoste, DateSortiePoste)) AS DureeOccupation
                FROM DetailsFichesProduction
                WHERE NumPoste IN (18, 19, 20)
                    AND DateEntreePoste >= @DateDebut
                    AND DateSortiePoste < @DateFin
                GROUP BY 
                    DATEPART(YEAR, DateEntreePoste), 
                    DATEPART(WEEK, DateEntreePoste)
                ORDER BY Year, WeekNumber;
                """, dateDebut, dateFin);

        // Listes pour stocker les données brutes
        List<Integer> yData = new ArrayList<>();
        List<Integer> xWeeks = new ArrayList<>(); // Semaine de l'année
        List<Integer> xYears = new ArrayList<>(); // Année correspondante

        try (
            Statement statement = SQL_DATA.getInstance().getStatement();
            ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int year = resultSet.getInt("Year");
                int week = resultSet.getInt("WeekNumber");
                int duration = resultSet.getInt("DureeOccupation");

                Week w = new Week(week, year);
                rawSeries.add(w, duration);

                // Stocker les données brutes pour le lissage
                xWeeks.add(week);
                xYears.add(year);
                yData.add(duration);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Appliquer le lissage (par exemple, une fenêtre de 3 semaines)
        List<Double> smoothedYData = smoothData(yData, 10);

        // Ajouter les données lissées à la nouvelle série
        for (int i = 0; i < smoothedYData.size(); i++) {
            Week w = new Week(xWeeks.get(i), xYears.get(i));
            smoothedSeries.add(w, smoothedYData.get(i));
        }

        // Ajouter les séries au dataset
        if (dataset == null) {
            dataset = new TimeSeriesCollection(rawSeries);
            dataset.addSeries(smoothedSeries);
        } else {
            dataset.removeAllSeries();
            dataset.addSeries(rawSeries);  // Série brute
            dataset.addSeries(smoothedSeries);  // Série lissée
            chartPanel.repaint();
        }
    }
    private List<Double> smoothData(List<Integer> yData, int windowSize) {
        List<Double> smoothedData = new ArrayList<>();
        for (int i = 0; i < yData.size(); i++) {
            int start = Math.max(0, i - windowSize / 2);
            int end = Math.min(yData.size() - 1, i + windowSize / 2);

            // Calcul de la moyenne mobile
            double sum = 0;
            for (int j = start; j <= end; j++) {
                sum += yData.get(j);
            }
            smoothedData.add(sum / (end - start + 1));
        }
        return smoothedData;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("JFreeChart Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new StatsMensuel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
