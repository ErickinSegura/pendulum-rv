# Cambios del servidor por día

Esta lista describe qué se va desbloqueando conforme avanza el contador de días del servidor (`mundo.dia` en `settings.yml`). Cada sección agrupa lo que se activa al llegar a ese día. Todo lo del Día 0 está disponible desde el arranque; lo demás se "enciende" automáticamente al alcanzar el día indicado.

---

## Día 0 — Base del servidor (disponible desde el inicio)

### Sistemas de juego
- **Sistema de 3 vidas.** Número configurable de vidas (3 por defecto). Al morir se pierde una vida y, al quedarse sin vidas, el jugador recibe un baneo temporal.
- **Tótem de la inmortalidad.** Funciona de forma 100% confiable hasta el día 20; al usarlo se anuncia en el chat a todo el servidor. (Su comportamiento cambia en el día 20, ver abajo.)
- **Muertes especiales:** pilar/marca de muerte en el lugar donde cae el jugador, mensajes de muerte personalizados según la causa, y cofre con los drops del jugador en vez de esparcir el inventario por el suelo.
- **Retos de equipo.** Desafíos que el equipo debe completar para ganar premios (por defecto, tótems de la inmortalidad). Cuatro tipos: conseguir X de un ítem, obtener un logro concreto, matar X de cierto mob, o minar X de cierto bloque.
- **Ruleta de castigos.** Penalizaciones al fallar: perder ítems (ej. 5 diamantes), bloquear slots del inventario, reducir corazones, prohibir un ítem (ej. perlas de ender), y castigos de equipo (construir monumento, regalar tótems, no usar portales del Nether un día, organizar PvP).
- **Bingo.** Cartillas por equipo con progreso de recolectar/matar/minar, sincronizadas al scoreboard y a la base de datos.
- **Sistema de perks.** Habilidades de jugador (Lifesteal, SharedSpace, Last Stand, etc.).
- **Equipos y Cofre de equipo (TeamChest).** Organización en equipos y almacenamiento compartido entre miembros.
- **Pociones apilables**, **sistema de logros** y **reloj de eventos** con sonido.

### Rangos, equipos y emblemas (chat y tab)
- **Rangos.** Cada jugador tiene un rango (`admin`, `moderador`, `invitado`, `miembro`) que se muestra como etiqueta con color y descripción al pasar el cursor. Se configura en `players.yml`.
- **Equipos.** Prefijo de equipo `[Nombre]` junto al nombre; al pasar el cursor muestra los integrantes del equipo y sus relojes/vidas.
- **Emblema Founder.** Estrella dorada (✦) que aparece después del nombre de quienes participaron en Pendulum 1 (`founder: true` en `players.yml`). Al pasar el cursor indica "Founder — Participó en Pendulum 1".
- **Mensaje de muerte personalizado** por jugador, también en `players.yml`.

### Canje de códigos (easter eggs) — `/pdl canjear <código>`
Códigos secretos escondidos por la web y el servidor; cada uno se canjea **una sola vez por jugador** y entrega una recompensa. Configurables en `canjes.yml` sin recompilar. Ejemplos de recompensas: tótems, stacks de recursos, pociones, discos de música aleatorios, cabezas, un **Estandarte del Fundador** conmemorativo, y el **Reloj Dorado del Péndulo**.

- **Reloj Dorado del Péndulo.** Ítem de un solo uso: click derecho para otorgar **1 hora extra de día** al servidor; lo anuncia en el chat con partículas y sonido. Es un easter egg, se obtiene canjeando su código.

---

## Día 5 — Guerra de bases, trims y mundo más duro

### Evento ChargeBase (Zona de Carga)
Evento de defensa de base por oleadas. Aparecen mobs personalizados, cada uno con tier **básico** y **avanzado**, repartidos en cinco roles. Cada tier tiene su propio comportamiento programado:

