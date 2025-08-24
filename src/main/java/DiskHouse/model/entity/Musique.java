package DiskHouse.model.entity;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Musique extends Identifier {
    private String titre;
    private float duree;          // par défaut supposée en secondes (voir formatDuree)
    private Album album;
    private List<Artiste> artistes;

    // Image (comme pour Playlist)
    private String coverImageURL; // URL (toExternalForm) de l'image; défaut = /PP.png

    // ?-------------------------------*/
    // ? -------- Constructeur --------*/
    // ? ------------------------------*/
    public Musique(String titre, float duree, Album album, List<Artiste> artistes) {
        super();
        this.titre = titre;
        this.duree = duree;
        this.album = album;
        this.artistes = (artistes != null) ? new ArrayList<>(artistes) : new ArrayList<>();

        // image par défaut
        this.coverImageURL = resolveDefaultImage();
    }

    /** Constructeur optionnel si tu veux forcer une cover au moment de la création. */
    public Musique(String titre, float duree, Album album, List<Artiste> artistes, String coverImageURL) {
        this(titre, duree, album, artistes);
        setCoverImageURL(coverImageURL); // appliquera le fallback si null/blank
    }

    /** Essaie de charger /PP.png depuis le classpath, sinon renvoie "" pour éviter les NPE. */
    private String resolveDefaultImage() {
        try {
            URL url = getClass().getResource("/PP.png");
            return (url != null) ? url.toExternalForm() : "";
        } catch (Exception ex) {
            return "";
        }
    }

    // ?-------------------------------*/
    // ? --------    GETTERS   --------*/
    // ? ------------------------------*/
    public String getTitre() { return titre; }
    public float getDuree() { return duree; }
    public Album getAlbum() { return album; }
    public List<Artiste> getArtistes() { return artistes; }
    public String getCoverImageURL() { return coverImageURL; }

    // ?-------------------------------*/
    // ? --------    SETTERS   --------*/
    // ? ------------------------------*/
    public void setTitre(String titre) { this.titre = titre; }

    /** Si tu veux autoriser 0 ou négatif, enlève le garde-fou. */
    public void setDuree(float duree) {
        if (duree > 0) this.duree = duree;
    }

    public void setAlbum(Album album) { this.album = album; }

    public void setArtistes(List<Artiste> artistes) {
        this.artistes = (artistes != null) ? new ArrayList<>(artistes) : new ArrayList<>();
    }

    /** Si url == null ou vide → remet l’image par défaut. */
    public void setCoverImageURL(String coverImageURL) {
        if (coverImageURL == null || coverImageURL.isBlank()) {
            this.coverImageURL = resolveDefaultImage();
        } else {
            this.coverImageURL = coverImageURL;
        }
    }

    // ?-------------------------------*/
    // ? --------     UTILS    --------*/
    // ? ------------------------------*/
    /** Formate la durée (en secondes) au format mm:ss. */
    private static String formatDuree(float duree) {
        int totalSeconds = Math.max(0, Math.round(duree));
        int mm = totalSeconds / 60;
        int ss = totalSeconds % 60;
        return String.format("%d:%02d", mm, ss);
        // Si ta durée est en minutes, remplace par: return String.valueOf(duree);
    }

    /** Helpers pour gérer la liste d’artistes. */
    public void ajouterArtiste(Artiste a) {
        if (a == null) return;
        this.artistes.add(a);
    }

    public boolean retirerArtiste(Artiste a) {
        if (a == null) return false;
        return this.artistes.remove(a);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Musique [\n");
        sb.append("  Id      : ").append(getId()).append("\n");
        sb.append("  Titre   : ").append(titre != null ? titre : "inconnu").append("\n");
        sb.append("  Durée   : ").append(formatDuree(duree)).append("\n");

        // format d’année comme dans Album
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy");
        String albumTxt = "inconnu";
        if (album != null) {
            String annee = (album.getDateSortie() != null) ? album.getDateSortie().format(fmt) : "inconnue";
            albumTxt = album.getTitreAlbum() + " (" + annee + ")";
        }
        sb.append("  Album   : ").append(albumTxt).append("\n");

        sb.append("  Image   : ").append(coverImageURL != null ? coverImageURL : "").append("\n");

        sb.append("  Artistes: ");
        if (artistes != null && !artistes.isEmpty()) {
            String noms = artistes.stream()
                    .map(a -> (a != null) ? a.getNom() : "inconnu")
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            sb.append(noms);
        } else {
            sb.append("aucun");
        }
        sb.append("\n]");
        return sb.toString();
    }

    // --- equals / hashCode (basés sur titre + durée + album) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Musique)) return false;
        Musique musique = (Musique) o;
        return Float.compare(musique.duree, duree) == 0
                && Objects.equals(titre, musique.titre)
                && Objects.equals(album, musique.album);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titre, duree, album);
    }
}
