package ni.edu.uam.sistemafacturacion.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ni.edu.uam.sistemafacturacion.model.Categoria;
import ni.edu.uam.sistemafacturacion.service.CategoriaService;

import java.util.Optional;

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
    private TableColumn<Categoria, Boolean> colActiva;

    // BOTONES
    @FXML
    private Button btnNuevo;

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

        nuevo();
    }

    // CONFIGURAR COLUMNAS
    private void configurarColumnas() {

        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                cellData.getValue().getId()).asObject());

        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));

        colActiva.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isActiva()).asObject());
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
    private void mostrarCategoria(
            Categoria categoria) {
        txtId.setText( String.valueOf(categoria.getId()));
        txtNombre.setText(categoria.getNombre());
        chkActiva.setSelected(
                categoria.isActiva());
    }

    // CARGAR CATEGORÍAS
    private void cargarCategorias() {
        try {
            tblCategorias.setItems(
                    FXCollections.observableArrayList(categoriaService.listar())
            );
        } catch (Exception e) {
            mostrarError("Error al cargar las categorías", e.getMessage());
        }
    }

    // NUEVO
    @FXML
    private void nuevo() {
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
        if (!validarFormulario()) {
            return;
        }

        String nombre = txtNombre.getText().trim();

        boolean activa = chkActiva.isSelected();

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setActiva(activa);
        try {
            categoriaService.guardar(categoria);
            mostrarInformacion(
                    "Categoría guardada",
                    "La categoría se registró correctamente."
            );
            cargarCategorias();
            nuevo();
        } catch (Exception e) {
            mostrarError("No se pudo guardar la categoría", e.getMessage());
        }
    }

    // ACTUALIZAR
    @FXML
    private void actualizar() {
        if (!validarFormulario()) {
            return;
        }

        if (txtId.getText().isBlank()) {
            mostrarAdvertencia("Seleccione una categoría", "Debe seleccionar una categoría de la tabla.");
            return;
        }

        Optional<ButtonType> resultado =
                mostrarConfirmacion("Actualizar categoría", "¿Desea actualizar esta categoría?");

        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        try {
            Integer id = Integer.parseInt(txtId.getText());
            Categoria categoria = new Categoria();
            categoria.setId(id);
            categoria.setNombre( txtNombre.getText().trim());
            categoria.setActiva( chkActiva.isSelected());
            categoriaService.actualizar(categoria);
            mostrarInformacion("Categoría actualizada", "La categoría se actualizó correctamente.");
            cargarCategorias();
            nuevo();
        } catch (NumberFormatException e) {
            mostrarError("ID inválido", "El identificador de la categoría no es válido.");
        } catch (Exception e) {
            mostrarError("No se pudo actualizar la categoría", e.getMessage());
        }
    }

    // ELIMINAR
    @FXML
    private void eliminar() {
        if (txtId.getText().isBlank()) {
            mostrarAdvertencia("Seleccione una categoría", "Debe seleccionar una categoría de la tabla.");
            return;
        }

        Optional<ButtonType> resultado = mostrarConfirmacion("Eliminar categoría", "¿Está seguro de eliminar esta categoría?");
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        try {
            Integer id = Integer.parseInt(txtId.getText());
            categoriaService.eliminar(id);
            mostrarInformacion("Categoría eliminada", "La categoría se eliminó correctamente.");
            cargarCategorias();
            nuevo();
        } catch (NumberFormatException e) {
            mostrarError("ID inválido","El identificador de la categoría no es válido.");
        } catch (Exception e) {
            mostrarError("No se pudo eliminar la categoría", e.getMessage());
        }
    }

    // VALIDAR FORMULARIO
    private boolean validarFormulario() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarAdvertencia("Campo requerido", "Debe ingresar el nombre de la categoría.");
            txtNombre.requestFocus();
            return false;
        }

        if (nombre.length() > 100) {
            mostrarAdvertencia("Nombre demasiado largo", "El nombre no puede superar los 100 caracteres.");
            txtNombre.requestFocus();
            return false;
        }
        return true;
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