#### Atacante — cuerpo a cuerpo, persigue al jugador más cercano
- **Atacante Básico ("La Cebolla").** Zombie. 60 de vida, 8 de daño, velocidad de movimiento 0.32 (más rápido que un zombie normal), tamaño 1.1. Lleva casco de cabeza de dragón y armadura de hierro con trim de cobre (no se cae al morir). Persigue y golpea de cerca. **Frenesí:** al bajar del 50% de vida gana Velocidad III y Fuerza II permanentes (con partículas de lava). Se activa una sola vez. **Suelta:** Fragmento de Ataque (20%).
- **Atacante Avanzado ("El Hueso").** Wither skeleton. 80 de vida, 14 de daño, velocidad 0.35, tamaño 1.4. Peto de hierro con trim de cobre. **Embestida:** cada 5 s, si hay un jugador a 20 bloques o menos, se lanza en salto hacia él (con partículas de lava y rugido). Si al caer queda a 2 bloques o menos del objetivo, le aplica Lentitud II y Debilidad por 3 s. **Suelta:** Unión de Ataque (10%).

#### Defensor — tanque, aguanta y protege
- **Defensor Básico ("Estudios San Ángel").** Sniffer. 120 de vida, 15 de daño cuerpo a cuerpo, tamaño 1.3, velocidad 0.25. Resistencia permanente, 80% de resistencia al empuje y empuje de ataque reforzado. **Último aliento:** al bajar del 30% de vida gana Resistencia II y Velocidad I por 10 s (con partículas de tótem). Una sola vez. **Suelta:** Fragmento de Defensa (20%).
- **Defensor Avanzado ("Multimedios").** Iron golem del doble de tamaño (escala 2). 150 de vida, tamaño 2, velocidad 0.25. Resistencia permanente, 80% de resistencia al empuje, y su golpe lanza a los jugadores por los aires (empuje vertical). **Último aliento** igual que el básico. Además **refleja el 25% del daño** que recibe de vuelta al atacante (espinas). **Suelta:** Unión de Defensa (10%).

#### Controlador — control a distancia
- **Controlador Básico ("El Trigonometrico").** Illusioner. 60 de vida. Mantiene su comportamiento nativo (dispara flechas, se vuelve invisible, crea clones). **Sus flechas** aplican, al azar (50/50), Levitación II por 5 s o Caída Lenta por 3 s. **Suelta:** Fragmento de Control (20%).
- **Controlador Avanzado ("Malboro").** Breeze. 80 de vida, velocidad 0.35. Dispara ráfagas de viento (nativo del breeze). **Zonas de viento:** cada 5 s crea una ráfaga a 3–8 bloques de distancia (radio 2, dura 3 s) que da Levitación III a quien la pise. **Correa:** mientras persigue a un jugador, si este se aleja más de 12 bloques lo jala de vuelta. **Parpadeo:** al bajar del 50% de vida, cada golpe que recibe lo teletransporta 2–5 bloques al azar. **Suelta:** Unión de Control (10%).

#### Healer — sana a sus aliados y huye de los jugadores
- **Healer Básico ("Eh un trials").** Bruja. 40 de vida. Huye de los jugadores (los detecta a 10 bloques). Cada 5 s **cura 18 de vida** a los mobs del evento en un radio de 20 bloques. Cuando la golpean, 30% de probabilidad de lanzar una poción de Veneno II (5 s) al atacante. **Suelta:** Fragmento de Heal (20%).
- **Healer Avanzado.** Allay. 140 de vida, velocidad 0.38 (muy veloz), huye detectando a 14 bloques. Cura igual que el básico (18 de vida cada 5 s en radio 20) y lanza pociones de veneno. Además **resucita aliados:** cuando muere un mob del evento a 30 bloques o menos, 35% de probabilidad de revivirlo al 40% de su vida (no revive a otros healers). **Suelta:** Unión de Heal (10%).
- *Nota: en el código sigue sin apodo (aparece como "Healer Avanzado").*

