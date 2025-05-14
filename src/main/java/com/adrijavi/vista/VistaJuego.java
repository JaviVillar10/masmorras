package com.adrijavi.vista;

import com.adrijavi.modelo.Juego;
import com.adrijavi.modelo.Celda;
import com.adrijavi.modelo.TipoCelda;
import com.adrijavi.modelo.Protagonista;
import com.adrijavi.modelo.Enemigo;
import com.adrijavi.modelo.Personaje;
import com.adrijavi.observador.ObservadorJuego;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Font;

public class VistaJuego extends BorderPane implements ObservadorJuego {
    private Juego juego;
    private GridPane gridTablero;
    private VBox panelInformacion;
    private Label labelOrdenTurnos;

    public VistaJuego(Juego juego) {
        this.juego = juego;
        this.juego.añadirObservador(this);
        configurarLayout();
        actualizarTablero();
        actualizarPanelInformacion();
    }

    private void configurarLayout() {
        // Configuración del tablero
        gridTablero = new GridPane();
        gridTablero.setHgap(2);
        gridTablero.setVgap(2);
        gridTablero.setPadding(new Insets(10));
        gridTablero.setStyle("-fx-background-color: #2C1810; -fx-border-color: #8B4513; -fx-border-width: 3; -fx-border-radius: 5;");
        this.setCenter(gridTablero);

        // Configuración del panel de información
        panelInformacion = new VBox(10);
        panelInformacion.setPadding(new Insets(15));
        panelInformacion.setMaxWidth(200);
        panelInformacion.setStyle("-fx-background-color: #2C1810; -fx-border-color: #8B4513; -fx-border-width: 3; -fx-border-radius: 5;");
        
        Label tituloPanel = new Label("Información del Juego");
        tituloPanel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-font-family: 'Times New Roman';");
        panelInformacion.getChildren().add(tituloPanel);
        
        labelOrdenTurnos = new Label();
        labelOrdenTurnos.setFont(new Font(12));
        panelInformacion.getChildren().add(labelOrdenTurnos);
        this.setRight(panelInformacion);
    }

    public void actualizarTablero() {
        gridTablero.getChildren().clear();
        Celda[][] celdas = juego.getTablero();
        if (celdas == null) return;
        for (int f = 0; f < celdas.length; f++) {
            final int fila = f;
            for (int c = 0; c < celdas[0].length; c++) {
                final int columna = c;
                Celda celda = celdas[fila][columna];
                StackPane panelCelda = new StackPane();
                panelCelda.setPrefSize(60, 60);
                
                if (celda.getTipo() == TipoCelda.PARED) {
                    panelCelda.setStyle("-fx-background-color: #4A3728; -fx-border-color: #8B4513; -fx-border-width: 1;");
                } else if (celda.getTipo() == TipoCelda.TRAMPA) {
                    panelCelda.setStyle("-fx-background-color: #D2B48C; -fx-border-color: #8B4513; -fx-border-width: 1;");
                    Label trampa = new Label("*");
                    trampa.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill:rgba(68, 0, 255, 0.4);");
                    panelCelda.getChildren().add(trampa);
                } else {
                    panelCelda.setStyle("-fx-background-color: #D2B48C; -fx-border-color: #8B4513; -fx-border-width: 1;");
                }

                if (juego.getProtagonista() != null && juego.getProtagonista().getFila() == fila &&
                        juego.getProtagonista().getColumna() == columna && juego.getProtagonista().estaVivo()) {
                    Label etiqueta = new Label("P");
                    etiqueta.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4169E1;");
                    panelCelda.getChildren().add(etiqueta);
                }
                gridTablero.add(panelCelda, columna, fila);
            }
        }
    }

    public void actualizarPanelInformacion() {
        panelInformacion.getChildren().clear();
        
        // Título del panel
        Label tituloPanel = new Label("Información del Juego");
        tituloPanel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-font-family: 'Times New Roman';");
        panelInformacion.getChildren().add(tituloPanel);

        // Información del protagonista
        if (juego.getProtagonista() != null) {
            Protagonista p = juego.getProtagonista();
            Label infoP = new Label("Protagonista: " + p.getNombre() +
                    "\nSalud: " + p.getSalud() +
                    "\nFuerza: " + p.getFuerza() +
                    "\nDefensa: " + p.getDefensa() +
                    "\nVelocidad: " + p.getVelocidad() +
                    "\nPercepción: " + p.getPercepcion());
            infoP.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-background-color: rgba(70, 70, 70, 0.8); -fx-padding: 10; -fx-background-radius: 5;");
            panelInformacion.getChildren().add(infoP);
        }
        
        // Sección de enemigos
        Label etiquetaEnemigos = new Label("Enemigos:");
        etiquetaEnemigos.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-font-family: 'Times New Roman'; -fx-padding: 5 0 5 0;");
        panelInformacion.getChildren().add(etiquetaEnemigos);
        
        if (juego.getEnemigos() != null && !juego.getEnemigos().isEmpty()) {
            for (Enemigo enemigo : juego.getEnemigos()) {
                Label etiquetaEnemigo = new Label(enemigo.getNombre() + " -- Salud: " + enemigo.getSalud());
                etiquetaEnemigo.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-background-color: rgba(70, 70, 70, 0.8); -fx-padding: 10; -fx-background-radius: 5;");
                panelInformacion.getChildren().add(etiquetaEnemigo);
            }
        }
        
        // Sección de orden de turnos
        Label tituloTurnos = new Label("Orden de Turnos:");
        tituloTurnos.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-font-family: 'Times New Roman'; -fx-padding: 5 0 5 0;");
        panelInformacion.getChildren().add(tituloTurnos);
        
        StringBuilder sb = new StringBuilder();
        for (Personaje p : juego.getOrdenTurnos()) {
            sb.append(p.getNombre()).append(" (").append(p.getVelocidad()).append(")\n");
        }
        labelOrdenTurnos = new Label(sb.toString());
        labelOrdenTurnos.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-background-color: rgba(70, 70, 70, 0.8); -fx-padding: 10; -fx-background-radius: 5;");
        panelInformacion.getChildren().add(labelOrdenTurnos);
    }

    @Override
    public void alActualizarJuego() {
        actualizarTablero();
        actualizarPanelInformacion();
    }
}
