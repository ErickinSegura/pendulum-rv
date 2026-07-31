# Pendulum — Resource Pack base (custom items)

Base lista para que **solo agregues tus texturas PNG**. Toda la estructura,
los modelos y las definiciones de item ya están hechos.

## Cómo agregar tus texturas

1. Pon cada PNG (16x16 recomendado) en:
   `assets/minecraft/textures/item/custom/`
2. El nombre del archivo debe coincidir con la `key` del item (ver tabla).
   Ej: el núcleo atacante usa `nucleo_atacante.png`.
3. Listo. No tienes que tocar ningún `.json`.

## Items incluidos

| Item                    | Material base          | CMD | Textura (PNG)                  |
|-------------------------|------------------------|-----|--------------------------------|
| Clawn                   | popped_chorus_fruit    | 1   | clawn.png                      |
| Fragmento de Ataque     | popped_chorus_fruit    | 2   | fragmento_ataque.png           |
| Unión de Ataque         | popped_chorus_fruit    | 3   | union_ataque.png               |
| Fragmento de Defensa    | popped_chorus_fruit    | 4   | fragmento_defensa.png          |
| Unión de Defensa        | popped_chorus_fruit    | 5   | union_defensa.png              |
| Fragmento de Curación   | popped_chorus_fruit    | 6   | fragmento_heal.png             |
| Unión de Curación       | popped_chorus_fruit    | 7   | union_heal.png                 |
| Fragmento de Control    | popped_chorus_fruit    | 8   | fragmento_control.png          |
| Unión de Control        | popped_chorus_fruit    | 9   | union_control.png              |
| Unión Híbrida           | popped_chorus_fruit    | 10  | union_hibrida.png              |
| Núcleo Atacante         | popped_chorus_fruit    | 11  | nucleo_atacante.png            |
| Núcleo Defensor         | popped_chorus_fruit    | 12  | nucleo_defensor.png            |
| Núcleo Sanador          | popped_chorus_fruit    | 13  | nucleo_heal.png                |
| Núcleo de Control       | popped_chorus_fruit    | 14  | nucleo_control.png             |
| Núcleo Atacante Híbrido | popped_chorus_fruit    | 15  | nucleo_atacante_hibrido.png    |
| Núcleo Defensor Híbrido | popped_chorus_fruit    | 16  | nucleo_defensor_hibrido.png    |
| Núcleo Sanador Híbrido  | popped_chorus_fruit    | 17  | nucleo_heal_hibrido.png        |
| Núcleo Control Híbrido  | popped_chorus_fruit    | 18  | nucleo_control_hibrido.png     |
| Papa Explosiva          | potato                 | 1   | papa_explosiva.png             |
| Lanzapapas              | crossbow               | 1   | lanzapapas.png                 |
| Placeholder (debug)     | stick                  | 1   | placeholder.png                |
| Varita de Barrera       | end_rod                | 1   | varita_barrera.png             |
| Varita Barrera Mejorada | breeze_rod             | 2   | varita_barrera_mejorada.png    |
| Varita Curativa         | blaze_rod              | 1   | varita_curativa.png            |
| Frenesí                 | blaze_powder           | 1   | frenesi.png                    |
| Lazo                    | fishing_rod            | 1   | lazo.png                       |
| Ancla de Vínculo        | recovery_compass       | 1   | ancla_vinculo.png              |
| Frasco del Vacío        | shulker_shell          | 1   | frasco_vacio.png               |
| Zanahoria Encantada     | golden_carrot          | 1   | zanahoria_encantada.png        |
| Dirty Hearty            | rotten_flesh           | 1   | dirty_hearty.png               |
| Liviano (modifier)      | feather                | 1   | liviano_modifier.png           |
| Irrompible (modifier)   | nether_brick           | 1   | unbreakable_modifier.png       |
| Temple (modifier)       | ghast_tear             | 1   | temple_modifier.png            |

## Estructura

```
resourcepack/
├── pack.mcmeta
├── pack.png                 (opcional: ícono del pack, 128x128)
└── assets/minecraft/
    ├── items/               definiciones que mapean CMD -> modelo
    │   ├── popped_chorus_fruit.json
    │   ├── potato.json
    │   ├── crossbow.json
    │   └── stick.json
    ├── models/item/custom/  un modelo por item (ya hechos)
    └── textures/item/custom/ <-- TUS PNGs van aquí
```

## Notas

- **`pack_format`**: está en `pack.mcmeta` como `64` con un rango
  `supported_formats` amplio. Si tu cliente marca el pack como incompatible,
  ajusta `pack_format` al número exacto de tu versión (1.21.11). El pack
  igual suele cargar aceptando la advertencia.
- **Lanzapapas (ballesta)**: usa un modelo plano simple. Las ballestas
  normales conservan su animación de carga porque la definición vanilla se
  mantiene como `fallback`. Si quieres animación de tensado para el Lanzapapas,
  habría que añadir modelos de `pulling` aparte.
- Usa **layer0 = item/generated** (sprite plano). Si algún item lleva modelo 3D,
  cambia su `.json` en `models/item/custom/`.
- Para servir el pack automáticamente, comprime el **contenido** de `resourcepack/`
  (que `pack.mcmeta` quede en la raíz del .zip, no dentro de una subcarpeta).
```
