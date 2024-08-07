package org.tecal.ui;

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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.tecal.scheduler.CST;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.Barre;


public class CPO_Panel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTable mTableGammes;
	private JTable mTableBarres;
	private DefaultTableModel mModelBarres;
	private JButton btnRun;
	private JTextArea textArea;
	JComboBox<String> vitesseCombo ;
	private DefaultTableModel mModelGammes;
	private Integer mNumBarre;

	@SuppressWarnings("unused")
	private CPO_IHM mCPO_IHM;


	public CPO_Panel(CPO_IHM cpoIhm) {

		mCPO_IHM=cpoIhm;
		mNumBarre=0;
		createPanelCPO();

	}
	public CPO_Panel( ) {


		mNumBarre=0;
		createPanelCPO();

	}






	private void createPanelCPO() {
		JScrollPane scrollPaneMsg = new JScrollPane();

		// créer un ComboBox
        vitesseCombo = new JComboBox<>();
        vitesseCombo.addItem("lente");
        vitesseCombo.addItem("normale");
        vitesseCombo.addItem("rapide");

		JLabel lblGammes = new JLabel("gammes:");
		lblGammes.setFont(new Font("Tahoma", Font.PLAIN, 13));

		JTextField textFiltre = new JTextField();
		textFiltre.setColumns(10);

		JButton btnReload  = new JButton();
		setIconButton(this,btnReload,"icons8-update-16.png");


		btnReload.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					SQL_DATA.getInstance().setMissingTimeMovesGammes();
					 //mModelGammes.fireTableStructureChanged();
					 //mTableGammes.revalidate();
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

		JLabel lblBarreLabel = new JLabel("barres:                     vitesses:");
		lblBarreLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));

		JScrollPane scrollPaneBarres = new JScrollPane();

		btnRun = new JButton("GO");
		btnRun.setHorizontalAlignment(SwingConstants.LEFT);

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
									.addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(70)
									.addComponent(btnReload, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										)

								.addComponent(scrollPane_gamme, GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
							.addGap(18)
							.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblBarreLabel, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 204, GroupLayout.PREFERRED_SIZE)
								.addComponent(scrollPaneBarres, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 247, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_panelCPO.createParallelGroup(Alignment.TRAILING)
								.addComponent(btnRun, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)

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
						.addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnReload, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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

							.addPreferredGap(ComponentPlacement.RELATED, 158, Short.MAX_VALUE)
							.addComponent(btnRun)))
					.addGap(47)
					.addComponent(scrollPaneMsg, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
					.addGap(24))
		);



		setIconButton(this,btnUpButton,"icons8-up-16.png");
		setIconButton(this,btnDownButton,"icons8-down-16.png");
		setIconButton(this,btnRun,"icons8-play-16.png");


		btnUpButton.setHorizontalAlignment(SwingConstants.CENTER);
		btnUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveRowBy(-1);
			}
		});

		btnDownButton.setHorizontalAlignment(SwingConstants.CENTER);
		btnDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveRowBy(1);
			}
		});

		textArea = new JTextArea();
		scrollPaneMsg.setViewportView(textArea);

		try {
			mModelBarres= new DefaultTableModel() ;

			mTableBarres = new JTable() {

	            private static final long serialVersionUID = 1L;

	            @Override
	            public boolean isCellEditable(int row, int column) {
	              return column == 2 || column == 3|| column == 4;
	            }


	        };

			buildTableModelBarre();




		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		mTableBarres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mTableBarres.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		scrollPaneBarres.setViewportView(mTableBarres);


		 // It creates and displays the table
	    try {

	    	 mModelGammes = new DefaultTableModel() {


	 			private static final long serialVersionUID = 1L;

	 			@Override
	     	    public boolean isCellEditable(int row, int column) {
	     	       //all cells false
	     	       return false;
	     	    }
	     	};

	     	mTableGammes = new JTable(mModelGammes) {
	       		 /**
	   			 *
	   			 */
	   			private static final long serialVersionUID = 1L;

	   			@Override
	                public Component prepareRenderer(
	                        TableCellRenderer renderer, int row, int column) {
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





	    setActionListener();
	    mTableGammes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_gamme.setViewportView(mTableGammes);
		setLayout(gl_panelCPO);
	}


	public DefaultTableModel getModelBarres() {
		return mModelBarres;
	}
	public void setModelBarres(LinkedHashMap<Integer, Barre> set) {
		mModelBarres.setRowCount(0);
		//mNumBarre=0;
        for (Map.Entry<Integer, Barre> entry : set.entrySet()) {

        	Barre b=  entry.getValue();

            Object[] rowO = { b.idbarre,b.gamme,CST.VITESSES[b.vitesseMontee],CST.VITESSES[b.vitesseDescente],b.prioritaire };
			if(b.idbarre>mNumBarre) {
				mNumBarre=b.idbarre;
			}
			mModelBarres.addRow(rowO);
        }

	}
	public void setModelBarres(DefaultTableModel mModelBarres) {
		this.mModelBarres = mModelBarres;
	}

	public void setText(String s) {
		textArea.setText(textArea.getText()+s);
	}

	private void  setActionListener() {
		  btnRun.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					LinkedHashMap<Integer,String> gammes=new LinkedHashMap <>();
					LinkedHashMap<Integer,Barre> barres=new LinkedHashMap <>();

					//if(mTableBarres.getRowCount()<2) {
					//	JOptionPane.showMessageDialog(mCPO_IHM, "Minimum deux barres requises !","Tecal CPO", JOptionPane.ERROR_MESSAGE);
					//}else {

						// utiliser un objet de classe Barre ave vitesse et prio
						gammes.clear();
						for (int count = 0; count < mTableBarres.getRowCount(); count++){
							int idbarre=(int) mTableBarres.getValueAt(count, 0);
							String gamme=mTableBarres.getValueAt(count, 1).toString();

							vitesseCombo.setSelectedItem( mTableBarres.getValueAt(count, 2) );
							int indexMontee= vitesseCombo.getSelectedIndex();


							vitesseCombo.setSelectedItem( mTableBarres.getValueAt(count, 3) );
							int indexDesc = vitesseCombo.getSelectedIndex();

							boolean prio=(Boolean) mTableBarres.getValueAt(count, 4);
							gammes.put(idbarre,gamme);
							barres.put(idbarre,new Barre(idbarre,gamme,indexMontee,indexDesc,prio));
						}


						mCPO_IHM.run(barres);





					//}


				}
			});

	}


	public   void buildTableModelBarre()
	        throws SQLException {



	    // names of columns
	    Vector<String> columnNames = new Vector<>();

	    columnNames.add("barre");
	    columnNames.add("gamme");
	    columnNames.add("montée");
	    columnNames.add("desc.");
	    columnNames.add("prio.");

	    mModelBarres.setColumnIdentifiers(columnNames);


        mTableBarres.setModel(mModelBarres);

        TableColumn colMontee =mTableBarres.getColumnModel().getColumn(2);


        //définir l'éditeur par défaut
        colMontee.setCellEditor(new DefaultCellEditor(vitesseCombo));

        TableColumn colDescente =mTableBarres.getColumnModel().getColumn(3);
        //définir l'éditeur par défaut
        colDescente.setCellEditor(new DefaultCellEditor(vitesseCombo));


        TableColumn colPrio =mTableBarres.getColumnModel().getColumn(4);
        colPrio.setCellEditor(mTableBarres.getDefaultEditor(Boolean.class));
        colPrio.setCellRenderer(mTableBarres.getDefaultRenderer(Boolean.class));

        mTableBarres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	TableColumnModel columnModel = mTableBarres.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(0).setMaxWidth(50);
        mTableBarres.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        mTableBarres.addMouseListener(new MouseAdapter() {
    	    @Override
			public void mousePressed(MouseEvent mouseEvent) {
    	        JTable table =(JTable) mouseEvent.getSource();
    	        DefaultTableModel model=(DefaultTableModel) table.getModel();
    	        Point point = mouseEvent.getPoint();
    	        int row = table.rowAtPoint(point);
    	        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
    	        	if(model.getDataVector().get(row).get(0)==mNumBarre) {
						//mNumBarre--;
					}
    	        	model.removeRow(row);

    	        }
    	    }
    	});





	}

	public void removeBarre(int barre) {
		int i =0;
		boolean found = false;
		 for (; i < mTableBarres.getRowCount(); i++) {  // Loop through the rows
		       
	        if(barre== (int)mTableBarres.getValueAt(i, 0)) {
	        	found=true;
	        	break;
	        }
		 }
		 if(found) {
			 mModelBarres.removeRow(i);
			 mTableBarres.addNotify();
			 }
	}
	
	public  void buildTableModelGamme()
	        throws SQLException {



	    mTableGammes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        mTableGammes.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        mTableGammes.addMouseListener(new MouseAdapter() {
    	    @Override
			public void mousePressed(MouseEvent mouseEvent) {
    	        JTable table =(JTable) mouseEvent.getSource();
    	        Point point = mouseEvent.getPoint();
    	        int row = table.rowAtPoint(point);
    	        row=table.convertRowIndexToModel(row);

    	        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
    	        	String gamme=table.getModel().getValueAt(row, 0).toString();

    	        	Object[] rowO = {++mNumBarre, gamme,CST.VITESSES[CST.VITESSE_NORMALE],CST.VITESSES[CST.VITESSE_NORMALE],false };
    	        	mModelBarres.addRow(rowO);

    	        }
    	    }
    	});

        mTableGammes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){

			private static final long serialVersionUID = 1L;

			@Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                int modelIndex = table.convertRowIndexToModel(row)      ;

                String gamme = (String)table.getModel().getValueAt(modelIndex, 0);


                if (SQL_DATA.getInstance().getMissingTimeMovesGammes().contains(gamme)) {
                    setBackground(Color.RED);
                    setForeground(Color.BLACK);
                } else {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                return this;
            }
        });



	}
	public void setRessource(ResultSet rs ) throws SQLException {
		ResultSetMetaData metaData =rs.getMetaData();

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

	public static void setIconButton(Object o,JButton btnButton,String fileName) {
		try {

			InputStream in = o.getClass().getResourceAsStream("/"+fileName) ;
			BufferedImage someImage = ImageIO.read(in);
			ImageIcon img = new ImageIcon(someImage);
		    btnButton.setIcon(  img);
		  } catch (Exception ex) {
		    System.out.println(ex);
		  }
	}

	private void moveRowBy(int by)
	{
	    DefaultTableModel model = (DefaultTableModel) mTableBarres.getModel();
	    int[] rows = mTableBarres.getSelectedRows();
	    int destination = rows[0] + by;
	    int rowCount = model.getRowCount();

	    if (destination < 0 || destination >= rowCount)
	    {
	        return;
	    }

	    model.moveRow(rows[0], rows[rows.length - 1], destination);
	    mTableBarres.setRowSelectionInterval(rows[0] + by, rows[rows.length - 1] + by);
	}

}
