package com.tpo.adivinanzas.modelo;

/** cuantos candidatos cumplen y no cumplen un filtro; con esto compara el greedy de la avanzada. */
public class EvaluacionFiltro {

    private final Filtro filtro;
    private final int cumplen;
    private final int noCumplen;

    public EvaluacionFiltro(Filtro filtro, int cumplen, int noCumplen) {
        this.filtro = filtro;
        this.cumplen = cumplen;
        this.noCumplen = noCumplen;
    }

    public Filtro getFiltro()  { return filtro; }
    public int getCumplen()    { return cumplen; }
    public int getNoCumplen()  { return noCumplen; }

    /** el grupo mas grande de las dos particiones: el peor caso si se pregunta este filtro. */
    public int peorCaso() { return Math.max(cumplen, noCumplen); }
}
