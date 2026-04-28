package mx.unison.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import mx.unison.modelos.*;
import java.io.IOException;

public class ProductosController {

    @FXML private TableView<Producto> tableView;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colDescripcion;
    @FXML private TableColumn<Producto, Integer> colCantidad;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, String> colAlmacen;
    @FXML private TableColumn<Producto, String> colCreado;
    @FXML private TableColumn<Producto, String> colUltMod;
    @FXML private TableColumn<Producto, String> colUltUsuario;
    @FXML private Button backButton, addButton, editButton, deleteButton;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private ProductoDAO productoDAO;
    private AlmacenDAO almacenDAO;
    private String usuarioActual = "ADMIN";

    @FXML
    public void initialize() {
        try {
            DatabaseHelper dbh = new DatabaseHelper();
            productoDAO = new ProductoDAOImpl(dbh.getConnectionSource());
            almacenDAO = new AlmacenDAOImpl(dbh.getConnectionSource());
        } catch (Exception e) {
            showError("Error de base de datos: " + e.getMessage());
        }

        // Bindings corregidos para visibilidad
        colId.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().id));
        colNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().nombre));
        colDescripcion.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().descripcion));
        colCantidad.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().cantidad));
        colPrecio.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().precio));
        colAlmacen.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().almacen != null ? cd.getValue().almacen.nombre : "Sin almacén"));
        colCreado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().fechaCreacion));
        colUltMod.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().fechaModificacion));
        colUltUsuario.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().ultimoUsuario));

        tableView.setItems(productos);
        reloadData();

        // ACCIÓN DEL BOTÓN REGRESAR (Añadida)
        backButton.setOnAction(e -> regresarInicio());

        addButton.setOnAction(e -> openEditor(null));
        editButton.setOnAction(e -> {
            Producto sel = tableView.getSelectionModel().getSelectedItem();
            if (sel != null) openEditor(sel);
        });
        deleteButton.setOnAction(e -> {
            Producto sel = tableView.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            var r = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar producto?", ButtonType.YES, ButtonType.NO);
            r.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    try {
                        productoDAO.eliminar(sel);
                        reloadData();
                    } catch (Exception ex) { showError(ex.getMessage()); }
                }
            });
        });
    }

    // Método para navegar al inicio (HomeView)
    private void regresarInicio() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/mx/unison/view/HomeView.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException ex) {
            showError("No se pudo cargar la pantalla de inicio: " + ex.getMessage());
        }
    }

    private void reloadData() {
        productos.clear();
        try {
            productos.addAll(productoDAO.buscarTodos());
        } catch (Exception ex) {
            showError("Error al cargar: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        // Intentamos asignar el dueño de la ventana para que el error aparezca centrado
        if (tableView != null && tableView.getScene() != null) {
            a.initOwner(tableView.getScene().getWindow());
        }
        a.setHeaderText("Error en la operación");
        a.showAndWait();
    }

    private void openEditor(Producto producto) {
        Stage dialog = new Stage();
        dialog.initOwner(tableView.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(producto == null ? "Nuevo Producto" : "Editar Producto");

        // Card layout
        VBox card = new VBox(16);
        card.getStyleClass().add("card-dark");
        card.setPadding(new Insets(28, 38, 28, 38));

        Label lblTitle = new Label(producto == null ? "Nuevo Producto" : "Editar Producto");
        lblTitle.getStyleClass().add("title-card");

        // Campos de entrada
        TextField txtNombre = new TextField(producto != null ? producto.nombre : "");
        txtNombre.setPromptText("Nombre...");

        TextField txtDescripcion = new TextField(producto != null ? producto.descripcion : "");
        txtDescripcion.setPromptText("Descripción...");

        Spinner<Integer> spnCantidad = new Spinner<>(0, 100000,
                producto != null ? producto.cantidad : 0, 1);
        spnCantidad.setEditable(true);

        Spinner<Double> spnPrecio = new Spinner<>(0.0, 1000000.0,
                producto != null ? producto.precio : 0.0, 0.5);
        spnPrecio.setEditable(true);

        // ComboBox para almacenes
        ComboBox<Almacen> cmbAlmacen = new ComboBox<>();
        cmbAlmacen.setPromptText("Selecciona un almacén...");
        try {
            cmbAlmacen.setItems(FXCollections.observableArrayList(almacenDAO.buscarTodos()));
            cmbAlmacen.setCellFactory(lv -> new ListCell<Almacen>() {
                @Override
                protected void updateItem(Almacen item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.nombre);
                }
            });
            cmbAlmacen.setButtonCell(new ListCell<Almacen>() {
                @Override
                protected void updateItem(Almacen item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.nombre);
                }
            });

            if (producto != null && producto.almacen != null) {
                cmbAlmacen.setValue(producto.almacen);
            }
        } catch (Exception ex) {
            showError("Error al cargar almacenes: " + ex.getMessage());
        }

        Button btnSave = new Button("Guardar");
        btnSave.getStyleClass().add("button-primary");
        btnSave.setDefaultButton(true);

        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().add("button-flat");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.add(new Label("Nombre"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Descripción"), 0, 1);
        grid.add(txtDescripcion, 1, 1);
        grid.add(new Label("Cantidad"), 0, 2);
        grid.add(spnCantidad, 1, 2);
        grid.add(new Label("Precio"), 0, 3);
        grid.add(spnPrecio, 1, 3);
        grid.add(new Label("Almacén"), 0, 4);
        grid.add(cmbAlmacen, 1, 4);

        HBox actions = new HBox(14, btnSave, btnCancel);
        card.getChildren().addAll(lblTitle, grid, actions);

        Scene scene = new Scene(card);
        scene.getStylesheets().add(getClass().getResource("/mx/unison/view/dark-theme.css").toExternalForm());
        dialog.setScene(scene);

        btnSave.setOnAction(e -> {
            try {
                if (txtNombre.getText().trim().isEmpty()) {
                    showError("El nombre no puede ser vacío");
                    return;
                }
                if (producto == null) {
                    Producto nuevo = new Producto();
                    asignarDatos(nuevo, txtNombre, txtDescripcion, spnCantidad, spnPrecio, cmbAlmacen);
                    productoDAO.crear(nuevo);
                } else {
                    asignarDatos(producto, txtNombre, txtDescripcion, spnCantidad, spnPrecio, cmbAlmacen);
                    productoDAO.actualizar(producto);
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

    // Metodo auxiliar para evitar repetir código dentro de openEditor
    private void asignarDatos(Producto p, TextField n, TextField d, Spinner<Integer> c, Spinner<Double> pr, ComboBox<Almacen> a) {
        p.nombre = n.getText().trim();
        p.descripcion = d.getText().trim();
        p.cantidad = c.getValue();
        p.precio = pr.getValue();
        p.almacen = a.getValue();
        p.ultimoUsuario = this.usuarioActual;
    }


    // openEditor y showError permanecen igual...

    public void setOnBack(Runnable r) {
        backButton.setOnAction(e -> r.run());
    }
}