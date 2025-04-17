package DiskHouse.view.model.entity;
import java.util.List;

public class Musique extends Identifier {
    private List<Artiste> Artistes;
    private String Titre;
    private Album Album;
    private float Duree;

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
        this.Titre = titre;
    }

    public void setDuree(float duree) {
        this.Duree = duree;
    }

    public void setAlbum(Album album) {
        this.Album = album;
    }

    public void setArtistes(List<Artiste> artistes) {
        this.Artistes = artistes;
    }

    /*------------------------------------------------------------------------*/
    /*----------------------------Ensemble des get----------------------------*/
    /*------------------------------------------------------------------------*/

    public String getTitre() {
        return Titre;
    }

    public float getDuree() {
        return Duree;
    }

    public Album getAlbum() {
        return Album;
    }

    public List<Artiste> getArtistes() {
        return Artistes;
    }


    /*---------------------------------------*/
    /*------------MéthodeBasique------------ */
    /*---------------------------------------*/


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Musique [\n");
        sb.append("  Id     : ").append(getId()).append("\n");
        sb.append("  Titre  : ").append(getTitre()).append("\n");
        sb.append("  Durée  : ").append(getDuree()).append("\n");
        if (Album != null) {
            sb.append("  Album  : ").append(Album.getTitreAlbum()).append(" (").append(Album.getDateSortie()).append(")\n");
        } else {
            sb.append("  Album  : null\n");
        }
        sb.append("  Artistes : ");
        for (Artiste artiste : Artistes) {
            sb.append(artiste.getNom()).append(", ");
        }
        sb.append("\n]");
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;   // Si l'objet est lui même alors il sont égaux
        if (o == null || getClass() != o.getClass()) // Si l'objet est null, ou que sa classe et différente de la mienne alors on n'est pas egaux
            return false;
        Musique song = (Musique) o;
        return getTitre().equals(song.getTitre()) &&
                Float.compare(getDuree(), song.getDuree()) == 0 &&
                getAlbum().equals(song.getAlbum());
    }
}
