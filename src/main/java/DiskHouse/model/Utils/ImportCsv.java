package DiskHouse.model.Utils;

import DiskHouse.model.entity.Playlist;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.*;

public class ImportCsv implements IImport {
    @Override
    public List<Playlist> importData(String filePath) throws Exception {
        Map<String, Playlist> playlistMap = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 8) continue; // skip invalid lines
                String nomPlaylist = parts[0].trim();
                String titre = parts[1].trim();
                float duree = parseFloatSafe(parts[2].trim());
                String albumTitre = parts[3].trim();
                String artistesStr = parts[4].trim();
                String coverImageURL = parts[5].trim();
                LocalDate dateAjout = parseDateSafe(parts[6].trim());
                String coverPlaylist = parts[7].trim();
                // Artistes
                List<Artiste> artistes = new ArrayList<>();
                if (!artistesStr.isEmpty()) {
                    for (String pseudo : artistesStr.split(";")) {
                        artistes.add(new Artiste(pseudo.trim(), null));
                    }
                }
                Album album = albumTitre.isEmpty() ? null : new Album(albumTitre, null, null);
                Musique musique = new Musique(titre, duree, album, artistes, coverImageURL);
                // Playlist
                Playlist playlist = playlistMap.get(nomPlaylist);
                if (playlist == null) {
                    playlist = new Playlist(nomPlaylist, new ArrayList<>());
                    playlist.setCoverImageURL(coverPlaylist);
                    playlistMap.put(nomPlaylist, playlist);
                }
                playlist.getMusiques().add(musique);
            }
        }
        return new ArrayList<>(playlistMap.values());
    }
    private float parseFloatSafe(String s) {
        try { return Float.parseFloat(s); } catch (Exception e) { return 0f; }
    }
    private LocalDate parseDateSafe(String s) {
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }
}
