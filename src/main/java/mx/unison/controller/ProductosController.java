package mx.unison.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import mx.unison.modelos.*;

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
    private String usuarioActual = "ADMIN"; // O pasar desde Login

    @FXML
    public void initialize() {
        try {
            DatabaseHelper dbh = new DatabaseHelper();
            productoDAO = new ProductoDAOImpl(dbh.getConnectionSource());
            almacenDAO = new AlmacenDAOImpl(dbh.getConnectionSource());
        } catch (Exception e) {
            showError("Error de base de datos: " + e.getMessage());
        }

        // Table column bindings
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colAlmacen.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            if (p.almacen != null) {
                return new javafx.beans.property.SimpleStringProperty(p.almacen.nombre);
            }
            return new javafx.beans.property.SimpleStringProperty("Sin almacén");
        });
        colCreado.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colUltMod.setCellValueFactory(new PropertyValueFactory<>("fechaModificacion"));
        colUltUsuario.setCellValueFactory(new PropertyValueFactory<>("ultimoUsuario"));

        tableView.setItems(productos);
        reloadData();

        // Button actions
        addButton.setOnAction(e -> openEditor(null));
        editButton.setOnAction(e -> {
            Producto sel = tableView.getSelectionModel().getSelectedItem();
            if (sel != null) openEditor(sel);
        });
        deleteButton.setOnAction(e -> {
            Producto sel = tableView.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            var r = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Seguro que desea eliminar el producto?", ButtonType.YES, ButtonType.NO);
            r.setHeaderText("Confirmar eliminación");
            r.initOwner(tableView.getScene().getWindow());
            r.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    try {
                        productoDAO.eliminar(sel);
                        reloadData();
                    } catch (Exception ex) {
                        showError("No fue posible eliminar el producto: " + ex.getMessage());
                    }
                }
            });
        });
    }

    private void reloadData() {
        productos.clear();
        try {
            productos.addAll(productoDAO.buscarTodos());
        } catch (Exception ex) {
            showError("No fue posible cargar productos: " + ex.getMessage());
        }
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

            // Si es edición, seleccionar el almacén actual
            if (producto != null && producto.almacen != null) {
                cmbAlmacen.setValue(producto.almacen);
            }
        } catch (Exception ex) {
            showError("Error al cargar almacenes: " + ex.getMessage());
        }

        // Botones de acción
        Button btnSave = new Button("Guardar");
        btnSave.getStyleClass().add("button-primary");
        btnSave.setDefaultButton(true);

        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().add("button-flat");

        // Grid para mejor organización
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        grid.add(new Label("Nombre"), 0, 0);
        grid.add(txtNombre, 1, 0);
        GridPane.setHgrow(txtNombre, Priority.ALWAYS);

        grid.add(new Label("Descripción"), 0, 1);
        grid.add(txtDescripcion, 1, 1);
        GridPane.setHgrow(txtDescripcion, Priority.ALWAYS);

        grid.add(new Label("Cantidad"), 0, 2);
        grid.add(spnCantidad, 1, 2);

        grid.add(new Label("Precio"), 0, 3);
        grid.add(spnPrecio, 1, 3);

        grid.add(new Label("Almacén"), 0, 4);
        grid.add(cmbAlmacen, 1, 4);
        GridPane.setHgrow(cmbAlmacen, Priority.ALWAYS);

        HBox actions = new HBox(14, btnSave, btnCancel);

        card.getChildren().addAll(lblTitle, grid, actions);

        Scene scene = new Scene(card);
        scene.getStylesheets().add(getClass().getResource("/mx/unison/view/dark-theme.css").toExternalForm());
        dialog.setScene(scene);

        btnSave.setOnAction(e -> {
            try {
                String nombre = txtNombre.getText().trim();
                String descripcion = txtDescripcion.getText().trim();
                int cantidad = spnCantidad.getValue();
                double precio = spnPrecio.getValue();

                if (nombre.isEmpty()) {
                    showError("El nombre no puede ser vacío");
                    return;
                }

                if (producto == null) {
                    // Crear nuevo producto
                    Producto nuevo = new Producto();
                    nuevo.nombre = nombre;
                    nuevo.descripcion = descripcion;
                    nuevo.cantidad = cantidad;
                    nuevo.precio = precio;
                    nuevo.almacen = cmbAlmacen.getValue();
                    nuevo.ultimoUsuario = usuarioActual;
                    productoDAO.crear(nuevo);
                } else {
                    // Actualizar producto existente
                    producto.nombre = nombre;
                    producto.descripcion = descripcion;
                    producto.cantidad = cantidad;
                    producto.precio = precio;
                    producto.almacen = cmbAlmacen.getValue();
                    producto.ultimoUsuario = usuarioActual;
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

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.initOwner(tableView != null && tableView.getScene() != null ? tableView.getScene().getWindow() : null);
        a.setHeaderText("Error");
        a.showAndWait();
    }

    public void setOnBack(Runnable r) {
        backButton.setOnAction(e -> r.run());
    }

    public void setUsuarioActual(String usuario) {
        this.usuarioActual = usuario;
    }
}