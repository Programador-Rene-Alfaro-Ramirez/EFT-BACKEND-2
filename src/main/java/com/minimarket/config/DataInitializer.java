package com.minimarket.config;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            // ========== ROLES ==========
            if (rolRepository.findByNombre("ROLE_ADMIN").isEmpty()) {
                rolRepository.save(new Rol(null, "ROLE_ADMIN", Set.of()));
            }
            if (rolRepository.findByNombre("ROLE_CAJERO").isEmpty()) {
                rolRepository.save(new Rol(null, "ROLE_CAJERO", Set.of()));
            }
            if (rolRepository.findByNombre("ROLE_CLIENTE").isEmpty()) {
                rolRepository.save(new Rol(null, "ROLE_CLIENTE", Set.of()));
            }

            // ========== USUARIOS ==========
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                Set<Rol> adminRoles = new HashSet<>(Set.of(rolRepository.findByNombre("ROLE_ADMIN").get()));
                admin.setRoles(adminRoles);
                usuarioRepository.save(admin);
                System.out.println("[INIT] Usuario 'admin' creado con rol ADMIN. Password: admin123");
            }

            if (usuarioRepository.findByUsername("cajero").isEmpty()) {
                Usuario cajero = new Usuario();
                cajero.setUsername("cajero");
                cajero.setPassword(passwordEncoder.encode("cajero123"));
                cajero.setRoles(new HashSet<>(Set.of(rolRepository.findByNombre("ROLE_CAJERO").get())));
                usuarioRepository.save(cajero);
                System.out.println("[INIT] Usuario 'cajero' creado con rol CAJERO. Password: cajero123");
            }

            if (usuarioRepository.findByUsername("cliente").isEmpty()) {
                Usuario cliente = new Usuario();
                cliente.setUsername("cliente");
                cliente.setPassword(passwordEncoder.encode("cliente123"));
                cliente.setRoles(new HashSet<>(Set.of(rolRepository.findByNombre("ROLE_CLIENTE").get())));
                usuarioRepository.save(cliente);
                System.out.println("[INIT] Usuario 'cliente' creado con rol CLIENTE. Password: cliente123");
            }

            // ========== CATEGORÍAS ==========
            String[] categoriasNombres = {
                "Bebidas",
                "Lácteos",
                "Frutas y Verduras",
                "Carnes",
                "Panadería",
                "Limpieza",
                "Aseo Personal",
                "Abarrotes"
            };

            for (String nombre : categoriasNombres) {
                if (categoriaRepository.findByNombre(nombre).isEmpty()) {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre);
                    categoriaRepository.save(categoria);
                    System.out.println("[INIT] Categoría '" + nombre + "' creada");
                }
            }

            // ========== PRODUCTOS ==========
            String[][] productosData = {
                // {nombre, precio, stock, categoria}
                {"Coca-Cola 1.5L", "2.50", "100", "Bebidas"},
                {"Agua Mineral 600ml", "1.00", "200", "Bebidas"},
                {"Jugo de Naranja 1L", "3.20", "80", "Bebidas"},
                {"Leche Entera 1L", "1.80", "150", "Lácteos"},
                {"Yogur Natural 500g", "2.50", "100", "Lácteos"},
                {"Queso Duro 1kg", "8.50", "50", "Lácteos"},
                {"Manzana Roja 1kg", "3.00", "120", "Frutas y Verduras"},
                {"Plátano 1kg", "1.50", "150", "Frutas y Verduras"},
                {"Tomate 1kg", "2.00", "100", "Frutas y Verduras"},
                {"Pechuga de Pollo 1kg", "6.50", "80", "Carnes"},
                {"Carne Molida 1kg", "7.00", "60", "Carnes"},
                {"Jamón de Cerdo 300g", "4.50", "70", "Carnes"},
                {"Pan de Molde", "2.80", "90", "Panadería"},
                {"Croissant", "1.50", "100", "Panadería"},
                {"Donas (6 pzas)", "5.00", "50", "Panadería"},
                {"Detergente 1kg", "4.20", "80", "Limpieza"},
                {"Lavandería 1L", "5.50", "60", "Limpieza"},
                {"Cloro 1L", "2.00", "100", "Limpieza"},
                {"Jabón de Tocador", "1.80", "120", "Aseo Personal"},
                {"Shampoo 400ml", "6.50", "70", "Aseo Personal"},
                {"Pasta de Dientes", "2.50", "150", "Aseo Personal"},
                {"Arroz 1kg", "2.20", "200", "Abarrotes"},
                {"Fideos 500g", "1.80", "180", "Abarrotes"},
                {"Aceite 1L", "3.50", "120", "Abarrotes"},
                {"Azúcar 1kg", "2.00", "150", "Abarrotes"},
                {"Café 250g", "5.00", "100", "Abarrotes"}
            };

            for (String[] producto : productosData) {
                String nombre = producto[0];
                Double precio = Double.parseDouble(producto[1]);
                Integer stock = Integer.parseInt(producto[2]);
                String categoriaNombre = producto[3];

                if (productoRepository.findByNombre(nombre).isEmpty()) {
                    Categoria categoria = categoriaRepository.findByNombre(categoriaNombre)
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + categoriaNombre));

                    Producto p = new Producto();
                    p.setNombre(nombre);
                    p.setPrecio(precio);
                    p.setStock(stock);
                    p.setCategoria(categoria);
                    productoRepository.save(p);

                    // Crear registro de inventario (ENTRADA inicial)
                    Inventario inventario = new Inventario();
                    inventario.setProducto(p);
                    inventario.setCantidad(stock);
                    inventario.setTipoMovimiento("ENTRADA");
                    inventario.setFechaMovimiento(new Date());
                    inventarioRepository.save(inventario);

                    System.out.println("[INIT] Producto '" + nombre + "' creado (stock: " + stock + ", precio: " + precio + ", categoría: " + categoriaNombre + ")");
                }
            }

            System.out.println("[INIT] Datos inicializados correctamente.");
        };
    }
}
