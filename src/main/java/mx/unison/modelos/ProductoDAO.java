package mx.unison.modelos;

import java.util.List;

public interface ProductoDAO {
    void crear(Producto producto) throws Exception;
    Producto buscarPorId(int id) throws Exception;
    List<Producto> buscarTodos() throws Exception;
    void actualizar(Producto producto) throws Exception;
    void eliminar(Producto producto) throws Exception;
}