package mx.unison.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import mx.unison.vistas.Navigation;
import mx.unison.MainApp;

public class HomeController {

    @FXML private Button btnProductos;
    @FXML private Button btnAlmacenes;
    @FXML private Button btnCerrarSesion;

    @FXML
    public void initialize() {
        // Preferencia: Usa Navigation.loadView o MainApp.setRoot, según tu estructura.
        btnAlmacenes.setOnAction(e -> {
            // Navega a Almacenes
            // Navigation.loadView("/mx/unison/view/AlmacenesView.fxml", "Almacenes");
            MainApp.setRoot("/mx/unison/view/AlmacenesView.fxml");
        });

        btnProductos.setOnAction(e -> {
            // Navega a Productos
            // Navigation.loadView("/mx/unison/view/ProductosView.fxml", "Productos");
            MainApp.setRoot("/mx/unison/view/ProductosView.fxml");
        });

        btnCerrarSesion.setOnAction(e -> {
            // Regresa al Login
            // Navigation.loadView("/mx/unison/view/LoginView.fxml", "Iniciar sesión");
            MainApp.setRoot("/mx/unison/view/LoginView.fxml");
        });
    }
}