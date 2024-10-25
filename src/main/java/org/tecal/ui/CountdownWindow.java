package org.tecal.ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CountdownWindow extends JFrame {
    private static final long serialVersionUID = 1L;
	private JLabel countdownLabel;
    private int countdown = 60; // Durée en secondes (1 minute)
    private Timer timer;

    public CountdownWindow(int barre) {
        setTitle("Décompte de la barre: "+barre);
        countdownLabel = new JLabel("Temps restant : " + countdown + " secondes", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(countdownLabel, BorderLayout.CENTER);

        // Configuration du timer pour mettre à jour toutes les secondes
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--;
                countdownLabel.setText("Chargement dans " + countdown + " secondes");
                if (countdown <= 0) {
                    timer.stop();
                    dispose(); // Fermer la fenêtre
                }
            }
        });

        setSize(350, 120);
        setLocationRelativeTo(null); // Centrer la fenêtre sur l'écran
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public void startCountdown() {
        timer.start();
        setVisible(true);
    }

    public static void main(String[] args) {
        // Lancer la fenêtre de décompte sans bloquer la fenêtre appelante
        CountdownWindow countdownWindow = new CountdownWindow(1);
        countdownWindow.startCountdown();
        
        // Exemple de fenêtre appelante indépendante
        JFrame mainFrame = new JFrame("Fenêtre principale");
        mainFrame.setSize(400, 200);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}
