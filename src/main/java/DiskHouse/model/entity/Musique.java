package DiskHouse.model.entity;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Musique extends Identifier {
    private List<Artiste> artistes;
    private String titre;
    private Album album;
    private float duree;

    public Musique(String titre, float duree, Album album, List<Artiste> artistes) {
        super();
        setTitre(titre);
        setDuree(duree);
        setAlbum(album);
        setArtistes(artistes);
    }

    /*------------------------------------------------------------------------*/
    /*----------------------------Ensemble des set----------------------------*/
    /*------------------------------------------------------------------------*/

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDuree(float duree) {
        this.duree = duree;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public void setArtistes(List<Artiste> artistes) {
        this.artistes = artistes;
    }

    /*------------------------------------------------------------------------*/
    /*----------------------------Ensemble des get----------------------------*/
    /*------------------------------------------------------------------------*/

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

    /*---------------------------------------*/
    /*------------MéthodeBasique------------ */
    /*---------------------------------------*/


    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String formattedDate = (album != null && album.getDateSortie() != null) ? sdf.format(album.getDateSortie()) : "inconnu";

        StringBuilder sb = new StringBuilder();
        sb.append("Musique [\n")
                .append("  Id     : ").append(getId()).append("\n")
                .append("  Titre  : ").append(getTitre()).append("\n")
                .append("  Durée  : ").append(getDuree()).append("\n")
                .append("  Album  : ").append(album != null ? album.getTitreAlbum() + " (" + formattedDate + ")" : "inconnu").append("\n")
                .append("  Artistes : ");

        for (Artiste artiste : getArtistes()) {
            sb.append(artiste.getNom()).append(", ");
        }
        sb.append("\n]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Musique musique = (Musique) o;
        return Float.compare(musique.duree, duree) == 0 &&
                titre.equals(musique.titre) &&
                (album != null ? album.equals(musique.album) : musique.album == null) && // Comparaison des albums
                artistes.equals(musique.artistes);
    }
}
