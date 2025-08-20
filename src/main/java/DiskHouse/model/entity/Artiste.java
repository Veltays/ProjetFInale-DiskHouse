package DiskHouse.model.entity;

import java.util.List;

public class Artiste extends Identifier {
    private String nom;
    private String prenom;
    private String pseudo;
    private List<Album> albums;

    // Constructeur
    public Artiste(String nom, String prenom, String pseudo, List<Album> albums) {
        super();
        this.nom = nom;
        this.prenom = prenom;
        this.pseudo = pseudo;
        this.albums = albums;
    }

    /*--------------------------------------------*/
    /*------------ Getters -----------------------*/
    /*--------------------------------------------*/

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getPseudo() {
        return pseudo;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    /*--------------------------------------------*/
    /*------------ Setters -----------------------*/
    /*--------------------------------------------*/

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    /*--------------------------------------------*/
    /*------------ toString ----------------------*/
    /*--------------------------------------------*/

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Artiste [\n");
        sb.append("  Id     : ").append(getId()).append("\n");
        sb.append("  Nom     : ").append(getNom()).append("\n");
        sb.append("  Prénom  : ").append(getPrenom()).append("\n");
        sb.append("  Pseudo  : ").append(getPseudo()).append("\n");
        sb.append("  Albums  :\n");
        for (Album album : albums) {
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Artiste artiste = (Artiste) o;
        return nom.equals(artiste.nom) &&
                prenom.equals(artiste.prenom) &&
                pseudo.equals(artiste.pseudo) &&
                albums.equals(artiste.albums);
    }
}
