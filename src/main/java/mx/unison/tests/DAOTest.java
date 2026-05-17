package mx.unison.tests;

import mx.unison.modelos.*;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.j256.ormlite.support.ConnectionSource;
import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DAOTest {

    private ConnectionSource connectionSource;
    private UsuarioDAO usuarioDAO;
    private AlmacenDAO almacenDAO;
    private ProductoDAO productoDAO;

    @BeforeAll
    void setup() throws Exception {
        // Conexion y DAOs
        connectionSource = new JdbcConnectionSource("jdbc:sqlite::memory:");
        // Tablas
        TableUtils.createTableIfNotExists(connectionSource, Usuario.class);
        TableUtils.createTableIfNotExists(connectionSource, Almacen.class);
        TableUtils.createTableIfNotExists(connectionSource, Producto.class);
        // DAOs
        usuarioDAO = new UsuarioDAOImpl(connectionSource);
        almacenDAO = new AlmacenDAOImpl(connectionSource);
        productoDAO = new ProductoDAOImpl(connectionSource);
    }

    @AfterAll
    void tearDown() throws Exception {
        if (connectionSource != null) connectionSource.close();
    }

    @Test
    void usuarioDAO_CRUD() throws Exception {
        // Crear usuario
        Usuario u = new Usuario();
        u.nombre = "john";
        u.password = "md5hash";
        u.rol = "USER";
        usuarioDAO.crear(u);

        // Buscar por ID
        Usuario buscado = usuarioDAO.buscarPorId(u.id);
        assertNotNull(buscado);
        assertEquals(u.nombre, buscado.nombre);

        // Buscar por nombre
        Usuario buscadoPorNombre = usuarioDAO.buscarPorNombre("john");
        assertNotNull(buscadoPorNombre);
        assertEquals(u.id, buscadoPorNombre.id);

        // Buscar todos
        List<Usuario> usuarios = usuarioDAO.buscarTodos();
        assertEquals(1, usuarios.size());

        // Actualizar
        buscado.rol = "ADMIN";
        usuarioDAO.actualizar(buscado);
        Usuario actualizado = usuarioDAO.buscarPorId(u.id);
        assertEquals("ADMIN", actualizado.rol);

        // Eliminar
        usuarioDAO.eliminar(actualizado);
        usuarios = usuarioDAO.buscarTodos();
        assertTrue(usuarios.isEmpty());
    }

    @Test
    void almacenDAO_CRUD() throws Exception {
        Almacen a = new Almacen();
        a.nombre = "Sucursal 1";
        a.ubicacion = "Hermosillo";
        a.ultimoUsuario = "admin";
        almacenDAO.crear(a);

        // Buscar por ID
        Almacen buscado = almacenDAO.buscarPorId(a.id);
        assertNotNull(buscado);
        assertEquals(a.nombre, buscado.nombre);

        // Buscar todos
        List<Almacen> almacenes = almacenDAO.buscarTodos();
        assertEquals(1, almacenes.size());

        // Actualizar
        buscado.ubicacion = "Guaymas";
        almacenDAO.actualizar(buscado);
        Almacen act = almacenDAO.buscarPorId(a.id);
        assertEquals("Guaymas", act.ubicacion);

        // Eliminar
        almacenDAO.eliminar(act);
        almacenes = almacenDAO.buscarTodos();
        assertTrue(almacenes.isEmpty());
    }

    @Test
    void productoDAO_CRUD() throws Exception {
        // Crear Almacen primero (relación FK)
        Almacen a = new Almacen();
        a.nombre = "Suc";
        a.ubicacion = "Sonora";
        a.ultimoUsuario = "admin";
        almacenDAO.crear(a);

        Producto p = new Producto();
        p.nombre = "Gansito";
        p.descripcion = "Pastelito";
        p.cantidad = 50;
        p.precio = 12.50;
        p.almacen = a;
        p.ultimoUsuario = "admin";
        productoDAO.crear(p);

        // Buscar por ID
        Producto buscado = productoDAO.buscarPorId(p.id);
        assertNotNull(buscado);
        assertEquals("Gansito", buscado.nombre);

        // Buscar todos
        List<Producto> productos = productoDAO.buscarTodos();
        assertEquals(1, productos.size());

        // Actualizar
        buscado.precio = 14.00;
        productoDAO.actualizar(buscado);
        Producto actualizado = productoDAO.buscarPorId(buscado.id);
        assertEquals(14.00, actualizado.precio);

        // Eliminar
        productoDAO.eliminar(actualizado);
        productos = productoDAO.buscarTodos();
        assertTrue(productos.isEmpty());
    }
}