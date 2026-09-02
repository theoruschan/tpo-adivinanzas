package com.tpo.adivinanzas.persistencia;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class MarcadorRepositorio {

    private final Path archivo;

    public MarcadorRepositorio(String rutaArchivo) {
        this.archivo = Paths.get(rutaArchivo);
    }

    /** suma una victoria al usuario; lo crea si todavia no jugo. */
    public void registrarVictoria(String nombreUsuario) {
        String nombreNormalizado = normalizarNombre(nombreUsuario);
        if (nombreNormalizado.isEmpty()) return;

        Map<String, Marcador> mapa = cargarTodos();
        mapa.computeIfAbsent(nombreNormalizado, n -> new Marcador(nombreUsuario.trim(), 0))
            .incrementarVictorias();
        guardarTodos(mapa);
    }

    /** victorias de un jugador, 0 si no esta en el archivo. */
    public int getVictorias(String nombreUsuario) {
        Marcador marcador = cargarTodos().get(normalizarNombre(nombreUsuario));
        return marcador == null ? 0 : marcador.getVictorias();
    }

    /** top n jugadores ordenados de mas a menos victorias. */
    public List<Marcador> topN(int n) {
        return cargarTodos().values().stream()
                .sorted()
                .limit(n)
                .collect(Collectors.toList());
    }

    /** arma un mapa nombre -> marcador leyendo lineas "nombre: victorias"; si el archivo
     * no existe todavia devuelve vacio, y las lineas corruptas se ignoran en vez de romper todo. */
    private Map<String, Marcador> cargarTodos() {
        Map<String, Marcador> mapa = new LinkedHashMap<>();
        if (!Files.exists(archivo)) return mapa;

        try (BufferedReader br = Files.newBufferedReader(archivo)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String[] partes = separarLinea(linea);
                if (partes.length == 2) {
                    try {
                        String nombre = partes[0].trim();
                        int victorias = Integer.parseInt(partes[1].trim());
                        if (nombre.isEmpty() || victorias < 0) continue;

                        String clave = normalizarNombre(nombre);
                        Marcador marcador = mapa.get(clave);
                        if (marcador == null) {
                            mapa.put(clave, new Marcador(nombre, victorias));
                        } else {
                            marcador.sumarVictorias(victorias);
                        }
                    } catch (NumberFormatException e) {
                        // fila malformada, se ignora
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo marcador: " + e.getMessage());
        }
        return mapa;
    }

    /** reescribe el archivo entero desde el mapa en memoria, una fila por usuario. */
    private void guardarTodos(Map<String, Marcador> mapa) {
        try (BufferedWriter bw = Files.newBufferedWriter(archivo)) {
            for (Marcador m : mapa.values()) {
                bw.write(m.getNombreUsuario() + ": " + m.getVictorias());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo marcador: " + e.getMessage());
        }
    }

    private String[] separarLinea(String linea) {
        if (linea.contains(":")) return linea.split(":", 2);
        return linea.split(",", 2);
    }

    private String normalizarNombre(String nombre) {
        return nombre == null ? "" : nombre.trim().toLowerCase(Locale.ROOT);
    }
}
