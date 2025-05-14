package com.adrijavi.modelo;

public class Celda {
    private TipoCelda tipo;
    private int fila;
    private int columna;

    public Celda(TipoCelda tipo, int fila, int columna) {
        this.tipo = tipo;
        this.fila = fila;
        this.columna = columna;
    }

    public TipoCelda getTipo() { return tipo; }
    public int getFila() { return fila; }
    public int getColumna() { return columna; }
    
    public boolean esCaminable() {
        return tipo == TipoCelda.SUELO;
    }
}