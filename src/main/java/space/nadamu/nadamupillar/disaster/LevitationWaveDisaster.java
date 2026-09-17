package space.nadamu.nadamupillar.disaster;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumSet;
import java.util.Set;

public class LevitationWaveDisaster implements Disaster {
    @Override
    public String getId() {
        return "levitation_wave";
    }

    @Override
    public String getDisplayName() {
        return "Гравитационная аномалия";
    }

    @Override
    public String getWarningMessage() {
        return "<red><bold>⚠ ВНИМАНИЕ:</bold> <yellow>Гравитационная аномалия! Все взлетают!</yellow></red>";
    }

    @Override
    public Set<Environment> getAllowedEnvironments() {
        return EnumSet.allOf(Environment.class);
    }

    @Override
    public void execute(DisasterContext context) {
        for (Player player : context.getAlivePlayers()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 80, 0, false, true, true));
        }
    }
}
