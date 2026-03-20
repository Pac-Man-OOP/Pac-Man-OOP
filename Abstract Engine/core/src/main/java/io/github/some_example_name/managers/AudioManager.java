package io.github.some_example_name.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import io.github.some_example_name.managers.audio.AudioDefinition;
import io.github.some_example_name.managers.audio.AudioLoadResult;
import io.github.some_example_name.managers.audio.AudioLoader;
import io.github.some_example_name.managers.audio.AudioLoaderRegistry;
import io.github.some_example_name.managers.audio.AudioAssetType;

public class AudioManager implements Disposable {

    private final Map<String, Sound> soundEffects = new HashMap<>();
    private final Map<String, Music> musicTracks = new HashMap<>();
    private final Map<String, Float> soundVolumes = new HashMap<>();
    private final Map<String, Long> loopingSoundInstances = new HashMap<>();
    private final AudioLoaderRegistry audioLoaderRegistry;

    private float masterVolume = 1f;
    private float musicVolume = 1f;
    private float soundMasterVolume = 1f;
    private boolean muted = false;
    private Music currentMusic;

    public AudioManager() {
        this(new AudioLoaderRegistry());
    }

    public AudioManager(AudioLoaderRegistry audioLoaderRegistry) {
        if (audioLoaderRegistry == null) {
            throw new IllegalArgumentException("audioLoaderRegistry cannot be null");
        }
        this.audioLoaderRegistry = audioLoaderRegistry;
    }

    public void loadSound(String id, String path) {
        loadSound(AudioDefinition.sound(id, path));
    }

    public void loadMusic(String id, String path) {
        loadMusic(AudioDefinition.music(id, path));
    }

    public void loadSound(String id, String primaryPath, String... fallbackPaths) {
        loadSound(AudioDefinition.sound(id, primaryPath, fallbackPaths));
    }

    public void loadMusic(String id, String primaryPath, String... fallbackPaths) {
        loadMusic(AudioDefinition.music(id, primaryPath, fallbackPaths));
    }

    public void registerSoundLoader(String extension, AudioLoader<Sound> loader) {
        audioLoaderRegistry.register(AudioAssetType.SOUND, extension, loader);
    }

    public void registerMusicLoader(String extension, AudioLoader<Music> loader) {
        audioLoaderRegistry.register(AudioAssetType.MUSIC, extension, loader);
    }

    public void playSound(String id, boolean loop) {
        Sound sound = soundEffects.get(id);
        if (sound == null) {
            return;
        }

        long instanceId = sound.play(resolveSoundVolume(id));
        if (loop) {
            sound.setLooping(instanceId, true);
        }
    }

    public void playLoopingSound(String id) {
        Sound sound = soundEffects.get(id);
        if (sound == null || loopingSoundInstances.containsKey(id)) {
            return;
        }

        long instanceId = sound.loop(resolveSoundVolume(id));
        loopingSoundInstances.put(id, instanceId);
    }

    public void stopLoopingSound(String id) {
        if (id == null || id.isBlank()) {
            return;
        }

        Sound sound = soundEffects.get(id);
        Long instanceId = loopingSoundInstances.remove(id);
        if (sound == null || instanceId == null) {
            return;
        }

        sound.stop(instanceId);
    }

    public void playMusic(String id, boolean loop) {
        Music nextMusic = musicTracks.get(id);
        if (nextMusic == null) {
            return;
        }

        if (currentMusic != null && currentMusic != nextMusic) {
            currentMusic.stop();
        }

        currentMusic = nextMusic;
        currentMusic.setLooping(loop);
        currentMusic.setVolume(resolveMusicVolume());
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public void setMusicVolume(float volume) {
        musicVolume = MathUtils.clamp(volume, 0f, 1f);
        if (currentMusic != null) {
            currentMusic.setVolume(resolveMusicVolume());
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMasterVolume(float volume) {
        masterVolume = MathUtils.clamp(volume, 0f, 1f);
        if (currentMusic != null) {
            currentMusic.setVolume(resolveMusicVolume());
        }
        refreshLoopingSoundVolumes();
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setSoundMasterVolume(float volume) {
        soundMasterVolume = MathUtils.clamp(volume, 0f, 1f);
        refreshLoopingSoundVolumes();
    }

    public float getSoundMasterVolume() {
        return soundMasterVolume;
    }

    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null) {
            currentMusic.play();
        }
    }

    public void setSoundVolume(String id, float volume) {
        if (id == null || id.isBlank()) {
            return;
        }
        soundVolumes.put(id, MathUtils.clamp(volume, 0f, 1f));
        refreshLoopingSoundVolume(id);
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (currentMusic != null) {
            currentMusic.setVolume(resolveMusicVolume());
        }
        refreshLoopingSoundVolumes();
    }

    public boolean isMuted() {
        return muted;
    }

    @Override
    public void dispose() {
        Set<Sound> sounds = new HashSet<>(soundEffects.values());
        for (Sound sound : sounds) {
            if (sound != null) {
                sound.dispose();
            }
        }
        soundEffects.clear();
        soundVolumes.clear();
        loopingSoundInstances.clear();

        Set<Music> music = new HashSet<>(musicTracks.values());
        for (Music track : music) {
            if (track != null) {
                track.dispose();
            }
        }
        musicTracks.clear();
        currentMusic = null;
    }

    private void logMissingAsset(String type, String id, List<String> candidatePaths) {
        if (Gdx.app != null) {
            Gdx.app.log("AudioManager",
                "Skipping " + type + " [" + id + "]; no supported candidate found at paths: " + String.join(", ", candidatePaths));
        }
    }

    private float resolveSoundVolume(String id) {
        return muted ? 0f : MathUtils.clamp(soundVolumes.getOrDefault(id, 1f) * soundMasterVolume * masterVolume, 0f, 1f);
    }

    private float resolveMusicVolume() {
        return muted ? 0f : MathUtils.clamp(masterVolume * musicVolume, 0f, 1f);
    }

    private void refreshLoopingSoundVolumes() {
        for (String id : loopingSoundInstances.keySet()) {
            refreshLoopingSoundVolume(id);
        }
    }

    private void refreshLoopingSoundVolume(String id) {
        Sound sound = soundEffects.get(id);
        Long instanceId = loopingSoundInstances.get(id);
        if (sound == null || instanceId == null) {
            return;
        }
        sound.setVolume(instanceId, resolveSoundVolume(id));
    }

    private void loadSound(AudioDefinition definition) {
        AudioLoadResult<?> loadResult = audioLoaderRegistry.loadFirstAvailable(definition);
        if (loadResult == null) {
            logMissingAsset("sound", definition.getId(), definition.getCandidatePaths());
            return;
        }

        Sound previous = soundEffects.put(definition.getId(), (Sound) loadResult.getAsset());
        if (previous != null) {
            Long loopingInstanceId = loopingSoundInstances.remove(definition.getId());
            if (loopingInstanceId != null) {
                previous.stop(loopingInstanceId);
            }
            previous.dispose();
        }
        soundVolumes.putIfAbsent(definition.getId(), 1f);
    }

    private void loadMusic(AudioDefinition definition) {
        AudioLoadResult<?> loadResult = audioLoaderRegistry.loadFirstAvailable(definition);
        if (loadResult == null) {
            logMissingAsset("music", definition.getId(), definition.getCandidatePaths());
            return;
        }

        Music previous = musicTracks.put(definition.getId(), (Music) loadResult.getAsset());
        if (previous != null) {
            if (currentMusic == previous) {
                currentMusic = null;
            }
            previous.dispose();
        }
    }
}
