package com.adrijavi.controlador;

import com.adrijavi.modelo.Juego;
import com.adrijavi.modelo.Protagonista;
import com.adrijavi.observador.ObservadorJuego;

import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;

public class ControladorJuego implements ObservadorJuego {
    private Juego juego;

    public ControladorJuego(Juego juego) {
        this.juego = juego;
        this.juego.añadirObservador(this);
    }
    
    public EventHandler<KeyEvent> getManejadorEventoTeclado() {
        return event -> {
            if (!(juego.getPersonajeEnTurno() instanceof Protagonista)) return;
            
            String direccion = null;
            switch(event.getCode()){
                case UP:
                case W: direccion = "ARRIBA"; break;
                case DOWN:
                case S: direccion = "ABAJO"; break;
                case LEFT:
                case A: direccion = "IZQUIERDA"; break;
                case RIGHT:
                case D: direccion = "DERECHA"; break;
                default: break;
            }
            
            if(direccion != null){
                juego.moverProtagonista(direccion);
                juego.turnoEnemigos();
                juego.siguienteTurno();
            }
        };
    }

    @Override
    public void alActualizarJuego() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'alActualizarJuego'");
    }
    
}