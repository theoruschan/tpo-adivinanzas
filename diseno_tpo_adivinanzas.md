# TPO Programación 3 — Juego de Adivinanzas (tipo "¿Quién es quién?")
### Documento de diseño y arquitectura general

## 1. Resumen del problema

El juego enfrenta a un jugador humano contra la máquina, con tres modos posibles: humano contra una máquina, máquina contra máquina (con visibilidad del proceso de búsqueda de cada una), y humano contra dos máquinas en rondas sucesivas con distinto nivel de acierto. Cada participante elige en secreto uno de 23 personajes, y en cada turno puede lanzar una pregunta de filtro para acotar el conjunto de sospechosos o arriesgar directamente el nombre del personaje elegido por el rival. Gana quien adivina primero; no hay límite de cantidad de preguntas, el único límite es que alguien acierte.

Este documento cubre la estructura general (paquetes, clases, responsabilidades y flujos), incluyendo dónde se aplican Divide and Conquer y Greedy dentro de la lógica de las máquinas.

## 2. Reglas del enunciado mapeadas a decisiones de diseño

Hay 23 personajes, iguales para ambos jugadores, con un conjunto fijo de características distinguibles. Esto se modela como un catálogo único compartido (`CatalogoPersonajes`) del que ambos jugadores parten, en vez de listas independientes por jugador.

Los personajes arrancan ordenados solo por género, y es la máquina quien los redispone en una lista ordenada de forma autoincremental a medida que se agregan. Esto implica dos representaciones: el orden de alta/inicialización (por género) y un identificador autoincremental (`id`) que la máquina asigna y usa internamente para su propio ordenamiento e indexación.

El jugador elige un personaje y la máquina no puede acceder directamente a esa variable. Esto se resuelve encapsulando el personaje secreto dentro de cada jugador (campo privado, sin getter público del valor crudo) y exponiendo únicamente un método de consulta indirecta, del estilo `responder(Filtro filtro): boolean`, que evalúa el filtro contra el personaje secreto sin revelarlo directamente. La máquina nunca lee el atributo; solo recibe respuestas booleanas a las preguntas que ella misma formula.

En cada turno el jugador activo puede hacer una de dos cosas: lanzar una suposición directa sobre el personaje del rival, o aplicar un filtro/pregunta como pista para achicar el conjunto de candidatos propios. Ambas acciones se modelan como dos tipos de jugada distintos dentro de un mismo turno.

Debe haber dos máquinas con comportamiento distinto: una menos acertiva (preguntas al azar) y otra que además parte con la ventaja de conocer las preguntas ya realizadas por la primera. Esto se resuelve con una jerarquía de jugadores máquina con distinta estrategia. En el modo de dos rondas, `MaquinaAvanzada` recibe una referencia a `MaquinaBasica` para consultar qué filtros ya usó y no repetirlos, y además reaplica sobre sus propios candidatos las respuestas reales que `MaquinaBasica` obtuvo del usuario en la R1 (mismo personaje secreto en ambas rondas), arrancando la R2 con un conjunto de candidatos ya reducido en vez de con el catálogo completo.

El jugador no puede cambiar de personaje una vez iniciada la partida, y cada máquina debe elegir un personaje distinto al de las demás. Ambas reglas se validan al momento de asignar personajes secretos al arrancar la partida, antes de que empiece el intercambio de turnos.

Se debe persistir un marcador histórico con la cantidad de partidas ganadas por nombre de usuario (top 1, top 2, top 3...). Se resuelve con un archivo de texto y una clase de acceso a datos dedicada, separada del resto de la lógica de juego.

Sobre la cantidad de preguntas: el enunciado dice en un punto que es "infinita" y en otro que es "finita", pero la aclaración de este último punto es "uno puede hablar hasta que el otro no adivine a quien seleccionó". Ambas frases describen lo mismo: no hay un tope numérico de preguntas, la partida simplemente termina cuando alguien acierta el personaje del rival. No hay contradicción real, es una cuestión de redacción, pero conviene aclararlo explícitamente en el informe final para que no se preste a confusión.

Los filtros aplicables son: género, calvicie, uso de lentes, y color de pelo con tres valores posibles (colorado, negro, amarillo). Son 4 características en total (una de ellas con 3 valores en vez de 2).

## 3. Modelo de dominio

### 3.1 Personaje

Representa a cada uno de los 23 personajes del catálogo. Atributos: `id` (autoincremental, asignado por la máquina/catálogo), `nombre`, `genero` (enum, por ejemplo `MASCULINO`/`FEMENINO`), `calvo` (boolean), `usaLentes` (boolean), `colorPelo` (enum `COLORADO`/`NEGRO`/`AMARILLO`, o `null` si el personaje es calvo). Un calvo no tiene color de pelo por definición, así que se modela como ausencia de valor en vez de asignarle uno arbitrario; el filtro de color (`FiltroColorPelo`) ya responde "no" de forma natural para cualquier personaje con `colorPelo == null`, sin necesitar un caso especial.

