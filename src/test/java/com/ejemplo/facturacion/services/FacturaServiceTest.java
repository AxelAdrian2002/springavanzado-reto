package com.ejemplo.facturacion.services;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ejemplo.facturacion.valueobjects.Articulo;
import com.ejemplo.facturacion.valueobjects.Direccion;
import com.ejemplo.facturacion.valueobjects.Factura;
import com.ejemplo.facturacion.valueobjects.Orden;

/**
 * Pruebas unitarias para FacturaService
 * 
 * Cubre todos los métodos públicos incluyendo:
 * - generarFactura (síncrono)
 * - iniciarFacturaAsincrona
 * - obtenerFacturaAsincrona
 * - crearFacturaAsincrona (asíncrono con @Async)
 * - getFacturas
 * - setFacturas
 */
@SpringBootTest
@DisplayName("Pruebas unitarias para FacturaService")
public class FacturaServiceTest {

    @Autowired
    private FacturaService facturaService;

    private Orden ordenPrueba;
    private List<Articulo> articulosPrueba;

    @BeforeEach
    public void setUp() {
        // Limpiar el mapa de facturas antes de cada prueba
        facturaService.setFacturas(new java.util.HashMap<>());

        // Crear artículos de prueba
        articulosPrueba = new ArrayList<>();

        Articulo articulo1 = new Articulo();
        articulo1.setProducto("PANTALON");
        articulo1.setCantidad(2);
        articulo1.setPrecioUnitario(BigDecimal.valueOf(4.55));
        articulosPrueba.add(articulo1);

        Articulo articulo2 = new Articulo();
        articulo2.setProducto("CAMISA");
        articulo2.setCantidad(3);
        articulo2.setPrecioUnitario(BigDecimal.valueOf(7.88));
        articulosPrueba.add(articulo2);

        // Crear dirección de prueba
        Direccion direccion = new Direccion();
        direccion.setPais("MX");
        direccion.setEstado("NUEVO LEON");
        direccion.setMunicipio("MONTERREY");
        direccion.setCiudad("MONTERREY");
        direccion.setCalle("Eugenio Garza Sada");
        direccion.setNumero("2501 Sur");
        direccion.setTelefono("81 8358 2000");
        direccion.setCodigoPostal("64700");

        // Crear orden de prueba
        ordenPrueba = new Orden();
        ordenPrueba.setId(1L);
        ordenPrueba.setUsuario("egarza");
        ordenPrueba.setDireccionCliente(direccion);
        ordenPrueba.setArticulos(articulosPrueba);
    }

    @Test
    @DisplayName("Test 1: generarFactura debe crear una factura con cálculos correctos")
    public void testGenerarFactura_CalculosCorrectos() throws InterruptedException {

        Factura factura = facturaService.generarFactura(ordenPrueba);

        assertNotNull(factura, "La factura no debe ser null");
        assertNotNull(factura.getId(), "El ID de la factura no debe ser null");
        assertEquals(articulosPrueba, factura.getArticulos(), "Los artículos deben coincidir");

        // Verificar cálculos
        assertEquals(0, new BigDecimal("32.74").compareTo(factura.getSubtotal()),
                "El subtotal debe ser 32.74");

        // IVA: 32.74 * 0.16 = 5.24
        assertEquals(0, new BigDecimal("5.24").compareTo(factura.getIva()),
                "El IVA debe ser 5.24");

        // Total: 32.74 + 5.24 = 37.98
        assertEquals(0, new BigDecimal("37.98").compareTo(factura.getTotal()),
                "El total debe ser 37.98");
    }

    @Test
    @DisplayName("Test 2: generarFactura debe almacenar la factura en el mapa")
    public void testGenerarFactura_AlmacenaEnMapa() throws InterruptedException {

        Factura factura = facturaService.generarFactura(ordenPrueba);

        Map<String, Optional<Factura>> facturas = facturaService.getFacturas();
        assertTrue(facturas.containsKey(factura.getId()),
                "El mapa debe contener la factura generada");
        assertTrue(facturas.get(factura.getId()).isPresent(),
                "La factura debe estar presente en el Optional");
        assertEquals(factura, facturas.get(factura.getId()).get(),
                "La factura almacenada debe ser la misma");
    }

    @Test
    @DisplayName("Test 3: generarFactura debe generar ID único")
    public void testGenerarFactura_IDUnico() throws InterruptedException {

        Factura factura1 = facturaService.generarFactura(ordenPrueba);
        Thread.sleep(10); 
        Factura factura2 = facturaService.generarFactura(ordenPrueba);
        assertNotEquals(factura1.getId(), factura2.getId(),
                "Los IDs deben ser únicos");
    }

