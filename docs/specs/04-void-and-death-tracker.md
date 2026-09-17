# Спецификация 4: Void & Death Interceptor

## 1. Назначение
Перехват любых сценариев гибели игрока (падение в бездну, взрыв, PvP урон) с предотвращением ванильного экрана смерти (`You Died!`), мгновенным переключением в режим наблюдателя и асинхронной телепортацией.

## 2. Логика перехвата

### Перехват смертельного урона (`EntityDamageEvent`)
1. Проверяется, что сущность — `Player`, и игрок участвует в матче в роли `ALIVE`.
2. Если `player.getHealth() - event.getFinalDamage() <= 0`:
   - Отменить событие: `event.setCancelled(true)`.
   - Вызвать процедуру выбывания: `eliminatePlayer(player, DamageCause)`.

### Отслеживание порога пустоты
1. В каждом тике или событии перемещения проверяется координата $Y$.
2. Если $Y < Y_{\min}$ (по умолчанию -10):
   - Отменить вертикальный импульс (`player.setVelocity(new Vector(0, 0, 0))`).
   - Вызвать процедуру выбывания: `eliminatePlayer(player, DamageCause.VOID)`.

### Процедура выбывания (`eliminatePlayer`)
1. Восстановление здоровья игрока до максимума (`player.setHealth(player.getMaxHealth())`).
2. Очистка инвентаря (`player.getInventory().clear()`) и снятие активных зелий.
3. Смена роли в реестре: `gamePlayer.setRole(PlayerRole.SPECTATOR)`.
4. Смена игрового режима: `player.setGameMode(GameMode.SPECTATOR)`.
5. Публикация кастомного события: `Bukkit.getPluginManager().callEvent(new PlayerEliminateEvent(gamePlayer, cause))`.
6. Асинхронная телепортация: `player.teleportAsync(spectatorSpawnLocation)`.
7. Оповещение в чат через Adventure MiniMessage: `<red><bold>☠</bold> <gray>Игрок <white><player_name></white> выбыл из битвы!</gray></red>`.
8. Проверка условия окончания матча в `GameManager`.
