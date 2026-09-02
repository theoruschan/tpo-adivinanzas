package com.tpo.adivinanzas.catalogo;

import com.tpo.adivinanzas.modelo.ColorPelo;
import com.tpo.adivinanzas.modelo.Genero;
import com.tpo.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CatalogoPersonajes {

    /**instanciamos una lista para guardar a los personajes*/
    private static final CatalogoPersonajes INSTANCE = new CatalogoPersonajes();

    private final List<Personaje> porGenero = new ArrayList<>();
    private final List<Personaje> porId;

    private CatalogoPersonajes() {
        int id = 1;
        /** primero los generos femeninos y dps ponemos a los masculinos */
        id = add(id, "Ana",       Genero.FEMENINO,  false, false, ColorPelo.COLORADO);
        id = add(id, "Beatriz",   Genero.FEMENINO,  false, true,  ColorPelo.NEGRO);
        id = add(id, "Carmen",    Genero.FEMENINO,  false, false, ColorPelo.AMARILLO);
        id = add(id, "Daniela",   Genero.FEMENINO,  false, true,  ColorPelo.COLORADO);
        id = add(id, "Elena",     Genero.FEMENINO,  false, false, ColorPelo.NEGRO);
        id = add(id, "Florencia", Genero.FEMENINO,  false, true,  ColorPelo.AMARILLO);
        id = add(id, "Gabriela",  Genero.FEMENINO,  false, false, ColorPelo.COLORADO);
        id = add(id, "Helena",    Genero.FEMENINO,  false, true,  ColorPelo.NEGRO);
        id = add(id, "Irene",     Genero.FEMENINO,  true,  false, null);
        id = add(id, "Alejandro", Genero.MASCULINO, false, false, ColorPelo.NEGRO);
        id = add(id, "Bruno",     Genero.MASCULINO, true,  false, null);
        id = add(id, "Carlos",    Genero.MASCULINO, false, true,  ColorPelo.AMARILLO);
        id = add(id, "Diego",     Genero.MASCULINO, true,  true,  null);
        id = add(id, "Eduardo",   Genero.MASCULINO, false, false, ColorPelo.COLORADO);
        id = add(id, "Felipe",    Genero.MASCULINO, false, true,  ColorPelo.NEGRO);
        id = add(id, "Gustavo",   Genero.MASCULINO, true,  false, null);
        id = add(id, "Hernán",    Genero.MASCULINO, false, false, ColorPelo.AMARILLO);
        id = add(id, "Ignacio",   Genero.MASCULINO, false, true,  ColorPelo.COLORADO);
        id = add(id, "Javier",    Genero.MASCULINO, true,  true,  null);
        id = add(id, "Kevin",     Genero.MASCULINO, false, false, ColorPelo.NEGRO);
        id = add(id, "Lucas",     Genero.MASCULINO, false, true,  ColorPelo.AMARILLO);
        id = add(id, "Miguel",    Genero.MASCULINO, true,  false, null);
             add(id, "Nicolás",   Genero.MASCULINO, false, false, ColorPelo.COLORADO);

        porId = new ArrayList<>(porGenero);
        porId.sort(Comparator.comparingInt(Personaje::getId));
    }

    /**metodo que agrega un personaje a la lista y incrementa su id en 1*/
    private int add(int id, String nombre, Genero g, boolean calvo, boolean lentes, ColorPelo c) {
        porGenero.add(new Personaje(id, nombre, g, calvo, lentes, c));
        return id + 1;
    }

    public static CatalogoPersonajes getInstance() { return INSTANCE; }

    /** lista en orden de alta (por género): estado inicial del juego. */
    public List<Personaje> getPersonajesPorGenero() { return new ArrayList<>(porGenero); }

    /** lista ordenada por id autoincremental: vista interna de la máquina. */
    public List<Personaje> getPersonajesPorId() { return new ArrayList<>(porId); }

    public Personaje buscarPorNombre(String nombre) {
        return porGenero.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombre.trim()))
                .findFirst().orElse(null);
    }

    public int getCantidad() { return porGenero.size(); }
}
