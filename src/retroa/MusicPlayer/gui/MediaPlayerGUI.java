package retroa.MusicPlayer.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import retroa.MusicPlayer.player.MusicPlayer;
import retroa.MusicPlayer.player.SongFileInfo;

public class MediaPlayerGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private MusicPlayer musicPlayer;
	
	JPanel controlsPanel;
	JButton playPauseButton;
	JButton stopSongButton;
	JTable musicFoundTable;
	JScrollPane songsTableScrollPane;
	
	String musicFolderPath = "/Users/alexander/Music/Music/MyMusic";
	
	String selectedSongPath = "/Users/alexander/Music/Music/MyMusic/Ice Cube - You Know How We Do It (Official Music Video).mp3";
	
	public ArrayList<SongFileInfo> songFileInfos = new ArrayList<SongFileInfo>();
    private DefaultTableModel songsTableModel;
	
	public MediaPlayerGUI(MusicPlayer musicPlayer) {
		this.musicPlayer = musicPlayer;
		
        File[] files = new File(musicFolderPath).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                	if (file.getName().endsWith(".mp3")) {
                		try (FileInputStream inputStream = new FileInputStream(file)) {
                            Parser parser = new AutoDetectParser();
                            BodyContentHandler handler = new BodyContentHandler();
                            Metadata metadata = new Metadata();
                            ParseContext context = new ParseContext();

                            parser.parse(inputStream, handler, metadata, context);

                            String songName = metadata.get("dc:title");
                            String artist = metadata.get("xmpDM:artist");
                            String album = metadata.get("xmpDM:album");
                            String genre = metadata.get("xmpDM:genre");
                            
                            songFileInfos.add(new SongFileInfo(songName, album, artist, genre, file.getPath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                	}
                }
            }
        }

        String[] columnNames = {"Song Name", "Album", "Artist", "Genre"};
        songsTableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        for (SongFileInfo fileInfo : songFileInfos) {
            Object[] rowData = {fileInfo.songName, fileInfo.songAlbum, fileInfo.songArtist, fileInfo.songGenre};
            songsTableModel.addRow(rowData);
        }
	}
	
	public void loadGUI() {
		controlsPanel = new JPanel();
		playPauseButton = new JButton("Play");
		stopSongButton = new JButton("Stop");
		musicFoundTable = new JTable(songsTableModel);
		songsTableScrollPane = new JScrollPane(musicFoundTable);
		
		playPauseButton.setActionCommand("play_current_song");
		stopSongButton.setActionCommand("stop_current_song");
		playPauseButton.addActionListener(this);
		stopSongButton.addActionListener(this);
        
		musicFoundTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check if the event is a double-click with left button
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    // Get the selected row index
                    int row = musicFoundTable.getSelectedRow();
                    
                    // Get the data from the selected row
                    String songPath = songFileInfos.get(row).songPath;

                    selectedSongPath = songPath;
                    
                    playPauseButton.setText("Pause");
        			playPauseButton.setActionCommand("pause_current_song");
        			playPauseButton.setEnabled(true);
        			
        			if (musicPlayer == null) {
        				musicPlayer = new MusicPlayer();
        			}
        			
        			try {
        				musicPlayer.SetSongFilePath(selectedSongPath);
        				musicPlayer.play();
        				
        				stopSongButton.setEnabled(true);
        			} catch (JavaLayerException e1) {
        				e1.printStackTrace();
        			}
                }
            }
        });
		
		controlsPanel.add(playPauseButton, BorderLayout.CENTER);
		controlsPanel.add(stopSongButton, BorderLayout.SOUTH);
		
		this.add(controlsPanel, BorderLayout.NORTH);
		this.add(songsTableScrollPane, BorderLayout.CENTER);
	}
	
	public void setupJFrameProperties(int width, int height, String title) {
		this.setSize(width, height);
		this.setTitle(title);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "play_current_song") {
			playPauseButton.setText("Pause");
			playPauseButton.setActionCommand("pause_current_song");
			
			if (musicPlayer == null) {
				musicPlayer = new MusicPlayer();
			}
			
			try {
				if (musicPlayer.isSongPlaying) {
					musicPlayer.resume();
					
					stopSongButton.setEnabled(true);
				} else {
					musicPlayer.SetSongFilePath(selectedSongPath);
					musicPlayer.play();
					
					stopSongButton.setEnabled(true);
				}
			} catch (JavaLayerException e1) {
				e1.printStackTrace();
			}
		}
		
		if (e.getActionCommand() == "pause_current_song") {
			playPauseButton.setText("Play");
			playPauseButton.setActionCommand("play_current_song");
			
			musicPlayer.pause();
		}
		
		if (e.getActionCommand() == "stop_current_song") {
			musicPlayer.stop();
			
			musicPlayer = null;
			
			stopSongButton.setEnabled(false);
			
			playPauseButton.setText("Play");
			playPauseButton.setActionCommand("play_current_song");
			playPauseButton.setEnabled(false);
			
			selectedSongPath = "";
		}
	}
}
