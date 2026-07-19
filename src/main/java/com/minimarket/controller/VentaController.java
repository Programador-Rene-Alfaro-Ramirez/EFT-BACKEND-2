package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
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
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Venta", description = "Endpoints para gestión de ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @Operation(summary = "Obtener todas las ventas", description = "Retorna una lista de ventas con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Venta.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> listarVentas() {
        List<Venta> ventas = ventaService.findAll();

        List<EntityModel<Venta>> ventaModels = ventas.stream()
            .map(venta -> EntityModel.of(venta,
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"),
                linkTo(methodOn(DetalleVentaController.class).listarDetallesPorVenta(venta.getId())).withRel("detalles"),
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(venta.getUsuario().getId())).withRel("usuario")
            ))
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel();
        CollectionModel<EntityModel<Venta>> result = CollectionModel.of(ventaModels, selfLink);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener venta por ID", description = "Retorna una venta específica con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venta encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Venta.class))),
        @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Venta>> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        if (venta == null) {
            return ResponseEntity.notFound().build();
        }

        EntityModel<Venta> model = EntityModel.of(venta,
            linkTo(methodOn(VentaController.class).obtenerVentaPorId(id)).withSelfRel(),
            linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"),
            linkTo(methodOn(DetalleVentaController.class).listarDetallesPorVenta(id)).withRel("detalles"),
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(venta.getUsuario().getId())).withRel("usuario")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Registrar venta", description = "Crea una nueva venta con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Venta creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Venta.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CAJERO")
    })
    @PreAuthorize("hasRole('CAJERO')")
    @PostMapping
    public ResponseEntity<EntityModel<Venta>> guardarVenta(@RequestBody Venta venta) {
        Venta nuevaVenta = ventaService.save(venta);

        EntityModel<Venta> model = EntityModel.of(nuevaVenta,
            linkTo(methodOn(VentaController.class).obtenerVentaPorId(nuevaVenta.getId())).withSelfRel(),
            linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"),
            linkTo(methodOn(DetalleVentaController.class).listarDetallesPorVenta(nuevaVenta.getId())).withRel("detalles"),
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(nuevaVenta.getUsuario().getId())).withRel("usuario")
        );

        return ResponseEntity.created(linkTo(methodOn(VentaController.class).obtenerVentaPorId(nuevaVenta.getId())).toUri()).body(model);
    }

    @Operation(summary = "Procesar venta", description = "Procesa la venta desde un carrito y devuelve la venta creada con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venta procesada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Venta.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CAJERO")
    })
    @PreAuthorize("hasRole('CAJERO')")
    @PostMapping("/procesar")
    public ResponseEntity<EntityModel<Venta>> procesarVenta(@RequestBody Map<String, Object> request) {
        Long carritoId = request.get("carritoId") == null ? null : Long.valueOf(request.get("carritoId").toString());
        Long usuarioId = request.get("usuarioId") == null ? null : Long.valueOf(request.get("usuarioId").toString());
        Venta ventaProcesada = ventaService.procesarVenta(carritoId, usuarioId);

        EntityModel<Venta> model = EntityModel.of(ventaProcesada,
            linkTo(methodOn(VentaController.class).obtenerVentaPorId(ventaProcesada.getId())).withSelfRel(),
            linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"),
            linkTo(methodOn(DetalleVentaController.class).listarDetallesPorVenta(ventaProcesada.getId())).withRel("detalles"),
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(ventaProcesada.getUsuario().getId())).withRel("usuario")
        );

        return ResponseEntity.ok(model);
    }
}