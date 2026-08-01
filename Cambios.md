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
Evento de defensa de base por oleadas. Aparecen mobs personalizados, cada uno con tier **básico** y **avanzado**, repartidos en cinco roles:

- **Atacante** — daño puro de cuerpo a cuerpo.
  - *Atacante Básico:* zombie con casco de cabeza de dragón y armadura de hierro. 60 de vida, 8 de daño, algo más rápido y grande de lo normal, fuerte empuje.
  - *Atacante Avanzado:* wither skeleton. 80 de vida, 14 de daño, más rápido, mayor escala y empuje aún más fuerte.
- **Defensor** — tanque que aguanta y bloquea.
  - *Defensor Básico ("Estudios San Ángel"):* sniffer con 120 de vida, Resistencia permanente y alta resistencia al empuje.
  - *Defensor Avanzado ("Multimedios"):* iron golem gigante (doble de tamaño) con 150 de vida, Resistencia y empuje vertical.
- **Controlador** — control de área a distancia.
  - *Controlador Básico ("El Trigonometrico"):* illusioner, 60 de vida.
  - *Controlador Avanzado ("Malboro"):* breeze, 80 de vida, más veloz.
- **Healer** — apoyo que cura y huye del combate.
  - *Healer Básico ("Eh un trials"):* bruja con 40 de vida que huye de los jugadores.
  - *Healer Avanzado:* allay con 140 de vida, muy rápido, que mantiene distancia.
- **Híbrido** — combina amenaza y sorpresa.
  - *Híbrido Básico:* creeper **cargado** (eléctrico) pequeño y veloz, 80 de vida, explosión de radio 3.
  - *Híbrido Avanzado ("C Ñank F1"):* warden con 120 de vida, gran daño y enorme rango de detección.

### Ítems personalizados 
- **Varita Barrera.** Click derecho: libera una **onda de repulsión** (radio 6 bloques) que empuja a los enemigos cercanos y les aplica Lentitud, mientras cubre al usuario con **Resistencia**. Enfriamiento de 15 segundos.
- **Varita Barrera Mejorada.** Click derecho: **provoca** a todos los mobs cercanos (radio 10) para que ataquen al usuario, activa un **aura de espinas** que refleja el 25% del daño recibido durante 10 segundos, y otorga **Resistencia II** al usuario y a sus compañeros de equipo dentro del radio. Enfriamiento de 30 segundos.
- **Varita Curativa.** Click derecho: cura **4 corazones** en radio de 5 bloques a los miembros de su equipo (o solo a sí mismo si no tiene equipo), les da corazones de **Absorción** y limpia **veneno y wither**. Enfriamiento de 10 segundos, mostrado como el de las perlas de ender.
- **Lanzapapas.** Ballesta personalizada que se carga con una **Papa Explosiva** (tarda ~1.25 s en cargar) y la dispara como proyectil. Al impactar provoca una explosión (radio 3). Si el lanzador está a menos de 5 bloques del impacto, recibe daño por la onda.
- **Papa Explosiva.** Comestible-trampa: al comerla, explota (radio 6) y mata al jugador en el acto, con mensaje de muerte propio ("se suicidó comiendo una Papa Explosiva"). También sirve de munición para el Lanzapapas, atribuyendo la muerte al lanzador.
- **Zanahoria Rellenable.** Comestible con **cargas**: al comerla restaura hambre como una zanahoria dorada pero **no se consume**, solo gasta una carga. Se rellena agachándose o en la mesa de crafteo con zanahorias normales; vacía, avisa que hay que rellenarla.
- **Lazo.** Caña de pescar convertida en **gancho de agarre**: cuando el anzuelo se clava en un bloque, impulsa al jugador en arco hacia ese punto para balancearse. Se suelta al agacharse, soltar la caña, acercarse demasiado al ancla o tras un máximo de tiempo. Anula el daño por caída durante el balanceo.
- **Frenesí (modo berserk).** Click derecho: otorga **Velocidad II + Fuerza II** durante 8 segundos, pero mientras dura recibes **+50% de daño**. Enfriamiento de 20 segundos.
- **Clawn.** Mientras lo llevas en cualquier mano: **+4 de rango de interacción** (bloques y entidades) pero **-5 de daño de ataque**. El bono se desactiva si empuñas un mazo o una lanza.
- **Fragmentos, Núcleos y Uniones.** Materiales de crafteo. Hay núcleos por rol de ChargeBase (atacante, defensor, control, heal). Los **núcleos híbridos** están bloqueados hasta el día 10.


### Trims de armadura con efectos
A partir del día 5, si llevas **las 4 piezas de armadura con trim del mismo material**, obtienes un efecto de poción permanente (renovado cada segundo) mientras la lleves puesta. El efecto depende del material del trim:

| Material del trim | Efecto |
|---|---|
| Cuarzo | Impulso de salto |
| Hierro | Resistencia |
| Netherita | Resistencia al fuego |
| Redstone | Velocidad |
| Cobre | Gracia del delfín |
| Oro | Prisa |
| Esmeralda | Héroe de la aldea |
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
