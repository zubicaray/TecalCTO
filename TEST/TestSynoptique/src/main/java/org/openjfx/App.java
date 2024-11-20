package org.openjfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Couleur de remplissage de la cuve en code hexadécimal
        String cuveColorHex = "#E42828"; // Rouge foncé
        
        // Création de la forme de la cuve
        Rectangle cuveBody = new Rectangle(100, 150);
        cuveBody.setArcWidth(15);  // Coins arrondis
        cuveBody.setArcHeight(15 );
        cuveBody.setFill(Color.web(cuveColorHex)); // Utilisation de la couleur hexadécimale
        cuveBody.setStroke(Color.BLACK);
        
        // Création de la partie supérieure arrondie de la cuve
        Ellipse topEllipse = new Ellipse(50, 10);
        topEllipse.setFill(Color.GRAY); // Même couleur que le corps de la cuve
        topEllipse.setStroke(Color.BLACK);
        topEllipse.setTranslateY(-70);  // Ajuster pour être au-dessus du corps

        // Texte sous la cuve
        Text label = new Text("C17"); // Mise à jour du texte
        label.setFont(Font.font(20));
        label.setTranslateY(90);

        // Création du symbole "interdit" (cercle et deux lignes croisées)
        Circle interditCircle = new Circle(30);
        interditCircle.setFill(Color.WHITE); // Cercle vide
        interditCircle.setStroke(Color.BLACK); // Bordure rouge

        Line interditLine1 = new Line(-21, -21, 21, 21); // Première ligne diagonale
        interditLine1.setStroke(Color.BLACK);
        interditLine1.setStrokeWidth(4);

        Line interditLine2 = new Line(-21, 21, 21, -21); // Deuxième ligne diagonale croisée
        interditLine2.setStroke(Color.BLACK);
        interditLine2.setStrokeWidth(4);

        // Empilement des éléments pour former l'image complète
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(cuveBody, topEllipse, interditCircle, interditLine1, interditLine2, label);

        // Configuration de la scène
        Scene scene = new Scene(stackPane, 200, 250);
        primaryStage.setTitle("Cuve avec symbole interdit et étiquette");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
