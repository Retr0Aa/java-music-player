package retroa.MusicPlayer.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import javazoom.jl.decoder.JavaLayerException;
import org.jetbrains.annotations.NotNull;
import retroa.MusicPlayer.application.PlaylistJSONManager;
import retroa.MusicPlayer.player.MusicPlayer;
import retroa.MusicPlayer.player.SongFileInfo;

public class MediaPlayerGUI extends JFrame implements ActionListener {
	@Serial
	private static final long serialVersionUID = 1L;

	private MusicPlayer musicPlayer;

	JMenuBar menuBar;
	JPanel controlsPanel;
	JButton playPauseButton;
	JButton stopSongButton;
	JTextField searchTextField;
	JTable musicFoundTable;
	JPanel musicPanel;
	JScrollPane songsTableScrollPane;
	JTree playlistsTree;
	JScrollPane playlistsScrollPane;

	String musicFolderPath = "/Users/alexander/Music/Music/MyMusic";

	String selectedSongPath = "/Users/alexander/Music/Music/MyMusic/Ice Cube - You Know How We Do It (Official Music Video).mp3";

	public ArrayList<SongFileInfo> songFileInfos = new ArrayList<SongFileInfo>();
	private DefaultTableModel songsTableModel;

	private ArrayList<PlaylistInfo> loadedPlaylists;


