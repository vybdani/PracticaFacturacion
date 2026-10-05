package ni.edu.uam.sistemafacturacion.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import ni.edu.uam.sistemafacturacion.App;
import ni.edu.uam.sistemafacturacion.util.Alertas;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML
    private Button btnSalir;

    // Ventanas secundarias abiertas, por ruta FXML (evita abrir la misma dos veces)
    private final Map<String, Stage> ventanasAbiertas = new HashMap<>();


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
     * Abre una vista FXML en una ventana que depende de la principal.
     * Categorías y Productos pueden estar abiertas a la vez;
     * si la ventana ya está abierta, solo se trae al frente.
     */
    private void abrirVentana(
            String rutaFxml,
            String titulo,
            double ancho,
            double alto) {

        Stage abierta = ventanasAbiertas.get(rutaFxml);
        if (abierta != null) {
            abierta.toFront();
            return;
        }

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
             * Hace que la nueva ventana dependa
             * de la ventana principal (se cierra con ella).
             */
            stage.initOwner(ventanaPrincipal());

            ventanasAbiertas.put(rutaFxml, stage);
            stage.setOnHidden(evento -> ventanasAbiertas.remove(rutaFxml));

            stage.show();

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