#### Híbrido — mezcla de roles, se desbloquean sus núcleos híbridos el día 10
- **Híbrido Básico.** Creeper **cargado** (eléctrico), pequeño (escala 0.7) y veloz (velocidad 0.42). 80 de vida, radio de explosión 3. Emite chispas y chasquidos constantes. **Al encender la mecha:** aplica Oscuridad y Lentitud II (3 s) a los jugadores a 6 bloques, se vuelve invulnerable 1.5 s, y **enciende en cadena** a otros híbridos básicos a 8 bloques. **Al explotar:** cura 20 de vida a los mobs aliados a 10 bloques y deja una **zona eléctrica** en el suelo (radio 4, dura 5 s) que hace daño y ralentiza a quien la pise. **Si muere sin explotar:** suelta una descarga que hace 2 corazones de daño más Lentitud II y Debilidad (4 s) a los jugadores a 5 bloques. **Suelta:** Unión Híbrida (1%).
- *Nota: en el código sigue sin apodo (aparece como "Hibrido Básico").*
- **Híbrido Avanzado ("C Ñank F1").** Warden. 120 de vida, 18 de daño, tamaño 1.3, velocidad 0.32, detecta jugadores a 40 bloques. Tiene **3 fases según su vida** (cambio con partículas y rugido de warden):
  - *Fase 1 (siempre activa):* aura cada 4 s en radio 14 que aplica Oscuridad y Lentitud a los jugadores.
  - *Fase 2 (bajo 66% de vida):* la Lentitud del aura sube de nivel.
  - *Fase 3 (bajo 33% de vida):* se **regenera 6 de vida cada 2 s**, la Lentitud sube a nivel 3, y gana un **escudo** que anula por completo un golpe cada 2 s.
  - **Suelta:** Unión Híbrida (10%).

### Ítems personalizados (día 5)
Se fabrican con los **núcleos base** de ChargeBase. Los que necesitan un **núcleo híbrido** (y por tanto una Unión Híbrida) están más abajo, en el día 10.

- **Varita Barrera** (usa Núcleo Defensor). Click derecho: onda de repulsión en radio 6 que empuja a los enemigos cercanos y les aplica Lentitud I (2 s), y da Resistencia I al usuario (5 s). No afecta a los compañeros de equipo. Enfriamiento de 15 s.
- **Papa Explosiva** (sin núcleo). Comestible-trampa: al comerla explota (radio 6) y mata al jugador en el acto, con mensaje de muerte propio ("se suicidó comiendo una Papa Explosiva"). También es la munición del Lanzapapas, atribuyendo la muerte al lanzador.
- **Zanahoria Rellenable** (usa Núcleo Heal). Comestible con **cargas**: al comerla restaura hambre como una zanahoria dorada pero **no se consume**, solo gasta una carga. Se rellena agachándose o en la mesa de crafteo con zanahorias normales; vacía, avisa que hay que rellenarla.
- **Frenesí (modo berserk)** (usa Núcleo Atacante). Click derecho: **Velocidad II + Fuerza II** durante 8 s, pero mientras dura recibes **+50% de daño**. Enfriamiento de 20 s.
- **Clawn** (usa Núcleo Control). Mientras lo llevas en cualquier mano: **+4 de rango de interacción** (bloques y entidades) pero **-5 de daño de ataque**. Se desactiva si empuñas un mazo o una lanza.
- **Fragmentos, Núcleos y Uniones.** Materiales de crafteo que sueltan los mobs de ChargeBase. Hay un núcleo base por rol (atacante, defensor, control, heal). Los **núcleos híbridos** —y todo lo que los usa— están bloqueados hasta el día 10.


### Trims de armadura con efectos
A partir del día 5, si llevas **las 4 piezas de armadura con trim del mismo material** (las 4 deben tener trim y ser del mismo material), obtienes un efecto de poción de nivel I mientras las lleves puestas. El juego lo comprueba y lo renueva cada segundo, así que se siente permanente. El efecto depende del material del trim:

| Material del trim | Efecto |
|---|---|
| Cuarzo | Impulso de salto |
| Hierro | Resistencia al fuego |
| Netherita | Resistencia |
| Redstone | Velocidad |
| Cobre | Caída lenta |
| Oro | Prisa |
| Esmeralda | Invisibilidad |
| Diamante | Fuerza |
| Lapislázuli | Respiración acuática |
| Amatista | Visión nocturna |

