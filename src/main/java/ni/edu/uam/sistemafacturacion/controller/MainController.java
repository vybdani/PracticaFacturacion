package ni.edu.uam.sistemafacturacion.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ni.edu.uam.sistemafacturacion.App;
import ni.edu.uam.sistemafacturacion.util.Alertas;

import java.io.IOException;

public class MainController {

    @FXML
    private Button btnSalir;


    /**
     * Abre la ventana de categorías.
     */
    @FXML
    private void abrirCategorias() {

        abrirVentana(
                "/ni/edu/uam/sistemafacturacion/view/categoria-view.fxml",
                "Gestión de Categorías",
                850,
                600
        );
    }


    /**
     * Abre la ventana de productos.
     */
    @FXML
    private void abrirProductos() {

        abrirVentana(
                "/ni/edu/uam/sistemafacturacion/view/producto-view.fxml",
                "Gestión de Productos",
                1050,
                760
        );
    }


    /**
     * Abre una vista FXML en una ventana modal
     * que depende de la ventana principal.
     */
    private void abrirVentana(
            String rutaFxml,
            String titulo,
            double ancho,
            double alto) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource(rutaFxml)
            );

            Scene scene = new Scene(
                    loader.load(),
                    ancho,
                    alto
            );

            scene.getStylesheets().add(
                    App.class.getResource(App.TEMA_CSS).toExternalForm()
            );

            Stage stage = new Stage();

            stage.setTitle(titulo);

            stage.setScene(scene);

            stage.setMinWidth(750);
            stage.setMinHeight(500);

            /*
             * Impide trabajar con la ventana principal
             * mientras la ventana secundaria esté abierta.
             */
            stage.initModality(
                    Modality.WINDOW_MODAL
            );

            /*
             * Hace que la nueva ventana dependa
             * de la ventana principal.
             */
            stage.initOwner(ventanaPrincipal());

            stage.showAndWait();

        } catch (IOException e) {

            Alertas.error(
                    "Error al abrir " + titulo,
                    e.getMessage()
            );
        }
    }


    /**
     * Cierra la aplicación.
     */
    @FXML
    private void salir() {
        ventanaPrincipal().close();
    }


    private Stage ventanaPrincipal() {
        return (Stage) btnSalir.getScene().getWindow();
    }
}
