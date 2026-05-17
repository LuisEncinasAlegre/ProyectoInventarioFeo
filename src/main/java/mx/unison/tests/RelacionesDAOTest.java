package mx.unison.modelos;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.List;

public class RelacionesDAOTest {

    private ConnectionSource conn;
    private AlmacenDAO almacenDAO;
    private ProductoDAO productoDAO;

    @BeforeEach
    void setup() throws Exception {
        // Usa una BD en memoria para aislar pruebas
        conn = new JdbcConnectionSource("jdbc:sqlite::memory:");
        TableUtils.createTableIfNotExists(conn, Almacen.class);
        TableUtils.createTableIfNotExists(conn, Producto.class);

        almacenDAO = new AlmacenDAOImpl(conn);
        productoDAO = new ProductoDAOImpl(conn);
    }

    @AfterEach
    void cleanup() throws Exception {
        conn.close();
    }

    @Test
    void testCrearProductoLigadoAlmacenYConsistencia() throws Exception {
        // Crea un almacén
        Almacen almacen = new Almacen();
        almacen.nombre = "Almacen Central";
        almacen.ubicacion = "Edificio A";
        almacenDAO.crear(almacen);
        assertTrue(almacen.id > 0);

        // Crea un producto ligado a ese almacén
        Producto prod = new Producto();
        prod.nombre = "Laptop";
        prod.descripcion = "Dell XPS 13";
        prod.cantidad = 5;
        prod.precio = 15000.0;
        prod.almacen = almacen;
        prod.ultimoUsuario = "ADMIN";
        productoDAO.crear(prod);
        assertTrue(prod.id > 0);

        // Consultas y validaciones de relación
        Producto prodDB = productoDAO.buscarPorId(prod.id);
        assertNotNull(prodDB);
        assertNotNull(prodDB.almacen);
        assertEquals("Almacen Central", prodDB.almacen.nombre);

        List<Producto> productos = productoDAO.buscarTodos();
        assertEquals(1, productos.size());
        assertEquals(almacen.id, productos.get(0).almacen.id);

        // Eliminación: si la DB no tiene ON DELETE CASCADE fallará al borrar el almacén antes del producto
        // Intentémoslo y confirmemos el resultado.
        Exception excepcionEsperada = null;
        try {
            almacenDAO.eliminar(almacen);
        } catch (Exception ex) {
            excepcionEsperada = ex;
        }

        // Debe lanzarse excepción o seguir la política definida según tu DB
        // Si quieres ON DELETE CASCADE, adapta la definición SQL o ignora el assertNotNull para testear lo que aplica
        assertNotNull(excepcionEsperada, "No se debería poder eliminar almacenes con productos ligados (a menos que ON DELETE CASCADE esté activado)");
    }

    @Test
    void testEliminarProductoYLuegoAlmacen() throws Exception {
        // Crea almacén y producto ligado
        Almacen almacen = new Almacen();
        almacen.nombre = "Bodega";
        almacen.ubicacion = "Zona Sur";
        almacenDAO.crear(almacen);

        Producto prod = new Producto();
        prod.nombre = "Monitor";
        prod.descripcion = "LG 24''";
        prod.cantidad = 10;
        prod.precio = 3000.0;
        prod.almacen = almacen;
        prod.ultimoUsuario = "TEST";
        productoDAO.crear(prod);

        // Borra primero el producto
        productoDAO.eliminar(prod);
        assertTrue(productoDAO.buscarTodos().isEmpty());

        // Ahora puede borrarse el almacén
        almacenDAO.eliminar(almacen);

        assertTrue(almacenDAO.buscarTodos().isEmpty());
    }
}