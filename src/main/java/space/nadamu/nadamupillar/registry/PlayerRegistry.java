package space.nadamu.nadamupillar.registry;

import org.bukkit.entity.Player;
import space.nadamu.nadamupillar.domain.GamePlayer;
import space.nadamu.nadamupillar.domain.PlayerRole;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerRegistry {
    private final Map<UUID, GamePlayer> players = new ConcurrentHashMap<>();

    public GamePlayer register(Player player, PlayerRole initialRole) {
        return players.computeIfAbsent(
            player.getUniqueId(),
            uuid -> new GamePlayer(uuid, player.getName(), initialRole)
        );
    }

    public GamePlayer getOrCreatePlayer(Player player) {
        return register(player, PlayerRole.SPECTATOR);
    }

    public Optional<GamePlayer> getPlayer(UUID uniqueId) {
        return Optional.ofNullable(players.get(uniqueId));
    }

    public Optional<GamePlayer> getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    public void unregister(UUID uniqueId) {
        players.remove(uniqueId);
    }

    public Collection<GamePlayer> getAllPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public Collection<GamePlayer> getAlivePlayers() {
        return players.values().stream()
                .filter(GamePlayer::isAlive)
                .collect(Collectors.toUnmodifiableList());
    }

    public int getAliveCount() {
        return (int) players.values().stream()
                .filter(GamePlayer::isAlive)
                .count();
    }

    public int getTotalCount() {
        return players.size();
    }

    public void setAllRoles(PlayerRole role) {
        players.values().forEach(player -> player.setRole(role));
    }

    public void clear() {
        players.clear();
    }
}
