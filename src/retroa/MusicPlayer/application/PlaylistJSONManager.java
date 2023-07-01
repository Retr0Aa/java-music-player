package retroa.MusicPlayer.application;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import retroa.MusicPlayer.gui.PlaylistInfo;
import retroa.MusicPlayer.player.SongFileInfo;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class PlaylistJSONManager {
    public static void SaveNewPlaylist(@NotNull PlaylistInfo pInfo) {
        JSONObject jo = new JSONObject();

        jo.put("name", pInfo.name);

        JSONArray m = new JSONArray();

        for (String song :
                pInfo.songs) {
            m.add(song);
        }

        jo.put("songs", m);

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(System.getProperty("user.dir") + "/playlists/" + pInfo.name + ".playlist");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.write(jo.toJSONString());

        pw.flush();
        pw.close();
    }

    public static PlaylistInfo LoadSinglePlaylist(String file) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Reader reader = new FileReader(file);

        JSONObject jsonObject = (JSONObject) parser.parse(reader);

        String name = (String) jsonObject.get("name");
        List<String> songs = new ArrayList<>();
        JSONArray songsArray = (JSONArray) jsonObject.get("songs");

        @SuppressWarnings("unchecked")
        Iterator<String> it = songsArray.iterator();
        while (it.hasNext()) {
            songs.add(it.next());
        }
        reader.close();

        return new PlaylistInfo(name, songs);
    }

    public static ArrayList<PlaylistInfo> LoadAllPlaylists() {
        File[] files = new File(System.getProperty("user.dir") + "/playlists").listFiles();

        ArrayList<PlaylistInfo> tempList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".playlist")) {
                        try {
                            tempList.add(LoadSinglePlaylist(file.getAbsolutePath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return tempList;
    }
}
