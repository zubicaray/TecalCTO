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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultRowSorter;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.tecal.scheduler.CST;
import org.tecal.scheduler.GanttChart;
import org.tecal.scheduler.TecalOrdo;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.AssignedTask;
import org.tecal.scheduler.types.Barre;


public class CPO_IHM extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTabbedPane tabbedPaneGantt;
	private JTabbedPane tabbedPane ;
	private JPanel panel_chart;
	private JPanel panelGantt ;
	private DefaultTableModel modelDerives;
	private JTable tableDerives;
	private JPanel panel;
	private GanttChart mGanttTecalOR;
	private timerGantt mTimer;
	private TecalOrdo mTecalOrdo;


	private LinkedHashMap<Integer,Barre> mBarresSettingsFutures;

	public LinkedHashMap<Integer,Barre> getBarres() {
		return mBarresSettingsFutures;
	}
	/*
	public void setBarres(LinkedHashMap<Integer,Barre> inBarresSettingsFutures) {
		this.mBarresSettingsFutures = inBarresSettingsFutures;
	}
	*/
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


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {



					CPO_IHM frame = new CPO_IHM();

					frame.setTitle("Tecal Ordonnanceur");
					if(System.getenv("TEST_CPO") != null && System.getenv("TEST_CPO").equals("1")) {
						frame.runTest();
					}
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void runTest () {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		mBarresSettingsFutures=mTecalOrdo.runTest();
		mCPO_PANEL.setModelBarres(mBarresSettingsFutures);
		execute();
		setCursor(Cursor.getDefaultCursor());
	}

	public void run (LinkedHashMap<Integer,Barre> barres) {

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		mBarresSettingsFutures=barres;

		// on garde les jobs en cours des générations précédentes
		manageOngoingJobs();
		mCPO_PANEL.setModelBarres(mBarresSettingsFutures);
		if(mBarresSettingsFutures.size()>0) {
			mTecalOrdo.run(mBarresSettingsFutures,(long)mGanttTecalOR.getTimeBar().getValue());
		}
		else {
			mTecalOrdo.setHasSolution(true);
		}


		execute();

		if(mTecalOrdo.hasSolution()) {
			tabbedPane.setSelectedIndex(1);
			//mCPO_PANEL.getModelBarres().setRowCount(0);
		}

		setCursor(Cursor.getDefaultCursor());

	}


	public class timerGantt extends TimerTask {
	    @Override
	    public void run() {
	        //System.out.println("Email sent at: " + LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledExecutionTime()), ZoneId.systemDefault()));

	    	mGanttTecalOR.getTimeBar().setValue(mGanttTecalOR.getTimeBar().getValue()+1);

	    	//TODO
	    	// mutualiser avec fct manageOngoingJobs
	    	double current_time=mGanttTecalOR.getTimeBar().getValue();
	    	for( Entry<Integer, List<AssignedTask>> entry  :mTecalOrdo.getAssignedTasksByBarreId().entrySet()) {

				List<AssignedTask> values=entry.getValue();
				AssignedTask first=values.get(0);
				int barreid=entry.getKey();
				if( first.start<current_time) {
					// on ne garde pas les taches du job car il n'a pas encore commencé
					mCPO_PANEL.removeBarre(barreid);
				}
				if( first.end == current_time+60   ) 	 {
					CountdownWindow countdownModal = new CountdownWindow(barreid);
				     countdownModal.startCountdown();
				}

			}

	    }
	}


	private void manageOngoingJobs() {


		if(mTecalOrdo.getAssignedTasksByBarreId().size() ==0){
			return;
		}

		ArrayList<Integer> barresCommencantes	=new ArrayList<>();

		double current_time=mGanttTecalOR.getTimeBar().getValue();
		for( Entry<Integer, List<AssignedTask>> entry  :mTecalOrdo.getAssignedTasksByBarreId().entrySet()) {

			List<AssignedTask> values=entry.getValue();
			AssignedTask first=values.get(0);
			AssignedTask last=values.get(values.size()-1);
			int barreid=entry.getKey();
			if(first.end<current_time && last.start>current_time) {
				//job commencé et non fini
				barresCommencantes.add(barreid);
			}

			if(last.start<current_time) {
				// job fini
				mTecalOrdo.removeBarreFinie(barreid);
			}
		}

		mTecalOrdo.setBarresEnCours(barresCommencantes);

		for(Integer barreId:barresCommencantes) {
			mBarresSettingsFutures.remove(barreId);
		}
	}



	private void execute() {

		mGanttTecalOR.model_diag(mTecalOrdo);
		mCPO_PANEL.setText(mTecalOrdo.getOutputMsg().toString());
		setDerives();

	}


	public void startTime() {

		if(mTecalOrdo.hasSolution()) {

			if(mTimer == null) {
				this.mTimer=new timerGantt();
				mGanttTecalOR.setStartTime();
				new Timer().scheduleAtFixedRate(mTimer, 0, 1000);

			}
		}

	}


	private void setDerives() {
		DefaultTableModel modelDerives =getDerives();

		modelDerives.setRowCount(0);

		for(List<AssignedTask> lat : mTecalOrdo.getAssignedJobs().values()) {
			for(AssignedTask at :lat) {
				if(at.derive>at.end) {
					if(!mTecalOrdo.getBarresEnCours().contains( at.barreID)) {
						long t=at.derive-at.start;
						String minutes=String.format("%02d:%02d",  t / 60, (t % 60));
						Object[] rowO = {
								mTecalOrdo.getAllJobs().get(at.barreID).getName(),
								SQL_DATA.getInstance().getZones().get(at.numzone).codezone,
								minutes };
						modelDerives.addRow(rowO);
					}

				}
			}

		}
	}


	public CPO_IHM() {
		mTecalOrdo=new TecalOrdo(CST.SQLSERVER);
		int[] params= {
				CST.TEMPS_ZONE_OVERLAP_MIN,
				CST.TEMPS_MVT_PONT_MIN_JOB,
				CST.GAP_ZONE_NOOVERLAP,
				CST.TEMPS_MVT_PONT,
				CST.TEMPS_ANO_ENTRE_P1_P2,
				CST.TEMPS_MAX_SOLVEUR,
				CST.ANODISATION_NUMZONE,
				CST.CAPACITE_ANODISATION
		};


		mTecalOrdo.setParams(params);
		init();
	}

	/**
	 * Create the frame based on production
	 */
	public CPO_IHM(int[] params) {

		mTecalOrdo=new TecalOrdo(CST.SQLSERVER);
		mTecalOrdo.setParams(params);
		init();

	}

	private void init() {

		TecalGUI.cosmeticGUI();
		mGanttTecalOR = new GanttChart("Diagramme Gantt de la production");

		mCPO_PANEL= new CPO_Panel(this);
		setIconImages(TecalGUI.loadIcons(this));
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 985, 650);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		tabbedPane = new JTabbedPane(SwingConstants.TOP);

		tabbedPane.addTab("Gammes", null, mCPO_PANEL, null);

		panelGantt = new JPanel();

		tabbedPane.addTab("Gantt", null, panelGantt, null);

		panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(mGanttTecalOR.getChartPanel(),BorderLayout.CENTER);

		JPanel panelButtons = new JPanel();

		GroupLayout gl_panelGantt = new GroupLayout(panelGantt);
		gl_panelGantt.setHorizontalGroup(
			gl_panelGantt.createParallelGroup(Alignment.TRAILING)
				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE)
				.addGroup(gl_panelGantt.createSequentialGroup()
					.addGap(263)
					.addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, 164, Short.MAX_VALUE)
					.addGap(239))
		);
		gl_panelGantt.setVerticalGroup(
			gl_panelGantt.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelGantt.createSequentialGroup()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnBackButton = new JButton();
		panelButtons.add(btnBackButton);
		CPO_Panel.setIconButton(this,btnBackButton,"icons8-back-16.png");

		btnStartButton = new JButton();
		panelButtons.add(btnStartButton);
		CPO_Panel.setIconButton(this,btnStartButton,"icons8-jouer-16.png");

		btnForeButton = new JButton();
		panelButtons.add(btnForeButton);
		JButton bigFore = new JButton();
		panelButtons.add(bigFore);

		panelGantt.setLayout(gl_panelGantt);
		CPO_Panel.setIconButton(this,btnForeButton,"icons8-fore-16.png");


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
				if(mGanttTecalOR !=null) {
					mGanttTecalOR.foreward(2);
				}
			}
		});



		btnBackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mGanttTecalOR !=null) {
					mGanttTecalOR.backward(2);
				}
			}
		});
		CPO_Panel.setIconButton(this,bigFore,"icons8-fore-16.png");

		bigFore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mGanttTecalOR !=null) {
					mGanttTecalOR.foreward(200);
				}
			}
		});


		JPanel panelDerives = new JPanel();
		tabbedPane.addTab("Dérives", null, panelDerives, null);


	    modelDerives=new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
					"barre", "zone", "dérive"
				}
			);

		JScrollPane scrollPane = new JScrollPane();

		tableDerives = new JTable(modelDerives);
		tableDerives.setSize(new Dimension(32000, 50000));


		TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableDerives.getModel());
		tableDerives.setRowSorter(sorter);

		List<DefaultRowSorter.SortKey> sortKeys = new ArrayList<>(2);
		sortKeys.add(new DefaultRowSorter.SortKey(0, SortOrder.ASCENDING));
		sortKeys.add(new DefaultRowSorter.SortKey(1, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		panelDerives.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));


		scrollPane.setViewportView(tableDerives);
		panelDerives.add(scrollPane);

		contentPane.add(tabbedPane);

		//createPanelCPO(panelCPO);
		UIManager.put( "Panel.foreground", new Color(255,255,255) );

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {

				ResultSet rs= SQL_DATA.getInstance().getEnteteGammes();
				try {
					mCPO_PANEL.setRessource(rs);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
	}




	public   DefaultTableModel getDerives()	{
		return modelDerives;
	}



	public JTabbedPane getTabbedPaneGantt() {
		return tabbedPaneGantt;
	}

	public void setTabbedPaneGantt(JTabbedPane tabbedPaneGantt) {
		this.tabbedPaneGantt = tabbedPaneGantt;
	}

	public JPanel getPanel_chart() {
		return panel_chart;
	}

	public void setPanel_chart(JPanel panel_chart) {
		this.panel_chart = panel_chart;
	}
	public void setTimer(timerGantt mTimer) {
		this.mTimer = mTimer;
	}

}
