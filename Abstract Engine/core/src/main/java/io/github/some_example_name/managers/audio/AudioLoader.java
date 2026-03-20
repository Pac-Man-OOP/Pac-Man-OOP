package io.github.some_example_name.managers.audio;

import com.badlogic.gdx.files.FileHandle;

@FunctionalInterface
public interface AudioLoader<T> {

    T load(FileHandle fileHandle);
}
