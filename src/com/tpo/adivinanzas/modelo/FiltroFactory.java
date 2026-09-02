package com.tpo.adivinanzas.modelo;

import java.util.ArrayList;
import java.util.List;

public class FiltroFactory {


    private FiltroFactory() {}

    /** inicializacion de todos los filtros */
    public static List<Filtro> todosFiltros() {
        List<Filtro> filtros = new ArrayList<>();
        filtros.add(new FiltroGenero(Genero.MASCULINO));
        filtros.add(new FiltroGenero(Genero.FEMENINO));
        filtros.add(new FiltroCalvo(true));
        filtros.add(new FiltroCalvo(false));
        filtros.add(new FiltroUsaLentes(true));
        filtros.add(new FiltroUsaLentes(false));
        filtros.add(new FiltroColorPelo(ColorPelo.COLORADO));
        filtros.add(new FiltroColorPelo(ColorPelo.NEGRO));
        filtros.add(new FiltroColorPelo(ColorPelo.AMARILLO));
        return filtros;
    }
}
