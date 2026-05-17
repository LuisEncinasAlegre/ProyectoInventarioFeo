package mx.unison.tests;

import mx.unison.controller.LoginController;
import mx.unison.modelos.*;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginAuthTest {

    static ConnectionSource connectionSource;
    static UsuarioDAOImpl usuarioDAO;

    @BeforeAll
    static void setup() throws Exception {
        // Conexión a una base de datos en memoria VOLÁTIL para pruebas
        connectionSource = new JdbcConnectionSource("jdbc:sqlite::memory:");
        TableUtils.createTableIfNotExists(connectionSource, Usuario.class);
        usuarioDAO = new UsuarioDAOImpl(connectionSource);

        // Usuario de prueba por defecto
        Usuario admin = new Usuario();
        admin.nombre = "ADMIN";
        admin.password = LoginController.md5("admin123");
        admin.rol = "ADMIN";
        usuarioDAO.crear(admin);

        // Usuario extra para prueba
        Usuario user = new Usuario();
        user.nombre = "user1";
        user.password = LoginController.md5("mypassword");
        user.rol = "USER";
        usuarioDAO.crear(user);
    }

    @AfterAll
    static void cleanup() throws Exception {
        connectionSource.close();
    }

    @Test
    @Order(1)
    void testMd5Hash() {
        // Validar que el MD5 coincide con resultado esperado
        assertEquals("0192023a7bbd73250516f069df18b500", LoginController.md5("admin123"));
        assertEquals("34819d7beeabb9260a5c854bc85b3e44", LoginController.md5("mypassword"));
        assertEquals("", LoginController.md5(null));
    }

    @Test
    @Order(2)
    void testUsuarioEncontradoPorNombreCorrecto() throws Exception {
        Usuario admin = usuarioDAO.buscarPorNombre("ADMIN");
        assertNotNull(admin);
        assertEquals("ADMIN", admin.nombre);

        Usuario user = usuarioDAO.buscarPorNombre("user1");
        assertNotNull(user);
        assertEquals("user1", user.nombre);
    }

    @Test
    @Order(3)
    void testUsuarioNoEncontradoPorNombre() throws Exception {
        Usuario notExists = usuarioDAO.buscarPorNombre("nonexistent");
        assertNull(notExists);
    }

    @Test
    @Order(4)
    void testAuthContrasenaCorrecta() throws Exception {
        Usuario user = usuarioDAO.buscarPorNombre("user1");
        assertNotNull(user);
        assertEquals(LoginController.md5("mypassword"), user.password);
    }

    @Test
    @Order(5)
    void testAuthContrasenaIncorrecta() throws Exception {
        Usuario user = usuarioDAO.buscarPorNombre("user1");
        // Contraseña incorrecta
        assertNotEquals(LoginController.md5("otraContraseña"), user.password);
    }
}