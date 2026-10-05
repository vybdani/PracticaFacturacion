package ni.edu.uam.sistemafacturacion.service;

import ni.edu.uam.sistemafacturacion.dao.CategoriaDao;
import ni.edu.uam.sistemafacturacion.model.Categoria;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CategoriaService {
    private final CategoriaDao categoriaDao;

    public CategoriaService() {
        categoriaDao = new CategoriaDao();
    }

    public List<Categoria> listar() throws SQLException {
        return categoriaDao.listar();
    }

    public void guardar(Categoria categoria) throws SQLException {
        categoriaDao.guardar(categoria);
    }

    public void actualizar(Categoria categoria) throws SQLException {
        categoriaDao.actualizar(categoria);
    }

    public void eliminar(int id) throws SQLException {
        categoriaDao.eliminar(id);
    }

    public boolean existeNombre(String nombre) throws SQLException {
        return categoriaDao.existeNombre(nombre);
    }

    public boolean existeNombre(String nombre, int idExcluir) throws SQLException {
        return categoriaDao.existeNombre(nombre, idExcluir);
    }

    public Map<Integer, Integer> contarProductosPorCategoria() throws SQLException {
        return categoriaDao.contarProductosPorCategoria();
    }

    public boolean tieneProductos(int categoriaId) throws SQLException {
        return categoriaDao.tieneProductos(categoriaId);
    }
}
