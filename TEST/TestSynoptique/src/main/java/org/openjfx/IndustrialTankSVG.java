package org.openjfx;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class IndustrialTankSVG extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Définir une variable pour l'échelle globale
        double scale = 1; // Ajustez cette valeur pour changer la taille globale

       
        
        // Créer un SVGPath pour la forme de la cuve avec l'échelle appliquée
        SVGPath tankSVG = new SVGPath();
        tankSVG.setContent(
            "M " + (50 * scale) + " 0 " +
            "L " + (150 * scale) + " 0 " +
            "Q " + (170 * scale) + " " + (20 * scale) + " " + (170 * scale) + " " + (40 * scale) + " " +
            "L " + (170 * scale) + " " + (150 * scale) + " " +
            "Q " + (170 * scale) + " " + (170 * scale) + " " + (150 * scale) + " " + (170 * scale) + " " +
            "L " + (50 * scale) + " " + (170 * scale) + " " +
            "Q " + (30 * scale) + " " + (170 * scale) + " " + (30 * scale) + " " + (150 * scale) + " " +
            "L " + (30 * scale) + " " + (40 * scale) + " " +
            "Q " + (30 * scale) + " " + (20 * scale) + " " + (50 * scale) + " 0 Z"
        );
        tankSVG.setFill(Color.LIGHTGRAY);
        tankSVG.setStroke(Color.DARKGRAY);
        tankSVG.setStrokeWidth(2 * scale); // Adapter l'épaisseur du contour à l'échelle

        // Ajouter le texte "C17" en dessous de la cuve
        Text label = new Text("C17");
        label.setFont(new Font("Arial", 24 * scale)); // Ajuster la taille de la police à l'échelle
        label.setFill(Color.BLACK);
       // Ajuster la position verticale du texte
        label.setTranslateY(200 * scale); 
        label.setTranslateX(80*scale); 
        Group tankGroup  = new Group();
        tankGroup.getChildren().addAll(tankSVG, label);
        tankGroup.setTranslateX(100); 
        

        // Créer une charge suspendue au chariot
        Rectangle load = new Rectangle(40 * scale, 60 * scale, Color.ORANGE);
        load.setTranslateY(-20 * scale); // Ajuster la position verticale
        load.setStroke(Color.BLACK);
        load.setAccessibleText(STYLESHEET_CASPIAN);
       
        Group bridgeGroup = createBrige();
        
        bridgeGroup.setScaleX(scale);
        bridgeGroup.setScaleY(scale);
        bridgeGroup.setTranslateY(-200 * scale);

        // Créer une StackPane pour centrer tous les éléments
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(bridgeGroup, tankGroup,load);

        // Créer une scène et afficher la fenêtre
        Scene scene = new Scene(stackPane, 300 * scale, 500 * scale);
        primaryStage.setTitle("Cuve Industrielle avec Pont Roulant");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static Group createBrige() {
        Group group = new Group();

        SVGPath path1 = new SVGPath();
        path1.setContent("m 248.77515,-47.934171 c 32.02321,-9.144966 63.31561,-9.390411 93.82089,-0.05089 1.68737,0.551347 1.81424,0.01526 1.90635,2.37254 l -0.44439,119.11468 18.24726,-0.113433 c 0,0 1.20513,-0.0125 1.67305,0.453645 0.43004,0.428416 0.31356,1.365598 0.31356,1.365598 l 0.33942,37.914721 -138.57347,-0.16167 -0.10109,-38.087182 c 0,0 -0.0424,-0.83668 0.30875,-1.191802 0.4361,-0.441036 1.53734,-0.309661 1.53734,-0.309661 l 19.94392,0.0573 -0.20497,-120.223086 c 0.13919,-0.798153 0.43816,-0.853929 1.23338,-1.14076 z");
       
        path1.setFill(Color.LIGHTGRAY);
        path1.setStroke(Color.BLACK);

        SVGPath path2 = new SVGPath();
        path2.setContent("m 232.82317,81.301769 125.45045,0.0316 -0.18377,24.748811 -125.24171,-0.12013 z");
               path2.setFill(Color.RED);
        path2.setStroke(Color.BLACK);

        SVGPath path3 = new SVGPath();
        path3.setContent("m 256.41159,-39.527299 c 28.23855,-8.11711 54.76551,-8.906549 79.02196,0.02444 L 335.1676,73.536252 c -25.08245,-0.04238 -50.00823,-0.09247 -78.92862,0.0539 z");
        path3.setFill(Color.LIGHTGRAY);
        path3.setStroke(Color.BLACK);

        SVGPath path4 = new SVGPath();
        path4.setContent("m 321.63287,-48.687409 c 2.62092,-0.656531 5.36382,-0.689358 8.26809,0.102961 l -0.094,182.365418 c -3.10937,0.72501 -5.82143,0.68988 -8.21641,0.0481 z");
  
        path4.setFill(Color.LIGHTGRAY);
        path4.setStroke(Color.BLACK);
        
        SVGPath path5 = new SVGPath();
        path5.setContent("m 261.66571,-48.668545 c 2.65286,-0.649377 5.36204,-0.925458 8.26809,0.102961 l -0.094,182.365424 c -2.98499,0.98123 -5.67623,0.81076 -8.21641,0.0481 z");

        path5.setFill(Color.LIGHTGRAY);
        path5.setStroke(Color.BLACK);

        group.getChildren().addAll(path1, path2, path3, path4, path5);

        return group;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
