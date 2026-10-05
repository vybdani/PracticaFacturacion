package ni.edu.uam.sistemafacturacion.dao;

import ni.edu.uam.sistemafacturacion.config.DatabaseConnection;
import ni.edu.uam.sistemafacturacion.model.Categoria;
import ni.edu.uam.sistemafacturacion.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDao {
    // Marca como activas las categorías con productos y como inactivas las vacías (solo las que cambian)
    private static final String SQL_ESTADO_CATEGORIAS =
            "UPDATE categorias c SET activa = EXISTS (SELECT 1 FROM productos p WHERE p.categoria_id = c.id) "
                    + "WHERE c.activa <> EXISTS (SELECT 1 FROM productos p WHERE p.categoria_id = c.id)";

    public List<Producto> listar() throws SQLException {
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

                // Productos registrados antes de la llave foránea RESTRICT pueden no tener categoría
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
        }
        return productos;
    }

    public void guardar(Producto producto) throws SQLException {
        String sql = "INSERT INTO productos(codigo, nombre, categoria_id, precio_venta, existencia, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        ejecutarYActualizarCategorias(sql, statement -> {
            statement.setString(1, producto.getCodigo());
            statement.setString(2, producto.getNombre());
            statement.setInt(3, producto.getCategoria().getId());
            statement.setBigDecimal(4, producto.getPrecioVenta());
            statement.setInt(5, producto.getExistencia());
            statement.setBoolean(6, producto.isActivo());
        });
    }

    public void actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, categoria_id = ?, "
                + "precio_venta = ?, existencia = ?, activo = ? WHERE id = ?";
        ejecutarYActualizarCategorias(sql, statement -> {
            statement.setString(1, producto.getCodigo());
            statement.setString(2, producto.getNombre());
            statement.setInt(3, producto.getCategoria().getId());
            statement.setBigDecimal(4, producto.getPrecioVenta());
            statement.setInt(5, producto.getExistencia());
            statement.setBoolean(6, producto.isActivo());
            statement.setInt(7, producto.getId());
        });
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id = ?";
        ejecutarYActualizarCategorias(sql, statement -> statement.setInt(1, id));
    }

    /*
     * Una categoría está activa solo si tiene productos.
     * Ejecuta el INSERT, UPDATE o DELETE del producto y, en la misma transacción,
     * vuelve a calcular el estado de las categorías: si algo falla no se guarda ninguno de los dos.
     */
    private void ejecutarYActualizarCategorias(String sql, AsignarParametros parametros) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (
                    PreparedStatement statement = connection.prepareStatement(sql);
                    PreparedStatement estado = connection.prepareStatement(SQL_ESTADO_CATEGORIAS)
            ) {
                parametros.asignar(statement);
                statement.executeUpdate();
                estado.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @FunctionalInterface
    private interface AsignarParametros {
        void asignar(PreparedStatement statement) throws SQLException;
    }

    // Para registrar: busca el código en todos los productos
    public boolean existeCodigo(String codigo) throws SQLException {
        return existeCodigo(codigo, 0);
    }

    // Para actualizar: excluye el producto que se está modificando (los id empiezan en 1)
    public boolean existeCodigo(String codigo, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos WHERE LOWER(codigo) = LOWER(?) AND id <> ?";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, codigo);
            statement.setInt(2, idExcluir);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }
}
