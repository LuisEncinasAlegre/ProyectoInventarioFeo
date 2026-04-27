package mx.unison.modelos;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "almacenes")
public class Almacen {
    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(canBeNull = false)
    public String nombre;

    @DatabaseField
    public String ubicacion;

    @DatabaseField(columnName = "fecha_hora_creacion")
    public String fechaHoraCreacion;

    @DatabaseField(columnName = "fecha_hora_ultima_modificacion")
    public String fechaHoraUltimaMod;

    @DatabaseField(columnName = "ultimo_usuario_en_modificar")
    public String ultimoUsuario;
}