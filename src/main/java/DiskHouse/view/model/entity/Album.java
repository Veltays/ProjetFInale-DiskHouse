package DiskHouse.view.model.entity;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Album extends Identifier {
    private String TitreAlbum;
    private int DateSortie;
    private List<Musique> Musiques;


    // Constructeur
    public Album(String titreAlbum, int dateSortie, List<Musique> musiques)
    {
        super();
        this.TitreAlbum = titreAlbum;
        this.DateSortie = dateSortie;
        this.Musiques = musiques;
    }



    /*-----------------------------------*/
    /*------------ Getters --------------*/
    /*-----------------------------------*/

    public String getTitreAlbum() {
        return TitreAlbum;
    }

    public int getDateSortie() {
        return DateSortie;
    }

    public List<Musique> getMusiques() {
        return Musiques;
    }

    /*-----------------------------------*/
    /*------------ Setters --------------*/
    /*-----------------------------------*/

    public void setTitreAlbum(String titreAlbum) {
        TitreAlbum = titreAlbum;
    }

    public void setDateSortie(int dateSortie) {
        DateSortie = dateSortie;
    }

    public void setMusiques(List<Musique> musiques) {
        Musiques = musiques;
    }

    /*-----------------------------------*/
    /*------------ toString -------------*/
    /*-----------------------------------*/

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Album [\n");
        sb.append("  Id     : ").append(getId()).append("\n");
        sb.append("  Titre : ").append(getTitreAlbum()).append("\n");
        sb.append("  Date  : ").append(getDateSortie()).append("\n");
        sb.append("  Musiques : ");
        for (Musique m : Musiques) {
            sb.append(m.getTitre()).append(", ");
        }
        sb.append("\n]");
        return sb.toString();
    }


    /*-----------------------------------*/
    /*------------ equals ---------------*/
    /*-----------------------------------*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;
        Album album = (Album) o;
        return TitreAlbum.equals(album.TitreAlbum) &&
                DateSortie == album.DateSortie;
    }
}