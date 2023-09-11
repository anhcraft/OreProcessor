package dev.anhcraft.oreprocessor.util;

import com.google.gson.Gson;
import dev.anhcraft.oreprocessor.OreProcessor;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScopedLog {
    private static final Gson GSON = new Gson();
    private final PluginLogger logger;
    private final Map<String, String> data;

    public ScopedLog(PluginLogger logger, String scope) {
        this.logger = logger;
        this.data = new HashMap<>(4);
        data.put("timestamp", OreProcessor.getInstance().mainConfig.dateTimeFormat.format(new Date()));
        data.put("scope", scope);
    }

    public ScopedLog add(String key, Object value) {
        data.put(key, str(value));
        return this;
    }

    private String str(Object value) {
        if (value instanceof OfflinePlayer) {
            return ((OfflinePlayer) value).getName()+"#"+((OfflinePlayer) value).getUniqueId();
        } else if (value instanceof EconomyResponse) {
            EconomyResponse er = (EconomyResponse) value;
            String err = er.errorMessage == null ? "" : er.errorMessage;
            return String.format("a=%.3f|b=%.5f|e=%s", er.amount, er.balance, err);
        }
        return String.valueOf(value);
    }

    public void flush() {
        logger.writeRaw(GSON.toJson(data));
    }
}
