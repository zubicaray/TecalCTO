package org.tecal.ui.stats;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.tecal.scheduler.data.SQL_DATA;

import com.toedter.calendar.JDateChooser;

public class TauxAnodisationPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JDateChooser dateDebutChooser;
    private JDateChooser dateFinChooser;
    private JPanel chartPanel;
    private SQL_DATA sqlCnx;
    private boolean showingSmoothCurves = true;
    JButton btnToggleSeries ;
    JTextField windowSizeField ;
    
    // Récupération des données depuis la base de données
    TimeSeries seriesCurve ;
    TimeSeries seriesBar;
    TimeSeries seriesSmoothed ;
    TimeSeries seriesCurveSmoothed ;
    
    public TauxAnodisationPanel() {
        sqlCnx = SQL_DATA.getInstance();
        setLayout(new BorderLayout());

        // Panel supérieur pour les sélecteurs de dates
        JPanel datePanel = new JPanel();
        FlowLayout fl_datePanel = new FlowLayout(FlowLayout.CENTER);
        fl_datePanel.setAlignOnBaseline(true);
        datePanel.setLayout(fl_datePanel);
       

        btnToggleSeries = new JButton("Lisser les données");

        datePanel.add(btnToggleSeries); // Ajouter le bouton au panneau supérieur

        btnToggleSeries.addActionListener(e -> toggleSeriesVisibility());

        JLabel lblDateDebut = new JLabel("début:");
        dateDebutChooser = new JDateChooser();
        dateDebutChooser.setDate(new Date()); // Par défaut, aujourd'hui

        JLabel lblDateFin = new JLabel("fin:");
        dateFinChooser = new JDateChooser();
        dateFinChooser.setDate(new Date()); // Par défaut, aujourd'hui
        
        dateDebutChooser.setPreferredSize(new java.awt.Dimension(100, 20));
        dateFinChooser.setPreferredSize(new java.awt.Dimension(100, 20));
        
        // Définir les dates par défaut : startDateChooser = 1 jour avant la date actuelle, endDateChooser = date du jour
        Date today = new Date();
        Date oneYearBefore = new Date(today.getTime() - 24L * 3600 * 1000*365); // Un an avant la date actuelle
        dateDebutChooser.setDate(oneYearBefore);
        dateFinChooser.setDate(today);
        
       
        dateFinChooser.getDateEditor().addPropertyChangeListener(
        	    new PropertyChangeListener() {
        	        @Override
        	        public void propertyChange(PropertyChangeEvent e) {
        	        	afficherGraphique();
        	        }
        	    });
        dateDebutChooser.getDateEditor().addPropertyChangeListener(
        	    new PropertyChangeListener() {
        	        @Override
        	        public void propertyChange(PropertyChangeEvent e) {
        	        	afficherGraphique();
        	        }
        	    });
        
        JButton btnAfficher = new JButton("MAJ");
        JLabel lblWindowSize = new JLabel("Lissage :");
        windowSizeField = new JTextField("10"); // Valeur par défaut
        windowSizeField.setPreferredSize(new java.awt.Dimension(50, 30)); // Taille du champ

        datePanel.add(lblDateDebut);
        datePanel.add(dateDebutChooser);
        datePanel.add(lblDateFin);
        datePanel.add(dateFinChooser);
        datePanel.add(btnAfficher); 
        datePanel.add(lblWindowSize);
        datePanel.add(windowSizeField);

        add(datePanel, BorderLayout.NORTH);

        // Panel pour le graphique
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
        
 
        afficherGraphique();
        
        // Action sur le bouton
        btnAfficher.addActionListener(e -> afficherGraphique());
    }
    private void toggleSeriesVisibility() {
        
        showingSmoothCurves = !showingSmoothCurves;
        redrawSeries();
    }
	private void redrawSeries() {
		XYPlot plot = ((JFreeChart) ((ChartPanel) chartPanel.getComponent(0)).getChart()).getXYPlot();

        // Bascule la visibilité entre les courbes originales et lissées
        if (!showingSmoothCurves) {
            // Afficher les courbes originales
            if (plot.getRenderer(0) != null) {
                plot.getRenderer(0).setSeriesVisible(0, true); // Durée originale
            }
            if (plot.getRenderer(2) != null) {
                plot.getRenderer(2).setSeriesVisible(0, true); // Taux original
            }
            // Cacher les courbes lissées
            if (plot.getRenderer(1) != null) {
                plot.getRenderer(1).setSeriesVisible(0, false); // Taux lissé
            }
            if (plot.getRenderer(3) != null) {
                plot.getRenderer(3).setSeriesVisible(0, false); // Durée lissée
            }
            if (plot.getRenderer(4) != null) {
                plot.getRenderer(4).setSeriesVisible(0, false); // Durée lissée
            }

            // Mettre à jour le bouton
            btnToggleSeries.setText("Lisser les données");
        } else {
            // Afficher les courbes lissées
            if (plot.getRenderer(0) != null) {
                plot.getRenderer(0).setSeriesVisible(0, false); // Cacher durée originale
            }
            if (plot.getRenderer(2) != null) {
                plot.getRenderer(2).setSeriesVisible(0, false); // Cacher taux original
            }
            if (plot.getRenderer(1) != null) {
                plot.getRenderer(1).setSeriesVisible(0, true); // Taux lissé
            }
            if (plot.getRenderer(3) != null) {
                plot.getRenderer(3).setSeriesVisible(0, true); // Durée lissée
            }

            // Mettre à jour le bouton
            btnToggleSeries.setText("Données brutes");
        }
	}

    private void afficherGraphique() {
        Date dateDebut = dateDebutChooser.getDate();
        Date dateFin = dateFinChooser.getDate();
        
        int windowSize;
        try {
            windowSize = Integer.parseInt(windowSizeField.getText());
            if (windowSize <= 0) {
                throw new NumberFormatException("La fenêtre doit être un entier positif.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer une taille de fenêtre valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Formatteurs pour l'affichage des tooltips
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 

        if (dateDebut == null || dateFin == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner des dates valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dateDebut.after(dateFin)) {
            JOptionPane.showMessageDialog(this, "La date de début doit être antérieure ou égale à la date de fin.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        getDatas(dateDebut, dateFin, windowSize);

        // Datasets
        TimeSeriesCollection datasetCurve = new TimeSeriesCollection(seriesCurve);
        TimeSeriesCollection datasetBar = new TimeSeriesCollection(seriesBar);
        TimeSeriesCollection datasetSmoothed = new TimeSeriesCollection(seriesSmoothed);
        TimeSeriesCollection datasetCurveSmoothed = new TimeSeriesCollection(seriesCurveSmoothed);
        
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Taux de remplissage et temps en anodisation",
                "Jour",
                "Durée (heures)",
                datasetCurve,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        XYStepRenderer LineRendererHeures = new XYStepRenderer();
        plot.setRenderer(0, LineRendererHeures);
        LineRendererHeures.setSeriesPaint(0, Color.MAGENTA);

        NumberAxis axisY2 = new NumberAxis("Taux de remplissage (%)");
        axisY2.setLabelPaint(Color.GREEN);
        plot.setRangeAxis(1, axisY2);
        plot.setDataset(1, datasetSmoothed);
        plot.setDataset(2, datasetBar);
        plot.mapDatasetToRangeAxis(1, 1);
        plot.mapDatasetToRangeAxis(2, 1);

        XYLineAndShapeRenderer lineRendererTaux = new XYLineAndShapeRenderer(true, false);
        lineRendererTaux.setSeriesPaint(0, Color.GREEN);
        lineRendererTaux.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(2, lineRendererTaux);

        XYLineAndShapeRenderer lineRendererTauxSmoothed = new XYLineAndShapeRenderer(true, false);
        lineRendererTauxSmoothed.setSeriesPaint(0, Color.GREEN);
        lineRendererTauxSmoothed.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        plot.setRenderer(1, lineRendererTauxSmoothed);

        plot.setDataset(3, datasetCurveSmoothed);
        plot.mapDatasetToRangeAxis(3, 0);

        XYLineAndShapeRenderer lineRendererHeuresSmoothed = new XYLineAndShapeRenderer(true, false);
        lineRendererHeuresSmoothed.setSeriesPaint(0, Color.MAGENTA);
        lineRendererHeuresSmoothed.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        plot.setRenderer(3, lineRendererHeuresSmoothed);        
       
        plot.mapDatasetToRangeAxis(4, 0); // Utiliser l'axe principal Y (heures)

       
        
        DateAxis dateAxis = new DateAxis("Jour");
        dateAxis.setTimeline(org.jfree.chart.axis.SegmentedTimeline.newMondayThroughFridayTimeline());
        dateAxis.setRange(datasetCurve.getDomainBounds(true));
        plot.setDomainAxis(dateAxis);

        NumberAxis valueAxis = (NumberAxis) plot.getRangeAxis();
        valueAxis.setLabelPaint(Color.MAGENTA);

        // Suppression du datasetCorrelation, axisY3, renderer 4

        setToolTips(dateFormat, LineRendererHeures, lineRendererHeuresSmoothed, lineRendererTaux, lineRendererTauxSmoothed);

        chartPanel.removeAll();
        chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartPanel.validate();

        redrawSeries();
    }

    private void getDatas(Date dateDebut, Date dateFin, int windowSize) {
        seriesCurve = new TimeSeries("Total heures anodisation");
        seriesBar = new TimeSeries("Taux de remplissage (%)");
        seriesSmoothed = new TimeSeries("Taux de remplissage lissé");
        seriesCurveSmoothed = new TimeSeries("Total heures lissé");
       

        List<Integer> tauxData = new ArrayList<>();
        List<Integer> dureeData = new ArrayList<>();
        List<Date> dates = new ArrayList<>();

        try {
            ResultSet resultSet = sqlCnx.getTauxAnodisationJours(dateDebut, dateFin);
            while (resultSet.next()) {
                Date jour = resultSet.getDate("Jour");
                double taux = resultSet.getDouble("TauxOccupationPourcentage");
                int duree = resultSet.getInt("DureeOccupation");
            
               
                seriesCurve.addOrUpdate(new Day(jour), duree ); // heures
                seriesBar.addOrUpdate(new Day(jour), taux);

                tauxData.add((int) taux);
                dureeData.add((int) (duree ));
                dates.add(jour);
            }

            List<Double> smoothedTaux = smoothData(tauxData, windowSize);
            for (int i = 0; i < smoothedTaux.size(); i++) {
                seriesSmoothed.addOrUpdate(new Day(dates.get(i)), smoothedTaux.get(i));
            }

            List<Double> smoothedDuree = smoothData(dureeData, windowSize);
            for (int i = 0; i < smoothedDuree.size(); i++) {
                seriesCurveSmoothed.addOrUpdate(seriesCurve.getTimePeriod(i), smoothedDuree.get(i));
            }

            // Suppression du calcul de corrélation

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
	private void setToolTips(SimpleDateFormat dateFormat, XYStepRenderer LineRendererHeures,
			XYLineAndShapeRenderer lineRendererHeuresSmoothed,
			XYLineAndShapeRenderer lineRendererTaux, XYLineAndShapeRenderer lineRendererTauxSmoothed) {
		LineRendererHeures.setSeriesToolTipGenerator(0, (dataset, series, item) -> {
            Number value = dataset.getY(series, item);
            double hours = value.doubleValue();
            int fullHours = (int) hours;
            int minutes = (int) ((hours - fullHours) * 60);
            String formattedDate = dateFormat.format(dataset.getX(series, item));
            return String.format("Jour : %s, Durée : %d h %02d min", formattedDate, fullHours, minutes);
        });
		
		
		 
		lineRendererHeuresSmoothed.setSeriesToolTipGenerator(0, (dataset, series, item) -> {
			Number value = dataset.getY(series, item);
            double hours = value.doubleValue();
            int fullHours = (int) hours;
            int minutes = (int) ((hours - fullHours) * 60);
            String formattedDate = dateFormat.format(dataset.getX(series, item));
            return String.format("Jour : %s, Durée : %d h %02d min", formattedDate, fullHours, minutes);
        });
       
        lineRendererTauxSmoothed.setSeriesToolTipGenerator(0, (dataset, series, item) -> {
            Number value = dataset.getY(series, item);
            String formattedDate = dateFormat.format(dataset.getX(series, item));
            return String.format("Jour : %s, Taux lissé : %.2f %%", formattedDate, value.doubleValue());
        });
        
        lineRendererTaux.setSeriesToolTipGenerator(0, (dataset, series, item) -> {
            Number value = dataset.getY(series, item);
            String formattedDate = dateFormat.format(dataset.getX(series, item));
            return String.format("Jour : %s, Taux : %.2f %%", formattedDate, value.doubleValue());
        });
        
   
       

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
            JFrame frame = new JFrame("Taux de remplissage en anodisation");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setContentPane(new TauxAnodisationPanel());
            frame.setVisible(true);
        });
    }
}
