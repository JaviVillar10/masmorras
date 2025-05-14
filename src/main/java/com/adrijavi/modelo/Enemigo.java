package com.adrijavi.modelo;

import java.util.Random;

public class Enemigo extends Personaje {
    private boolean esJefeFinal = false;
    private Random random = new Random();

    public Enemigo(String nombre, int salud, int fuerza, int defensa, int velocidad, int percepcion, int fila, int columna) {
        super(nombre, salud, fuerza, defensa, velocidad, percepcion, fila, columna);
        // Si es el Rey Mono, marcarlo como jefe final
        if (nombre.equals("Rey Mono")) {
            esJefeFinal = true;
        }
    }

    @Override
    public void recibirDano(int dano) {
        super.recibirDano(dano);
        
        // Si es el jefe final y ha muerto, hay 70 de probabilidad de resurrección
        if (esJefeFinal && !estaVivo()) {
            if (random.nextDouble() < 0.7) { 
                // Resurrección con 100 de vida
                setSalud(100);
            }
        }
    }
}