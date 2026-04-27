package mx.unison.modelos;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import mx.unison.controller.LoginController;

import java.sql.SQLException;

public class DatabaseHelper {

    private static final String DATABASE_URL = "jdbc:sqlite:Inventario.db";
    private ConnectionSource connectionSource;

    public DatabaseHelper() throws Exception {
        this.connectionSource = new JdbcConnectionSource(DATABASE_URL);

        // Crear las tablas si no existen
        TableUtils.createTableIfNotExists(connectionSource, Usuario.class);
        TableUtils.createTableIfNotExists(connectionSource, Almacen.class);
        TableUtils.createTableIfNotExists(connectionSource, Producto.class);

        // Insertar usuario administrador por defecto si la tabla de usuarios está vacía
        insertDefaultAdminUser();
    }

    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    public void close() throws Exception {
        if (connectionSource != null) {
            connectionSource.close();
        }
    }

    /**
     * Inserta un usuario administrador por defecto si la tabla "usuarios" está vacía.
     */
    private void insertDefaultAdminUser() throws Exception {
        UsuarioDAO usuarioDAO = new UsuarioDAOImpl(this.connectionSource);

        if (usuarioDAO.buscarTodos().isEmpty()) {
            Usuario admin = new Usuario();
            admin.nombre = "ADMIN";
            admin.password = LoginController.md5("admin123"); // Usar el hashing de la contraseña
            admin.rol = "ADMIN";
            usuarioDAO.crear(admin);
        }
    }
}