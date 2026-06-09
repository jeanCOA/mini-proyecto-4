#  Yu-Gi-Oh! Duelo — MP4

> Juego de cartas por turnos inspirado en Yu-Gi-Oh!, desarrollado en Java con arquitectura MVC, patrones de diseño avanzados, estructuras de datos justificadas, persistencia en texto plano y carga dinámica de cartas mediante Java Reflection API.

---

## Descripción

Yu-Gi-Oh! Duelo es un juego de cartas por turnos en el que dos jugadores se enfrentan con mazos de 25 cartas cada uno. Los jugadores pueden invocar monstruos al campo, activar cartas mágicas para obtener ventajas tácticas, colocar trampas para sorprender al rival y combatir monstruo contra monstruo. La partida termina cuando los Puntos de Vida (LP) de un jugador llegan a 0 o cuando se queda sin cartas en el mazo.

**El proyecto (MP4) extiende una base funcional (MP3)** añadiendo:

-  **Estructuras de datos** cuidadosamente elegidas y justificadas técnicamente
-  **Cinco patrones de diseño** (Observer, Command, Memento, Singleton, Factory)
-  **Persistencia en texto plano** — guardar y cargar partidas en cualquier momento
-  **Java Reflection API** — carga dinámica de cartas sin recompilar el proyecto
-  **Doble interfaz** — modo GUI (Swing) y modo consola interactiva

---

##  Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java SE | 11+ | Lenguaje principal |
| Java Swing | JDK | Interfaz gráfica (GUI) |
| Java Reflection API | JDK | Carga dinámica de cartas 
| Java I/O (BufferedReader/PrintWriter) | JDK | Persistencia en texto plano |
| ArrayDeque | JDK | Mazo (pila) e historial de comandos |
| LinkedList | JDK | Mano del jugador |
| TreeMap / LinkedHashSet | JDK | Cementerio indexado |
| HashMap | JDK | Índice de cartas por nombre |

**No hay dependencias externas** — el proyecto compila y ejecuta únicamente con el JDK estándar.

---

##  Arquitectura General

El proyecto sigue el patrón **MVC (Model-View-Controller)** con una separación clara en capas:

```
┌─────────────────────────────────────────────────────┐
│                     App.java                        │
│         (punto de entrada)                          │
└──────────────┬──────────────────────────────────────┘
               │
    ┌──────────▼──────────┐
    │    CONTROLLER        │
    │  DuelController      │◄── ObservadorDuelo (Observer)
    │  InicioController    │
    └──────────┬──────────┘
               │ usa
    ┌──────────▼──────────┐      ┌─────────────────────┐
    │       MODEL          │      │       VIEW           │
    │  CampoBatalla        │      │  VentanaDuelo (GUI)  │
    │  Jugador             │      │  ConsolaDuelo (TUI)  │
    │  Carta / subclases   │      │  VentanaInicio       │
    │  FabricaDeCartas     │      │  IDuelView (interfaz)│
    │  Mazo, Mano          │      └─────────────────────┘
    │  command/            │
    │  memento/            │
    └──────────────────────┘
               │
    ┌──────────▼──────────┐
    │    PERSISTENCE       │
    │  GestorPersistencia  │  (Singleton)
    │  CargadorCartas      │  (Reflection)
    └──────────────────────┘
```

- **Model**: toda la lógica del juego reside en `src/model/`. No tiene ninguna dependencia hacia la vista.
- **View**: `VentanaDuelo` (Swing) y `ConsolaDuelo` (consola) implementan la misma interfaz `IDuelView`, lo que hace que el controlador sea completamente agnóstico a la interfaz usada.
- **Controller**: `DuelController` orquesta el juego. Implementa `ObservadorDuelo` para recibir eventos del modelo y delega las acciones del usuario al modelo usando el patrón **Command**.
- **Persistence**: capa separada para guardar/cargar partidas y registrar resultados.

---

##  Patrones de Diseño Utilizados

### 1. Observer

**Nombre del patrón:** Observer (Observador)

**Justificación técnica y de negocio:**
El campo de batalla necesita notificar cambios (inicio de turno, combate, ganador, sin mazo) sin acoplarse directamente a la vista ni al controlador. Si `CampoBatalla` llamara directamente a métodos de `DuelController` o `VentanaDuelo`, el modelo dependería de la UI, rompiendo el principio de inversión de dependencias. Con Observer, el modelo solo conoce la interfaz `ObservadorDuelo` y puede notificar a cualquier cosa que la implemente.

