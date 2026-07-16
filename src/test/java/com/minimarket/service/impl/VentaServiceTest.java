package com.minimarket.service.impl;

import com.minimarket.entity.*;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;
    
    @Mock
    private CarritoRepository carritoRepository;
    
    @Mock
    private InventarioService inventarioService;
    
    @InjectMocks
    private VentaServiceImpl ventaService;
    
    private Usuario usuarioMock;
    private Carrito carritoMock;
    private Venta ventaMock;
    private DetalleVenta detalleMock;
    private Producto productoMock;
    
    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("Usuario Test");  // ✅ CORREGIDO: setUsername
        
        productoMock = new Producto();
        productoMock.setId(1L);
        productoMock.setNombre("Producto Test");
        productoMock.setPrecio(1000.0);
        productoMock.setStock(10);
        
        carritoMock = new Carrito();
        carritoMock.setId(1L);
        carritoMock.setUsuario(usuarioMock);
        
        detalleMock = new DetalleVenta();
        detalleMock.setId(1L);
        detalleMock.setProducto(productoMock);
        detalleMock.setCantidad(2);
        detalleMock.setPrecio(1000.0);
        
        ventaMock = new Venta();
        ventaMock.setId(1L);
        ventaMock.setUsuario(usuarioMock);
        ventaMock.setFecha(new Date());
        ventaMock.setDetalles(new ArrayList<>());
        ventaMock.getDetalles().add(detalleMock);
    }
    
    // ============ PRUEBAS CRUD ============
    
    @Test
    void testFindAllVenta() {
        when(ventaRepository.findAll()).thenReturn(List.of(ventaMock));
        
        List<Venta> result = ventaService.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ventaRepository).findAll();
    }
    
    @Test
    void testFindByIdVentaExistente() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaMock));
        
        Venta result = ventaService.findById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(ventaRepository).findById(1L);
    }
    
    @Test
    void testFindByIdVentaNoExistente() {
        when(ventaRepository.findById(999L)).thenReturn(Optional.empty());
        
        Venta result = ventaService.findById(999L);
        
        assertNull(result);
        verify(ventaRepository).findById(999L);
    }
    
    @Test
    void testSaveVenta() {
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaMock);
        
        Venta result = ventaService.save(ventaMock);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(ventaRepository).save(ventaMock);
    }
    
    @Test
    void testFindByUsuarioId() {
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(ventaMock));
        
        List<Venta> result = ventaService.findByUsuarioId(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ventaRepository).findByUsuarioId(1L);
    }
    
    // ============ PRUEBAS DE LÓGICA DE NEGOCIO ============
    
    @Test
    void testCalcularTotalConDetalles() {
        List<DetalleVenta> detalles = new ArrayList<>();
        detalles.add(detalleMock);
        
        DetalleVenta detalle2 = new DetalleVenta();
        detalle2.setProducto(productoMock);
        detalle2.setCantidad(3);
        detalle2.setPrecio(500.0);
        detalles.add(detalle2);
        
        Double total = ventaService.calcularTotal(detalles);
        
        assertEquals(3500.0, total);
    }
    
    @Test
    void testCalcularTotalConDetallesVacios() {
        Double total = ventaService.calcularTotal(new ArrayList<>());
        assertEquals(0.0, total);
    }
    
    @Test
    void testCalcularTotalConDetallesNull() {
        Double total = ventaService.calcularTotal(null);
        assertEquals(0.0, total);
    }
    
    @Test
    void testValidarCarritoValido() {
        assertDoesNotThrow(() -> ventaService.validarCarrito(carritoMock));
    }
    
    @Test
    void testValidarCarritoNull() {
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ventaService.validarCarrito(null));
        assertEquals("El carrito no puede ser null", exception.getMessage());
    }
    
    @Test
    void testVerificarStockEnVenta() {
        List<DetalleVenta> detalles = List.of(detalleMock);
        assertDoesNotThrow(() -> ventaService.verificarStockEnVenta(detalles));
    }
    
    @Test
    void testVerificarStockEnVentaConDetallesNull() {
        assertDoesNotThrow(() -> ventaService.verificarStockEnVenta(null));
    }
    
    @Test
    void testVerificarStockEnVentaConDetallesVacios() {
        assertDoesNotThrow(() -> ventaService.verificarStockEnVenta(new ArrayList<>()));
    }
    
    @Test
    void testProcesarVentaUsuarioNoAutenticado() {
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ventaService.procesarVenta(1L, null));
        assertEquals("Usuario no autenticado", exception.getMessage());
    }
    
    @Test
    void testProcesarVentaCarritoNoEncontrado() {
        when(carritoRepository.findById(999L)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ventaService.procesarVenta(999L, 1L));
        assertEquals("Carrito no encontrado con ID: 999", exception.getMessage());
    }
    
    @Test
    void testProcesarVentaCarritoNoPerteneceAlUsuario() {
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(2L);
        carritoMock.setUsuario(otroUsuario);
        
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carritoMock));
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ventaService.procesarVenta(1L, 1L));
        assertEquals("El carrito no pertenece al usuario autenticado", exception.getMessage());
    }
}