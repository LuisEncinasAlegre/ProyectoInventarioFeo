package mx.unison;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class AlmacenesPanelTest extends SwingTestSupport {

    private static final String DB_FILE = "Inventario.db";
    private Database db;
    private AlmacenesPanel panel;

    protected void setUp() throws Exception {
        File f = new File(DB_FILE);
        if (f.exists()) {
            f.delete();
        }
        db = new Database();
        panel = new AlmacenesPanel(db, new Runnable() {
            public void run() {
            }
        });
    }

    private DefaultTableModel getModel() throws Exception {
        Field f = AlmacenesPanel.class.getDeclaredField("model");
        f.setAccessible(true);
        return (DefaultTableModel) f.get(panel);
    }

    private void openForm(final Almacen almacen) throws Exception {
        final Method m = AlmacenesPanel.class.getDeclaredMethod("openForm", Almacen.class);
        m.setAccessible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    m.invoke(panel, new Object[]{almacen});
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

    public void testNewAlmacenFormHasSaveCancelAndPersistsData() throws Exception {
        openForm(null);

        final JDialog dialog = waitForDialog("Nuevo Almacén", 4000L);
        assertNotNull(dialog);

        List<JTextField> fields = findAll(dialog.getContentPane(), JTextField.class);
        assertEquals(2, fields.size());

        runOnEdtAndWait(new Runnable() {
            public void run() {
                fields.get(0).setText("Bodega Norte");
                fields.get(1).setText("Zona 1");
            }
        });

        assertNotNull(findButton(dialog.getContentPane(), "Guardar"));
        assertNotNull(findButton(dialog.getContentPane(), "Cancelar"));

        runOnEdtAndWait(new Runnable() {
            public void run() {
                findButton(dialog.getContentPane(), "Guardar").doClick();
            }
        });

        Thread.sleep(200L);

        assertEquals(1, db.listAlmacenes().size());
        assertEquals("Bodega Norte", db.listAlmacenes().get(0).nombre);
        assertEquals("Zona 1", db.listAlmacenes().get(0).ubicacion);
        assertEquals(1, getModel().getRowCount());
        assertEquals("Bodega Norte", getModel().getValueAt(0, 1));
        assertEquals("Zona 1", getModel().getValueAt(0, 2));
    }

    public void testEditAlmacenUpdatesExistingRowAndRefreshesTable() throws Exception {
        int id = db.insertAlmacen("Viejo", "Zona A", "ADMIN");
        assertTrue(id > 0);

        Almacen a = new Almacen();
        a.id = id;
        a.nombre = "Viejo";
        a.ubicacion = "Zona A";

        openForm(a);

        final JDialog dialog = waitForDialog("Editar Almacén", 4000L);
        assertNotNull(dialog);

        List<JTextField> fields = findAll(dialog.getContentPane(), JTextField.class);
        assertEquals(2, fields.size());

        runOnEdtAndWait(new Runnable() {
            public void run() {
                fields.get(0).setText("Nuevo");
                fields.get(1).setText("Zona B");
            }
        });

        runOnEdtAndWait(new Runnable() {
            public void run() {
                findButton(dialog.getContentPane(), "Guardar").doClick();
            }
        });

        Thread.sleep(200L);

        Almacen updated = db.listAlmacenes().get(0);
        assertEquals("Nuevo", updated.nombre);
        assertEquals("Zona B", updated.ubicacion);
        assertNotNull(updated.fechaHoraUltimaMod);
        assertEquals(1, getModel().getRowCount());
        assertEquals("Nuevo", getModel().getValueAt(0, 1));
    }
}