**Archivos involucrados:**
- `controller/ObservadorDuelo.java` — interfaz del observador
- `controller/SujetoDuelo.java` — interfaz del sujeto
- `model/CampoBatalla.java` — implementa `SujetoDuelo`, dispara eventos
- `controller/DuelController.java` — implementa `ObservadorDuelo`, reacciona a eventos

**Cómo funciona en este proyecto:**
`CampoBatalla` mantiene una `List<ObservadorDuelo>`. Cuando ocurre un evento significativo (inicio de duelo, combate, fin de turno, ganador declarado), llama a `notificarObservadores(tipoEvento, detalle)`. `DuelController` se registra como observador en su constructor con `campo.registrarObservador(this)`. Al recibir el evento `"GANADOR"`, el controlador llama a `vista.mostrarGanador()`.

**Beneficios obtenidos:**
- El modelo no importa ninguna clase de la vista ni del controlador
- Se pueden agregar nuevos observadores (por ejemplo, un logger de red) sin modificar `CampoBatalla`
- Separación limpia entre lógica de negocio y reacción a eventos

---

### 2.  Command

**Nombre del patrón:** Command (Comando)

**Justificación técnica y de negocio:**
El juego necesita la funcionalidad de **deshacer la última jugada** dentro del mismo turno (botón "Deshacer Jugada"). Sin el patrón Command, implementar undo requeriría lógica ad-hoc dispersa en múltiples clases. Con Command, cada acción del jugador es un objeto independiente que sabe cómo ejecutarse (`ejecutar()`) y cómo revertirse (`deshacer()`), guardando el estado previo necesario para la reversión.

**Archivos involucrados:**
- `model/command/IComando.java` — interfaz con `ejecutar()`, `deshacer()`, `getDescripcion()`
- `model/command/ComandoJugarCarta.java` — encapsula jugar una carta desde la mano
- `model/command/ComandoAtacar.java` — encapsula un ataque (directo o contra monstruo)
- `model/command/HistorialComandos.java` — pila (`ArrayDeque`) de comandos ejecutados
- `controller/DuelController.java` — crea y ejecuta los comandos

**Cómo funciona en este proyecto:**
Cuando el jugador decide atacar, `DuelController.accionAtacar()` crea un `ComandoAtacar` pasando el campo, el atacante, el defensor y los jugadores. Antes de ejecutarlo, guarda los LP actuales y si los monstruos existen en el campo. Llama a `historial.ejecutar(cmd)`, que llama a `cmd.ejecutar()` y apila el comando. Si el jugador pulsa "Deshacer", `historial.deshacer()` extrae el último comando y llama a `cmd.deshacer()`, restaurando LP y monstruos. Al terminar el turno, `historial.limpiar()` vacía la pila — no se puede deshacer entre turnos.

**Beneficios obtenidos:**
- Undo/redo sin lógica duplicada
- Cada comando es una unidad testeable de forma aislada
- El historial puede extenderse para replay o auditoría

---

### 3.  Memento

**Nombre del patrón:** Memento

**Justificación técnica y de negocio:**
Guardar y cargar partidas requiere capturar el estado completo del duelo en un momento dado y poder restaurarlo después sin exponer los atributos privados de `Jugador` ni `CampoBatalla`. El patrón Memento provee exactamente eso: el `MementoManager` (caretaker) crea un snapshot (`EstadoDuelo`) sin que ningún código externo necesite conocer los detalles internos del estado.

**Archivos involucrados:**
- `model/memento/EstadoDuelo.java` — snapshot completo e inmutable del duelo
- `model/memento/EstadoJugador.java` — snapshot de un jugador (LP, mano, campo, trampas, mazo)
- `model/memento/MementoManager.java` — captura y restaura el estado
- `controller/DuelController.java` — llama a `mm.capturar(campo)` al guardar
- `controller/InicioController.java` — llama a `mm.restaurar(snapshot, campo)` al cargar
- `utils/GestorPersistencia.java` — serializa/deserializa el snapshot a/desde disco

**Cómo funciona en este proyecto:**
`MementoManager.capturar(campo)` itera sobre los dos jugadores, serializa cada zona (mano, campo, trampas, mazo) como listas de strings y construye un `EstadoDuelo` inmutable (todos sus campos son `final`). Para el campo de los monstruos, serializa los atributos relevantes en formato `nombre|nivel|atk|def|puedeAtacar|enDefensa`. Al restaurar, `MementoManager.restaurar()` reconstruye las cartas por nombre consultando `FabricaDeCartas` y repone todas las zonas del jugador.

