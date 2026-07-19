package com.minimarket.controller;

import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@Tag(name = "Categoria", description = "Endpoints para gestión de categorías")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(summary = "Obtener todas las categorías", description = "Retorna una lista de categorías con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Categoria>>> listarCategorias() {
        List<Categoria> categorias = categoriaService.findAll();

        List<EntityModel<Categoria>> categoriaModels = categorias.stream()
            .map(categoria -> EntityModel.of(categoria,
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("categorias"),
                linkTo(methodOn(ProductoController.class).listarProductosPorCategoria(categoria.getId())).withRel("productos")
            ))
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(CategoriaController.class).listarCategorias()).withSelfRel();
        CollectionModel<EntityModel<Categoria>> result = CollectionModel.of(categoriaModels, selfLink);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener categoría por ID", description = "Retorna una categoría específica con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Categoria>> obtenerCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria == null) {
            return ResponseEntity.notFound().build();
        }

        EntityModel<Categoria> model = EntityModel.of(categoria,
            linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(id)).withSelfRel(),
            linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("categorias"),
            linkTo(methodOn(ProductoController.class).listarProductosPorCategoria(id)).withRel("productos"),
            linkTo(methodOn(CategoriaController.class).eliminarCategoria(id)).withRel("delete")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EntityModel<Categoria>> guardarCategoria(@RequestBody Categoria categoria) {
        Categoria nuevaCategoria = categoriaService.save(categoria);

        EntityModel<Categoria> model = EntityModel.of(nuevaCategoria,
            linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(nuevaCategoria.getId())).withSelfRel(),
            linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("categorias"),
            linkTo(methodOn(ProductoController.class).listarProductosPorCategoria(nuevaCategoria.getId())).withRel("productos")
        );

        return ResponseEntity.created(linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(nuevaCategoria.getId())).toUri()).body(model);
    }

    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Categoria>> actualizarCategoria(@PathVariable Long id, @RequestBody Categoria categoria) {
        Categoria categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente == null) {
            return ResponseEntity.notFound().build();
        }

        categoria.setId(id);
        Categoria actualizada = categoriaService.save(categoria);

        EntityModel<Categoria> model = EntityModel.of(actualizada,
            linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(id)).withSelfRel(),
            linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("categorias"),
            linkTo(methodOn(ProductoController.class).listarProductosPorCategoria(id)).withRel("productos")
        );

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
