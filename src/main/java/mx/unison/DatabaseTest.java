package mx.unison;

import junit.framework.TestCase;

import java.io.File;
import java.sql.*;

public class DatabaseTest extends TestCase {

    private static final String DB_FILE = "Inventario.db";
    private Database db;

    protected void setUp() throws Exception {
        deleteDbFile();
        db = new Database();
    }

    private void deleteDbFile() {
        File f = new File(DB_FILE);
        if (f.exists()) {
            f.delete();
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
    }

    private boolean tableExists(String table) throws Exception {
        try (Connection c = connect()) {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getTables(null, null, table, null)) {
                return rs.next();
            }
        }
    }

    private boolean columnExists(String table, String column) throws Exception {
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private int countRows(String sql) throws Exception {
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String queryString(String sql) throws Exception {
        try (Connection c = connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    public void testSchemaIsCreatedCorrectly() throws Exception {
        assertTrue(tableExists("usuarios"));
        assertTrue(tableExists("almacenes"));
        assertTrue(tableExists("productos"));

        assertTrue(columnExists("usuarios", "nombre"));
        assertTrue(columnExists("usuarios", "password"));
        assertTrue(columnExists("usuarios", "Ultimo_Inicio_de_Sesion"));
        assertTrue(columnExists("almacenes", "nombre"));
        assertTrue(columnExists("almacenes", "ubicacion"));
        assertTrue(columnExists("productos", "almacen_id"));
        assertTrue(columnExists("productos", "descripcion"));
    }

    public void testDefaultUsersAreInserted() throws Exception {
        assertEquals(3, countRows("SELECT COUNT(*) FROM usuarios"));
        assertEquals(1, countRows("SELECT COUNT(*) FROM usuarios WHERE nombre='ADMIN'"));
        assertEquals(1, countRows("SELECT COUNT(*) FROM usuarios WHERE nombre='PRODUCTOS'"));
        assertEquals(1, countRows("SELECT COUNT(*) FROM usuarios WHERE nombre='ALMACENES'"));
    }

    public void testUsernameRemainsUnique() throws Exception {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO usuarios(nombre, password, rol) VALUES(?, ?, ?)")) {
            ps.setString(1, "ADMIN");
            ps.setString(2, "x");
            ps.setString(3, "ADMIN");
            ps.executeUpdate();
            fail("La inserción duplicada debió fallar por UNIQUE.");
        } catch (SQLException expected) {
            assertTrue(true);
        }
    }

    public void testAuthenticateUpdatesLastLogin() throws Exception {
        assertNull(queryString("SELECT Ultimo_Inicio_de_Sesion FROM usuarios WHERE nombre='ADMIN'"));

        Usuario u = db.authenticate("ADMIN", "admin23");
        assertNotNull(u);
        assertEquals("ADMIN", u.rol);

        String lastLogin = queryString("SELECT Ultimo_Inicio_de_Sesion FROM usuarios WHERE nombre='ADMIN'");
        assertNotNull(lastLogin);
        assertTrue(lastLogin.length() > 0);
    }

    public void testInsertUpdateAndDeleteAlmacen() throws Exception {
        int id = db.insertAlmacen("Bodega Norte", "Zona 1", "ADMIN");
        assertTrue(id > 0);

        assertEquals(1, countRows("SELECT COUNT(*) FROM almacenes WHERE id=" + id));
        assertEquals("Bodega Norte", queryString("SELECT nombre FROM almacenes WHERE id=" + id));
        assertEquals("Zona 1", queryString("SELECT ubicacion FROM almacenes WHERE id=" + id));
        assertEquals("ADMIN", queryString("SELECT ultimo_usuario_en_modificar FROM almacenes WHERE id=" + id));

        db.updateAlmacen(id, "Bodega Sur", "Zona 2", "PRODUCTOS");
        assertEquals("Bodega Sur", queryString("SELECT nombre FROM almacenes WHERE id=" + id));
        assertEquals("Zona 2", queryString("SELECT ubicacion FROM almacenes WHERE id=" + id));
        assertEquals("PRODUCTOS", queryString("SELECT ultimo_usuario_en_modificar FROM almacenes WHERE id=" + id));
        assertNotNull(queryString("SELECT fecha_hora_ultima_modificacion FROM almacenes WHERE id=" + id));

        db.deleteAlmacen(id);
        assertEquals(0, countRows("SELECT COUNT(*) FROM almacenes WHERE id=" + id));
    }

    public void testInsertUpdateAndDeleteProducto() throws Exception {
        int almId = db.insertAlmacen("Bodega Central", "Planta Baja", "ADMIN");
        assertTrue(almId > 0);

        Producto p = new Producto();
        p.nombre = "Teclado";
        p.descripcion = "Mecánico";
        p.cantidad = 10;
        p.precio = 499.99;
        p.almacenId = almId;

        int id = db.insertProducto(p, "ADMIN");
        assertTrue(id > 0);

        assertEquals("Teclado", queryString("SELECT nombre FROM productos WHERE id=" + id));
        assertEquals("Mecánico", queryString("SELECT descripcion FROM productos WHERE id=" + id));
        assertEquals("10", queryString("SELECT cantidad FROM productos WHERE id=" + id));
        assertEquals("ADMIN", queryString("SELECT ultimo_usuario_en_modificar FROM productos WHERE id=" + id));
        assertEquals(String.valueOf(almId), queryString("SELECT almacen_id FROM productos WHERE id=" + id));

        p.id = id;
        p.nombre = "Teclado Pro";
        p.descripcion = "Mecánico RGB";
        p.cantidad = 7;
        p.precio = 699.50;
        p.almacenId = 0;
        db.updateProducto(p, "PRODUCTOS");

        assertEquals("Teclado Pro", queryString("SELECT nombre FROM productos WHERE id=" + id));
        assertEquals("Mecánico RGB", queryString("SELECT descripcion FROM productos WHERE id=" + id));
        assertEquals("7", queryString("SELECT cantidad FROM productos WHERE id=" + id));
        assertEquals("PRODUCTOS", queryString("SELECT ultimo_usuario_en_modificar FROM productos WHERE id=" + id));
        assertNull(queryString("SELECT almacen_id FROM productos WHERE id=" + id));

        db.deleteProducto(id);
        assertEquals(0, countRows("SELECT COUNT(*) FROM productos WHERE id=" + id));
    }
}