**Beneficios obtenidos:**
- Guardado y carga completa de partidas sin romper encapsulamiento
- Los snapshots son inmutables: no hay riesgo de que una carga modifique el historial
- El formato en texto plano hace que los archivos de guardado sean legibles y editables manualmente

---

### 4.  Factory

**Nombre del patrón:** Factory Method / Static Factory

**Justificación técnica y de negocio:**
El mazo tiene 50 cartas de tipos y subtipos distintos (6 monstruos base × 6 tipos + 10 mágicas + 10 trampas). Sin una fábrica centralizada, la creación del mazo estaría dispersa en múltiples lugares (o se repetiría), haciendo difícil modificar la composición del mazo o agregar nuevas cartas. `FabricaDeCartas` actúa como única fuente de verdad para la composición del mazo.

**Archivos involucrados:**
- `model/FabricaDeCartas.java` — métodos estáticos `crearMazoCompleto()`, `crearMonstruos()`, `crearMagicas()`, `crearTrampas()`
- `model/CampoBatalla.java` — usa `FabricaDeCartas.crearMazoCompleto()` al iniciar el duelo
- `model/memento/MementoManager.java` — usa los métodos de la fábrica para reconstruir cartas al restaurar

**Cómo funciona en este proyecto:**
`FabricaDeCartas.crearMazoCompleto()` delega en tres submétodos que instancian cada tipo de carta. Los monstruos se crean con sus stats (`byte nivelCarta`, `short atk`, `short def`) en bucles para las copias múltiples. Las mágicas y trampas son instancias únicas de sus respectivas clases concretas. `CampoBatalla.repartirCartasIniciales()` llama a la fábrica, mezcla el mazo resultante y reparte 5 cartas a cada jugador.

**Beneficios obtenidos:**
- Un único lugar para cambiar la composición del mazo
- `MementoManager` puede reconstruir cartas por nombre sin tener lógica de instanciación propia
- Facilita agregar nuevos tipos de carta sin modificar `CampoBatalla`

---

### 5.  Singleton

**Nombre del patrón:** Singleton

**Justificación técnica y de negocio:**
Toda la aplicación (GUI y consola) debe pasar por un único punto de acceso a disco para guardar partidas, cargar partidas y registrar resultados. Si cada componente creara su propio `GestorPersistencia`, podrían producirse escrituras concurrentes o inconsistencias en `resultados.txt`. El Singleton garantiza que hay exactamente una instancia en toda la JVM.

**Archivos involucrados:**
- `utils/GestorPersistencia.java` — implementa el Singleton con `getInstance()`

**Cómo funciona en este proyecto:**
Constructor privado previene la instanciación directa. `getInstance()` crea la instancia solo si `instancia == null` (lazy initialization). La instancia crea la carpeta `guardados/` al inicializarse. Cualquier clase del sistema (controladores, `App`) accede siempre vía `GestorPersistencia.getInstance()`.

**Beneficios obtenidos:**
- Punto único de acceso a todos los archivos del juego
- La carpeta `guardados/` se crea automáticamente la primera vez
- Sin estado duplicado ni riesgo de escrituras concurrentes

---

##  Estructuras de Datos Utilizadas

### 1. `ArrayDeque` como Pila — Mazo del jugador

**Por qué se eligió:** El mazo de cartas es naturalmente una **pila LIFO**: siempre se roba la carta de arriba. `ArrayDeque.pop()` es O(1), mientras que `ArrayList.remove(0)` es O(n) porque desplaza todos los elementos.

**Archivos:** `model/Mazo.java`, `model/command/HistorialComandos.java`

**Implementación:** `ArrayDeque<Carta> cartas`. `robar()` → `cartas.pop()`. `barajar()` convierte el deque a `ArrayList`, aplica `Collections.shuffle()` y reconstruye el deque. La misma estructura se usa en `HistorialComandos` para la pila de comandos deshacer.

---

### 2. `LinkedList` — Mano del jugador

**Por qué se eligió:** Robar una carta la agrega al **frente** de la mano con `addFirst()`, que es O(1) en `LinkedList` frente a O(n) en `ArrayList` (que desplaza todos los elementos). El resto del código accede a la mano mediante la interfaz `List<Carta>`, por lo que el cambio de implementación es transparente.

