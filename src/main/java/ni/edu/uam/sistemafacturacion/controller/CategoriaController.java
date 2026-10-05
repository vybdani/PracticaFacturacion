package ni.edu.uam.sistemafacturacion.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ni.edu.uam.sistemafacturacion.model.Categoria;
import ni.edu.uam.sistemafacturacion.service.CategoriaService;
import ni.edu.uam.sistemafacturacion.service.ProductoService;
import ni.edu.uam.sistemafacturacion.util.Alertas;

public class CategoriaController {
    // CAMPOS DEL FORMULARIO
    @FXML
    private TextField txtId;

    @FXML
    private TextField txtNombre;

    @FXML
    private CheckBox chkActiva;

    // TABLA
    @FXML
    private TableView<Categoria> tblCategorias;

    @FXML
    private TableColumn<Categoria, Integer> colId;

    @FXML
    private TableColumn<Categoria, String> colNombre;

    @FXML
    private TableColumn<Categoria, String> colActiva;

    // BOTONES
    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    // SERVICE
    private CategoriaService categoriaService;

    private ProductoService productoService;

    // Categoría seleccionada en la tabla (la que se actualiza o elimina)
    private Categoria categoriaSeleccionada;

    // INICIALIZACIÓN
    @FXML
    public void initialize() {

        categoriaService = new CategoriaService();

        productoService = new ProductoService();

        configurarColumnas();

        configurarSeleccionTabla();

        cargarCategorias();

        nuevo();
    }

    // CONFIGURAR COLUMNAS
    private void configurarColumnas() {

        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                cellData.getValue().getId()).asObject());

        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));

        colActiva.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isActiva() ? "Sí" : "No"));
    }

    // SELECCIONAR REGISTRO
    private void configurarSeleccionTabla() {

        tblCategorias
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, seleccionado) -> {
                            if (seleccionado != null) {
                                mostrarCategoria(seleccionado);
                            }
                        }
                );
    }

    // MOSTRAR CATEGORIA
    private void mostrarCategoria(Categoria categoria) {
        categoriaSeleccionada = categoria;

        txtId.setText(String.valueOf(categoria.getId()));
        txtNombre.setText(categoria.getNombre());
        chkActiva.setSelected(categoria.isActiva());

        // Con una categoría seleccionada se puede actualizar o eliminar, no guardar
        btnGuardar.setDisable(true);
        btnActualizar.setDisable(false);
        btnEliminar.setDisable(false);
    }

    // CARGAR CATEGORÍAS
    private void cargarCategorias() {
        try {
            tblCategorias.setItems(
                    FXCollections.observableArrayList(categoriaService.listar())
            );
        } catch (Exception e) {
            Alertas.error("Error al cargar las categorías", e.getMessage());
        }
    }

    // NUEVO
    @FXML
    private void nuevo() {
        categoriaSeleccionada = null;

        txtId.clear();
        txtNombre.clear();
        chkActiva.setSelected(true);
        tblCategorias
                .getSelectionModel()
                .clearSelection();
        txtNombre.requestFocus();

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    // GUARDAR
    @FXML
    private void guardar() {
        if (!validarFormulario(null)) {
            return;
        }

        Categoria categoria = new Categoria(null, txtNombre.getText().trim(), chkActiva.isSelected());
        try {
            categoriaService.guardar(categoria);
        } catch (Exception e) {
            Alertas.error("No se pudo guardar la categoría", e.getMessage());
            return;
        }

        Alertas.informacion("Categoría guardada", "La categoría se registró correctamente.");
        cargarCategorias();
        nuevo();
    }

    // ACTUALIZAR
    @FXML
    private void actualizar() {
        if (categoriaSeleccionada == null) {
            Alertas.advertencia("Seleccione una categoría", "Debe seleccionar una categoría de la tabla.");
            return;
        }

        if (!validarFormulario(categoriaSeleccionada)) {
            return;
        }

        if (!Alertas.confirmar("Actualizar categoría", "¿Desea actualizar esta categoría?")) {
            return;
        }

        Categoria categoria = new Categoria(
                categoriaSeleccionada.getId(), txtNombre.getText().trim(), chkActiva.isSelected());
        try {
            categoriaService.actualizar(categoria);
        } catch (Exception e) {
            Alertas.error("No se pudo actualizar la categoría", e.getMessage());
            return;
        }

        Alertas.informacion("Categoría actualizada", "La categoría se actualizó correctamente.");
        cargarCategorias();
        nuevo();
    }

    // ELIMINAR
    @FXML
    private void eliminar() {
        if (categoriaSeleccionada == null) {
            Alertas.advertencia("Seleccione una categoría", "Debe seleccionar una categoría de la tabla.");
            return;
        }

        // Avisar si hay productos que quedarán sin categoría
        String mensaje = "¿Está seguro de eliminar la categoría \"" + categoriaSeleccionada.getNombre() + "\"?";
        int cantidad = contarProductosAsignados(categoriaSeleccionada.getId());
        if (cantidad > 0) {
            mensaje += "\n\nTiene " + cantidad + (cantidad == 1 ? " producto asignado, que quedará" : " productos asignados, que quedarán")
                    + " como \"Sin categoría\". Podrá asignarles otra categoría desde Productos.";
        }

        if (!Alertas.confirmar("Eliminar categoría", mensaje)) {
            return;
        }

        try {
            categoriaService.eliminar(categoriaSeleccionada.getId());
        } catch (Exception e) {
            Alertas.error("No se pudo eliminar la categoría", e.getMessage());
            return;
        }

        Alertas.informacion("Categoría eliminada", "La categoría se eliminó correctamente.");
        cargarCategorias();
        nuevo();
    }

    // Si no se puede contar, se toma como 0 y se muestra la confirmación normal
    private int contarProductosAsignados(int categoriaId) {
        try {
            return productoService.contarPorCategoria(categoriaId);
        } catch (Exception e) {
            return 0;
        }
    }

    // VALIDAR FORMULARIO
    // "editando" es la categoría que se está actualizando (para no marcar su propio nombre como duplicado).
    private boolean validarFormulario(Categoria editando) {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            Alertas.advertencia("Campo requerido", "Debe ingresar el nombre de la categoría.");
            txtNombre.requestFocus();
            return false;
        }

        if (nombre.length() > 100) {
            Alertas.advertencia("Nombre demasiado largo", "El nombre no puede superar los 100 caracteres.");
            txtNombre.requestFocus();
            return false;
        }

        if (existeNombre(nombre, editando)) {
            Alertas.advertencia("Categoría duplicada", "Ya existe una categoría con el nombre \"" + nombre + "\".");
            txtNombre.requestFocus();
            return false;
        }
        return true;
    }

    // Verifica si otra categoría ya usa el nombre (sin distinguir mayúsculas/minúsculas)
    private boolean existeNombre(String nombre, Categoria excluir) {
        return tblCategorias.getItems().stream()
                .anyMatch(c -> c != excluir && c.getNombre().equalsIgnoreCase(nombre));
    }
}
