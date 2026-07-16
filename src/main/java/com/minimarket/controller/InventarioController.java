package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.service.InventarioService;
import com.minimarket.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Endpoints para gestión de movimientos de inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoService productoService;

    @Operation(summary = "Obtener todos los movimientos de inventario", description = "Retorna una lista de todos los movimientos con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de movimientos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Inventario>>> listarMovimientosDeInventario() {
        List<Inventario> inventarios = inventarioService.findAll();
        
        List<EntityModel<Inventario>> inventarioModels = inventarios.stream()
            .map(inv -> {
                Producto producto = productoService.findById(inv.getProducto().getId());
                return EntityModel.of(inv,
                    linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(inv.getId())).withSelfRel(),
                    linkTo(methodOn(ProductoController.class).obtenerProductoPorId(inv.getProducto().getId())).withRel("producto"),
                    linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("inventario")
                );
            })
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withSelfRel();
        CollectionModel<EntityModel<Inventario>> result = CollectionModel.of(inventarioModels, selfLink);
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener movimiento de inventario por ID", description = "Retorna un movimiento específico con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Inventario>> obtenerMovimientoPorId(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario == null) {
            return ResponseEntity.notFound().build();
        }
        
        EntityModel<Inventario> model = EntityModel.of(inventario,
            linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(id)).withSelfRel(),
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(inventario.getProducto().getId())).withRel("producto"),
            linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("inventario"),
            linkTo(methodOn(InventarioController.class).eliminarMovimiento(id)).withRel("delete")
        );
        
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Registrar movimiento de inventario", description = "Crea un nuevo movimiento de inventario (ENTRADA o SALIDA)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movimiento creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EntityModel<Inventario>> registrarMovimiento(@RequestBody Inventario inventario) {
        Inventario nuevoInventario = inventarioService.save(inventario);
        
        EntityModel<Inventario> model = EntityModel.of(nuevoInventario,
            linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(nuevoInventario.getId())).withSelfRel(),
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(nuevoInventario.getProducto().getId())).withRel("producto"),
            linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("inventario")
        );
        
        return ResponseEntity.created(linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(nuevoInventario.getId())).toUri()).body(model);
    }

    @Operation(summary = "Actualizar movimiento de inventario", description = "Actualiza un movimiento de inventario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Inventario>> actualizarMovimiento(@PathVariable Long id, @RequestBody Inventario inventario) {
        Inventario existente = inventarioService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        inventario.setId(id);
        Inventario actualizado = inventarioService.save(inventario);
        
        EntityModel<Inventario> model = EntityModel.of(actualizado,
            linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(id)).withSelfRel(),
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(actualizado.getProducto().getId())).withRel("producto"),
            linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("inventario")
        );
        
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Eliminar movimiento de inventario", description = "Elimina un movimiento de inventario por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Movimiento eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario == null) {
            return ResponseEntity.notFound().build();
        }
        inventarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener movimientos por producto", description = "Retorna los movimientos de inventario de un producto específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimientos encontrados",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<CollectionModel<EntityModel<Inventario>>> getInventarioByProducto(@PathVariable Long productoId) {
        List<Inventario> inventarios = inventarioService.findByProductoId(productoId);
        
        List<EntityModel<Inventario>> inventarioModels = inventarios.stream()
            .map(inv -> EntityModel.of(inv,
                linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(inv.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(productoId)).withRel("producto"),
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withRel("inventario")
            ))
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(InventarioController.class).getInventarioByProducto(productoId)).withSelfRel();
        CollectionModel<EntityModel<Inventario>> result = CollectionModel.of(inventarioModels, selfLink);
        
        return ResponseEntity.ok(result);
    }
}