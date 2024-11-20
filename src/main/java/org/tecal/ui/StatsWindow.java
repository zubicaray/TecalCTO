package org.tecal.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.tecal.scheduler.data.SQL_DATA;

import java.awt.*;
import java.sql.*;

public class StatsWindow extends JFrame {
	    private static final long serialVersionUID = 1L;
		private JTable table;
		private SQL_DATA sqlCnx;

	    public StatsWindow(String[] listeOF) {
	        setTitle("Taux d'occupation en anodisation");
	        setSize(800, 600);
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	        // Configuration de la JTable
	        table = new JTable();
	        sqlCnx = SQL_DATA.getInstance();
	        JScrollPane scrollPane = new JScrollPane(table);
	        add(scrollPane, BorderLayout.CENTER);

	        // Charger les données dans la JTable
	        chargerDonnees(listeOF);
	        pack();
	        ajusterHauteurFenetre();
	        setLocationRelativeTo(null); // Centrer la fenêtre à l'écran
	    }

	    private void chargerDonnees(String[] listeOF) {
	    	try {
		    	ResultSet resultSet=sqlCnx.getStatsAnodisation(listeOF);
	            // Obtenir les métadonnées pour configurer la JTable
	            ResultSetMetaData metaData;
				
				int columnCount;
				metaData = resultSet.getMetaData();
				columnCount = metaData.getColumnCount();
				
				 // Noms des colonnes
	            String[] columnNames = new String[columnCount];
	            for (int i = 1; i <= columnCount; i++) {
	                columnNames[i - 1] = metaData.getColumnName(i);
	            }

	            // Données de la table
	            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
	            while (resultSet.next()) {
	                Object[] row = new Object[columnCount];
	                for (int i = 1; i <= columnCount; i++) {
	                    row[i - 1] = resultSet.getObject(i);
	                }
	                tableModel.addRow(row);
	            }

	            // Appliquer le modèle à la JTable
	            table.setModel(tableModel);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           
		

           

	    }
	    private void ajusterHauteurFenetre() {
	        int tablePreferredHeight = table.getPreferredSize().height;
	        int scrollPanePreferredHeight = tablePreferredHeight + table.getTableHeader().getPreferredSize().height;

	        // Définir la hauteur préférée
	        JScrollPane scrollPane = (JScrollPane) getContentPane().getComponent(0);
	        scrollPane.setPreferredSize(new java.awt.Dimension(scrollPane.getPreferredSize().width, scrollPanePreferredHeight));

	        pack(); // Réajuster la fenêtre
	    }
	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {

	        	String[] liste={"00086810","00086809","00086808","00086807","00086806","00086805","00086804","00086803",
	        	        	        	    "00086802","00086801","00086800","00086799","00086798","00086797","00086796","00086795","00086794",
	        	        	        	    "00086793","00086792","00086791","00086790","00086789","00086788","00086787","00086786","00086785",
	        	        	        	    "00086784","00086783","00086782","00086781","00086780","00086779","00086778","00086777","00086776","00086775"};	
	        	StatsWindow fenetre = new StatsWindow( liste);
	            fenetre.setVisible(true);
	        });
	    }
	}
