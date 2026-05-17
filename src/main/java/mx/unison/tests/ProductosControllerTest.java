package mx.unison.tests;

import mx.unison.controller.ProductosController;
import mx.unison.modelos.Producto;
import mx.unison.modelos.Almacen;
import org.junit.jupiter.api.Test;
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.ComboBox;

import static org.junit.jupiter.api.Assertions.*;

public class ProductosControllerTest {

    @Test
    void asignarDatos_asignaCorrectamente() {
        // Instancia controller y producto
        ProductosController controller = new ProductosController();
        Producto prod = new Producto();
        // Mocks de inputs JavaFX
        TextField nombreTX = new TextField("CocaCola 355ml");
        TextField descTX  = new TextField("Refresco sabor cola");
        Spinner<Integer> cantSpin = new Spinner<>(0, 9999, 10);
        Spinner<Double> precioSpin = new Spinner<>(0.0, 9999.0, 19.5);
        ComboBox<Almacen> cbAlmacen = new ComboBox<>();
        Almacen alm = new Almacen();
        alm.id = 43;
        alm.nombre = "Bodega sur";
        cbAlmacen.getItems().add(alm);
        cbAlmacen.setValue(alm);

        // Llama método a probar
        controller.asignarDatos(prod, nombreTX, descTX, cantSpin, precioSpin, cbAlmacen);

        // Aserciones
        assertEquals("CocaCola 355ml", prod.nombre); // nombre
        assertEquals("Refresco sabor cola", prod.descripcion); // descripcion
        assertEquals(10, prod.cantidad); // cantidad
        assertEquals(19.5, prod.precio); // precio
        assertNotNull(prod.almacen); // almacén asignado
        assertEquals("Bodega sur", prod.almacen.nombre); // almacén correcto
        assertEquals("ADMIN", prod.ultimoUsuario); // usuario actual en test
    }
}