package mx.unison.modelos;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import java.util.List;

public class ProductoDAOImpl implements ProductoDAO {
    private final Dao<Producto, Integer> dao;

    public ProductoDAOImpl(ConnectionSource cs) throws Exception {
        this.dao = DaoManager.createDao(cs, Producto.class);
    }

    @Override
    public void crear(Producto producto) throws Exception {
        dao.create(producto);
    }

    @Override
    public Producto buscarPorId(int id) throws Exception {
        return dao.queryForId(id);
    }

    @Override
    public List<Producto> buscarTodos() throws Exception {
        return dao.queryForAll();
    }

    @Override
    public void actualizar(Producto producto) throws Exception {
        dao.update(producto);
    }

    @Override
    public void eliminar(Producto producto) throws Exception {
        dao.delete(producto);
    }
}