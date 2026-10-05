package ni.edu.uam.sistemafacturacion.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ni.edu.uam.sistemafacturacion.model.Categoria;
import ni.edu.uam.sistemafacturacion.service.CategoriaService;
import ni.edu.uam.sistemafacturacion.util.Alertas;
import ni.edu.uam.sistemafacturacion.util.Sincronizacion;

import java.sql.SQLException;
import java.util.Map;

public class CategoriaController {
    // CAMPOS DEL FORMULARIO
    @FXML
    private TextField txtId;

    @FXML
    private TextField txtNombre;

    // Estado de solo lectura: la categoría está activa cuando tiene productos
    @FXML
    private Label lblEstado;

    // TABLA
    @FXML
    private TableView<Categoria> tblCategorias;

    @FXML
    private TableColumn<Categoria, Integer> colId;

    @FXML
    private TableColumn<Categoria, String> colNombre;

    @FXML
    private TableColumn<Categoria, String> colActiva;

    @FXML
    private TableColumn<Categoria, Integer> colProductos;

    // Productos por categoría (id de categoría -> cantidad), para la columna "Productos"
    private Map<Integer, Integer> productosPorCategoria = Map.of();

    // BOTONES
    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    // SERVICE
    private CategoriaService categoriaService;

    // INICIALIZACIÓN
    @FXML
    public void initialize() {

        categoriaService = new CategoriaService();

        configurarColumnas();

        configurarSeleccionTabla();

        cargarCategorias();

        // Si en la ventana de Productos se agrega, cambia o elimina un producto, se actualiza la columna "Productos"
        Sincronizacion.escucharProductos(tblCategorias, this::actualizarConteoYEstado);

        nuevo();
    }

    // CONFIGURAR COLUMNAS
    private void configurarColumnas() {

        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                cellData.getValue().getId()).asObject());

        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));

        colActiva.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isActiva() ? "Sí" : "No"));

        colProductos.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                productosPorCategoria.getOrDefault(cellData.getValue().getId(), 0)).asObject());
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
        txtId.setText(String.valueOf(categoria.getId()));
        txtNombre.setText(categoria.getNombre());
        lblEstado.setText(describirEstado(categoria));

        // Con una categoría seleccionada se puede actualizar o eliminar, no guardar
        btnGuardar.setDisable(true);
        btnActualizar.setDisable(false);
        btnEliminar.setDisable(false);
    }

    // CARGAR CATEGORÍAS
    private void cargarCategorias() {
        try {
            productosPorCategoria = categoriaService.contarProductosPorCategoria();
            tblCategorias.setItems(
                    FXCollections.observableArrayList(categoriaService.listar())
            );
        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible cargar las categorías.", e);
        }
    }

    // Un cambio en Productos solo afecta el conteo y el estado (activa = tiene productos):
    // se actualizan sin tocar la selección ni el nombre que se está escribiendo
    private void actualizarConteoYEstado() {
        try {
            productosPorCategoria = categoriaService.contarProductosPorCategoria();

            for (Categoria categoria : tblCategorias.getItems()) {
                categoria.setActiva(productosPorCategoria.getOrDefault(categoria.getId(), 0) > 0);
            }
            tblCategorias.refresh();

            Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                lblEstado.setText(describirEstado(seleccionada));
            }
        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible actualizar la cantidad de productos.", e);
        }
    }

    // NUEVO
    @FXML
    private void nuevo() {
        txtId.clear();
        txtNombre.clear();
        lblEstado.setText("Inactiva: se activará cuando tenga productos.");
        tblCategorias
                .getSelectionModel()
                .clearSelection();
        txtNombre.requestFocus();

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    // GUARDAR (INSERT)
    @FXML
    private void guardar() {
        try {
            Categoria categoria = obtenerCategoriaFormulario();

            if (categoriaService.existeNombre(categoria.getNombre())) {
                Alertas.advertencia("Categoría duplicada", "Ya existe una categoría con ese nombre.");
                txtNombre.requestFocus();
                return;
            }

            categoriaService.guardar(categoria);

            Alertas.informacion("Categoría registrada", "La categoría se guardó correctamente.");
            cargarCategorias();
            Sincronizacion.categoriasCambiaron();
            nuevo();

        } catch (IllegalArgumentException e) {
            Alertas.advertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible registrar la categoría.", e);
        }
    }

    // ACTUALIZAR (UPDATE)
    @FXML
    private void actualizar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            Alertas.advertencia("Seleccione una categoría", "Debe seleccionar la categoría que desea actualizar.");
            return;
        }

        try {
            // Se validan otra vez los datos y se conserva el id: el UPDATE nunca crea un registro nuevo
            Categoria categoria = obtenerCategoriaFormulario();
            categoria.setId(seleccionada.getId());

            if (categoriaService.existeNombre(categoria.getNombre(), categoria.getId())) {
                Alertas.advertencia("Categoría duplicada", "Ya existe otra categoría con ese nombre.");
                txtNombre.requestFocus();
                return;
            }

            if (!Alertas.confirmar("Actualizar categoría", "¿Desea actualizar esta categoría?")) {
                return;
            }

            categoriaService.actualizar(categoria);

            Alertas.informacion("Categoría actualizada", "La categoría se actualizó correctamente.");
            cargarCategorias();
            Sincronizacion.categoriasCambiaron();
            nuevo();

        } catch (IllegalArgumentException e) {
            Alertas.advertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible actualizar la categoría.", e);
        }
    }

    // ELIMINAR (DELETE)
    @FXML
    private void eliminar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            Alertas.advertencia("Seleccione una categoría", "Debe seleccionar la categoría que desea eliminar.");
            return;
        }

        try {
            // Integridad referencial: no se elimina una categoría que usan los productos
            if (categoriaService.tieneProductos(seleccionada.getId())) {
                Alertas.advertencia(
                        "Categoría en uso",
                        "No puede eliminar la categoría porque tiene productos asociados."
                );
                return;
            }

            if (!Alertas.confirmar(
                    "Eliminar categoría",
                    "¿Está seguro de eliminar la categoría \"" + seleccionada.getNombre() + "\"?"
            )) {
                return;
            }

            categoriaService.eliminar(seleccionada.getId());

            Alertas.informacion("Categoría eliminada", "La categoría se eliminó correctamente.");
            cargarCategorias();
            Sincronizacion.categoriasCambiaron();
            nuevo();

        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible eliminar la categoría.", e);
        }
    }

    // LEER Y VALIDAR FORMULARIO
    // Devuelve la categoría con los datos del formulario o lanza IllegalArgumentException si hay errores.
    private Categoria obtenerCategoriaFormulario() {
        // trim(): un nombre con solo espacios queda vacío
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            throw invalido(txtNombre, "El nombre de la categoría es obligatorio.");
        }

        if (nombre.length() > 100) {
            throw invalido(txtNombre, "El nombre no puede superar los 100 caracteres.");
        }

        // "activa" no se captura en el formulario: lo decide la base según los productos
        return new Categoria(null, nombre, false);
    }

    // Texto del estado según la cantidad de productos de la categoría
    private String describirEstado(Categoria categoria) {
        int cantidad = productosPorCategoria.getOrDefault(categoria.getId(), 0);
        if (cantidad == 0) {
            return "Inactiva: no tiene productos.";
        }
        return "Activa: tiene " + cantidad + (cantidad == 1 ? " producto." : " productos.");
    }

    // Lleva el cursor al campo con error y crea la excepción con el mensaje
    private IllegalArgumentException invalido(Control campo, String mensaje) {
        campo.requestFocus();
        return new IllegalArgumentException(mensaje);
    }
}
