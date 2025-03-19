package org.tecal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jfree.ui.RefineryUtilities;
import org.tecal.scheduler.CST;
import org.tecal.scheduler.TecalOrdoParams;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.Barre;
import org.tecal.ui.frame.CPO_LOGS_GANT;
import org.tecal.ui.frame.FicheProductionDialog;
import org.tecal.ui.panel.ZonesPanel;
import org.tecal.ui.stats.InstabiliteGraph;
import org.tecal.ui.stats.StatsQualite;
import org.tecal.ui.stats.StatsWindow;
import org.tecal.ui.stats.TauxAnodisationPanel;

import com.formdev.flatlaf.FlatDarkLaf;



class DateLabelFormatter extends AbstractFormatter {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String datePattern = "yyyy-MM-dd";
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

	@Override
	public Object stringToValue(String text) throws ParseException {
		return dateFormatter.parseObject(text);
	}

	@Override
	public String valueToString(Object value) throws ParseException {
		if (value != null) {
			Calendar cal = (Calendar) value;
			return dateFormatter.format(cal.getTime());
		}

		return "";
	}
}

public class TecalGUI {

	private JFrame frmTecalOrdonnanceur;
	// private JFrame mCPO_IHM;
	private CPO_IHM mCPO_IHM;

	private DefaultTableModel modelVisuProd;

	List<Image> mIcons;

	private SQL_DATA sqlCnx;

	private JTextField textTEMPS_ZONE_OVERLAP_MIN;

	private JTextField textTEMPS_MVT_PONT_MIN_JOB;
	private JTextField textTEMPS_MVT_PONT;
	private JTextField textTEMPS_ANO_ENTRE_P1_P2;
	private JTextField textANODISATION_NUMZONE;
	private JTabbedPane tabbedPaneMain;
	private JTable tableOF;
	private JDatePickerImpl datePicker;
	JPanel panelVisuProd;
	JTabbedPane mParamTabs;
	JScrollPane scrollPaneVisuProd;

	private JTextField textTEMPS_MAX_SOLVEUR;
	private JTextField textEnd;
	private JTextField textStart;
	private JTable tableTpsMvts;
	private JButton btnRefresh;
	private JButton btnRefreshCalib;
	private JButton btnEraseButton;
	private JButton btnImport;
	private JPanel panelCalibrage;
	private JTextField filterTextField;
	private TableRowSorter<DefaultTableModel> sorter;

	private JTable mTableCalibrage;
	private DefaultTableModel tableModelCalibrage;
	private JTextField textNbAno;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		cosmeticGUI();

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					String version=CPO_IHM.getManifestVersion();
					TecalGUI window = new TecalGUI();
					window.frmTecalOrdonnanceur.setTitle("Tecal GUI - " + version);
					window.frmTecalOrdonnanceur.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void cosmeticGUI() {
		FlatDarkLaf.setup();

		Color back = new Color(255, 248, 223);
		Color fore = new Color(1, 1, 1);
		UIManager.put("Table.background", back);
		UIManager.put("TextArea.background", back);
		UIManager.put("TextArea.foreground", fore);
		UIManager.put("TextField.background", back);
		UIManager.put("ComboBox.foreground", fore);
		UIManager.put("ComboBox.background", back);
		UIManager.put("TextField.foreground", fore);
		UIManager.put("Table.foreground", fore);
		UIManager.put("Button.arc", 999);
		UIManager.put("Component.arc", 999);
		UIManager.put("ProgressBar.arc", 999);
		UIManager.put("TextComponent.arc", 999);
	}

	/**
	 * Create the application.
	 */
	public TecalGUI() {

		sqlCnx = SQL_DATA.getInstance();
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frmTecalOrdonnanceur = new JFrame();

		frmTecalOrdonnanceur.setTitle("Tecal PROD");

		mIcons = loadIcons(this);
		frmTecalOrdonnanceur.setIconImage(mIcons.get(0));
		
		frmTecalOrdonnanceur.setIconImages(mIcons);
		
		// Chargement de l'icône depuis le classpath
        java.net.URL iconURL = TecalGUI.class.getClassLoader().getResource("icon_32x32.ico");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            frmTecalOrdonnanceur.setIconImage(icon.getImage());
        } else {
            System.err.println("Icône non trouvée !");
        }

