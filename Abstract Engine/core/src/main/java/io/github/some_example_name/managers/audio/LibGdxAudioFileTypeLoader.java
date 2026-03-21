package io.github.some_example_name.managers.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

/**
 * Default loader backed by libGDX audio APIs.
 */
public final class LibGdxAudioFileTypeLoader implements AudioFileTypeLoader {

    @Override
    public Sound loadSound(FileHandle fileHandle) {
        return Gdx.audio.newSound(fileHandle);
    }

    @Override
    public Music loadMusic(FileHandle fileHandle) {
        return Gdx.audio.newMusic(fileHandle);
    }
}
