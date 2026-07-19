package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;
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
@RequestMapping("/api/productos")
@Tag(name = "Producto", description = "Endpoints para gestión de productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Obtener todos los productos", description = "Retorna una lista de productos con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listarProductos() {
        List<Producto> productos = productoService.findAll();

        List<EntityModel<Producto>> productoModels = productos.stream()
            .map(producto -> EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"),
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(producto.getCategoria().getId())).withRel("categoria")
            ))
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(ProductoController.class).listarProductos()).withSelfRel();
        CollectionModel<EntityModel<Producto>> result = CollectionModel.of(productoModels, selfLink);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener producto por ID", description = "Retorna un producto específico con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> obtenerProductoPorId(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        EntityModel<Producto> model = EntityModel.of(producto,
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(id)).withSelfRel(),
            linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"),
            linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(producto.getCategoria().getId())).withRel("categoria"),
            linkTo(methodOn(ProductoController.class).eliminarProducto(id)).withRel("delete")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Obtener productos por categoría", description = "Retorna una lista de productos filtrada por categoría con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listarProductosPorCategoria(@PathVariable Long categoriaId) {
        List<Producto> productos = productoService.findByCategoriaId(categoriaId);

        List<EntityModel<Producto>> productoModels = productos.stream()
            .map(producto -> EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"),
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoriaId)).withRel("categoria")
            ))
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(ProductoController.class).listarProductosPorCategoria(categoriaId)).withSelfRel();
        CollectionModel<EntityModel<Producto>> result = CollectionModel.of(productoModels, selfLink);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Crear producto", description = "Crea un nuevo producto con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EntityModel<Producto>> guardarProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoService.save(producto);

        EntityModel<Producto> model = EntityModel.of(nuevoProducto,
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(nuevoProducto.getId())).withSelfRel(),
            linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"),
            linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(nuevoProducto.getCategoria().getId())).withRel("categoria")
        );

        return ResponseEntity.created(linkTo(methodOn(ProductoController.class).obtenerProductoPorId(nuevoProducto.getId())).toUri()).body(model);
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        Producto productoExistente = productoService.findById(id);
        if (productoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        producto.setId(id);
        Producto actualizado = productoService.save(producto);

        EntityModel<Producto> model = EntityModel.of(actualizado,
            linkTo(methodOn(ProductoController.class).obtenerProductoPorId(id)).withSelfRel(),
            linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"),
            linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(actualizado.getCategoria().getId())).withRel("categoria")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Eliminar producto", description = "Elimina un producto por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
} 