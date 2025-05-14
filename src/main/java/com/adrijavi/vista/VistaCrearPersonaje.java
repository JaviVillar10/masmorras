package com.adrijavi.vista;

import com.adrijavi.modelo.Protagonista;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class VistaCrearPersonaje {
    private Stage stage;
    private TextField tfNombre;
    private Spinner<Integer> spSalud, spFuerza, spDefensa, spVelocidad, spPercepcion;
    private Button btnIniciar;
    private Label lblPuntosRestantes;
    private int puntosRestantes = 15;

    public VistaCrearPersonaje(Stage stage) {
        this.stage = stage;
    }

    /**
     * Muestra la pantalla de configuración y ejecuta la función onJugadorCreado cuando se introducen los datos correctos.
     */
    public void mostrar(Consumer<Protagonista> onJugadorCreado) {
        VBox raíz = new VBox(10);
        raíz.setPadding(new Insets(15));

        Label título = new Label("Configuración del Protagonista");
        título.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        raíz.getChildren().add(título);

        tfNombre = new TextField();
        tfNombre.setPromptText("Nombre");

        lblPuntosRestantes = new Label("Puntos restantes: " + puntosRestantes);

        spSalud = crearSpinner();
        spFuerza = crearSpinner();
        spDefensa = crearSpinner();
        spVelocidad = crearSpinner();
        spPercepcion = crearSpinner();

        raíz.getChildren().addAll(
                new Label("Nombre:"), tfNombre,
                lblPuntosRestantes,
                new Label("Salud:"), spSalud,
                new Label("Fuerza:"), spFuerza,
                new Label("Defensa:"), spDefensa,
                new Label("Velocidad:"), spVelocidad,
                new Label("Percepción:"), spPercepcion
        );

        btnIniciar = new Button("Iniciar Juego");
        btnIniciar.setOnAction(e -> {
            try {
                String nombre = tfNombre.getText().trim();
                if (puntosRestantes != 0) {
                    throw new IllegalArgumentException("Debes asignar todos los puntos antes de continuar.");
                }
                int salud = spSalud.getValue();
                int fuerza = spFuerza.getValue();
                int defensa = spDefensa.getValue();
                int velocidad = spVelocidad.getValue();
                int percepcion = spPercepcion.getValue();

                Protagonista protagonista = new Protagonista(nombre, salud, fuerza, defensa, velocidad, percepcion, 1, 1);
                onJugadorCreado.accept(protagonista);
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Datos incorrectos. Por favor, revise la información.");
                alert.showAndWait();
            }
        });
        raíz.getChildren().add(btnIniciar);

        Scene escena = new Scene(raíz, 300, 400);
        stage.setTitle("Configuración del Protagonista");
        stage.setScene(escena);
        stage.show();
    }

    private Spinner<Integer> crearSpinner() {
        Spinner<Integer> spinner = new Spinner<>(0, 20, 0);
        spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            int cambio = newValue - oldValue;
            puntosRestantes -= cambio;
            lblPuntosRestantes.setText("Puntos restantes: " + puntosRestantes);
            if (puntosRestantes < 0) {
                spinner.getValueFactory().setValue(oldValue);
                puntosRestantes += cambio;
                lblPuntosRestantes.setText("Puntos restantes: " + puntosRestantes);
            }
        });
        return spinner;
    }
}
