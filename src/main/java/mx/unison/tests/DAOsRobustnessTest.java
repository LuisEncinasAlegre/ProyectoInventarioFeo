package mx.unison.tests;

import mx.unison.modelos.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class DAOsRobustnessTest {

    private static DatabaseHelper dbHelper;
    private static UsuarioDAO usuarioDAO;
    private static AlmacenDAO almacenDAO;
    private static ProductoDAO productoDAO;

    @BeforeAll
    static void setup() throws Exception {
        dbHelper = new DatabaseHelper();
        usuarioDAO = new UsuarioDAOImpl(dbHelper.getConnectionSource());
        almacenDAO = new AlmacenDAOImpl(dbHelper.getConnectionSource());
        productoDAO = new ProductoDAOImpl(dbHelper.getConnectionSource());
    }

    @AfterAll
    static void tearDown() throws Exception {
        dbHelper.close();
    }

    @Nested
    @DisplayName("UsuarioDAO tests")
    class UsuarioDAOTest {
        @Test
        void crearUsuario_NombreNulo_debeFallar() {
            Usuario usuario = new Usuario();
            usuario.nombre = null;
            usuario.password = "abc";
            usuario.rol = "ADMIN";
            assertThrows(Exception.class, () -> usuarioDAO.crear(usuario));
        }

        @Test
        void crearUsuario_NombreVacio_debeFallar() {
            Usuario usuario = new Usuario();
            usuario.nombre = "";
            usuario.password = "123";
            usuario.rol = "ADMIN";
            // Puede lanzar excepción por unicidad/no nulo según reglas de la bd
            assertThrows(Exception.class, () -> usuarioDAO.crear(usuario));
        }

        @Test
        void crearUsuario_PasswordNulo_debeFallar() {
            Usuario usuario = new Usuario();
            usuario.nombre = "testuser";
            usuario.password = null;
            usuario.rol = "ADMIN";
            assertThrows(Exception.class, () -> usuarioDAO.crear(usuario));
        }

        @Test
        void crearUsuario_RolNulo_debeFallar() {
            Usuario usuario = new Usuario();
            usuario.nombre = "testuser2";
            usuario.password = "xyz";
            usuario.rol = null;
            assertThrows(Exception.class, () -> usuarioDAO.crear(usuario));
        }
    }

    @Nested
    @DisplayName("AlmacenDAO tests")
    class AlmacenDAOTest {
        @Test
        void crearAlmacen_NombreNulo_debeFallar() {
            Almacen almacen = new Almacen();
            almacen.nombre = null;
            almacen.ubicacion = "A";
            assertThrows(Exception.class, () -> almacenDAO.crear(almacen));
        }

        @Test
        void crearAlmacen_NombreVacio_debeFallar() {
            Almacen almacen = new Almacen();
            almacen.nombre = "";
            almacen.ubicacion = "B";
            assertThrows(Exception.class, () -> almacenDAO.crear(almacen));
        }
    }

    @Nested
    @DisplayName("ProductoDAO tests")
    class ProductoDAOTest {
        @Test
        void crearProducto_NombreNulo_debeFallar() {
            Producto p = new Producto();
            p.nombre = null;
            p.descripcion = "desc";
            p.cantidad = 10;
            p.precio = 100.0;
            assertThrows(Exception.class, () -> productoDAO.crear(p));
        }

        @Test
        void crearProducto_CantidadNegativa_debeFallar() {
            Producto p = new Producto();
            p.nombre = "algo";
            p.descripcion = "desc";
            p.cantidad = -4;
            p.precio = 10.0;
            assertThrows(Exception.class, () -> productoDAO.crear(p));
        }

        @Test
        void crearProducto_PrecioNegativo_debeFallar() {
            Producto p = new Producto();
            p.nombre = "algo";
            p.descripcion = "desc";
            p.cantidad = 1;
            p.precio = -15.0;
            assertThrows(Exception.class, () -> productoDAO.crear(p));
        }
    }
}