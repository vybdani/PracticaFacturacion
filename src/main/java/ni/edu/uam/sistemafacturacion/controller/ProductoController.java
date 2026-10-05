package ni.edu.uam.sistemafacturacion.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import ni.edu.uam.sistemafacturacion.model.Categoria;
import ni.edu.uam.sistemafacturacion.model.Producto;
import ni.edu.uam.sistemafacturacion.service.CategoriaService;
import ni.edu.uam.sistemafacturacion.service.ProductoService;
import ni.edu.uam.sistemafacturacion.util.Alertas;
import ni.edu.uam.sistemafacturacion.util.Sincronizacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ProductoController {
    // OPCIONES DEL FILTRO DE ESTADO
    private static final String ESTADO_TODOS = "Todos";
    private static final String ESTADO_ACTIVOS = "Activos";
    private static final String ESTADO_INACTIVOS = "Inactivos";

    // Opción "Todas" del filtro de categoría (se compara por referencia)
    private static final Categoria CATEGORIA_TODAS =
            new Categoria(null, "Todas las categorías", true);

    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("9999999999.99");

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
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    // COLECCIONES
    // ObservableList -> FilteredList -> TableView
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    private final FilteredList<Producto> productosFiltrados = new FilteredList<>(productos, producto -> true);

    // SERVICE
    private CategoriaService categoriaService;

    private ProductoService productoService;

    // INICIALIZACIÓN
    @FXML
    public void initialize() {

        categoriaService = new CategoriaService();

        productoService = new ProductoService();

        configurarColumnas();

        configurarCombosCategoria();

        cargarCategorias(true);

        configurarFiltros();

        cargarProductos();

        configurarSeleccionTabla();

        // Si en la ventana de Categorías se agrega, cambia o elimina una categoría, se recarga esta ventana
        Sincronizacion.escucharCategorias(tblProductos, this::recargarPorCambioCategorias);

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
    // avisarSiVacia: el aviso "Sin categorías" solo se muestra al abrir la ventana
    private void cargarCategorias(boolean avisarSiVacia) {
        List<Categoria> categorias = List.of();
        try {
            categorias = categoriaService.listar();

            if (avisarSiVacia && categorias.isEmpty()) {
                Alertas.advertencia(
                        "Sin categorías",
                        "No se encontraron categorías en la base de datos. "
                                + "Registre categorías en el módulo de Categorías para poder crear productos."
                );
            }
        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible cargar las categorías.", e);
        }

        // Formulario: todas las categorías (una categoría vacía está inactiva
        // y se activa justamente al recibir su primer producto)
        cmbCategoria.setItems(FXCollections.observableArrayList(categorias));

        // Filtro: "Todas" + todas las categorías
        ObservableList<Categoria> opcionesFiltro = FXCollections.observableArrayList(CATEGORIA_TODAS);
        opcionesFiltro.addAll(categorias);
        cmbFiltroCategoria.setItems(opcionesFiltro);
    }

    // CONFIGURAR COMBOS DE CATEGORÍA (una sola vez)
    private void configurarCombosCategoria() {
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

        cmbCategoria.setConverter(convertidor);
        cmbFiltroCategoria.setConverter(convertidor);

        // JavaFX deja de mostrar el texto guía ("Seleccione una categoría") después de limpiar el combo;
        // esta celda lo vuelve a mostrar cuando no hay categoría elegida
        cmbCategoria.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Categoria categoria, boolean empty) {
                super.updateItem(categoria, empty);
                boolean vacio = empty || categoria == null;
                setText(vacio ? cmbCategoria.getPromptText() : categoria.getNombre());
                setStyle(vacio ? "-fx-text-fill: #A8927D;" : "");
            }
        });
    }

    // RECARGAR CUANDO CAMBIAN LAS CATEGORÍAS
    // Actualiza combos y tabla (nombres de categoría) sin perder lo que el usuario tenía elegido o escrito
    private void recargarPorCambioCategorias() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        Categoria categoriaFormulario = cmbCategoria.getValue();
        Categoria categoriaFiltro = cmbFiltroCategoria.getValue();

        String codigo = txtCodigo.getText();
        String nombre = txtNombre.getText();
        String precio = txtPrecioVenta.getText();
        String existencia = txtExistencia.getText();
        boolean activo = chkActivo.isSelected();

        cargarCategorias(false);

        // Filtro: la misma categoría (con sus datos nuevos) o "Todas" si ya no existe
        cmbFiltroCategoria.setValue(
                categoriaFiltro == null || categoriaFiltro == CATEGORIA_TODAS
                        ? CATEGORIA_TODAS
                        : cmbFiltroCategoria.getItems().stream()
                                .filter(c -> Objects.equals(c.getId(), categoriaFiltro.getId()))
                                .findFirst()
                                .orElse(CATEGORIA_TODAS)
        );

        cargarProductos();

        if (seleccionado != null) {
            // Se vuelve a seleccionar el producto para poder seguir actualizándolo o eliminándolo
            productosFiltrados.stream()
                    .filter(p -> p.getId().equals(seleccionado.getId()))
                    .findFirst()
                    .ifPresent(p -> tblProductos.getSelectionModel().select(p));
        }

        // Se restaura lo que el usuario tenía escrito en el formulario
        txtCodigo.setText(codigo);
        txtNombre.setText(nombre);
        txtPrecioVenta.setText(precio);
        txtExistencia.setText(existencia);
        chkActivo.setSelected(activo);

        // La categoría elegida, con su nombre nuevo (o vacía si se eliminó)
        cmbCategoria.setValue(buscarCategoriaEnCombo(categoriaFormulario));
    }

    // CARGAR PRODUCTOS DESDE LA BASE DE DATOS
    // Se llama al abrir la ventana y después de cada INSERT, UPDATE o DELETE
    private void cargarProductos() {
        try {
            productos.setAll(productoService.listar());
        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible cargar los productos.", e);
        }
        actualizarTotal();
    }

    // CONFIGURAR BÚSQUEDA Y FILTROS
    private void configurarFiltros() {

        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                ESTADO_TODOS, ESTADO_ACTIVOS, ESTADO_INACTIVOS
        ));
        cmbFiltroEstado.getSelectionModel().select(ESTADO_TODOS);
        cmbFiltroCategoria.getSelectionModel().select(CATEGORIA_TODAS);

        // La lista original conserva todos los productos; el filtro decide cuáles se muestran
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

    // Busca la categoría (por id) dentro de las opciones del ComboBox; null si ya no existe
    private Categoria buscarCategoriaEnCombo(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return cmbCategoria.getItems().stream()
                .filter(c -> Objects.equals(c.getId(), categoria.getId()))
                .findFirst()
                .orElse(null);
    }

    // NUEVO
    @FXML
    private void nuevo() {
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

    // GUARDAR (INSERT)
    @FXML
    private void guardar() {
        try {
            Producto producto = obtenerProductoFormulario();

            if (productoService.existeCodigo(producto.getCodigo())) {
                Alertas.advertencia("Código duplicado", "Ya existe un producto con ese código.");
                txtCodigo.requestFocus();
                return;
            }

            productoService.guardar(producto);

            Alertas.informacion("Producto registrado", "El producto se guardó correctamente.");
            cargarProductos();
            Sincronizacion.productosCambiaron();
            nuevo();

        } catch (IllegalArgumentException e) {
            Alertas.advertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible registrar el producto.", e);
        }
    }

    // ACTUALIZAR (UPDATE)
    @FXML
    private void actualizar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Alertas.advertencia("Seleccione un producto", "Debe seleccionar el producto que desea actualizar.");
            return;
        }

        try {
            // Se validan otra vez los datos y se conserva el id: el UPDATE nunca crea un registro nuevo
            Producto producto = obtenerProductoFormulario();
            producto.setId(seleccionado.getId());

            if (productoService.existeCodigo(producto.getCodigo(), producto.getId())) {
                Alertas.advertencia("Código duplicado", "Ya existe otro producto con ese código.");
                txtCodigo.requestFocus();
                return;
            }

            if (!Alertas.confirmar("Actualizar producto", "¿Desea actualizar este producto?")) {
                return;
            }

            productoService.actualizar(producto);

            Alertas.informacion("Producto actualizado", "El producto se actualizó correctamente.");
            cargarProductos();
            Sincronizacion.productosCambiaron();
            nuevo();

        } catch (IllegalArgumentException e) {
            Alertas.advertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible actualizar el producto.", e);
        }
    }

    // ELIMINAR (DELETE)
    @FXML
    private void eliminar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Alertas.advertencia("Seleccione un producto", "Debe seleccionar el producto que desea eliminar.");
            return;
        }

        if (!Alertas.confirmar(
                "Eliminar producto",
                "¿Está seguro de eliminar el producto \"" + seleccionado.getNombre() + "\"?"
        )) {
            return;
        }

        try {
            productoService.eliminar(seleccionado.getId());

            Alertas.informacion("Producto eliminado", "El producto se eliminó correctamente.");
            cargarProductos();
            Sincronizacion.productosCambiaron();
            nuevo();

        } catch (SQLException e) {
            Alertas.errorBaseDatos("No fue posible eliminar el producto.", e);
        }
    }

    // LEER Y VALIDAR FORMULARIO
    // Devuelve el producto con los datos del formulario o lanza IllegalArgumentException si hay errores.
    private Producto obtenerProductoFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (codigo.isEmpty()) {
            throw invalido(txtCodigo, "El código es obligatorio.");
        }

        if (codigo.length() > 30) {
            throw invalido(txtCodigo, "El código no puede superar los 30 caracteres.");
        }

        if (nombre.isEmpty()) {
            throw invalido(txtNombre, "El nombre es obligatorio.");
        }

        if (nombre.length() > 150) {
            throw invalido(txtNombre, "El nombre no puede superar los 150 caracteres.");
        }

        Categoria categoria = cmbCategoria.getSelectionModel().getSelectedItem();

        if (categoria == null) {
            throw invalido(cmbCategoria, "Debe seleccionar una categoría.");
        }

        BigDecimal precioVenta;
        try {
            // Se acepta la coma como separador decimal (10,50 = 10.50)
            String textoPrecio = txtPrecioVenta.getText().trim().replace(',', '.');
            precioVenta = new BigDecimal(textoPrecio).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw invalido(txtPrecioVenta, "El precio debe ser un valor numérico.");
        }

        if (precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw invalido(txtPrecioVenta, "El precio de venta debe ser mayor que cero.");
        }

        // La columna precio_venta es NUMERIC(12,2): hasta 10 dígitos enteros
        if (precioVenta.compareTo(PRECIO_MAXIMO) > 0) {
            throw invalido(txtPrecioVenta, "El precio de venta no puede superar " + PRECIO_MAXIMO.toPlainString() + ".");
        }

        int existencia;
        try {
            existencia = Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            throw invalido(txtExistencia, "La existencia debe ser un número entero.");
        }

        if (existencia < 0) {
            throw invalido(txtExistencia, "La existencia no puede ser negativa.");
        }

        return new Producto(null, codigo, nombre, categoria, precioVenta, existencia, chkActivo.isSelected());
    }

    // Lleva el cursor al campo con error y crea la excepción con el mensaje
    private IllegalArgumentException invalido(Control campo, String mensaje) {
        campo.requestFocus();
        return new IllegalArgumentException(mensaje);
    }
}
