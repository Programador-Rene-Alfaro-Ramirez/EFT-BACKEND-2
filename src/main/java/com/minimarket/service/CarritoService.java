package com.minimarket.service;

import com.minimarket.entity.Carrito;

import java.util.List;

public interface CarritoService {
    
    // ===== MÉTODOS CRUD EXISTENTES =====
    List<Carrito> findAll();
    Carrito findById(Long id);
    Carrito save(Carrito carrito);
    void deleteById(Long id);
    List<Carrito> findByUsuarioId(Long usuarioId);
    
    // ===== NUEVOS MÉTODOS DE LÓGICA DE NEGOCIO =====
    Carrito agregarProducto(Long carritoId, Long productoId, Integer cantidad);
    void validarStockDisponible(Long productoId, Integer cantidad);
    Carrito obtenerCarritoPorId(Long id);
    void vaciarCarrito(Long carritoId);
}
