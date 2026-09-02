package com.tpo.adivinanzas.jugador;

import com.tpo.adivinanzas.controlador.EstadoPartida;
import com.tpo.adivinanzas.modelo.EvaluacionFiltro;
import com.tpo.adivinanzas.modelo.Filtro;
import com.tpo.adivinanzas.modelo.FiltroFactory;
import com.tpo.adivinanzas.modelo.Jugada;
import com.tpo.adivinanzas.modelo.Personaje;
import com.tpo.adivinanzas.modelo.PreguntaFiltro;
import com.tpo.adivinanzas.modelo.Suposicion;

import java.util.ArrayList;
import java.util.List;

public class MaquinaAvanzada extends JugadorMaquina {

    /** filtros que ya pregunto esta maquina, para no repetirlos. */
    private final List<Filtro> filtrosUsados = new ArrayList<>();

    /** evaluacion greedy del ultimo turno, para mostrar el razonamiento en consola. */
    private List<EvaluacionFiltro> ultimaEvaluacion = new ArrayList<>();

    /** referencia opcional, solo se usa en el modo dos maquinas para no repetir sus preguntas. */
    private MaquinaBasica maquinaBasica;

    public MaquinaAvanzada(String nombre) { super(nombre); }

    public MaquinaBasica getMaquinaBasica() { return maquinaBasica; }
    public void setMaquinaBasica(MaquinaBasica mb) { this.maquinaBasica = mb; }

    /** si ya hay un solo candidato o no quedan filtros para preguntar, no hay nada que
     * decidir por greedy: se arriesga directamente. sino, delega en elegirMejorFiltro. */
    @Override
    public Jugada elegirJugada(EstadoPartida estado) {
        ultimaEvaluacion = new ArrayList<>();

        if (candidatos.size() == 1) {
            return new Suposicion(candidatos.get(0).getNombre());
        }

        List<Filtro> disponibles = filtrosDisponibles();
        if (disponibles.isEmpty()) {
            return new Suposicion(candidatos.get(0).getNombre());
        }

        Filtro mejorFiltro = elegirMejorFiltro(disponibles);
        filtrosUsados.add(mejorFiltro);
        return new PreguntaFiltro(mejorFiltro);
    }

    /** el criterio de que es "mejor" es greedy (minimax de este turno, sin mirar turnos
     * futuros); pero encontrar cual de los filtros disponibles cumple ese criterio se
     * resuelve con Divide and Conquer, al estilo del ejercicio de "mejor candidato":
     * dividir la lista al medio, resolver cada mitad por separado y combinar quedandose
     * con la de menor peor-caso. */
    private Filtro elegirMejorFiltro(List<Filtro> filtrosCandidatos) {
        EvaluacionFiltro mejor = mejorPorDivideYVenceras(filtrosCandidatos, 0, filtrosCandidatos.size() - 1);
        return mejor.getFiltro();
    }

    /** caso base: un solo filtro en el rango, es "el mejor" de si mismo. caso recursivo:
     * parte el rango al medio, resuelve cada mitad (divide + conquista) y combina
     * comparando cual de las dos trae el menor peorCaso(). */
    private EvaluacionFiltro mejorPorDivideYVenceras(List<Filtro> filtros, int lo, int hi) {
        if (lo == hi) {
            EvaluacionFiltro evaluacion = evaluar(filtros.get(lo));
            ultimaEvaluacion.add(evaluacion);
            return evaluacion;
        }

        int mid = (lo + hi) / 2;
        EvaluacionFiltro mejorIzquierda = mejorPorDivideYVenceras(filtros, lo, mid);
        EvaluacionFiltro mejorDerecha = mejorPorDivideYVenceras(filtros, mid + 1, hi);

        // combina: el criterio minimax (menor peor-caso) decide cual de las dos mitades gana.
        return mejorIzquierda.peorCaso() <= mejorDerecha.peorCaso() ? mejorIzquierda : mejorDerecha;
    }

    /** cuantos candidatos quedarian de cada lado (cumplen / no cumplen) si se preguntara este filtro. */
    private EvaluacionFiltro evaluar(Filtro filtro) {
        List<List<Personaje>> particion = particionar(candidatos, filtro);
        return new EvaluacionFiltro(filtro, particion.get(0).size(), particion.get(1).size());
    }

    /** filtros que todavia no pregunto esta maquina ni MaquinaBasica, si comparte partida con ella. */
    private List<Filtro> filtrosDisponibles() {
        List<String> yaUsadas = new ArrayList<>();
        filtrosUsados.forEach(f -> yaUsadas.add(f.getDescripcion()));
        if (maquinaBasica != null) {
            maquinaBasica.getFiltrosUsados().forEach(f -> yaUsadas.add(f.getDescripcion()));
        }

        List<Filtro> todos = FiltroFactory.todosFiltros();
        todos.removeIf(f -> yaUsadas.contains(f.getDescripcion()));
        return todos;
    }

    /** para que el controlador pueda mostrar el razonamiento greedy del ultimo turno. */
    public List<EvaluacionFiltro> getUltimaEvaluacion() {
        return new ArrayList<>(ultimaEvaluacion);
    }
}
