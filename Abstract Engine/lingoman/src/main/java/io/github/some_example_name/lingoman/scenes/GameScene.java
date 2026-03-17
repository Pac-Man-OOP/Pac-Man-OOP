package io.github.some_example_name.lingoman.scenes;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSceneIds;
import io.github.some_example_name.lingoman.LingoSession;
import io.github.some_example_name.lingoman.managers.LingoGameplayAudioManager;
import io.github.some_example_name.lingoman.managers.LingoHudManager;
import io.github.some_example_name.lingoman.managers.LingoRoundManager;
import io.github.some_example_name.scenes.Scene;

public class GameScene implements Scene {

    private static final String PROFILE_FILE = "lingoman_progress.json";

    private EngineContext context;
    private final LingoRoundManager roundManager = new LingoRoundManager();
    private final LingoHudManager hudManager = new LingoHudManager();
    private final LingoGameplayAudioManager gameplayAudioManager = new LingoGameplayAudioManager();

    @Override
    public void initialize(EngineContext context) {
        this.context = context;
        roundManager.initialize(context);
    }

    @Override
    public void enter() {
        if (!LingoSession.get().consumeGameResumeRequest()) {
            roundManager.startNewRound(hudManager);
            System.out.println("[LingoMan] Game started");
        } else {
            System.out.println("[LingoMan] Game resumed");
        }
        gameplayAudioManager.enterGameplay(context);
    }

    @Override
    public void exit() {
        gameplayAudioManager.exitGameplay(context);
        System.out.println("[LingoMan] Game exit");
    }

    @Override
    public void handleInput() {
        if (context.getInputManager().isActionJustPressed(LingoInputActions.GAME_MENU)) {
            context.getSceneManager().setActiveScene(LingoSceneIds.PAUSE);
        }
    }

    @Override
    public void update(float deltaTime) {
        hudManager.update(deltaTime);

        gameplayAudioManager.updateMovementLoop(context, roundManager.isMovementInputPressed());
        LingoRoundManager.RoundOutcome outcome = roundManager.update(deltaTime, PROFILE_FILE);
        if (outcome.isTerminal()) {
            gameplayAudioManager.stopGameplayAudio(context);
            context.getSceneManager().setActiveScene(LingoSceneIds.GAME_OVER);
        }
    }

    @Override
    public void render() {
        context.getOutputManager().clearScreen(0.05f, 0.12f, 0.07f, 1f);
        roundManager.render(context.getOutputManager());
        hudManager.render(context.getOutputManager(), LingoSession.get().getGameState());
    }

    @Override
    public void dispose() {
        gameplayAudioManager.stopGameplayAudio(context);
        roundManager.dispose();
        System.out.println("[LingoMan] Game disposed");
    }
}