**Archivos:** `model/Jugador.java`

**Implementación:** `LinkedList<Carta> mano`. `getMano()` expone `List<Carta>` (no `LinkedList`) para respetar el encapsulamiento. `robarCarta()` llama a `mano.addFirst(c)`.

---

### 3. `Queue` con `ArrayDeque` — Log de eventos del turno

**Por qué se eligió:** El log de eventos del duelo es una **cola FIFO**: los eventos se procesan en el orden exacto en que ocurren. `ArrayDeque` implementa `Queue` con `offer()` y `poll()` en O(1).

**Archivos:** `model/LogEventos.java`

**Implementación:** `Queue<String> eventos = new ArrayDeque<>()`. `registrar(mensaje)` → `eventos.offer(mensaje)`. `siguiente()` → `eventos.poll()`. `limpiar()` borra la cola al cambiar de turno.

---

### 4. `LinkedHashSet` — Cementerio de cartas

**Por qué se eligió:** El cementerio necesita dos propiedades: **no duplicados** (una carta destruida no se registra dos veces) y **orden de inserción** para que el historial de destrucciones sea legible. `LinkedHashSet` ofrece ambas con búsqueda O(1).

**Archivos:** `model/CementerioSet.java`

**Implementación:** `Set<String> nombresDestruidos = new LinkedHashSet<>()`. `agregar(carta)` añade el nombre; la propiedad Set garantiza unicidad automáticamente. `yaDestruida(nombre)` → O(1) con `contains()`.

---

### 5. `TreeMap` — Índice de monstruos del cementerio por ATK

**Por qué se eligió:** Para poder consultar cuál fue el **monstruo más fuerte destruido**, se necesita un mapa ordenado por ATK. `TreeMap` mantiene las claves ordenadas de forma ascendente, permitiendo `lastEntry()` en O(log n). Un `HashMap` no garantizaría ningún orden.

**Archivos:** `model/CementerioSet.java`

**Implementación:** `TreeMap<Integer, List<String>> monstruosPorAtk`. Cuando un `CartaMonstruo` es destruido, se agrega su nombre a la lista de su ATK. `monstruoMasFuerte()` llama a `monstruosPorAtk.lastEntry()`.

---

### 6. `HashMap` — Índice de cartas por nombre

**Por qué se eligió:** Buscar una carta por nombre durante la carga de partidas o la carga por Reflection necesita ser O(1). Recorrer una lista sería O(n) en cada búsqueda, lo que resulta costoso cuando el mazo tiene 50 cartas y se restaura el estado completo de ambos jugadores.

**Archivos:** `model/IndiceCartas.java`

**Implementación:** `Map<String, Carta> indice = new HashMap<>()`. `registrar(carta)` indexa la carta por su nombre. `buscar(nombre)` → `indice.get(nombre)` en O(1).

---

##  Estructura del Proyecto

