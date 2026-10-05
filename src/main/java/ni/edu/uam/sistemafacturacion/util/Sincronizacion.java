package ni.edu.uam.sistemafacturacion.util;

import javafx.scene.Node;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;

/*
 * Mantiene conectadas las ventanas de Categorías y Productos.
 * Cuando una ventana guarda, actualiza o elimina, avisa aquí y
 * la otra ventana (si está abierta) vuelve a cargar sus datos.
 */
public final class Sincronizacion {

    private static final List<Runnable> alCambiarCategorias = new ArrayList<>();

    private static final List<Runnable> alCambiarProductos = new ArrayList<>();

    private Sincronizacion() {
    }

    // La acción se ejecuta cada vez que cambian las categorías, mientras la ventana del nodo esté abierta
    public static void escucharCategorias(Node nodo, Runnable accion) {
        registrar(alCambiarCategorias, nodo, accion);
    }

    public static void escucharProductos(Node nodo, Runnable accion) {
        registrar(alCambiarProductos, nodo, accion);
    }

    public static void categoriasCambiaron() {
        notificar(alCambiarCategorias);
    }

    public static void productosCambiaron() {
        notificar(alCambiarProductos);
    }

    private static void registrar(List<Runnable> lista, Node nodo, Runnable accion) {
        lista.add(accion);

        // Al cerrar la ventana se deja de escuchar
        nodo.sceneProperty().addListener((observable, anterior, scene) -> {
            if (scene != null) {
                scene.windowProperty().addListener((obs, anteriorVentana, ventana) -> {
                    if (ventana != null) {
                        ventana.addEventHandler(WindowEvent.WINDOW_HIDDEN, evento -> lista.remove(accion));
                    }
                });
            }
        });
    }

    private static void notificar(List<Runnable> lista) {
        // Se recorre una copia por si alguna acción cierra una ventana durante el aviso
        for (Runnable accion : List.copyOf(lista)) {
            accion.run();
        }
    }
}
