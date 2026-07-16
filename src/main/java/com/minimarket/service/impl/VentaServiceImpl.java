package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Venta;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private CarritoRepository carritoRepository;
    
    @Autowired
    private InventarioService inventarioService;
    
    @Autowired
    private ProductoRepository productoRepository;

    // ============ MÉTODOS CRUD ============

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta save(Venta venta) {
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    // ============ NUEVOS MÉTODOS DE LÓGICA DE NEGOCIO ============

    @Override
    @Transactional
    public Venta procesarVenta(Long carritoId, Long usuarioId) {
        // 1. Validar usuario
        if (usuarioId == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        // 2. Buscar carrito
        Carrito carrito = carritoRepository.findById(carritoId)
            .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));
        
        // 3. Validar que el carrito pertenece al usuario
        if (carrito.getUsuario() == null || !carrito.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("El carrito no pertenece al usuario autenticado");
        }
        
        // 4. Validar carrito no vacío
        validarCarrito(carrito);
        
        // 5. Crear detalles de venta
        List<DetalleVenta> detalles = crearDetallesDesdeCarrito(carrito);
        
        // 6. Verificar stock
        verificarStockEnVenta(detalles);
        
        // 7. Calcular total
        Double total = calcularTotal(detalles);
        
        // 8. Crear venta
        Venta venta = new Venta();
        venta.setUsuario(carrito.getUsuario());
        venta.setFecha(new Date());
        venta.setDetalles(detalles);
        
        // 9. Actualizar stock
        for (DetalleVenta detalle : detalles) {
            inventarioService.registrarMovimiento(
                detalle.getProducto().getId(),
                detalle.getCantidad(),
                "SALIDA"
            );
        }
        
        // 10. Guardar venta
        return ventaRepository.save(venta);
    }

    @Override
    public Double calcularTotal(List<DetalleVenta> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return 0.0;
        }
        
        return detalles.stream()
            .mapToDouble(detalle -> detalle.getPrecio() * detalle.getCantidad())
            .sum();
    }

    @Override
    public void validarCarrito(Carrito carrito) {
        if (carrito == null) {
            throw new RuntimeException("El carrito no puede ser null");
        }
    }

    @Override
    public void verificarStockEnVenta(List<DetalleVenta> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return;
        }
        
        for (DetalleVenta detalle : detalles) {
            inventarioService.verificarStockDisponible(
                detalle.getProducto().getId(),
                detalle.getCantidad()
            );
        }
    }

    // ============ MÉTODO AUXILIAR ============

    private List<DetalleVenta> crearDetallesDesdeCarrito(Carrito carrito) {
        // TODO: Implementar según la relación real de Carrito
        return new ArrayList<>();
    }
}