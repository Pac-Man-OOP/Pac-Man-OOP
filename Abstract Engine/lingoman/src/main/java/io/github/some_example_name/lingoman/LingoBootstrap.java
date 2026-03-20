package io.github.some_example_name.lingoman;

import com.badlogic.gdx.Input;

import io.github.some_example_name.EngineBootstrap;
import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.graphics.LingoSprites;
import io.github.some_example_name.lingoman.scenes.GameOverScene;
import io.github.some_example_name.lingoman.scenes.GameScene;
import io.github.some_example_name.lingoman.scenes.FoundWordsScene;
import io.github.some_example_name.lingoman.scenes.MenuScene;
import io.github.some_example_name.lingoman.scenes.PauseScene;
import io.github.some_example_name.lingoman.scenes.SettingsScene;
import io.github.some_example_name.managers.AudioManager;
import io.github.some_example_name.managers.InputManager;
import io.github.some_example_name.managers.SceneManager;

public final class LingoBootstrap implements EngineBootstrap {

    @Override
    public void initialize(EngineContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }

        configureInput(context.getInputManager());
        configureAudio(context.getAudioManager());
        configureScenes(context.getSceneManager());
    }

    private void configureInput(InputManager input) {
        input.bindAction(LingoInputActions.MENU_UP, Input.Keys.UP, Input.Keys.W);
        input.bindAction(LingoInputActions.MENU_DOWN, Input.Keys.DOWN, Input.Keys.S);
        input.bindAction(LingoInputActions.MENU_LEFT, Input.Keys.LEFT, Input.Keys.A);
        input.bindAction(LingoInputActions.MENU_RIGHT, Input.Keys.RIGHT, Input.Keys.D);
        input.bindAction(LingoInputActions.MENU_CONFIRM, Input.Keys.ENTER);

        input.bindAction(LingoInputActions.GAME_MENU, Input.Keys.M, Input.Keys.ESCAPE);
        input.bindAction(LingoInputActions.GAME_RESTART, Input.Keys.R);

        input.bindAction(LingoInputActions.MOVE_UP, Input.Keys.UP, Input.Keys.W);
        input.bindAction(LingoInputActions.MOVE_DOWN, Input.Keys.DOWN, Input.Keys.S);
        input.bindAction(LingoInputActions.MOVE_LEFT, Input.Keys.LEFT, Input.Keys.A);
        input.bindAction(LingoInputActions.MOVE_RIGHT, Input.Keys.RIGHT, Input.Keys.D);
        input.bindAction(LingoInputActions.DASH, Input.Keys.SPACE);
    }

    private void configureScenes(SceneManager sceneManager) {
        sceneManager.registerScene(LingoSceneIds.MENU, new MenuScene());
        sceneManager.registerScene(LingoSceneIds.FOUND_WORDS, new FoundWordsScene());
        sceneManager.registerScene(LingoSceneIds.SETTINGS, new SettingsScene());
        sceneManager.registerScene(LingoSceneIds.GAME, new GameScene());
        sceneManager.registerScene(LingoSceneIds.PAUSE, new PauseScene());
        sceneManager.registerScene(LingoSceneIds.GAME_OVER, new GameOverScene());
        sceneManager.setActiveScene(LingoSceneIds.MENU);
    }

    private void configureAudio(AudioManager audio) {
        audio.loadSound(LingoAudio.SFX_COLLECT_LETTER, LingoAudio.PATH_SFX_COLLECT_LETTER);
        audio.loadSound(LingoAudio.SFX_MOVE, LingoAudio.PATH_SFX_MOVE);
        audio.loadSound(LingoAudio.SFX_GAME_OVER, LingoAudio.PATH_SFX_GAME_OVER);
        audio.loadSound(LingoAudio.SFX_VICTORY, LingoAudio.PATH_SFX_VICTORY);
        audio.loadSound(LingoAudio.SFX_HURT, LingoAudio.PATH_SFX_HURT);

        audio.loadMusic(LingoAudio.BGM_GAME, LingoAudio.PATH_BGM_GAME);
    }

    @Override
    public void dispose(EngineContext context) {
        LingoSprites.disposeAll();
    }
}
