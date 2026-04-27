package mx.unison.modelos;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper {

    private static final String DATABASE_URL = "jdbc:sqlite:Inventario.db";
    private ConnectionSource connectionSource;

    public DatabaseHelper() throws Exception {
        this.connectionSource = new JdbcConnectionSource(DATABASE_URL);
        TableUtils.createTableIfNotExists(connectionSource, Usuario.class);
        TableUtils.createTableIfNotExists(connectionSource, Almacen.class);
        TableUtils.createTableIfNotExists(connectionSource, Producto.class);
    }

    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    public void close() throws Exception {
        if (connectionSource != null) {
            connectionSource.close();
        }
    }
}