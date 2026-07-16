package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Usuario;
import com.minimarket.service.CarritoService;
import com.minimarket.service.UsuarioService;
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
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Endpoints para gestión de carritos de compras")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Obtener todos los carritos", description = "Retorna una lista de todos los carritos con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de carritos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Carrito>>> listarCarrito() {
        List<Carrito> carritos = carritoService.findAll();
        
        List<EntityModel<Carrito>> carritoModels = carritos.stream()
            .map(carrito -> {
                Usuario usuario = usuarioService.findById(carrito.getUsuario().getId()).orElse(null);
                return EntityModel.of(carrito,
                    linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(carrito.getId())).withSelfRel(),
                    linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(carrito.getUsuario().getId())).withRel("usuario"),
                    linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("carritos")
                );
            })
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(CarritoController.class).listarCarrito()).withSelfRel();
        CollectionModel<EntityModel<Carrito>> result = CollectionModel.of(carritoModels, selfLink);
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener carrito por ID", description = "Retorna un carrito específico con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrito encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Carrito>> obtenerCarritoPorId(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        
        EntityModel<Carrito> model = EntityModel.of(carrito,
            linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(id)).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(carrito.getUsuario().getId())).withRel("usuario"),
            linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("carritos"),
            linkTo(methodOn(CarritoController.class).eliminarProductoDelCarrito(id)).withRel("delete")
        );
        
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Agregar producto al carrito", description = "Crea un nuevo carrito o agrega un producto al carrito existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Carrito creado/actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENTE")
    })
    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping
    public ResponseEntity<EntityModel<Carrito>> agregarProductoAlCarrito(@RequestBody Carrito carrito) {
        Carrito nuevoCarrito = carritoService.save(carrito);
        
        EntityModel<Carrito> model = EntityModel.of(nuevoCarrito,
            linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(nuevoCarrito.getId())).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(nuevoCarrito.getUsuario().getId())).withRel("usuario"),
            linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("carritos")
        );
        
        return ResponseEntity.created(linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(nuevoCarrito.getId())).toUri()).body(model);
    }

    @Operation(summary = "Actualizar carrito", description = "Actualiza un carrito existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrito actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENTE")
    })
    @PreAuthorize("hasRole('CLIENTE')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Carrito>> actualizarCarrito(@PathVariable Long id, @RequestBody Carrito carrito) {
        Carrito existente = carritoService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        carrito.setId(id);
        Carrito actualizado = carritoService.save(carrito);
        
        EntityModel<Carrito> model = EntityModel.of(actualizado,
            linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(id)).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(actualizado.getUsuario().getId())).withRel("usuario"),
            linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("carritos")
        );
        
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Eliminar producto del carrito", description = "Elimina un carrito por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Carrito eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENTE")
    })
    @PreAuthorize("hasRole('CLIENTE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        carritoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}