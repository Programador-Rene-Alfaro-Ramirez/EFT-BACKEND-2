package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    // ============ MÉTODOS CRUD ============

    @Override
    public List<Carrito> findAll() {
        return carritoRepository.findAll();
    }

    @Override
    public Carrito findById(Long id) {
        return carritoRepository.findById(id).orElse(null);
    }

    @Override
    public Carrito save(Carrito carrito) {
        return carritoRepository.save(carrito);
    }

    @Override
    public void deleteById(Long id) {
        carritoRepository.deleteById(id);
    }

    @Override
    public List<Carrito> findByUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    // ============ NUEVOS MÉTODOS DE LÓGICA DE NEGOCIO ============

    @Override
    @Transactional
    public Carrito agregarProducto(Long carritoId, Long productoId, Integer cantidad) {
        // 1. Validar cantidad
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        
        // 2. Validar stock disponible
        validarStockDisponible(productoId, cantidad);
        
        // 3. Buscar carrito
        Carrito carrito = carritoRepository.findById(carritoId)
            .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));
        
        // 4. Buscar producto
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
        
        // 5. Agregar producto al carrito
        // TODO: Implementar según la estructura de Carrito
        // Ejemplo si Carrito tiene List<ItemCarrito>:
        /*
        ItemCarrito item = new ItemCarrito();
        item.setProducto(producto);
        item.setCantidad(cantidad);
        carrito.getItems().add(item);
        */
        
        // 6. Guardar carrito
        return carritoRepository.save(carrito);
    }

    @Override
    public void validarStockDisponible(Long productoId, Integer cantidad) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
        
        Integer stockActual = producto.getStock() != null ? producto.getStock() : 0;
        if (stockActual < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre() + 
                ". Stock actual: " + stockActual + ", Cantidad solicitada: " + cantidad);
        }
    }

    @Override
    public Carrito obtenerCarritoPorId(Long id) {
        return findById(id);
    }

    @Override
    @Transactional
    public void vaciarCarrito(Long carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
            .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));
        
        // TODO: Limpiar los items del carrito
        // carrito.setItems(new ArrayList<>());
        // carritoRepository.save(carrito);
    }
}