```
MP4/
├── src/                          # Código fuente Java
│   ├── App.java                  # Punto de entrada (modo GUI / Consola / Stats)
│   ├── controller/               # Capa controladora (MVC)
│   │   ├── DuelController.java   # Controlador principal del duelo
│   │   ├── InicioController.java # Controlador de la pantalla de inicio
│   │   ├── ObservadorDuelo.java  # Interfaz Observer (observador)
│   │   └── SujetoDuelo.java      # Interfaz Observer (sujeto)
│   ├── model/                    # Lógica de negocio pura
│   │   ├── Carta.java            # Clase abstracta base
│   │   ├── CartaMonstruo.java    # Monstruo con ATK/DEF/nivel/boosts
│   │   ├── CartaMagica.java      # Magia abstracta con activar()
│   │   ├── CartaTrampa.java      # Trampa abstracta con condición de activación
│   │   ├── Activable.java        # Interfaz para cartas activables
│   │   ├── Contexto.java         # Bolsa de contexto para activación de cartas
│   │   ├── CampoBatalla.java     # Motor del duelo + implementa SujetoDuelo
│   │   ├── Jugador.java          # Estado del jugador (mano, campo, LP, flags)
│   │   ├── Mazo.java             # Pila de cartas (ArrayDeque)
│   │   ├── FabricaDeCartas.java  # Factory del mazo completo
│   │   ├── IndiceCartas.java     # HashMap nombre→carta
│   │   ├── LogEventos.java       # Cola FIFO de eventos del turno
│   │   ├── CementerioSet.java    # Set + TreeMap para el cementerio
│   │   ├── command/              # Patrón Command
│   │   │   ├── IComando.java
│   │   │   ├── ComandoAtacar.java
│   │   │   ├── ComandoJugarCarta.java
│   │   │   └── HistorialComandos.java
│   │   ├── memento/              # Patrón Memento (guardar/cargar)
│   │   │   ├── EstadoDuelo.java
│   │   │   ├── EstadoJugador.java
│   │   │   └── MementoManager.java
│   │   └── cards/                # Implementaciones concretas de cartas
│   │       ├── magic/            # PotOfGreed, EspadaDeZeus, EscudoDeAtenea, etc.
│   │       └── trap/             # ContraAtaque, CampoMinado, BoltDivino, etc.
│   ├── view/                     # Capa de presentación
│   │   ├── IDuelView.java        # Interfaz común GUI/Consola
│   │   ├── VentanaDuelo.java     # Vista principal Swing (GUI)
│   │   ├── VentanaInicio.java    # Menú inicial Swing
│   │   └── ConsolaDuelo.java     # Vista de texto interactiva
│   ├── persistence/
│   │   └── CargadorCartas.java   # Reflection API — carga cartas desde .txt
│   └── utils/
│       └── GestorPersistencia.java # Singleton — archivos de guardado y resultados
├── bin/                          # Bytecode compilado (.class)
├── resources/
│   └── cartas/                   # Definiciones de cartas para Reflection
│       ├── BoltDivino.txt
│       ├── EspadaDeZeus.txt
│       └── ...
├── guardados/                    # Partidas guardadas (.txt, generado en ejecución)
├── resultados.txt                # Historial de resultados de duelos
└── README.md
```

---

##  Análisis Archivo por Archivo (Resumen)

| Archivo | Responsabilidad |
|---|---|
| `App.java` | Punto de entrada. Pregunta el modo (GUI / consola / estadísticas) e instancia la vista correspondiente. |
| `CampoBatalla.java` | Motor central del duelo. Maneja turnos, combate, ataques directos, boosts y notificaciones Observer. |
| `Jugador.java` | Estado completo de un jugador: LP, mano (`LinkedList`), mazo, campo de monstruos, zona de trampas y flags de turno. |
| `Carta.java` | Clase abstracta raíz con `getNombre()` y `getTipo()` abstracto. |
| `CartaMonstruo.java` | Extiende `Carta`. Gestiona ATK/DEF, boosts temporales (se borran al cambiar turno), modo defensa y estado de ataque del turno. |
| `CartaMagica.java` | Clase abstracta que implementa `Activable`. Cada subclase en `cards/magic/` define su efecto en `activar(Contexto)`. |
| `CartaTrampa.java` | Clase abstracta con `puedoActivarme(Contexto)` y `activar(Contexto)`. Las trampas se colocan boca abajo y se activan en respuesta a acciones del oponente. |
| `Contexto.java` | Objeto contenedor que viaja a `activar()`. Contiene jugador activo, oponente, campo y monstruo atacante (para trampas de respuesta). |
| `FabricaDeCartas.java` | Factory estático que construye el mazo de 50 cartas. Centraliza la composición del mazo. |
| `Mazo.java` | Pila de cartas sobre `ArrayDeque`. `robar()` → O(1). Incluye `barajar()` y `repartir(n)`. |
| `CementerioSet.java` | Registra cartas destruidas con `LinkedHashSet` (unicidad + orden) y `TreeMap` (monstruos por ATK). |
| `IndiceCartas.java` | `HashMap<String, Carta>` para búsqueda O(1) por nombre. Usado por Reflection y Memento. |
| `LogEventos.java` | Cola FIFO de eventos del turno sobre `ArrayDeque`. |
| `IComando.java` | Interfaz Command con `ejecutar()`, `deshacer()`, `getDescripcion()`. |
| `ComandoAtacar.java` | Implementa un ataque. Guarda LP previos y presencia en campo antes de ejecutar para restaurar en `deshacer()`. |
| `ComandoJugarCarta.java` | Implementa jugar una carta. Soporta sacrificio para monstruos de nivel 5+. `deshacer()` devuelve la carta a la mano. |
| `HistorialComandos.java` | Pila (`ArrayDeque`) de comandos. `ejecutar()` apila; `deshacer()` desapila y revierte. Se limpia al final del turno. |
| `EstadoDuelo.java` | Snapshot inmutable del duelo completo (`final` en todos los campos). |
| `EstadoJugador.java` | Snapshot de un jugador con sus zonas serializadas como listas de strings. |
| `MementoManager.java` | Caretaker del Memento. `capturar()` serializa el campo; `restaurar()` lo reconstruye usando `FabricaDeCartas`. |
| `GestorPersistencia.java` | Singleton que escribe/lee archivos `.txt` de guardado y mantiene `resultados.txt`. |
| `CargadorCartas.java` | Usa `Class.forName()` y `getDeclaredConstructor()` para instanciar cartas desde archivos de definición `.txt`. |
| `IDuelView.java` | Interfaz de la vista con `agregarLog()`, `actualizarUI()`, `mostrarGanador()`, `pedirSeleccion()`, `pedirTexto()`. |
| `DuelController.java` | Controlador principal. Implementa `ObservadorDuelo`, crea comandos Command, coordina Memento y persiste resultados. |

