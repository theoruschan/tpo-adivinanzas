package com.tpo.adivinanzas.modelo;

public class Personaje {

    private final int id;
    private final String nombre;
    private final Genero genero;
    private final boolean calvo;
    private final boolean usaLentes;
    private final ColorPelo colorPelo;

    public Personaje(int id, String nombre, Genero genero,
                     boolean calvo, boolean usaLentes, ColorPelo colorPelo) {
        this.id = id;
        this.nombre = nombre;
        this.genero = genero;
        this.calvo = calvo;
        this.usaLentes = usaLentes;
        this.colorPelo = colorPelo;
    }

    public int getId()           { return id; }
    public String getNombre()    { return nombre; }
    public Genero getGenero()    { return genero; }
    public boolean isCalvo()     { return calvo; }
    public boolean isUsaLentes() { return usaLentes; }
    public ColorPelo getColorPelo() { return colorPelo; }

    @Override
    public String toString() {
        return String.format("[%2d] %-10s | %s | %s | %s | %s",
                id, nombre,
                genero == Genero.MASCULINO ? "Hombre" : "Mujer",
                calvo      ? "Calvo"      : "Con pelo",
                usaLentes  ? "Con lentes" : "Sin lentes",
                colorPelo == null ? "sin pelo" : "pelo " + colorPelo.name().toLowerCase());
    }
}
