package ni.edu.uam.sistemafacturacion.dao;

import ni.edu.uam.sistemafacturacion.config.DatabaseConnection;
import ni.edu.uam.sistemafacturacion.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriaDao {
    public List<Categoria> listar() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        // "activa" se calcula: una categoría está activa solo si tiene productos
        String sql = "SELECT c.id, c.nombre, "
                + "EXISTS (SELECT 1 FROM productos p WHERE p.categoria_id = c.id) AS activa "
                + "FROM categorias c ORDER BY c.id";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {

                Categoria categoria = new Categoria();
                categoria.setId(resultSet.getInt("id"));
                categoria.setNombre(resultSet.getString("nombre"));
                categoria.setActiva(resultSet.getBoolean("activa"));
                categorias.add(categoria);
            }
        }
        return categorias;
    }

    // Una categoría nueva no tiene productos, así que nace inactiva
    public void guardar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categorias(nombre, activa) VALUES (?, FALSE)";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, categoria.getNombre());
            statement.executeUpdate();
        }
    }

    // Solo se cambia el nombre; el estado depende de los productos (ver ProductoDao)
    public void actualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, categoria.getNombre());
            statement.setInt(2, categoria.getId());
            statement.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    // Para registrar: busca el nombre en todas las categorías
    public boolean existeNombre(String nombre) throws SQLException {
        return existeNombre(nombre, 0);
    }

    // Para actualizar: excluye la categoría que se está modificando (los id empiezan en 1)
    public boolean existeNombre(String nombre, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categorias WHERE LOWER(nombre) = LOWER(?) AND id <> ?";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, nombre);
            statement.setInt(2, idExcluir);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    // Cantidad de productos de cada categoría (id de categoría -> cantidad)
    public Map<Integer, Integer> contarProductosPorCategoria() throws SQLException {
        Map<Integer, Integer> conteo = new HashMap<>();
        String sql = "SELECT categoria_id, COUNT(*) FROM productos "
                + "WHERE categoria_id IS NOT NULL GROUP BY categoria_id";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                conteo.put(resultSet.getInt(1), resultSet.getInt(2));
            }
        }
        return conteo;
    }

    // Integridad referencial: una categoría con productos no se puede eliminar
    public boolean tieneProductos(int categoriaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos WHERE categoria_id = ?";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, categoriaId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }
}
