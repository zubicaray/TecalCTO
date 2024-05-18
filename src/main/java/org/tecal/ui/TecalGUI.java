package org.tecal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jfree.ui.RefineryUtilities;
import org.tecal.scheduler.CST;
import org.tecal.scheduler.GanttChart;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.Barre;

import com.formdev.flatlaf.FlatDarkLaf;


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
	//private JFrame mCPO_IHM;
	private CPO_IHM   mCPO_IHM;

	private DefaultTableModel modelVisuProd ;



	List<Image> mIcons;

	private SQL_DATA sqlCnx ;


	private JTextField textTEMPS_ZONE_OVERLAP_MIN;
	private JTextField textGAP_ZONE_NOOVERLAP;
	private JTextField textTEMPS_MVT_PONT_MIN_JOB;
	private JTextField textTEMPS_MVT_PONT;
	private JTextField textTEMPS_ANO_ENTRE_P1_P2;
	private JTextField textNUMZONE_DEBUT_PONT_2;
	private JTabbedPane tabbedPaneMain;
	private JTable tableOF;
	private JDatePickerImpl datePicker;
	JPanel panelVisuProd;
	JScrollPane scrollPaneVisuProd;

	private JTextField textTEMPS_MAX_SOLVEUR;
	private JTextField textEnd;
	private JTextField textStart;
	private  JTable tableTpsMvts;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		cosmeticGUI();

		EventQueue.invokeLater(new Runnable() {
			@Override
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

	public static void cosmeticGUI() {
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
	}

	/**
	 * Create the application.
	 */
	public TecalGUI() {

		sqlCnx = SQL_DATA.getInstance();
		initialize();

	}



	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {


		frmTecalOrdonnanceur = new JFrame();

		frmTecalOrdonnanceur.setTitle("Tecal PROD");

		mIcons=loadIcons(this);



		frmTecalOrdonnanceur.setIconImages(mIcons);

		//mCPO_IHM = new CPO(mIcons);

		frmTecalOrdonnanceur.setBounds(100, 100, 729, 674);
		frmTecalOrdonnanceur.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		tabbedPaneMain = new JTabbedPane(SwingConstants.TOP);
		GroupLayout groupLayout = new GroupLayout(frmTecalOrdonnanceur.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPaneMain, GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPaneMain, GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
		);





		//scrollPane_gamme.setViewportView(tableGammes);
		//panelCPO.setLayout(gl_panel);
		buildVisuProd();
		buildMvtPonts();
		buildParamsTab(tabbedPaneMain);

		frmTecalOrdonnanceur.getContentPane().setLayout(groupLayout);
	}

	public static  List<Image>  loadIcons( Object o) {
		List<Image>  lIcons = new ArrayList<>();
		try {

			InputStream in = o.getClass().getResourceAsStream("/gantt-chart 16.png") ;
			BufferedImage someImage = ImageIO.read(in);
			ImageIcon img;
			img = new ImageIcon(someImage);
			lIcons.add(img.getImage());
			in = o.getClass().getResourceAsStream("/gantt-chart 32.png") ;
			someImage = ImageIO.read(in);
			img = new ImageIcon(someImage);
			lIcons.add(img.getImage());




		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} // usable in Java 9+

		return lIcons;
	}


	private void  buildVisuProd() {

		panelVisuProd = new JPanel(new BorderLayout());





		UtilDateModel model = new UtilDateModel();

		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
		// Don't know about the formatter, but there it is...
		datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

		datePicker.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					buildTableModelVisuProd();
				} catch (SQLException e1) {
					JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		});

		tabbedPaneMain.addTab("Visuel de prod", null, panelVisuProd, null);


		JPanel buttonsDate = new JPanel(new FlowLayout());
		buttonsDate.add(datePicker);
		JButton btnRunVisuProd = new JButton("Gantt PROD");
		buttonsDate.add(btnRunVisuProd);
		JButton btnGanttCpo = new JButton("Gantt CPO");
		buttonsDate.add(btnGanttCpo);


		panelVisuProd.add(buttonsDate, BorderLayout.PAGE_START);



		scrollPaneVisuProd = new JScrollPane();
		scrollPaneVisuProd.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
			}
		});
		panelVisuProd.add(scrollPaneVisuProd, BorderLayout.CENTER);




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
				 final GanttChart ganttTecal = new GanttChart("Prod du "+d);
				 ganttTecal.prod_diag(lOF,d);
				 ganttTecal.pack();
				 ganttTecal.setSize(new java.awt.Dimension(1500, 870));
			     RefineryUtilities.centerFrameOnScreen(ganttTecal);
			     ganttTecal.setVisible(true);
			     ganttTecal.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			     frmTecalOrdonnanceur.setCursor(Cursor.getDefaultCursor());




			}
		});


		btnGanttCpo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				 int[] sel = tableOF.getSelectedRows();
				 if(sel.length==0) {
					 JOptionPane.showMessageDialog(frmTecalOrdonnanceur, "Pas d'OF choisi !","Visuel CPO", JOptionPane.ERROR_MESSAGE);
					 return;
				 }


				 LinkedHashMap<Integer,Barre> barres=new LinkedHashMap <>();
				 for(int i=0;i<sel.length;i++) {
					 Barre b=new Barre( 
							 i+1,
							 tableOF.getModel().getValueAt(sel[i], 1).toString(),
							 0,
							 0,
							 false);
					 
					 barres.put(b.idbarre,b);
				 }




				int [] params={
						Integer.valueOf(textTEMPS_ZONE_OVERLAP_MIN.getText()),
						Integer.valueOf(textTEMPS_MVT_PONT_MIN_JOB.getText()),
						Integer.valueOf(textGAP_ZONE_NOOVERLAP.getText()),
						Integer.valueOf(textTEMPS_MVT_PONT.getText()),
						Integer.valueOf(textTEMPS_ANO_ENTRE_P1_P2.getText()),
						Integer.valueOf(textTEMPS_MAX_SOLVEUR.getText())};




				//mTecalOrdo.setBarres(gammes);
				mCPO_IHM=new CPO_IHM(params);
				frmTecalOrdonnanceur.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				//mTecalOrdo.run(mCPO_IHM);
				mCPO_IHM.run(barres);
				frmTecalOrdonnanceur.setCursor(Cursor.getDefaultCursor());
				mCPO_IHM.setVisible(true);

			}
		});




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

	private void filterMvts() {
		List<RowFilter<Object,Object>> filters = new ArrayList<>(2);
		filters.add(RowFilter.regexFilter(textStart.getText(), 1));
		filters.add(RowFilter.regexFilter(textEnd.getText(), 3));
		RowFilter<Object, Object> rf = RowFilter.andFilter(filters);

		TableRowSorter<TableModel> sorter = new TableRowSorter<>((tableTpsMvts.getModel()));
	    sorter.setRowFilter(rf);

	    tableTpsMvts.setRowSorter(sorter);
	}

	private void buildMvtPonts() {
		JPanel panelMvtPonts = new JPanel(new BorderLayout());
		tabbedPaneMain.addTab("Temps mouvements", null, panelMvtPonts, null);


		textStart = new JTextField();
		textStart.setColumns(10);

		textEnd = new JTextField();
		textEnd.setColumns(10);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		buttonsPanel.add(textStart);
		buttonsPanel.add(textEnd);


		JScrollPane scrollMvtsTable = new JScrollPane();


		 panelMvtPonts.add(buttonsPanel, BorderLayout.PAGE_START);
		 panelMvtPonts.add(scrollMvtsTable, BorderLayout.CENTER);

		 tableTpsMvts = new JTable();

		 try {
			buildTableModelMvts();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		 //scrollMvtsTable.add(tableTpsMvts);
		 scrollMvtsTable.setViewportView(tableTpsMvts);



		textStart.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {


				filterMvts();
			}
		});




		textEnd.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				filterMvts();
			}


		});







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



	public   void buildTableModelMvts()
	        throws SQLException {


		ResultSet rs =sqlCnx.getTpsMvts();
	    ResultSetMetaData metaData = rs.getMetaData();

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

	    DefaultTableModel modelMvts = new DefaultTableModel(data, columnNames) {

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

    	tableTpsMvts.setModel(modelMvts);
    	TableColumnModel tcm = tableTpsMvts.getColumnModel();
    	tcm.removeColumn( tcm.getColumn(0) );
    	tcm.removeColumn( tcm.getColumn(1) );



		tableTpsMvts.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);




	}



	public   void buildTableModelVisuProd()
	        throws SQLException {

		java.util.Date d=(java.util.Date) datePicker.getModel().getValue();
		ResultSet rs =sqlCnx.getVisuProd(d);
	    ResultSetMetaData metaData = rs.getMetaData();

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

	    modelVisuProd = new DefaultTableModel(data, columnNames) {

			private static final long serialVersionUID = 1L;

			@Override
    	    public boolean isCellEditable(int row, int column) {
    	       //all cells false
    	       return false;
    	    }
    	};

    	tableOF = new JTable(modelVisuProd) {


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
		//scrollPaneVisuProd.setViewportView(tableOF);
		//panelVisuProd.setLayout(gl_panelVisuProd);
		tableOF.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);



		scrollPaneVisuProd.setViewportView(tableOF);

		tableOF.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){

				private static final long serialVersionUID = 1L;

				@Override
	            public Component getTableCellRendererComponent(JTable table,
	                    Object value, boolean isSelected, boolean hasFocus, int row, int col) {
	                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
	                String gamme = (String)table.getModel().getValueAt(row, 1);

	                if (SQL_DATA.getInstance().getMissingTimeMovesGammes().contains(gamme)) {
	                    setBackground(Color.RED);
	                    setForeground(Color.BLACK);
	                }
	                else {
	                    setBackground(table.getBackground());
	                    setForeground(table.getForeground());
	                }

	                return this;
	            }


	        });


		 final JPopupMenu popupMenu = new JPopupMenu();
	        JMenuItem item = new JMenuItem("Etalonnage  des mouvements");
	        item.addActionListener(new ActionListener() {

	            @Override
	            public void actionPerformed(ActionEvent e) {
	            	int row = tableOF.getSelectedRow();



	            	if(row>=0) {



	            		//JOptionPane.showMessageDialog(frmTecalOrdonnanceur, "Right-click gamme="+tableOF.getModel().getValueAt(row, 1).toString());
	            		int result = JOptionPane.showConfirmDialog((Component) null, "Voulez-vous aussi écraser les valeurs non nulles?","alert", JOptionPane.YES_NO_CANCEL_OPTION);

	            		if(result!=2) {
	            			boolean b = (result == 0);
	            			if(!SQL_DATA.getInstance().updateTpsMvts(tableOF.getModel().getValueAt(row, 0).toString(), b)){
	            				//MAJ des gammes
	            				SQL_DATA.getInstance().setMissingTimeMovesGammes();
	            			}
	            		}

	            	}
	            }
	        });
	        popupMenu.add(item);
	        tableOF.setComponentPopupMenu(popupMenu);

	}

}
