package org.tecal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
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

    public TauxAnodisationPanel() {
    	sqlCnx = SQL_DATA.getInstance();
        setLayout(new BorderLayout());

        // Panel supérieur pour les sélecteurs de dates
        JPanel datePanel = new JPanel();
        FlowLayout fl_datePanel = new FlowLayout(FlowLayout.CENTER);
        fl_datePanel.setAlignOnBaseline(true);
        datePanel.setLayout(fl_datePanel);

        JLabel lblDateDebut = new JLabel("début:");
        dateDebutChooser = new JDateChooser();
        dateDebutChooser.setDate(new Date()); // Par défaut, aujourd'hui

        JLabel lblDateFin = new JLabel("fin:");
        dateFinChooser = new JDateChooser();
        dateFinChooser.setDate(new Date()); // Par défaut, aujourd'hui

        JButton btnAfficher = new JButton("Afficher le Graphique");

        datePanel.add(lblDateDebut);
        datePanel.add(dateDebutChooser);
        datePanel.add(lblDateFin);
        datePanel.add(dateFinChooser);
        datePanel.add(btnAfficher);

        add(datePanel, BorderLayout.NORTH);

        // Panel pour le graphique
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        // Action sur le bouton
        btnAfficher.addActionListener(e -> afficherGraphique());
    }
    
    private void afficherGraphique() {
        Date dateDebut = dateDebutChooser.getDate();
        Date dateFin = dateFinChooser.getDate();
        
        // Formatteurs pour l'affichage des tooltips
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        if (dateDebut == null || dateFin == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner des dates valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dateDebut.after(dateFin)) {
            JOptionPane.showMessageDialog(this, "La date de début doit être antérieure ou égale à la date de fin.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Récupération des données depuis la base de données
        TimeSeries seriesCurve = new TimeSeries("Heures total d'anodisation");
        TimeSeries seriesBar = new TimeSeries("Taux d'occupation (%)");

        try {
            ResultSet resultSet = sqlCnx.getTauxAnodisationJours(dateDebut, dateFin);
            while (resultSet.next()) {
                Date jour = resultSet.getDate("Jour");
                double taux = resultSet.getDouble("TauxOccupationPourcentage");
                int duree = resultSet.getInt("DureeOccupation");

                // Ajouter les données
                seriesCurve.addOrUpdate(new Day(jour), duree / 3600.0); // Convertir en heures
                seriesBar.addOrUpdate(new Day(jour), taux);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Création des collections de séries
        TimeSeriesCollection datasetCurve = new TimeSeriesCollection(seriesCurve); // Durée d'Occupation
        TimeSeriesCollection datasetBar = new TimeSeriesCollection(seriesBar);    // Taux d'Occupation

        // Création du graphique
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Taux et Durée d'Occupation",
                "Jour",
                "Durée (heures)", // Axe Y principal
                datasetCurve,
                true,
                true,
                false
        );

        // Obtenir le plot
        XYPlot plot = chart.getXYPlot();
      
        
        // Rendu pour la courbe (série principale - Durée d'Occupation)
        XYStepRenderer lineRenderer = new XYStepRenderer(); // Activer uniquement les lignes
        plot.setRenderer(0, lineRenderer); // Série 0 : Durée d'Occupation

        lineRenderer.setSeriesToolTipGenerator(0, (dataset, series, item) -> {
            Number value = dataset.getY(series, item);
            double hours = value.doubleValue();
            int fullHours = (int) hours;
            int minutes = (int) ((hours - fullHours) * 60);
            String formattedDate = dateFormat.format(dataset.getX(series, item));
            return String.format("Jour : %s, Durée : %d h %02d min", formattedDate, fullHours, minutes);
        });
        
        // Ajouter la deuxième série (histogramme) avec un deuxième axe Y
        NumberAxis axisY2 = new NumberAxis("Taux d'Occupation (%)");
        plot.setRangeAxis(1, axisY2); // Ajouter l'axe Y2
        plot.setDataset(1, datasetBar); // Série 1 : Taux d'Occupation
        plot.mapDatasetToRangeAxis(1, 1); // Mapper la série 1 à l'axe Y2

        // Rendu pour l'histogramme
        XYBarRenderer barRenderer = new XYBarRenderer();
        barRenderer.setShadowXOffset(0); // Désactiver l'ombre
        barRenderer.setShadowYOffset(0);
        plot.setRenderer(1, barRenderer);
        
        barRenderer.setSeriesToolTipGenerator(0, (dataset, series, item) -> {
            Number value = dataset.getY(series, item);
            String formattedTaux = decimalFormat.format(value.doubleValue());
            String formattedDate = dateFormat.format(dataset.getX(series, item));
            return String.format("Jour : %s, Taux : %s %%", formattedDate, formattedTaux);
        });

        // Configurer l'axe des dates
        DateAxis dateAxis = new DateAxis("Jour");
        dateAxis.setTimeline(org.jfree.chart.axis.SegmentedTimeline.newMondayThroughFridayTimeline());
        dateAxis.setRange(datasetCurve.getDomainBounds(true));
        plot.setDomainAxis(dateAxis);

        NumberAxis valueAxis = (NumberAxis) plot.getRangeAxis();
      
        valueAxis.setLabelPaint(Color.RED); // Changer la couleur du label de l'axe Y
       
        
        // Mettre à jour le panel du graphique
        chartPanel.removeAll();
        chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartPanel.validate();
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Taux d'Occupation");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setContentPane(new TauxAnodisationPanel());
            frame.setVisible(true);
        });
    }
}
