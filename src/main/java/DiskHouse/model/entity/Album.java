package DiskHouse.model.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Représente un album.
 * - Contient un visuel (coverImageURL). Si null/blank => image par défaut "/PP.png".
 * - Copie défensive des listes.
 */
public class Album extends Identifier {

    private static final String DEFAULT_ALBUM_COVER_RESOURCE = "/PP.png";

    private String titreAlbum;
    private LocalDate dateSortie;
    private List<Musique> musiques;

    // Image (URL absolue, ex: http(s)://... OU URL issue des resources via getResource(...).toExternalForm())
    private String coverImageURL;

    // -------------------- Constructeurs --------------------
    public Album(String titreAlbum, LocalDate dateSortie, List<Musique> musiques) {
        this(titreAlbum, dateSortie, musiques, null);
    }

    public Album(String titreAlbum, LocalDate dateSortie, List<Musique> musiques, String coverImageURL) {
        super(); // init de l'ID via la classe parente
        this.titreAlbum = titreAlbum;
        this.dateSortie = dateSortie;
        this.musiques = (musiques != null) ? new ArrayList<>(musiques) : new ArrayList<>();
        // applique défaut si null/vide
        setCoverImageURL(coverImageURL);
    }

    // -------------------- Getters --------------------
    public String getTitreAlbum() {
        return titreAlbum;
    }

    public LocalDate getDateSortie() {
        return dateSortie;
    }

    public List<Musique> getMusiques() {
        return musiques;
    }

    public String getCoverImageURL() {
        return coverImageURL;
    }

    // -------------------- Setters --------------------
    public void setTitreAlbum(String titreAlbum) {
        this.titreAlbum = titreAlbum;
    }

    public void setDateSortie(LocalDate dateSortie) {
        // garde-fou simple (tu peux adapter suivant tes règles métier)
        if (dateSortie != null) {
            this.dateSortie = dateSortie;
        }
    }

    public void setMusiques(List<Musique> musiques) {
        this.musiques = (musiques != null) ? new ArrayList<>(musiques) : new ArrayList<>();
    }

    /**
     * Si url est null/blank => charge l'image par défaut depuis /resources.
     * Laisse passer n'importe quelle URL sinon (aucune validation).
     */
    public void setCoverImageURL(String url) {
        if (url != null && !url.isBlank()) {
            this.coverImageURL = url;
        } else {
            String fallback = resolveResourceToExternal(DEFAULT_ALBUM_COVER_RESOURCE);
            this.coverImageURL = (fallback != null) ? fallback : DEFAULT_ALBUM_COVER_RESOURCE; // dernier recours
        }
    }

    // -------------------- Utils --------------------
    private String resolveResourceToExternal(String resourcePath) {
        try {
            java.net.URL u = getClass().getResource(resourcePath);
            return (u != null) ? u.toExternalForm() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy");
        String formattedDate = (dateSortie != null) ? dateSortie.format(fmt) : "inconnue";

        StringBuilder sb = new StringBuilder();
        sb.append("Album [\n");
        sb.append("  Id     : ").append(getId()).append("\n");
        sb.append("  Titre  : ").append(titreAlbum != null ? titreAlbum : "inconnu").append("\n");
        sb.append("  Date   : ").append(formattedDate).append("\n");
        sb.append("  Image  : ").append(coverImageURL).append("\n");
        sb.append("  Musiques : \n");
        if (musiques != null && !musiques.isEmpty()) {
            for (Musique m : musiques) {
                sb.append("   - ").append(m != null ? m.getTitre() : "inconnue").append("\n");
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
        if (!(o instanceof Album)) return false;
        Album album = (Album) o;
        return Objects.equals(titreAlbum, album.titreAlbum)
                && Objects.equals(dateSortie, album.dateSortie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titreAlbum, dateSortie);
    }
}
