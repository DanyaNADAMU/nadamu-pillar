package space.nadamu.nadamupillar.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.nadamu.nadamupillar.fsm.ActiveState;
import space.nadamu.nadamupillar.fsm.GameManager;
import space.nadamu.nadamupillar.fsm.StartingState;
import space.nadamu.nadamupillar.fsm.WaitingState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PillarsCommand implements CommandExecutor, TabCompleter {
    private final GameManager gameManager;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public PillarsCommand(GameManager gameManager) {
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager cannot be null");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MINI_MESSAGE.deserialize("<yellow>Использование: /pillars <start|stop|forcenext|status></yellow>"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start" -> {
                int seconds = 5;
                if (args.length > 1) {
                    try {
                        seconds = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                gameManager.startMatch(seconds);
                sender.sendMessage(MINI_MESSAGE.deserialize("<green>Матч запущен! Отсчет: " + seconds + " сек.</green>"));
                return true;
            }
            case "stop" -> {
                gameManager.stopMatch();
                sender.sendMessage(MINI_MESSAGE.deserialize("<red>Матч остановлен и сброшен в режим ожидания.</red>"));
                return true;
            }
            case "forcenext" -> {
                if (gameManager.getCurrentState() instanceof StartingState) {
                    gameManager.transitionTo(new ActiveState(gameManager, gameManager.getPlayerRegistry(), gameManager.getArenaService()));
                    sender.sendMessage(MINI_MESSAGE.deserialize("<green>Отсчет пропущен! Фаза боя активирована.</green>"));
                } else if (gameManager.getCurrentState() instanceof ActiveState activeState) {
                    activeState.checkEndCondition();
                    sender.sendMessage(MINI_MESSAGE.deserialize("<yellow>Принудительная проверка условий завершения выполнена.</yellow>"));
                } else {
                    sender.sendMessage(MINI_MESSAGE.deserialize("<red>Команда недоступна в текущем состоянии: " + gameManager.getCurrentState().getName() + "</red>"));
                }
                return true;
            }
            case "status" -> {
                sender.sendMessage(MINI_MESSAGE.deserialize(
                        "<gold>=== Статус Pillars of Fortune ===</gold>\n" +
                        "<yellow>Текущее состояние: <white>" + gameManager.getCurrentState().getName() + "</white></yellow>\n" +
                        "<yellow>Всего игроков: <white>" + gameManager.getPlayerRegistry().getTotalCount() + "</white></yellow>\n" +
                        "<yellow>Живых игроков: <white>" + gameManager.getPlayerRegistry().getAliveCount() + "</white></yellow>"
                ));
                return true;
            }
            default -> {
                sender.sendMessage(MINI_MESSAGE.deserialize("<red>Неизвестная подкоманда. Используйте /pillars <start|stop|forcenext|status></red>"));
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(List.of("start", "stop", "forcenext", "status"));
            completions.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return completions;
        }
        return List.of();
    }
}
