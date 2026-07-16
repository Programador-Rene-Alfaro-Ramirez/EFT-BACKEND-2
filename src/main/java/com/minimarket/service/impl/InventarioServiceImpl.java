package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    // ============ MÉTODOS CRUD ============

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id).orElse(null);
    }

    @Override
    public Inventario save(Inventario inventario) {
        return inventarioRepository.save(inventario);
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    // ============ NUEVOS MÉTODOS DE LÓGICA DE NEGOCIO ============

    @Override
    @Transactional
    public Inventario registrarMovimiento(Long productoId, Integer cantidad, String tipoMovimiento) {
        // 1. Validar cantidad
        validarCantidad(cantidad);
        
        // 2. Validar tipo de movimiento
        validarTipoMovimiento(tipoMovimiento);
        
        // 3. Buscar producto
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
        
        // 4. Verificar stock para salida
        if (tipoMovimiento.equalsIgnoreCase("SALIDA")) {
            verificarStockDisponible(producto, cantidad);
            producto.setStock(producto.getStock() - cantidad);
        } else {
            // ENTRADA: sumar al stock
            Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;
            producto.setStock(stockActual + cantidad);
        }
        
        // 5. Guardar producto actualizado
        productoRepository.save(producto);
        
        // 6. Crear y guardar movimiento de inventario
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(cantidad);
        inventario.setTipoMovimiento(tipoMovimiento.toUpperCase());
        inventario.setFechaMovimiento(new Date());
        
        return inventarioRepository.save(inventario);
    }

    @Override
    public void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }

    @Override
    public void validarTipoMovimiento(String tipoMovimiento) {
        if (tipoMovimiento == null || tipoMovimiento.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento no puede estar vacío");
        }
        
        String tipo = tipoMovimiento.toUpperCase();
        if (!tipo.equals("ENTRADA") && !tipo.equals("SALIDA")) {
            throw new IllegalArgumentException("Tipo de movimiento no permitido. Use ENTRADA o SALIDA");
        }
    }

    @Override
    public void verificarStockDisponible(Producto producto, Integer cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        
        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;
        if (stockActual < cantidad) {
            throw new RuntimeException("Stock insuficiente. Stock actual: " + stockActual + 
                ", Cantidad solicitada: " + cantidad);
        }
    }

    @Override
    public void verificarStockDisponible(Long productoId, Integer cantidad) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
        verificarStockDisponible(producto, cantidad);
    }
}