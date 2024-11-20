package PlcReadWrite;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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

import com.github.xingshuangs.iot.exceptions.S7CommException;
import com.github.xingshuangs.iot.protocol.s7.enums.EPlcType;
import com.github.xingshuangs.iot.protocol.s7.service.S7PLC;
import com.github.xingshuangs.iot.utils.HexUtil;

public class xingshuang extends JFrame implements WindowListener{
	private static final long serialVersionUID = 1L;
	private JTextField dbFieldRead;
	private JTextField dbFieldWrite,valueFieldWrite;
	private JLabel readLabel, writeResultLabel;
	private S7PLC s7PLC ;

	public xingshuang() {
		// Configure la fenêtre principale
		setTitle("Test connexion S7-300 Tecal");
		setSize(500, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// Adresse IP de l'automate S7-300
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Initialise le connecteur PLC avec gestion d'erreur
		try {
			String plcIpAddress = "192.168.0.1";
			s7PLC = new S7PLC(EPlcType.S300, plcIpAddress);
			s7PLC.connect();
			
			s7PLC.setComCallback((tag, bytes) -> System.out.printf("%s[%d] %s%n", tag, bytes.length, HexUtil.toHexString(bytes)));
			if(s7PLC.checkConnected() ==false) {
				JOptionPane.showMessageDialog(this, "Erreur de connexion ", "Erreur"+s7PLC.getComCallback().toString(),
						JOptionPane.ERROR_MESSAGE);
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
		

		} catch (S7CommException e) {
			JOptionPane.showMessageDialog(this, "Erreur de connexion : " + e.getMessage(), "Erreur",
					JOptionPane.ERROR_MESSAGE);
			// return;
		}
	}
		

	private JPanel createReadPanel() {
		JPanel panel = new JPanel(new FlowLayout());

		// Champs de saisie pour le datablock et l'offset
		dbFieldRead = new JTextField("DB15.2.0",5); // Par défaut, DB = 3
		
		JLabel dbLabel = new JLabel("adresse: ");
		
		panel.add(dbLabel);
		panel.add(dbFieldRead);
		
		// Label pour afficher la valeur lue
		readLabel = new JLabel("Valeur : ");
		readLabel.setFont(new Font("Arial", Font.BOLD, 20));
		panel.add(readLabel);

		// Création d'un timer pour mettre à jour la valeur toutes les 2 secondes
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (s7PLC == null || !s7PLC.checkConnected()) {
					SwingUtilities.invokeLater(() -> readLabel.setText("Erreur de connexion au PLC."));
					return;
				}
				try {
					String adress = dbFieldRead.getText().trim();
				
					// Lit 2 octets depuis le datablock et l'offset spécifiés
					int data = s7PLC.readInt16(adress);
					
					SwingUtilities.invokeLater(() -> readLabel.setText("Valeur : " + data));
				} catch (S7CommException e) {
					SwingUtilities.invokeLater(() -> readLabel.setText("Erreur de lecture : " + e.getMessage()));
				}
			}
		}, 0, 2000); // 2000 ms = 2 secondes

		return panel;
	}

	private JPanel createWritePanel() {
		JPanel panel = new JPanel(new FlowLayout());

		// Champs de saisie pour le datablock, l'offset, et la valeur à écrire
		dbFieldWrite = new JTextField("DB15.2.0", 5); // Par défaut, DB = 3
		
		valueFieldWrite = new JTextField("0", 5); // Par défaut, valeur = 0
		JLabel dbLabel = new JLabel("Datablock: ");
		
		JLabel valueLabel = new JLabel("Valeur à écrire : ");

		panel.add(dbLabel);
		panel.add(dbFieldWrite);	
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
			if (s7PLC == null) {
				writeResultLabel.setText("Erreur de connexion au PLC.");
				return;
			}
			try {
				String adress = dbFieldWrite.getText().trim();			

				short value = Short.parseShort(valueFieldWrite.getText().trim());
				// Écrit les données dans le datablock et l'offset spécifiés
				s7PLC.writeInt32(adress,value );

				writeResultLabel.setText("Écriture réussie !");
			} catch (S7CommException ex) {
				writeResultLabel.setText("Erreur d'écriture : " + ex.getMessage());
			}
		});

		return panel;
	}
	@Override
	  public void windowClosing(WindowEvent e)
	  {/* can do cleanup here if necessary */}

	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        	xingshuang frame = new xingshuang();
            frame.setVisible(true);
        });
    }

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
