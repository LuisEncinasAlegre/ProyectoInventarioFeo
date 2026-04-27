package mx.unison.modelos;

import java.util.List;

public interface AlmacenDAO {
    void crear(Almacen almacen) throws Exception;
    Almacen buscarPorId(int id) throws Exception;
    List<Almacen> buscarTodos() throws Exception;
    void actualizar(Almacen almacen) throws Exception;
    void eliminar(Almacen almacen) throws Exception;
}