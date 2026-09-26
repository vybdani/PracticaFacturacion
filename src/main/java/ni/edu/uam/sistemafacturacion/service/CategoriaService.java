package ni.edu.uam.sistemafacturacion.service;

import ni.edu.uam.sistemafacturacion.dao.CategoriaDao;
import ni.edu.uam.sistemafacturacion.model.Categoria;

import java.util.List;

public class CategoriaService {
    private final CategoriaDao categoriaDao;

    public CategoriaService() {
        categoriaDao = new CategoriaDao();
    }

    public List<Categoria> listar() {
        return categoriaDao.listar();
    }

    public void guardar(Categoria categoria) {
        categoriaDao.guardar(categoria);
    }

    public void eliminar(int id) {
        categoriaDao.eliminar(id);
    }

    public void actualizar(Categoria categoria) {
        categoriaDao.actualizar(categoria);
    }
}

