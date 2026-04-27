package mx.unison.modelos;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import java.util.List;

public class AlmacenDAOImpl implements AlmacenDAO {
    private final Dao<Almacen, Integer> dao;

    public AlmacenDAOImpl(ConnectionSource cs) throws Exception {
        this.dao = DaoManager.createDao(cs, Almacen.class);
    }

    @Override
    public void crear(Almacen almacen) throws Exception {
        dao.create(almacen);
    }

    @Override
    public Almacen buscarPorId(int id) throws Exception {
        return dao.queryForId(id);
    }

    @Override
    public List<Almacen> buscarTodos() throws Exception {
        return dao.queryForAll();
    }

    @Override
    public void actualizar(Almacen almacen) throws Exception {
        dao.update(almacen);
    }

    @Override
    public void eliminar(Almacen almacen) throws Exception {
        dao.delete(almacen);
    }
}