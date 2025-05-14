package com.adrijavi.modelo;

/**
 * Clase base para el protagonista y los enemigos.
 */
public abstract class Personaje {
    protected String nombre;
    protected int salud;
    protected int saludMaxima;
    protected int fuerza;
    protected int defensa;
    protected int velocidad;
    protected int percepcion;
    protected int fila;
    protected int columna;
    
    public Personaje(String nombre, int salud, int fuerza, int defensa, int velocidad, int percepcion, int fila, int columna) {
        this.nombre = nombre;
        this.salud = salud;
        this.saludMaxima = salud;
        this.fuerza = fuerza;
        this.defensa = defensa;
        this.velocidad = velocidad;
        this.percepcion = percepcion;
        this.fila = fila;
        this.columna = columna;
    }
    
    public String getNombre() {
        return nombre;
    }
    public int getSalud() { return salud; }
    public void setSalud(int s) { salud = s; }
    public int getSaludMaxima() { return saludMaxima; }
    public void setSaludMaxima(int s) { saludMaxima = s; }
    public int getFuerza() { return fuerza; }
    public void setFuerza(int f) { fuerza = f; }
    public int getDefensa() { return defensa; }
    public void setDefensa(int d) { defensa = d; }
    public int getVelocidad() { return velocidad; }
    public void setVelocidad(int v) { velocidad = v; }
    public int getPercepcion() { return percepcion; }
    public void setPercepcion(int p) { percepcion = p; }
    
    public int getFila() { return fila; }
    public int getColumna() { return columna; }
    
    public void setPosicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }
    
    /**
     * Recibe daño y actualiza la salud del personaje.
     * @param dano Cantidad de daño a recibir
     */
    public void recibirDano(int dano) {
        int danoReal = Math.max(1, dano - defensa); // El daño mínimo es 1
        salud = Math.max(0, salud - danoReal);
    }
    
    /**
     * Verifica si el personaje está vivo.
     * @return true si el personaje está vivo, false si está muerto
     */
    public boolean estaVivo() {
        return salud > 0;
    }

    /**
     * Restaura la salud del personaje a su valor máximo original.
     */
    public void restaurarSalud() {
        this.salud = this.saludMaxima;
    }

    /**
     * Ataque sencillo: daño = fuerza atacante – defensa defensor.
     */
    public void atacar(Personaje objetivo) {
        int danio = this.fuerza - objetivo.defensa;
        if (danio < 1) {
            danio = 1;
        }
        objetivo.salud -= danio;
        System.out.println(this.nombre + " ataca a " + objetivo.nombre + " causando " + danio + " puntos de daño.");
    }
}
