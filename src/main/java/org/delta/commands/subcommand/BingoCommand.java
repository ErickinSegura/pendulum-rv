package org.delta.commands.subcommand;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.libs.builders.ItemBuilder;
import org.delta.managers.bingo.BingoChallenge;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoInventoryHolder;
import org.delta.managers.bingo.BingoProgressManager;
import org.delta.managers.bingo.BingoScoreManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BingoCommand implements SubCommand {

    @Override
    public String getName() {
        return "bingo";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!BingoDataManager.getInstance().isEnabled()) {
            player.sendMessage(MessageUtils.color("&c✘ El sistema de bingo está desactivado."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (args.length == 1) {
            abrirBingoGUI(player);
            return;
        }

        String subcomando = args[1].toLowerCase();

        switch (subcomando) {
            case "reset" -> {
                if (!checkPermission(player)) {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                    return;
                }
                resetearBingo(player, args);
            }
            case "stats" -> mostrarEstadisticas(player);
            case "leaderboard", "lb" -> {
                if (args.length == 2) {
                    mostrarLeaderboard(player);
                } else {
                    mostrarDetallesEquipo(player, args[2]);
                }
            }
            case "debug" -> {
                if (!checkPermission(player)) {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    return;
                }
                mostrarDebug(player);
            }
            default -> {
                player.sendMessage(MessageUtils.color("&c✘ Subcomando no reconocido."));
                showUsage(player);
            }
        }
    }

    private void mostrarLeaderboard(Player player) {
        BingoScoreManager scoreManager = BingoScoreManager.getInstance();
        List<Map.Entry<String, Integer>> leaderboard = scoreManager.getSortedLeaderboard();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&l🏆 LEADERBOARD DE BINGO &6&l🏆 &8&l≪"));
        player.sendMessage("");

        if (leaderboard.isEmpty()) {
            player.sendMessage(MessageUtils.color("&7Aún no hay equipos con puntos registrados"));
            player.sendMessage("");
            return;
        }

        int position = 1;
        for (Map.Entry<String, Integer> entry : leaderboard) {
            String medal = switch (position) {
                case 1 -> "&6🥇";
                case 2 -> "&7🥈";
                case 3 -> "&c🥉";
                default -> "&8#" + position;
            };

            String teamName = entry.getKey();
            int points = entry.getValue();

            player.sendMessage(MessageUtils.color(
                    medal + " &d" + teamName + " &8- &e" + points + " pts"
            ));

            position++;
        }

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Usa &d/pdl bingo lb <equipo> &7para ver detalles"));
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
    }

    private void mostrarDetallesEquipo(Player player, String teamName) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);

        if (team == null) {
            player.sendMessage(MessageUtils.color("&c✘ El equipo &f" + teamName + " &cno existe."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        BingoScoreManager scoreManager = BingoScoreManager.getInstance();
        List<BingoScoreManager.ScoreEntry> history = scoreManager.getScoreHistory(teamName);
        int totalScore = scoreManager.getTotalScore(teamName);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&lDETALLE DE PUNTOS - &d" + teamName + " &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ 🏆 &7Puntuación total: &e" + totalScore + " pts"));
        player.sendMessage("");

        if (history.isEmpty()) {
            player.sendMessage(MessageUtils.color("&7Este equipo aún no ha ganado puntos"));
            player.sendMessage("");
            return;
        }

        // Agrupar por tipo
        Map<String, List<BingoScoreManager.ScoreEntry>> groupedByType = new java.util.HashMap<>();
        for (BingoScoreManager.ScoreEntry entry : history) {
            groupedByType.computeIfAbsent(entry.type(), k -> new ArrayList<>()).add(entry);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

        // Mostrar retos
        if (groupedByType.containsKey("CHALLENGE")) {
            List<BingoScoreManager.ScoreEntry> challenges = groupedByType.get("CHALLENGE");
            int challengeTotal = challenges.stream().mapToInt(BingoScoreManager.ScoreEntry::points).sum();

            player.sendMessage(MessageUtils.color("&8└ 📊 &7Retos completados: &a" + challenges.size() + " &8(&e" + challengeTotal + " pts&8)"));

            for (BingoScoreManager.ScoreEntry entry : challenges) {
                String date = dateFormat.format(new Date(entry.timestamp()));
                String positionStr = getPositionString(entry.position());

                player.sendMessage(MessageUtils.color(
                        "&8   ├ &f" + entry.description() +
                                " &8[" + positionStr + "&8] &7→ &e+" + entry.points() + " pts"
                ));
            }
            player.sendMessage("");
        }

        // Mostrar líneas
        if (groupedByType.containsKey("LINE")) {
            List<BingoScoreManager.ScoreEntry> lines = groupedByType.get("LINE");
            int lineTotal = lines.stream().mapToInt(BingoScoreManager.ScoreEntry::points).sum();

            player.sendMessage(MessageUtils.color("&8└ 🎯 &7Líneas completadas: &a" + lines.size() + " &8(&e" + lineTotal + " pts&8)"));

            for (BingoScoreManager.ScoreEntry entry : lines) {
                String date = dateFormat.format(new Date(entry.timestamp()));
                String positionStr = getPositionString(entry.position());

                player.sendMessage(MessageUtils.color(
                        "&8   ├ &f" + entry.description() +
                                " &8[" + positionStr + "&8] &7→ &e+" + entry.points() + " pts"
                ));
            }
            player.sendMessage("");
        }

        // Mostrar bingo completo
        if (groupedByType.containsKey("FULL_BINGO")) {
            List<BingoScoreManager.ScoreEntry> fullBingos = groupedByType.get("FULL_BINGO");

            for (BingoScoreManager.ScoreEntry entry : fullBingos) {
                String date = dateFormat.format(new Date(entry.timestamp()));
                String positionStr = getPositionString(entry.position());

                player.sendMessage(MessageUtils.color("&8└ 🎊 &7Bingo Completo:"));
                player.sendMessage(MessageUtils.color(
                        "&8   └ &6¡COMPLETADO! " +
                                " &8[" + positionStr + "&8] &7→ &e+" + entry.points() + " pts"
                ));
            }
            player.sendMessage("");
        }

        player.sendMessage(MessageUtils.color("&7Última actualización: " +
                dateFormat.format(new Date(history.get(history.size() - 1).timestamp()))));
        player.sendMessage("");

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
    }

    private String getPositionString(int position) {
        return switch (position) {
            case 1 -> "&61°";
            case 2 -> "&72°";
            case 3 -> "&c3°";
            default -> "&8" + position + "°";
        };
    }

    private void mostrarDebug(Player player) {
        Team team = BingoProgressManager.getInstance().getPlayerTeam(player.getName());
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&lDEBUG DE BINGO &8&l≪"));
        player.sendMessage("");

        if (team == null) {
            player.sendMessage(MessageUtils.color("&c✘ No estás en ningún equipo"));
            player.sendMessage(MessageUtils.color("&7Equipos disponibles:"));
            for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
                player.sendMessage(MessageUtils.color("&8  - &d" + t.getName() + " &7(" + t.getEntries().size() + " jugadores)"));
            }
        } else {
            player.sendMessage(MessageUtils.color("&a✔ Equipo actual: &d" + team.getName()));
            player.sendMessage(MessageUtils.color("&7Jugadores en el equipo:"));
            for (String entry : team.getEntries()) {
                player.sendMessage(MessageUtils.color("&8  - &f" + entry));
            }
        }

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Datos guardados en YAML:"));

        Map<String, Object> data = progressManager.getProgressData();

        if (data.containsKey("completed") && data.get("completed") instanceof Map<?, ?> completedMap) {
            player.sendMessage(MessageUtils.color("&7Equipos con progreso guardado:"));
            for (Object teamName : completedMap.keySet()) {
                Object teamData = completedMap.get(teamName);
                int count = 0;
                if (teamData instanceof List<?> list) {
                    count = list.size();
                }
                player.sendMessage(MessageUtils.color("&8  - &d" + teamName + " &7(" + count + " completados)"));
            }
        } else {
            player.sendMessage(MessageUtils.color("&c✘ No hay datos de equipos guardados"));
        }

        if (team != null) {
            player.sendMessage("");
            player.sendMessage(MessageUtils.color("&7Progreso cargado en memoria para &d" + team.getName() + "&7:"));
            Set<Integer> completed = progressManager.getCompletedChallenges(team.getName());
            player.sendMessage(MessageUtils.color("&8  - Completados: &a" + completed.size()));
            if (!completed.isEmpty()) {
                player.sendMessage(MessageUtils.color("&8  - IDs: &f" + completed));
            }

            Map<Integer, Integer> progress = progressManager.getAllProgress(team.getName());
            player.sendMessage(MessageUtils.color("&8  - Progreso activo: &a" + progress.size() + " retos"));
            if (!progress.isEmpty()) {
                player.sendMessage(MessageUtils.color("&8  - Detalle:"));
                progress.forEach((id, amount) -> {
                    player.sendMessage(MessageUtils.color("&8    • Reto " + id + ": &f" + amount));
                });
            }
        }

        player.sendMessage("");
    }

    private void abrirBingoGUI(Player player) {
        Team team = BingoProgressManager.getInstance().getPlayerTeam(player.getName());

        if (team == null) {
            player.sendMessage(MessageUtils.color("&c✘ No perteneces a ningún equipo."));
            player.sendMessage(MessageUtils.color("&7Usa &d/pdl bingo debug &7para más información"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        openBingoGUI(player, team);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    private void openBingoGUI(Player player, Team team) {
        int gridSize = BingoDataManager.getInstance().getGridSize();

        int rows = gridSize;
        if (rows > 6) rows = 6;

        int inventorySize = rows * 9;

        Component title = MessageUtils.color("&6&lBingo - " + team.getName());
        BingoInventoryHolder holder = new BingoInventoryHolder(team.getName());
        Inventory gui = Bukkit.createInventory(holder, inventorySize, title);

        Map<String, BingoChallenge> challenges = BingoDataManager.getInstance().getChallenges();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        int startSlot = 2;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int challengeId = (row * gridSize) + col + 1;

                if (challengeId > challenges.size()) break;

                BingoChallenge challenge = challenges.get(String.valueOf(challengeId));
                if (challenge == null) continue;

                int slot = startSlot + (row * 9) + col;
                ItemStack item = createChallengeItem(challenge, team.getName(), progressManager);
                gui.setItem(slot, item);
            }
        }

        fillEmptySlots(gui, gridSize, startSlot);

        player.openInventory(gui);
    }

    private void fillEmptySlots(Inventory gui, int gridSize, int startSlot) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();

        for (int i = 0; i < gui.getSize(); i++) {
            int row = i / 9;
            int col = i % 9;

            boolean isInGrid = (row >= (startSlot / 9)) && (row < (startSlot / 9) + gridSize) &&
                    (col >= (startSlot % 9)) &&
                    (col < (startSlot % 9) + gridSize);

            if (gui.getItem(i) == null && !isInGrid) {
                gui.setItem(i, glass);
            }
        }
    }

    private ItemStack createChallengeItem(BingoChallenge challenge, String teamName,
                                          BingoProgressManager progressManager) {
        Material material;
        try {
            material = Material.valueOf(challenge.icon());
        } catch (IllegalArgumentException e) {
            material = Material.PAPER;
        }

        boolean completed = progressManager.isChallengeCompleted(teamName, challenge.id());
        int progress = progressManager.getProgress(teamName, challenge.id());

        String displayName = (completed ? "&a✔ " : "&7") + challenge.displayName();

        List<String> lore = new ArrayList<>();
        lore.add(challenge.description());
        lore.add("");
        lore.add("&7Progreso: &e" + progress + "&7/&e" + challenge.amount());

        if (completed) {
            lore.add("");
            lore.add("&a&l✔ COMPLETADO");
        } else {
            lore.add("");
            lore.add("&dHaz clic para completar");
        }

        ItemBuilder builder = new ItemBuilder(material)
                .setDisplayName(ItemBuilder.format(displayName))
                .setLore(lore.stream()
                        .map(ItemBuilder::format)
                        .toList());

        if (completed) {
            builder.addEnchant(Enchantment.AQUA_AFFINITY, 1)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS);
        }

        return builder.build();
    }

    private void mostrarEstadisticas(Player player) {
        Team team = BingoProgressManager.getInstance().getPlayerTeam(player.getName());

        if (team == null) {
            player.sendMessage(MessageUtils.color("&c✘ No perteneces a ningún equipo."));
            player.sendMessage(MessageUtils.color("&7Usa &d/pdl bingo debug &7para más información"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        BingoProgressManager progressManager = BingoProgressManager.getInstance();
        BingoScoreManager scoreManager = BingoScoreManager.getInstance();

        Set<Integer> completados = progressManager.getCompletedChallenges(team.getName());
        int totalChallenges = BingoDataManager.getInstance().getChallenges().size();
        int totalScore = scoreManager.getTotalScore(team.getName());

        Set<Integer> rowsCompletadas = progressManager.getCompletedRows(team.getName());
        Set<Integer> columnasCompletadas = progressManager.getCompletedColumns(team.getName());
        boolean diagonal1 = progressManager.hasCompletedDiagonal1(team.getName());
        boolean diagonal2 = progressManager.hasCompletedDiagonal2(team.getName());

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&lESTADÍSTICAS DEL BINGO &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ 🏆 &7Equipo: &d" + team.getName()));
        player.sendMessage("");

        sendBingoStat(player, "🏅", "Puntuación total", totalScore + " pts");
        sendBingoStat(player, "📊", "Retos completados", completados.size() + "/" + totalChallenges);

        int porcentaje = totalChallenges > 0 ? (int) ((completados.size() / (double) totalChallenges) * 100) : 0;
        sendBingoStat(player, "📈", "Progreso total", porcentaje + "%");

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ 🎯 &7Líneas de Bingo:"));

        if (rowsCompletadas.isEmpty() && columnasCompletadas.isEmpty() && !diagonal1 && !diagonal2) {
            player.sendMessage(MessageUtils.color("&8   &7Ninguna línea completada aún"));
        } else {
            if (!rowsCompletadas.isEmpty()) {
                player.sendMessage(MessageUtils.color("&8   ├ &aFilas: &d" + rowsCompletadas.size()));
            }
            if (!columnasCompletadas.isEmpty()) {
                player.sendMessage(MessageUtils.color("&8   ├ &aColumnas: &d" + columnasCompletadas.size()));
            }
            if (diagonal1) {
                player.sendMessage(MessageUtils.color("&8   ├ &aDiagonal principal: &d✔"));
            }
            if (diagonal2) {
                player.sendMessage(MessageUtils.color("&8   └ &aDiagonal secundaria: &d✔"));
            }
        }

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Usa &d/pdl bingo lb " + team.getName() + " &7para ver detalles"));
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
    }

    private void resetearBingo(Player executor, String[] args) {
        if (args.length == 2) {
            resetearTodoElBingo(executor);
        } else if (args.length == 3) {
            String teamName = args[2];
            resetearEquipo(executor, teamName);
        } else {
            executor.sendMessage(MessageUtils.color("&c✘ Uso incorrecto."));
            executor.sendMessage(MessageUtils.color("&7Usa: &d/pdl bingo reset &7o &d/pdl bingo reset <equipo>"));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }

    private void resetearTodoElBingo(Player executor) {
        executor.playSound(executor.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &c&lADVERTENCIA &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&7Estás a punto de &creiniciar todo el progreso &7del bingo."));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ 📊 &7Esto incluye:"));
        executor.sendMessage(MessageUtils.color("&8   ├ &7Progreso de &ctodos los equipos"));
        executor.sendMessage(MessageUtils.color("&8   ├ &7Retos &ccompletados"));
        executor.sendMessage(MessageUtils.color("&8   ├ &7Líneas de &cbingo"));
        executor.sendMessage(MessageUtils.color("&8   ├ &7Estadísticas &cgenerales"));
        executor.sendMessage(MessageUtils.color("&8   └ &7Puntuaciones y &cleaderboard"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&ePara confirmar, usa: &d/pdl bingo reset confirm"));
        executor.sendMessage("");

        executor.playSound(executor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
    }

    private void resetearEquipo(Player executor, String teamName) {
        if (teamName.equalsIgnoreCase("confirm")) {
            ejecutarResetGlobal(executor);
            return;
        }

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);

        if (team == null) {
            executor.sendMessage(MessageUtils.color("&c✘ El equipo &f" + teamName + " &cno existe."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        BingoProgressManager progressManager = BingoProgressManager.getInstance();
        BingoScoreManager scoreManager = BingoScoreManager.getInstance();

        int completados = progressManager.getCompletedChallenges(teamName).size();
        int puntosAntes = scoreManager.getTotalScore(teamName);

        progressManager.resetTeamProgress(teamName);
        scoreManager.resetTeamScore(teamName);
        BingoDataManager.getInstance().saveProgress();

        executor.playSound(executor.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &a&lRESET EXITOSO &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ 🏆 &7Equipo: &d" + teamName));
        executor.sendMessage(MessageUtils.color("&8└ 📊 &7Retos reseteados: &d" + completados));
        executor.sendMessage(MessageUtils.color("&8└ 🏅 &7Puntos perdidos: &d" + puntosAntes + " pts"));
        executor.sendMessage(MessageUtils.color("&8└ ✔ &7El equipo puede comenzar desde cero"));
        executor.sendMessage("");

        for (String memberName : team.getEntries()) {
            Player member = Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage("");
                member.sendMessage(MessageUtils.color("&8&l≫ &6&lNOTIFICACIÓN DE BINGO &8&l≪"));
                member.sendMessage("");
                member.sendMessage(MessageUtils.color("&7El progreso de tu equipo &d" + teamName + " &7ha sido &creseteado"));
                member.sendMessage("");
                member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1.0f);
            }
        }
    }

    private void ejecutarResetGlobal(Player executor) {
        BingoProgressManager progressManager = BingoProgressManager.getInstance();
        BingoScoreManager scoreManager = BingoScoreManager.getInstance();

        int totalEquipos = 0;
        int totalCompletados = 0;
        int totalPuntos = 0;

        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            Set<Integer> completados = progressManager.getCompletedChallenges(team.getName());
            int puntos = scoreManager.getTotalScore(team.getName());

            if (!completados.isEmpty() || puntos > 0) {
                totalEquipos++;
                totalCompletados += completados.size();
                totalPuntos += puntos;
            }
        }

        BingoDataManager.getInstance().resetProgress();
        scoreManager.resetAllScores();

        executor.playSound(executor.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &a&lRESET GLOBAL EXITOSO &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ 🏆 &7Equipos afectados: &d" + totalEquipos));
        executor.sendMessage(MessageUtils.color("&8└ 📊 &7Total de retos reseteados: &d" + totalCompletados));
        executor.sendMessage(MessageUtils.color("&8└ 🏅 &7Total de puntos perdidos: &d" + totalPuntos + " pts"));
        executor.sendMessage(MessageUtils.color("&8└ ✔ &7Todos los equipos comenzarán desde cero"));
        executor.sendMessage("");

        Component anuncio = MessageUtils.color("&8&l≫ &6&lBINGO RESETEADO &8&l≪ &7El progreso ha sido reiniciado");
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(anuncio);
        Bukkit.broadcast(Component.empty());

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1.0f);
        }
    }

    private void sendBingoStat(Player player, String icon, String label, String value) {
        player.sendMessage(MessageUtils.color("&8└ " + icon + " &7" + label + ": &d" + value));
    }

    private boolean checkPermission(Player player) {
        String[] ops = PendulumSettings.getInstance().getOp();
        if (ops == null) return false;
        return java.util.Arrays.asList(ops).contains(player.getName());
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }

    @Override
    public void showUsage(Player player) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&lCOMANDOS DE BINGO &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&d/pdl bingo &7- Abrir el tablero de bingo"));
        player.sendMessage(MessageUtils.color("&d/pdl bingo stats &7- Ver estadísticas del equipo"));
        player.sendMessage(MessageUtils.color("&d/pdl bingo lb &7- Ver leaderboard general"));
        player.sendMessage(MessageUtils.color("&d/pdl bingo lb <equipo> &7- Ver detalles de un equipo"));

        if (checkPermission(player)) {
            player.sendMessage(MessageUtils.color("&d/pdl bingo debug &7- Ver información de debug &8(Admin)"));
            player.sendMessage(MessageUtils.color("&d/pdl bingo reset &7- Resetear todo el bingo &8(Admin)"));
            player.sendMessage(MessageUtils.color("&d/pdl bingo reset <equipo> &7- Resetear un equipo &8(Admin)"));
        }

        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }
}