package mx.unison;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class PanelProductosTest extends SwingTestSupport {

    private static final String DB_FILE = "Inventario.db";
    private Database db;
    private PanelProductos panel;
    private int almacenId;

    protected void setUp() throws Exception {
        File f = new File(DB_FILE);
        if (f.exists()) {
            f.delete();
        }
        db = new Database();
        almacenId = db.insertAlmacen("Principal", "Piso 1", "ADMIN");
        panel = new PanelProductos(db, new Runnable() {
            public void run() {
            }
        });
    }

    private DefaultTableModel getModel() throws Exception {
        Field f = PanelProductos.class.getDeclaredField("model");
        f.setAccessible(true);
        return (DefaultTableModel) f.get(panel);
    }

    private void openForm(final Producto producto) throws Exception {
        final Method m = PanelProductos.class.getDeclaredMethod("openForm", Producto.class);
        m.setAccessible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    m.invoke(panel, new Object[]{producto});
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        });
    }

    public void testTopButtonsExist() {
        assertNotNull(findButton(panel, "Regresar"));
        assertNotNull(findButton(panel, "Agregar"));
        assertNotNull(findButton(panel, "Modificar"));
        assertNotNull(findButton(panel, "Eliminar"));
    }

    public void testNewProductFormContainsExpectedControls() throws Exception {
        openForm(null);

        final JDialog dialog = waitForDialog("Nuevo Producto", 4000L);
        assertNotNull(dialog);

        List<JTextField> fields = findAll(dialog.getContentPane(), JTextField.class);
        List<JSpinner> spinners = findAll(dialog.getContentPane(), JSpinner.class);
        List<JComboBox> combos = findAll(dialog.getContentPane(), JComboBox.class);

        assertEquals(2, fields.size());
        assertEquals(2, spinners.size());
        assertEquals(1, combos.size());

        JComboBox combo = combos.get(0);
        assertTrue(combo.getItemCount() >= 1);
        assertEquals("Sin almacén", String.valueOf(combo.getItemAt(0)));

        assertNotNull(findButton(dialog.getContentPane(), "Guardar"));
        assertNotNull(findButton(dialog.getContentPane(), "Cancelar"));

        disposeDialog(dialog);
    }

    public void testInsertProductAndRefreshesTable() throws Exception {
        openForm(null);

        final JDialog dialog = waitForDialog("Nuevo Producto", 4000L);
        assertNotNull(dialog);

        final List<JTextField> fields = findAll(dialog.getContentPane(), JTextField.class);
        final List<JSpinner> spinners = findAll(dialog.getContentPane(), JSpinner.class);
        final List<JComboBox> combos = findAll(dialog.getContentPane(), JComboBox.class);

        runOnEdtAndWait(new Runnable() {
            public void run() {
                fields.get(0).setText("Mouse");
                fields.get(1).setText("Óptico");
                spinners.get(0).setValue(Integer.valueOf(12));
                spinners.get(1).setValue(Double.valueOf(149.90));
                combos.get(0).setSelectedIndex(1);
            }
        });

        runOnEdtAndWait(new Runnable() {
            public void run() {
                findButton(dialog.getContentPane(), "Guardar").doClick();
            }
        });

        Thread.sleep(200L);

        assertEquals(1, db.listProductos().size());
        Producto p = db.listProductos().get(0);
        assertEquals("Mouse", p.nombre);
        assertEquals("Óptico", p.descripcion);
        assertEquals(12, p.cantidad);
        assertEquals(149.90, p.precio, 0.01);
        assertEquals(almacenId, p.almacenId);
        assertEquals("Principal", p.almacenNombre);
        assertEquals(1, getModel().getRowCount());
        assertEquals("Mouse", getModel().getValueAt(0, 1));
        assertEquals("Principal", getModel().getValueAt(0, 5));
    }

    public void testProductCanBeSavedWithoutAlmacen() throws Exception {
        openForm(null);

        final JDialog dialog = waitForDialog("Nuevo Producto", 4000L);
        assertNotNull(dialog);

        final List<JTextField> fields = findAll(dialog.getContentPane(), JTextField.class);
        final List<JSpinner> spinners = findAll(dialog.getContentPane(), JSpinner.class);
        final List<JComboBox> combos = findAll(dialog.getContentPane(), JComboBox.class);

        runOnEdtAndWait(new Runnable() {
            public void run() {
                fields.get(0).setText("Cable");
                fields.get(1).setText("USB-C");
                spinners.get(0).setValue(Integer.valueOf(3));
                spinners.get(1).setValue(Double.valueOf(89.50));
                combos.get(0).setSelectedIndex(0);
            }
        });

        runOnEdtAndWait(new Runnable() {
            public void run() {
                findButton(dialog.getContentPane(), "Guardar").doClick();
            }
        });

        Thread.sleep(200L);

        Producto p = db.listProductos().get(0);
        assertEquals("Cable", p.nombre);
        assertNull(p.almacenNombre);
        assertEquals(0, p.almacenId);
    }

    public void testEditProductUpdatesExistingRow() throws Exception {
        Producto p = new Producto();
        p.nombre = "Laptop";
        p.descripcion = "14 pulgadas";
        p.cantidad = 5;
        p.precio = 15999.00;
        p.almacenId = almacenId;

        int id = db.insertProducto(p, "ADMIN");
        assertTrue(id > 0);

        Producto edit = new Producto();
        edit.id = id;
        edit.nombre = "Laptop";
        edit.descripcion = "14 pulgadas";
        edit.cantidad = 5;
        edit.precio = 15999.00;
        edit.almacenId = almacenId;
        edit.almacenNombre = "Principal";

        openForm(edit);

        final JDialog dialog = waitForDialog("Editar Producto", 4000L);
        assertNotNull(dialog);

        final List<JTextField> fields = findAll(dialog.getContentPane(), JTextField.class);
        final List<JSpinner> spinners = findAll(dialog.getContentPane(), JSpinner.class);

        runOnEdtAndWait(new Runnable() {
            public void run() {
                fields.get(0).setText("Laptop Pro");
                fields.get(1).setText("16 pulgadas");
                spinners.get(0).setValue(Integer.valueOf(2));
                spinners.get(1).setValue(Double.valueOf(18999.00));
            }
        });

        runOnEdtAndWait(new Runnable() {
            public void run() {
                findButton(dialog.getContentPane(), "Guardar").doClick();
            }
        });

        Thread.sleep(200L);

        Producto updated = db.listProductos().get(0);
        assertEquals(id, updated.id);
        assertEquals("Laptop Pro", updated.nombre);
        assertEquals("16 pulgadas", updated.descripcion);
        assertEquals(2, updated.cantidad);
        assertEquals(18999.00, updated.precio, 0.01);
        assertEquals(almacenId, updated.almacenId);
        assertEquals(1, getModel().getRowCount());
        assertEquals("Laptop Pro", getModel().getValueAt(0, 1));
    }
}