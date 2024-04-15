package org.tecal.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jfree.ui.RefineryUtilities;
import org.tecal.scheduler.CST;
import org.tecal.scheduler.GanttChart;
import org.tecal.scheduler.TecalOrdo;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.GammeType;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Font;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JCheckBox;

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
	//private JFrame gantFrame;
	private CPO_IHM   gantFrame;
	private JTable tableGammes;
	private DefaultTableModel modelGammes ;
	private DefaultTableModel modelVisuProd ;
	private JTable tableBarres;
	private DefaultTableModel modelBarres;
	private JTextField textFiltre;
	private JTextArea textArea;
	private JRadioButton rdbtnFastModeRadioButton;
	private JComboBox<Integer> comboDifficult;
	private ImageIcon img;
	List<Image> mIcons;
	
	private SQL_DATA sqlCnx ;
	private int mNumBarre=0;
	private TecalOrdo mTecalOrdo;
	private JTextField textTEMPS_ZONE_OVERLAP_MIN;
	private JTextField textGAP_ZONE_NOOVERLAP;
	private JTextField textTEMPS_MVT_PONT_MIN_JOB;
	private JTextField textTEMPS_MVT_PONT;
	private JTextField textTEMPS_ANO_ENTRE_P1_P2;
	private JTextField textNUMZONE_DEBUT_PONT_2;
	private JTabbedPane tabbedPane_1;
	private JTable tableOF;
	private JDatePickerImpl datePicker;
	JPanel panelVisuProd; 
	JScrollPane scrollPaneVisuProd;
	private JScrollPane scrollPaneMsg_1;
	private JLabel lblGammes;
	private JLabel lblHardynessLabel;
	private JScrollPane scrollPane_1;
	private JScrollPane scrollPaneBarres;
	private JButton btnUpButton;
	private JButton btnRun_1;
	private JButton btnDownButton_1;
	private JLabel lblBarreLabel_1;
	private JTextField textTEMPS_MAX_SOLVEUR;

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		FlatDarkLaf.setup();
		
		Color back=new Color(255, 248, 223);
		Color fore=new Color(1,1,1);
		UIManager.put( "Table.background", back );
		UIManager.put( "TextArea.background", back );
		UIManager.put( "TextArea.foreground", fore );
		UIManager.put( "TextField.background", back );
		UIManager.put( "ComboBox.foreground", fore );
		UIManager.put( "ComboBox.background", back );
		UIManager.put( "TextField.foreground", fore );
		UIManager.put( "Table.foreground", fore );
		UIManager.put( "Button.arc", 999 );
		UIManager.put( "Component.arc", 999 );
		UIManager.put( "ProgressBar.arc", 999 );
		UIManager.put( "TextComponent.arc", 999 );
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TecalGUI window = new TecalGUI();
					window.frmTecalOrdonnanceur.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TecalGUI() {
		
		sqlCnx = new SQL_DATA();
		initialize();
		
		
		mTecalOrdo=new TecalOrdo(CST.SQLSERVER);
		
		
	}
	
	public void initTable(ArrayList<GammeType> gammeList, JTable table) {

		  DefaultTableModel model = (DefaultTableModel)table.getModel();
		     for(GammeType gamme : gammeList){
		          model.addRow(new Object[]{gamme.numgamme, gamme.numgamme });
		     }
		     table.setModel(model);
		}


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		
		frmTecalOrdonnanceur = new JFrame();
		frmTecalOrdonnanceur.setTitle("Tecal Ordonnanceur");
		
		mIcons = new ArrayList<Image>();
		try {
			
			InputStream in = getClass().getResourceAsStream("/gantt-chart 16.png") ;					
			BufferedImage someImage = ImageIO.read(in);
			img = new ImageIcon(someImage);
			mIcons.add(img.getImage());
			in = getClass().getResourceAsStream("/gantt-chart 32.png") ;
			someImage = ImageIO.read(in);
			img = new ImageIcon(someImage);			
			mIcons.add(img.getImage());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // usable in Java 9+
	   
		
		
		frmTecalOrdonnanceur.setIconImages(mIcons); 
		
		//gantFrame = new CPO(mIcons);
		
		frmTecalOrdonnanceur.setBounds(100, 100, 729, 661);
		frmTecalOrdonnanceur.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		GroupLayout groupLayout = new GroupLayout(frmTecalOrdonnanceur.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPane_1, GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPane_1, GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
		);
		
		JPanel panelCPO = new JPanel();
		tabbedPane_1.addTab("Choix des gammes", null, panelCPO, null);
		
		scrollPane_1 = new JScrollPane();
		
		scrollPaneBarres = new JScrollPane();
		
		textFiltre = new JTextField();
		textFiltre.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(((DefaultTableModel) tableGammes.getModel())); 
			    sorter.setRowFilter(RowFilter.regexFilter(textFiltre.getText()));

			    tableGammes.setRowSorter(sorter);
			}
		});
		textFiltre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		textFiltre.setColumns(10);
		
		lblBarreLabel_1 = new JLabel("barres:                     vitesses:");
		lblBarreLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 13));
		
		lblGammes = new JLabel("gammes:");
		lblGammes.setFont(new Font("Tahoma", Font.PLAIN, 13));
		
		btnUpButton = new JButton();
		btnUpButton.setHorizontalAlignment(SwingConstants.CENTER);
		btnUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveRowBy(-1);
			}
		});
		btnDownButton_1 = new JButton();
		btnDownButton_1.setHorizontalAlignment(SwingConstants.CENTER);
		btnDownButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveRowBy(1);
			}
		});
		
		setIconButton(btnUpButton,"icons8-up-16.png");
		setIconButton(btnDownButton_1,"icons8-down-16.png");
		
		
	
		
		rdbtnFastModeRadioButton = new JRadioButton("mode approx.");
		
		rdbtnFastModeRadioButton.setSelected(CST.MODE_ECO);
		

		
		Integer [] comboVals= {3,4,5,6,7,8,9,10,11,12,13};
		comboDifficult = new JComboBox<Integer>(comboVals);
		comboDifficult.setSelectedItem(7);
		if(! rdbtnFastModeRadioButton.isSelected()) {
			comboDifficult.setEnabled(false);
		}
		
		rdbtnFastModeRadioButton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				
				comboDifficult.setEnabled(rdbtnFastModeRadioButton.isSelected());
				
			}
		});
		
		lblHardynessLabel = new JLabel("difficulté");
		
		scrollPaneMsg_1 = new JScrollPane();
		
		btnRun_1 = new JButton("GO");
		btnRun_1.setHorizontalAlignment(SwingConstants.LEFT);
		setIconButton(btnRun_1,"icons8-play-16.png");
		
		
		GroupLayout gl_panel = mainGuiGrouping(panelCPO, scrollPane_1, scrollPaneBarres, lblBarreLabel_1, lblGammes,
				btnUpButton, btnDownButton_1, lblHardynessLabel, scrollPaneMsg_1, btnRun_1);
		
		
		btnRun_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				LinkedHashMap<Integer,String> gammes=new LinkedHashMap <Integer,String>();
				if(tableBarres.getRowCount()<2) {
					JOptionPane.showMessageDialog(frmTecalOrdonnanceur, "Minimum deux barres requises !","Tecal CPO", JOptionPane.ERROR_MESSAGE);
				}else {
					
					gammes.clear();
					for (int count = 0; count < tableBarres.getRowCount(); count++){
						gammes.put((int)tableBarres.getValueAt(count, 0),tableBarres.getValueAt(count, 1).toString());
					}
					
					mTecalOrdo.setParams(
							Integer.valueOf(textTEMPS_ZONE_OVERLAP_MIN.getText()),
							Integer.valueOf(textTEMPS_MVT_PONT_MIN_JOB.getText()),
							Integer.valueOf(textGAP_ZONE_NOOVERLAP.getText()),
							Integer.valueOf(textTEMPS_MVT_PONT.getText()),
							Integer.valueOf(textTEMPS_ANO_ENTRE_P1_P2.getText()),
							Integer.valueOf(textTEMPS_MAX_SOLVEUR.getText()));
					
					mTecalOrdo.setBarres(gammes);
					gantFrame=new CPO_IHM(mIcons);
					frmTecalOrdonnanceur.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					mTecalOrdo.run(rdbtnFastModeRadioButton.isSelected(),(int)comboDifficult.getSelectedItem(),gantFrame);	
					frmTecalOrdonnanceur.setCursor(Cursor.getDefaultCursor());
					textArea.setText(mTecalOrdo.print());
					
					
					
				}
				
				
			}
		});
		
		textArea = new JTextArea();
		scrollPaneMsg_1.setViewportView(textArea);
		
		try {
			
			
			buildTableModelBarre();
			tableBarres = new JTable(modelBarres) {

	            private static final long serialVersionUID = 1L;

	            @Override
	            public boolean isCellEditable(int row, int column) {
	              return column == 2 || column == 3;
	            }
	          
			
	        };
	        
	        TableColumn colMontee =tableBarres.getColumnModel().getColumn(2);
	        // créer un ComboBox
	        JComboBox<String> cb = new JComboBox<>();
	        cb.addItem("lente");
	        cb.addItem("normale");
	        cb.addItem("rapide");
	        //définir l'éditeur par défaut
	        colMontee.setCellEditor(new DefaultCellEditor(cb));
	        
	        TableColumn colDescente =tableBarres.getColumnModel().getColumn(3);	
	        //définir l'éditeur par défaut
	        colDescente.setCellEditor(new DefaultCellEditor(cb));
	        
	        
			tableBarres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    	TableColumnModel columnModel = tableBarres.getColumnModel();
	        columnModel.getColumn(0).setPreferredWidth(40);
	        columnModel.getColumn(0).setMaxWidth(40);	          	
	        tableBarres.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	        
	        tableBarres.addMouseListener(new MouseAdapter() {
	    	    public void mousePressed(MouseEvent mouseEvent) {
	    	        JTable table =(JTable) mouseEvent.getSource();
	    	        Point point = mouseEvent.getPoint();
	    	        int row = table.rowAtPoint(point);
	    	        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {	    	        	
	    	        	modelBarres.removeRow(row);	         	
	    	        }
	    	    }
	    	});
	        
	        
	        
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scrollPaneBarres.setViewportView(tableBarres);
		
		
		ResultSet rs =sqlCnx.getEnteteGammes();
		

	    // It creates and displays the table
	    try {
	    	buildTableModelGamme(rs);
	    	tableGammes = new JTable(modelGammes);
	    	
	    	
	    	
	    	
	    	tableGammes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    	TableColumnModel columnModel = tableGammes.getColumnModel();
	        columnModel.getColumn(0).setPreferredWidth(70);
	        columnModel.getColumn(0).setMaxWidth(400);
	          	
	    	tableGammes.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	    	
	    	tableGammes.addMouseListener(new MouseAdapter() {
	    	    public void mousePressed(MouseEvent mouseEvent) {
	    	        JTable table =(JTable) mouseEvent.getSource();
	    	        Point point = mouseEvent.getPoint();
	    	        int row = table.rowAtPoint(point);
	    	        row=table.convertRowIndexToModel(row); 	  
	    	        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
	    	        	String gamme=table.getModel().getValueAt(row, 0).toString();
	    	        	mNumBarre++;
	    	        	Object[] rowO = { mNumBarre, gamme,"normale","normale" };
	    	        	modelBarres.addRow(rowO);
	    	        	
	    	        }
	    	    }
	    	});
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		scrollPane_1.setViewportView(tableGammes);
		panelCPO.setLayout(gl_panel);
		buildVisuProd();
		buildParamsTab(tabbedPane_1);
		
		frmTecalOrdonnanceur.getContentPane().setLayout(groupLayout);
	}

	private GroupLayout mainGuiGrouping(JPanel panel, JScrollPane scrollPane, JScrollPane scrollPaneBarres,
			JLabel lblBarreLabel, JLabel lblGammes, JButton btnUpButton, JButton btnDownButton,
			JLabel lblHardynessLabel, JScrollPane scrollPaneMsg, JButton btnRun) {
		
		JCheckBox chckbxPrio = new JCheckBox(" prioriser");
		chckbxPrio.setFont(new Font("Tahoma", Font.PLAIN, 13));
		GroupLayout gl_panelCPO = new GroupLayout(panel);
		gl_panelCPO.setHorizontalGroup(
			gl_panelCPO.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelCPO.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPaneMsg_1, GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
						.addGroup(gl_panelCPO.createSequentialGroup()
							.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelCPO.createSequentialGroup()
									.addComponent(lblGammes, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
									.addGap(111)
									.addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panelCPO.createSequentialGroup()
									.addComponent(rdbtnFastModeRadioButton)
									.addGap(34)
									.addComponent(lblHardynessLabel, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(comboDifficult, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
								.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE))
							.addGap(18)
							.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblBarreLabel_1, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 204, GroupLayout.PREFERRED_SIZE)
								.addComponent(scrollPaneBarres, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_panelCPO.createParallelGroup(Alignment.TRAILING)
								.addComponent(btnRun_1, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
								.addComponent(chckbxPrio, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panelCPO.createSequentialGroup()
									.addGroup(gl_panelCPO.createParallelGroup(Alignment.TRAILING)
										.addComponent(btnDownButton_1, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
										.addComponent(btnUpButton, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
									.addGap(50)))))
					.addContainerGap())
		);
		gl_panelCPO.setVerticalGroup(
			gl_panelCPO.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelCPO.createSequentialGroup()
					.addGap(50)
					.addGroup(gl_panelCPO.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblGammes, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblBarreLabel_1))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelCPO.createParallelGroup(Alignment.BASELINE)
							.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
							.addComponent(scrollPaneBarres, GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))
						.addGroup(gl_panelCPO.createSequentialGroup()
							.addGap(35)
							.addComponent(btnUpButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnDownButton_1)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(chckbxPrio)
							.addPreferredGap(ComponentPlacement.RELATED, 144, Short.MAX_VALUE)
							.addComponent(btnRun_1)))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelCPO.createParallelGroup(Alignment.BASELINE)
						.addComponent(rdbtnFastModeRadioButton)
						.addComponent(lblHardynessLabel)
						.addComponent(comboDifficult, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addComponent(scrollPaneMsg_1, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
					.addGap(24))
		);
		return gl_panelCPO;
	}
	
	private void  buildVisuProd() {
		
		panelVisuProd = new JPanel();
		UtilDateModel model = new UtilDateModel();
		//model.setDate(20,04,2014);
		// Need this...
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		// Don't know about the formatter, but there it is...
		datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		
		datePicker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					buildTableModelVisuProd();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		tabbedPane_1.addTab("Visuel de prod", null, panelVisuProd, null);
		
		scrollPaneVisuProd = new JScrollPane();

		
		
		JButton btnRunVisuProd = new JButton("Gantt PROD");
		btnRunVisuProd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				 int[] sel = tableOF.getSelectedRows();
				 if(sel.length==0) {			
					 JOptionPane.showMessageDialog(frmTecalOrdonnanceur, "Pas d'OF choisi !","Visuel de prod", JOptionPane.ERROR_MESSAGE);
					 return;
				 }
				
				 String lOF[]=new String[sel.length];
				 for(int i=0;i<sel.length;i++) {
					 lOF[i]=tableOF.getModel().getValueAt(sel[i], 0).toString();
				 }
				 
				  
				 frmTecalOrdonnanceur.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				 Date d=(Date)datePicker.getModel().getValue();
				 final GanttChart ganttTecal = new GanttChart(sqlCnx,"Prod du "+d);
				 ganttTecal.prod_diag(lOF,d);
				 ganttTecal.pack();
				 ganttTecal.setSize(new java.awt.Dimension(1500, 870));
			     RefineryUtilities.centerFrameOnScreen(ganttTecal);
			     ganttTecal.setVisible(true);
			     ganttTecal.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			     frmTecalOrdonnanceur.setCursor(Cursor.getDefaultCursor());

				
				
				 
			}
		});
		
		JButton btnGanttCpo = new JButton("Gantt CPO");
		btnGanttCpo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				 int[] sel = tableOF.getSelectedRows();
				 if(sel.length==0) {			
					 JOptionPane.showMessageDialog(frmTecalOrdonnanceur, "Pas d'OF choisi !","Visuel CPO", JOptionPane.ERROR_MESSAGE);
					 return;
				 }
				
				
				 LinkedHashMap<Integer,String> gammes=new LinkedHashMap <Integer,String>();
				 for(int i=0;i<sel.length;i++) {					
					 gammes.put(i,tableOF.getModel().getValueAt(sel[i], 1).toString());
				 }
				
				
								
							
							
				
				 
				 mTecalOrdo.setParams(
							Integer.valueOf(textTEMPS_ZONE_OVERLAP_MIN.getText()),
							Integer.valueOf(textTEMPS_MVT_PONT_MIN_JOB.getText()),
							Integer.valueOf(textGAP_ZONE_NOOVERLAP.getText()),
							Integer.valueOf(textTEMPS_MVT_PONT.getText()),
							Integer.valueOf(textTEMPS_ANO_ENTRE_P1_P2.getText()),
							Integer.valueOf(textTEMPS_MAX_SOLVEUR.getText()));
					
					mTecalOrdo.setBarres(gammes);
					gantFrame=new CPO_IHM(mIcons);
					frmTecalOrdonnanceur.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					mTecalOrdo.run(rdbtnFastModeRadioButton.isSelected(),(int)comboDifficult.getSelectedItem(),gantFrame);	
					frmTecalOrdonnanceur.setCursor(Cursor.getDefaultCursor());
					textArea.setText(mTecalOrdo.print());
				
			}
		});
		GroupLayout gl_panelVisuProd = new GroupLayout(panelVisuProd);
		gl_panelVisuProd.setHorizontalGroup(
			gl_panelVisuProd.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelVisuProd.createSequentialGroup()
					.addGroup(gl_panelVisuProd.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelVisuProd.createSequentialGroup()
							.addGap(199)
							.addComponent(datePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(35)
							.addComponent(btnRunVisuProd)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnGanttCpo, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panelVisuProd.createSequentialGroup()
							.addGap(22)
							.addComponent(scrollPaneVisuProd, GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)))
					.addGap(29))
		);
		gl_panelVisuProd.setVerticalGroup(
			gl_panelVisuProd.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelVisuProd.createSequentialGroup()
					.addGap(5)
					.addGroup(gl_panelVisuProd.createParallelGroup(Alignment.LEADING)
						.addComponent(datePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panelVisuProd.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnRunVisuProd)
							.addComponent(btnGanttCpo)))
					.addGap(42)
					.addComponent(scrollPaneVisuProd, GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
					.addGap(37))
		);
		
		
		panelVisuProd.setLayout(gl_panelVisuProd);
		
		
		
		
	}

	private void buildParamsTab(JTabbedPane tabbedPane) {
		
	
		
		JPanel panel_param = new JPanel();
		tabbedPane.addTab("Paramètres", null, panel_param, null);
		
		JLabel lblTailleZone = new JLabel("TEMPS_ZONE_OVERLAP_MIN");
		
		textTEMPS_ZONE_OVERLAP_MIN = new JTextField();
		textTEMPS_ZONE_OVERLAP_MIN.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_ZONE_OVERLAP_MIN.setColumns(10);
		textTEMPS_ZONE_OVERLAP_MIN.setText(Integer.toString(CST.TEMPS_ZONE_OVERLAP_MIN));
		
		JLabel lblScuritEntreZones = new JLabel("GAP_ZONE_NOOVERLAP");
		
		textGAP_ZONE_NOOVERLAP = new JTextField();
		textGAP_ZONE_NOOVERLAP.setHorizontalAlignment(SwingConstants.RIGHT);
		textGAP_ZONE_NOOVERLAP.setColumns(10);
		textGAP_ZONE_NOOVERLAP.setText(Integer.toString(CST.GAP_ZONE_NOOVERLAP));
		
		JLabel lblcartGroupes = new JLabel("TEMPS_MVT_PONT_MIN_JOB");
		
		textTEMPS_MVT_PONT_MIN_JOB = new JTextField();
		textTEMPS_MVT_PONT_MIN_JOB.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_MVT_PONT_MIN_JOB.setColumns(10);
		textTEMPS_MVT_PONT_MIN_JOB.setText(Integer.toString(CST.TEMPS_MVT_PONT_MIN_JOB));
		
		JLabel lblNewLabel = new JLabel("TEMPS_MVT_PONT");
		
		JLabel lblNewLabel_1 = new JLabel("TEMPS_ANO_ENTRE_P1_P2");
		
		textTEMPS_MVT_PONT = new JTextField();
		textTEMPS_MVT_PONT.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_MVT_PONT.setColumns(10);
		textTEMPS_MVT_PONT.setText(Integer.toString(CST.TEMPS_MVT_PONT));
		
		textTEMPS_ANO_ENTRE_P1_P2 = new JTextField();
		textTEMPS_ANO_ENTRE_P1_P2.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_ANO_ENTRE_P1_P2.setColumns(10);
		textTEMPS_ANO_ENTRE_P1_P2.setText(Integer.toString(CST.TEMPS_ANO_ENTRE_P1_P2));
		
		JLabel lblNewLabel_2 = new JLabel("NUMZONE_DEBUT_PONT_2");
		
		textNUMZONE_DEBUT_PONT_2 = new JTextField();
		textNUMZONE_DEBUT_PONT_2.setHorizontalAlignment(SwingConstants.RIGHT);
		textNUMZONE_DEBUT_PONT_2.setColumns(10);
		textNUMZONE_DEBUT_PONT_2.setText(Integer.toString(CST.NUMZONE_DEBUT_PONT_2));
		
		JLabel lblNewLabel_2_1 = new JLabel("TEMPS MAX SOLVER");
		
		textTEMPS_MAX_SOLVEUR = new JTextField();
		textTEMPS_MAX_SOLVEUR.setText("40");
		textTEMPS_MAX_SOLVEUR.setHorizontalAlignment(SwingConstants.RIGHT);
		textTEMPS_MAX_SOLVEUR.setColumns(10);
		
		GroupLayout gl_panel_param = new GroupLayout(panel_param);
		gl_panel_param.setHorizontalGroup(
			gl_panel_param.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_param.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_param.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_param.createParallelGroup(Alignment.LEADING, false)
							.addComponent(lblTailleZone, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblScuritEntreZones, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addComponent(lblNewLabel)
						.addComponent(lblcartGroupes, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_1)
						.addComponent(lblNewLabel_2)
						.addComponent(lblNewLabel_2_1, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE))
					.addGap(31)
					.addGroup(gl_panel_param.createParallelGroup(Alignment.LEADING)
						.addComponent(textTEMPS_MAX_SOLVEUR, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel_param.createParallelGroup(Alignment.LEADING, false)
							.addComponent(textNUMZONE_DEBUT_PONT_2, 0, 0, Short.MAX_VALUE)
							.addComponent(textTEMPS_ANO_ENTRE_P1_P2, 0, 0, Short.MAX_VALUE)
							.addComponent(textTEMPS_MVT_PONT_MIN_JOB, 0, 0, Short.MAX_VALUE)
							.addComponent(textTEMPS_MVT_PONT, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
							.addComponent(textGAP_ZONE_NOOVERLAP, Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
							.addComponent(textTEMPS_ZONE_OVERLAP_MIN, Alignment.TRAILING, 0, 0, Short.MAX_VALUE)))
					.addContainerGap(447, Short.MAX_VALUE))
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
						.addComponent(lblScuritEntreZones)
						.addComponent(textGAP_ZONE_NOOVERLAP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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
						.addComponent(textNUMZONE_DEBUT_PONT_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_param.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_2_1)
						.addComponent(textTEMPS_MAX_SOLVEUR, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(334, Short.MAX_VALUE))
		);
		gl_panel_param.linkSize(SwingConstants.VERTICAL, new Component[] {lblTailleZone, lblScuritEntreZones, lblcartGroupes, lblNewLabel, lblNewLabel_1});
		gl_panel_param.linkSize(SwingConstants.HORIZONTAL, new Component[] {lblTailleZone, lblScuritEntreZones, lblcartGroupes, lblNewLabel, lblNewLabel_1});
		panel_param.setLayout(gl_panel_param);
	}

	private void setIconButton(JButton btnButton,String fileName) {
		try {
			
			InputStream in = getClass().getResourceAsStream("/"+fileName) ;					
			BufferedImage someImage = ImageIO.read(in);
			img = new ImageIcon(someImage);		
		    btnButton.setIcon(  img);		    
		  } catch (Exception ex) {
		    System.out.println(ex);
		  }
	}

	public void buildTableModelBarre()
	        throws SQLException {

	   

	    // names of columns
	    Vector<String> columnNames = new Vector<String>();
	   
	    columnNames.add("barre");
	    columnNames.add("gamme");
	    columnNames.add("montée");
	    columnNames.add("desc.");
	    


	    modelBarres= new DefaultTableModel(null, columnNames) {

    	    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
    	    public boolean isCellEditable(int row, int column) {
    	       //all cells false
    	       return false;
    	    }
    	};
   


	}
	
	public   void buildTableModelGamme(ResultSet rs)
	        throws SQLException {

	    ResultSetMetaData metaData = rs.getMetaData();

	    // names of columns
	    Vector<String> columnNames = new Vector<String>();
	    int columnCount = metaData.getColumnCount();
	    for (int column = 1; column <= columnCount; column++) {
	        columnNames.add(metaData.getColumnName(column));
	    }

	    // data of the table
	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    while (rs.next()) {
	        Vector<Object> vector = new Vector<Object>();
	        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
	            vector.add(rs.getObject(columnIndex));
	        }
	        data.add(vector);
	    }
	    
	    modelGammes = new DefaultTableModel(data, columnNames) {

    	    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
    	    public boolean isCellEditable(int row, int column) {
    	       //all cells false
    	       return false;
    	    }
    	};
   

	}
	
	public   void buildTableModelVisuProd()
	        throws SQLException {

		java.util.Date d=(java.util.Date) datePicker.getModel().getValue();
		ResultSet rs =sqlCnx.getVisuProd(d);
	    ResultSetMetaData metaData = rs.getMetaData();

	    // names of columns
	    Vector<String> columnNames = new Vector<String>();
	    int columnCount = metaData.getColumnCount();
	    for (int column = 1; column <= columnCount; column++) {
	        columnNames.add(metaData.getColumnName(column));
	    }

	    // data of the table
	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    while (rs.next()) {
	        Vector<Object> vector = new Vector<Object>();
	        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
	            vector.add(rs.getObject(columnIndex));
	        }
	        data.add(vector);
	    }
	    
	    modelVisuProd = new DefaultTableModel(data, columnNames) {

    	    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
    	    public boolean isCellEditable(int row, int column) {
    	       //all cells false
    	       return false;
    	    }
    	};
   
    	tableOF = new JTable(modelVisuProd);
		scrollPaneVisuProd.setViewportView(tableOF);
		//panelVisuProd.setLayout(gl_panelVisuProd);
		
		
		tableOF.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
	
		tableOF.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent mouseEvent) {
		        JTable table =(JTable) mouseEvent.getSource();
		        Point point = mouseEvent.getPoint();
		        int row = table.rowAtPoint(point);
		        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {	    	        	
		        	modelVisuProd.removeRow(row);	         	
		        }
		    }
		});
		scrollPaneVisuProd.setViewportView(tableOF);

	}
	
	private void moveRowBy(int by)
	{
	    DefaultTableModel model = (DefaultTableModel) tableBarres.getModel();
	    int[] rows = tableBarres.getSelectedRows();
	    int destination = rows[0] + by;
	    int rowCount = model.getRowCount();

	    if (destination < 0 || destination >= rowCount)
	    {
	        return;
	    }

	    model.moveRow(rows[0], rows[rows.length - 1], destination);
	    tableBarres.setRowSelectionInterval(rows[0] + by, rows[rows.length - 1] + by);
	}
}
