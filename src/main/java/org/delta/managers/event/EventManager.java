package org.delta.managers.event;

import org.bukkit.Location;
import org.delta.managers.event.impl.InauguracionEvent;
import org.delta.pendulum;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventManager {

    private final pendulum plugin;
    private final Map<String, ServerEvent> events = new LinkedHashMap<>();

    private ServerEvent activeEvent = null;
    private Location anchor = null;

    public EventManager(pendulum plugin) {
        this.plugin = plugin;
        registerEvents();
    }

    private void registerEvents() {
        register(new InauguracionEvent(plugin));
    }

    private void register(ServerEvent event) {
        events.put(event.getId().toLowerCase(), event);
    }

    public boolean activate(String id, Location anchor) {
        if (activeEvent != null) return false;
        ServerEvent event = events.get(id.toLowerCase());
        if (event == null) return false;

        this.anchor = anchor.clone();
        this.activeEvent = event;
        event.activate(this.anchor);
        return true;
    }

    public boolean deactivate() {
        if (activeEvent == null) return false;
        activeEvent.deactivate();
        activeEvent = null;
        anchor = null;
        return true;
    }

    public boolean isActive() {
        return activeEvent != null;
    }

    public boolean isActive(String id) {
        return activeEvent != null && activeEvent.getId().equalsIgnoreCase(id);
    }

    public ServerEvent getActiveEvent() {
        return activeEvent;
    }

    public Location getAnchor() {
        return anchor;
    }

    public ServerEvent get(String id) {
        return events.get(id.toLowerCase());
    }

    public Collection<ServerEvent> getEvents() {
        return events.values();
    }
}
