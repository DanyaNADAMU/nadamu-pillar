package space.nadamu.nadamupillar.disaster;

import org.bukkit.World.Environment;

import java.util.Set;

public interface Disaster {
    /**
     * Unique identifier for configuration referencing.
     */
    String getId();

    /**
     * Human-readable display name.
     */
    String getDisplayName();

    /**
     * MiniMessage formatted warning message displayed before disaster occurs.
     */
    String getWarningMessage();

    /**
     * Set of dimensions where this disaster is allowed to trigger.
     */
    Set<Environment> getAllowedEnvironments();

    /**
     * Countdown duration before execution in seconds (default 5s).
     */
    default int getWarningDurationSeconds() {
        return 5;
    }

    /**
     * Executes the disaster effect.
     */
    void execute(DisasterContext context);
}
