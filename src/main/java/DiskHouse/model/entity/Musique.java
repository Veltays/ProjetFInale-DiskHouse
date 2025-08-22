package DiskHouse.model.entity;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Musique extends Identifier {
    private String titre;
    private float duree;          // en minutes ou secondes ? (à documenter)
    private Album album;
    private List<Artiste> artistes;

    // ?-------------------------------*/
    // ? -------- Constructeur --------*/
    // ? ------------------------------*/
    public Musique(String titre, float duree, Album album, List<Artiste> artistes) {
        super();
        this.titre = titre;
        this.duree = duree;
        this.album = album;
        // copie défensive pour éviter les effets de bord
        this.artistes = (artistes != null) ? new ArrayList<>(artistes) : new ArrayList<>();
    }

    // ?-------------------------------*/
    // ? --------    GETTERS   --------*/
    // ? ------------------------------*/
    public String getTitre() {
        return titre;
    }

    public float getDuree() {
        return duree;
    }

    public Album getAlbum() {
        return album;
    }

    public List<Artiste> getArtistes() {
        return artistes;
    }

    // ?-------------------------------*/
    // ? --------    SETTERS   --------*/
    // ? ------------------------------*/
    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDuree(float duree) {
        // garde-fou simple; adapte si tu veux tolérer 0 ou négatif
        if (duree > 0) this.duree = duree;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public void setArtistes(List<Artiste> artistes) {
        this.artistes = (artistes != null) ? new ArrayList<>(artistes) : new ArrayList<>();
    }

    // ?-------------------------------*/
    // ? --------     UTILS    --------*/
    // ? ------------------------------*/
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Musique [\n");
        sb.append("  Id      : ").append(getId()).append("\n");
        sb.append("  Titre   : ").append(titre != null ? titre : "inconnu").append("\n");
        sb.append("  Durée   : ").append(duree).append("\n");

        // format d’année comme dans Album
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy");
        String albumTxt = "inconnu";
        if (album != null) {
            String annee = (album.getDateSortie() != null) ? album.getDateSortie().format(fmt) : "inconnue";
            albumTxt = album.getTitreAlbum() + " (" + annee + ")";
        }
        sb.append("  Album   : ").append(albumTxt).append("\n");

        sb.append("  Artistes: ");
        if (artistes != null && !artistes.isEmpty()) {
            String noms = artistes.stream()
                    .map(a -> a != null ? a.getNom() : "inconnu")
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            sb.append(noms);
        } else {
            sb.append("aucun");
        }
        sb.append("\n]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Musique)) return false;
        Musique musique = (Musique) o;
        // pas de comparaison profonde de la liste (on peut changer selon besoin)
        return Float.compare(musique.duree, duree) == 0
                && Objects.equals(titre, musique.titre)
                && Objects.equals(album, musique.album);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titre, duree, album);
    }
}
