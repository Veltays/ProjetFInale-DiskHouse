package DiskHouse.model.Utils;

public class ExportCsv implements IExport {
    @Override
    public void export(Object rootObj, String filePath) throws Exception {
        if (!(rootObj instanceof DiskHouse.Controller.MainPageController root)) {
            throw new IllegalArgumentException("Le paramètre doit être un MainPageController");
        }
        var playlists = root.getPlaylistDAO().getAll();
        var musiques = root.getMusicDAO().getAll();
        var artistes = root.getArtisteDAO().getAll();
        var albums = root.getAlbumDAO().getAll();
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            // ARTISTES
            writer.println("id;pseudo;imageURL");
            for (var a : artistes) {
                writer.printf("%d;%s;%s\n", a.getId(), a.getPseudo(), a.getImageURL() == null ? "" : a.getImageURL());
            }
            writer.println();
            // ALBUMS
            writer.println("id;titreAlbum;dateSortie;coverImageURL");
            for (var al : albums) {
                writer.printf("%d;%s;%s;%s\n", al.getId(), al.getTitreAlbum(), al.getDateSortie(), al.getCoverImageURL() == null ? "" : al.getCoverImageURL());
            }
            writer.println();
            // MUSIQUES
            writer.println("id;titre;duree;albumId;coverImageURL;artistesIds");
            for (var m : musiques) {
                StringBuilder artistesIds = new StringBuilder();
                if (m.getArtistes() != null) {
                    for (var art : m.getArtistes()) {
                        if (artistesIds.length() > 0) artistesIds.append(",");
                        artistesIds.append(art.getId());
                    }
                }
                int albumId = (m.getAlbum() != null) ? m.getAlbum().getId() : -1;
                writer.printf("%d;%s;%.2f;%d;%s;%s\n", m.getId(), m.getTitre(), m.getDuree(), albumId, m.getCoverImageURL() == null ? "" : m.getCoverImageURL(), artistesIds);
            }
            writer.println();
            // PLAYLISTS
            writer.println("id;nomPlaylist;coverImageURL;musiquesIds");
            for (var p : playlists) {
                StringBuilder musiquesIds = new StringBuilder();
                if (p.getMusiques() != null) {
                    for (var mu : p.getMusiques()) {
                        if (musiquesIds.length() > 0) musiquesIds.append(",");
                        musiquesIds.append(mu.getId());
                    }
                }
                writer.printf("%d;%s;%s;%s\n", p.getId(), p.getNomPlaylist(), p.getCoverImageURL() == null ? "" : p.getCoverImageURL(), musiquesIds);
            }
        }
    }
}
