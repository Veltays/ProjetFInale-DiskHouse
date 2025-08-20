package DiskHouse.model.entity;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;



public class Album extends Identifier {
    private String titreAlbum;
    private Date dateSortie;
    private List<Musique> musiques;

    // Constructeur
    public Album(String titreAlbum, Date dateSortie, List<Musique> musiques) {
        super();
        this.titreAlbum = titreAlbum;
        this.dateSortie = dateSortie;
        this.musiques = musiques;
    }

    /*-----------------------------------*/
    /*------------ Getters --------------*/
    /*-----------------------------------*/

    public String getTitreAlbum() {
        return titreAlbum;
    }

    public Date  getDateSortie() {
        return dateSortie;
    }

    public List<Musique> getMusiques() {
        return musiques;
    }

    /*-----------------------------------*/
    /*------------ Setters --------------*/
    /*-----------------------------------*/

    public void setTitreAlbum(String titreAlbum) {
        this.titreAlbum = titreAlbum;
    }

    public void setDateSortie(Date dateSortie) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateSortie);
        int year = cal.get(Calendar.YEAR);

        if (year < 2025) {
            this.dateSortie = dateSortie;
        }
    }

    public void setMusiques(List<Musique> musiques) {
        this.musiques = musiques;
    }

    /*-----------------------------------*/
    /*------------ toString -------------*/
    /*-----------------------------------*/

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Album [\n");
        sb.append("  Id    : ").append(getId()).append("\n");
        sb.append("  Titre : ").append(getTitreAlbum()).append("\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String formattedDate = (dateSortie != null) ? sdf.format(dateSortie) : "inconnue";
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

    /*-----------------------------------*/
    /*------------ equals ---------------*/
    /*-----------------------------------*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Si l'objet est le même
        if (!(o instanceof Album)) return false; // Si l'objet est d'une autre classe
        Album album = (Album) o;
        // Comparaison des titres et des dates
        return titreAlbum.equals(album.titreAlbum) &&
                (dateSortie != null && dateSortie.equals(album.dateSortie)); // Comparaison des dates avec equals()
    }

}