---



### Estructura de los archivos de guardado

Los archivos en `guardados/*.txt` son texto plano legible. Ejemplo:

```
# Partida guardada - Yu-Gi-Oh!
turno=8
primerTurno=false
jugadorActivo=Jean
J1.nombre=Juan
J1.lp=8000
J1.campo=Bestia del Bosque|4|1500|1200|true|false
J1.mano=Guerrero De La Luz,Campo Minado,...
```

### Agregar una nueva carta (sin recompilar el core)

1. Crear la clase Java en `src/model/cards/magic/` o `src/model/cards/trap/`
2. Crear el archivo `.txt` en `resources/cartas/` con:
   ```
   clase=model.cards.magic.NuevaCarta
   nombre=Nombre de la Carta
   descripcion=Descripción del efecto.
   tipo=MAGICA
   ```
3. La carta será cargada automáticamente por `CargadorCartas` en la siguiente ejecución.

---

##  Flujo Principal de Ejecución

```
App.main()
    │
    ├── [opción 1 / GUI]
    │       VentanaInicio.setVisible(true)
    │           └── InicioController.iniciarDuelo(n1, n2)
    │                   ├── new Jugador(n1), new Jugador(n2)
    │                   ├── new CampoBatalla(j1, j2)
    │                   ├── campo.iniciarDuelo()
    │                   │       ├── FabricaDeCartas.crearMazoCompleto()  [Factory]
    │                   │       ├── Collections.shuffle(mazo)
    │                   │       ├── repartir 25 cartas a cada jugador
    │                   │       └── notificarObservadores("INICIO_DUELO")  [Observer]
    │                   ├── new DuelController(campo)
    │                   │       └── campo.registrarObservador(this)  [Observer]
    │                   └── controller.iniciarPrimerTurno()
    │
    └── [Turno de juego]
            ├── Jugador elige acción (GUI o consola)
            │
            ├── [Jugar Carta]
            │       DuelController.accionJugarCarta()
            │           ├── new ComandoJugarCarta(...)  [Command]
            │           └── historial.ejecutar(cmd)
            │                   └── cmd.ejecutar() → jugador.jugarCarta()
            │
            ├── [Atacar]
            │       DuelController.accionAtacar()
            │           ├── (ofrece trampas de respuesta al defensor)
            │           ├── new ComandoAtacar(...)  [Command]
            │           └── historial.ejecutar(cmd)
            │                   └── cmd.ejecutar() → campo.resolverCombate()
            │                           └── notificarObservadores("COMBATE")  [Observer]
            │
            ├── [Deshacer]
            │       historial.deshacer()  [Command undo]
            │           └── cmd.deshacer() → restaura LP y monstruos
            │
            ├── [Guardar Partida]
            │       MementoManager.capturar(campo)  [Memento]
            │           └── GestorPersistencia.getInstance().guardarPartida(snapshot)  [Singleton]
            │
            └── [Terminar Turno]
                    campo.terminarTurno()
                        └── jugadorActivo cambia
                    campo.prepararTurno()
                        ├── jugador roba carta (Mazo.pop() O(1))
                        └── notificarObservadores("INICIO_TURNO")
```

---

##  Decisiones de Diseño

