# TPO Adivinanzas — Programación 3

Juego de adivinanzas tipo **"¿Quién es quién?"** hecho en Java para el Trabajo Práctico Obligatorio de Programación 3. Cada participante elige en secreto uno de 23 personajes; en cada turno puede preguntar un filtro (género, calvicie, uso de lentes, color de pelo) para acotar sus candidatos, o arriesgar directamente el nombre del personaje del rival. Gana quien adivina primero — no hay límite de preguntas.

## Modos de juego

- **Jugador vs Máquina**: vos contra una `MaquinaBasica`, que pregunta filtros al azar.
- **Máquina vs Máquina**: `MaquinaBasica` contra `MaquinaAvanzada`, mostrando en consola el proceso de búsqueda de ambas.
- **Jugador vs Dos Máquinas**: dos rondas seguidas con el mismo personaje secreto — primero contra `MaquinaBasica`, y si la ganás, después contra `MaquinaAvanzada`. Como el personaje secreto no cambia entre rondas, la avanzada arranca la segunda ronda con ventaja real: no repite las preguntas que ya hizo la básica y además ya sabe las respuestas que obtuvo, así que empieza con menos candidatos posibles en vez de con los 23 personajes completos.

Al terminar una partida se registra la victoria en un marcador persistente (`marcador.txt`) y se puede consultar el ranking desde el menú principal.

## Arquitectura

El proyecto sigue **MVC**:

```
com.tpo.adivinanzas
├── modelo/          Personaje, Filtro (+ familia), Jugada (+ Suposicion/PreguntaFiltro), EvaluacionFiltro
├── catalogo/        CatalogoPersonajes — repositorio único de los 23 personajes
├── jugador/         Jugador (abstracta), JugadorUsuario, JugadorMaquina (abstracta), MaquinaBasica, MaquinaAvanzada
├── vista/           Vista (interfaz) + VistaConsola y VistaSwing — dos entradas/salidas intercambiables
├── controlador/     MotorDeJuego, Partida, EstadoPartida, HistorialPreguntas, ModoJuego (+ sus 3 implementaciones), ControladorPrincipal
├── persistencia/    Marcador, MarcadorRepositorio (lectura/escritura del archivo de texto de victorias)
└── app/             Main — punto de entrada, arma Vista + Controlador y les cede el control
```

`modelo`, `catalogo` y `jugador` son, en conjunto, el **Modelo**: tipos de datos, el catálogo de personajes y la jerarquía de jugadores con su lógica de decisión — ninguna de estas clases hace `System.out`/`Scanner` ni Swing directamente. `VistaConsola` y `VistaSwing` implementan la misma interfaz `Vista` y no conocen reglas del juego; el juego arranca con `VistaSwing` (`app/Main.java`) sin que el motor ni las máquinas necesiten saberlo. El paquete `controlador` orquesta el flujo de turnos, le pide datos a la Vista y aplica las reglas contra el Modelo.

## Algoritmos de la cátedra

`MaquinaAvanzada` combina las dos técnicas de cátedra, cada una en una capa distinta del mismo problema (elegir qué filtro preguntar). El detalle completo, con la comparación contra los ejemplos de cátedra y el análisis de complejidad, está en `documentacion/justificacion.pdf`.

**Greedy** define el criterio de qué filtro es "el mejor" (`MaquinaAvanzada.elegirJugada`, apoyado en `JugadorMaquina.particionar`). Para cada filtro disponible mide cuántos candidatos quedarían en el grupo "sí" y cuántos en el grupo "no". Como todavía no sabe cuál será la respuesta, mira el peor caso de cada filtro: el tamaño del grupo más grande (`EvaluacionFiltro.peorCaso()`). El filtro elegido es el de menor peor caso — criterio minimax. Es greedy porque toma la mejor decisión local del turno actual, sin mirar las próximas rondas. `MaquinaBasica`, en cambio, elige filtros al azar a propósito, para representar una estrategia menos acertiva.

**Divide and Conquer** resuelve cómo encontrar ese filtro de menor peor-caso entre los disponibles (`MaquinaAvanzada.elegirMejorFiltro` / `mejorPorDivideYVenceras`): divide la lista de filtros al medio, resuelve cada mitad por recursión y combina quedándose con la mitad que trae el menor peor-caso — el mismo esquema que el ejercicio de cátedra de "mejor candidato".

El filtrado de candidatos tras cada respuesta (`JugadorMaquina.filtrarCandidatos`) no usa ninguna de las dos técnicas: es un filtrado de una sola pasada (separa en cumple/no cumple y descarta el grupo que no corresponde a la respuesta real), sin recursión ni combinación de resultados.

## Cómo compilar y correr

```bash
# compilar
find src -name "*.java" > sources.txt
javac -d out @sources.txt

# correr
java -cp out com.tpo.adivinanzas.app.Main
```

También se puede abrir como proyecto de IntelliJ IDEA (raíz de fuentes en `src/`, sin Maven/Gradle) y correr `com.tpo.adivinanzas.app.Main`.
