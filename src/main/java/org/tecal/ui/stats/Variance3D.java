package org.tecal.ui.stats;
import processing.core.PApplet;
import processing.core.PVector;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;

import org.tecal.scheduler.data.SQL_DATA;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Variance3D extends PApplet {
    private List<PVector> points;
    private List<String> labels;
    private float angleX = 0, angleY = 0;

    private JDateChooser dateStartChooser, dateEndChooser;
    private JButton btnLoadData;
    private JFrame controlFrame;
    Connection conn ;

    public static void main(String[] args) {
        PApplet.main("org.tecal.ui.stats.Variance3D");
    }

    public Variance3D() {
    	conn = SQL_DATA.getInstance().getConnection();
       
    }
    public void settings() {
    	System.setProperty("jogl.disable.openglcore", "false");
    	  size(800, 600, "processing.opengl.PGraphics3D");
    	}
    public void setup() {
        setupGUI();
        points = new ArrayList<>();
        labels = new ArrayList<>();
        fetchData();
    }

    public void draw() {
        background(30);
        lights();

        translate(width / 2f, height / 2f, -200);
        rotateX(angleX);
        rotateY(angleY);

        drawAxis();
        drawPoints();
    }

    private void setupGUI() {
        controlFrame = new JFrame("Contrôles");
        controlFrame.setSize(400, 150);
        controlFrame.setLayout(new FlowLayout());

        dateStartChooser = new JDateChooser();
        dateEndChooser = new JDateChooser();
        btnLoadData = new JButton("Charger les données");

        Calendar cal = Calendar.getInstance();
        dateEndChooser.setDate(cal.getTime()); // Aujourd'hui
        cal.add(Calendar.MONTH, -1);
        dateStartChooser.setDate(cal.getTime()); // Un mois avant

        btnLoadData.addActionListener(e -> fetchData());

        controlFrame.add(new JLabel("Date début:"));
        controlFrame.add(dateStartChooser);
        controlFrame.add(new JLabel("Date fin:"));
        controlFrame.add(dateEndChooser);
        controlFrame.add(btnLoadData);
        controlFrame.setVisible(true);
    }

    private void drawAxis() {
        stroke(255, 0, 0);
        line(-100, 0, 0, 100, 0, 0); // X (Rouge)
        stroke(0, 255, 0);
        line(0, -100, 0, 0, 100, 0); // Y (Vert)
        stroke(0, 0, 255);
        line(0, 0, -100, 0, 0, 100); // Z (Bleu)
    }

    private void drawPoints() {
        textSize(12);
        fill(255);

        for (int i = 0; i < points.size(); i++) {
            PVector point = points.get(i);
            pushMatrix();
            translate(point.x * 50, -point.y * 50, point.z * 50);
            fill(255, 200, 0);
            noStroke();
            sphere(5);
            popMatrix();

            // Affichage des labels
            pushMatrix();
            translate(point.x * 50, -point.y * 50, point.z * 50);
            fill(255);
            text(labels.get(i), 10, 10);
            popMatrix();
        }
    }

    private void fetchData() {
        points.clear();
        labels.clear();

        
        String query = "SELECT P1.NomPoste AS libelleX, P2.NomPoste AS libelleY, " +
                "P1.NumPoste AS X, P2.NumPoste AS Y, " +
                "STDEV(D.TempsDeplacement + dbo.getOffset(C.vitesse_bas, C.vitesse_haut)) AS variance_t " +
                "FROM ANODISATION.dbo.DetailsFichesProduction D " +
                "INNER JOIN ANODISATION.dbo.DetailsChargesProduction C ON C.NumFicheProduction=D.NumFicheProduction AND C.NumLigne=1 " +
                "INNER JOIN ANODISATION.dbo.POSTES P1 ON P1.NumPoste = D.NumPostePrecedent " +
                "INNER JOIN ANODISATION.dbo.POSTES P2 ON P2.NumPoste = D.NumPoste " +
                "WHERE D.NumPoste NOT IN (1,2,0) AND D.NumPostePrecedent NOT IN (0,41,42) " +
                "AND D.TempsDeplacement < 60 AND ABS(D.NumPostePrecedent - D.NumPoste) < 20 " +
                "AND D.DateEntreePoste BETWEEN ? AND ? " +
                "GROUP BY P1.NumPoste, P2.NumPoste, P1.NomPoste, P2.NomPoste";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            java.sql.Date dateStart = new java.sql.Date(dateStartChooser.getDate().getTime());
            java.sql.Date dateEnd = new java.sql.Date(dateEndChooser.getDate().getTime());
            stmt.setDate(1, dateStart);
            stmt.setDate(2, dateEnd);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int x = rs.getInt("X");
                int y = rs.getInt("Y");
                float varianceT = rs.getFloat("variance_t");

                points.add(new PVector(x, y, varianceT));
                labels.add(rs.getString("libelleX") + " → " + rs.getString("libelleY"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void keyPressed() {
        if (key == 'a') angleY -= 0.1;
        if (key == 'd') angleY += 0.1;
        if (key == 'w') angleX -= 0.1;
        if (key == 's') angleX += 0.1;
    }
}