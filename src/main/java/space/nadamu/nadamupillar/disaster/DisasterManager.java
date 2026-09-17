package space.nadamu.nadamupillar.disaster;

import org.bukkit.World.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

public class DisasterManager {
    private final Map<String, Disaster> registry = new LinkedHashMap<>();
    private final Random random = new Random();
    private final Logger logger;

    public DisasterManager(Logger logger) {
        this.logger = logger != null ? logger : Logger.getLogger(DisasterManager.class.getName());
        registerDefaults();
    }

    public DisasterManager() {
        this(null);
    }

    private void registerDefaults() {
        registerDisaster(new MeteorShowerDisaster());
        registerDisaster(new AnvilRainDisaster());
        registerDisaster(new GhastAssaultDisaster());
        registerDisaster(new WindChargeStormDisaster());
        registerDisaster(new LevitationWaveDisaster());
    }

    public void registerDisaster(Disaster disaster) {
        if (disaster != null) {
            registry.put(disaster.getId().toLowerCase(), disaster);
            logger.fine("Registered disaster: " + disaster.getId());
        }
    }

    public Optional<Disaster> getDisaster(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(registry.get(id.toLowerCase()));
    }

    public Collection<Disaster> getAllDisasters() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public List<Disaster> getAvailableDisasters(Environment environment, List<String> allowedIds) {
        List<Disaster> matches = new ArrayList<>();
        for (Disaster d : registry.values()) {
            if (environment != null && !d.getAllowedEnvironments().contains(environment)) {
                continue;
            }
            if (allowedIds != null && !allowedIds.isEmpty()) {
                boolean idMatch = allowedIds.stream().anyMatch(id -> id.equalsIgnoreCase(d.getId()));
                if (!idMatch) {
                    continue;
                }
            }
            matches.add(d);
        }
        return matches;
    }

    public Optional<Disaster> selectRandomDisaster(Environment environment, List<String> allowedIds) {
        List<Disaster> pool = getAvailableDisasters(environment, allowedIds);
        if (pool.isEmpty()) {
            // Fallback: ignore allowedIds filter if pool is empty
            pool = getAvailableDisasters(environment, Collections.emptyList());
        }
        if (pool.isEmpty() && !registry.isEmpty()) {
            // Absolute fallback: any registered disaster
            pool = new ArrayList<>(registry.values());
        }
        if (pool.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(pool.get(random.nextInt(pool.size())));
    }
}
