package com.adrijavi.modelo;

import com.adrijavi.observador.ObservadorJuego;
import java.io.*;
import java.util.*;

public class Juego {
    private Celda[][] tablero;
    private int filas;
    private int columnas;
    private Protagonista protagonista;
    private List<Enemigo> enemigos;
    private List<ObservadorJuego> observadores;
    private List<Personaje> ordenTurnos;
    private int indiceTurnoActual = 0;
    private int nivelActual = 1;

    public Juego() {
        observadores = new ArrayList<>();
        enemigos = new ArrayList<>();
        ordenTurnos = new ArrayList<>();
    }

    // ================== MÉTODOS DE CARGA ==================
    /**
     * Carga el tablero desde un archivo de texto.
     * Formato: '#' para paredes, '.' para suelos. Añado * para trampas.
     */
    public void cargarTablero(String nombreArchivo) throws IOException {
        // Obtener la ruta del archivo desde resources
        InputStream inputStream = getClass().getResourceAsStream("/" + nombreArchivo);
        if (inputStream == null) {
            throw new FileNotFoundException("Archivo de tablero no encontrado: " + nombreArchivo);
        }

        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty())
                    lineas.add(linea);
            }
        }

        filas = lineas.size();
        columnas = lineas.get(0).length();
        tablero = new Celda[filas][columnas];

        for (int f = 0; f < filas; f++) {
            String linea = lineas.get(f);
            for (int c = 0; c < columnas; c++) {
                char caracter = linea.charAt(c);
                TipoCelda tipo;
                switch (caracter) {
                    case '#':
                        tipo = TipoCelda.PARED;
                        break;
                    case '*':
                        tipo = TipoCelda.TRAMPA;
                        break;
                    default:
                        tipo = TipoCelda.SUELO;
                }
                tablero[f][c] = new Celda(tipo, f, c);
            }
        }
    }

    /**
     * Carga enemigos desde un archivo.
     * Formato: nombre,fila,columna,salud,fuerza,defensa,velocidad,percepcion
     */
    public void cargarEnemigos(String nombreArchivo) throws IOException {
        // Obtener la ruta del archivo desde resources
        InputStream inputStream = getClass().getResourceAsStream("/" + nombreArchivo);
        if (inputStream == null) {
            throw new FileNotFoundException("Archivo de enemigos no encontrado: " + nombreArchivo);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty())
                    continue;
                String[] partes = linea.split(",");
                if (partes.length < 8)
                    continue;

                Enemigo enemigo = new Enemigo(
                        partes[0].trim(),
                        Integer.parseInt(partes[3].trim()),
                        Integer.parseInt(partes[4].trim()),
                        Integer.parseInt(partes[5].trim()),
                        Integer.parseInt(partes[6].trim()),
                        Integer.parseInt(partes[7].trim()),
                        Integer.parseInt(partes[1].trim()),
                        Integer.parseInt(partes[2].trim()));
                enemigos.add(enemigo);
            }
        }
    }

    // ================== LÓGICA DE TURNOS ==================
    public void siguienteTurno() {
        if (ordenTurnos.isEmpty()) {
            System.out.println("[JUEGO] ¡Todos los personajes han muerto!");
            return;
        }

        // Verificar si el protagonista está vivo
        if (protagonista == null || !protagonista.estaVivo()) {
            System.out.println("[JUEGO] El protagonista ha muerto. Fin del juego.");
            notificarObservadores();
            return;
        }

        // Verificar si el nivel está completo
        if (esNivelCompleto()) {
            System.out.println("[JUEGO] Nivel completado.");
            notificarObservadores();
            return;
        }

        // Avanzar al siguiente turno
        indiceTurnoActual = (indiceTurnoActual + 1) % ordenTurnos.size();

        // Depuración: Mostrar el personaje en turno
        Personaje personajeEnTurno = ordenTurnos.get(indiceTurnoActual);
        System.out.println("[JUEGO] Turno de: " + personajeEnTurno.getNombre());

        // Si el turno actual es de un enemigo, ejecutar su turno automáticamente
        if (personajeEnTurno instanceof Enemigo) {
            System.out.println("[JUEGO] Ejecutando turno del enemigo: " + personajeEnTurno.getNombre());
            ejecutarTurnoEnemigo(personajeEnTurno);
            // Notificar a los observadores después de que el enemigo complete su turno
            notificarObservadores();
            // Avanzar automáticamente al siguiente turno después de que el enemigo actúe
            siguienteTurno();
        } else {
            // Si es el turno del protagonista, solo notificar a los observadores
            notificarObservadores();
        }
    }

    public void inicializarOrdenTurnos() {
        System.out.println("[JUEGO] Inicializando orden de turnos...");
        List<Personaje> nuevosTurnos = new ArrayList<>();

        // Protagonista
        if (protagonista != null && protagonista.estaVivo()) {
            nuevosTurnos.add(protagonista);
            System.out.println("[JUEGO] Añadido protagonista a la lista de turnos");
        }

        // Enemigos vivos
        for (Enemigo enemigo : enemigos) {
            if (enemigo.estaVivo()) {
                nuevosTurnos.add(enemigo);
                System.out.println("[JUEGO] Añadido enemigo " + enemigo.getNombre() + " a la lista de turnos");
            }
        }

        // Ordenar por velocidad descendente
        nuevosTurnos.sort((p1, p2) -> p2.getVelocidad() - p1.getVelocidad());

        // Ajustar índice del turno actual
        Personaje personajeActual = ordenTurnos.isEmpty() ? null : ordenTurnos.get(indiceTurnoActual);
        ordenTurnos = nuevosTurnos;

        if (personajeActual != null) {
            indiceTurnoActual = ordenTurnos.indexOf(personajeActual);
            if (indiceTurnoActual == -1) {
                indiceTurnoActual = 0; // Si el personaje actual fue eliminado, reiniciar al primer turno
            }
        } else {
            indiceTurnoActual = 0;
        }

        // Mostrar el nuevo orden de turnos
        System.out.println("[JUEGO] Orden de turnos inicializado:");
        for (Personaje p : ordenTurnos) {
            System.out.println(" - " + p.getNombre() + " (Velocidad: " + p.getVelocidad() + ")");
        }
    }

    // MOVIMIENTO Y COMBATE
    public void moverProtagonista(String direccion) {
        if (esJuegoTerminado() || !(getPersonajeEnTurno() instanceof Protagonista))
            return;

        int filaActual = protagonista.getFila();
        int colActual = protagonista.getColumna();
        int nuevaFila = filaActual;
        int nuevaCol = colActual;

        switch (direccion) {
            case "ARRIBA":
                nuevaFila--;
                break;
            case "ABAJO":
                nuevaFila++;
                break;
            case "IZQUIERDA":
                nuevaCol--;
                break;
            case "DERECHA":
                nuevaCol++;
                break;
            default:
                return;
        }

        intentarMovimiento(protagonista, nuevaFila, nuevaCol);

        // Avanzar al siguiente turno después de que el protagonista actúe
        System.out.println("[DEPURACIÓN] Avanzando al siguiente turno...");
        System.out.println("[DEPURACIÓN] Lista de turnos:");
        for (Personaje p : ordenTurnos) {
            System.out.println(" - " + p.getNombre() + " (Velocidad: " + p.getVelocidad() + ")");
        }
        System.out.println("[DEPURACIÓN] Índice del turno actual: " + indiceTurnoActual);
        siguienteTurno();
    }

    public void intentarMovimiento(Personaje personaje, int nuevaFila, int nuevaCol) {
        // Validar límites del tablero
        if (nuevaFila < 0 || nuevaFila >= filas || nuevaCol < 0 || nuevaCol >= columnas) {
            System.out.println("[MOVIMIENTO] Movimiento fuera de límites.");
            return;
        }

        // Validar celda caminable
        Celda celdaDestino = tablero[nuevaFila][nuevaCol];
        if (!celdaDestino.esCaminable()) {
            System.out.println("[MOVIMIENTO] Celda no caminable.");
            return;
        }

        // Buscar personaje en la celda destino
        Personaje otro = obtenerPersonajeEn(nuevaFila, nuevaCol);

        // Caso 1: Hay un personaje en la celda destino
        if (otro != null) {
            System.out.println("[MOVIMIENTO] " + personaje.getNombre() + " ataca a " + otro.getNombre());
            personaje.atacar(otro);
            if (!otro.estaVivo()) {
                System.out.println("[MOVIMIENTO] " + otro.getNombre() + " ha sido eliminado.");
                if (otro instanceof Enemigo) {
                    int indiceEliminado = ordenTurnos.indexOf(otro);
                    enemigos.remove(otro);
                    ordenTurnos.remove(otro);

                    // Ajustar índice del turno actual si es necesario
                    if (indiceEliminado < indiceTurnoActual) {
                        indiceTurnoActual = (indiceTurnoActual - 1 + ordenTurnos.size()) % ordenTurnos.size();
                    } else if (indiceEliminado == indiceTurnoActual) {
                        indiceTurnoActual = indiceTurnoActual % ordenTurnos.size();
                    }

                    System.out.println("[DEPURACIÓN] Lista de turnos después de eliminar un enemigo:");
                    for (Personaje p : ordenTurnos) {
                        System.out.println(" - " + p.getNombre() + " (Velocidad: " + p.getVelocidad() + ")");
                    }
                    System.out.println("[DEPURACIÓN] Índice del turno actual: " + indiceTurnoActual);
                }
            }
        }
        // Caso 2: Celda vacía
        else {
            System.out.println(
                    "[MOVIMIENTO] " + personaje.getNombre() + " se mueve a (" + nuevaFila + ", " + nuevaCol + ")");
            personaje.setPosicion(nuevaFila, nuevaCol);
            if (celdaDestino.getTipo() == TipoCelda.TRAMPA) {
                personaje.setSalud(personaje.getSalud() - 1);
            }
        }

        notificarObservadores();
    }

    // ================== LÓGICA DE ENEMIGOS ==================
    public void turnoEnemigos() {
        System.out.println("\n=== TURNO ENEMIGOS ===");

        // Verificar si el protagonista está vivo
        if (protagonista == null || !protagonista.estaVivo()) {
            System.out.println("[JUEGO] El protagonista ha muerto. Fin del juego.");
            notificarObservadores(); // Notificar a la vista para mostrar "Game Over"
            return;
        }

        // Crear copia para evitar ConcurrentModificationException
        List<Enemigo> enemigosActivos = new ArrayList<>(enemigos);

        for (Enemigo enemigo : enemigosActivos) {
            if (!enemigo.estaVivo()) {
                System.out.println("[ENEMIGO] " + enemigo.getNombre() + " está muerto. Saltando...");
                continue;
            }

            ejecutarTurnoEnemigo(enemigo);
        }

        System.out.println("[DEPURACIÓN] Turno de enemigos completado.");
    }

    private void ejecutarTurnoEnemigo(Personaje personaje) {
        if (!(personaje instanceof Enemigo)) {
            return;
        }

        Enemigo enemigo = (Enemigo) personaje;

        System.out.println("\n[ENEMIGO] Turno de: " + enemigo.getNombre() + " (Posición: "
                + enemigo.getFila() + "," + enemigo.getColumna() + ")");

        // Calcular distancia al protagonista
        int distancia = Math.abs(protagonista.getFila() - enemigo.getFila())
                + Math.abs(protagonista.getColumna() - enemigo.getColumna());

        System.out.println("[ENEMIGO] Distancia al protagonista: " + distancia
                + " | Percepción: " + enemigo.getPercepcion());

        int filaDestino = enemigo.getFila();
        int colDestino = enemigo.getColumna();

        // Lógica de movimiento
        if (distancia <= enemigo.getPercepcion()) {
            System.out.println("[ENEMIGO] Moviendo hacia el protagonista...");

            // Movimiento vertical
            if (protagonista.getFila() < enemigo.getFila())
                filaDestino--;
            else if (protagonista.getFila() > enemigo.getFila())
                filaDestino++;

            // Movimiento horizontal
            if (protagonista.getColumna() < enemigo.getColumna())
                colDestino--;
            else if (protagonista.getColumna() > enemigo.getColumna())
                colDestino++;

        } else {
            System.out.println("[ENEMIGO] Movimiento aleatorio...");
            int[] df = { -1, 1, 0, 0 }; // Arriba, abajo, izquierda, derecha
            int[] dc = { 0, 0, -1, 1 };
            int indice = new Random().nextInt(4);
            filaDestino += df[indice];
            colDestino += dc[indice];
        }

        // Validar límites
        if (filaDestino >= 0 && filaDestino < filas
                && colDestino >= 0 && colDestino < columnas) {

            System.out.println("[ENEMIGO] Movimiento válido a (" + filaDestino + "," + colDestino + ")");
            intentarMovimiento(enemigo, filaDestino, colDestino);
        } else {
            System.out.println("[ENEMIGO] Movimiento inválido: Fuera del tablero");
        }

        // Asegurar que el turno del enemigo termine
        System.out.println("[ENEMIGO] Turno completado para: " + enemigo.getNombre());
    }

    // ================== OBSERVADORES ==================
    public void añadirObservador(ObservadorJuego observador) {
        observadores.add(observador);
    }

    private void notificarObservadores() {
        for (ObservadorJuego obs : observadores) {
            obs.alActualizarJuego();
        }
    }

    // ================== GETTERS & HELPERS ==================
    public Personaje obtenerPersonajeEn(int f, int c) {
        // Verificar protagonista
        if (protagonista != null
                && protagonista.getFila() == f
                && protagonista.getColumna() == c
                && protagonista.estaVivo()) {
            return protagonista;
        }

        // Verificar enemigos
        for (Enemigo enemigo : enemigos) {
            if (enemigo.getFila() == f
                    && enemigo.getColumna() == c
                    && enemigo.estaVivo()) {
                return enemigo;
            }
        }

        return null; // Celda vacía
    }

    public boolean esJuegoTerminado() {
        return protagonista == null || !protagonista.estaVivo();
    }

    public boolean esNivelCompleto() {
        return enemigos.stream().noneMatch(Enemigo::estaVivo);
    }

    /**
     * Carga el siguiente nivel cuando el jugador lo decide.
     * 
     * @return true si se cargó el siguiente nivel, false si no hay más niveles
     */
    public boolean cargarSiguienteNivel() {
        nivelActual++;
        try {
            cargarTablero("mapa" + nivelActual + ".txt");
            cargarEnemigos("enemigos" + nivelActual + ".txt");
            // Aumentar estadísticas del protagonista
            if (protagonista != null) {
                protagonista.setSalud(protagonista.getSalud() + 1);
                protagonista.setFuerza(protagonista.getFuerza() + 1);
                protagonista.setDefensa(protagonista.getDefensa() + 1);
                protagonista.setVelocidad(protagonista.getVelocidad() + 1);
                protagonista.setPercepcion(protagonista.getPercepcion() + 1);
                protagonista.restaurarSalud();
            }
            // Limpiar la lista de turnos antes de inicializarla
            ordenTurnos.clear();
            inicializarOrdenTurnos();
            System.out.println("[JUEGO] Nivel " + nivelActual + " cargado correctamente.");
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("[JUEGO] No se encontraron más niveles. ¡Has completado el juego!");
            nivelActual--; // Revertir el incremento si no hay más niveles
            return false;
        } catch (IOException e) {
            System.out.println("[JUEGO] Error al cargar el siguiente nivel: " + e.getMessage());
            nivelActual--; // Revertir el incremento si hay error
            return false;
        }
    }

    // ================== GETTERS & SETTERS ==================
    public Celda[][] getTablero() {
        return tablero;
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public Protagonista getProtagonista() {
        return protagonista;
    }

    public List<Enemigo> getEnemigos() {
        return enemigos;
    }

    public List<Personaje> getOrdenTurnos() {
        return ordenTurnos;
    }

    public Personaje getPersonajeEnTurno() {
        return ordenTurnos.isEmpty() ? null : ordenTurnos.get(indiceTurnoActual);
    }

    public void setProtagonista(Protagonista p) {
        this.protagonista = p;
    }

    public int getNivelActual() {
        return nivelActual;
    }
}
