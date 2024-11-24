package org.tecal.ui;

	import javax.swing.*;
	import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.tecal.scheduler.data.SQL_DATA;

import java.awt.*;
	import java.sql.*;

	public class ZonesPanel extends JPanel {

	    private static final long serialVersionUID = 1L;
		private JTable table;
	    private DefaultTableModel tableModel;
	    private Connection connection=SQL_DATA.getInstance().getmConnection();

	    public ZonesPanel() {
	        setLayout(new BorderLayout());

	        // Initialize table model
	        String[] columnNames = {
	        		"NumZone", 
	            "CodeZone", 
	            "NomPremierPoste",
	            "NomDernierPoste", "Nombre de postes", "dérive", "Sécurite ponts"
	        };
	        tableModel = new DefaultTableModel(columnNames, 0) {
	            private static final long serialVersionUID = 1L;

				@Override
	            public boolean isCellEditable(int row, int column) {
	                return column != 0; // Prevent editing of the primary key
	            }
	        };
	        table = new JTable(tableModel);
	        add(new JScrollPane(table), BorderLayout.CENTER);
	     // Add checkbox for the "SecuritePonts" column
	        table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()));
	        table.getColumnModel().getColumn(6).setCellRenderer(new CheckboxRenderer());


	        // Add buttons for actions
	        JPanel buttonPanel = new JPanel();
	        JButton loadButton = new JButton("Charger les données");
	        JButton saveButton = new JButton("Enregistrer les modifications");
	        buttonPanel.add(loadButton);
	        buttonPanel.add(saveButton);
	        add(buttonPanel, BorderLayout.SOUTH);

	        // Add action listeners
	        loadButton.addActionListener(e -> loadData());
	        saveButton.addActionListener(e -> saveData());

	       
	    }

	    private void loadData() {
	        tableModel.setRowCount(0); // Clear existing data
	     
            ResultSet rs = SQL_DATA.getInstance().getBDDZones();

            try {
				while (rs.next()) {
				    Object[] row = new Object[11];
				    row[0] = rs.getString("NumZone");
				    row[1] = rs.getString("CodeZone");				   
				    row[2] = rs.getString("NomPremierPoste");
				    row[3] = rs.getString("NomDernierPoste");
				    row[4] = rs.getShort("NbrPostes");
				    row[5] = rs.getObject("derive");
				    row[6] = rs.getObject("SecuritePonts") != null && (Short) rs.getObject("SecuritePonts") == 1;
				    tableModel.addRow(row);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	      
	    }

	    private void saveData() {
	        String updateQuery = "UPDATE Zones SET derive = ?, SecuritePonts = ? "
	            + "WHERE NumZone = ?";

	        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
	            for (int i = 0; i < tableModel.getRowCount(); i++) {
	                pstmt.setShort(1, Short.parseShort(tableModel.getValueAt(i, 5).toString()));
	                pstmt.setBoolean(2, Boolean.parseBoolean(tableModel.getValueAt(i, 6).toString()));	            
	           
	                pstmt.setInt(3, Integer.parseInt(tableModel.getValueAt(i, 0).toString()));

	                pstmt.addBatch();
	            }
	            pstmt.executeBatch();
	            JOptionPane.showMessageDialog(this, "Données enregistrées avec succès !");
	            SQL_DATA.getInstance().setZonesSecu();
	            SQL_DATA.getInstance().setZones();
	        } catch (SQLException e) {
	            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement des données : " + e.getMessage());
	        }
	    }
	 // Renderer for the checkbox column
	    private static class CheckboxRenderer extends JCheckBox implements TableCellRenderer {
	        private static final long serialVersionUID = 1L;

			@SuppressWarnings("unused")
			public CheckboxRenderer() {
	            setHorizontalAlignment(SwingConstants.CENTER);
	        }

	        @Override
	        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	            setSelected(value != null && (boolean) value);
	            return this;
	        }
	    }


	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	            JFrame frame = new JFrame("Gestion des Zones");
	            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	            frame.setContentPane(new ZonesPanel());
	            frame.setSize(800, 600);
	            frame.setLocationRelativeTo(null);
	            frame.setVisible(true);
	        });
	    }
	}
