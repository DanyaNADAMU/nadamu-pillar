package space.nadamu.nadamuPillar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender commandSender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args) {
        if(args.length == 0) {
            return false;
        }
        if(args[0].equalsIgnoreCase("start")) {
            if(!Game.start()) {
                commandSender.mess
            }
            return true;
        }
        return false;
    }
}
