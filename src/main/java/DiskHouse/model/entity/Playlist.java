package DiskHouse.model.entity;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Playlist extends Identifier {
    private String nomPlaylist;
    private List<Musique> musiques;
    private int nombreMusique;

    // Image de couverture
    private String coverImageURL; // URL (toExternalForm) de l'image de couverture

    // ?-------------------------------*/
    // ? -------- Constructeur --------*/
    // ? ------------------------------*/
    public Playlist(String nomPlaylist, List<Musique> musiques) {
        super(); // init ID via la classe parente
        this.nomPlaylist = nomPlaylist;
        this.musiques = (musiques != null) ? new ArrayList<>(musiques) : new ArrayList<>();
        this.nombreMusique = this.musiques.size();

        // image par défaut
        this.coverImageURL = resolveDefaultImage();
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
    public String getNomPlaylist() { return nomPlaylist; }
    public List<Musique> getMusiques() { return musiques; }
    public int getNombreMusique() { return nombreMusique; }
    public String getCoverImageURL() { return coverImageURL; }

    // ?-------------------------------*/
    // ? --------    SETTERS   --------*/
    // ? ------------------------------*/
    public void setNomPlaylist(String nomPlaylist) {
        this.nomPlaylist = nomPlaylist;
    }

    public void setMusiques(List<Musique> musiques) {
        this.musiques = (musiques != null) ? new ArrayList<>(musiques) : new ArrayList<>();
        this.nombreMusique = this.musiques.size();
    }

    /** Si url == null ou vide → remet l’image par défaut. */
    public void setCoverImageURL(String coverImageURL) {
        if (coverImageURL == null || coverImageURL.isBlank()) {
            this.coverImageURL = resolveDefaultImage();
        } else {
            this.coverImageURL = coverImageURL;
        }
    }

    // On évite un setter public direct sur nombreMusique : la valeur est dérivée de la liste.
    private void setNombreMusiqueInternal(int count) {
        this.nombreMusique = Math.max(count, 0);
    }

    // ?-------------------------------*/
    // ? --------    UTILS     --------*/
    // ? ------------------------------*/
    public void ajouterMusique(Musique m) {
        if (m == null) return;
        this.musiques.add(m);
        setNombreMusiqueInternal(this.musiques.size());
    }

    public boolean retirerMusique(Musique m) {
        if (m == null) return false;
        boolean removed = this.musiques.remove(m);
        if (removed) setNombreMusiqueInternal(this.musiques.size());
        return removed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Playlist [\n");
        sb.append("  Id      : ").append(getId()).append("\n");
        sb.append("  Nom     : ").append(nomPlaylist != null ? nomPlaylist : "inconnue").append("\n");
        sb.append("  Nombre  : ").append(nombreMusique).append("\n");
        sb.append("  Image   : ").append(coverImageURL != null ? coverImageURL : "").append("\n");
        sb.append("  Musiques : \n");
        if (musiques != null && !musiques.isEmpty()) {
            for (Musique m : musiques) {
                sb.append("   - ").append(m != null ? m.getTitre() : "(null)").append("\n");
            }
        } else {
            sb.append("   Aucune musique\n");
        }
        sb.append("]");
        return sb.toString();
    }

    // --- equals / hashCode (basés sur le nom, à ajuster si besoin) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Playlist)) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(nomPlaylist, playlist.nomPlaylist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomPlaylist);
    }
}
