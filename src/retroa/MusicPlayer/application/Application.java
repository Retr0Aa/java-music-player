package retroa.MusicPlayer.application;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.*;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import retroa.MusicPlayer.gui.MediaPlayerGUI;
import retroa.MusicPlayer.player.MusicPlayer;

public class Application {
	
	public static void main(String[] args) { new Application(); }
	
	Application() {

		if (!new File(System.getProperty("user.dir") + "/playlists").exists()) {
			try {
				Files.createDirectory(Path.of(System.getProperty("user.dir") + "/playlists"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		
		MusicPlayer musicPlayer = new MusicPlayer();
		MediaPlayerGUI mpg = new MediaPlayerGUI(musicPlayer);
		
		mpg.loadGUI();
		mpg.setupJFrameProperties(400, 700, "Retr0A's Media Player");
	}
}
