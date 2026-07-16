package com.minimarket.service;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Venta;

import java.util.List;

public interface VentaService {
    
    // ===== MÉTODOS CRUD EXISTENTES =====
    List<Venta> findAll();
    Venta findById(Long id);
    Venta save(Venta venta);
    List<Venta> findByUsuarioId(Long usuarioId);
    
    // ===== NUEVOS MÉTODOS DE LÓGICA DE NEGOCIO =====
    Venta procesarVenta(Long carritoId, Long usuarioId);
    Double calcularTotal(List<DetalleVenta> detalles);
    void validarCarrito(Carrito carrito);
    void verificarStockEnVenta(List<DetalleVenta> detalles);
}
