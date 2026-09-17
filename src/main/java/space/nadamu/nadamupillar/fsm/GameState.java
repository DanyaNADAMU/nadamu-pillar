package space.nadamu.nadamupillar.fsm;

public interface GameState {
    void onEnter();
    void onTick(int currentTick);
    void onExit();
    boolean canPvp();
    boolean canBreakBlocks();
    String getName();
}
