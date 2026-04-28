package mx.unison.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import mx.unison.modelos.*;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class AlmacenesController {

    @FXML private TableView<Almacen> tableView;
    @FXML private TableColumn<Almacen, Integer> colId;
    @FXML private TableColumn<Almacen, String>  colNombre;
    @FXML private TableColumn<Almacen, String>  colUbicacion;
    @FXML private TableColumn<Almacen, String>  colCreado;
    @FXML private TableColumn<Almacen, String>  colUltMod;
    @FXML private TableColumn<Almacen, String>  colUltUsuario;
    @FXML private Button backButton, addButton, editButton, deleteButton;

    private final ObservableList<Almacen> almacenes = FXCollections.observableArrayList();
    private AlmacenDAO almacenDAO;

    public void initialize() {
        try {
            DatabaseHelper dbh = new DatabaseHelper();
            almacenDAO = new AlmacenDAOImpl(dbh.getConnectionSource());
        } catch (Exception e) {
            showError("Error de base de datos: " + e.getMessage());
        }

        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().id));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().nombre));
        colUbicacion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().ubicacion));
        colCreado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().fechaHoraCreacion));
        colUltMod.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().fechaHoraUltimaMod));
        colUltUsuario.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().ultimoUsuario));

        tableView.setItems(almacenes);
        reloadData();

        // Button actions
        backButton.setOnAction(e -> regresarInicio());

        addButton.setOnAction(e -> openEditor(null));
        editButton.setOnAction(e -> {
            Almacen sel = tableView.getSelectionModel().getSelectedItem();
            if (sel != null) openEditor(sel);
        });

        deleteButton.setOnAction(e -> {
            Almacen sel = tableView.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            var r = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que desea eliminar el almacén?", ButtonType.YES, ButtonType.NO);
            r.setHeaderText("Confirmar eliminación");
            r.initOwner(tableView.getScene().getWindow());
            r.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    try {
                        almacenDAO.eliminar(sel);
                        reloadData();
                    } catch (Exception ex) {
                        showError("No fue posible eliminar el almacén: " + ex.getMessage());
                    }
                }
            });
        });
        // El boton backButton delega a un handler externo (metodo a configurar via callback en el Main)
    }

    private void regresarInicio() {
        try {
            // Cambia "MainView.fxml" por el nombre real de tu pantalla de inicio
            Parent root = FXMLLoader.load(getClass().getResource("/mx/unison/view/HomeView.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException ex) {
            showError("No se pudo cargar la pantalla de inicio: " + ex.getMessage());
        }
    }

    private void reloadData() {
        almacenes.clear();
        try {
            almacenes.addAll(almacenDAO.buscarTodos());
        } catch (Exception ex) {
            showError("No fue posible cargar almacenes: " + ex.getMessage());
        }
    }

    // Editor modal usando Stage/card minimalista
    private void openEditor(Almacen almacen) {
        Stage dialog = new Stage();
        dialog.initOwner(tableView.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(almacen == null ? "Nuevo Almacén" : "Editar Almacén");

        // Card layout
        VBox card = new VBox(16);
        card.getStyleClass().add("card-dark");
        card.setPadding(new Insets(28,38,28,38));

        Label lblTitle = new Label(almacen == null ? "Nuevo Almacén" : "Editar Almacén");
        lblTitle.getStyleClass().add("title-card");

        TextField txtNombre = new TextField(almacen != null ? almacen.nombre : "");
        txtNombre.setPromptText("Nombre...");
        TextField txtUbicacion = new TextField(almacen != null ? almacen.ubicacion : "");
        txtUbicacion.setPromptText("Ubicación...");

        Button btnSave = new Button("Guardar");
        btnSave.getStyleClass().add("button-primary");
        btnSave.setDefaultButton(true);

        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().add("button-flat");

        HBox actions = new HBox(14, btnSave, btnCancel);

        card.getChildren().addAll(lblTitle, new Label("Nombre"), txtNombre, new Label("Ubicación"), txtUbicacion, actions);

        Scene scene = new Scene(card);
        scene.getStylesheets().add(getClass().getResource("/mx/unison/view/dark-theme.css").toExternalForm());
        dialog.setScene(scene);

        btnSave.setOnAction(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                String ubicacion = txtUbicacion.getText().trim();
                if (nombre.isEmpty()) { showError("El nombre no puede ser vacío"); return; }

                if (almacen == null) {
                    Almacen nuevo = new Almacen();
                    nuevo.nombre = nombre;
                    nuevo.ubicacion = ubicacion;
                    nuevo.ultimoUsuario = "ADMIN"; // o usuario actual real
                    almacenDAO.crear(nuevo);
                } else {
                    almacen.nombre = nombre;
                    almacen.ubicacion = ubicacion;
                    almacen.ultimoUsuario = "ADMIN";
                    almacenDAO.actualizar(almacen);
                }
                reloadData();
                dialog.close();
            } catch (Exception ex) {
                showError("Error al guardar: " + ex.getMessage());
            }
        });

        btnCancel.setOnAction(e -> dialog.close());

        dialog.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.initOwner(tableView != null && tableView.getScene() != null ? tableView.getScene().getWindow() : null);
        a.setHeaderText("Error");
        a.showAndWait();
    }

    // Puedes inyectar aquí vía el Main la acción de backButton si lo deseas:
    public void setOnBack(Runnable r) {
        backButton.setOnAction(e -> r.run());
    }
}
