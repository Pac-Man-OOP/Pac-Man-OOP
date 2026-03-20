package io.github.some_example_name.managers.audio;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public final class AudioLoaderRegistry {

    private final Map<AudioAssetType, Map<String, AudioLoader<?>>> loadersByType = new EnumMap<>(AudioAssetType.class);

    public AudioLoaderRegistry() {
        for (AudioAssetType assetType : AudioAssetType.values()) {
            loadersByType.put(assetType, new HashMap<>());
        }
        registerDefaultLoaders();
    }

    public void register(AudioAssetType assetType, String extension, AudioLoader<?> loader) {
        if (assetType == null) {
            throw new IllegalArgumentException("Audio asset type cannot be null.");
        }
        if (loader == null) {
            throw new IllegalArgumentException("Audio loader cannot be null.");
        }

        loadersByType.get(assetType).put(normalizeExtension(extension), loader);
    }

    public AudioLoadResult<?> loadFirstAvailable(AudioDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Audio definition cannot be null.");
        }

        for (String candidatePath : definition.getCandidatePaths()) {
            String extension = extractExtension(candidatePath);
            AudioLoader<?> loader = loadersByType.get(definition.getAssetType()).get(extension);
            if (loader == null) {
                continue;
            }

            FileHandle fileHandle = Gdx.files.internal(candidatePath);
            if (!fileHandle.exists()) {
                continue;
            }

            return new AudioLoadResult<>(candidatePath, loader.load(fileHandle));
        }

        return null;
    }

    private void registerDefaultLoaders() {
        AudioLoader<Sound> soundLoader = fileHandle -> Gdx.audio.newSound(fileHandle);
        register(AudioAssetType.SOUND, "wav", soundLoader);
        register(AudioAssetType.SOUND, "mp3", soundLoader);
        register(AudioAssetType.SOUND, "ogg", soundLoader);

        AudioLoader<Music> musicLoader = fileHandle -> Gdx.audio.newMusic(fileHandle);
        register(AudioAssetType.MUSIC, "wav", musicLoader);
        register(AudioAssetType.MUSIC, "mp3", musicLoader);
        register(AudioAssetType.MUSIC, "ogg", musicLoader);
    }

    private String normalizeExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("Audio extension cannot be null or blank.");
        }
        return extension.startsWith(".")
            ? extension.substring(1).toLowerCase(Locale.ROOT)
            : extension.toLowerCase(Locale.ROOT);
    }

    private String extractExtension(String path) {
        int extensionIndex = path.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == path.length() - 1) {
            return "";
        }
        return path.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }
}
