package javaapplication3;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

class Usuario {
    private String id;
    private String nombre;
    private String email;
    private String direccion;
    private String telefono;
    private String contrasena;

    // Constructor
    public Usuario(String id, String nombre, String email, String direccion, String telefono, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.direccion = direccion;
        this.telefono = telefono;
        this.contrasena = contrasena;
    }

    // Constructor vacío para uso con BD
    public Usuario() {}

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getContrasena() { return contrasena; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    @Override
    public String toString() {
        return "ID: " + id +
               " | Nombre: " + nombre +
               " | Email: " + email +
               " | Dirección: " + direccion +
               " | Teléfono: " + telefono +
               " | Contraseña: " + contrasena;
    }
}

public class JavaApplication3 {

    // Cambiado de private a public para permitir acceso desde ConexionBD
    public static final String URL = "jdbc:mysql://localhost:3306/prueba_salchiexpress";
    public static final String USER = "root";
    public static final String PASSWORD = "";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static void main(String[] args) {
        // Inicializar conexión
        ConexionBD.inicializar();

        try (Scanner scanner = new Scanner(System.in)) {
            int opcion;
            do {
                mostrarMenu();
                opcion = obtenerOpcion(scanner);
                switch (opcion) {
                    case 1:
                        registrarUsuario(scanner);
                        break;
                    case 2:
                        mostrarUsuarios();
                        break;
                    case 3:
                        buscarUsuario(scanner);
                        break;
                    case 4:
                        System.out.println("Saliendo del sistema...");
                        ConexionBD.cerrarConexion();
                        break;
                    default:
                        System.out.println("Opción no válida. Intente nuevamente.");
                }
            } while (opcion != 4);
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n=== SISTEMA DE REGISTRO DE USUARIOS (BD MySQL) ===");
        System.out.println("1. Registrar nuevo usuario");
        System.out.println("2. Mostrar todos los usuarios");
        System.out.println("3. Buscar usuario por ID");
        System.out.println("4. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static int obtenerOpcion(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void registrarUsuario(Scanner scanner) {
        System.out.println("\n--- REGISTRO DE NUEVO USUARIO ---");
        System.out.print("Ingrese ID: ");
        String id = scanner.nextLine();

        try {
            if (usuarioExiste(id)) {
                System.out.println("Error: El ID ya está registrado.");
                return;
            }

            System.out.print("Ingrese Nombre: ");
            String nombre = scanner.nextLine();

            System.out.print("Ingrese Email: ");
            String email = scanner.nextLine();

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                System.out.println("Error: Formato de email inválido.");
                return;
            }

            System.out.print("Ingrese Dirección: ");
            String direccion = scanner.nextLine();

            System.out.print("Ingrese Teléfono: ");
            String telefono = scanner.nextLine();

            System.out.print("Ingrese Contraseña: ");
            String contrasena = scanner.nextLine();

            if (id.isEmpty() || nombre.isEmpty() || email.isEmpty() || contrasena.isEmpty()) {
                System.out.println("Error: Campos obligatorios no pueden estar vacíos.");
                return;
            }

            insertarUsuario(id, nombre, email, direccion, telefono, contrasena);
            System.out.println("✅ Usuario registrado exitosamente en la base de datos.");
            System.out.println("Datos: ID=" + id + ", Nombre=" + nombre + ", Email=" + email);
        } catch (SQLException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
        }
    }

    private static boolean usuarioExiste(String id) throws SQLException {
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM usuarios WHERE id = ?")) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void insertarUsuario(String id, String nombre, String email, String direccion, String telefono, String contrasena) throws SQLException {
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO usuarios (id, nombre, email, direccion, telefono, contrasena) VALUES (?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, id);
            pstmt.setString(2, nombre);
            pstmt.setString(3, email);
            pstmt.setString(4, direccion);
            pstmt.setString(5, telefono);
            pstmt.setString(6, contrasena);
            pstmt.executeUpdate();
        }
    }

    private static void mostrarUsuarios() {
        System.out.println("\n--- LISTA DE USUARIOS REGISTRADOS ---");
        try {
            List<Usuario> usuariosLista = obtenerTodosUsuarios();
            if (usuariosLista.isEmpty()) {
                System.out.println("No hay usuarios registrados.");
                return;
            }
            for (int i = 0; i < usuariosLista.size(); i++) {
                System.out.println("Usuario " + (i + 1) + ": " + usuariosLista.get(i));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener usuarios: " + e.getMessage());
        }
    }

    private static List<Usuario> obtenerTodosUsuarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM usuarios")) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getString("id"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                u.setDireccion(rs.getString("direccion"));
                u.setTelefono(rs.getString("telefono"));
                u.setContrasena(rs.getString("contrasena"));
                usuarios.add(u);
            }
        }
        return usuarios;
    }

    private static void buscarUsuario(Scanner scanner) {
        System.out.println("\n--- BUSCAR USUARIO POR ID ---");
        System.out.print("Ingrese el ID del usuario a buscar: ");
        String idBuscado = scanner.nextLine();
        try {
            Usuario usuario = obtenerUsuarioPorId(idBuscado);
            if (usuario != null) {
                System.out.println("Usuario encontrado:");
                System.out.println(usuario);
            } else {
                System.out.println("Usuario con ID '" + idBuscado + "' no encontrado.");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar usuario: " + e.getMessage());
        }
    }

    private static Usuario obtenerUsuarioPorId(String id) throws SQLException {
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM usuarios WHERE id = ?")) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getString("id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setEmail(rs.getString("email"));
                    u.setDireccion(rs.getString("direccion"));
                    u.setTelefono(rs.getString("telefono"));
                    u.setContrasena(rs.getString("contrasena"));
                    return u;
                }
                return null;
            }
        }
    }
}

class ConexionBD {
    private static Connection conexion = null;

    public static void inicializar() {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Usando las constantes públicas de JavaApplication3
                conexion = DriverManager.getConnection(JavaApplication3.URL, JavaApplication3.USER, JavaApplication3.PASSWORD);
                System.out.println("Conexión a la base de datos establecida.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Driver JDBC no encontrado. Asegúrate de agregar mysql-connector-j.jar al proyecto.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error de conexión a la BD: " + e.getMessage());
            System.out.println("Verifica que MySQL esté corriendo en localhost:3306 y que la BD 'prueba_salchiexpress' exista.");
            e.printStackTrace();
        }
    }

    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                inicializar();
            }
        } catch (SQLException e) {
            System.out.println("Error al verificar conexión: " + e.getMessage());
            e.printStackTrace();
        }
        return conexion;
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión a la base de datos cerrada.");
            }
        } catch (SQLException e) {
            System.out.println("Error al cerrar conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }
}