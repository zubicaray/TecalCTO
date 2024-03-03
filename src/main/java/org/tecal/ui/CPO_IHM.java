package org.tecal.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;
import org.tecal.scheduler.GanttChart;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.DefaultRowSorter;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;


public class CPO_IHM extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTabbedPane tabbedPaneGantt;
	private JTabbedPane tabbedPane ;
	private JPanel panel_chart;
	private GroupLayout gl_panelGantt ; 
	JPanel panelGantt ;
	private DefaultTableModel modelDerives;
	private JTable tableDerives;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CPO_IHM frame = new CPO_IHM(null);
					//frame.addGantt(null) ;
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CPO_IHM(List<Image> icons) {
		
		setIconImages(icons); 
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 985, 650);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	
		
		panelGantt = new JPanel();
		tabbedPane.addTab("Gantt", null, panelGantt, null);
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
		
		
		GroupLayout gl_panelDerives = new GroupLayout(panelDerives);
		gl_panelDerives.setHorizontalGroup(
			gl_panelDerives.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panelDerives.createSequentialGroup()
					.addGap(58)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 319, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(68, Short.MAX_VALUE))
		);
		gl_panelDerives.setVerticalGroup(
			gl_panelDerives.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelDerives.createSequentialGroup()
					.addGap(34)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
					.addGap(40))
		);
		
		tableDerives = new JTable(modelDerives);
		
		
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableDerives.getModel());
		tableDerives.setRowSorter(sorter);

		List<DefaultRowSorter.SortKey> sortKeys = new ArrayList<>(2);
		sortKeys.add(new DefaultRowSorter.SortKey(0, SortOrder.ASCENDING));
		sortKeys.add(new DefaultRowSorter.SortKey(1, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		
		
		scrollPane.setViewportView(tableDerives);
		panelDerives.setLayout(gl_panelDerives);
		
		gl_panelGantt = new GroupLayout(panelGantt);
		UIManager.put( "Panel.foreground", new Color(255,255,255) );
		
		
		
	}
	
	public   DefaultTableModel getDerives()
	{
		
		return modelDerives;
		
		
		
	}
	
	public  void  addGantt(ChartPanel  cp,GanttChart ganttTecalOR ) {
		
		cp.addChartMouseListener((ChartMouseListener) new ChartMouseListener(){
		    public void chartMouseClicked(ChartMouseEvent event){
		    	
		    	 int mouseX = event.getTrigger().getX();
		         int mouseY = event.getTrigger().getY();
		         //System.out.println("x = " + mouseX + ", y = " + mouseY);
		         Point2D p =cp.translateScreenToJava2D(
		                 new Point(mouseX, mouseY));
		         XYPlot plot = (XYPlot) cp.getChart().getPlot();
		         org.jfree.chart.ChartRenderingInfo info = cp.getChartRenderingInfo();
		         Rectangle2D dataArea = info.getPlotInfo().getDataArea();

		         ValueAxis domainAxis = plot.getDomainAxis();
		         RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
		         ValueAxis rangeAxis = plot.getRangeAxis();
		         RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
		         double chartX = domainAxis.java2DToValue(p.getX(), dataArea,
		                 domainAxisEdge);
		         double chartY = rangeAxis.java2DToValue(p.getY(), dataArea,
		                 rangeAxisEdge);
		    	
		    	//ganttTecalOR.setTime(chartX*10.0);
		    }

			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				// TODO Auto-generated method stub
				
			}
		  
		});
		
		JButton btnStartButton = new JButton("Start");
		
		btnStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ganttTecalOR.setTime((double) 2222);
				btnStartButton.setEnabled(false);
			}
		});
		JButton btnForeButton = new JButton("avancer");
		
		btnForeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ganttTecalOR.foreward(2);
			}
		});
		
		JButton btnBackButton = new JButton("reculer");
		
		btnBackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ganttTecalOR.backward(2);
			}
		});
	
		
		gl_panelGantt = new GroupLayout(panelGantt);
		gl_panelGantt.setHorizontalGroup(
				gl_panelGantt.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panelGantt.createSequentialGroup()
					
					.addComponent(cp, GroupLayout.PREFERRED_SIZE, 919, Short.MAX_VALUE)
					.addGap(58))
					//.addContainerGap(68, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, gl_panelGantt.createSequentialGroup()
					.addContainerGap(212, Short.MAX_VALUE)
					.addComponent(btnBackButton)
					.addGap(45)
					.addComponent(btnForeButton)
					.addGap(45)
					.addComponent(btnStartButton)
					.addContainerGap())
		);

		
		gl_panelGantt.setVerticalGroup(
				gl_panelGantt.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelGantt.createSequentialGroup()
					.addGap(34)
					.addComponent(cp, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelGantt.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnBackButton)
						.addComponent(btnForeButton)
						.addComponent(btnStartButton))
					.addContainerGap())
		);
		
	
		
		
		cp.setForeground(new Color(255,255,255));
		
	
		
			
			
			
		
		panelGantt.setLayout(gl_panelGantt);
		contentPane.add(tabbedPane);			;
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
}
