package org.tecal.ui;

import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.tecal.scheduler.data.SQL_DATA;




public class CPO_Panel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable tableGammes;
	private JTable table_barre;
	private DefaultTableModel modelBarres;
	public DefaultTableModel getModelBarres() {
		return modelBarres;
	}
	public void setModelBarres(DefaultTableModel modelBarres) {
		this.modelBarres = modelBarres;
	}
	private DefaultTableModel modelGammes;
	private AtomicInteger mNumBarre;
	private JCheckBox chckbxPrio ;
	
	public JCheckBox getChckbxPrio() {
		return chckbxPrio;
	}
	public void setChckbxPrio(JCheckBox chckbxPrio) {
		this.chckbxPrio = chckbxPrio;
	}
		
	
	public CPO_Panel() {
		
		mNumBarre=new AtomicInteger();
		
		
		createPanelCPO();
		
	}
	private void createPanelCPO() {
		JScrollPane scrollPaneMsg = new JScrollPane();
		
		JLabel lblGammes = new JLabel("gammes:");
		lblGammes.setFont(new Font("Tahoma", Font.PLAIN, 13));
		
		JTextField textField = new JTextField();
		textField.setColumns(10);
		
		JScrollPane scrollPane_gamme = new JScrollPane();
		
		JLabel lblBarreLabel = new JLabel("barres:                     vitesses:");
		lblBarreLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		
		JScrollPane scrollPaneBarres = new JScrollPane();
		
		JButton btnRun = new JButton("GO");
		btnRun.setHorizontalAlignment(SwingConstants.LEFT);
		
		chckbxPrio = new JCheckBox(" prioriser");
		chckbxPrio.setFont(new Font("Tahoma", Font.PLAIN, 13));
		
		JButton btnDownButton = new JButton();
		btnDownButton.setHorizontalAlignment(SwingConstants.CENTER);
		
		JButton btnUpButton = new JButton();
		btnUpButton.setHorizontalAlignment(SwingConstants.CENTER);
		GroupLayout gl_panelCPO = new GroupLayout(this);
		gl_panelCPO.setHorizontalGroup(
			gl_panelCPO.createParallelGroup(Alignment.LEADING)
				.addGap(0, 708, Short.MAX_VALUE)
				.addGroup(gl_panelCPO.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPaneMsg, GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
						.addGroup(gl_panelCPO.createSequentialGroup()
							.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelCPO.createSequentialGroup()
									.addComponent(lblGammes, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
									.addGap(111)
									.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addComponent(scrollPane_gamme, GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE))
							.addGap(18)
							.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblBarreLabel, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 204, GroupLayout.PREFERRED_SIZE)
								.addComponent(scrollPaneBarres, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_panelCPO.createParallelGroup(Alignment.TRAILING)
								.addComponent(btnRun, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
								.addComponent(chckbxPrio, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panelCPO.createSequentialGroup()
									.addGroup(gl_panelCPO.createParallelGroup(Alignment.TRAILING)
										.addComponent(btnDownButton, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
										.addComponent(btnUpButton, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
									.addGap(50)))))
					.addContainerGap())
		);
		gl_panelCPO.setVerticalGroup(
			gl_panelCPO.createParallelGroup(Alignment.LEADING)
				.addGap(0, 594, Short.MAX_VALUE)
				.addGroup(gl_panelCPO.createSequentialGroup()
					.addGap(50)
					.addGroup(gl_panelCPO.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblGammes, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblBarreLabel))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelCPO.createParallelGroup(Alignment.BASELINE)
							.addComponent(scrollPane_gamme, GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
							.addComponent(scrollPaneBarres, GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))
						.addGroup(gl_panelCPO.createSequentialGroup()
							.addGap(35)
							.addComponent(btnUpButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnDownButton)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(chckbxPrio)
							.addPreferredGap(ComponentPlacement.RELATED, 158, Short.MAX_VALUE)
							.addComponent(btnRun)))
					.addGap(47)
					.addComponent(scrollPaneMsg, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
					.addGap(24))
		);
		
		
		
		
		
		try {
			modelBarres= new DefaultTableModel() ;
		    	
			table_barre = new JTable() {

	            private static final long serialVersionUID = 1L;

	            @Override
	            public boolean isCellEditable(int row, int column) {
	              return column == 2 || column == 3;
	            }
	          
			
	        };
	     
			buildTableModelBarre(modelBarres,table_barre);
			
	        
	        
	        
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		table_barre.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table_barre.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		scrollPaneBarres.setViewportView(table_barre);
		
		
		 // It creates and displays the table
	    try {   		
	    	
	    	 modelGammes = new DefaultTableModel() {

	     	
	 			private static final long serialVersionUID = 1L;

	 			@Override
	     	    public boolean isCellEditable(int row, int column) {
	     	       //all cells false
	     	       return false;
	     	    }
	     	};
	     	
	     	tableGammes = new JTable();
	     	
	     	tableGammes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	     	tableGammes.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	     	
			buildTableModelGamme(modelGammes,tableGammes,modelBarres,mNumBarre);
	    	
			  
	    	
	    	
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		scrollPane_gamme.setViewportView(tableGammes);
		setLayout(gl_panelCPO);
	}
	
	
	

	public   void buildTableModelBarre(DefaultTableModel inModelBarres,JTable inTableBarres)
	        throws SQLException {

	   

	    // names of columns
	    Vector<String> columnNames = new Vector<String>();
	   
	    columnNames.add("barre");
	    columnNames.add("gamme");
	    columnNames.add("montée");
	    columnNames.add("desc.");
	    
	    inModelBarres.setColumnIdentifiers(columnNames);   
       
        
        inTableBarres.setModel(inModelBarres);
        
        TableColumn colMontee =inTableBarres.getColumnModel().getColumn(2);
        // créer un ComboBox
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem("lente");
        cb.addItem("normale");
        cb.addItem("rapide");
        //définir l'éditeur par défaut
        colMontee.setCellEditor(new DefaultCellEditor(cb));
        
        TableColumn colDescente =inTableBarres.getColumnModel().getColumn(3);	
        //définir l'éditeur par défaut
        colDescente.setCellEditor(new DefaultCellEditor(cb));
        
        
        inTableBarres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	TableColumnModel columnModel = inTableBarres.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(0).setMaxWidth(40);	          	
        inTableBarres.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        
        inTableBarres.addMouseListener(new MouseAdapter() {
    	    public void mousePressed(MouseEvent mouseEvent) {
    	        JTable table =(JTable) mouseEvent.getSource();
    	        DefaultTableModel model=(DefaultTableModel) table.getModel();
    	        Point point = mouseEvent.getPoint();
    	        int row = table.rowAtPoint(point);
    	        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {	    	        	
    	        	model.removeRow(row);	         	
    	        }
    	    }
    	});
   
        
      


	}
	
	public  void buildTableModelGamme(DefaultTableModel model,JTable inTableGammes,DefaultTableModel modelBarre,AtomicInteger numBarre)
	        throws SQLException {

		
	    
	    
	    inTableGammes.setModel(model);

	    inTableGammes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
          	
        inTableGammes.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    	
        inTableGammes.addMouseListener(new MouseAdapter() {
    	    public void mousePressed(MouseEvent mouseEvent) {
    	        JTable table =(JTable) mouseEvent.getSource();    	       
    	        Point point = mouseEvent.getPoint();
    	        int row = table.rowAtPoint(point);
    	        row=table.convertRowIndexToModel(row); 	  
    	        int a = table.getSelectedRow();
    	        int b = mouseEvent.getClickCount();
    	        if (mouseEvent.getClickCount() == 1 && table.getSelectedRow() != -1) {
    	        	String gamme=table.getModel().getValueAt(row, 0).toString();
    	        	numBarre.set(numBarre.get()+1);
    	        	Object[] rowO = { numBarre.get(), gamme,"normale","normale" };
    	        	modelBarre.addRow(rowO);
    	        	
    	        }
    	    }
    	});
	   
   

	}
	public void setRessource(ResultSet rs ) throws SQLException {
		ResultSetMetaData metaData =rs.getMetaData();

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
	    modelGammes.setDataVector(data, columnNames);
		TableColumnModel columnModel = tableGammes.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(70);
        columnModel.getColumn(0).setMaxWidth(400);
	    
	}
	
}
