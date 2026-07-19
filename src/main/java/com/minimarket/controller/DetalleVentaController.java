package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize; // Import necesario

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/detalle-ventas")
@Tag(name = "DetalleVenta", description = "Endpoints para gestión de detalles de venta")
public class DetalleVentaController {

    private final DetalleVentaService detalleVentaService;

    public DetalleVentaController(DetalleVentaService detalleVentaService) {
        this.detalleVentaService = detalleVentaService;
    }

    @Operation(summary = "Obtener todos los detalles de venta", description = "Retorna una lista de detalles de venta con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de detalles obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DetalleVenta>>> listarDetalleVentas() {
        List<DetalleVenta> detalles = detalleVentaService.findAll();

        List<EntityModel<DetalleVenta>> detalleModels = detalles.stream()
            .map(detalle -> EntityModel.of(detalle,
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(detalle.getId())).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"),
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(detalle.getVenta().getId())).withRel("venta"),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(detalle.getProducto().getId())).withRel("producto")
            ))
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withSelfRel();
        CollectionModel<EntityModel<DetalleVenta>> result = CollectionModel.of(detalleModels, selfLink);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener detalle de venta por ID", description = "Retorna un detalle de venta específico con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DetalleVenta>> obtenerDetalleVentaPorId(@PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta == null) {
            return ResponseEntity.notFound().build();
        }

        EntityModel<DetalleVenta> model = EntityModel.of(detalleVenta,
            linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(id)).withSelfRel(),
            linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"),
            linkTo(methodOn(VentaController.class).obtenerVentaPorId(detalleVenta.getVenta().getId())).withRel("venta"),
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(detalleVenta.getProducto().getId())).withRel("producto"),
            linkTo(methodOn(DetalleVentaController.class).eliminarDetalleVenta(id)).withRel("delete")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Obtener detalles por venta", description = "Retorna los detalles asociados a una venta con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de detalles obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<CollectionModel<EntityModel<DetalleVenta>>> listarDetallesPorVenta(@PathVariable Long ventaId) {
        List<DetalleVenta> detalles = detalleVentaService.findByVentaId(ventaId);

        List<EntityModel<DetalleVenta>> detalleModels = detalles.stream()
            .map(detalle -> EntityModel.of(detalle,
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(detalle.getId())).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"),
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(ventaId)).withRel("venta"),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(detalle.getProducto().getId())).withRel("producto")
            ))
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(DetalleVentaController.class).listarDetallesPorVenta(ventaId)).withSelfRel();
        CollectionModel<EntityModel<DetalleVenta>> result = CollectionModel.of(detalleModels, selfLink);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Crear detalle de venta", description = "Crea un nuevo detalle de venta con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Detalle creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CAJERO")
    })
    @PreAuthorize("hasRole('CAJERO')")
    @PostMapping
    public ResponseEntity<EntityModel<DetalleVenta>> guardarDetalleVenta(@RequestBody DetalleVenta detalleVenta) {
        DetalleVenta nuevoDetalle = detalleVentaService.save(detalleVenta);

        EntityModel<DetalleVenta> model = EntityModel.of(nuevoDetalle,
            linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(nuevoDetalle.getId())).withSelfRel(),
            linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"),
            linkTo(methodOn(VentaController.class).obtenerVentaPorId(nuevoDetalle.getVenta().getId())).withRel("venta"),
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(nuevoDetalle.getProducto().getId())).withRel("producto")
        );

        return ResponseEntity.created(linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(nuevoDetalle.getId())).toUri()).body(model);
    }

    @Operation(summary = "Actualizar detalle de venta", description = "Actualiza un detalle de venta existente con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CAJERO")
    })
    @PreAuthorize("hasRole('CAJERO')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<DetalleVenta>> actualizarDetalleVenta(@PathVariable Long id, @RequestBody DetalleVenta detalleVenta) {
        DetalleVenta existente = detalleVentaService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        detalleVenta.setId(id);
        DetalleVenta actualizado = detalleVentaService.save(detalleVenta);

        EntityModel<DetalleVenta> model = EntityModel.of(actualizado,
            linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(id)).withSelfRel(),
            linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"),
            linkTo(methodOn(VentaController.class).obtenerVentaPorId(actualizado.getVenta().getId())).withRel("venta"),
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(actualizado.getProducto().getId())).withRel("producto")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Eliminar detalle de venta", description = "Elimina un detalle de venta por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Detalle eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CAJERO")
    })
    @PreAuthorize("hasRole('CAJERO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDetalleVenta(@PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta != null) {
            detalleVentaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
