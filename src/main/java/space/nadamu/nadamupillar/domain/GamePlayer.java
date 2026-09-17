package space.nadamu.nadamupillar.domain;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class GamePlayer {
    private final UUID uniqueId;
    private final String name;
    private PlayerRole role;

    public GamePlayer(UUID uniqueId, String name, PlayerRole role) {
        this.uniqueId = Objects.requireNonNull(uniqueId, "uniqueId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.role = Objects.requireNonNull(role, "role cannot be null");
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = Objects.requireNonNull(role, "role cannot be null");
    }

    public boolean isAlive() {
        return role == PlayerRole.ALIVE;
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamePlayer that = (GamePlayer) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
