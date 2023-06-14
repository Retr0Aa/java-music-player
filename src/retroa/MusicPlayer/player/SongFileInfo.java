package retroa.MusicPlayer.player;

public class SongFileInfo {
	public String songName;
	public String songAlbum;
	public String songArtist;
	public String songGenre;
	
	public String songPath;
	
	public SongFileInfo(String songName, String songAlbum, String songArtist, String songGenre, String songPath) {
		super();
		this.songName = songName;
		this.songAlbum = songAlbum;
		this.songArtist = songArtist;
		this.songGenre = songGenre;
		
		this.songPath = songPath;
	}
}
