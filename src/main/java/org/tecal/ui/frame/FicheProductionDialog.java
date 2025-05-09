package org.tecal.ui.frame;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.ui.stats.CourbeTempsDeplacement;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class FicheProductionDialog extends JDialog {
    private static final long serialVersionUID = 1L;
	private JTable table;
    private DefaultTableModel tableModel;
    
    public FicheProductionDialog(Frame parent, String numFicheProduction,String gamme) {
        super(parent, "Détails Fiche Production: "+numFicheProduction, true);
        setLayout(new BorderLayout());
        
        // Définition des colonnes
        String[] columnNames = {"Départ", "Arrivée", "Réel", "Calibrage","N1","N2"};
        
        tableModel = new DefaultTableModel(columnNames,0) {
            private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rend toutes les cellules non éditables
            }
        };

       
        table = new JTable(tableModel);
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

			@Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int reel = (int) table.getValueAt(row, 2);
                int calibrage = (int) table.getValueAt(row, 3);
                if (Math.abs(calibrage - reel) >= 10) {
                    cell.setBackground(Color.RED);
                } else {
                    cell.setBackground(Color.WHITE);
                }
                return cell;
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) { // Double-clic et ligne sélectionnée
                    int selectedRow = table.getSelectedRow();
                    Object postePrecedent = table.getValueAt(selectedRow, 4); // Colonne 4
                    Object posteActuel = table.getValueAt(selectedRow, 5);    // Colonne 5
                    
                    if (postePrecedent != null && posteActuel != null) {
                        try {
                            int numPostePrecedent = Integer.parseInt(postePrecedent.toString());
                            int numPosteActuel = Integer.parseInt(posteActuel.toString());

                         // Récupérer la fenêtre parente (peut être un JFrame ou un JDialog)
                            Window parentWindow = SwingUtilities.getWindowAncestor(table);

                            // Créer et initialiser la fenêtre CourbeTempsDeplacement
                            CourbeTempsDeplacement courbe = new CourbeTempsDeplacement();
                            courbe.initialiserFiltres(numPostePrecedent, numPosteActuel);

                            // Création du JDialog modal avec le bon parent
                            JDialog dialog = new JDialog(parentWindow, "Courbe Temps de Déplacement", Dialog.ModalityType.APPLICATION_MODAL);
                            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                            dialog.setSize(800, 600);
                            dialog.setLayout(new BorderLayout());
                            dialog.add(courbe, BorderLayout.CENTER);
                            dialog.setLocationRelativeTo(parentWindow); // Centrer par rapport à la fenêtre parente
                            dialog.setVisible(true); // Rendre la fenêtre modale

                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(table, "Erreur : Numéro de poste invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        Connection connection=SQL_DATA.getInstance().getConnection();
        
        // Remplissage des données
        loadData(numFicheProduction, connection);
     // Masquer les colonnes 4 et 5
        for (int col = 4; col <= 5; col++) {
            table.getColumnModel().getColumn(col).setMinWidth(0);
            table.getColumnModel().getColumn(col).setMaxWidth(0);
            table.getColumnModel().getColumn(col).setWidth(0);
            table.getColumnModel().getColumn(col).setPreferredWidth(0);
        }
        // Ajout de la table dans un JScrollPane
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Panel pour les boutons
        JPanel buttonPanel = new JPanel();
        
        // Bouton de fermeture
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        // Bouton calibrer
        JButton calibrerButton = new JButton("Calibrer");
        
        calibrerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				calibrage(numFicheProduction, gamme) ;
				loadData(numFicheProduction, connection);
			}
		});
        buttonPanel.add(calibrerButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        setSize(600, 400);
        setLocationRelativeTo(parent);
    }
    
    private void loadData(String numFicheProduction, Connection connection) {
    	tableModel.setRowCount(0); // Efface les anciennes données
        String query = "SELECT Z1.CodeZone AS depart, Z2.CodeZone AS arrivee, " +
                       "F.TempsDeplacement AS reel, TD.normal + dbo.getOffset(DC.vitesse_bas, DC.vitesse_haut) AS calibrage, " +
                       "P1.NumPoste AS N1, P2.NumPoste AS N2 " +
                       "FROM DetailsFichesProduction F " +
                       "INNER JOIN DetailsChargesProduction DC ON DC.NumLigne=1 AND DC.NumFicheProduction=F.NumFicheProduction   " +
                       "INNER JOIN Postes P1 ON P1.NumPoste=F.NumPostePrecedent " +
                       "INNER JOIN Zones Z1 ON Z1.NumZone=P1.NumZone " +
                       "INNER JOIN Postes P2 ON P2.NumPoste=F.NumPoste " +
                       "INNER JOIN Zones Z2 ON Z2.NumZone=P2.NumZone " +
                       "INNER JOIN TempsDeplacements TD ON Z2.NumZone=TD.arrivee AND Z1.NumZone=TD.depart " +
                       "WHERE F.NumFicheProduction=?   " +
                       "ORDER BY F.numligne";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, numFicheProduction);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String depart = rs.getString("depart");
                    String arrivee = rs.getString("arrivee");
                    int reel = rs.getInt("reel");
                    int calibrage = rs.getInt("calibrage");
                    int N1 = rs.getInt("N1");
                    int N2 = rs.getInt("N2");
                    tableModel.addRow(new Object[]{depart, arrivee, reel, calibrage,N1,N2});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des données: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void calibrage(String of, String gamme) {


		int resultGammeChanged = 0;
		if (SQL_DATA.getInstance().gammeChangedAfterOF(of, gamme)) {
			resultGammeChanged = JOptionPane.showConfirmDialog((Component) null,
					"La gamme a changé depuis que cet OF est passé en prod. Des mouvements peuvent manquer ... continuer?",
					"alert", JOptionPane.YES_NO_OPTION);

		}

		if (resultGammeChanged == 0) {

			int result = JOptionPane.showConfirmDialog((Component) null,
					"OF choisi: " + of + ". Voulez-vous aussi écraser les valeurs non nulles?", "alert",
					JOptionPane.YES_NO_CANCEL_OPTION);

			if (result != 2) {
				boolean updateNull = (result == 0);
				
				if (!SQL_DATA.getInstance().updateTpsMvts(of, updateNull)) {
					// MAJ des gammes
					SQL_DATA.getInstance().setMissingTimeMovesGammes();
				}
			}
		

			if (SQL_DATA.getInstance().gammeCalibrageExists(gamme)) {
				result = JOptionPane.showConfirmDialog((Component) null,
						"La gamme a déjà un OF de calibré, MAJ ?", "alert", JOptionPane.YES_NO_OPTION);

				if (result == 0) {
					SQL_DATA.getInstance().updateCalibrageGamme(gamme, of);
				}
			} else {
				SQL_DATA.getInstance().insertCalibrageGamme(gamme, of);
			}

		}

	}

    
    public static void main(String[] args) {
        // Exemple de connexion (adapter selon votre configuration)
       
            SwingUtilities.invokeLater(() -> new FicheProductionDialog(null,"00089612", "000735").setVisible(true));
       
    }
}