### MVC estricto con interfaz `IDuelView`
El controlador nunca importa `VentanaDuelo` ni `ConsolaDuelo` directamente; solo conoce `IDuelView`. Esto hace posible que el juego corra en dos modos completamente distintos sin cambiar una línea del controlador o el modelo.

### `Contexto` como objeto de paso de datos
En lugar de pasar `CampoBatalla`, `Jugador` activo y oponente como parámetros separados a cada `activar()`, se empaquetan en un `Contexto`. Esto evita que las firmas de métodos cambien cada vez que se necesite acceso a un nuevo dato del estado del duelo, y reduce el acoplamiento entre las cartas concretas y el modelo.

### Serialización de monstruos en campo con formato `pipe`
El estado del monstruo en campo se serializa como `nombre|nivel|atk|def|puedeAtacar|enDefensa`. Este formato es simple, legible en texto plano y fácilmente parseable con `split("\\|")`. Se eligió sobre JSON o XML para evitar dependencias externas y mantener los archivos de guardado editables a mano.

### `byte` y `short` para los atributos de CartaMonstruo
Los atributos `nivelCarta` (byte), `atk` y `def` (short) usan tipos primitivos de menor tamaño que `int` intencionalmente. Con mazos de hasta 50 cartas instanciadas simultáneamente, el impacto en memoria es marginal, pero la decisión demuestra conciencia del tamaño de los datos: el nivel de una carta nunca supera 12, y ATK/DEF raramente supera 4000.

### Reflection API para extensibilidad
`CargadorCartas` usa `Class.forName()` para instanciar cartas en tiempo de ejecución sin importarlas. Esto sigue el principio Abierto/Cerrado: el sistema está abierto a la extensión (nuevas cartas) pero cerrado a la modificación (no hay que tocar `FabricaDeCartas` ni `CargadorCartas`).

---

##  Posibles Mejoras

- **Hilo de juego separado para la GUI:** Algunas operaciones de la UI corren en el Event Dispatch Thread de Swing; mover la lógica de juego a un `SwingWorker` evitaría posibles congeladas en turnos complejos.
- **Índice de cartas en `CargadorCartas`:** Actualmente `MementoManager.reconstruirCarta()` llama a `FabricaDeCartas` iterando listas en cada restauración. Integrar `IndiceCartas` reduciría eso a O(1).
- **Múltiples comandos de undo por turno:** El sistema actual deshace solo la última acción. Ampliar `HistorialComandos` para un historial ilimitado dentro del turno sería trivial dado que ya usa una pila.
- **Pruebas unitarias:** El desacoplamiento entre modelo y vista hace que `CampoBatalla`, `Jugador`, y todos los comandos sean fácilmente testables con JUnit sin necesitar la GUI.
- **Soporte para más de un sacrificio:** Actualmente los monstruos de nivel 5-6 requieren un sacrificio y los de nivel 7+ teóricamente necesitarían dos, pero el sistema solo gestiona uno. Extender `ComandoJugarCarta` para manejar sacrificios múltiples es un camino natural.
- **Cifrado de archivos de guardado:** Los `.txt` son legibles y editables, lo que permite "hacer trampa". Aplicar un hash de verificación o cifrado ligero añadiría integridad.

---

##  Conclusión

Yu-Gi-Oh! Duelo MP4 es un sistema bien estructurado que va más allá de un simple juego de consola. La combinación del **patrón MVC** con la interfaz `IDuelView` garantiza que el mismo motor de juego funcione en GUI y consola sin cambios. Los **cinco patrones de diseño** no son ornamentales: Observer desacopla modelo de vista, Command habilita undo real en tiempo de ejecución, Memento provee persistencia con integridad de estado, Factory centraliza la composición del mazo y Singleton protege el acceso a disco.

Las **estructuras de datos** elegidas responden a necesidades concretas medibles en términos de complejidad temporal: `ArrayDeque` para O(1) en robo de carta y reversión de comandos, `LinkedList` para O(1) en adición al frente de la mano, `LinkedHashSet` para unicidad con orden en el cementerio, `TreeMap` para consultas de rango en el índice de ATK y `HashMap` para búsqueda O(1) por nombre de carta.

La integración de **Java Reflection** demuestra madurez en el uso del ecosistema Java, y el **formato de persistencia en texto plano** garantiza que los guardados sean portables, inspeccionables y editables sin herramientas especiales.

---

*Desarrollado por Joseph Andrey Puerta, Juan Pablo Rada y Jean Carlo Ospina.*