package com.minimarket.service;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;

import java.util.List;

public interface InventarioService {
    
    // ===== MÉTODOS CRUD EXISTENTES =====
    List<Inventario> findAll();
    Inventario findById(Long id);
    Inventario save(Inventario inventario);
    void deleteById(Long id);
    List<Inventario> findByProductoId(Long productoId);
    
    // ===== NUEVOS MÉTODOS DE LÓGICA DE NEGOCIO =====
    Inventario registrarMovimiento(Long productoId, Integer cantidad, String tipoMovimiento);
    void validarCantidad(Integer cantidad);
    void validarTipoMovimiento(String tipoMovimiento);
    void verificarStockDisponible(Producto producto, Integer cantidad);
    void verificarStockDisponible(Long productoId, Integer cantidad);
}