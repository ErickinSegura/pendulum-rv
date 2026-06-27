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
- **Varita Barrera.** Click derecho: genera una cúpula esférica de cristal (radio 3 bloques) alrededor del jugador, usando solo espacios de aire. Dura 10 segundos y luego se rompe sola. Enfriamiento de 30 segundos.
- **Varita Barrera Mejorada.** Versión grande: cúpula de **doble capa** de cristal (radio 8 bloques), dura 15 segundos, enfriamiento 25 segundos. Además otorga **Resistencia II** al usuario y a sus compañeros de equipo dentro del radio.
- **Varita Curativa.** Click derecho: libera una onda de **Vida Instantánea II (4 corazones)** en radio de 5 bloques. Solo cura a los miembros de su equipo (o solo a sí mismo si no tiene equipo). Enfriamiento de 10 segundos, mostrado como el de las perlas de ender.
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

---

## Día 20 — El tótem deja de ser garantía

- **Riesgo en el tótem.** Desde el día 20, usar un tótem ya no es seguro al 100%: tiene un **1% de probabilidad de fallar**. Si falla, la resurrección se cancela, se reproducen partículas y sonido de ruptura, y se anuncia al servidor que el tótem del jugador falló. Antes del día 20 siempre funciona.
- **Logro "Tentando al destino"** por usar un tótem bajo riesgo.