    @Test
    @DisplayName("Test 4: generarFactura con artículos vacíos debe retornar totales en cero")
    public void testGenerarFactura_ArticulosVacios() throws InterruptedException {
        ordenPrueba.setArticulos(new ArrayList<>());

        Factura factura = facturaService.generarFactura(ordenPrueba);

        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getSubtotal()),
                "El subtotal debe ser 0");
        assertEquals(0, new BigDecimal("0.00").compareTo(factura.getIva()),
                "El IVA debe ser 0.00");
        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getTotal()),
                "El total debe ser 0");
    }

    @Test
    @DisplayName("Test 5: generarFactura con artículos null debe manejar correctamente")
    public void testGenerarFactura_ArticulosNull() throws InterruptedException {

        ordenPrueba.setArticulos(null);

        Factura factura = facturaService.generarFactura(ordenPrueba);

        assertEquals(0, BigDecimal.ZERO.compareTo(factura.getSubtotal()),
                "El subtotal debe ser 0 cuando los artículos son null");
    }

    @Test
    @DisplayName("Test 6: iniciarFacturaAsincrona debe retornar ID inmediatamente")
    public void testIniciarFacturaAsincrona_RetornaIDInmediato() throws InterruptedException {

        long startTime = System.currentTimeMillis();
        String idFactura = facturaService.iniciarFacturaAsincrona(ordenPrueba);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertNotNull(idFactura, "El ID de la factura no debe ser null");
        assertTrue(duration < 1000,
                "El método debe retornar en menos de 1 segundo (fue: " + duration + "ms)");
    }

    @Test
    @DisplayName("Test 7: iniciarFacturaAsincrona debe crear entrada con Optional.empty")
    public void testIniciarFacturaAsincrona_CreaEntradaVacia() throws InterruptedException {

        String idFactura = facturaService.iniciarFacturaAsincrona(ordenPrueba);

        Map<String, Optional<Factura>> facturas = facturaService.getFacturas();
        assertTrue(facturas.containsKey(idFactura),
                "El mapa debe contener el ID de la factura");

        Optional<Factura> facturaOpt = facturas.get(idFactura);
        assertNotNull(facturaOpt, "El Optional no debe ser null");
        assertTrue(facturaOpt.isEmpty(),
                "El Optional debe estar vacío (procesamiento en curso)");
    }

    @Test
    @DisplayName("Test 8: crearFacturaAsincrona debe completar el procesamiento")
    public void testCrearFacturaAsincrona_CompletaProcesamiento() throws InterruptedException {

        String idFactura = facturaService.iniciarFacturaAsincrona(ordenPrueba);

        Thread.sleep(6000); 

        // Assert
        Optional<Factura> facturaOpt = facturaService.obtenerFacturaAsincrona(idFactura);
        assertNotNull(facturaOpt, "El Optional no debe ser null");
        assertTrue(facturaOpt.isPresent(),
                "La factura debe estar presente después del procesamiento");

        Factura factura = facturaOpt.get();
        assertNotNull(factura, "La factura no debe ser null");
        assertEquals(idFactura, factura.getId(), "El ID debe coincidir");

        // Verificar cálculos
        assertEquals(0, new BigDecimal("32.74").compareTo(factura.getSubtotal()),
                "El subtotal debe ser 32.74");
        assertEquals(0, new BigDecimal("5.24").compareTo(factura.getIva()),
                "El IVA debe ser 5.24");
        assertEquals(0, new BigDecimal("37.98").compareTo(factura.getTotal()),
                "El total debe ser 37.98");
    }

    @Test
    @DisplayName("Test 9: obtenerFacturaAsincrona debe retornar null para ID inexistente")
    public void testObtenerFacturaAsincrona_IDInexistente() {
       
        Optional<Factura> facturaOpt = facturaService.obtenerFacturaAsincrona("999999");

        assertNull(facturaOpt,
                "Debe retornar null cuando el ID no existe");
    }

    @Test
    @DisplayName("Test 10: obtenerFacturaAsincrona debe retornar Optional.empty en proceso")
    public void testObtenerFacturaAsincrona_EnProceso() throws InterruptedException {
        String idFactura = facturaService.iniciarFacturaAsincrona(ordenPrueba);

        Thread.sleep(100); // Pequeña espera para que se registre
        Optional<Factura> facturaOpt = facturaService.obtenerFacturaAsincrona(idFactura);

        assertNotNull(facturaOpt, "El Optional no debe ser null");
        assertTrue(facturaOpt.isEmpty(),
                "El Optional debe estar vacío mientras está en proceso");
    }

    @Test
    @DisplayName("Test 11: obtenerFacturaAsincrona debe retornar factura completada")
    public void testObtenerFacturaAsincrona_Completada() throws InterruptedException {

        String idFactura = facturaService.iniciarFacturaAsincrona(ordenPrueba);
        Thread.sleep(6000); 

        Optional<Factura> facturaOpt = facturaService.obtenerFacturaAsincrona(idFactura);

        assertNotNull(facturaOpt, "El Optional no debe ser null");
        assertTrue(facturaOpt.isPresent(),
                "El Optional debe contener la factura completada");
        assertEquals(idFactura, facturaOpt.get().getId(),
                "El ID de la factura debe coincidir");
    }

    @Test
    @DisplayName("Test 12: getFacturas debe retornar el mapa de facturas")
    public void testGetFacturas() {

        Map<String, Optional<Factura>> facturas = facturaService.getFacturas();

        assertNotNull(facturas, "El mapa de facturas no debe ser null");
        assertTrue(facturas instanceof Map, "Debe retornar un Map");
    }

    @Test
    @DisplayName("Test 13: setFacturas debe actualizar el mapa de facturas")
    public void testSetFacturas() {

        Map<String, Optional<Factura>> nuevoMapa = new java.util.HashMap<>();
        Factura facturaTest = new Factura();
        facturaTest.setId("test123");
        nuevoMapa.put("test123", Optional.of(facturaTest));

        facturaService.setFacturas(nuevoMapa);
        Map<String, Optional<Factura>> facturasActualizadas = facturaService.getFacturas();

        assertEquals(nuevoMapa, facturasActualizadas,
                "El mapa debe ser actualizado correctamente");
        assertTrue(facturasActualizadas.containsKey("test123"),
                "El nuevo mapa debe contener la factura de prueba");
    }

    @Test
    @DisplayName("Test 14: Procesamiento asíncrono múltiple debe manejar varias facturas")
    public void testProcesamientoAsincrono_MultiplesFacturas() throws InterruptedException {

        Orden orden1 = crearOrdenPrueba("usuario1");
        Orden orden2 = crearOrdenPrueba("usuario2");
        Orden orden3 = crearOrdenPrueba("usuario3");

        String id1 = facturaService.iniciarFacturaAsincrona(orden1);
        String id2 = facturaService.iniciarFacturaAsincrona(orden2);
        String id3 = facturaService.iniciarFacturaAsincrona(orden3);

        // Verificar que todas están en proceso
        assertTrue(facturaService.obtenerFacturaAsincrona(id1).isEmpty(),
                "Factura 1 debe estar en proceso");
        assertTrue(facturaService.obtenerFacturaAsincrona(id2).isEmpty(),
                "Factura 2 debe estar en proceso");
        assertTrue(facturaService.obtenerFacturaAsincrona(id3).isEmpty(),
                "Factura 3 debe estar en proceso");

        Thread.sleep(6000);

        assertTrue(facturaService.obtenerFacturaAsincrona(id1).isPresent(),
                "Factura 1 debe estar completada");
        assertTrue(facturaService.obtenerFacturaAsincrona(id2).isPresent(),
                "Factura 2 debe estar completada");
        assertTrue(facturaService.obtenerFacturaAsincrona(id3).isPresent(),
                "Factura 3 debe estar completada");
    }

    @Test
    @DisplayName("Test 15: Cálculo de IVA con redondeo UP debe ser correcto")
    public void testCalculoIVA_RedondeoUP() throws InterruptedException {
        // Arrange - Crear orden que genere IVA con decimales
        Articulo articulo = new Articulo();
        articulo.setProducto("PRODUCTO_TEST");
        articulo.setCantidad(1);
        articulo.setPrecioUnitario(BigDecimal.valueOf(10.55));

        List<Articulo> articulos = new ArrayList<>();
        articulos.add(articulo);
        ordenPrueba.setArticulos(articulos);

        Factura factura = facturaService.generarFactura(ordenPrueba);

        // IVA: 10.55 * 0.16 = 1.69
        assertEquals(0, new BigDecimal("10.55").compareTo(factura.getSubtotal()),
                "El subtotal debe ser 10.55");
        assertEquals(0, new BigDecimal("1.69").compareTo(factura.getIva()),
                "El IVA debe ser 1.69 (redondeado)");
        assertEquals(0, new BigDecimal("12.24").compareTo(factura.getTotal()),
                "El total debe ser 12.24");
    }

    // Método auxiliar
    private Orden crearOrdenPrueba(String usuario) {
        Articulo articulo = new Articulo();
        articulo.setProducto("PRODUCTO");
        articulo.setCantidad(1);
        articulo.setPrecioUnitario(BigDecimal.valueOf(10.00));

        List<Articulo> articulos = new ArrayList<>();
        articulos.add(articulo);

        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setArticulos(articulos);

        return orden;
    }
}