### 3.2 CatalogoPersonajes

Contiene los 23 personajes. Ofrece dos vistas: una lista ordenada por género (estado inicial, tal como llegan los personajes) y una lista ordenada por `id` autoincremental (la que arma y usa la máquina). Es una única instancia compartida entre todos los jugadores de una partida; ningún jugador la modifica, solo la consulta para reducir su propio conjunto de candidatos.

### 3.3 Filtro (pregunta)

Representa una pregunta de tipo pista, por ejemplo "¿es hombre?", "¿es calvo?", "¿tiene el pelo negro?". Se modela como una clase (o familia de clases) con un método `evaluar(Personaje p): boolean`. Cada filtro queda asociado al atributo que consulta, de forma que se puede razonar sobre "qué filtros ya se probaron" y "qué filtros quedan por probar". Esa información se usa en la estrategia greedy de `MaquinaAvanzada`.

### 3.4 Jugada

Representa la acción de un turno: o bien una `Suposicion` (arriesga el nombre completo del personaje rival) o bien una `PreguntaFiltro` (aplica un `Filtro` y espera respuesta sí/no). Modelarlo como jerarquía (o como un tipo con un `enum TipoJugada`) permite que el motor de turnos trate ambas de forma uniforme.

### 3.5 Jugador (clase abstracta o interfaz)

Responsabilidades comunes: mantener el personaje secreto propio en forma encapsulada, exponer `responder(Filtro filtro): boolean` para contestar preguntas del rival sin exponer el personaje, y exponer `elegirJugada(EstadoPartida estado): Jugada` para decidir qué hacer en su turno.

**JugadorUsuario**: `elegirJugada` se resuelve desde el `MotorDeJuego`, que le pide la entrada a `VistaConsola`. La clase del usuario no lee consola directamente.

**JugadorMaquina** (abstracta): agrega el conjunto de candidatos propios (subconjunto del catálogo que todavía no fue descartado en base a las respuestas recibidas). `elegirJugada` es donde cada máquina concreta decide su estrategia.

- **MaquinaBasica**: estrategia más simple (por ejemplo, elige filtros sin optimizar la reducción esperada del conjunto). Representa a la máquina "menos acertiva".
- **MaquinaAvanzada**: usa greedy para elegir el mejor filtro disponible. En el modo de dos rondas, también puede consultar los filtros ya usados por `MaquinaBasica` para no repetir preguntas, y hereda las respuestas reales de esos filtros (vía `HistorialPreguntas` de la R1) para arrancar la R2 con sus candidatos ya reducidos.

### 3.6 HistorialPreguntas

Registro cronológico de las jugadas hechas en la partida (quién preguntó, qué filtro, qué respuesta obtuvo). Vive en el estado de la partida y se usa para mostrar y conservar el recorrido de preguntas/respuestas.

### 3.7 Partida / MotorDeJuego

Orquesta la partida: asigna personajes secretos respetando las restricciones (nadie repite personaje, el humano no puede cambiar el suyo), define el orden de turnos según el modo elegido, aplica cada `Jugada` (valida la respuesta, actualiza el historial, chequea si hay ganador) y determina el fin de la partida cuando alguien acierta la `Suposicion` correcta.

### 3.8 ModoJuego

Encapsula las diferencias entre los tres modos (quiénes participan, en qué orden juegan, qué información comparte cada máquina). Se puede modelar como una estrategia (`ModoJuego` interfaz) que el `MotorDeJuego` recibe al iniciar la partida, con tres implementaciones: `JugadorVsMaquina`, `MaquinaVsMaquina`, `JugadorVsDosMaquinas`. En `MaquinaVsMaquina`, además, el modo debe ir emitiendo/logueando cada paso del proceso de búsqueda de ambas máquinas para que se pueda presenciar en consola.

### 3.9 Marcador y persistencia

`Marcador`: registro de un nombre de usuario y su cantidad de partidas ganadas. `MarcadorRepositorio`: encapsula la lectura y escritura del archivo de texto `marcador.txt` (una fila por usuario, con formato `nombre: victorias`), y expone una consulta de ranking (top N) ordenando por partidas ganadas. Esta clase es la única que conoce el formato del archivo; el resto del sistema solo le pide "sumar una victoria a este usuario" o "dame el top 3".

## 4. Estructura de paquetes propuesta

