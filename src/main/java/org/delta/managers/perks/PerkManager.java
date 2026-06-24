package org.delta.managers.perks;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delta.database.repositories.TeamRepository;
import org.delta.pendulum;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.delta.libs.MessageUtils.sendConsole;

public class PerkManager {

    private static PerkManager instance;
    private File dataFile;
    private FileConfiguration data;

    private final Map<String, Set<Perk>> activePerks = new HashMap<>();

    private PerkManager() {}

    public static PerkManager getInstance() {
        if (instance == null) instance = new PerkManager();
        return instance;
    }

    public static void initialize(File dataFolder) {
        getInstance().setup(dataFolder);
    }

    private void setup(File dataFolder) {
        dataFile = new File(dataFolder, "perks.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                sendConsole("&cError creando perks.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        load();
    }

    private void load() {
        activePerks.clear();
        if (!data.isConfigurationSection("perks")) return;

        for (String teamId : data.getConfigurationSection("perks").getKeys(false)) {
            List<String> perkNames = data.getStringList("perks." + teamId);
            Set<Perk> perks = perkNames.stream()
                    .map(name -> {
                        try { return Perk.valueOf(name); }
                        catch (IllegalArgumentException e) {
                            sendConsole("&ePerk desconocida ignorada: " + name);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(HashSet::new));

            activePerks.put(teamId, perks);
        }

        sendConsole("&dPerks cargadas correctamente.");
    }

    private void save() {
        data.set("perks", null);

        activePerks.forEach((teamId, perks) -> {
            List<String> perkNames = perks.stream()
                    .map(Enum::name)
                    .toList();
            data.set("perks." + teamId, perkNames);
        });

        try {
            data.save(dataFile);
        } catch (IOException e) {
            sendConsole("&cError guardando perks.yml: " + e.getMessage());
        }
    }


    public void assignPerk(String teamId, Perk perk) {
        Set<Perk> perks = new HashSet<>();
        perks.add(perk);
        activePerks.put(teamId, perks);
        var db = pendulum.getInstance().getDatabaseManager();
        db.teams().updatePerk(teamId, perk.getId());
        save();
    }

    public void removePerk(String teamId, Perk perk) {
        if (!activePerks.containsKey(teamId)) return;
        activePerks.get(teamId).remove(perk);
        pendulum.getInstance().getDatabaseManager().teams().clearPerk(teamId);
        save();
    }

    public void resetTeam(String teamId) {
        activePerks.remove(teamId);
        pendulum.getInstance().getDatabaseManager().teams().clearPerk(teamId);
        save();
    }

    public void resetAll() {
        var db = pendulum.getInstance().getDatabaseManager();
        activePerks.keySet().forEach(teamId -> db.teams().clearPerk(teamId));
        activePerks.clear();
        save();
    }

    public Set<Perk> getTeamPerks(String teamId) {
        return activePerks.getOrDefault(teamId, Collections.emptySet());
    }

}