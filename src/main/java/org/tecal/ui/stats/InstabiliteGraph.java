package org.tecal.ui.stats;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.tecal.scheduler.data.SQL_DATA;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
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
        cal.add(Calendar.MONTH, -1); // Par défaut, un mois avant aujourd'hui
        dateDebutChooser.setDate(cal.getTime());
        
        controlPanel.add(dateDebutChooser);
        
        controlPanel.add(new JLabel("Date fin:"));
        dateFinChooser = new JDateChooser();
        dateFinChooser.setDate(new Date()); // Par défaut, aujourd'hui
        controlPanel.add(dateFinChooser);
        
        JButton fetchButton = new JButton("Afficher le Graphique");
        controlPanel.add(fetchButton);

        add(controlPanel, BorderLayout.NORTH);

        // Panel du graphique
        chartPanel = new JPanel(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        // Action du bouton
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

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String req="""           
            WITH VarianceData AS (
            SELECT 
        		  D.NumPostePrecedent AS X, D.NumPoste AS Y, P1.NomPoste AS libelleX, 
        		  P2.NomPoste AS libelleY, 
                  STDEV(D.TempsDeplacement+ dbo.getOffset(C.vitesse_bas, C.vitesse_haut)) AS variance_t 
            FROM ANODISATION.dbo.DetailsFichesProduction D 
	            INNER JOIN ANODISATION.dbo.DetailsChargesProduction C 
				    on C.NumFicheProduction=D.NumFicheProduction and C.NumLigne=1
	            INNER JOIN ANODISATION.dbo.POSTES P1 ON P1.NumPoste = D.NumPostePrecedent 
	            INNER JOIN ANODISATION.dbo.POSTES P2 ON P2.NumPoste = D.NumPoste 
            WHERE D.NumPoste not in (1,2,0) and D.NumPostePrecedent not in (0,41,42) 
        		and D.TempsDeplacement <70
        		and abs(D.NumPostePrecedent-D.NumPoste) <20 AND D.DateEntreePoste BETWEEN ? AND ? 
            GROUP BY D.NumPostePrecedent, D.NumPoste, P1.NomPoste, P2.NomPoste 
            ) 
            SELECT TOP 10 * FROM VarianceData ORDER BY variance_t DESC
            """;    
        try (
            PreparedStatement stmt = conn.prepareStatement(req)) {

            stmt.setTimestamp(1, dateDebutStr);
            stmt.setTimestamp(2, dateFinStr);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String couple = rs.getString("libelleX") + " → " + rs.getString("libelleY");
                double variance = rs.getDouble("variance_t");
                dataset.addValue(variance, "Instabilité", couple);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Instabilité des couples (X, Y)",
                "Couples (X, Y)",
                "Variance du Temps",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        
        // Ajustement de l'axe des ordonnées pour afficher les vraies valeurs
     // Ajustement de l'axe des ordonnées pour afficher les vraies valeurs
        CategoryPlot plot = (CategoryPlot) barChart.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(false); // Permet d'afficher des valeurs supérieures à 1 sans restriction

        // Rotation des labels des X (couples)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        ChartPanel chart = new ChartPanel(barChart);
        chartPanel.removeAll();
        chartPanel.add(chart, BorderLayout.CENTER);
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
