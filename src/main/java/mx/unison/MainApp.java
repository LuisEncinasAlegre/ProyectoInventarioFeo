package mx.unison;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mx.unison.modelos.DatabaseHelper;

public class MainApp extends Application {

    private static Stage mainStage; // Referencia central para gestión de escenas

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicializa la base de datos y sus tablas
        new DatabaseHelper();

        mainStage = primaryStage;
        mainStage.setTitle("Sistema de Inventario");

        // Carga la vista inicial (Login)
        Parent root = FXMLLoader.load(getClass().getResource("/mx/unison/view/LoginView.fxml"));
        Scene scene = new Scene(root);
        // Si tienes un CSS global:
        scene.getStylesheets().add(getClass().getResource("/mx/unison/view/dark-theme.css").toExternalForm());
        mainStage.setScene(scene);
        mainStage.show();
    }

    /**
     * Cambia la escena actual (Root) del Stage principal.
     * @param fxmlPath Ruta del FXML desde resources, por ejemplo "/mx/unison/view/HomeView.fxml"
     */
    public static void setRoot(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource(fxmlPath));
            mainStage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            // Maneja error (puedes mostrar un Alert)
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}