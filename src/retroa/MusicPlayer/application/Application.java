package retroa.MusicPlayer.application;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import retroa.MusicPlayer.player.MusicPlayer;

public class Application {
	
	public static void main(String[] args) { new Application(); }
	
	Application() {
		MusicPlayer musicPlayer = new MusicPlayer();
		
		musicPlayer.playSong("/Users/alexander/Music/Music/MyMusic/Ice Cube - You Know How We Do It (Official Music Video).mp3");
	}
}
