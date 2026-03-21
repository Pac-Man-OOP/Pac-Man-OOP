package io.github.some_example_name.managers.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

/**
 * Strategy contract for loading audio assets by file type.
 */
public interface AudioFileTypeLoader {

    Sound loadSound(FileHandle fileHandle);

    Music loadMusic(FileHandle fileHandle);
}
