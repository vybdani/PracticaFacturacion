package ni.edu.uam.sistemafacturacion.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import ni.edu.uam.sistemafacturacion.model.Categoria;
import ni.edu.uam.sistemafacturacion.model.Producto;
import ni.edu.uam.sistemafacturacion.service.CategoriaService;
import ni.edu.uam.sistemafacturacion.service.ProductoService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProductoController {
    // OPCIONES DEL FILTRO DE ESTADO
    private static final String ESTADO_TODOS = "Todos";
    private static final String ESTADO_ACTIVOS = "Activos";
    private static final String ESTADO_INACTIVOS = "Inactivos";

    // Opción "Todas" del filtro de categoría (se compara por referencia)
    private static final Categoria CATEGORIA_TODAS =
            new Categoria(null, "Todas las categorías", true);

    // CAMPOS DEL FORMULARIO
    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private ComboBox<Categoria> cmbCategoria;

    @FXML
    private TextField txtPrecioVenta;

    @FXML
    private TextField txtExistencia;

    @FXML
    private CheckBox chkActivo;

    // BÚSQUEDA Y FILTROS
    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cmbFiltroEstado;

    @FXML
    private ComboBox<Categoria> cmbFiltroCategoria;

    // TABLA
    @FXML
    private TableView<Producto> tblProductos;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, String> colCategoria;

    @FXML
    private TableColumn<Producto, BigDecimal> colPrecioVenta;

    @FXML
    private TableColumn<Producto, Integer> colExistencia;

    @FXML
    private TableColumn<Producto, String> colActivo;

    @FXML
    private Label lblTotal;

    // BOTONES
    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    // COLECCIONES
    // ObservableList -> FilteredList -> TableView
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    private FilteredList<Producto> productosFiltrados;

    // SERVICE
    private CategoriaService categoriaService;

    private ProductoService productoService;

    // Producto seleccionado en la tabla (el que se actualiza o elimina)
    private Producto productoSeleccionado;

    // INICIALIZACIÓN
    @FXML
    public void initialize() {

        categoriaService = new CategoriaService();

        productoService = new ProductoService();

        configurarColumnas();

        cargarCategorias();

        cargarProductos();

        configurarFiltros();

        configurarSeleccionTabla();

        nuevo();
    }

    // CONFIGURAR COLUMNAS
    private void configurarColumnas() {

        colCodigo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCodigo()));

        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));

        colCategoria.setCellValueFactory(cellData -> {
            Categoria categoria = cellData.getValue().getCategoria();
            return new SimpleStringProperty(categoria != null ? categoria.getNombre() : "Sin categoría");
        });

        colPrecioVenta.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrecioVenta()));

        colExistencia.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                cellData.getValue().getExistencia()).asObject());

        colActivo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isActivo() ? "Sí" : "No"));
    }

    // CARGAR CATEGORÍAS
    private void cargarCategorias() {
        List<Categoria> categorias = List.of();
        try {
            categorias = categoriaService.listar();
        } catch (Exception e) {
            mostrarError("Error al cargar las categorías", e.getMessage());
        }

        if (categorias.isEmpty()) {
            mostrarAdvertencia(
                    "Sin categorías",
                    "No se encontraron categorías en la base de datos. "
                            + "Registre categorías en el módulo de Categorías para poder crear productos."
            );
        }

        StringConverter<Categoria> convertidor = new StringConverter<>() {
            @Override
            public String toString(Categoria categoria) {
                return categoria != null ? categoria.getNombre() : "";
            }

            @Override
            public Categoria fromString(String texto) {
                return null;
            }
        };

        // Formulario: solo categorías activas
        cmbCategoria.setItems(FXCollections.observableArrayList(
                categorias.stream().filter(Categoria::isActiva).toList()
        ));
        cmbCategoria.setConverter(convertidor);

        // Filtro: "Todas" + todas las categorías
        ObservableList<Categoria> opcionesFiltro = FXCollections.observableArrayList(CATEGORIA_TODAS);
        opcionesFiltro.addAll(categorias);
        cmbFiltroCategoria.setItems(opcionesFiltro);
        cmbFiltroCategoria.setConverter(convertidor);
    }

    // CARGAR PRODUCTOS DESDE LA BASE DE DATOS
    private void cargarProductos() {
        try {
            productos.setAll(productoService.listar());
        } catch (Exception e) {
            mostrarError("Error al cargar los productos", e.getMessage());
        }
    }

    // CONFIGURAR BÚSQUEDA Y FILTROS
    private void configurarFiltros() {

        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                ESTADO_TODOS, ESTADO_ACTIVOS, ESTADO_INACTIVOS
        ));
        cmbFiltroEstado.getSelectionModel().select(ESTADO_TODOS);
        cmbFiltroCategoria.getSelectionModel().select(CATEGORIA_TODAS);

        // La lista original conserva todos los productos; el filtro decide cuáles se muestran
        productosFiltrados = new FilteredList<>(productos, producto -> true);
        tblProductos.setItems(productosFiltrados);

        txtBuscar.textProperty().addListener((observable, anterior, nuevo) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((observable, anterior, nuevo) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((observable, anterior, nuevo) -> aplicarFiltros());

        aplicarFiltros();
    }

    // APLICAR FILTROS (búsqueda + estado + categoría)
    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String estado = cmbFiltroEstado.getValue();
        Categoria categoriaFiltro = cmbFiltroCategoria.getValue();

        productosFiltrados.setPredicate(producto ->
                coincideBusqueda(producto, texto)
                        && coincideEstado(producto, estado)
                        && coincideCategoria(producto, categoriaFiltro)
        );

        actualizarTotal();
    }

    private boolean coincideBusqueda(Producto producto, String texto) {
        if (texto.isEmpty()) {
            return true;
        }

        String nombreCategoria = producto.getCategoria() != null ? producto.getCategoria().getNombre() : "";

        return contiene(producto.getCodigo(), texto)
                || contiene(producto.getNombre(), texto)
                || contiene(nombreCategoria, texto);
    }

    private boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
    }

    private boolean coincideEstado(Producto producto, String estado) {
        if (ESTADO_ACTIVOS.equals(estado)) {
            return producto.isActivo();
        }
        if (ESTADO_INACTIVOS.equals(estado)) {
            return !producto.isActivo();
        }
        return true;
    }

    private boolean coincideCategoria(Producto producto, Categoria categoriaFiltro) {
        if (categoriaFiltro == null || categoriaFiltro == CATEGORIA_TODAS) {
            return true;
        }
        return producto.getCategoria() != null
                && Objects.equals(producto.getCategoria().getId(), categoriaFiltro.getId());
    }

    private void actualizarTotal() {
        lblTotal.setText("Mostrando " + productosFiltrados.size() + " de " + productos.size() + " productos");
    }

    // LIMPIAR FILTROS
    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroEstado.getSelectionModel().select(ESTADO_TODOS);
        cmbFiltroCategoria.getSelectionModel().select(CATEGORIA_TODAS);
    }

    // SELECCIONAR REGISTRO
    private void configurarSeleccionTabla() {

        tblProductos
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, seleccionado) -> {
                            if (seleccionado != null) {
                                mostrarProducto(seleccionado);
                            }
                        }
                );
    }

    // MOSTRAR PRODUCTO EN EL FORMULARIO
    private void mostrarProducto(Producto producto) {
        productoSeleccionado = producto;

        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());
        cmbCategoria.setValue(buscarCategoriaEnCombo(producto.getCategoria()));
        txtPrecioVenta.setText(producto.getPrecioVenta().toPlainString());
        txtExistencia.setText(String.valueOf(producto.getExistencia()));
        chkActivo.setSelected(producto.isActivo());

        btnGuardar.setDisable(true);
        btnActualizar.setDisable(false);
        btnEliminar.setDisable(false);
    }

    // Busca la categoría del producto dentro de las opciones del ComboBox
    private Categoria buscarCategoriaEnCombo(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return cmbCategoria.getItems().stream()
                .filter(c -> Objects.equals(c.getId(), categoria.getId()))
                .findFirst()
                .orElse(categoria);
    }

    // NUEVO
    @FXML
    private void nuevo() {
        productoSeleccionado = null;

        txtCodigo.clear();
        txtNombre.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbCategoria.setValue(null);
        txtPrecioVenta.clear();
        txtExistencia.clear();
        chkActivo.setSelected(true);

        tblProductos
                .getSelectionModel()
                .clearSelection();
        txtCodigo.requestFocus();

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    // GUARDAR (CREATE)
    @FXML
    private void guardar() {
        Producto datos = leerFormulario(null);
        if (datos == null) {
            return;
        }

        try {
            // Se guarda en la base de datos (asigna el id) y luego en la colección
            productoService.guardar(datos);
        } catch (Exception e) {
            mostrarError("No se pudo guardar el producto", e.getMessage());
            return;
        }
        productos.add(datos);

        aplicarFiltros();
        mostrarInformacion("Producto guardado", "El producto se registró correctamente.");
        nuevo();
    }

    // ACTUALIZAR (UPDATE)
    @FXML
    private void actualizar() {
        if (productoSeleccionado == null) {
            mostrarAdvertencia("Seleccione un producto", "Debe seleccionar un producto de la tabla.");
            return;
        }

        Producto datos = leerFormulario(productoSeleccionado);
        if (datos == null) {
            return;
        }

        Optional<ButtonType> resultado =
                mostrarConfirmacion("Actualizar producto", "¿Desea actualizar este producto?");

        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        datos.setId(productoSeleccionado.getId());
        try {
            productoService.actualizar(datos);
        } catch (Exception e) {
            mostrarError("No se pudo actualizar el producto", e.getMessage());
            return;
        }

        // Se modifica el objeto seleccionado, no se crea un registro nuevo
        productoSeleccionado.setCodigo(datos.getCodigo());
        productoSeleccionado.setNombre(datos.getNombre());
        productoSeleccionado.setCategoria(datos.getCategoria());
        productoSeleccionado.setPrecioVenta(datos.getPrecioVenta());
        productoSeleccionado.setExistencia(datos.getExistencia());
        productoSeleccionado.setActivo(datos.isActivo());

        // Reaplicar el filtro por si el producto modificado ya no cumple la búsqueda
        aplicarFiltros();
        tblProductos.refresh();

        mostrarInformacion("Producto actualizado", "El producto se actualizó correctamente.");
        nuevo();
    }

    // ELIMINAR (DELETE)
    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            mostrarAdvertencia("Seleccione un producto", "Debe seleccionar un producto de la tabla.");
            return;
        }

        Optional<ButtonType> resultado = mostrarConfirmacion(
                "Eliminar producto",
                "¿Está seguro de eliminar el producto \"" + productoSeleccionado.getNombre() + "\"?"
        );
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        try {
            productoService.eliminar(productoSeleccionado.getId());
        } catch (Exception e) {
            mostrarError("No se pudo eliminar el producto", e.getMessage());
            return;
        }
        productos.remove(productoSeleccionado);

        actualizarTotal();
        mostrarInformacion("Producto eliminado", "El producto se eliminó correctamente.");
        nuevo();
    }

    // LEER Y VALIDAR FORMULARIO
    // Devuelve un Producto con los datos del formulario, o null si hay errores.
    // "editando" es el producto que se está actualizando (para no marcar su propio código como duplicado).
    private Producto leerFormulario(Producto editando) {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        Categoria categoria = cmbCategoria.getValue();
        String textoPrecio = txtPrecioVenta.getText().trim();
        String textoExistencia = txtExistencia.getText().trim();

        if (codigo.isEmpty()) {
            mostrarAdvertencia("Campo requerido", "Debe ingresar el código del producto.");
            txtCodigo.requestFocus();
            return null;
        }

        if (existeCodigo(codigo, editando)) {
            mostrarAdvertencia("Código duplicado", "Ya existe un producto con el código \"" + codigo + "\".");
            txtCodigo.requestFocus();
            return null;
        }

        if (nombre.isEmpty()) {
            mostrarAdvertencia("Campo requerido", "Debe ingresar el nombre del producto.");
            txtNombre.requestFocus();
            return null;
        }

        if (categoria == null) {
            mostrarAdvertencia("Campo requerido", "Debe seleccionar una categoría.");
            cmbCategoria.requestFocus();
            return null;
        }

        BigDecimal precioVenta;
        try {
            precioVenta = new BigDecimal(textoPrecio).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Precio inválido", "El precio de venta debe ser un valor numérico.");
            txtPrecioVenta.requestFocus();
            return null;
        }

        if (precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarAdvertencia("Precio inválido", "El precio de venta debe ser mayor que cero.");
            txtPrecioVenta.requestFocus();
            return null;
        }

        int existencia;
        try {
            existencia = Integer.parseInt(textoExistencia);
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Existencia inválida", "La existencia debe ser un número entero.");
            txtExistencia.requestFocus();
            return null;
        }

        if (existencia < 0) {
            mostrarAdvertencia("Existencia inválida", "La existencia no puede ser negativa.");
            txtExistencia.requestFocus();
            return null;
        }

        return new Producto(null, codigo, nombre, categoria, precioVenta, existencia, chkActivo.isSelected());
    }

    // Verifica si otro producto ya usa el código (sin distinguir mayúsculas/minúsculas)
    private boolean existeCodigo(String codigo, Producto excluir) {
        return productos.stream()
                .anyMatch(p -> p != excluir && p.getCodigo().equalsIgnoreCase(codigo));
    }

    // ALERTA DE INFORMACIÓN
    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ALERTA DE ADVERTENCIA
    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ALERTA DE ERROR
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje != null ? mensaje : "Se produjo un error inesperado.");
        alert.showAndWait();
    }

    // CONFIRMACIÓN
    private Optional<ButtonType> mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait();
    }
}
