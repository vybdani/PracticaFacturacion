package ni.edu.uam.sistemafacturacion.service;

import ni.edu.uam.sistemafacturacion.dao.ProductoDao;
import ni.edu.uam.sistemafacturacion.model.Producto;

import java.util.List;

public class ProductoService {
    private final ProductoDao productoDao;

    public ProductoService() {
        productoDao = new ProductoDao();
    }

    public List<Producto> listar() {
        return productoDao.listar();
    }

    public void guardar(Producto producto) {
        productoDao.guardar(producto);
    }

    public void actualizar(Producto producto) {
        productoDao.actualizar(producto);
    }

    public int contarPorCategoria(int categoriaId) {
        return productoDao.contarPorCategoria(categoriaId);
    }

    public void eliminar(int id) {
        productoDao.eliminar(id);
    }
}
