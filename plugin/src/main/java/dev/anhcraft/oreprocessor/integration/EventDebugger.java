package dev.anhcraft.oreprocessor.integration;

import org.bukkit.event.HandlerList;

import java.util.Map;

public interface EventDebugger {
    Map<String, HandlerList> getEventHandlers();
}
