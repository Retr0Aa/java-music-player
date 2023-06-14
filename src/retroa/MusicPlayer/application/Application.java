package retroa.MusicPlayer.application;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import retroa.MusicPlayer.gui.MediaPlayerGUI;
import retroa.MusicPlayer.player.MusicPlayer;

public class Application {
	
	public static void main(String[] args) { new Application(); }
	
	Application() {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		
		MusicPlayer musicPlayer = new MusicPlayer();
		MediaPlayerGUI mpg = new MediaPlayerGUI(musicPlayer);
		
		mpg.loadGUI();
		mpg.setupJFrameProperties(400, 700, "Retr0A's Media Player");
	}
}
