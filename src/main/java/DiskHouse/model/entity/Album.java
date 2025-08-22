package DiskHouse.model.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Album extends Identifier {
    private String titreAlbum;
    private LocalDate dateSortie;
    private List<Musique> musiques;


    // ?-------------------------------*/
    // ? -------- Constructeur --------*/
    // ? ------------------------------*/

    public Album(String titreAlbum, LocalDate dateSortie, List<Musique> musiques) {
        super(); // appele a la classe parente Identifier pour initialiser l'ID
        this.titreAlbum = titreAlbum;
        this.dateSortie = dateSortie;
        // copie défensive pour éviter les effets de bord
        this.musiques = (musiques != null) ? new ArrayList<>(musiques) : new ArrayList<>();
    }





    // ?-------------------------------*/
    // ? --------    GETTERS   --------*/
    // ? ------------------------------*/

    public String getTitreAlbum() {
        return titreAlbum;
    }

    public LocalDate getDateSortie() {
        return dateSortie;
    }

    public List<Musique> getMusiques() {
        return musiques;
    }

    // ?-------------------------------*/
    // ? --------    SETTER    --------*/
    // ? ------------------------------*/
    public void setTitreAlbum(String titreAlbum) {
        this.titreAlbum = titreAlbum;
    }

    public void setDateSortie(LocalDate dateSortie) {
        if (dateSortie != null && dateSortie.getYear() < 2025) {
            this.dateSortie = dateSortie;
        }
        // sinon on ignore (même logique que ton code d'origine)
    }


    public void setMusiques(List<Musique> musiques) {
        this.musiques = (musiques != null) ? new ArrayList<>(musiques) : new ArrayList<>();
    }




    // ?-------------------------------*/
    // ? --------    UTILS     --------*/
    // ? ------------------------------*/
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Album [\n");
        sb.append("  Id    : ").append(getId()).append("\n");
        sb.append("  Titre : ").append(getTitreAlbum()).append("\n");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy");
        String formattedDate = (dateSortie != null) ? dateSortie.format(fmt) : "inconnue";
        sb.append("  Date  : ").append(formattedDate).append("\n");

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
        if (!(o instanceof Album)) return false;
        Album album = (Album) o;
        // on ne compare pas la liste pour éviter des comparaisons profondes/cycles
        return Objects.equals(titreAlbum, album.titreAlbum)
                && Objects.equals(dateSortie, album.dateSortie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titreAlbum, dateSortie);
    }
}
