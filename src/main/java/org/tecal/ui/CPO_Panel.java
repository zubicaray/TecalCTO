package org.tecal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tecal.scheduler.CST;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.Barre;
import org.tecal.scheduler.types.ElementGamme;

public class CPO_Panel extends JPanel {

	private static final Logger logger = LogManager.getLogger(CPO_Panel.class);
	private static final long serialVersionUID = 1L;
	private JTable mTableGammes;
	private JTable mTableBarres;
	private BarreTableModel mModelBarres;
	private JButton mBtnRun;
	private JTextField mTxtTpsMaxSolver;
	private JButton mBtnDelButton ;
	private JTextArea mTextArea;
	private JComboBox<String> mVitesseCombo;
	private DefaultTableModel mModelGammes;
	private Integer mNumBarre;


	private CPO_IHM mCPO_IHM;


	public CPO_Panel(CPO_IHM cpoIhm) {

		mCPO_IHM = cpoIhm;
		mNumBarre = 0;
		createPanelCPO();

	}

	public CPO_Panel() {
		mNumBarre = 0;
		createPanelCPO();
	}

	private void createPanelCPO() {
		JScrollPane scrollPaneMsg = new JScrollPane();

		// créer un ComboBox
		mVitesseCombo = new JComboBox<>();
		mVitesseCombo.addItem("lente");
		mVitesseCombo.addItem("normale");
		mVitesseCombo.addItem("rapide");

		JLabel lblGammes = new JLabel("gammes:");
		lblGammes.setFont(new Font("Tahoma", Font.PLAIN, 13));

		JTextField textFiltre = new JTextField();
		textFiltre.setColumns(10);

		JButton btnReload = new JButton();
		setIconButton(this, btnReload, "icons8-update-16.png");

		btnReload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				SQL_DATA.getInstance().setMissingTimeMovesGammes();
				// mModelGammes.fireTableStructureChanged();
				// mTableGammes.revalidate();
				mTableGammes.repaint();
			}
		});

		textFiltre.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				TableRowSorter<TableModel> sorter = new TableRowSorter<>((mTableGammes.getModel()));
				sorter.setRowFilter(RowFilter.regexFilter(textFiltre.getText()));

				mTableGammes.setRowSorter(sorter);
				mTableGammes.repaint();

			}
		});
		textFiltre.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		textFiltre.setColumns(10);

		JScrollPane scrollPane_gamme = new JScrollPane();

		JScrollPane scrollPaneBarres = new JScrollPane();

		mBtnRun = new JButton("GO");
		mBtnRun.setHorizontalAlignment(SwingConstants.LEFT);

		// Ajouter JTextField pour mTecalOrdo
	    mTxtTpsMaxSolver = new JTextField();
	    mTxtTpsMaxSolver.setColumns(10);
	    mTxtTpsMaxSolver.setHorizontalAlignment(SwingConstants.RIGHT);
	    mTxtTpsMaxSolver.setText(String.valueOf(mCPO_IHM.getTecalOrdo().getTpsMaxSolver())); // Initialisation avec getTpsMaxSolver()

	    // Listener pour mettre à jour mTecalOrdo
	    mTxtTpsMaxSolver.addActionListener(e -> {
	        try {
	            int tps = Integer.parseInt(mTxtTpsMaxSolver.getText().trim());
	            mCPO_IHM.getTecalOrdo().setTpsMaxSolver(tps);
	            logger.info("Temps maximum du solver mis à jour : " + tps);
	        } catch (NumberFormatException ex) {
	            JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre entier valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
	        }
	    });

		JButton btnDownButton = new JButton();
		btnDownButton.setHorizontalAlignment(SwingConstants.CENTER);

		JButton btnUpButton = new JButton();
		btnUpButton.setHorizontalAlignment(SwingConstants.CENTER);

		mBtnDelButton = new JButton();

		setIconButton(this, mBtnDelButton, "icons8-delete-16.png");

		GroupLayout gl_panelCPO = buildGrouping(scrollPaneMsg, lblGammes, textFiltre, btnReload, scrollPane_gamme,
				scrollPaneBarres, btnDownButton, btnUpButton, mBtnDelButton);

		setIconButton(this, btnUpButton, "icons8-up-16.png");
		setIconButton(this, btnDownButton, "icons8-down-16.png");
		setIconButton(this, mBtnRun, "icons8-play-16.png");

		mBtnDelButton.setHorizontalAlignment(SwingConstants.CENTER);
		mBtnDelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = mTableBarres.getSelectedRow();
				int max = mTableBarres.getRowCount();

				if (row >= 0 && row < max) { // Vérifie qu'une ligne est sélectionnée
					try {
						mModelBarres.removeBarre(row); // Supprime la ligne dans le modèle
						mTableBarres.clearSelection(); // Efface la sélection pour éviter des indices invalides
						mTableBarres.repaint();
						//mModelBarres.fireTableDataChanged();
					} catch (ArrayIndexOutOfBoundsException ex) {
						System.err.println("Erreur : ligne invalide sélectionnée.");
					}
				} else {
					JOptionPane.showMessageDialog(mTableBarres, "Aucune ligne sélectionnée.", "Erreur",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		btnUpButton.setHorizontalAlignment(SwingConstants.CENTER);
		btnUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 if (! mTableBarres.isEditing()) {
					 moveRowBy(-1);
				 }

			}
		});

		btnDownButton.setHorizontalAlignment(SwingConstants.CENTER);
		btnDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 if (! mTableBarres.isEditing()) {
					 moveRowBy(1);
				 }

			}
		});

		mTextArea = new JTextArea();
		scrollPaneMsg.setViewportView(mTextArea);

		buildTables(scrollPaneBarres);

		setActionListener();
		mTableGammes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_gamme.setViewportView(mTableGammes);
		setLayout(gl_panelCPO);
	}

	private GroupLayout buildGrouping(JScrollPane scrollPaneMsg, JLabel lblGammes, JTextField textFiltre,
			JButton btnReload, JScrollPane scrollPane_gamme, JScrollPane scrollPaneBarres, JButton btnDownButton,
			JButton btnUpButton, JButton btnDelButton) {
		GroupLayout gl_panelCPO = new GroupLayout(this);
	    gl_panelCPO.setHorizontalGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
	        .addGroup(gl_panelCPO.createSequentialGroup()
	            .addContainerGap()
	            .addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
	                .addComponent(scrollPaneMsg, GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
	                .addGroup(gl_panelCPO.createSequentialGroup()
	                    .addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
	                        .addGroup(gl_panelCPO.createSequentialGroup()
	                            .addComponent(scrollPane_gamme, GroupLayout.PREFERRED_SIZE, 616, GroupLayout.PREFERRED_SIZE)
	                            .addPreferredGap(ComponentPlacement.UNRELATED))
	                        .addGroup(Alignment.TRAILING, gl_panelCPO.createSequentialGroup()
	                            .addComponent(lblGammes, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
	                            .addGap(111)
	                            .addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                            .addGap(70).addComponent(btnReload).addGap(232)))
	                    .addComponent(scrollPaneBarres, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
	                    .addGap(18)
	                    .addGroup(gl_panelCPO.createParallelGroup(Alignment.TRAILING)
	                    	.addComponent(mTxtTpsMaxSolver, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
	                        .addComponent(mBtnRun, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
	                        .addGroup(gl_panelCPO.createSequentialGroup()
	                            .addGroup(gl_panelCPO.createParallelGroup(Alignment.TRAILING)
	                                .addComponent(btnDownButton, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
	                                .addComponent(btnUpButton, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
	                                .addComponent(btnDelButton, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
	                            .addGap(50)))))
	            .addContainerGap())

	    );
	    gl_panelCPO.setVerticalGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
	        .addGroup(gl_panelCPO.createSequentialGroup()
	            .addGap(50)
	            .addGroup(gl_panelCPO.createParallelGroup(Alignment.BASELINE)
	                .addComponent(lblGammes, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
	                .addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                .addComponent(btnReload))
	            .addGap(18)
	            .addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
	                .addGroup(gl_panelCPO.createParallelGroup(Alignment.BASELINE)
	                    .addComponent(scrollPane_gamme, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
	                    .addComponent(scrollPaneBarres, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
	                .addGroup(gl_panelCPO.createSequentialGroup()
	                    .addGap(35).addComponent(btnUpButton)
	                    .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnDownButton)
	                    .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(btnDelButton)
	                    .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(mTxtTpsMaxSolver, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                    .addGap(10)
	                    .addComponent(mBtnRun)))
	            .addGap(47)
	            .addComponent(scrollPaneMsg, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
	            .addGap(24))
	    );
	    return gl_panelCPO;
	}

	private void buildTables(JScrollPane scrollPaneBarres) {
		try {
			mModelBarres = new BarreTableModel() ;
			mTableBarres = new JTable();
			buildTableModelBarre();

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		mTableBarres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mTableBarres.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		scrollPaneBarres.setViewportView(mTableBarres);

		// It creates and displays the table
		try {

			mModelGammes = new DefaultTableModel() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isCellEditable(int row, int col) {
					return false; // Renders column 0 uneditable.

				}
			};

			mTableGammes = new JTable(mModelGammes) {
				/**
				*
				*/
				private static final long serialVersionUID = 1L;

				@Override
				public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
					Component c = super.prepareRenderer(renderer, row, column);

					if (isRowSelected(row)) {
						c.setBackground(Color.black);
						c.setForeground(Color.white);
					}
					return c;
				}

			};

			buildTableModelGamme();

			mTableGammes.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public BarreTableModel getModelBarres() {
		return mModelBarres;
	}

	public void setModelBarres(LinkedHashMap<Integer, Barre> set) {

		SwingUtilities.invokeLater(() -> {
			mModelBarres.setRowCount(0);

			for (Map.Entry<Integer, Barre> entry : set.entrySet()) {

				Barre b = entry.getValue();

				if (b.getIdbarre() > mNumBarre) {
					mNumBarre = b.getIdbarre();
				}

				mModelBarres.addBarre(b);
				mTableBarres.repaint();
				mTableBarres.revalidate();

			}
		});


	}



	public void setText(String s) {
		mTextArea.setText(mTextArea.getText() + s);
		mTextArea.setCaretPosition(mTextArea.getDocument().getLength());
	}

	public class GammeTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private final List<ElementGamme> gammeArray;
	    private final String[] columnNames = { "ligne", "zone", "durée", "dérive", "bloque pont" };

	    public GammeTableModel(List<ElementGamme> gammeArray) {
	        this.gammeArray = gammeArray;
	    }

	    @Override
	    public int getRowCount() {
	        return gammeArray.size();
	    }

	    @Override
	    public int getColumnCount() {
	        return columnNames.length;
	    }
	    @Override
	    public Class<?> getColumnClass(int columnIndex) {
	        switch (columnIndex) {
	            case 0: return Integer.class;    // Num Ligne
	            case 1: return String.class;     // Code Zone
	            case 2: return Integer.class;    // Time
	            case 3: return Integer.class;    // Derive
	            case 4: return Boolean.class;    // Bloque Pont
	            default: return Object.class;
	        }
	    }

	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	        ElementGamme element = gammeArray.get(rowIndex);
	        switch (columnIndex) {
	            case 0: return element.numligne;
	            case 1: return element.codezone;
	            case 2: return element.time;
	            case 3: return element.derive;
	            case 4: return element.BloquePont;
	            default: return null;
	        }
	    }

	    @Override
	    public String getColumnName(int column) {
	        return columnNames[column];
	    }

	    @Override
	    public boolean isCellEditable(int rowIndex, int columnIndex) {
	        return columnIndex == 2 || columnIndex == 3 || columnIndex == 4; // time, derive et BloquePont
	    }

	    @Override
	    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	        ElementGamme element = gammeArray.get(rowIndex);
	        switch (columnIndex) {
	            case 2: element.time=Integer.parseInt(aValue.toString()); break;
	            case 3: element.derive=(int) Double.parseDouble(aValue.toString()); break;
	            case 4: element.BloquePont=(Boolean) aValue; break;
	        }
	        fireTableCellUpdated(rowIndex, columnIndex); // Notifie la table que les données ont changé
	    }
	}
	public class BarreTableModel extends AbstractTableModel {
	    private static final long serialVersionUID = 1L;
		private final List<Barre> barres; // Liste des objets Barre
	    private final String[] columnNames = { "ID", "barre","gamme",  "desc.", "montée", "prio." };
	    public final static String[] VITESSES = CST.VITESSES;// Combobox valeurs

	    public BarreTableModel(List<Barre> barres) {
	        this.barres = barres;
	    }
	    public BarreTableModel() {
	        this.barres = new ArrayList<>();
	    }

	    @Override
	    public int getRowCount() {
	        return barres.size();
	    }

	    @Override
	    public int getColumnCount() {
	        return columnNames.length;
	    }
	    public Barre getBarre(int rowIndex) {
	        return barres.get(rowIndex);
	       }
	    
	    public void insertRow(int rowIndex, Barre barre) {
	        if (rowIndex < 0 || rowIndex > barres.size()) {
	            throw new IndexOutOfBoundsException("Index hors limites : " + rowIndex);
	        }
	        barres.add(rowIndex, barre); // Insère le nouvel objet à l'index spécifié
	        fireTableRowsInserted(rowIndex, rowIndex); // Notifie la table de l'ajout
	    }

	    
	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	        Barre barre = barres.get(rowIndex);
	        switch (columnIndex) {
	            case 1: return barre.getBarreNom();
	            case 0: return barre.getIdbarre();
	            case 2: return barre.getGamme();
	            case 3: return VITESSES[barre.getVitesseDescente()]; // Index -> Valeur
	            case 4: return VITESSES[barre.getVitesseMontee()]; // Index -> Valeur
	            case 5: return barre.isPrioritaire();
	            default: return null;
	        }
	    }
	    public void setRowCount(int newRowCount) {
	        int currentRowCount = barres.size();

	        if (newRowCount > currentRowCount) {
	            // Ajouter des lignes vides si le nouveau nombre de lignes est plus grand
	            for (int i = currentRowCount; i < newRowCount; i++) {
	                barres.add(new Barre(0, "", "", 0, 0, false)); // Barre vide avec valeurs par défaut
	            }
	        } else if (newRowCount < currentRowCount) {
	            // Supprimer les lignes en trop
	            for (int i = currentRowCount - 1; i >= newRowCount; i--) {
	                barres.remove(i);
	            }
	        }

	        fireTableDataChanged(); // Notifier la table que les données ont changé
	    }

	    @Override
	    public String getColumnName(int column) {
	        return columnNames[column];
	    }

	    @Override
	    public boolean isCellEditable(int rowIndex, int columnIndex) {
	        return columnIndex == 1	 || columnIndex == 3 || columnIndex == 4 || columnIndex == 5; // Éditable pour vitesses et prioritaire
	    }

	    @Override
	    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	        Barre barre = barres.get(rowIndex);
	        switch (columnIndex) {
		        case 1: // nom barre
	                barre.setBarreNom  (aValue.toString());
	                break;
	            case 3: // Vitesse Descente
	                barre.setVitesseDescente(java.util.Arrays.asList(VITESSES).indexOf(aValue.toString()));
	                break;
	            case 4: // Vitesse Montée
	                barre.setVitesseMontee(java.util.Arrays.asList(VITESSES).indexOf(aValue.toString()));
	                break;
	            case 5: // Prioritaire
	                barre.setPrioritaire((Boolean) aValue);
	                break;
	        }
	        fireTableCellUpdated(rowIndex, columnIndex);
	    }

	    public void addBarre(Barre barre) {
	    	barres.add(barre);
	        fireTableRowsInserted(barres.size() - 1, barres.size() - 1);
	    }

	    public void removeBarre(int rowIndex) {
	    	barres.remove(rowIndex);
	        fireTableRowsDeleted(rowIndex, rowIndex);
	    }


	}

	// Classe interne pour le SwingWorker
	private static class Worker extends SwingWorker<Void, Void> {
		private final CPO_Panel main;

		// Passer une référence à la classe Main
		public Worker(CPO_Panel main) {
			this.main = main;
		}

		@Override
		protected Void doInBackground() throws Exception {

			try {
				main.execute(); // Appel de la méthode
		    } catch (Exception e) {
		        JOptionPane.showMessageDialog(null,
		            "Erreur dans doInBackground: " + e.getMessage(),
		            "Erreur",
		            JOptionPane.ERROR_MESSAGE);


		        logger.error("Erreur dans doInBackground", e);
		    }


			return null;
		}

		@Override
		protected void done() {
			System.out.println("Thread terminé !");
		}
	}

	private void setActionListener() {
		mBtnRun.addActionListener(e -> {


			computeBarresFutures();

			// Créer et démarrer un SwingWorker
			new Worker(this).execute();

		});

	}
	public void execute() {
		mCPO_IHM.run();
	}

	private void computeBarresFutures() {

		LinkedHashMap<Integer, Barre> barres = new LinkedHashMap<>();
	
		try {

			for (int count = 0; count < mTableBarres.getRowCount(); count++) {

				 if (mTableBarres.getValueAt(count, 0) == null) {
			        logger.error("mTableBarres Valeur null détectée à la ligne " + count + ", colonne 0.");
			        continue; // Ignorer cette ligne
			    }

				Barre b=mModelBarres.getBarre(count);
				if (b.isPrioritaire()) {
					logger.info("barre: " + b.getBarreNom() + " prioritaire !");
				}

				barres.put(b.getIdbarre(), b);
				
			}
			mCPO_IHM.setBarresSettingsFutures( barres);
		}
		catch(Exception e) {
			logger.error("Erreur execute recup vitesse "+e.getMessage());
			JOptionPane.showMessageDialog(null, "Erreur execute recup vitesse "+e.getMessage(), "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		}

	}
	public void setBarresSettingsFutures(LinkedHashMap<Integer, Barre> barresSettingsFutures) {
		for(Barre b:barresSettingsFutures.values()) {
			mModelBarres.addBarre(b);
		}
	}

	private  void showGammeEditor(CPO_IHM cpo,List<ElementGamme> gammeArray) {
	    // Créer le modèle pour gammeArray
	    GammeTableModel gammeModel = new GammeTableModel(gammeArray);
	    JTable gammeTable = new JTable(gammeModel);

	    // Configurer les colonnes
	    gammeTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JCheckBox()));
	    gammeTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
	        private static final long serialVersionUID = 1L;

			@Override
	        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	            JCheckBox checkBox = new JCheckBox();
	            checkBox.setSelected((Boolean) value);
	            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
	            if (isSelected) {
	                checkBox.setBackground(table.getSelectionBackground());
	            } else {
	                checkBox.setBackground(table.getBackground());
	            }
	            return checkBox;
	        }
	    });

	    // Fenêtre modale
	    JDialog dialog = new JDialog(cpo, "Éditeur de Gamme", true);
	    dialog.setLayout(new BorderLayout());
	    dialog.add(new JScrollPane(gammeTable), BorderLayout.CENTER);

	    JButton closeButton = new JButton("Fermer");
	    closeButton.addActionListener(e -> dialog.dispose());

	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(closeButton);

	    dialog.add(buttonPanel, BorderLayout.SOUTH);
	    dialog.setSize(600, 300);
	    dialog.setLocationRelativeTo(null); // Centrer la fenêtre
	    dialog.setVisible(true);
	}

	public void buildTableModelBarre() throws SQLException {





		mTableBarres.setModel(mModelBarres);

		TableColumnModel tcm = mTableBarres.getColumnModel();
		tcm.removeColumn(tcm.getColumn(0));

		tcm.getColumn(0).setPreferredWidth(100);


		TableColumn colBarre = mTableBarres.getColumnModel().getColumn(0);
		colBarre.setCellEditor(new DefaultCellEditor(new JTextField()));

		TableColumn colMontee = mTableBarres.getColumnModel().getColumn(2);

		// définir l'éditeur par défaut
		colMontee.setCellEditor(new DefaultCellEditor(mVitesseCombo));

		TableColumn colDescente = mTableBarres.getColumnModel().getColumn(3);
		// définir l'éditeur par défaut
		colDescente.setCellEditor(new DefaultCellEditor(mVitesseCombo));

		TableColumn colPrio = mTableBarres.getColumnModel().getColumn(4);
		colPrio.setCellEditor(mTableBarres.getDefaultEditor(Boolean.class));
		colPrio.setCellRenderer(mTableBarres.getDefaultRenderer(Boolean.class));

		mTableBarres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mTableBarres.getTableHeader().setDraggedColumn( null );

		// columnModel.getColumn(0).setMaxWidth(50);
		mTableBarres.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		 JPopupMenu popupMenu = new JPopupMenu();
         JMenuItem changeGammeItem = new JMenuItem("Changer Gamme");
         popupMenu.add(changeGammeItem);

         // Ajouter le menu au clic droit
         mTableBarres.addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 if (e.isPopupTrigger()) {
                     int row = mTableBarres.rowAtPoint(e.getPoint());
                     if (row != -1) {
                    	 mTableBarres.setRowSelectionInterval(row, row); // Sélectionner la ligne
                         popupMenu.show(mTableBarres, e.getX(), e.getY());
                     }
                 }
             }

             @Override
             public void mouseReleased(MouseEvent e) {
                 mousePressed(e);
             }
         });

         // Action pour l'item "Changer Gamme"
         changeGammeItem.addActionListener(e -> {
             int selectedRow = mTableBarres.getSelectedRow();
             if (selectedRow != -1) {
                 Barre selectedBarre = mModelBarres.getBarre(selectedRow);
                 showGammeEditor(mCPO_IHM,selectedBarre.getGammeArray());
             }
         });

	}

	public void removeBarre(int barre) {
	    // Terminer l'édition de cellule si nécessaire
	    if (mTableBarres.isEditing()) {
	        mTableBarres.getCellEditor().stopCellEditing();
	    }
	    synchronized (mModelBarres) {
	    	int rowCount = mTableBarres.getRowCount();
		    if (rowCount > 0) {
		        int i = 0;
		        boolean found = false;

		        for (; i < rowCount; i++) {
		            Object value = mModelBarres.getValueAt(i, 0); // Assurez-vous que l'indice est valide
		            if (value instanceof Integer && barre == (int) value) {
		                found = true;
		                break;
		            }
		        }

		        if (found) {
		            mModelBarres.removeBarre(i); // Supprime la ligne et notifie automatiquement les changements
		            mTableBarres.clearSelection(); // Efface la sélection pour éviter des indices invalides

					//mModelBarres.fireTableDataChanged();
					mTableBarres.repaint();
		            logger.info("Barre avec ID " + barre + " partie en prod.");
		        } else {
		            logger.warn("Barre avec ID " + barre + " introuvable.");
		        }
		    } else {
		        logger.warn("Aucune barre à supprimer : la table est vide.");
		    }
	    }


	}

	public void buildTableModelGamme() throws SQLException {

		mTableGammes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		mTableGammes.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		mTableGammes.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				JTable table = (JTable) mouseEvent.getSource();
				Point point = mouseEvent.getPoint();
				int row = table.rowAtPoint(point);
				if(table.getRowCount()>0) {
					if (row >= 0 && row < table.getRowCount()) {
						// Appeler convertRowIndexToModel ici
						row = table.convertRowIndexToModel(row);
						if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
							String gamme = table.getModel().getValueAt(row, 0).toString();


							Barre b=new Barre( ++mNumBarre, mNumBarre + "", gamme, CST.VITESSE_NORMALE,
									CST.VITESSE_NORMALE, false );
							mModelBarres.addBarre(b);
							mTableBarres.clearSelection();
							mTableBarres.repaint();

						}
					}
				}
			}
		});

		mTableGammes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {


				if (table.getRowCount()>0 && row >= 0 && row < table.getRowCount()) {

					int modelIndex = table.convertRowIndexToModel(row);
					if(modelIndex>0) {
						String gamme = (String) table.getModel().getValueAt(modelIndex, 0);

						if (SQL_DATA.getInstance().getMissingTimeMovesGammes().contains(gamme)) {
							setBackground(Color.RED);
							setForeground(Color.BLACK);
						} else {
							setBackground(table.getBackground());
							setForeground(table.getForeground());
						}
					}


				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			}
		});

	}

	public void setRessource(ResultSet rs) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();

		// names of columns
		Vector<String> columnNames = new Vector<>();
		int columnCount = metaData.getColumnCount();
		for (int column = 1; column <= columnCount; column++) {
			columnNames.add(metaData.getColumnName(column));
		}

		// data of the table
		Vector<Vector<Object>> data = new Vector<>();
		while (rs.next()) {
			Vector<Object> vector = new Vector<>();
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				vector.add(rs.getObject(columnIndex));
			}
			data.add(vector);
		}
		mModelGammes.setDataVector(data, columnNames);
		TableColumnModel columnModel = mTableGammes.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(70);
		columnModel.getColumn(0).setMaxWidth(400);

	}

	public static void setIconButton(Object o, JButton btnButton, String fileName) {
		try {

			InputStream in = o.getClass().getResourceAsStream("/" + fileName);
			BufferedImage someImage = ImageIO.read(in);
			ImageIcon img = new ImageIcon(someImage);
			btnButton.setIcon(img);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	private void moveRowBy(int by) {

		SwingUtilities.invokeLater(() -> {
			
		    if (mModelBarres.getRowCount() > 0) {
		    	int[] rows = mTableBarres.getSelectedRows();
			    if (rows.length == 0) {
			        return; // Pas de ligne sélectionnée
			    }

			    int row = rows[0];
			    int rowCount = mModelBarres.getRowCount();
			    int destination = row + by;

			    // Vérifiez si les indices sont valides
			    if (row < 0 || row >= rowCount || destination < 0 || destination >= rowCount) {
			        return;
			    }

			    try {
			        // Sauvegarde des données de la ligne
			    	Barre data = mModelBarres.getBarre(row);
			        
			        // Modification du modèle
			        SwingUtilities.invokeLater(() -> {
			        	mModelBarres.removeBarre(row);
			        	mModelBarres.insertRow(destination, data);
			            mTableBarres.setRowSelectionInterval(destination, destination);
			        });

			    } catch (Exception e) {
			        logger.error("Erreur MOVE ! " + e.getMessage(), e);
			        JOptionPane.showMessageDialog(null, "Erreur lors du déplacement de ligne : " + e.getMessage(),
			                "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			    }
		    }
		});


	}
	public void set_enable(boolean enable) {
		mTableBarres.setEnabled(enable);
		mTableGammes.setEnabled(enable);
		mBtnRun.setEnabled(enable);
		mBtnDelButton.setEnabled(enable);
	}
}
