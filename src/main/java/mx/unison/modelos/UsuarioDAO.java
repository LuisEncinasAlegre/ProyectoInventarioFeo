package mx.unison.modelos;

import java.util.List;

public interface UsuarioDAO {
    void crear(Usuario usuario) throws Exception;
    Usuario buscarPorId(int id) throws Exception;
    Usuario buscarPorNombre(String nombre) throws Exception;
    List<Usuario> buscarTodos() throws Exception;
    void actualizar(Usuario usuario) throws Exception;
    void eliminar(Usuario usuario) throws Exception;
}