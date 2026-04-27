package mx.unison.modelos;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {
    private final Dao<Usuario, Integer> dao;

    public UsuarioDAOImpl(ConnectionSource cs) throws Exception {
        this.dao = DaoManager.createDao(cs, Usuario.class);
    }

    @Override
    public void crear(Usuario usuario) throws Exception {
        dao.create(usuario);
    }

    @Override
    public Usuario buscarPorId(int id) throws Exception {
        return dao.queryForId(id);
    }

    @Override
    public Usuario buscarPorNombre(String nombre) throws Exception {
        List<Usuario> result = dao.queryForEq("nombre", nombre);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<Usuario> buscarTodos() throws Exception {
        return dao.queryForAll();
    }

    @Override
    public void actualizar(Usuario usuario) throws Exception {
        dao.update(usuario);
    }

    @Override
    public void eliminar(Usuario usuario) throws Exception {
        dao.delete(usuario);
    }
}