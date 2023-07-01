package retroa.MusicPlayer.gui;

import java.util.List;

public class PlaylistInfo {
    public String name;
    public List<String> songs;

    public PlaylistInfo(String name, List<String> songs) {
        this.name = name;
        this.songs = songs;
    }
}