	public MediaPlayerGUI(MusicPlayer musicPlayer) {
		this.musicPlayer = musicPlayer;

		loadedPlaylists = PlaylistJSONManager.LoadAllPlaylists();

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

							songFileInfos.add(new SongFileInfo(songName != null ? songName : file.getName(),
									album != null ? album : "", artist != null ? artist : "", genre != null ? genre : "",
									file.getPath()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		String[] columnNames = {"Song Name", "Album", "Artist", "Genre"};
		songsTableModel = new DefaultTableModel(columnNames, 0) {
			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		RefreshSongsTable();
	}

	public void RefreshSongsTable() {
		for (int i = 0; i < songsTableModel.getRowCount(); i++) {
			songsTableModel.removeRow(i);
		}

		for (SongFileInfo fileInfo : songFileInfos) {
			Object[] rowData = {fileInfo.songName, fileInfo.songAlbum, fileInfo.songArtist, fileInfo.songGenre};

			songsTableModel.addRow(rowData);
		}
	}

	public void loadGUI() {
		menuBar = new JMenuBar();
		controlsPanel = new JPanel();
		playPauseButton = new JButton("Play");
		stopSongButton = new JButton("Stop");
		searchTextField = new JTextField("");
		musicFoundTable = new JTable(songsTableModel);
		musicPanel = new JPanel(new BorderLayout());
		songsTableScrollPane = new JScrollPane(musicPanel);
		playlistsTree = new JTree();
		playlistsScrollPane = new JScrollPane(playlistsTree);

		playlistsTree.setModel(new DefaultTreeModel(getPlaylistsTreeRoot()));

		// Create toolbar components
		JMenu fileMenu = new JMenu("File");
		JMenuItem setMusicFolderMenuItem = new JMenuItem("Set Music Folder");

		setMusicFolderMenuItem.setActionCommand("menu_setMusicFolder");
		setMusicFolderMenuItem.addActionListener(this);

		stopSongButton.setActionCommand("stop_current_song");
		playPauseButton.setActionCommand("play_current_song");
		playPauseButton.addActionListener(this);
		stopSongButton.addActionListener(this);

		musicFoundTable.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Check if the event is a double-click with the left button
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					// Get the selected row index
					int row = musicFoundTable.getSelectedRow();

					// Get the data from the selected row
					selectedSongPath = songFileInfos.get(row).songPath;

					playPauseButton.setText("Pause");
					playPauseButton.setActionCommand("pause_current_song");
					playPauseButton.setEnabled(true);

					if (musicPlayer == null) {
						musicPlayer = new MusicPlayer();
					}

					try {
						musicPlayer.setSongFilePath(selectedSongPath);
						musicPlayer.play();

						stopSongButton.setEnabled(true);
					} catch (JavaLayerException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		playlistsTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					TreePath path = playlistsTree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
						DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
						if (parentNode != null) {
							DefaultMutableTreeNode grandparentNode = (DefaultMutableTreeNode) parentNode.getParent();
							if (grandparentNode != null) {
								int parentIndex = grandparentNode.getIndex(parentNode);

								String selectedSongPath = loadedPlaylists.get(parentIndex).songs.get(parentNode.getIndex(selectedNode));

								playPauseButton.setText("Pause");
								playPauseButton.setActionCommand("pause_current_song");
								playPauseButton.setEnabled(true);

								if (musicPlayer == null) {
									musicPlayer = new MusicPlayer();
								}

								try {
									musicPlayer.setSongFilePath(selectedSongPath);
									musicPlayer.play();

									stopSongButton.setEnabled(true);
								} catch (JavaLayerException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}

				if (SwingUtilities.isRightMouseButton(e) && !e.isConsumed()) {
					// Get the selected node
					TreePath path = playlistsTree.getPathForLocation(e.getX(), e.getY());
					playlistsTree.setSelectionPath(path);

					// Create and show the context menu
					JPopupMenu popupMenu = createPlaylistTreeContextMenu();
					if (popupMenu != null) {
						popupMenu.show(playlistsTree, e.getX(), e.getY());
					}
				}

				/*if (e.getClickCount() >= 2) {

					// Get the selected row index
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) playlistsTree.getLastSelectedPathComponent();
					if (selectedNode != null) {

						// Get the data from the selected row
						selectedSongPath = songFileInfos.get(selectedNode.getIndex(playlistsTree.getModel().)).songPath;

						playPauseButton.setText("Pause");
						playPauseButton.setActionCommand("pause_current_song");
						playPauseButton.setEnabled(true);

						if (musicPlayer == null) {
							musicPlayer = new MusicPlayer();
						}

						try {
							musicPlayer.setSongFilePath(selectedSongPath);
							musicPlayer.play();

							stopSongButton.setEnabled(true);
						} catch (JavaLayerException e1) {
							e1.printStackTrace();
						}
					}
				}*/
			}
		});

		musicFoundTable.setModel(songsTableModel);

		fileMenu.add(setMusicFolderMenuItem);

		menuBar.add(fileMenu);

		controlsPanel.add(playPauseButton, BorderLayout.CENTER);
		controlsPanel.add(stopSongButton, BorderLayout.SOUTH);

		musicPanel.add(searchTextField, BorderLayout.NORTH);
		musicPanel.add(musicFoundTable, BorderLayout.CENTER);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Songs", songsTableScrollPane);
		tabbedPane.addTab("Playlists", playlistsScrollPane);

		this.setJMenuBar(menuBar);

		this.add(controlsPanel, BorderLayout.NORTH);
		this.add(tabbedPane, BorderLayout.CENTER);
	}

	@NotNull
	private DefaultMutableTreeNode getPlaylistsTreeRoot() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Playlists");

		// Add loaded playlists as child nodes to the root
		for (PlaylistInfo playlistInfo : loadedPlaylists) {
			DefaultMutableTreeNode playlistNode = new DefaultMutableTreeNode(playlistInfo.name);
			root.add(playlistNode);

			for (String song : playlistInfo.songs) {
				try (FileInputStream inputStream = new FileInputStream(song)) {
					Parser parser = new AutoDetectParser();
					BodyContentHandler handler = new BodyContentHandler();
					Metadata metadata = new Metadata();
					ParseContext context = new ParseContext();

					parser.parse(inputStream, handler, metadata, context);

					String songName = metadata.get("dc:title") == null ? song : metadata.get("dc:title");

					String formattedText = songName + " (" + song + ')';

					playlistNode.add(new DefaultMutableTreeNode(formattedText));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return root;
	}

	private JPopupMenu createPlaylistTreeContextMenu() {

		// Customize the content of the context menu based on the selected node
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) playlistsTree.getLastSelectedPathComponent();
		if (selectedNode != null) {
			if (selectedNode.getParent() == playlistsTree.getModel().getRoot() ||
				selectedNode.isRoot()) {
				JPopupMenu popupMenu = new JPopupMenu();

				JMenuItem menuItem = new JMenuItem(selectedNode.isRoot() ? "Create Playlist" : "Add Song To Playlist");

				if (selectedNode.getParent() == playlistsTree.getModel().getRoot()) {
					JMenuItem secondMenuItem = new JMenuItem("Delete Playlist");

					secondMenuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							int canDeletePlaylist = JOptionPane.showConfirmDialog(null,
									"Are you sure you want to delete playlist " + selectedNode.toString() + "?"
							);

							if (canDeletePlaylist == JOptionPane.YES_OPTION) {
								File fileToDelete = new File(System.getProperty("user.dir") + "/playlists/" + selectedNode.toString() + ".playlist");
								fileToDelete.delete();

								loadedPlaylists = PlaylistJSONManager.LoadAllPlaylists();
								playlistsTree.setModel(new DefaultTreeModel(getPlaylistsTreeRoot()));
							}
						}
					});

					popupMenu.add(secondMenuItem);
				}
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (selectedNode.isRoot()) {
							String newPlaylistName = JOptionPane.showInputDialog("Enter Playlist Name");

							if (newPlaylistName != null) {
								if (!newPlaylistName.isBlank()) {
									PlaylistJSONManager.SaveNewPlaylist(new PlaylistInfo(newPlaylistName, new ArrayList<>()));

									loadedPlaylists = PlaylistJSONManager.LoadAllPlaylists();
									playlistsTree.setModel(new DefaultTreeModel(getPlaylistsTreeRoot()));
								}
							}
						}

						if (selectedNode.getParent() == playlistsTree.getModel().getRoot()) {
							// TODO: Add Song To Playlist

							List<String> options = new ArrayList<>();

							for (SongFileInfo songFileInfo : songFileInfos) {
								options.add(songFileInfo.songName);
							}

							// Display the JOptionPane with a JComboBox
							String selectedOption = (String) JOptionPane.showInputDialog(
									null,
									"Select Song:",
									"Option Selection",
									JOptionPane.QUESTION_MESSAGE,
									null,
									options.toArray(),
									options.get(0));

							// Handle the selected option
							if (selectedOption != null) {
								for(int i = 0; i < loadedPlaylists.size(); i++) {
									if (loadedPlaylists.get(i).name == selectedNode.toString()) {
										for (SongFileInfo songFileInfo : songFileInfos) {
											if (songFileInfo.songName == selectedOption) {
												loadedPlaylists.get(i).songs.add(songFileInfo.songPath);
											}
										}

										PlaylistJSONManager.SaveNewPlaylist(loadedPlaylists.get(i));
									}
								}

								loadedPlaylists = PlaylistJSONManager.LoadAllPlaylists();
								playlistsTree.setModel(new DefaultTreeModel(getPlaylistsTreeRoot()));
							} else {
								System.out.println("No option selected");
							}
						}
					}
				});

				popupMenu.add(menuItem);

				return popupMenu;
			}
			if (selectedNode.getLevel() + 1 == 3) {
				JPopupMenu popupMenu = new JPopupMenu();

				JMenuItem menuItem = new JMenuItem("Remove Song From Playlist");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
							int canRemoveSong = JOptionPane.showConfirmDialog(null,
									"Are you sure you want to remove " +  songFileInfos.get(selectedNode.getParent().getIndex(selectedNode)).songName + " from playlist " +
											selectedNode.getParent().toString() + "?"
							);

							if (canRemoveSong == JOptionPane.YES_OPTION) {
								PlaylistInfo editedPlaylistInfo = new PlaylistInfo("error", new ArrayList<>());

								for (PlaylistInfo playlistInfo : loadedPlaylists) {
									if (playlistInfo.name == selectedNode.getParent().toString()) { editedPlaylistInfo = playlistInfo; }
								}

								editedPlaylistInfo.songs.remove(selectedNode.getParent().getIndex(selectedNode));

								PlaylistJSONManager.SaveNewPlaylist(editedPlaylistInfo);

								loadedPlaylists = PlaylistJSONManager.LoadAllPlaylists();
								playlistsTree.setModel(new DefaultTreeModel(getPlaylistsTreeRoot()));
						}
					}
				});

				popupMenu.add(menuItem);

				return popupMenu;
			}
		}

		return null;
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
		if (e.getActionCommand().equals("play_current_song")) {
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
					musicPlayer.setSongFilePath(selectedSongPath);
					musicPlayer.play();

					stopSongButton.setEnabled(true);
				}
			} catch (JavaLayerException e1) {
				e1.printStackTrace();
			}
		}

		if (e.getActionCommand().equals("pause_current_song")) {
			playPauseButton.setText("Play");
			playPauseButton.setActionCommand("play_current_song");

			musicPlayer.pause();
		}

		if (e.getActionCommand().equals("stop_current_song")) {
			musicPlayer.stop();

			musicPlayer = null;

			stopSongButton.setEnabled(false);

			playPauseButton.setText("Play");
			playPauseButton.setActionCommand("play_current_song");
			playPauseButton.setEnabled(false);

			selectedSongPath = "";
		}

		if (e.getActionCommand().equals("menu_setMusicFolder")) {
			JFileChooser j = new JFileChooser();
			j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			if (j.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				musicFolderPath = j.getSelectedFile().getPath();

				songFileInfos.clear();

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
								} catch (Exception exception) {
									exception.printStackTrace();
								}
							}
						}
					}
				}

				String[] columnNames = {"Song Name", "Album", "Artist", "Genre"};
				songsTableModel = new DefaultTableModel(columnNames, 0) {
					@Serial
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

				musicFoundTable.setModel(songsTableModel);
			}
		}
	}
}
