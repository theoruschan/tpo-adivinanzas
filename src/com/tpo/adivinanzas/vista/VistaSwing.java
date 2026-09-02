package com.tpo.adivinanzas.vista;

import com.tpo.adivinanzas.modelo.EvaluacionFiltro;
import com.tpo.adivinanzas.modelo.Filtro;
import com.tpo.adivinanzas.modelo.Personaje;
import com.tpo.adivinanzas.persistencia.Marcador;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * vista swing de una sola pantalla. no hay ventanas emergentes tapando nada:
 * el log grande de la izquierda (que hace de "consola" del juego, con todo
 * el detalle que antes solo se veía por consola real, incluido el
 * razonamiento greedy) queda siempre visible y con scroll propio, el panel
 * de la derecha con el personaje propio y los candidatos que van quedando
 * también queda siempre a la vista, y todo lo que hay que responder se
 * contesta con los controles fijos de abajo, no con un dialogo modal.
 *
 * como el motor de juego (MotorDeJuego) corre bloqueando en un hilo aparte
 * (ver app/Main.java), cada "pedir algo" de acá arma el control de abajo y
 * se queda esperando en una cola hasta que el usuario aprieta el botón. el
 * que efectivamente pone la respuesta en la cola es el listener del botón,
 * que corre en la EDT.
 */
public class VistaSwing implements Vista {

    private final JFrame frame = new JFrame("juego de adivinanzas — TPO Prog 3");

    private final JTextArea log = new JTextArea();

    private final JTextArea panelPersonajePropio = new JTextArea("(todavía no elegiste personaje)");
    private final DefaultListModel<String> modeloCandidatos = new DefaultListModel<>();
    private final JList<String> listaCandidatos = new JList<>(modeloCandidatos);

    private final JLabel lblPregunta = new JLabel(" ");
    private final JComboBox<String> comboOpciones = new JComboBox<>();
    private final JTextField campoTexto = new JTextField();
    private final JButton botonConfirmar = new JButton("confirmar");

    /** acá se deja la respuesta del usuario para que el hilo del juego (que está esperando) la retire. */
    private final BlockingQueue<Object> respuestas = new ArrayBlockingQueue<>(1);

    public VistaSwing() {
        armarVentana();
    }

    // ---- armado de la ventana, todo en un único frame ----

