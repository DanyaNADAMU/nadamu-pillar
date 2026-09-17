package space.nadamu.nadamupillar.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.nadamu.nadamupillar.domain.GamePlayer;

import java.util.Objects;

public class PlayerEliminateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final GamePlayer player;
    private final DamageCause cause;

    public PlayerEliminateEvent(@NotNull GamePlayer player, @Nullable DamageCause cause) {
        this.player = Objects.requireNonNull(player, "player cannot be null");
        this.cause = cause;
    }

    @NotNull
    public GamePlayer getPlayer() {
        return player;
    }

    @Nullable
    public DamageCause getCause() {
        return cause;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
