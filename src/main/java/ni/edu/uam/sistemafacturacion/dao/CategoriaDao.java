package ni.edu.uam.sistemafacturacion.dao;

import ni.edu.uam.sistemafacturacion.config.DatabaseConnection;
import ni.edu.uam.sistemafacturacion.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDao {
    public List<Categoria> listar() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "select * from categorias";
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorias;
    }

    public void guardar(Categoria categoria) {

        String sql = "INSERT INTO categorias(nombre, activa) VALUES (?, ?)";
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, categoria.getNombre());
            statement.setBoolean(2, categoria.isActiva());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try(
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
        ){
            statement.setInt(1, id);
            statement.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void actualizar(Categoria categoria) {
        String sql =  "UPDATE categorias SET nombre = ?, activa = ? WHERE id = ?";
        try(
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
        ){
            statement.setString(1, categoria.getNombre());
            statement.setBoolean(2, categoria.isActiva());
            statement.setInt(3, categoria.getId());
            statement.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

}