### Otros
- **Osos polares hostiles.**
- **Info de la Zona de Carga** visible en `/pdl info` (estado, ubicación, radio y tiempo restante, o cuándo será la próxima).

---

## Día 10 — Se abre el End y los mobs se vuelven peligrosos

- **Apertura del End.** El End permanece sellado hasta el día 10: antes, cualquier intento de cruzar un portal del End se cancela con aviso ("el End está sellado hasta el día 10").
- **Dragona del Vacío.** Mob especial del End.
- **Creepers del End y variantes de creeper.**
- **Endermen hostiles.**
- **Mobs con equipamiento y mobs mejorados** (suben la dificultad general del mundo).
- **Crafteo de núcleos híbridos** (atacante, defensor, control y heal), ligados a los roles de ChargeBase.

### Ítems personalizados que requieren núcleo híbrido
Cada uno lleva un núcleo híbrido en su receta, así que solo pueden fabricarse a partir del día 10 (el núcleo híbrido está bloqueado hasta entonces).

- **Varita Barrera Mejorada** (usa Núcleo Defensor Híbrido). Click derecho: **provoca** a todos los mobs a radio 10 para que ataquen al usuario, y da **Resistencia II** (10 s) al usuario y a sus compañeros de equipo dentro del radio. Durante esos 10 s, **refleja el 25% del daño** recibido de vuelta al atacante. Enfriamiento de 30 s.
- **Varita Curativa** (usa Núcleo Heal Híbrido). Click derecho: cura **4 corazones** en radio 5 a los compañeros de equipo (o solo a sí mismo si no tiene equipo), da **Absorción I** (20 s) y limpia **veneno y wither**. Enfriamiento de 10 s, mostrado como el de las perlas de ender.
- **Lanzapapas** (usa Núcleo Atacante Híbrido). Ballesta que se carga con una **Papa Explosiva** (~1.25 s) y la dispara como proyectil. Al impactar explota (radio 3). Si el lanzador está a menos de 5 bloques del impacto, recibe daño de la onda.
- **Lazo** (usa Núcleo Control Híbrido). Caña de pescar convertida en **gancho de agarre**: cuando el anzuelo se clava en un bloque, impulsa al jugador en arco hacia ese punto para balancearse. Se suelta al agacharse, soltar la caña, acercarse demasiado al ancla o tras un tiempo máximo. Anula el daño por caída durante el balanceo.

### Santuario del Vacío (estructura flotante)
- **Torre flotante en el End**, suspendida en el vacío de los biomas exteriores (Highlands/Midlands). Generación muy rara; al aparecer, el jugador más cercano recibe un aviso (sin coordenadas) y la consola registra dónde se generó.
- **Por pisos:** isla de end stone, claustro de columnas de purpur en la base, torre central con una cámara intermedia y un **santuario abierto en lo alto** con el boss. Agujas en las esquinas y escalera de mano conectando los niveles.
- **Spawners de enderman** en la planta baja y la cámara (generan rápido).
- **Botín del End:** perlas, fruta chorus, obsidiana, ojos de ender, aliento de dragón, caparazones de shulker, cristales del End, scrap de netherita, y raros **élitros**, bloques de diamante y manzanas encantadas. Además, los **items del Vacío** (ver abajo).

### Custodio del Vacío (jefe)
- **Enderman gigante** con barra de jefe (~350 de vida) que custodia el santuario. Se mantiene agresivo y no cae al vacío (se reposiciona si queda sobre el abismo).
- **Ataques:** parpadeo (teleporte + golpe), salva de proyectiles teledirigidos, pulso de levitación (peligrosísimo en una isla flotante), aliento del vacío (nube con Wither) y velo del vacío (oscuridad + lentitud). Entra en **frenesí** bajo el 40% de vida.
- **Drops:** XP, aliento de dragón y **1 a 5 items del Vacío** al azar.

