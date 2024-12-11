package org.tecal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.DefaultRowSorter;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tecal.scheduler.CST;
import org.tecal.scheduler.GanttChart;
import org.tecal.scheduler.TecalOrdo;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.AssignedTask;
import org.tecal.scheduler.types.Barre;

public class CPO_IHM extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel mMainPane;

	private JTabbedPane mTabbedPane;

	private JPanel mPanelGantt;
	private GanttChart mGanttTecalOR;

	private JPanel mPanelDerives;
	private DefaultTableModel mModelDerives;
	private JTable mTableDerives;

	private timerGantt mTimer;
	private TecalOrdo mTecalOrdo;
	private static final Logger logger = LogManager.getLogger(CPO_IHM.class);

	public class MyExceptionHandler {
		public void handle(Throwable throwable) {
			// Gérer les exceptions de Swing ici
			JOptionPane.showMessageDialog(null, "Erreur Swing non interceptée : " + throwable.getMessage(), "Erreur",
					JOptionPane.ERROR_MESSAGE);

			// Log de l'exception
			Logger logger = LogManager.getLogger(MyExceptionHandler.class);
			logger.error("Exception dans l'EDT", throwable);
		}
	}

	private LinkedHashMap<Integer, Barre> mBarresSettingsFutures;

	public LinkedHashMap<Integer, Barre> getmBarresSettingsFutures() {
		return mBarresSettingsFutures;
	}

	public void setBarresSettingsFutures(LinkedHashMap<Integer, Barre> mBarresSettingsFutures) {
		this.mBarresSettingsFutures = mBarresSettingsFutures;
	}

	public LinkedHashMap<Integer, Barre> getBarres() {
		return mBarresSettingsFutures;
	}

	public TecalOrdo getTecalOrdo() {
		return mTecalOrdo;
	}

	public void setTecalOrdo(TecalOrdo mTecalOrdo) {
		this.mTecalOrdo = mTecalOrdo;
	}

	private CPO_Panel mCPO_PANEL;
	private JButton btnForeButton;
	private JButton btnBackButton;
	private JButton btnStartButton;

	public CPO_Panel getCpoPanel() {
		return mCPO_PANEL;
	}

	private static String getManifestVersion() {
		try {
			// Lire le MANIFEST.MF depuis le classpath
			InputStream manifestStream = CPO_Panel.class.getResourceAsStream("/META-INF/MANIFEST.MF");
			if (manifestStream != null) {
				Manifest manifest = new Manifest(manifestStream);
				Attributes attributes = manifest.getMainAttributes();
				return attributes.getValue("Implementation-Version");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Inconnue";
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {

					String version = getManifestVersion();
					Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
						// Gérer les exceptions non interceptées ici

						JOptionPane.showMessageDialog(null, "Une erreur est survenue : " + throwable.getMessage(),
								"Erreur", JOptionPane.ERROR_MESSAGE);

						logger.error("Exception non interceptée dans le thread : " + thread.getName(), throwable);
					});

					SwingUtilities.invokeLater(() -> {
						// Définit un gestionnaire global pour les exceptions dans Swing
						System.setProperty("sun.awt.exception.handler", "org.tecal.ui.MyExceptionHandler");
					});

					CPO_IHM frame = new CPO_IHM();

					// Récupérer le nom de l'hôte
					String hostname = InetAddress.getLocalHost().getHostName();
					frame.setTitle("Tecal CPO - " + version);
					if (hostname.equals("zubi-Latitude-5300")) {
						// frame.runTest();
					}

					frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							int response = JOptionPane.showConfirmDialog(frame, "Voulez-vous vraiment quitter ?",
									"Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

							if (response == JOptionPane.YES_OPTION) {
								frame.dispose(); // Ferme la fenêtre
							} else {
								frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Empêche la
																										// fermeture
							}
						}
					});

					if (System.getenv("TEST_CPO") != null && System.getenv("TEST_CPO").equals("1")) {
						frame.runTest();
					}
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void runTest() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		mBarresSettingsFutures = mTecalOrdo.runTest();
		mCPO_PANEL.setModelBarres(mBarresSettingsFutures);
		execute();
		setCursor(Cursor.getDefaultCursor());
	}

	public void run() {

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		ModalProgressBar progressBar = new ModalProgressBar(mTecalOrdo.getTpsMaxSolver());

		SwingUtilities.invokeLater(() -> {
			progressBar.createAndShowDialog();

		});
		mCPO_PANEL.set_enable(false);

		mCPO_PANEL.setModelBarres(mBarresSettingsFutures);
		if (mBarresSettingsFutures.size() > 0) {
			try {
				mTecalOrdo.execute(mBarresSettingsFutures, (long) mGanttTecalOR.getTimeBar().getValue());

			} catch (Exception e) {
				String msg = "Erreur du moteur Google OR: " + e.getMessage();
				logger.error(msg);
				JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			}

		} else {
			mTecalOrdo.setHasSolution(true);
		}

		try {
			execute();
		} catch (Exception e) {
			String msg = "Erreur de contruction du diagramme de Gantt : " + e.getMessage();
			logger.error(msg);
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		}

		if (mTecalOrdo.hasSolution()) {
			mTabbedPane.setSelectedIndex(1);
			// mCPO_PANEL.getModelBarres().setRowCount(0);
		}
		mCPO_PANEL.set_enable(true);

		progressBar.stop();

		setCursor(Cursor.getDefaultCursor());

	}

	public class timerGantt extends TimerTask {
		@Override
		public void run() {
			try {
				// Votre code existant
				mGanttTecalOR.getTimeBar().setValue(mGanttTecalOR.getTimeBar().getValue() + 1);
				manageOngoingJobs();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Erreur dans la tâche périodique : " + e.getMessage(), "Erreur",
						JOptionPane.ERROR_MESSAGE);

				logger.error("Erreur dans la tâche périodique", e);
			}
		}

	}

	private void logAndRemoveBarreTasks(List<Integer> barreToremove) {
		for (Integer i : barreToremove) {

			List<AssignedTask> listTask = mTecalOrdo.getAssignedTasksByBarreId().get(i);

			LocalDateTime d = LocalDateTime.now();
			LocalDateTime start = mGanttTecalOR.getStartTime();
			int cptZone = 1;

			for (AssignedTask a : listTask) {

				LocalDateTime deb = start.plusSeconds(a.start - CST.CPT_GANTT_OFFSET);
				LocalDateTime fin = start.plusSeconds(a.end - CST.CPT_GANTT_OFFSET);

				SQL_DATA.getInstance().insertLogCPO(d, a.barreID, mTecalOrdo.getBarreLabels().get(i), cptZone,
						a.numzone, deb, fin);
				cptZone++;
			}

			// on loggera les temps de chaque zone avant
			mTecalOrdo.removeAssignedTaskByBarreId(i);
		}
	}

	private void manageOngoingJobs() {

		if (mTecalOrdo.isWorking()) {
			return;
		}

		ArrayList<Integer> barresCommencantes = new ArrayList<>();

		double current_time = mGanttTecalOR.getTimeBar().getValue();
		for (Entry<Integer, List<AssignedTask>> entry : mTecalOrdo.getAssignedTasksByBarreId().entrySet()) {

			List<AssignedTask> values = entry.getValue();
			AssignedTask first = values.get(0);
			// AssignedTask last=values.get(values.size()-1);
			int barreid = entry.getKey();
			if (first.end < current_time) { // && last.start>current_time) {
				// job commencé et non fini
				barresCommencantes.add(barreid);
				mTecalOrdo.addFixedJobsEnCours(barreid);
				logger.info("barreid:" + barreid + " en cours ");
			

			}

			if (first.end == current_time + 60) {
				CountdownWindow countdownModal = new CountdownWindow(mTecalOrdo.getBarreLabels().get(barreid));
				countdownModal.startCountdown();
			}

		}

		logAndRemoveBarreTasks(barresCommencantes);

		for (Integer barreId : barresCommencantes) {
			mBarresSettingsFutures.remove(barreId);
			SwingUtilities.invokeLater(() -> {
				mCPO_PANEL.removeBarre(barreId);
			});

		}
	}

	private void execute() {

		mGanttTecalOR.model_diag(mTecalOrdo);
		mCPO_PANEL.setText(mTecalOrdo.getOutputMsg().toString());
		setDerives();

	}

	public void startTime() {

		if (mTecalOrdo.hasSolution()) {

			if (mTimer == null) {
				this.mTimer = new timerGantt();
				mGanttTecalOR.setStartTime();
				new Timer().scheduleAtFixedRate(mTimer, 0, 1000);

			}
		}

	}

	private void setDerives() {
		DefaultTableModel mModelDerives = getDerives();

		mModelDerives.setRowCount(0);

		for (List<AssignedTask> lat : mTecalOrdo.getAssignedJobs().values()) {
			for (AssignedTask at : lat) {
				if (at.derive > at.end) {
					if (!mTecalOrdo.getBarresEnCours().contains(at.barreID)) {
						long t = at.derive - at.start;
						String minutes = String.format("%02d:%02d", t / 60, (t % 60));
						Object[] rowO = { mTecalOrdo.getAllJobs().get(at.barreID).getName(),
								SQL_DATA.getInstance().getZones().get(at.numzone).codezone, minutes };
						mModelDerives.addRow(rowO);
					}

				}
			}

		}
	}

	public CPO_IHM() {
		mTecalOrdo = new TecalOrdo(CST.SQLSERVER);
		int[] params = { CST.TEMPS_ZONE_OVERLAP_MIN, CST.TEMPS_MVT_PONT_MIN_JOB, CST.GAP_ZONE_NOOVERLAP,
				CST.TEMPS_MVT_PONT, CST.TEMPS_ANO_ENTRE_P1_P2, CST.TEMPS_MAX_SOLVEUR, CST.ANODISATION_NUMZONE,
				CST.CAPACITE_ANODISATION };

		mTecalOrdo.setParams(params);
		init();
	}

	/**
	 * Create the frame based on production
	 */
	public CPO_IHM(int[] params) {

		mTecalOrdo = new TecalOrdo(CST.SQLSERVER);
		mTecalOrdo.setParams(params);
		init();

	}

	private void init() {

		TecalGUI.cosmeticGUI();
		mGanttTecalOR = new GanttChart("Diagramme Gantt de la production");

		mCPO_PANEL = new CPO_Panel(this);
		setIconImages(TecalGUI.loadIcons(this));
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 1085, 650);
		mMainPane = new JPanel();
		mMainPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(mMainPane);
		mMainPane.setLayout(new BorderLayout(0, 0));

		mTabbedPane = new JTabbedPane(SwingConstants.TOP);

		mTabbedPane.addTab("Gammes", null, mCPO_PANEL, null);

		mPanelGantt = new JPanel();

		mTabbedPane.addTab("Gantt", null, mPanelGantt, null);

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(mGanttTecalOR.getChartPanel(), BorderLayout.CENTER);

		JPanel panelButtons = new JPanel();

		GroupLayout gl_panelGantt = new GroupLayout(mPanelGantt);
		gl_panelGantt.setHorizontalGroup(gl_panelGantt.createParallelGroup(Alignment.TRAILING)
				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE)
				.addGroup(gl_panelGantt.createSequentialGroup().addGap(263)
						.addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, 164, Short.MAX_VALUE).addGap(239)));
		gl_panelGantt
				.setVerticalGroup(
						gl_panelGantt.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelGantt.createSequentialGroup()
										.addComponent(panel, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(panelButtons,
												GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));
		panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnBackButton = new JButton();
		panelButtons.add(btnBackButton);
		CPO_Panel.setIconButton(this, btnBackButton, "icons8-back-16.png");

		btnStartButton = new JButton();
		panelButtons.add(btnStartButton);
		CPO_Panel.setIconButton(this, btnStartButton, "icons8-jouer-16.png");

		btnForeButton = new JButton();
		panelButtons.add(btnForeButton);
		JButton bigFore = new JButton();
		panelButtons.add(bigFore);

		mPanelGantt.setLayout(gl_panelGantt);
		CPO_Panel.setIconButton(this, btnForeButton, "icons8-fore-16.png");

		btnStartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startTime();
				btnStartButton.setEnabled(false);
			}
		});

		btnForeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mGanttTecalOR != null) {
					mGanttTecalOR.foreward(2);
				}
			}
		});

		btnBackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mGanttTecalOR != null) {
					mGanttTecalOR.backward(2);
				}
			}
		});
		CPO_Panel.setIconButton(this, bigFore, "icons8-fore-16.png");

		bigFore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mGanttTecalOR != null) {
					mGanttTecalOR.foreward(200);
				}
			}
		});

		mPanelDerives = new JPanel();
		mTabbedPane.addTab("Dérives", null, mPanelDerives, null);

		mModelDerives = new DefaultTableModel(new Object[][] {}, new String[] { "barre", "zone", "dérive" });

		JScrollPane scrollPane = new JScrollPane();

		mTableDerives = new JTable(mModelDerives);
		mTableDerives.setSize(new Dimension(32000, 50000));

		TableRowSorter<TableModel> sorter = new TableRowSorter<>(mTableDerives.getModel());
		mTableDerives.setRowSorter(sorter);

		List<DefaultRowSorter.SortKey> sortKeys = new ArrayList<>(2);
		sortKeys.add(new DefaultRowSorter.SortKey(0, SortOrder.ASCENDING));
		sortKeys.add(new DefaultRowSorter.SortKey(1, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		// mPanelDerives.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		// Définir un BorderLayout pour permettre une extension complète
		mPanelDerives.setLayout(new BorderLayout());

		scrollPane.setViewportView(mTableDerives);
		mPanelDerives.add(scrollPane, BorderLayout.CENTER);

		mMainPane.add(mTabbedPane);

		// createPanelCPO(panelCPO);
		UIManager.put("Panel.foreground", new Color(255, 255, 255));

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {

				ResultSet rs = SQL_DATA.getInstance().getEnteteGammes();
				try {
					mCPO_PANEL.setRessource(rs);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	public DefaultTableModel getDerives() {
		return mModelDerives;
	}

	public void setTimer(timerGantt mTimer) {
		this.mTimer = mTimer;
	}

}
