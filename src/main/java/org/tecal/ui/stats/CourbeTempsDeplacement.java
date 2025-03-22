package org.tecal.ui.stats;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.tecal.scheduler.data.SQL_DATA;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class CourbeTempsDeplacement extends JPanel {
    private static final long serialVersionUID = 1L;
    private JDateChooser dateDebutChooser, dateFinChooser;
    private JComboBox<String> poste1ComboBox, poste2ComboBox;
    
	public void setPoste1ComboBox(JComboBox<String> poste1ComboBox) {
		this.poste1ComboBox = poste1ComboBox;
	}

	
	public void setPoste2ComboBox(JComboBox<String> poste2ComboBox) {
		this.poste2ComboBox = poste2ComboBox;
	}

	private JPanel chartPanel;
    private Map<String, Integer> postesMap = new HashMap<>();
    private Connection conn;

    public CourbeTempsDeplacement() {
        conn = SQL_DATA.getInstance().getConnection();
        setLayout(new BorderLayout());

        // ** Panel pour la sélection des dates et postes **
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        // ** Ligne 1 : Date Début et Date Fin (centrées) **
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("Date début :"), gbc);

        dateDebutChooser = new JDateChooser();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        dateDebutChooser.setDate(cal.getTime());
        gbc.gridx = 1;
        topPanel.add(dateDebutChooser, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Date fin :"), gbc);

        dateFinChooser = new JDateChooser();
        dateFinChooser.setDate(new Date());
        gbc.gridx = 3;
        topPanel.add(dateFinChooser, gbc);

        // ** Ligne 2 : ComboBox Poste 1 et Poste 2 (centrées et alignées) **
        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(new JLabel("Poste précédent :"), gbc);

        poste1ComboBox = new JComboBox<>();
        gbc.gridx = 1;
        topPanel.add(poste1ComboBox, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Poste actuel :"), gbc);

        poste2ComboBox = new JComboBox<>();
        gbc.gridx = 3;
        topPanel.add(poste2ComboBox, gbc);

        // ** Ligne 3 : Bouton centré **
        JButton fetchButton = new JButton("Afficher la Courbe");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        topPanel.add(fetchButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Panel du graphique
        chartPanel = new JPanel(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        // Remplir les combobox au démarrage
        remplirComboBox();

        // Action du bouton
        fetchButton.addActionListener((ActionEvent e) -> afficherGraphique());
    }

    private void remplirComboBox() {
        String req = "SELECT NomPoste, NumPoste FROM dbo.Postes";
        try (PreparedStatement stmt = conn.prepareStatement(req);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String posteLabel = rs.getString("NomPoste");
                int posteNum = rs.getInt("NumPoste");
                postesMap.put(posteLabel, posteNum);
                poste1ComboBox.addItem(posteLabel);
                poste2ComboBox.addItem(posteLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    private int getCalibrage(int dep,int arrivee) {
        String req = """
        		
        		SELECT T.normal 
        		FROM  dbo.TempsDeplacements   T
        		INNER JOIN dbo.Postes P1 on ?=P1.NumPoste AND T.depart=P1.NumZone
        		INNER JOIN dbo.Postes P2 on ?=P2.NumPoste AND T.arrivee=P2.NumZone
        """;
        int tps=0;
       
       
        try (PreparedStatement stmt = conn.prepareStatement(req)){
        	 stmt.setInt(1, dep);
             stmt.setInt(2, arrivee);	
            ResultSet rs = stmt.executeQuery();
        
            if (rs.next()) {
                tps= rs.getInt("normal");
               
            }
            

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
		return tps;
    }

    private void afficherGraphique() {
    	
    	
        Date dateDebut = dateDebutChooser.getDate();
        Date dateFin = dateFinChooser.getDate();
        if (dateDebut == null || dateFin == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner les dates.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String poste1Label = (String) poste1ComboBox.getSelectedItem();
        String poste2Label = (String) poste2ComboBox.getSelectedItem();
        if (poste1Label == null || poste2Label == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner les postes.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int poste1 = postesMap.get(poste1Label);
        int poste2 = postesMap.get(poste2Label);
        int moyenne=getCalibrage(poste1,poste2);

        Timestamp dateDebutStr = new Timestamp(dateDebut.getTime());
        Timestamp dateFinStr = new Timestamp(dateFin.getTime());

        TimeSeries series = new TimeSeries("Temps de déplacement");
        String req = """
            SELECT 
			    CAST(D.DateEntreePoste AS DATE) AS Jour, 
			    AVG(D.TempsDeplacement - dbo.getOffset(C.vitesse_bas, C.vitesse_haut)) AS TempsDeplacementMoyen
			FROM ANODISATION.dbo.DetailsFichesProduction D
			INNER JOIN ANODISATION.dbo.DetailsChargesProduction C 
			    ON C.NumFicheProduction = D.NumFicheProduction AND C.NumLigne = 1
			WHERE D.NumPostePrecedent = ? 
			    AND D.NumPoste = ?
			    AND D.DateEntreePoste BETWEEN ? AND ?
			GROUP BY CAST(D.DateEntreePoste AS DATE)
			ORDER BY Jour;
        """;

        try (PreparedStatement stmt = conn.prepareStatement(req)) {
            stmt.setInt(1, poste1);
            stmt.setInt(2, poste2);
            stmt.setTimestamp(3, dateDebutStr);
            stmt.setTimestamp(4, dateFinStr);
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                double tempsDeplacement = rs.getDouble("TempsDeplacementMoyen");
                Date date = dbDateFormat.parse(rs.getString("Jour"));
                series.addOrUpdate(new Day(date), tempsDeplacement);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Création du dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        // Création du graphique
        JFreeChart lineChart = ChartFactory.createTimeSeriesChart(
                "Évolution du Temps de Déplacement",
                "Date",
                "Temps (secondes)",
                dataset,
                true, true, false
        );
        
      


        // Personnalisation du graphique
        XYPlot plot = lineChart.getXYPlot();
        
        ValueMarker horizontalLine = new ValueMarker(moyenne);
        horizontalLine.setPaint(Color.BLUE); // Couleur plus visible
        horizontalLine.setStroke(new BasicStroke(1.0f)); // Épaisseur plus grande
        plot.addRangeMarker(horizontalLine); 
        
        
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);

        // Configuration de l'axe X avec affichage des dates et labels verticaux
        DateAxis dateAxis = new DateAxis("Date");
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy/MM/dd"));
        dateAxis.setVerticalTickLabels(true);
        plot.setDomainAxis(dateAxis);

        // Configuration de l'axe Y
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setLabel("Temps (secondes)");
     
        
        
        // Affichage
        ChartPanel chart = new ChartPanel(lineChart);
      
   
        chartPanel.removeAll();
        chartPanel.add(chart, BorderLayout.CENTER);
        chartPanel.revalidate(); // Ajouté
        chartPanel.repaint();    // Ajouté
    }
    
    public void initialiserFiltres(int numPostePrecedent, int numPosteActuel) {
        // Initialisation des dates (dernier mois par défaut)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        dateDebutChooser.setDate(cal.getTime());
        dateFinChooser.setDate(new Date());

        // Sélection des postes dans les JComboBox
        for (Map.Entry<String, Integer> entry : postesMap.entrySet()) {
            if (entry.getValue() == numPostePrecedent) {
                poste1ComboBox.setSelectedItem(entry.getKey());
            }
            if (entry.getValue() == numPosteActuel) {
                poste2ComboBox.setSelectedItem(entry.getKey());
            }
        }
        afficherGraphique();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Courbe du Temps de Déplacement");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            CourbeTempsDeplacement c=new CourbeTempsDeplacement();
            frame.add(c);
            c.initialiserFiltres(9,15);
            frame.setVisible(true);
        });
    }
}
