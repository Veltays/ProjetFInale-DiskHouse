package DiskHouse.view.model.entity;

import java.util.List;

public class Artiste {
    private String Nom;
    private String Prenom;
    private String Pseudo;
    private List<Album> Albums;

    // Constructeur
    public Artiste(String Nom, String Prenom, String Pseudo, List<Album> Albums) {
        this.Nom = Nom;
        this.Prenom = Prenom;
        this.Pseudo = Pseudo;
        this.Albums = Albums;
    }

    /*--------------------------------------------*/
    /*------------ Getters -----------------------*/
    /*--------------------------------------------*/

    public String getNom() {
        return Nom;
    }

    public String getPrenom() {
        return Prenom;
    }

    public String getPseudo() {
        return Pseudo;
    }

    public List<Album> getAlbums() {
        return Albums;
    }

    /*--------------------------------------------*/
    /*------------ Setters -----------------------*/
    /*--------------------------------------------*/

    public void setNom(String nom) {
        this.Nom = nom;
    }

    public void setPrenom(String prenom) {
        this.Prenom = prenom;
    }

    public void setPseudo(String pseudo) {
        this.Pseudo = pseudo;
    }

    public void setAlbums(List<Album> albums) {
        this.Albums = albums;
    }

    /*--------------------------------------------*/
    /*------------ toString ----------------------*/
    /*--------------------------------------------*/

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Artiste [\n");
        sb.append("  Nom     : ").append(Nom).append("\n");
        sb.append("  Prénom  : ").append(Prenom).append("\n");
        sb.append("  Pseudo  : ").append(Pseudo).append("\n");
        sb.append("  Albums  :\n");
        for (Album album : Albums) {
            sb.append("    - ").append(album.getTitreAlbum()).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }


    /*--------------------------------------------*/
    /*--------------------- equals ---------------*/
    /*--------------------------------------------*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artiste artiste = (Artiste) o;
        return Nom.equals(artiste.Nom) &&
                Prenom.equals(artiste.Prenom) &&
                Pseudo.equals(artiste.Pseudo) &&
                Albums.equals(artiste.Albums);
    }

}
