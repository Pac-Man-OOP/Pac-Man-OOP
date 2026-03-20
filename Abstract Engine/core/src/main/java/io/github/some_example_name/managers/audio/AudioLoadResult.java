package io.github.some_example_name.managers.audio;

public final class AudioLoadResult<T> {

    private final String resolvedPath;
    private final T asset;

    public AudioLoadResult(String resolvedPath, T asset) {
        this.resolvedPath = resolvedPath;
        this.asset = asset;
    }

    public String getResolvedPath() {
        return resolvedPath;
    }

    public T getAsset() {
        return asset;
    }
}