### Items del Vacío
- **Ancla de Vínculo.** Click derecho: abre un menú con tus compañeros de equipo conectados y te **teletransportas junto al elegido** (incluso entre mundos). Enfriamiento prolongado (5 min). No se apila.
- **Frasco del Vacío.** **Click izquierdo** a un mob **pasivo o neutral** para guardarlo (conserva todo: domado, variante, color, montura/arnés, item que sujeta, vida, edad, nombre…); **click derecho al suelo** para liberarlo. No funciona con mobs hostiles, jefes ni mobs de eventos. No se apila. El lore muestra el nombre del mob (o su etiqueta si tiene una).

### Logro
- **Vigilia Rota** — derrota al Custodio del Vacío.

---

## Día 15 — La Forja Ancestral

### Sistema de modificadores
- **Modificadores de equipo.** Objetos especiales que se aplican a herramientas/armas/armaduras en la **mesa de herrería** (smithing table): se pone el objeto en la ranura base y el modificador en la de material, sin plantilla. Se valida que sea el modificador real (no el material vanilla suelto). Hay tres:
  - **Irrompible.** Vuelve indestructible cualquier objeto con durabilidad. No acumulable.
  - **Liviano.** Para armadura: **+5% de velocidad** por pieza, acumulable (apilando el modificador y entre piezas).
  - **Temple.** Para armadura: **+1 corazón** de vida máxima por aplicación, acumulable. **Incompatible con Irrompible** (no se pueden tener ambos en la misma pieza).

### Guardián de la Forja (jefe)
- **Boss personalizado** con barra de jefe, ~300 de vida, que se siente cercano a vanilla pero con **6 ataques**: golpe de yunque (AoE + empuje), llamarada, lluvia de fuego, salva de flechas, convocar escoria (magma cubes) y un tirón gravitatorio. Entra en **frenesí** por debajo del 40% de vida.
- **Drops.** Al morir elige uno de los tres modificadores y tiene un **50%** de soltarlo.

### Estructura: Forja Ancestral
- **Torre por niveles** (no genera antes del día 15): planta de forja con estaciones de herrería, lava y **spawners** (blaze y esqueleto wither), segundo piso de armería con balcón, y **terraza almenada donde aguarda el Guardián**. Torres de esquina, pórtico de entrada y patio exterior.
- **Variaciones de material:** clásica (blackstone), deepslate y nether.
- **Aviso de generación:** al generarse, el jugador más cercano recibe un mensaje (sin coordenadas); en consola se registran las coordenadas.
- **Botín** acorde al día 15+: diamantes, scrap/escombros de netherita, lingote de netherita (raro), plantillas de smithing, manzanas doradas/encantadas, y **muchas zanahorias y papas explosivas** por cofre.

### Logros
- **Maestro Herrero** — aplica un modificador en la mesa de herrería.
- **A Prueba de Todo** — aplica el modificador Irrompible.
- **Peso Pluma** — aplica el modificador Liviano.
- **Temple de Acero** — aplica el modificador Temple.
- **Guardián Caído** — derrota al Guardián de la Forja.

---

## Día 20 — El tótem deja de ser garantía

- **Riesgo en el tótem.** Desde el día 20, usar un tótem ya no es seguro al 100%: tiene un **1% de probabilidad de fallar**. Si falla, la resurrección se cancela, se reproducen partículas y sonido de ruptura, y se anuncia al servidor que el tótem del jugador falló. Antes del día 20 siempre funciona.
- **Logro "Tentando al destino"** por usar un tótem bajo riesgo.

---

## Recetas de crafteo (items personalizados)

Todas se elaboran en la **mesa de crafteo (3×3)** con forma exacta (las casillas vacías deben quedar vacías). Los ingredientes marcados como *Fragmento*, *Unión* y *Núcleo* son los materiales personalizados de ChargeBase.

### Núcleos base (día 5+)

Los cuatro núcleos comparten la misma forma: **4 Fragmentos del rol + 1 Unión del rol** en el centro. Cada uno usa el fragmento y la unión de su propia clase (atacante, defensor, heal, control).

```
[      ][ Fragmento ][      ]
[ Fragmento ][  Unión  ][ Fragmento ]
[      ][ Fragmento ][      ]
```

