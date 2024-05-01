package org.tecal.ui;

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
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;



public class CPO_Panel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JTable mTableGammes;
	private JTable mTableBarres;
	private DefaultTableModel modelBarres;
	private JButton btnRun;
	public DefaultTableModel getModelBarres() {
		return modelBarres;
	}
	public void setModelBarres(DefaultTableModel modelBarres) {
		this.modelBarres = modelBarres;
	}
	private JTextArea textArea;
	private DefaultTableModel mModelGammes;
	private AtomicInteger mNumBarre;
	private JCheckBox chckbxPrio ;

	@SuppressWarnings("unused")
	private CPO_IHM mCPO_IHM;

	public JCheckBox getChckbxPrio() {
		return chckbxPrio;
	}
	public void setChckbxPrio(JCheckBox chckbxPrio) {
		this.chckbxPrio = chckbxPrio;
	}
	public CPO_Panel(CPO_IHM cpoIhm) {

		mCPO_IHM=cpoIhm;
		mNumBarre=new AtomicInteger();
		createPanelCPO();

	}

	public CPO_Panel() {

		//mCPO_IHM=new CPO_IHM();
		mNumBarre=new AtomicInteger();
		createPanelCPO();

	}


	private void createPanelCPO() {
		JScrollPane scrollPaneMsg = new JScrollPane();

		JLabel lblGammes = new JLabel("gammes:");
		lblGammes.setFont(new Font("Tahoma", Font.PLAIN, 13));

		JTextField textFiltre = new JTextField();
		textFiltre.setColumns(10);


		textFiltre.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				TableRowSorter<TableModel> sorter = new TableRowSorter<>((mTableGammes.getModel()));
			    sorter.setRowFilter(RowFilter.regexFilter(textFiltre.getText()));

			    mTableGammes.setRowSorter(sorter);
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
									.addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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
						.addComponent(textFiltre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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



		setIconButton(btnUpButton,"icons8-up-16.png");
		setIconButton(btnDownButton,"icons8-down-16.png");
		setIconButton(btnRun,"icons8-play-16.png");


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
			modelBarres= new DefaultTableModel() ;

			mTableBarres = new JTable() {

	            private static final long serialVersionUID = 1L;

	            @Override
	            public boolean isCellEditable(int row, int column) {
	              return column == 2 || column == 3;
	            }


	        };

			buildTableModelBarre(modelBarres,mTableBarres);




		} catch (SQLException e) {
			// TODO Auto-generated catch block
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

	     	mTableGammes = new JTable();

	     	mTableGammes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	     	mTableGammes.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

			buildTableModelGamme(mModelGammes,mTableGammes,modelBarres,mNumBarre);





		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}





	    setActionListener();
		scrollPane_gamme.setViewportView(mTableGammes);
		setLayout(gl_panelCPO);
	}
	public void setText(String s) {
		textArea.setText(textArea.getText()+s);
	}

	private void  setActionListener() {
		  btnRun.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					LinkedHashMap<Integer,String> gammes=new LinkedHashMap <>();
					if(mTableBarres.getRowCount()<2) {
						JOptionPane.showMessageDialog(mCPO_IHM, "Minimum deux barres requises !","Tecal CPO", JOptionPane.ERROR_MESSAGE);
					}else {

						gammes.clear();
						for (int count = 0; count < mTableBarres.getRowCount(); count++){
							gammes.put((int)mTableBarres.getValueAt(count, 0),mTableBarres.getValueAt(count, 1).toString());
						}


						mCPO_IHM.run(gammes);

						//textArea.setText(mTecalOrdo.print());



					}


				}
			});

	}


	public   void buildTableModelBarre(DefaultTableModel inModelBarres,JTable inTableBarres)
	        throws SQLException {



	    // names of columns
	    Vector<String> columnNames = new Vector<>();

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
    	    @Override
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

	public  void buildTableModelGamme(DefaultTableModel model,JTable inmTableGammes,DefaultTableModel modelBarre,AtomicInteger numBarre)
	        throws SQLException {




	    inmTableGammes.setModel(model);

	    inmTableGammes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        inmTableGammes.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        inmTableGammes.addMouseListener(new MouseAdapter() {
    	    @Override
			public void mousePressed(MouseEvent mouseEvent) {
    	        JTable table =(JTable) mouseEvent.getSource();
    	        Point point = mouseEvent.getPoint();
    	        int row = table.rowAtPoint(point);
    	        row=table.convertRowIndexToModel(row);

    	        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
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

	public void setIconButton(JButton btnButton,String fileName) {
		try {

			InputStream in = getClass().getResourceAsStream("/"+fileName) ;
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
