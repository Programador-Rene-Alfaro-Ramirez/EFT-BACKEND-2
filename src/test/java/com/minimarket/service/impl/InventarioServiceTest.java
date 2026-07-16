package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;
    
    @Mock
    private ProductoRepository productoRepository;
    
    @InjectMocks
    private InventarioServiceImpl inventarioService;
    
    private Producto productoMock;
    private Inventario inventarioMock;
    
    @BeforeEach
    void setUp() {
        productoMock = new Producto();
        productoMock.setId(1L);
        productoMock.setNombre("Producto Test");
        productoMock.setStock(20);
        productoMock.setPrecio(1000.0);
        
        inventarioMock = new Inventario();
        inventarioMock.setId(1L);
        inventarioMock.setProducto(productoMock);
        inventarioMock.setCantidad(5);
        inventarioMock.setTipoMovimiento("ENTRADA");
        inventarioMock.setFechaMovimiento(new Date());
    }
    
    // ============ PRUEBAS CRUD ============
    
    @Test
    void testFindAllInventario() {
        when(inventarioRepository.findAll()).thenReturn(Arrays.asList(inventarioMock));
        
        List<Inventario> result = inventarioService.findAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventarioRepository).findAll();
    }
    
    @Test
    void testFindByIdInventarioExistente() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventarioMock));
        
        Inventario result = inventarioService.findById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(inventarioRepository).findById(1L);
    }
    
    @Test
    void testFindByIdInventarioNoExistente() {
        when(inventarioRepository.findById(999L)).thenReturn(Optional.empty());
        
        Inventario result = inventarioService.findById(999L);
        
        assertNull(result);
        verify(inventarioRepository).findById(999L);
    }
    
    @Test
    void testSaveInventario() {
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioMock);
        
        Inventario result = inventarioService.save(inventarioMock);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(inventarioRepository).save(inventarioMock);
    }
    
    @Test
    void testDeleteInventario() {
        doNothing().when(inventarioRepository).deleteById(1L);
        
        inventarioService.deleteById(1L);
        
        verify(inventarioRepository).deleteById(1L);
    }
    
    @Test
    void testFindByProductoId() {
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Arrays.asList(inventarioMock));
        
        List<Inventario> result = inventarioService.findByProductoId(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventarioRepository).findByProductoId(1L);
    }
    
    // ============ PRUEBAS DE LÓGICA DE NEGOCIO ============
    
    @Test
    void testRegistrarMovimientoEntradaExitosa() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioMock);
        
        Inventario result = inventarioService.registrarMovimiento(1L, 5, "ENTRADA");
        
        assertNotNull(result);
        assertEquals("ENTRADA", result.getTipoMovimiento());
        assertEquals(5, result.getCantidad());
        assertEquals(25, productoMock.getStock());
        verify(productoRepository).save(productoMock);
        verify(inventarioRepository).save(any(Inventario.class));
    }
    
    @Test
    void testRegistrarMovimientoSalidaExitosa() {
        // Crear un inventario específico para salida con cantidad 3
        Inventario inventarioSalida = new Inventario();
        inventarioSalida.setId(1L);
        inventarioSalida.setProducto(productoMock);
        inventarioSalida.setCantidad(3);
        inventarioSalida.setTipoMovimiento("SALIDA");
        inventarioSalida.setFechaMovimiento(new Date());
        
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioSalida);
        
        Inventario result = inventarioService.registrarMovimiento(1L, 3, "SALIDA");
        
        assertNotNull(result);
        assertEquals("SALIDA", result.getTipoMovimiento());
        assertEquals(3, result.getCantidad());
        assertEquals(17, productoMock.getStock());
        verify(productoRepository).save(productoMock);
        verify(inventarioRepository).save(any(Inventario.class));
    }
    
    @Test
    void testRegistrarMovimientoCantidadNula() {
        assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.registrarMovimiento(1L, null, "ENTRADA"));
    }
    
    @Test
    void testRegistrarMovimientoCantidadCero() {
        assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.registrarMovimiento(1L, 0, "ENTRADA"));
    }
    
    @Test
    void testRegistrarMovimientoCantidadNegativa() {
        assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.registrarMovimiento(1L, -5, "ENTRADA"));
    }
    
    @Test
    void testRegistrarMovimientoTipoInvalido() {
        assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.registrarMovimiento(1L, 5, "TRANSFERENCIA"));
    }
    
    @Test
    void testRegistrarMovimientoTipoNull() {
        assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.registrarMovimiento(1L, 5, null));
    }
    
    @Test
    void testRegistrarMovimientoTipoVacio() {
        assertThrows(IllegalArgumentException.class, 
            () -> inventarioService.registrarMovimiento(1L, 5, ""));
    }
    
    @Test
    void testRegistrarMovimientoProductoNoExistente() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, 
            () -> inventarioService.registrarMovimiento(999L, 5, "ENTRADA"));
    }
    
    @Test
    void testRegistrarMovimientoSalidaStockInsuficiente() {
        productoMock.setStock(3);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));
        
        assertThrows(RuntimeException.class, 
            () -> inventarioService.registrarMovimiento(1L, 5, "SALIDA"));
    }
    
    @Test
    void testVerificarStockDisponibleConProductoId() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));
        
        assertDoesNotThrow(() -> inventarioService.verificarStockDisponible(1L, 5));
    }
    
    @Test
    void testVerificarStockDisponibleConProductoIdInsuficiente() {
        productoMock.setStock(3);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));
        
        assertThrows(RuntimeException.class, 
            () -> inventarioService.verificarStockDisponible(1L, 5));
    }
    
    @Test
    void testVerificarStockDisponibleConProductoIdNoExistente() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, 
            () -> inventarioService.verificarStockDisponible(999L, 5));
    }
}