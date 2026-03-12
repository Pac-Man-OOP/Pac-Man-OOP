package io.github.some_example_name.lingoman;

import com.badlogic.gdx.Input;

import io.github.some_example_name.EngineBootstrap;
import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.scenes.GameOverScene;
import io.github.some_example_name.lingoman.scenes.GameScene;
import io.github.some_example_name.lingoman.scenes.MenuScene;
import io.github.some_example_name.managers.InputManager;
import io.github.some_example_name.managers.SceneManager;

public final class LingoBootstrap implements EngineBootstrap {

    @Override
    public void initialize(EngineContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }

        context.getSaveManager().register(LingoSession.get());
        context.getSaveManager().load(LingoSession.SAVE_FILE);
        configureInput(context.getInputManager());
        configureScenes(context.getSceneManager());
    }

    @Override
    public void dispose(EngineContext context) {
        if (context == null) {
            return;
        }
        context.getSaveManager().save(LingoSession.SAVE_FILE);
        context.getSaveManager().unregister(LingoSession.get().getSaveId());
    }

    private void configureInput(InputManager input) {
        input.bindAction(LingoInputActions.MENU_UP, Input.Keys.UP, Input.Keys.W);
        input.bindAction(LingoInputActions.MENU_DOWN, Input.Keys.DOWN, Input.Keys.S);
        input.bindAction(LingoInputActions.MENU_CONFIRM, Input.Keys.ENTER);

        input.bindAction(LingoInputActions.GAME_MENU, Input.Keys.M, Input.Keys.ESCAPE);
        input.bindAction(LingoInputActions.GAME_RESTART, Input.Keys.R);

        input.bindAction(LingoInputActions.MOVE_UP, Input.Keys.UP, Input.Keys.W);
        input.bindAction(LingoInputActions.MOVE_DOWN, Input.Keys.DOWN, Input.Keys.S);
        input.bindAction(LingoInputActions.MOVE_LEFT, Input.Keys.LEFT, Input.Keys.A);
        input.bindAction(LingoInputActions.MOVE_RIGHT, Input.Keys.RIGHT, Input.Keys.D);
    }

    private void configureScenes(SceneManager sceneManager) {
        sceneManager.registerScene(LingoSceneIds.MENU, new MenuScene());
        sceneManager.registerScene(LingoSceneIds.GAME, new GameScene());
        sceneManager.registerScene(LingoSceneIds.GAME_OVER, new GameOverScene());
        sceneManager.setActiveScene(LingoSceneIds.MENU);
    }
}
