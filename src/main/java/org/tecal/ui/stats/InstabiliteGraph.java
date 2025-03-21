package org.tecal.ui.stats;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.tecal.scheduler.data.SQL_DATA;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;

public class InstabiliteGraph extends JPanel {
    private static final long serialVersionUID = 1L;
    private JDateChooser dateDebutChooser;
    private JDateChooser dateFinChooser;
    private JPanel chartPanel;
    private Connection conn;
   

    public InstabiliteGraph() {
        conn = SQL_DATA.getInstance().getConnection();
        setLayout(new BorderLayout());

        // Panel des filtres
        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Date début:"));
        dateDebutChooser = new JDateChooser();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        dateDebutChooser.setDate(cal.getTime());

        controlPanel.add(dateDebutChooser);

        controlPanel.add(new JLabel("Date fin:"));
        dateFinChooser = new JDateChooser();
        dateFinChooser.setDate(new Date());
        controlPanel.add(dateFinChooser);

        JButton fetchButton = new JButton("Afficher le Graphique");
        controlPanel.add(fetchButton);

        add(controlPanel, BorderLayout.NORTH);

        // Panel du graphique
        chartPanel = new JPanel(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                afficherGraphique();
            }
        });
    }

    private void afficherGraphique() {
        Date dateDebut = dateDebutChooser.getDate();
        Date dateFin = dateFinChooser.getDate();
        if (dateDebut == null || dateFin == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner les deux dates.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Timestamp dateDebutStr = new Timestamp(dateDebut.getTime());
        Timestamp dateFinStr = new Timestamp(dateFin.getTime());

        DefaultCategoryDataset ecartTypeDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset moyenneDataset = new DefaultCategoryDataset();

        String req = """
            WITH Statistiques AS (
                SELECT 
                    D.NumPostePrecedent AS X, D.NumPoste AS Y, 
                    P1.NomPoste AS libelleX, P2.NomPoste AS libelleY,
                    STDEV(D.TempsDeplacement + dbo.getOffset(C.vitesse_bas, C.vitesse_haut)) AS ecart_type_t,
                    AVG(D.TempsDeplacement + dbo.getOffset(C.vitesse_bas, C.vitesse_haut)) AS moyenne_t
                FROM ANODISATION.dbo.DetailsFichesProduction D
                INNER JOIN ANODISATION.dbo.DetailsChargesProduction C 
                    ON C.NumFicheProduction = D.NumFicheProduction AND C.NumLigne = 1
                INNER JOIN ANODISATION.dbo.POSTES P1 
                    ON P1.NumPoste = D.NumPostePrecedent
                INNER JOIN ANODISATION.dbo.POSTES P2 
                    ON P2.NumPoste = D.NumPoste
                WHERE 
                    D.NumPoste NOT IN (1,2) 
                    AND D.NumPostePrecedent NOT IN (41,42) 
                    AND D.TempsDeplacement < 60 
                    --AND D.NumPostePrecedent != D.NumPoste
                    AND ABS(D.NumPostePrecedent - D.NumPoste) < 20 
                    AND D.TempsDeplacement <50
                    AND D.DateEntreePoste BETWEEN ? AND ?
                GROUP BY D.NumPostePrecedent, D.NumPoste, P1.NomPoste, P2.NomPoste
                HAVING STDEV(D.TempsDeplacement + dbo.getOffset(C.vitesse_bas, C.vitesse_haut)) > 4
            ) 
            SELECT * FROM Statistiques ORDER BY ecart_type_t DESC
            """;

        try (PreparedStatement stmt = conn.prepareStatement(req)) {
            stmt.setTimestamp(1, dateDebutStr);
            stmt.setTimestamp(2, dateFinStr);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String couple = rs.getString("libelleX") + " → " + rs.getString("libelleY");
                double ecartType = rs.getDouble("ecart_type_t");
                double moyenne = rs.getDouble("moyenne_t") ;// Réduction de la moyenne

                ecartTypeDataset.addValue(ecartType, "Écart-Type", couple);
                moyenneDataset.addValue(moyenne, "Moyenne (divisée)", couple);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Instabilité des mouvements (poste A->poste B)",
                "Couples (P1, P2)",
                "Écart-Type",
                ecartTypeDataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        // Axe Y principal (Écart-Type)
        NumberAxis axisLeft = (NumberAxis) plot.getRangeAxis();
        axisLeft.setAutoRangeIncludesZero(false);

        // Ajout du deuxième axe Y pour la Moyenne
        NumberAxis axisRight = new NumberAxis("Moyenne");
        axisRight.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(1, axisRight);

        // Associer la moyenne à l'axe droit
        plot.setDataset(1, moyenneDataset);
        plot.mapDatasetToRangeAxis(1, 1);

     // Utiliser un rendu en points pour la moyenne (sans ligne)
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, Color.BLUE);
        lineRenderer.setSeriesLinesVisible(0, false); // Ne pas relier les points
        lineRenderer.setSeriesShapesVisible(0, true); // Afficher uniquement les points
        plot.setRenderer(1, lineRenderer);
        
        lineRenderer.setSeriesToolTipGenerator(0, (dataset, series, item) -> {
            String couple = dataset.getColumnKey(item).toString();
            double moyenne = dataset.getValue(series, item).doubleValue() ; // Facteur inverse pour récupérer la vraie moyenne
            return "Moyenne pour " + couple + ": " + moyenne;
        });
        

        // Rotation des labels des X (couples)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        ChartPanel chartPanelComponent = new ChartPanel(chart);
        chartPanel.removeAll();
        chartPanel.add(chartPanelComponent, BorderLayout.CENTER);
        chartPanel.validate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("JFreeChart Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new InstabiliteGraph());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