    private void armarVentana() {
        // log: monoespaciado legible
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scrollLog = new JScrollPane(log);
        scrollLog.setBorder(BorderFactory.createTitledBorder("partida en curso"));

        // panel personaje propio: más legible
        panelPersonajePropio.setEditable(false);
        panelPersonajePropio.setLineWrap(true);
        panelPersonajePropio.setWrapStyleWord(true);
        panelPersonajePropio.setBackground(frame.getBackground());
        panelPersonajePropio.setBorder(BorderFactory.createTitledBorder("tu personaje secreto"));
        panelPersonajePropio.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        panelPersonajePropio.setMargin(new Insets(6,6,6,6));

        // lista de candidatos: usar fuente monoespaciada y render que permite wrap y tooltip
        listaCandidatos.setBorder(BorderFactory.createTitledBorder("tus candidatos restantes"));
        listaCandidatos.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        listaCandidatos.setVisibleRowCount(10);
        // renderer basado en HTML para permitir wrapping y alturas variables por elemento
        listaCandidatos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String text = value == null ? "" : value.toString().replace("\n", "<br>");
                String html = "<html><div style='width:300px; font-family:monospace;'>" + text + "</div></html>";
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, html, index, isSelected, cellHasFocus);
                lbl.setVerticalAlignment(SwingConstants.TOP);
                lbl.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
                lbl.setToolTipText(value == null ? null : value.toString());
                return lbl;
            }
        });
        JScrollPane scrollCandidatos = new JScrollPane(listaCandidatos);

        JPanel panelDerecho = new JPanel(new BorderLayout(0, 8));
        panelDerecho.setPreferredSize(new Dimension(360, 0));
        panelDerecho.add(panelPersonajePropio, BorderLayout.NORTH);
        panelDerecho.add(scrollCandidatos, BorderLayout.CENTER);

        // panel de acción: controles más grandes y accesibles
        JPanel panelAccion = new JPanel(new BorderLayout(8, 6));
        panelAccion.setBorder(BorderFactory.createTitledBorder("tu turno"));
        lblPregunta.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panelAccion.add(lblPregunta, BorderLayout.NORTH);
        JPanel campoCentral = new JPanel(new CardLayout());

        comboOpciones.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        comboOpciones.setToolTipText("Elige una opción con las flechas y confirma");
        campoTexto.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        campoTexto.setToolTipText("Escribe tu respuesta y presiona Enter o Confirmar");

        campoCentral.add(comboOpciones, "combo");
        campoCentral.add(campoTexto, "texto");
        panelAccion.add(campoCentral, BorderLayout.CENTER);

        // botón confirmar: tamaño mayor, tooltip y tecla rápida (Alt+C)
        botonConfirmar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        botonConfirmar.setPreferredSize(new Dimension(140, 40));
        botonConfirmar.setMnemonic('C');
        botonConfirmar.setToolTipText("Presionar para confirmar la acción (Alt+C)");
        botonConfirmar.getAccessibleContext().setAccessibleDescription("Botón para confirmar la elección o texto ingresado");

        panelAccion.add(botonConfirmar, BorderLayout.EAST);
        this.campoCentral = campoCentral;

        botonConfirmar.addActionListener(e -> confirmar());
        campoTexto.addActionListener(e -> confirmar());

        frame.setLayout(new BorderLayout(8, 8));
        // usar JSplitPane para permitir redimensionar la lista de candidatos vs log principal
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollLog, panelDerecho);
        split.setResizeWeight(0.7);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        frame.add(split, BorderLayout.CENTER);
        frame.add(panelAccion, BorderLayout.SOUTH);

        frame.setSize(1000, 680);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /** referencia al panel con CardLayout para poder alternar entre combo y texto libre. */
    private JPanel campoCentral;

    private enum ModoEntrada { COMBO, TEXTO, CONTINUAR }
    private ModoEntrada modoActual = ModoEntrada.CONTINUAR;

    private void confirmar() {
        Object respuesta = switch (modoActual) {
            case COMBO -> comboOpciones.getSelectedIndex();
            case TEXTO -> campoTexto.getText();
            case CONTINUAR -> Boolean.TRUE;
        };
        respuestas.offer(respuesta);
    }

    // ---- helpers de bajo nivel ----

    private void log(String texto) {
        SwingUtilities.invokeLater(() -> {
            log.append(texto + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }

    /** deja al hilo del juego esperando la próxima respuesta que ponga el listener del botón. */
    private Object tomarRespuesta() {
        try {
            return respuestas.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /** arma el control de abajo como un combo con opciones y espera a que confirmen. */
    private int elegir(String pregunta, String[] opciones, String textoBoton) {
        SwingUtilities.invokeLater(() -> {
            modoActual = ModoEntrada.COMBO;
            lblPregunta.setText(pregunta);
            comboOpciones.setModel(new DefaultComboBoxModel<>(opciones));
            comboOpciones.setSelectedIndex(0);
            ((CardLayout) campoCentral.getLayout()).show(campoCentral, "combo");
            botonConfirmar.setText(textoBoton);
        });
        return (int) tomarRespuesta();
    }

    /** arma el control de abajo como un campo de texto libre y espera a que confirmen. */
    private String pedirTexto(String pregunta, String textoBoton) {
        SwingUtilities.invokeLater(() -> {
            modoActual = ModoEntrada.TEXTO;
            lblPregunta.setText(pregunta);
            campoTexto.setText("");
            ((CardLayout) campoCentral.getLayout()).show(campoCentral, "texto");
            botonConfirmar.setText(textoBoton);
            campoTexto.requestFocusInWindow();
        });
        return (String) tomarRespuesta();
    }

    /** arma el control de abajo con un único botón "continuar", sin nada para elegir. */
    private void esperarContinuar(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            modoActual = ModoEntrada.CONTINUAR;
            lblPregunta.setText(mensaje);
            ((CardLayout) campoCentral.getLayout()).show(campoCentral, "texto");
            campoTexto.setText("");
            campoTexto.setEditable(false);
            botonConfirmar.setText("continuar");
        });
        tomarRespuesta();
        SwingUtilities.invokeLater(() -> campoTexto.setEditable(true));
    }

    /** actualiza el panel fijo de la derecha con los datos del personaje que eligió el usuario. */
    private void mostrarPersonajePropio(Personaje p) {
        SwingUtilities.invokeLater(() -> panelPersonajePropio.setText(
                p.getNombre() + "\n"
                        + "género: " + (p.getGenero().name().equals("MASCULINO") ? "hombre" : "mujer") + "\n"
                        + "calvo: " + (p.isCalvo() ? "sí" : "no") + "\n"
                        + "usa lentes: " + (p.isUsaLentes() ? "sí" : "no") + "\n"
                        + "color de pelo: " + (p.getColorPelo() == null ? "sin pelo" : p.getColorPelo().name().toLowerCase())));
    }

    // ---- Vista ----

    @Override
    public void mostrarBienvenida() {
        log("=== juego de adivinanzas — TPO Prog 3 ===");
    }

    @Override
    public void mostrarMenuPrincipal() {
        // el menú se arma directamente en leerLinea(), acá no hace falta nada.
    }

    @Override
    public String leerLinea() {
        String[] opciones = {
                "jugador vs máquina", "máquina vs máquina",
                "jugador vs dos máquinas", "ver ranking", "salir"
        };
        int idx = elegir("¿qué querés hacer?", opciones, "elegir");
        return String.valueOf(idx + 1);
    }

    @Override
    public void mostrarOpcionInvalida() {
        log("opción inválida.");
    }

    @Override
    public void mostrarDespedida() {
        log("¡hasta la próxima!");
    }

    @Override
    public void cerrar() {
        // no hay recursos que cerrar acá, a diferencia del scanner de la consola.
    }

    @Override
    public String pedirNombreUsuario() {
        String nombre = pedirTexto("ingresá tu nombre de usuario:", "confirmar");
        return (nombre == null || nombre.isBlank()) ? "jugador" : nombre.trim();
    }

    @Override
    public void pedirEnter(String mensaje) {
        esperarContinuar(mensaje);
    }

    @Override
    public int pedirPersonajeSecreto(String nombreJugador, List<Personaje> disponibles) {
        String[] opciones = disponibles.stream().map(Personaje::getNombre).toArray(String[]::new);
        int idx = elegir(nombreJugador + ", elegí tu personaje secreto:", opciones, "confirmar personaje");
        mostrarPersonajePropio(disponibles.get(idx));
        return idx;
    }

    @Override
    public void mostrarCatalogoCompleto(String nombreJugador, List<Personaje> todos) {
        log(nombreJugador + ", catálogo completo de personajes:");
        todos.forEach(p -> log("  " + p));
    }

    @Override
    public int pedirPersonajeSecretoDosMaquinas(List<Personaje> todos) {
        String[] opciones = todos.stream().map(Personaje::getNombre).toArray(String[]::new);
        int idx = elegir("elegí tu personaje secreto (no se puede cambiar):", opciones, "confirmar personaje");
        mostrarPersonajePropio(todos.get(idx));
        return idx;
    }

    @Override
    public void mostrarPersonajeElegido(String nombrePersonaje) {
        log("elegiste: " + nombrePersonaje + ". ¡comienza el juego!");
    }

    @Override
    public void mostrarInicioPartida(String nombreModo, List<String> nombresJugadores) {
        log("\n=== modo: " + nombreModo + " — jugadores: " + String.join(", ", nombresJugadores) + " ===");
        modeloCandidatos.clear();
    }

    @Override
    public void mostrarTurno(int numeroTurno, String nombreActivo, String nombreObjetivo) {
        log("--- turno " + numeroTurno + " | " + nombreActivo + " → " + nombreObjetivo + " ---");
    }

    @Override
    public void mostrarPregunta(String nombreActivo, String descripcionFiltro) {
        log(nombreActivo + " pregunta: " + descripcionFiltro);
    }

    @Override
    public void mostrarRespuesta(String nombreObjetivo, boolean respuesta) {
        log("respuesta de " + nombreObjetivo + ": " + (respuesta ? "sí" : "no"));
    }

    @Override
    public void mostrarCandidatosMaquina(String nombreActivo, int cantidad, List<String> nombresCandidatos) {
        // esto es "pensamiento" de la máquina, no del usuario, así que va al log grande igual que en consola.
        String detalle = nombresCandidatos == null ? "" : " (" + String.join(", ", nombresCandidatos) + ")";
        log(">>> candidatos restantes para " + nombreActivo + ": " + cantidad + detalle);
    }

    @Override
    public void mostrarRazonamientoGreedy(String nombreMaquina, List<EvaluacionFiltro> evaluaciones, Filtro elegido) {
        log(">>> " + nombreMaquina + " evalúa filtros (greedy, criterio minimax):");
        for (EvaluacionFiltro e : evaluaciones) {
            log(String.format("      %-28s cumplen: %2d | no cumplen: %2d | peor caso: %2d",
                    e.getFiltro().getDescripcion(), e.getCumplen(), e.getNoCumplen(), e.peorCaso()));
        }
        log(">>> " + nombreMaquina + " elige: \"" + elegido.getDescripcion() + "\" (minimiza el peor caso)");
    }

    @Override
    public void mostrarSuposicion(String nombreActivo, String nombrePersonaje) {
        log(nombreActivo + " supone: ¿es " + nombrePersonaje + "?");
    }

    @Override
    public void mostrarAcierto(String nombreObjetivo, String nombrePersonaje) {
        log("¡correcto! el personaje de " + nombreObjetivo + " era " + nombrePersonaje + ".");
    }

    @Override
    public void mostrarFallo(String nombreObjetivo, String nombrePersonaje) {
        log("incorrecto. el personaje de " + nombreObjetivo + " no es " + nombrePersonaje + ". sigue la partida.");
    }

    @Override
    public void mostrarFinPartida(String nombreGanador) {
        log("=== fin de partida — ganador: " + nombreGanador + " ===\n");
    }

    @Override
    public void mostrarLineaEnBlanco() {
        log("");
    }

    @Override
    public void mostrarCandidatosPropios(List<Personaje> candidatos) {
        log("tus candidatos restantes (" + candidatos.size() + "): "
                + candidatos.stream().map(Personaje::getNombre).collect(Collectors.joining(", ")));
        SwingUtilities.invokeLater(() -> {
            modeloCandidatos.clear();
            candidatos.forEach(p -> modeloCandidatos.addElement(p.toString()));
        });
    }

    @Override
    public int pedirAccionTurno() {
        String[] opciones = {
                "ver mi personaje secreto", "hacer una pregunta de filtro", "adivinar el personaje del rival"
        };
        return elegir("¿qué querés hacer?", opciones, "elegir");
    }

    @Override
    public void mostrarPersonajeSecreto(Personaje personaje) {
        // ya está siempre a la vista en el panel de la derecha, no hace falta tapar la pantalla con esto.
    }

    @Override
    public int pedirSeleccionFiltro(List<Filtro> filtros) {
        String[] opciones = filtros.stream().map(Filtro::getDescripcion).toArray(String[]::new);

        // muestra el combo y confirma solo al cambiar la selección, asi no hay que
        // apretar "continuar" aparte. se agrega un actionlistener temporal para eso.
        final int[] resultado = new int[1];
        final Object lock = new Object();
        final java.awt.event.ActionListener[] autoRef = new java.awt.event.ActionListener[1];

        SwingUtilities.invokeLater(() -> {
            modoActual = ModoEntrada.COMBO;
            lblPregunta.setText("elegí un filtro para preguntar:");
            comboOpciones.setModel(new DefaultComboBoxModel<>(opciones));
            comboOpciones.setSelectedIndex(0);
            ((CardLayout) campoCentral.getLayout()).show(campoCentral, "combo");
            botonConfirmar.setText("preguntar");

            // desactivar la confirmación automática inicial hasta que la UI esté lista
            final boolean[] enableAuto = {false};

            autoRef[0] = new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (!enableAuto[0]) return;
                    confirmar();
                }
            };

            comboOpciones.addActionListener(autoRef[0]);

            // habilita el auto-confirm recien despues de procesar el setSelectedIndex inicial
            SwingUtilities.invokeLater(() -> enableAuto[0] = true);
        });

        // esperar respuesta (la acción del combo llamará a confirmar())
        Object resp = tomarRespuesta();

        // limpiar listener y volver al estado normal en la EDT
        SwingUtilities.invokeLater(() -> {
            if (autoRef[0] != null) comboOpciones.removeActionListener(autoRef[0]);
            // restaurar vista de entrada a modo texto para mantener consistencia
            ((CardLayout) campoCentral.getLayout()).show(campoCentral, "texto");
            campoTexto.setText("");
        });

        return (int) resp;
    }

    @Override
    public int pedirSeleccionPersonajeSupuesto(List<Personaje> todos) {
        String[] opciones = todos.stream().map(Personaje::getNombre).toArray(String[]::new);
        return elegir("elegí el personaje que creés que tiene el rival:", opciones, "adivinar");
    }

    @Override
    public void mostrarResumenFinal(String nombreGanador, int victorias) {
        log(nombreGanador + " tiene ahora " + victorias + " victoria" + (victorias == 1 ? "" : "s") + " en total.");
    }

    @Override
    public void mostrarRanking(List<Marcador> top, int n) {
        log("--- ranking (top " + n + ") ---");
        if (top.isEmpty()) {
            log("  sin partidas registradas aún.");
        } else {
            for (int i = 0; i < top.size(); i++) {
                log("  " + (i + 1) + ". " + top.get(i));
            }
        }
    }

    @Override
    public void mostrarEncabezadoRonda1(String nombreUsuario) {
        log("\n=== ronda 1 — " + nombreUsuario + " vs máquina básica ===");
    }

    @Override
    public void mostrarEncabezadoRonda2(String nombreUsuario) {
        log("\n=== ronda 2 — " + nombreUsuario + " vs máquina avanzada ===");
    }

    @Override
    public void mostrarDerrotaRondaUno() {
        log("\nperdiste en la primera ronda. la segunda ronda no se va a jugar.");
    }

    @Override
    public void mostrarResumenDosRondas(String nombreUsuario, boolean usuarioGanoR1, boolean usuarioGanoR2) {
        log("\n=== resumen final — " + nombreUsuario + " vs 2 máquinas ===");
        log("  ronda 1: " + (usuarioGanoR1 ? "victoria" : "derrota"));
        log("  ronda 2: " + (usuarioGanoR2 ? "victoria" : "derrota"));
    }

    @Override
    public void mostrarTablaTop(List<Marcador> top) {
        log("--- top 3 ---");
        if (top.isEmpty()) {
            log("  sin registros aún.");
        } else {
            for (int i = 0; i < top.size(); i++) {
                log("  " + (i + 1) + ". " + top.get(i));
            }
        }
    }
}
