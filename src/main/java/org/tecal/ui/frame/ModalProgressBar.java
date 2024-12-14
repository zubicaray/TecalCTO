package org.tecal.ui.frame;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ModalProgressBar {

    private JDialog dialog;
    private JProgressBar progressBar;
    private Timer timer;
    private int duration;

    public ModalProgressBar(int durationInSeconds) {
        this.duration = durationInSeconds * 1000; // Convertir en millisecondes
        
    }

    public void createAndShowDialog() {
        // Créer un JDialog modal
        dialog = new JDialog((Frame) null, "Veuillez patienter", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setUndecorated(true); // Supprimer la décoration (barre de titre)
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 20);
        dialog.setLocationRelativeTo(null);

        // Ajouter une barre de progression
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        dialog.add(progressBar, BorderLayout.CENTER);

      

        // Mettre à jour la progression
        updateProgressBar();

        // Afficher le JDialog
        dialog.setVisible(true);
    }

    private void updateProgressBar() {
        timer = new Timer();
        int interval = 100; // Intervalle d'actualisation (en ms)
        int steps = duration / interval;
        TimerTask task = new TimerTask() {
            int progress = 0;

            @Override
            public void run() {
                progress++;
                progressBar.setValue(progress * 100 / steps);

                if (progress >= steps) {
                    timer.cancel();
                    dialog.dispose();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, interval);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
        dialog.dispose();
    }

    // Exemple d'utilisation
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ModalProgressBar progressBar = new ModalProgressBar(10); // Durée : 10 secondes

            progressBar.createAndShowDialog();
            // Exemple d'arrêt de la barre après 3 secondes
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    progressBar.stop();
                }
            }, 3000);
        });
    }
}
