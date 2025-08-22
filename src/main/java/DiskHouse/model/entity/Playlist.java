package DiskHouse.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Playlist extends Identifier {
    private String nomPlaylist;
    private List<Musique> musiques;
    private int nombreMusique;

    // ?-------------------------------*/
    // ? -------- Constructeur --------*/
    // ? ------------------------------*/
    public Playlist(String nomPlaylist, List<Musique> musiques) {
        super(); // init de l'ID via la classe parente
        this.nomPlaylist = nomPlaylist;
        // copie défensive pour éviter les effets de bord
        this.musiques = (musiques != null) ? new ArrayList<>(musiques) : new ArrayList<>();
        this.nombreMusique = this.musiques.size();
    }

    // ?-------------------------------*/
    // ? --------    GETTERS   --------*/
    // ? ------------------------------*/
    public String getNomPlaylist() {
        return nomPlaylist;
    }

    public List<Musique> getMusiques() {
        return musiques;
    }

    public int getNombreMusique() {
        return nombreMusique;
    }

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
        sb.append("  Musiques : \n");
        if (musiques != null && !musiques.isEmpty()) {
            for (Musique m : musiques) {
                sb.append("   - ").append(m.getTitre()).append("\n");
            }
        } else {
            sb.append("   Aucune musique\n");
        }
        sb.append("]");
        return sb.toString();
    }

    // --- equals / hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Playlist)) return false;
        Playlist playlist = (Playlist) o;
        // On évite la comparaison profonde de la liste
        return Objects.equals(nomPlaylist, playlist.nomPlaylist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomPlaylist);
    }
}
