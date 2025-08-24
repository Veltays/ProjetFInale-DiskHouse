package DiskHouse.model.Utils;

import DiskHouse.model.entity.Playlist;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportXml implements IExport {
    @Override
    public void export(Object data, String filePath) throws Exception {
        if (!(data instanceof List<?>)) {
            throw new IllegalArgumentException("Les données à exporter doivent être une liste de playlists.");
        }
        List<?> list = (List<?>) data;
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<playlists>\n");
        for (Object obj : list) {
            if (!(obj instanceof Playlist)) continue;
            Playlist playlist = (Playlist) obj;
            sb.append("  <playlist>\n");
            sb.append("    <nomPlaylist>").append(escapeXml(playlist.getNomPlaylist())).append("</nomPlaylist>\n");
            sb.append("    <nombreMusique>").append(playlist.getNombreMusique()).append("</nombreMusique>\n");
            sb.append("    <coverImageURL>").append(escapeXml(playlist.getCoverImageURL())).append("</coverImageURL>\n");
            sb.append("    <musiques>\n");
            if (playlist.getMusiques() != null) {
                for (Musique musique : playlist.getMusiques()) {
                    sb.append("      <musique>\n");
                    sb.append("        <titre>").append(escapeXml(musique.getTitre())).append("</titre>\n");
                    sb.append("        <duree>").append(musique.getDuree()).append("</duree>\n");
                    sb.append("        <album>");
                    Album album = musique.getAlbum();
                    sb.append(album != null ? escapeXml(album.getTitreAlbum()) : "");
                    sb.append("</album>\n");
                    sb.append("        <coverImageURL>").append(escapeXml(musique.getCoverImageURL())).append("</coverImageURL>\n");
                    sb.append("        <dateAjout>").append(musique.getDateAjout() != null ? musique.getDateAjout().format(DateTimeFormatter.ISO_DATE) : "").append("</dateAjout>\n");
                    sb.append("        <artistes>\n");
                    if (musique.getArtistes() != null) {
                        for (Artiste artiste : musique.getArtistes()) {
                            sb.append("          <artiste>").append(escapeXml(artiste.getPseudo())).append("</artiste>\n");
                        }
                    }
                    sb.append("        </artistes>\n");
                    sb.append("      </musique>\n");
                }
            }
            sb.append("    </musiques>\n");
            sb.append("  </playlist>\n");
        }
        sb.append("</playlists>\n");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(sb.toString());
        }
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
