package PlcReadWrite;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.github.s7connector.api.DaveArea;
import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.github.s7connector.exception.S7Exception;

public class PlcDisplay extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField dbFieldRead, offsetFieldRead;
    private JTextField dbFieldWrite, offsetFieldWrite, valueFieldWrite;
    private JLabel readLabel, writeResultLabel;
    private S7Connector connector;

    public PlcDisplay() {
        // Configure la fenêtre principale
        setTitle("Test connexion S7-300 Tecal");
        setSize(500, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Adresse IP de l'automate S7-300
        String plcIpAddress = "192.168.0.1";
        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialise le connecteur PLC avec gestion d'erreur
        try {
            connector = S7ConnectorFactory.buildTCPConnector()
                .withHost(plcIpAddress)
                .withRack(0) // optionnel
                .withSlot(2) // optionnel
                .build();
        } catch (S7Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            //return;
        }

        // Onglets de l'application
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Onglet READ
        JPanel readPanel = createReadPanel();
        tabbedPane.add("READ", readPanel);

        // Onglet WRITE
        JPanel writePanel = createWritePanel();
        tabbedPane.add("WRITE", writePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createReadPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        // Champs de saisie pour le datablock et l'offset
        dbFieldRead = new JTextField("3", 5);       // Par défaut, DB = 3
        offsetFieldRead = new JTextField("15", 5);  // Par défaut, Offset = 15
        JLabel dbLabel = new JLabel("Datablock (DB): ");
        JLabel offsetLabel = new JLabel("Offset: ");
        panel.add(dbLabel);
        panel.add(dbFieldRead);
        panel.add(offsetLabel);
        panel.add(offsetFieldRead);

        // Label pour afficher la valeur lue
        readLabel = new JLabel("Valeur : ");
        readLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(readLabel);

        // Création d'un timer pour mettre à jour la valeur toutes les 2 secondes
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (connector == null) {
                    SwingUtilities.invokeLater(() -> readLabel.setText("Erreur de connexion au PLC."));
                    return;
                }
                try {
                    int dbNumber = Integer.parseInt(dbFieldRead.getText().trim());
                    int offset = Integer.parseInt(offsetFieldRead.getText().trim());

                    // Lit 2 octets depuis le datablock et l'offset spécifiés
                    byte[] data = connector.read(DaveArea.DB, dbNumber, offset, 2);
                    short value = (short) ((data[0] << 8) | (data[1] & 0xFF));
                    SwingUtilities.invokeLater(() -> readLabel.setText("Valeur : " + value));
                } catch (S7Exception e) {
                    SwingUtilities.invokeLater(() -> readLabel.setText("Erreur de lecture : " + e.getMessage()));
                }
            }
        }, 0, 2000); // 2000 ms = 2 secondes

        return panel;
    }

    private JPanel createWritePanel() {
        JPanel panel = new JPanel(new FlowLayout());

        // Champs de saisie pour le datablock, l'offset, et la valeur à écrire
        dbFieldWrite = new JTextField("3", 5);          // Par défaut, DB = 3
        offsetFieldWrite = new JTextField("15", 5);     // Par défaut, Offset = 15
        valueFieldWrite = new JTextField("0", 5);       // Par défaut, valeur = 0
        JLabel dbLabel = new JLabel("Datablock: ");
        JLabel offsetLabel = new JLabel("Offset: ");
        JLabel valueLabel = new JLabel("Valeur à écrire : ");

        panel.add(dbLabel);
        panel.add(dbFieldWrite);
        panel.add(offsetLabel);
        panel.add(offsetFieldWrite);
        panel.add(valueLabel);
        panel.add(valueFieldWrite);

        // Bouton pour déclencher l'écriture
        JButton writeButton = new JButton("Écrire");
        writeResultLabel = new JLabel();
        writeResultLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(writeButton);
        panel.add(writeResultLabel);

        // Action du bouton
        writeButton.addActionListener(e -> {
            if (connector == null) {
                writeResultLabel.setText("Erreur de connexion au PLC.");
                return;
            }
            try {
                int dbNumber = Integer.parseInt(dbFieldWrite.getText().trim());
                int offset = Integer.parseInt(offsetFieldWrite.getText().trim());
                short value = Short.parseShort(valueFieldWrite.getText().trim());

                // Convertit la valeur en 2 octets
                byte[] data = new byte[2];
                data[0] = (byte) (value >> 8);
                data[1] = (byte) (value & 0xFF);

                // Écrit les données dans le datablock et l'offset spécifiés
                connector.write(DaveArea.DB, dbNumber, offset, data);
                
                writeResultLabel.setText("Écriture réussie !");
            } catch (S7Exception ex) {
                writeResultLabel.setText("Erreur d'écriture : " + ex.getMessage());
            }
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PlcDisplay frame = new PlcDisplay();
            frame.setVisible(true);
        });
    }
}
