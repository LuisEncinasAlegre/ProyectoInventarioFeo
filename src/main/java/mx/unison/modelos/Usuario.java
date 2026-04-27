package mx.unison.modelos;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "usuarios")
public class Usuario {
    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(unique = true, canBeNull = false)
    public String nombre;

    @DatabaseField(canBeNull = false)
    public String password;

    @DatabaseField(columnName = "Ultimo_Inicio_de_Sesion")
    public String ultimoInicioDeSesion;

    @DatabaseField(canBeNull = false)
    public String rol;
}