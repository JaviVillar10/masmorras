package com.adrijavi.controlador;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import com.adrijavi.modelo.*;
import com.adrijavi.observador.ObservadorJuego;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.control.DialogPane;

public class ControladorJuegoVista implements ObservadorJuego {
    @FXML
    private GridPane gridTablero;
    @FXML
    private VBox panelInformacion;
    @FXML
    private Label labelOrdenTurnos;
    @FXML
    private Label labelTurnoActual;
    @FXML
    private ImageView imagenProtagonista;
    @FXML
    private Label labelInfoProtagonista;
    @FXML
    private VBox panelEnemigos;
    private Juego juego;
    private boolean mostrandoDialogoNivel = false;
    private boolean mostrandoGameOver = false;
    private Map<String, Image> imagenesEnemigos;
    private Image imagenProtagonistaCache;

    public void setJuego(Juego juego) {
        this.juego = juego;
        juego.añadirObservador(this);
        precargarImagenes();
        actualizarVista();
    }

    private void precargarImagenes() {
        imagenesEnemigos = new HashMap<>();
        try {
            // Precargar imagen del protagonista
            imagenProtagonistaCache = new Image(getClass().getResourceAsStream("/com/adrijavi/recursos/protagonista.png"));
            imagenProtagonista.setImage(imagenProtagonistaCache);

            // Precargar imágenes de enemigos
            imagenesEnemigos.put("dragon", new Image(getClass().getResourceAsStream("/com/adrijavi/recursos/dragon.png")));
            imagenesEnemigos.put("esqueleto", new Image(getClass().getResourceAsStream("/com/adrijavi/recursos/esqueleto.png")));
            imagenesEnemigos.put("goblin", new Image(getClass().getResourceAsStream("/com/adrijavi/recursos/goblin.png")));
            imagenesEnemigos.put("rey mono", new Image(getClass().getResourceAsStream("/com/adrijavi/recursos/rey_mono.png")));
            imagenesEnemigos.put("enemigo", new Image(getClass().getResourceAsStream("/com/adrijavi/recursos/enemigo.png")));
        } catch (Exception e) {
            System.err.println("Error al precargar imágenes: " + e.getMessage());
        }
    }

    // Actualización completa de la vista
    private void actualizarVista() {
        actualizarTablero();
        actualizarPanelInformacion();
    }

