package io.github.some_example_name.lingoman.managers;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.LingoAudio;

public final class LingoGameplayAudioManager {

    private boolean moveLoopPlaying;

    public void enterGameplay(EngineContext context) {
        moveLoopPlaying = false;
        if (context != null) {
            context.getAudioManager().playMusic(LingoAudio.BGM_GAME, true);
        }
    }

    public void exitGameplay(EngineContext context) {
        stopGameplayAudio(context);
    }

    public void updateMovementLoop(EngineContext context, boolean shouldPlayMoveLoop) {
        if (context == null || shouldPlayMoveLoop == moveLoopPlaying) {
            return;
        }

        moveLoopPlaying = shouldPlayMoveLoop;
        if (moveLoopPlaying) {
            context.getAudioManager().playLoopingSound(LingoAudio.SFX_MOVE);
        } else {
            context.getAudioManager().stopLoopingSound(LingoAudio.SFX_MOVE);
        }
    }

    public void stopGameplayAudio(EngineContext context) {
        stopMovementLoop(context);
        if (context != null) {
            context.getAudioManager().stopMusic();
        }
    }

    private void stopMovementLoop(EngineContext context) {
        moveLoopPlaying = false;
        if (context != null) {
            context.getAudioManager().stopLoopingSound(LingoAudio.SFX_MOVE);
        }
    }
}
