package org.tecal.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartPanel;

import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;

public class CPO extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTabbedPane tabbedPaneGantt;
	private JTabbedPane tabbedPane ;
	private JPanel panel_chart;
	private GroupLayout gl_panelGantt ; 
	JPanel panelGantt ;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CPO frame = new CPO(null);
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
	public CPO(List<Image> icons) {
		
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
		
		gl_panelGantt = new GroupLayout(panelGantt);
		
		
		
	}
	
	public void  addGantt(ChartPanel  cp) {
		JButton btnNewButton = new JButton("New button");
		
		gl_panelGantt.setHorizontalGroup(
			gl_panelGantt.createParallelGroup(Alignment.TRAILING)
				.addComponent(cp, GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE)
				.addGroup(gl_panelGantt.createSequentialGroup()
					.addContainerGap(830, Short.MAX_VALUE)
					.addComponent(btnNewButton)
					.addGap(35))
		);
		gl_panelGantt.setVerticalGroup(
			gl_panelGantt.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelGantt.createSequentialGroup()
					.addComponent(cp, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
					.addGap(8)
					.addComponent(btnNewButton)
					.addContainerGap())
		);
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
