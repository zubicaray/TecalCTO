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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tecal.scheduler.CST;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.Barre;


public class CPO_Panel extends JPanel {

	private static final Logger logger = LogManager.getLogger(CPO_Panel.class);
	private static final long serialVersionUID = 1L;
	private JTable mTableGammes;
	private JTable mTableBarres;
	private DefaultTableModel mModelBarres;
	private JButton mBtnRun;
	private JTextArea mTextArea;
	private JComboBox<String> mVitesseCombo ;
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
        mVitesseCombo = new JComboBox<>();
        mVitesseCombo.addItem("lente");
        mVitesseCombo.addItem("normale");
        mVitesseCombo.addItem("rapide");

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

		JScrollPane scrollPaneBarres = new JScrollPane();

		mBtnRun = new JButton("GO");
		mBtnRun.setHorizontalAlignment(SwingConstants.LEFT);

		JButton btnDownButton = new JButton();
		btnDownButton.setHorizontalAlignment(SwingConstants.CENTER);

		JButton btnUpButton = new JButton();
		btnUpButton.setHorizontalAlignment(SwingConstants.CENTER);
		
		JButton btnDelButton = new JButton();
	
		setIconButton(this,btnDelButton,"icons8-delete-16.png");
		
		GroupLayout gl_panelCPO = buildGrouping(scrollPaneMsg, lblGammes, textFiltre, btnReload, scrollPane_gamme,
				scrollPaneBarres, btnDownButton, btnUpButton, btnDelButton);



		setIconButton(this,btnUpButton,"icons8-up-16.png");
		setIconButton(this,btnDownButton,"icons8-down-16.png");
		setIconButton(this,mBtnRun,"icons8-play-16.png");

