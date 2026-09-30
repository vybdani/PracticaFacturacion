package ni.edu.uam.sistemafacturacion.dao;

import ni.edu.uam.sistemafacturacion.config.DatabaseConnection;
import ni.edu.uam.sistemafacturacion.model.Categoria;
import ni.edu.uam.sistemafacturacion.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDao {
    public List<Producto> listar() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.id, p.codigo, p.nombre, p.precio_venta, p.existencia, p.activo, "
                + "c.id AS categoria_id, c.nombre AS categoria_nombre, c.activa AS categoria_activa "
                + "FROM productos p "
                + "LEFT JOIN categorias c ON c.id = p.categoria_id "
                + "ORDER BY p.id";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {

                // Si su categoría fue eliminada, el producto queda sin categoría (null)
                Categoria categoria = null;
                int categoriaId = resultSet.getInt("categoria_id");
                if (!resultSet.wasNull()) {
                    categoria = new Categoria();
                    categoria.setId(categoriaId);
                    categoria.setNombre(resultSet.getString("categoria_nombre"));
                    categoria.setActiva(resultSet.getBoolean("categoria_activa"));
                }

                Producto producto = new Producto();
                producto.setId(resultSet.getInt("id"));
                producto.setCodigo(resultSet.getString("codigo"));
                producto.setNombre(resultSet.getString("nombre"));
                producto.setCategoria(categoria);
                producto.setPrecioVenta(resultSet.getBigDecimal("precio_venta"));
                producto.setExistencia(resultSet.getInt("existencia"));
                producto.setActivo(resultSet.getBoolean("activo"));
                productos.add(producto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos: " + e.getMessage(), e);
        }
        return productos;
    }

    // Guarda el producto y le asigna el id generado por la base de datos
    public void guardar(Producto producto) {
        String sql = "INSERT INTO productos(codigo, nombre, categoria_id, precio_venta, existencia, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, producto.getCodigo());
            statement.setString(2, producto.getNombre());
            statement.setInt(3, producto.getCategoria().getId());
            statement.setBigDecimal(4, producto.getPrecioVenta());
            statement.setInt(5, producto.getExistencia());
            statement.setBoolean(6, producto.isActivo());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    producto.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el producto: " + e.getMessage(), e);
        }
    }

    public void actualizar(Producto producto) {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, categoria_id = ?, "
                + "precio_venta = ?, existencia = ?, activo = ? WHERE id = ?";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, producto.getCodigo());
            statement.setString(2, producto.getNombre());
            statement.setInt(3, producto.getCategoria().getId());
            statement.setBigDecimal(4, producto.getPrecioVenta());
            statement.setInt(5, producto.getExistencia());
            statement.setBoolean(6, producto.isActivo());
            statement.setInt(7, producto.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el producto: " + e.getMessage(), e);
        }
    }

    public int contarPorCategoria(int categoriaId) {
        String sql = "SELECT COUNT(*) FROM productos WHERE categoria_id = ?";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, categoriaId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar los productos de la categoría: " + e.getMessage(), e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el producto: " + e.getMessage(), e);
        }
    }
}
