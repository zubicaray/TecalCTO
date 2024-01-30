package org.tecal.ui;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.tecal.scheduler.CST;
import org.tecal.scheduler.TecalOrdo;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.GammeType;

import com.formdev.flatlaf.FlatLightLaf;

public class TecalGUI {

	private JFrame frame;
	private JTable tableGammes;
	private DefaultTableModel modelGammes ;
	private JTable tableBarres;
	private DefaultTableModel modelBarres;
	private JTextField textFiltre;
	private JRadioButton rdbtnFastModeRadioButton;
	private JComboBox<Integer> comboDifficult;
	
	private SQL_DATA sqlCnx ;
	private int mNumBarre=0;
	private JTextField textField;
	private TecalOrdo mTecalOrdo;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		FlatLightLaf.setup();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TecalGUI window = new TecalGUI();
					window.frame.setVisible(true);
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
		
		
		mTecalOrdo=new TecalOrdo();
		mTecalOrdo.setDataSource(CST.SQLSERVER);
		
		
		
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
		
		
		
		frame = new JFrame();
		frame.setBounds(100, 100, 702, 661);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
		);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Choix des gammes", null, panel, null);
		
		JScrollPane scrollPane = new JScrollPane();
		
		JScrollPane scrollPaneBarres = new JScrollPane();
		
		textFiltre = new JTextField();
		textFiltre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(((DefaultTableModel) tableGammes.getModel())); 
			    sorter.setRowFilter(RowFilter.regexFilter(textFiltre.getText()));

			    tableGammes.setRowSorter(sorter);
			}
		});
		textFiltre.setColumns(10);
		
		JLabel lblBarreLabel = new JLabel("barres:");
		
		JLabel lblGammes = new JLabel("gammes:");
		
		JButton btnUpButton = new JButton("up");
		btnUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveRowBy(-1);
			}
		});
		
		JButton btnDownButton = new JButton("down");
		btnDownButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveRowBy(1);
			}
		});
		
		rdbtnFastModeRadioButton = new JRadioButton("mode rapide");
		
		Integer [] comboVals= {3,4,5,6,7,8,9};
		comboDifficult = new JComboBox<Integer>(comboVals);
		comboDifficult.setSelectedItem(7);
		
		JLabel lblHardynessLabel = new JLabel("difficulté");
		
		JScrollPane scrollPaneMsg = new JScrollPane();
		
		JButton btnRun = new JButton("RUN");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				LinkedHashMap<Integer,String> gammes=new LinkedHashMap <Integer,String>();
				if(tableBarres.getRowCount()<2) {
					JOptionPane.showMessageDialog(frame, "Minimum deux barres requises !","Tecal CPO", JOptionPane.ERROR_MESSAGE);
				}else {
					
					for (int count = 0; count < tableBarres.getRowCount(); count++){
						gammes.put((int)tableBarres.getValueAt(count, 0),tableBarres.getValueAt(count, 1).toString());
					}
					
					mTecalOrdo.setBarres(gammes);
					mTecalOrdo.run(rdbtnFastModeRadioButton.isSelected(),(int)comboDifficult.getSelectedItem());	
					textField.setText(mTecalOrdo.print());
				}
				
				
			}
		});
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(scrollPaneMsg, GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(gl_panel.createSequentialGroup()
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(26)
									.addComponent(lblGammes, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE))
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(scrollPaneBarres, GroupLayout.PREFERRED_SIZE, 135, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
										.addComponent(btnUpButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnDownButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnRun, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)))
								.addComponent(lblBarreLabel, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE))
							.addGap(33))
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(rdbtnFastModeRadioButton)
							.addGap(137)
							.addComponent(lblHardynessLabel, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(comboDifficult, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(311, Short.MAX_VALUE))))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(105)
							.addComponent(btnUpButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnDownButton)
							.addGap(200))
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(49)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblBarreLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lblGammes, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
								.addComponent(scrollPaneBarres, GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(rdbtnFastModeRadioButton)
						.addComponent(comboDifficult, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblHardynessLabel)
						.addComponent(btnRun))
					.addGap(18)
					.addComponent(scrollPaneMsg, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
					.addGap(24))
		);
		
		textField = new JTextField();
		scrollPaneMsg.setViewportView(textField);
		textField.setColumns(10);
		
		try {
			
			
			buildTableModelBarre();
			tableBarres = new JTable(modelBarres);
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
	    	        	Object[] rowO = { mNumBarre, gamme };
	    	        	modelBarres.addRow(rowO);
	    	        	
	    	        }
	    	    }
	    	});
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		scrollPane.setViewportView(tableGammes);
		panel.setLayout(gl_panel);
		frame.getContentPane().setLayout(groupLayout);
	}

	public void buildTableModelBarre()
	        throws SQLException {

	   

	    // names of columns
	    Vector<String> columnNames = new Vector<String>();
	   
	    columnNames.add("barre");
	    columnNames.add("gamme");
	    


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
