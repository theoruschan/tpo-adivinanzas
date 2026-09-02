package com.tpo.adivinanzas.modelo;

public class FiltroColorPelo extends Filtro {

    private final ColorPelo color;

    public FiltroColorPelo(ColorPelo color) { this.color = color; }

    public ColorPelo getColor() { return color; }

    @Override
    public boolean evaluar(Personaje p) { return p.getColorPelo() == color; }

    @Override
    public String getDescripcion() {
        return "¿Tiene el pelo " + color.name().toLowerCase() + "?";
    }
}
