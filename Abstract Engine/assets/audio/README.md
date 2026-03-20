Place audio files in this folder using supported libGDX audio formats such as OGG (`.ogg`), MP3 (`.mp3`), or WAV (`.wav`).

The audio manager can try multiple candidate files for the same audio ID in order, so a game module can prefer one format and fall back to another when needed.

Expected files:
- `menu_navigate.ogg`
- `border_collision.ogg`
- `player_collision.ogg`
- `menu_bgm.ogg`
- `game_bgm.ogg`
- `pause_bgm.ogg`
- `game_over_bgm.ogg`

All paths are resolved relative to `assets/`.