    private void actualizarTablero() {
        gridTablero.getChildren().clear();
        Celda[][] celdas = juego.getTablero();
        if (celdas == null)
            return;

        for (int f = 0; f < celdas.length; f++) {
            for (int c = 0; c < celdas[0].length; c++) {
                StackPane panelCelda = new StackPane();
                panelCelda.setPrefSize(50, 50);
                
                // Estilo base para todas las celdas
                String estiloBase = "-fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 0);";
                
                // Estilo específico según el tipo de celda
                if (celdas[f][c].getTipo() == TipoCelda.PARED) {
                    panelCelda.setStyle(estiloBase + " -fx-background-color: linear-gradient(to bottom, #4A3728, #2C1810); -fx-border-color: #8B4513; -fx-border-width: 2;");
                } else if (celdas[f][c].getTipo() == TipoCelda.TRAMPA) {
                    panelCelda.setStyle(estiloBase + " -fx-background-color: linear-gradient(to bottom, #D2B48C, #A0522D); -fx-border-color: #8B4513; -fx-border-width: 1;");
                    Label trampa = new Label("*");
                    trampa.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");
                    panelCelda.getChildren().add(trampa);
                } else {
                    panelCelda.setStyle(estiloBase + " -fx-background-color: linear-gradient(to bottom, #D2B48C, #A0522D); -fx-border-color: #8B4513; -fx-border-width: 1;");
                }

                // Mostrar personajes con mejor estilo
                Personaje personaje = juego.obtenerPersonajeEn(f, c);
                if (personaje != null) {
                    Label etiqueta = new Label(personaje instanceof Protagonista ? "P" : "E");
                    etiqueta.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 2, 0, 0, 0);");
                    
                    if (personaje instanceof Protagonista) {
                        etiqueta.setTextFill(Color.rgb(65, 105, 225)); // Azul real
                    } else {
                        etiqueta.setTextFill(Color.rgb(220, 20, 60)); // Rojo carmesí
                    }
                    
                    panelCelda.getChildren().add(etiqueta);
                }
                gridTablero.add(panelCelda, c, f);
            }
        }
    }

    private void actualizarPanelInformacion() {
        // Turno actual
        Personaje personajeActual = juego.getPersonajeEnTurno();
        if (personajeActual != null) {
            labelTurnoActual.setText(String.format("Turno actual: %s\nSalud: %d", 
                personajeActual.getNombre(),
                personajeActual.getSalud()));
            labelTurnoActual.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        } else {
            labelTurnoActual.setText("Turno actual: N/A");
        }
        
        // Información del protagonista
        if (juego.getProtagonista() != null) {
            Protagonista p = juego.getProtagonista();
            labelInfoProtagonista.setText(
                String.format("Nombre: %s\nSalud: %d\nFuerza: %d\nDefensa: %d\nVelocidad: %d\nPercepción: %d",
                    p.getNombre(),
                    p.getSalud(),
                    p.getFuerza(),
                    p.getDefensa(),
                    p.getVelocidad(),
                    p.getPercepcion()
                )
            );
        }
        
        // Información de los enemigos
        panelEnemigos.getChildren().clear();
        for (Enemigo enemigo : juego.getEnemigos()) {
            if (enemigo.estaVivo()) {
                HBox contenedorEnemigo = new HBox(10);
                contenedorEnemigo.setAlignment(Pos.CENTER_LEFT);
                contenedorEnemigo.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-padding: 10; -fx-background-radius: 5;");
                
                // Obtener la imagen del enemigo desde el caché
                String tipoEnemigo = "";
                if (enemigo.getNombre().toLowerCase().contains("dragon")) {
                    tipoEnemigo = "dragon";
                } else if (enemigo.getNombre().toLowerCase().contains("esqueleto")) {
                    tipoEnemigo = "esqueleto";
                } else if (enemigo.getNombre().toLowerCase().contains("goblin")) {
                    tipoEnemigo = "goblin";
                } else if (enemigo.getNombre().toLowerCase().contains("rey mono")) {
                    tipoEnemigo = "rey mono";
                } else {
                    tipoEnemigo = "enemigo";
                }
                
                ImageView imagenEnemigo = new ImageView(imagenesEnemigos.get(tipoEnemigo));
                if (tipoEnemigo.equals("rey mono")) {
                    imagenEnemigo.setFitHeight(200);
                    imagenEnemigo.setFitWidth(200);
                    contenedorEnemigo.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");
                } else {
                    imagenEnemigo.setFitHeight(50);
                    imagenEnemigo.setFitWidth(50);
                    contenedorEnemigo.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-padding: 10; -fx-background-radius: 5;");
                }
                imagenEnemigo.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 0);");
                contenedorEnemigo.getChildren().add(imagenEnemigo);
                
                VBox infoEnemigo = new VBox(5);
                Label nombreEnemigo = new Label(enemigo.getNombre());
                nombreEnemigo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #FFD700; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 2, 0, 0, 0);");
                
                Label statsEnemigo = new Label(String.format(
                    "Salud: %d\nFuerza: %d\nDefensa: %d\nVelocidad: %d",
                    enemigo.getSalud(),
                    enemigo.getFuerza(),
                    enemigo.getDefensa(),
                    enemigo.getVelocidad()
                ));
                statsEnemigo.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                
                infoEnemigo.getChildren().addAll(nombreEnemigo, statsEnemigo);
                contenedorEnemigo.getChildren().add(infoEnemigo);
                
                panelEnemigos.getChildren().add(contenedorEnemigo);
            }
        }
        
        // Orden de turnos
        StringBuilder orden = new StringBuilder();
        for (Personaje p : juego.getOrdenTurnos()) {
            orden.append(String.format("%s (Vel: %d, Salud: %d)\n",
                p.getNombre(),
                p.getVelocidad(),
                p.getSalud()));
        }
        labelOrdenTurnos.setText(orden.toString());
    }

    @Override
    public void alActualizarJuego() {
        Platform.runLater(() -> {
            actualizarVista();

            // Verificar si el protagonista ha muerto
            if ((juego.getProtagonista() == null || !juego.getProtagonista().estaVivo()) && !mostrandoGameOver) {
                mostrandoGameOver = true;
                mostrarGameOver();
            } else if (juego.esNivelCompleto() && !mostrandoDialogoNivel) {
                mostrandoDialogoNivel = true;
                
                // Si es el nivel 3, mostrar pantalla de victoria
                if (juego.getNivelActual() == 3) {
                    mostrarVictoriaFinal();
                } else {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Nivel Completado");
                    alert.setHeaderText("¡Has completado el nivel " + juego.getNivelActual() + "!");
                    alert.setContentText("¿Quieres continuar al siguiente nivel?");

                    // Estilo para el diálogo
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.setStyle("-fx-background-color: #2C1810; -fx-background-radius: 10;");
                    
                    // Estilo para los botones
                    dialogPane.lookupButton(ButtonType.OK).setStyle("-fx-background-color: #006400; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                    dialogPane.lookupButton(ButtonType.CANCEL).setStyle("-fx-background-color: #4A3728; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                    
                    // Estilo para el contenido
                    dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-font-weight: bold;");
                    dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: #FFD700; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Times New Roman';");

                    ButtonType buttonTypeContinuar = new ButtonType("Continuar");
                    ButtonType buttonTypeTerminar = new ButtonType("Terminar Partida");
                    alert.getButtonTypes().setAll(buttonTypeContinuar, buttonTypeTerminar);

                    alert.showAndWait().ifPresent(response -> {
                        if (response == buttonTypeContinuar) {
                            if (!juego.cargarSiguienteNivel()) {
                                mostrarVictoriaFinal();
                            } else {
                                actualizarVista();
                            }
                        } else {
                            mostrarVictoriaFinal();
                        }
                        mostrandoDialogoNivel = false;
                    });
                }
            }
        });
    }

    private void mostrarGameOver() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("¡Has sido derrotado!");
        alert.setContentText("¿Qué deseas hacer?");
        
        // Estilo para el diálogo
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2C1810; -fx-background-radius: 10;");
        
        // Estilo para los botones
        dialogPane.lookupButton(ButtonType.OK).setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        dialogPane.lookupButton(ButtonType.CANCEL).setStyle("-fx-background-color: #4A3728; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Estilo para el contenido
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-font-weight: bold;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: #FFD700; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Times New Roman';");

        ButtonType buttonTypeReintentar = new ButtonType("Reintentar");
        ButtonType buttonTypeSalir = new ButtonType("Salir del Juego");
        alert.getButtonTypes().setAll(buttonTypeReintentar, buttonTypeSalir);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeReintentar) {
                // Reiniciar el juego
                try {
                    mostrandoGameOver = false;
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/adrijavi/vista/VistaCrearPersonaje.fxml"));
                    Parent root = loader.load();
                    Scene escena = new Scene(root, 400, 500);
                    Stage stage = (Stage) gridTablero.getScene().getWindow();
                    stage.setScene(escena);
                    stage.setTitle("Creación de Personaje");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                System.exit(0);
            }
        });
    }

    private void mostrarVictoriaFinal() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("¡Victoria!");
        alert.setHeaderText("¡Has completado el juego!");
        alert.setContentText("¿Qué deseas hacer?");
        
        // Estilo para el diálogo
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2C1810; -fx-background-radius: 10;");
        
        // Estilo para los botones
        dialogPane.lookupButton(ButtonType.OK).setStyle("-fx-background-color: #006400; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        dialogPane.lookupButton(ButtonType.CANCEL).setStyle("-fx-background-color: #4A3728; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Estilo para el contenido
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-font-weight: bold;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: #FFD700; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Times New Roman';");

        ButtonType buttonTypeNuevaPartida = new ButtonType("Nueva Partida");
        ButtonType buttonTypeSalir = new ButtonType("Salir del Juego");
        alert.getButtonTypes().setAll(buttonTypeNuevaPartida, buttonTypeSalir);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeNuevaPartida) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/adrijavi/vista/VistaCrearPersonaje.fxml"));
                    Parent root = loader.load();
                    Scene escena = new Scene(root, 400, 500);
                    Stage stage = (Stage) gridTablero.getScene().getWindow();
                    stage.setScene(escena);
                    stage.setTitle("Creación de Personaje");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                System.exit(0);
            }
        });
    }

    public EventHandler<KeyEvent> getManejadorEventoTeclado() {
        return event -> {
            // Si no es el turno del protagonista, avanzar al siguiente turno
            if (!(juego.getPersonajeEnTurno() instanceof Protagonista)) {
                System.out.println("Avanzando al siguiente turno...");
                juego.siguienteTurno();
                return;
            }

            String direccion = null;
            switch (event.getCode()) {
                case UP:
                case W:
                    direccion = "ARRIBA";
                    break;
                case DOWN:
                case S:
                    direccion = "ABAJO";
                    break;
                case LEFT:
                case A:
                    direccion = "IZQUIERDA";
                    break;
                case RIGHT:
                case D:
                    direccion = "DERECHA";
                    break;
                default:
                    System.out.println("Tecla no válida.");
                    return;
            }

            System.out.println("Protagonista se mueve en dirección: " + direccion);
            juego.moverProtagonista(direccion);
            juego.siguienteTurno();
        };
    }
}