### Núcleos híbridos (día 10+)

Misma forma, pero con **4 Fragmentos del rol + 1 Unión Híbrida** en el centro. Requieren día 10 o superior. Hay uno por rol: atacante, defensor, heal y control.

```
[      ][ Fragmento ][      ]
[ Fragmento ][ Unión Híbrida ][ Fragmento ]
[      ][ Fragmento ][      ]
```

### Papa Explosiva

```
[         ][  Pólvora  ][         ]
[ Polvo de Blaze ][   Papa   ][ Polvo de Blaze ]
[         ][  Pólvora  ][         ]
```

### Lanzapapas (usa Núcleo Atacante Híbrido)

```
[ Lingote de Hierro ][   Ballesta   ][ Lingote de Hierro ]
[   Cuerda   ][ Núcleo Atacante Híbrido ][   Cuerda   ]
[  Pólvora  ][    Papa    ][  Pólvora  ]
```

### Frenesí (usa Núcleo Atacante)

```
[ Crema de Magma ][ Vara de Blaze ][ Crema de Magma ]
[ Polvo de Blaze ][ Núcleo Atacante ][ Polvo de Blaze ]
[ Crema de Magma ][   Pólvora   ][ Crema de Magma ]
```

### Zanahoria Rellenable (usa Núcleo Heal)

```
[ Lingote de Oro ][ Zanahoria Dorada ][ Lingote de Oro ]
[ Zanahoria Dorada ][ Núcleo Heal ][ Zanahoria Dorada ]
[ Lingote de Oro ][ Sandía Reluciente ][ Lingote de Oro ]
```

### Varita Curativa (usa Núcleo Heal Híbrido)

```
[ Polvo de Piedra Luminosa ][ Sandía Reluciente ][ Polvo de Piedra Luminosa ]
[ Lágrima de Ghast ][ Núcleo Heal Híbrido ][ Lágrima de Ghast ]
[ Sandía Reluciente ][ Vara de Blaze ][ Sandía Reluciente ]
```

### Clawn (usa Núcleo Control)

```
[ Bloque de Cobre ][ Lingote de Cobre ][         ]
[ Lingote de Cobre ][ Núcleo Control ][         ]
[ Bloque de Cobre ][ Lingote de Cobre ][         ]
```

### Gancho (usa Núcleo Control Híbrido)

```
[   Cuerda   ][ Gancho de Cuerda Trampa ][   Cuerda   ]
[ Bola de Slime ][ Núcleo Control Híbrido ][ Bola de Slime ]
[   Cuerda   ][ Caña de Pescar ][   Cuerda   ]
```

### Varita Barrera (usa Núcleo Defensor)

```
[ Vidrio Tintado Cian ][ Vara de End ][ Vidrio Tintado Cian ]
[ Vidrio Tintado Cian ][ Núcleo Defensor ][ Vidrio Tintado Cian ]
[ Lingote de Hierro ][   Escudo   ][ Lingote de Hierro ]
```

### Varita Barrera Mejorada (usa Núcleo Defensor Híbrido)

```
[ Diamante ][         ][ Diamante ]
[ Vidrio Tintado Cian ][ Núcleo Defensor Híbrido ][ Vidrio Tintado Cian ]
[ Diamante ][         ][ Diamante ]
```

### Dirty Hearty (solo hasta el día 10)

```
[ Bloque de Oro ][ Bloque de Oro ][ Bloque de Oro ]
[ Bloque de Oro ][ Cabeza de Jugador ][ Bloque de Oro ]
[ Bloque de Oro ][ Bloque de Oro ][ Bloque de Oro ]
```

### Manzana Dorada Encantada

Cada casilla de oro requiere **2 Bloques de Oro** (16 en total).

```
[ Bloque de Oro x2 ][ Bloque de Oro x2 ][ Bloque de Oro x2 ]
[ Bloque de Oro x2 ][   Manzana   ][ Bloque de Oro x2 ]
[ Bloque de Oro x2 ][ Bloque de Oro x2 ][ Bloque de Oro x2 ]
```
