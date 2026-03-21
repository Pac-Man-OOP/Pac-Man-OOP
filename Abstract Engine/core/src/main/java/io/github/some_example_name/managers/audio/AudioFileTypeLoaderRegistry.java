package io.github.some_example_name.managers.audio;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AudioFileTypeLoaderRegistry {

    private static final String EXTENSION_WILDCARD = "*";

    private final Map<String, AudioFileTypeLoader> soundLoadersByExtension = new HashMap<>();
    private final Map<String, AudioFileTypeLoader> musicLoadersByExtension = new HashMap<>();

    private AudioFileTypeLoader defaultSoundLoader;
    private AudioFileTypeLoader defaultMusicLoader;

    public void registerSoundLoader(String extension, AudioFileTypeLoader loader) {
        soundLoadersByExtension.put(normalizeExtension(extension), requireLoader(loader));
    }

    public void registerMusicLoader(String extension, AudioFileTypeLoader loader) {
        musicLoadersByExtension.put(normalizeExtension(extension), requireLoader(loader));
    }

    public void registerLoaderForAll(String extension, AudioFileTypeLoader loader) {
        AudioFileTypeLoader nonNullLoader = requireLoader(loader);
        String normalizedExtension = normalizeExtension(extension);
        soundLoadersByExtension.put(normalizedExtension, nonNullLoader);
        musicLoadersByExtension.put(normalizedExtension, nonNullLoader);
    }

    public void setDefaultSoundLoader(AudioFileTypeLoader loader) {
        defaultSoundLoader = requireLoader(loader);
    }

    public void setDefaultMusicLoader(AudioFileTypeLoader loader) {
        defaultMusicLoader = requireLoader(loader);
    }

    public AudioFileTypeLoader resolveSoundLoader(String path) {
        return resolve(path, soundLoadersByExtension, defaultSoundLoader);
    }

    public AudioFileTypeLoader resolveMusicLoader(String path) {
        return resolve(path, musicLoadersByExtension, defaultMusicLoader);
    }

    private AudioFileTypeLoader resolve(String path,
            Map<String, AudioFileTypeLoader> byExtension,
            AudioFileTypeLoader defaultLoader) {
        String extension = extractExtension(path);
        AudioFileTypeLoader direct = byExtension.get(extension);
        if (direct != null) {
            return direct;
        }

        AudioFileTypeLoader wildcard = byExtension.get(EXTENSION_WILDCARD);
        if (wildcard != null) {
            return wildcard;
        }

        return defaultLoader;
    }

    private AudioFileTypeLoader requireLoader(AudioFileTypeLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("Audio loader cannot be null.");
        }
        return loader;
    }

    private String normalizeExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("Audio extension cannot be null or blank.");
        }

        String normalized = extension.trim().toLowerCase(Locale.ROOT);
        if (EXTENSION_WILDCARD.equals(normalized)) {
            return EXTENSION_WILDCARD;
        }

        if (normalized.startsWith(".")) {
            return normalized.substring(1);
        }

        return normalized;
    }

    private String extractExtension(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }

        int slashIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex <= slashIndex || dotIndex < 0 || dotIndex == path.length() - 1) {
            return "";
        }

        return path.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
