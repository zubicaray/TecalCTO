package org.tecal.ui;import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

public class TestGui extends JPanel {

final JTable table = new JTable(new MyTableModel());

public TestGui() {
    initializePanel();
}

private void initializePanel() {
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(475, 150));


    table.setFillsViewportHeight(true);
    JScrollPane pane = new JScrollPane(table);

    JLabel label2 = new JLabel("Row: ");
    final JTextField field2 = new JTextField(3);
    JButton add = new JButton("Select");

    table.setRowSelectionAllowed(true);
    table.setColumnSelectionAllowed(false);
    table.getSelectionModel().addListSelectionListener(new ListSelectionListenerImpl());
    TableColumn tc = table.getColumnModel().getColumn(3);
    tc.setCellEditor(table.getDefaultEditor(Boolean.class));
    tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));
    ((JComponent) table.getDefaultRenderer(Boolean.class)).setOpaque(true);
    tc.getCellEditor().addCellEditorListener(new CellEditorListenerImpl());

    add.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent event) {
            int index2 = 0;
            try {
                index2 = Integer.valueOf(field2.getText());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            table.addRowSelectionInterval(index2, index2);
            field2.setText(String.valueOf(index2));
        }
    });

    JPanel command = new JPanel(new FlowLayout());
    command.add(label2);
    command.add(field2);
    command.add(add);

    add(pane, BorderLayout.CENTER);
    add(command, BorderLayout.SOUTH);
}


public static void showFrame() {
    JPanel panel = new TestGui();
    panel.setOpaque(true);

    JFrame frame = new JFrame("JTable Row Selection");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setContentPane(panel);
    frame.pack();
    frame.setVisible(true);
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {

        public void run() {
            TestGui.showFrame();
        }
    });
}

public class MyTableModel extends AbstractTableModel {

    private String[] columns = {"ID", "NAME", "AGE", "A STUDENT?"};
    private Object[][] data = {
        {1, "Alice", 20, new Boolean(false)},
        {2, "Bob", 10, new Boolean(false)},
        {3, "Carol", 15, new Boolean(false)},
        {4, "Mallory", 25, new Boolean(false)}
    };

    
    
    public int getRowCount() {
        return data.length;
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }
    
    

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 3;
    }

    //
    // This method is used by the JTable to define the default
    // renderer or editor for each cell. For example if you have
    // a boolean data it will be rendered as a check box. A
    // number value is right aligned.
    //
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return data[0][columnIndex].getClass();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 3) {
            data[rowIndex][columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}

class ListSelectionListenerImpl implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent lse) {
        ListSelectionModel lsm = (ListSelectionModel) lse.getSource();
        int row = table.getRowCount();
        if (lsm.isSelectionEmpty()) {
        } else {
//                If any column is clicked other than checkbox then do normal selection
//                i.e select the click row and deselects the previous selection
            if (table.getSelectedColumn() != 3) {
                for (int i = 0; i < row; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        table.setValueAt(true, i, 3);
                    } else {
                        table.setValueAt(false, i, 3);
                    }
                }

            }
        }
    }
  }
public class CellEditorListenerImpl implements CellEditorListener{

    public void editingStopped(ChangeEvent e) {
        for(int i=0; i<table.getRowCount();i++){
            if((Boolean)table.getValueAt(i, 3)){
                table.addRowSelectionInterval(i, i);
            }
            else{
                table.removeRowSelectionInterval(i, i);
            }
        }
    }

    public void editingCanceled(ChangeEvent e) {
        System.out.println("do nothing");
    }
    

}
}