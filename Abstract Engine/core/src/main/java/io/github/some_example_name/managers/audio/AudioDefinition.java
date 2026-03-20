package io.github.some_example_name.managers.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AudioDefinition {

    private final String id;
    private final AudioAssetType assetType;
    private final List<String> candidatePaths;

    private AudioDefinition(String id, AudioAssetType assetType, List<String> candidatePaths) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Audio id cannot be null or blank.");
        }
        if (assetType == null) {
            throw new IllegalArgumentException("Audio asset type cannot be null.");
        }
        if (candidatePaths == null || candidatePaths.isEmpty()) {
            throw new IllegalArgumentException("At least one audio candidate path is required.");
        }

        this.id = id;
        this.assetType = assetType;
        this.candidatePaths = Collections.unmodifiableList(new ArrayList<>(candidatePaths));
    }

    public static AudioDefinition sound(String id, String primaryPath, String... fallbackPaths) {
        return new AudioDefinition(id, AudioAssetType.SOUND, buildCandidatePaths(primaryPath, fallbackPaths));
    }

    public static AudioDefinition music(String id, String primaryPath, String... fallbackPaths) {
        return new AudioDefinition(id, AudioAssetType.MUSIC, buildCandidatePaths(primaryPath, fallbackPaths));
    }

    public String getId() {
        return id;
    }

    public AudioAssetType getAssetType() {
        return assetType;
    }

    public List<String> getCandidatePaths() {
        return candidatePaths;
    }

    private static List<String> buildCandidatePaths(String primaryPath, String... fallbackPaths) {
        List<String> candidatePaths = new ArrayList<>();
        addCandidatePath(candidatePaths, primaryPath);
        if (fallbackPaths != null) {
            for (String fallbackPath : fallbackPaths) {
                addCandidatePath(candidatePaths, fallbackPath);
            }
        }
        return candidatePaths;
    }

    private static void addCandidatePath(List<String> candidatePaths, String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Audio path cannot be null or blank.");
        }
        candidatePaths.add(path);
    }
}
