package com.minimarket.controller;

import com.minimarket.entity.Usuario;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuario", description = "Endpoints para gestión de usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Obtener todos los usuarios", description = "Retorna una lista de todos los usuarios con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();
        
        List<EntityModel<Usuario>> usuarioModels = usuarios.stream()
            .map(usuario -> EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("usuarios"),
                linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(usuario.getId())).withRel("carrito")
            ))
            .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(UsuarioController.class).listarUsuarios()).withSelfRel();
        CollectionModel<EntityModel<Usuario>> result = CollectionModel.of(usuarioModels, selfLink);
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario específico con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Usuario user = usuario.get();
        EntityModel<Usuario> model = EntityModel.of(user,
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(id)).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("usuarios"),
            linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(id)).withRel("carrito"),
            linkTo(methodOn(UsuarioController.class).eliminarUsuario(id)).withRel("delete")
        );
        
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Crear un nuevo usuario", description = "Crea un nuevo usuario con enlaces HATEOAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> guardarUsuario(@RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.save(usuario);
        
        EntityModel<Usuario> model = EntityModel.of(nuevoUsuario,
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(nuevoUsuario.getId())).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("usuarios"),
            linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(nuevoUsuario.getId())).withRel("carrito")
        );
        
        return ResponseEntity.created(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(nuevoUsuario.getId())).toUri()).body(model);
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        usuario.setId(id);
        Usuario actualizado = usuarioService.save(usuario);
        
        EntityModel<Usuario> model = EntityModel.of(actualizado,
            linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(id)).withSelfRel(),
            linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("usuarios"),
            linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(id)).withRel("carrito")
        );
        
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}