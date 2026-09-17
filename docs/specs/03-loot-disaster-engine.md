# Спецификация 3: Loot & Disaster Engine

## 1. Интерфейс катастрофы
```java
package space.nadamu.nadamupillar.disaster;

import java.util.Set;
import org.bukkit.World.Environment;

public interface Disaster {
    String getId();
    Set<Environment> getAllowedEnvironments();
    int getWarningDurationSeconds();
    void execute(DisasterContext context);
}
```

## 2. Планировщик действий (ActionScheduler)
- Запускается при входе в состояние `ACTIVE`.
- Таймер лута: каждые $T_{\text{loot}}$ секунд (по умолчанию 10 сек) выдает всем живым игрокам случайный предмет из `WeightedLootTable`.
- Таймер катастроф: каждые $T_{\text{disaster}}$ секунд (по умолчанию 30 сек) выбирает доступную катастрофу для текущего измерения, транслирует предупреждение в чат/ActionBar за $N$ секунд до удара и активирует катастрофу.

## 3. Базовый пул катастроф
1. **Overworld**:
   - `MeteorShowerDisaster`: спавн активированных TNT с разлетом над ареной.
   - `AnvilRainDisaster`: спавн падающих наковален на высоте $Y + 15$ над каждым живым игроком.
2. **Nether**:
   - `GhastAssaultDisaster`: призыв агрессивных гастов вокруг столбов.
   - `RisingLavaDisaster`: временное появление лавового слоя под столбами.
3. **The End**:
   - `ShulkerLevitationDisaster`: наложение эффекта левитации на игроков и спавн снарядов шалкеров.
