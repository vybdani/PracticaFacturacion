package ni.edu.uam.sistemafacturacion.service;

import ni.edu.uam.sistemafacturacion.dao.ProductoDao;
import ni.edu.uam.sistemafacturacion.model.Producto;

import java.sql.SQLException;
import java.util.List;

public class ProductoService {
    private final ProductoDao productoDao;

    public ProductoService() {
        productoDao = new ProductoDao();
    }

    public List<Producto> listar() throws SQLException {
        return productoDao.listar();
    }

    public void guardar(Producto producto) throws SQLException {
        productoDao.guardar(producto);
    }

    public void actualizar(Producto producto) throws SQLException {
        productoDao.actualizar(producto);
    }

    public void eliminar(int id) throws SQLException {
        productoDao.eliminar(id);
    }

    public boolean existeCodigo(String codigo) throws SQLException {
        return productoDao.existeCodigo(codigo);
    }

    public boolean existeCodigo(String codigo, int idExcluir) throws SQLException {
        return productoDao.existeCodigo(codigo, idExcluir);
    }
}
