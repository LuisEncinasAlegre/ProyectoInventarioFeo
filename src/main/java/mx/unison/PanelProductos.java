package mx.unison;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelProductos extends JPanel{
    private final Database db;
    private final Runnable onGoBack;
    private JTable table;
    private DefaultTableModel model;

    public PanelProductos(Database db, Runnable onGoBack) {
        this.db = db;
        this.onGoBack = onGoBack;
        setLayout(new BorderLayout());
        initTop();
        initTable();
        loadData();
    }
    private void initTop() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton back = new JButton("Regresar");
        back.addActionListener(e -> onGoBack.run());
        JButton add = new JButton("Agregar");
        add.addActionListener(e -> openForm(null));
        JButton edit = new JButton("Modificar");
        edit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                int id = (int) model.getValueAt(r, 0);
                Producto p = new Producto(); p.id = id; // simplificado, en form volveremos a cargar
                openForm(p);
            }
        });
        JButton del = new JButton("Eliminar");
        del.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                int id = (int) model.getValueAt(r, 0);
                int opt = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar el producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    db.deleteProducto(id);
                    loadData();
                }
            }
        });
        top.add(back); top.add(add); top.add(edit); top.add(del);
        add(top, BorderLayout.NORTH);
    }

    private void initTable() {
        model = new DefaultTableModel(new Object[]{"ID","Nombre","Descripción","Cantidad","Precio","Almacén","Creado","Últ.Mod","Últ.Usuario"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
    private void loadData() {
        model.setRowCount(0);
        List<Producto> productos = db.listProductos();
        for (Producto p : productos) {
            model.addRow(new Object[]{p.id, p.nombre, p.descripcion, p.cantidad, p.precio, p.almacenNombre, p.fechaCreacion, p.fechaModificacion, p.ultimoUsuario});
        }
    }
    private void openForm(Producto p) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), p == null ? "Nuevo Producto" : "Editar Producto", true);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        // Campos del formulario
        JTextField txtNombre = new JTextField(p != null ? p.nombre : "");
        JTextField txtDesc = new JTextField(p != null ? p.descripcion : "");
        JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(p != null ? p.cantidad : 0, 0, 100000, 1));
        JSpinner spnPrecio = new JSpinner(new SpinnerNumberModel(p != null ? p.precio : 0.0, 0.0, 1000000.0, 0.5));

        // ComboBox para buscar los almacenes existentes en la base de datos
        JComboBox<String> cmbAlmacen = new JComboBox<>();
        List<Almacen> almacenes = db.listAlmacenes();
        cmbAlmacen.addItem("Sin almacén");

        int selectedIndex = 0;
        for (int i = 0; i < almacenes.size(); i++) {
            Almacen alm = almacenes.get(i);
            cmbAlmacen.addItem(alm.id + " - " + alm.nombre);
            // Si estamos editando, autoseleccionar el almacén que ya tenía el producto
            if (p != null && p.almacenNombre != null && p.almacenNombre.equals(alm.nombre)) {
                selectedIndex = i + 1;
            }
        }
        cmbAlmacen.setSelectedIndex(selectedIndex);

        dialog.add(new JLabel(" Nombre:")); dialog.add(txtNombre);
        dialog.add(new JLabel(" Descripción:")); dialog.add(txtDesc);
        dialog.add(new JLabel(" Cantidad:")); dialog.add(spnCantidad);
        dialog.add(new JLabel(" Precio:")); dialog.add(spnPrecio);
        dialog.add(new JLabel(" Almacén:")); dialog.add(cmbAlmacen);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> {
            Producto prodToSave = new Producto();
            if (p != null) prodToSave.id = p.id;
            prodToSave.nombre = txtNombre.getText();
            prodToSave.descripcion = txtDesc.getText();
            prodToSave.cantidad = (Integer) spnCantidad.getValue();
            prodToSave.precio = (Double) spnPrecio.getValue();

            int idx = cmbAlmacen.getSelectedIndex();
            if (idx > 0) {
                prodToSave.almacenId = almacenes.get(idx - 1).id; // -1 porque el índice 0 es "Sin almacén"
            } else {
                prodToSave.almacenId = 0;
            }

            String usuarioActual = "ADMIN"; // Temporal
            if (p == null) {
                db.insertProducto(prodToSave, usuarioActual);
            } else {
                db.updateProducto(prodToSave, usuarioActual);
            }
            dialog.dispose();
            loadData();
        });

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dialog.dispose());

        dialog.add(btnGuardar);
        dialog.add(btnCancelar);

        dialog.setVisible(true);
    }
}