		btnDelButton.setHorizontalAlignment(SwingConstants.CENTER);
		btnDelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
    	        int row = mTableBarres.getSelectedRow();
    	        if (row!= -1) {
    	        	
    	        	mModelBarres.removeRow(row);
    	        	mTableGammes.repaint();
    	        }
			}
		});
		
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
		gl_panelCPO.setHorizontalGroup(
			gl_panelCPO.createParallelGroup(Alignment.LEADING)
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
									.addGap(70)
									.addComponent(btnReload)
									.addGap(232)))
							.addComponent(scrollPaneBarres, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
							.addGap(18)
							.addGroup(gl_panelCPO.createParallelGroup(Alignment.TRAILING)
								.addComponent(mBtnRun, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panelCPO.createSequentialGroup()
									.addGroup(gl_panelCPO.createParallelGroup(Alignment.TRAILING)
										.addComponent(btnDownButton, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
										.addComponent(btnUpButton, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
										.addComponent(btnDelButton, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
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
						.addComponent(btnReload))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelCPO.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelCPO.createParallelGroup(Alignment.BASELINE)
							.addComponent(scrollPane_gamme, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
							.addComponent(scrollPaneBarres, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
						.addGroup(gl_panelCPO.createSequentialGroup()
							.addGap(35)
							.addComponent(btnUpButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnDownButton)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnDelButton)
							.addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(mBtnRun)))
					.addGap(47)
					.addComponent(scrollPaneMsg, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
					.addGap(24))
		);
		return gl_panelCPO;
	}
	private void buildTables(JScrollPane scrollPaneBarres) {
		try {
			mModelBarres= new DefaultTableModel() {
			    private static final long serialVersionUID = 1L;

			    @Override
			    public boolean isCellEditable(int row, int column) {
			        // Rendre éditables les colonnes nécessaires
			        return column == 1 || column == 5 || column == 3 || column == 4;
			    }
			};

			mTableBarres = new JTable() ;
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
				public boolean isCellEditable(int row, int col) {					
						return false; //Renders column 0 uneditable.
					
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
	}


	public DefaultTableModel getModelBarres() {
		return mModelBarres;
	}
	public void setModelBarres(LinkedHashMap<Integer, Barre> set) {
		mModelBarres.setRowCount(0);
		//mNumBarre=0;
        for (Map.Entry<Integer, Barre> entry : set.entrySet()) {

        	Barre b=  entry.getValue();

            Object[] rowO = { b.idbarre,b.barreNom,b.gamme,CST.VITESSES[b.vitesseMontee],CST.VITESSES[b.vitesseDescente],b.prioritaire };
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
		mTextArea.setText(mTextArea.getText()+s);
	}

	private void  setActionListener() {
		  mBtnRun.addActionListener(new ActionListener() {
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
							//int idbarre=(int) mTableBarres.getValueAt(count, 0);
							int idbarre=(int) mModelBarres.getValueAt(count, 0);
							String gamme=mTableBarres.getValueAt(count, 1).toString();
							String nomBarre=mTableBarres.getValueAt(count, 0).toString();

							mVitesseCombo.setSelectedItem( mTableBarres.getValueAt(count, 2) );
							int indexMontee= mVitesseCombo.getSelectedIndex();


							mVitesseCombo.setSelectedItem( mTableBarres.getValueAt(count, 3) );
							int indexDesc = mVitesseCombo.getSelectedIndex();

							boolean prio=(Boolean) mTableBarres.getValueAt(count, 4);
							if (prio)
								logger.info("barre: "+nomBarre+" prioritaire !");
							gammes.put(idbarre,gamme);
							barres.put(idbarre,new Barre(idbarre,nomBarre,gamme,indexMontee,indexDesc,prio));
						}


						mCPO_IHM.run(barres);





					//}


				}
			});

	}

	public  void buildTableModelBarre()
	        throws SQLException {



	    // names of columns
	    Vector<String> columnNames = new Vector<>();

	    columnNames.add("barre");
	    columnNames.add("id");
	    columnNames.add("gamme");
	    columnNames.add("montée");
	    columnNames.add("desc.");
	    columnNames.add("prio.");

	    mModelBarres.setColumnIdentifiers(columnNames);


        mTableBarres.setModel(mModelBarres);
        TableColumnModel tcm = mTableBarres.getColumnModel();
        tcm.removeColumn( tcm.getColumn(0 ));


        TableColumn colBarre = mTableBarres.getColumnModel().getColumn(0);
        colBarre.setCellEditor(new DefaultCellEditor(new JTextField()));
        

        TableColumn colMontee =mTableBarres.getColumnModel().getColumn(2);

        //définir l'éditeur par défaut
        colMontee.setCellEditor(new DefaultCellEditor(mVitesseCombo));

        TableColumn colDescente =mTableBarres.getColumnModel().getColumn(3);
        //définir l'éditeur par défaut
        colDescente.setCellEditor(new DefaultCellEditor(mVitesseCombo));


        TableColumn colPrio =mTableBarres.getColumnModel().getColumn(4);
        colPrio.setCellEditor(mTableBarres.getDefaultEditor(Boolean.class));
        colPrio.setCellRenderer(mTableBarres.getDefaultRenderer(Boolean.class));

        mTableBarres.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	TableColumnModel columnModel = mTableBarres.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        //columnModel.getColumn(0).setMaxWidth(50);
        mTableBarres.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);


	}

	public void removeBarre(int barre) {
		int i =0;
		boolean found = false;
		 for (; i < mTableBarres.getRowCount(); i++) {  // Loop through the rows
		       
	        if(barre== (int)mModelBarres.getValueAt(i, 0)) {
	        	found=true;
	        	break;
	        }
		 }
		 if(found) {
			 mModelBarres.removeRow(i);
			 mTableBarres.addNotify();
			 }
	}
	
	public void buildTableModelGamme()
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

    	        	Object[] rowO = {++mNumBarre,mNumBarre+"", gamme,CST.VITESSES[CST.VITESSE_NORMALE],CST.VITESSES[CST.VITESSE_NORMALE],false };
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
