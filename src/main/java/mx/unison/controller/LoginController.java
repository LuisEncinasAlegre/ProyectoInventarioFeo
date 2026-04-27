package mx.unison.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import mx.unison.modelos.*;
import mx.unison.vistas.Navigation;

import java.security.MessageDigest;

public class LoginController {
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private UsuarioDAO usuarioDAO;

    @FXML
    public void initialize() {
        try {
            DatabaseHelper dbh = new DatabaseHelper();
            usuarioDAO = new UsuarioDAOImpl(dbh.getConnectionSource());
        } catch (Exception e) {
            showError("Error al conectar con la base de datos.");
            loginButton.setDisable(true);
            return;
        }

        loginButton.setOnAction(e -> login());
        // permite usar ENTER
        passField.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.ENTER) login();
        });
        userField.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.ENTER) passField.requestFocus();
        });
    }

    private void login() {
        String username = userField.getText().trim();
        String password = passField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor ingresa usuario y contraseña.");
            return;
        }

        try {
            Usuario user = usuarioDAO.buscarPorNombre(username);
            if (user != null && md5(password).equalsIgnoreCase(user.password)) {
                errorLabel.setVisible(false);
                // Guardar último login, etc...
                Navigation.loadView("/mx/unison/view/HomeView.fxml", "Inicio");
            } else {
                showError("Credenciales inválidas.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ha ocurrido un error al autenticar.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    // MD5 hash. Usar igual que el proyecto original.
    public static String md5(String in) {
        if (in == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(in.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}