```
com.tpo.adivinanzas
├── modelo/          Personaje, Genero, ColorPelo, Filtro, Jugada, Suposicion, PreguntaFiltro
├── catalogo/         CatalogoPersonajes
├── jugador/          Jugador, JugadorUsuario, JugadorMaquina, MaquinaBasica, MaquinaAvanzada
├── controlador/      Partida, MotorDeJuego, EstadoPartida, HistorialPreguntas, ModoJuego,
│                     JugadorVsMaquina, MaquinaVsMaquina, JugadorVsDosMaquinas, ControladorPrincipal
├── persistencia/     Marcador, MarcadorRepositorio
└── app/              Main (punto de entrada, menú de modos por consola)
```

## 5. Flujo de una partida (visión general, independiente del modo)

Al iniciar, se carga el `CatalogoPersonajes` con los 23 personajes ordenados por género, y la máquina construye a partir de ahí su lista ordenada por `id` autoincremental. Después se asignan los personajes secretos: el humano elige el suyo (si participa), cada máquina elige el suyo propio, y se valida que no haya dos participantes con el mismo personaje. A partir de ahí el `MotorDeJuego` cede el turno según el `ModoJuego` activo. En cada turno, el jugador activo produce una `Jugada`: si es `PreguntaFiltro`, se la dirige al rival correspondiente, que responde con `responder(Filtro)` sin exponer su personaje, y quien preguntó actualiza su propio conjunto de candidatos descartando los que no cumplen la respuesta obtenida; si es `Suposicion`, se compara directamente contra el personaje secreto del rival y, si coincide, termina la partida con ese jugador como ganador. Al terminar, se actualiza el `MarcadorRepositorio` sumando una victoria al ganador, sea usuario o máquina.

## 6. Aplicación de Greedy y Divide and Conquer

El enunciado de la cátedra no exige usar las dos técnicas a la vez; alcanza con aplicar la que realmente resuelva un problema del dominio. Este proyecto terminó usando **las dos**, cada una en una capa distinta del mismo problema (elegir qué filtro preguntar) dentro de `MaquinaAvanzada`: Greedy define el criterio de qué es "el mejor" filtro, y Divide and Conquer resuelve el mecanismo para encontrarlo entre los disponibles. El detalle completo de esta decisión, con la comparación contra los ejemplos de cátedra, la complejidad y los patrones de diseño reutilizados, está en `documentacion/justificacion.pdf`.

**Greedy** define el criterio, en `MaquinaAvanzada.elegirJugada(...)`. Antes de elegir una pregunta, la máquina avanzada prueba mentalmente cada filtro disponible sobre sus candidatos actuales (`particionar`). Para cada filtro calcula dos números: cuántos candidatos quedarían si la respuesta fuera "sí" y cuántos quedarían si fuera "no". Como todavía no sabe qué responderá el rival, usa un criterio de peor caso: para cada filtro mira el grupo más grande que podría quedarle (`EvaluacionFiltro.peorCaso()`). El filtro elegido es el que tenga el peor caso más chico — minimax. Es greedy porque decide solo con la información del turno actual, sin mirar turnos futuros ni garantizar la menor cantidad total de preguntas de toda la partida.

**Divide and Conquer** resuelve el mecanismo, en `MaquinaAvanzada.elegirMejorFiltro(...)` / `mejorPorDivideYVenceras(...)`. Encontrar cuál de los filtros disponibles tiene el menor peor-caso se resuelve dividiendo la lista de filtros al medio, resolviendo cada mitad por separado (recursión) y combinando: se queda con el ganador de la mitad que tenga el peor-caso más chico. Es el mismo esquema que el ejercicio de cátedra de "mejor candidato" (encontrar el máximo/mínimo de un array dividiéndolo al medio), aplicado acá a la lista de filtros en vez de a la lista de candidatos.

`MaquinaBasica` no usa ninguna de las dos técnicas: elige un filtro al azar entre los no usados. Esa diferencia permite comparar una estrategia simple contra una estrategia optimizada.

**Dónde no aparece Divide and Conquer**: se evaluó aplicarlo también a la reducción de la lista de candidatos (`JugadorMaquina.filtrarCandidatos(...)`, antes llamado `reducirCandidatosDyC`). Se descartó esa etiqueta porque el método no recursiona ni combina dos subproblemas resueltos de forma independiente: solo separa la lista en dos grupos (cumplen/no cumplen) y descarta uno en base a una respuesta que ya se conoce, lo cual es un filtrado de una sola pasada, no D&C. Además ese método es compartido por `MaquinaBasica` y `MaquinaAvanzada` por igual, así que no tiene relación con lo que hace "avanzada" a la máquina avanzada. El D&C real de este TPO está en `mejorPorDivideYVenceras`, no acá.

## 7. Preguntas abiertas para la próxima iteración

El criterio de "menos acertiva" ya quedó implementado en `MaquinaBasica`: pregunta filtros al azar entre los no usados. El marcador registra al ganador de cada partida, sea usuario o máquina.