		// mCPO_IHM = new CPO(mIcons);

		frmTecalOrdonnanceur.setBounds(100, 100, 889, 674);
		frmTecalOrdonnanceur.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mParamTabs=new  JTabbedPane(SwingConstants.TOP);
		tabbedPaneMain = new JTabbedPane(SwingConstants.TOP);
		GroupLayout groupLayout = new GroupLayout(frmTecalOrdonnanceur.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPaneMain)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addComponent(tabbedPaneMain, GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
		);

		
		buildVisuProd();
		buildStats();
		buildMvtPonts();
		buildCalibrageTab();
		buildParamsTab(tabbedPaneMain);

		

		frmTecalOrdonnanceur.getContentPane().setLayout(groupLayout);
	}

	private void buildStats() {
		TauxAnodisationPanel  taux= new TauxAnodisationPanel();
		StatsQualite mens=new StatsQualite();
		InstabiliteGraph instGra=new InstabiliteGraph();
		JTabbedPane statTabbedPaneMain = new JTabbedPane(SwingConstants.LEFT);
		ImageIcon iconStat = new ImageIcon(this.getClass().getResource("/icons8-statistic-16.png"));

		tabbedPaneMain.addTab("Statistiques",iconStat,statTabbedPaneMain);
		statTabbedPaneMain.addTab("Production", null, taux, null);
		statTabbedPaneMain.addTab("Qualité", null, mens, null);
		statTabbedPaneMain.addTab("Stabilité des mouvements", null, instGra, null);
	}

	public static List<Image> loadIcons(Object o) {
		List<Image> lIcons = new ArrayList<>();
		try {

			InputStream in = o.getClass().getResourceAsStream("/gantt-chart 16.png");
			BufferedImage someImage = ImageIO.read(in);
			ImageIcon img;
			img = new ImageIcon(someImage);
			lIcons.add(img.getImage());
			in = o.getClass().getResourceAsStream("/gantt-chart 32.png");
			someImage = ImageIO.read(in);
			img = new ImageIcon(someImage);
			lIcons.add(img.getImage());

		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} // usable in Java 9+

		return lIcons;
	}

	private void buildVisuProd() {

		panelVisuProd = new JPanel(new BorderLayout());

		UtilDateModel model = new UtilDateModel();

		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		// Don't know about the formatter, but there it is...
		datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

		datePicker.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					buildTableModelVisuProd();
				} catch (SQLException e1) {
					JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		});
		ImageIcon icon = new ImageIcon(this.getClass().getResource("/icons8-visu-16.png"));
		tabbedPaneMain.addTab("Visuel de prod", icon, panelVisuProd, null);

		JPanel buttonsDate = new JPanel(new FlowLayout());
		buttonsDate.add(datePicker);
		JButton btnRunVisuProd = new JButton("Gantt PROD");
		buttonsDate.add(btnRunVisuProd);
		JButton btnGanttCpo = new JButton("Gantt CPO");
		buttonsDate.add(btnGanttCpo);

		JButton btnGanttCpoLogs = new JButton("CPO logs");
		buttonsDate.add(btnGanttCpoLogs);

		panelVisuProd.add(buttonsDate, BorderLayout.PAGE_START);

		scrollPaneVisuProd = new JScrollPane();
		scrollPaneVisuProd.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
			}
		});
		panelVisuProd.add(scrollPaneVisuProd, BorderLayout.CENTER);

		btnRunVisuProd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				if (tableOF == null) {
					return;
				}

				int[] sel = tableOF.getSelectedRows();
				if (sel.length == 0) {
					JOptionPane.showMessageDialog(frmTecalOrdonnanceur, "Pas d'OF choisi !", "Visuel de prod",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				String lOF[] = new String[sel.length];
				for (int i = 0; i < sel.length; i++) {
					lOF[i] = tableOF.getModel().getValueAt(sel[i], 0).toString();
				}

				frmTecalOrdonnanceur.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Date d = (Date) datePicker.getModel().getValue();
				final GanttChart ganttTecal = new GanttChart("Prod du " + d);
				ganttTecal.prod_diag(lOF, d);
				ganttTecal.pack();
				ganttTecal.setSize(new java.awt.Dimension(1500, 870));
				RefineryUtilities.centerFrameOnScreen(ganttTecal);
				ganttTecal.setVisible(true);
				ganttTecal.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frmTecalOrdonnanceur.setCursor(Cursor.getDefaultCursor());

				StatsWindow fenetre = new StatsWindow( lOF);
				fenetre.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	            fenetre.setVisible(true);

			}
		});

		btnGanttCpoLogs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				java.util.Date d = (java.util.Date) datePicker.getModel().getValue();
				if(d == null ){
					JOptionPane.showMessageDialog(frmTecalOrdonnanceur, "Pas de date choisie !", "LOGS CPO",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				CPO_LOGS_GANT panel = new CPO_LOGS_GANT(d);

				// Créer la fenêtre
				JFrame frame = new JFrame("Diagramme de Gantt");
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frame.setSize(800, 600);
				frame.add(panel);
				frame.setVisible(true);

			}
		});


		btnGanttCpo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (tableOF == null) {
					return;
				}

				int[] sel = tableOF.getSelectedRows();
				if (sel.length == 0) {
					JOptionPane.showMessageDialog(frmTecalOrdonnanceur, "Pas d'OF choisi !", "Visuel CPO",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				LinkedHashMap<Integer, Barre> barres = new LinkedHashMap<>();
				for (int i = 0; i < sel.length; i++) {
					int j =i+1;
					Barre b = new Barre(j,j+"", tableOF.getModel().getValueAt(sel[i], 1).toString(), CST.VITESSE_NORMALE,
							CST.VITESSE_NORMALE, false);

					barres.put(b.getIdbarre(), b);
				}



				SwingUtilities.invokeLater(() -> {
					mCPO_IHM = new CPO_IHM();
					mCPO_IHM.setTitle(frmTecalOrdonnanceur.getTitle());
					mCPO_IHM.setBarresSettingsFutures(barres);
					mCPO_IHM.setVisible(true);
					mCPO_IHM.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				});



			}


		});

	}

	 public static void applyEnterKeyBehavior(JTextField[] textFields, Runnable setParamsAction) {
	        for (JTextField textField : textFields) {
	            textField.addActionListener(new ActionListener() {
	                @Override
	                public void actionPerformed(ActionEvent e) {
	                    setParamsAction.run();
	                }
	            });
	        }
	    }

	private void loadCalibrageData() {
		try {

			tableModelCalibrage.setRowCount(0);

			Statement stmt = SQL_DATA.getInstance().getStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM CalibrageTempsGammes ");

			// Add rows to table model
			while (rs.next()) {
				Object[] rowData = new Object[3];
				for (int i = 1; i <= 3; i++) {
					rowData[i - 1] = rs.getObject(i);
				}
				tableModelCalibrage.addRow(rowData);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error loading data: " + e.getMessage());
		}
	}

	private void saveChanges() {
		try {
			for (int i = 0; i < tableModelCalibrage.getRowCount(); i++) {
				StringBuilder query = new StringBuilder("UPDATE CalibrageTempsGammes SET ");
				StringBuilder whereClause = new StringBuilder(" WHERE ");
				boolean firstColumn = true;
				boolean firstWhere = true;

				for (int j = 0; j < tableModelCalibrage.getColumnCount(); j++) {
					String columnName = tableModelCalibrage.getColumnName(j);
					Object value = tableModelCalibrage.getValueAt(i, j);

					if (firstColumn) {
						firstColumn = false;
					} else {
						query.append(", ");
					}
					query.append(columnName).append(" = '").append(value).append("'");

					// Assuming the first column is the primary key
					if (j == 0) {
						if (firstWhere) {
							firstWhere = false;
						} else {
							whereClause.append(" AND ");
						}
						whereClause.append(columnName).append(" = '").append(value).append("'");
					}
				}

				query.append(whereClause);
				Statement stmt = SQL_DATA.getInstance().getStatement();
				stmt.executeUpdate(query.toString());
			}

			JOptionPane.showMessageDialog(null, "Changes saved successfully.");
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error saving changes: " + e.getMessage());
		}
	}

	private void applyFilter() {
		String text = filterTextField.getText();
		if (text.trim().length() == 0) {
			sorter.setRowFilter(null);
		} else {
			sorter.setRowFilter(RowFilter.regexFilter(".*" + text + ".*", 0));

		}
	}
	// Méthode pour supprimer les lignes sélectionnées
    private void deleteSelectedRows() throws SQLException {
        int[] selectedRows = mTableCalibrage.getSelectedRows();

        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(frmTecalOrdonnanceur, "Veuillez sélectionner au moins une ligne à supprimer.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frmTecalOrdonnanceur, "Voulez-vous vraiment supprimer les lignes sélectionnées ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

		String deleteQuery = "DELETE FROM CalibrageTempsGammes WHERE NumGamme = ? AND NumFicheProduction = ?";
		PreparedStatement pstmt = SQL_DATA.getInstance().getPreparedStatement(deleteQuery);


		// Suppression dans le modèle de table en ordre décroissant pour éviter les
		// décalages d'indices
		for (int row = selectedRows.length - 1; row >= 0; row--) {
			
			int modelRow = mTableCalibrage.convertRowIndexToModel(row); // Index dans le modèle
			
			String numGamme = (String) tableModelCalibrage.getValueAt(modelRow, 0);
			String numFicheProduction = (String) tableModelCalibrage.getValueAt(modelRow, 1);

			// Définir les paramètres pour la suppression
			pstmt.setString(1, numGamme);
			pstmt.setString(2, numFicheProduction);
			pstmt.executeUpdate(); // Exécuter la suppression dans la base de données

			tableModelCalibrage.removeRow(modelRow); // Supprimer la ligne du modèle
		}

    }

	private void buildCalibrageTab() {

		panelCalibrage = new JPanel();
		mParamTabs.addTab("Fiche de calibrages", null, panelCalibrage, null);

		JPanel filterPanel = new JPanel();
		filterTextField = new JTextField();
		JButton filterButton = new JButton("Filtrer");

		filterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyFilter();
			}
		});
		filterPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		filterPanel.add(new JLabel("Filtrer sur la gamme: "));
		filterPanel.add(filterTextField);
		filterPanel.add(filterButton);

		btnRefreshCalib = new JButton("");

		btnRefreshCalib.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				loadCalibrageData();
				tableModelCalibrage.fireTableDataChanged();
				mTableCalibrage.repaint();
			}
		});

		CPO_Panel.setIconButton(this, btnRefreshCalib, "icons8-update-16.png");
		filterPanel.add(btnRefreshCalib);

		tableModelCalibrage = new DefaultTableModel(new Object[] { "NumGamme", "NumFicheProduction", "Date" }, 0) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};
		mTableCalibrage = new JTable(tableModelCalibrage);

		mTableCalibrage.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		sorter = new TableRowSorter<>(tableModelCalibrage);
		mTableCalibrage.setRowSorter(sorter);

		mTableCalibrage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					try {
						deleteSelectedRows();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		loadCalibrageData();

		// Add save button
		JButton saveButton = new JButton("Sauver");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveChanges();
			}
		});
		saveButton.setVisible(false);

		// Layout the components
		panelCalibrage.setLayout(new BorderLayout());
		panelCalibrage.add(new JScrollPane(mTableCalibrage), BorderLayout.CENTER);
		panelCalibrage.add(saveButton, BorderLayout.SOUTH);

		panelCalibrage.add(filterPanel, BorderLayout.NORTH);
	}

	 public static void applyColorChangingBehavior(JTextField textField) {
	        // Sauvegarder la couleur d'origine
	        Color originalColor = textField.getBackground();

	        // Ajout d'un écouteur de focus pour changer la couleur en orange lors de l'édition
	        textField.addFocusListener(new FocusListener() {
	            @Override
	            public void focusGained(FocusEvent e) {
	                textField.setBackground(Color.ORANGE);
	            }

	            @Override
	            public void focusLost(FocusEvent e) {
	                // Revenir à la couleur d'origine si le focus est perdu
	                textField.setBackground(originalColor);
	            }
	        });

	        // Ajout d'un écouteur d'action pour restaurer la couleur sur Entrée
	        textField.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                textField.setBackground(originalColor);
	            }
	        });
	    }


	private void buildParamsTab(JTabbedPane tabbedPane) {

		ImageIcon iconStat = new ImageIcon(this.getClass().getResource("/icons8-parameters-16.png"));
		tabbedPane.addTab("Paramètres",iconStat,mParamTabs);
		JPanel panel_param = new JPanel();
		ZonesPanel panelZones = new ZonesPanel();
		mParamTabs.addTab("Zones", null, panelZones, null);
		mParamTabs.addTab("Constantes CPO", null, panel_param, null);



		JLabel lblTailleZone = new JLabel("TEMPS_ZONE_OVERLAP_MIN");

		textTEMPS_ZONE_OVERLAP_MIN = new JTextField();
		textTEMPS_ZONE_OVERLAP_MIN.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_ZONE_OVERLAP_MIN.setColumns(10);
		textTEMPS_ZONE_OVERLAP_MIN.setText(Integer.toString(CST.TEMPS_ZONE_OVERLAP_MIN));

		applyColorChangingBehavior(textTEMPS_ZONE_OVERLAP_MIN);



		JLabel lblcartGroupes = new JLabel("TEMPS_MVT_PONT_MIN_JOB");

		textTEMPS_MVT_PONT_MIN_JOB = new JTextField();
		textTEMPS_MVT_PONT_MIN_JOB.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_MVT_PONT_MIN_JOB.setColumns(10);
		textTEMPS_MVT_PONT_MIN_JOB.setText(Integer.toString(CST.TEMPS_MVT_PONT_MIN_JOB));
		applyColorChangingBehavior(textTEMPS_MVT_PONT_MIN_JOB);

		JLabel lblNewLabel = new JLabel("TEMPS_MVT_PONT");

		JLabel lblNewLabel_1 = new JLabel("TEMPS_ANO_ENTRE_P1_P2");

		textTEMPS_MVT_PONT = new JTextField();
		textTEMPS_MVT_PONT.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_MVT_PONT.setColumns(10);
		textTEMPS_MVT_PONT.setText(Integer.toString(CST.TEMPS_MVT_PONT));
		applyColorChangingBehavior(textTEMPS_MVT_PONT);

		textTEMPS_ANO_ENTRE_P1_P2 = new JTextField();
		textTEMPS_ANO_ENTRE_P1_P2.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_ANO_ENTRE_P1_P2.setColumns(10);
		textTEMPS_ANO_ENTRE_P1_P2.setText(Integer.toString(CST.TEMPS_ANO_ENTRE_P1_P2));
		applyColorChangingBehavior(textTEMPS_ANO_ENTRE_P1_P2);

		JLabel lblNewLabel_2 = new JLabel("NUMZONE ANODISATION");

		textANODISATION_NUMZONE = new JTextField();
		textANODISATION_NUMZONE.setHorizontalAlignment(SwingConstants.RIGHT);
		textANODISATION_NUMZONE.setColumns(10);
		textANODISATION_NUMZONE.setText(Integer.toString(CST.ANODISATION_NUMZONE));
		applyColorChangingBehavior(textANODISATION_NUMZONE);

		JLabel lblNewLabel_2_1 = new JLabel("TEMPS MAX SOLVER");

		textTEMPS_MAX_SOLVEUR = new JTextField();
		textTEMPS_MAX_SOLVEUR.setText(Integer.toString(CST.TEMPS_MAX_SOLVEUR));
		textTEMPS_MAX_SOLVEUR.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_MAX_SOLVEUR.setColumns(10);
		applyColorChangingBehavior(textTEMPS_MAX_SOLVEUR);

		JLabel lblPostesANo = new JLabel("NB POSTES ANO");

		textNbAno = new JTextField();
		textNbAno.setText("0");
		textNbAno.setHorizontalAlignment(SwingConstants.RIGHT);
		textNbAno.setColumns(10);
		textNbAno.setText(Integer.toString(CST.CAPACITE_ANODISATION));
		applyColorChangingBehavior(textNbAno);
		  // La méthode à exécuter sur Entrée
        Runnable setParams = () -> {

        		TecalOrdoParams param=TecalOrdoParams.getInstance();
        		param.setTEMPS_ZONE_OVERLAP_MIN(Integer.valueOf(textTEMPS_ZONE_OVERLAP_MIN.getText()));
        		param.setTEMPS_MVT_PONT_MIN_JOB(Integer.valueOf(textTEMPS_MVT_PONT_MIN_JOB.getText()));
        		param.setTEMPS_MVT_PONT(Integer.valueOf(textTEMPS_MVT_PONT.getText()));
        		param.setTEMPS_ANO_ENTRE_P1_P2(Integer.valueOf(textTEMPS_ANO_ENTRE_P1_P2.getText()));
        		param.setTEMPS_MAX_SOLVEUR(Integer.valueOf(textTEMPS_MAX_SOLVEUR.getText()));
        		param.setNUMZONE_ANODISATION(Integer.valueOf(textANODISATION_NUMZONE.getText()));
        		param.setCAPACITE_ANODISATION(Integer.valueOf(textNbAno.getText()));

        };

		applyEnterKeyBehavior(new JTextField[]{textNbAno,
				textTEMPS_ZONE_OVERLAP_MIN,
				textTEMPS_MVT_PONT_MIN_JOB,textTEMPS_MVT_PONT,
				textTEMPS_MAX_SOLVEUR, textANODISATION_NUMZONE}, setParams);

        // Ajouter les champs

		GroupLayout gl_panel_param = new GroupLayout(panel_param);
		gl_panel_param.setHorizontalGroup(
			gl_panel_param.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_param.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_param.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_param.createParallelGroup(Alignment.LEADING, false)
							.addComponent(lblTailleZone, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							)
						.addComponent(lblNewLabel)
						.addComponent(lblcartGroupes, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_1)
						.addComponent(lblNewLabel_2)
						.addComponent(lblNewLabel_2_1, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPostesANo, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE))
					.addGap(31)
					.addGroup(gl_panel_param.createParallelGroup(Alignment.LEADING)
						.addComponent(textNbAno, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
						.addComponent(textTEMPS_MAX_SOLVEUR, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel_param.createParallelGroup(Alignment.LEADING, false)
							.addComponent(textANODISATION_NUMZONE, 0, 0, Short.MAX_VALUE)
							.addComponent(textTEMPS_ANO_ENTRE_P1_P2, 0, 0, Short.MAX_VALUE)
							.addComponent(textTEMPS_MVT_PONT_MIN_JOB, 0, 0, Short.MAX_VALUE)
							.addComponent(textTEMPS_MVT_PONT, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)

							.addComponent(textTEMPS_ZONE_OVERLAP_MIN, Alignment.TRAILING, 0, 0, Short.MAX_VALUE)))
					.addContainerGap(461, Short.MAX_VALUE))
		);
		gl_panel_param.setVerticalGroup(
			gl_panel_param.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_param.createSequentialGroup()
					.addGap(25)
					.addGroup(gl_panel_param.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblTailleZone)
						.addComponent(textTEMPS_ZONE_OVERLAP_MIN, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)

					.addGroup(gl_panel_param.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblcartGroupes)
						.addComponent(textTEMPS_MVT_PONT_MIN_JOB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_panel_param.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel)
						.addComponent(textTEMPS_MVT_PONT, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_panel_param.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_1)
						.addComponent(textTEMPS_ANO_ENTRE_P1_P2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_panel_param.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_2)
						.addComponent(textANODISATION_NUMZONE, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_param.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_2_1)
						.addComponent(textTEMPS_MAX_SOLVEUR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(21)
					.addGroup(gl_panel_param.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPostesANo)
						.addComponent(textNbAno, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(333, Short.MAX_VALUE))
		);
		gl_panel_param.linkSize(SwingConstants.VERTICAL, new Component[] {lblTailleZone,  lblcartGroupes, lblNewLabel, lblNewLabel_1});
		gl_panel_param.linkSize(SwingConstants.HORIZONTAL, new Component[] {lblTailleZone,  lblcartGroupes, lblNewLabel, lblNewLabel_1});
		panel_param.setLayout(gl_panel_param);
	}

	private void filterMvts() {
		List<RowFilter<Object, Object>> filters = new ArrayList<>(2);
		filters.add(RowFilter.regexFilter(textStart.getText(), 1));
		filters.add(RowFilter.regexFilter(textEnd.getText(), 3));
		RowFilter<Object, Object> rf = RowFilter.andFilter(filters);

		TableRowSorter<TableModel> sorter = new TableRowSorter<>((tableTpsMvts.getModel()));
		sorter.setRowFilter(rf);

		tableTpsMvts.setRowSorter(sorter);
	}

	private void buildMvtPonts() {
		JPanel panelMvtPonts = new JPanel(new BorderLayout());
		mParamTabs.addTab("Temps mouvements", null, panelMvtPonts, null);

		textStart = new JTextField();
		textStart.setColumns(10);

		textEnd = new JTextField();
		textEnd.setColumns(10);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		buttonsPanel.add(textStart);
		buttonsPanel.add(textEnd);

		JScrollPane scrollMvtsTable = new JScrollPane();

		panelMvtPonts.add(buttonsPanel, BorderLayout.PAGE_START);

		btnRefresh = new JButton("");
		CPO_Panel.setIconButton(this, btnRefresh, "icons8-update-16.png");

		btnRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				Vector<Vector<Object>> data = new Vector<>();
				Vector<String> columnNames;
				DefaultTableModel modelMvts;
				try {
					columnNames = getTpsMvts(data);
					modelMvts = new DefaultTableModel(data, columnNames) {

						/**
						 *
						 */
						private static final long serialVersionUID = 1L;

						@Override
						public boolean isCellEditable(int row, int column) {
							// all cells false
							return false;
						}
					};
					tableTpsMvts.setModel(modelMvts);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

				TableColumnModel tcm = tableTpsMvts.getColumnModel();
				tcm.removeColumn(tcm.getColumn(0));
				tcm.removeColumn(tcm.getColumn(1));
				tcm.removeColumn(tcm.getColumn(2));
				tcm.removeColumn(tcm.getColumn(3));

				tableTpsMvts.repaint();

			}
		});

		buttonsPanel.add(btnRefresh);

		btnEraseButton = new JButton("RAZ");
		btnEraseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				int result = JOptionPane.showConfirmDialog((Component) null,
						"Attention ! Vous allez mettre tous les temps à zéro ! Continuer?", "alerte",
						JOptionPane.YES_NO_OPTION);
				if (result == 0) {
					frmTecalOrdonnanceur.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					if (!SQL_DATA.getInstance().eraseTpsMvts()) {
						JOptionPane.showMessageDialog(null, "Echec de la  RAZ !");
					} else {
						JOptionPane.showMessageDialog(null, "RAZ réussie!");
						// MAJ des gammes
						SQL_DATA.getInstance().setMissingTimeMovesGammes();
					}
					frmTecalOrdonnanceur.setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		buttonsPanel.add(btnEraseButton);

		btnImport = new JButton("Calcul auto.");
		btnImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				int result = JOptionPane.showConfirmDialog((Component) null,
						"Attention ! Vous allez recalculer tous les temps ! Continuer?", "alerte",
						JOptionPane.YES_NO_OPTION);
				if (result == 0) {
					frmTecalOrdonnanceur.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					if (!SQL_DATA.getInstance().resetAllTpsMvts()) {
						JOptionPane.showMessageDialog(null, "Echec de la  MAJ !");
					} else {
						JOptionPane.showMessageDialog(null, "MAJ réussie!");
						// MAJ des gammes
						SQL_DATA.getInstance().setMissingTimeMovesGammes();
					}
					frmTecalOrdonnanceur.setCursor(Cursor.getDefaultCursor());
				}

			}
		});
		buttonsPanel.add(btnImport);
		panelMvtPonts.add(scrollMvtsTable, BorderLayout.CENTER);

		tableTpsMvts = new JTable();

		try {
			buildTableModelMvts();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		// scrollMvtsTable.add(tableTpsMvts);
		scrollMvtsTable.setViewportView(tableTpsMvts);

		textStart.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				filterMvts();
			}
		});

		textEnd.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				filterMvts();
			}

		});

	}

	public void setIconButton(JButton btnButton, String fileName) {
		try {

			InputStream in = getClass().getResourceAsStream("/" + fileName);
			BufferedImage someImage = ImageIO.read(in);
			ImageIcon img = new ImageIcon(someImage);
			btnButton.setIcon(img);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public void buildTableModelMvts() throws SQLException {

		Vector<Vector<Object>> data = new Vector<>();
		Vector<String> columnNames = getTpsMvts(data);

		DefaultTableModel modelMvts = new DefaultTableModel(data, columnNames) {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};

		tableTpsMvts.setModel(modelMvts);
		TableColumnModel tcm = tableTpsMvts.getColumnModel();
		tcm.removeColumn(tcm.getColumn(0));
		tcm.removeColumn(tcm.getColumn(1));
		tcm.removeColumn(tcm.getColumn(2));
		tcm.removeColumn(tcm.getColumn(3));

		tableTpsMvts.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

	}

	private Vector<String> getTpsMvts(Vector<Vector<Object>> data) throws SQLException {
		ResultSet rs = sqlCnx.getTpsMvts();
		ResultSetMetaData metaData = rs.getMetaData();

		// names of columns
		Vector<String> columnNames = new Vector<>();
		int columnCount = metaData.getColumnCount();
		for (int column = 1; column <= columnCount; column++) {
			columnNames.add(metaData.getColumnName(column));
		}

		// data of the table

		while (rs.next()) {
			Vector<Object> vector = new Vector<>();
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				vector.add(rs.getObject(columnIndex));
			}
			data.add(vector);
		}
		return columnNames;
	}

	public void buildTableModelVisuProd() throws SQLException {

		java.util.Date d = (java.util.Date) datePicker.getModel().getValue();
		frmTecalOrdonnanceur.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));		
		ResultSet rs = sqlCnx.getVisuProd(d);
		frmTecalOrdonnanceur.setCursor(Cursor.getDefaultCursor());
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

		modelVisuProd = new DefaultTableModel(data, columnNames) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};

		tableOF = new JTable(modelVisuProd) {

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
		
		TableColumnModel columnModel = tableOF.getColumnModel();
        TableColumn column = columnModel.getColumn(5);
        columnModel.removeColumn(column);
		// scrollPaneVisuProd.setViewportView(tableOF);
		// panelVisuProd.setLayout(gl_panelVisuProd);
		tableOF.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		scrollPaneVisuProd.setViewportView(tableOF);

		tableOF.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				String gamme = (String) table.getModel().getValueAt(row, 1);
				boolean bad_calib=(boolean) table.getModel().getValueAt(row, 5);
				if (SQL_DATA.getInstance().getMissingTimeMovesGammes().contains(gamme)) {
					setBackground(Color.RED);
					setForeground(Color.BLACK);
				} 
				else if(bad_calib) {
					setBackground(Color.yellow);
					setForeground(Color.BLACK);
					
				}else {
					setBackground(table.getBackground());
					setForeground(table.getForeground());
				}

				return this;
			}

		});





		tableOF.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 2) { // Vérifie si c'est un double-clic
		            int row = tableOF.rowAtPoint(e.getPoint());
		            if (row != -1) { // Vérifie que la ligne cliquée est valide
		                tableOF.setRowSelectionInterval(row, row); // Sélectionne la ligne
		                
		                // Appelle la méthode pour ouvrir le JDialog
		                openCalibrageDialog(frmTecalOrdonnanceur, tableOF, row);
		            }
		        }
		    }
		});









	}
	 /**
     * Affiche une fenêtre modale avec deux JComboBox et un bouton "Valider".
     */
    private  void openCalibrageDialog(JFrame parent, JTable table, int selectedRow) {
    	String of = tableOF.getModel().getValueAt(selectedRow, 0).toString();
		String gamme = tableOF.getModel().getValueAt(selectedRow, 1).toString();
		FicheProductionDialog dialog = new FicheProductionDialog(parent, of,gamme);

        dialog.setVisible(true);
    }



}
