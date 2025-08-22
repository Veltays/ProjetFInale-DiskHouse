package DiskHouse.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Artiste extends Identifier {
    private String nom;
    private String prenom;
    private String pseudo;
    private List<Album> albums;

    // ?-------------------------------*/
    // ? -------- Constructeur --------*/
    // ? ------------------------------*/
    public Artiste(String nom, String prenom, String pseudo, List<Album> albums) {
        super(); // appel au parent Identifier pour générer l’ID
        this.nom = nom;
        this.prenom = prenom;
        this.pseudo = pseudo;
        // copie défensive
        this.albums = (albums != null) ? new ArrayList<>(albums) : new ArrayList<>();
    }

    // ?-------------------------------*/
    // ? --------    GETTERS   --------*/
    // ? ------------------------------*/
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

    // ?-------------------------------*/
    // ? --------    SETTERS   --------*/
    // ? ------------------------------*/
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
        this.albums = (albums != null) ? new ArrayList<>(albums) : new ArrayList<>();
    }

    // ?-------------------------------*/
    // ? --------    UTILS     --------*/
    // ? ------------------------------*/
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Artiste [\n");
        sb.append("  Id      : ").append(getId()).append("\n");
        sb.append("  Nom     : ").append(getNom()).append("\n");
        sb.append("  Prénom  : ").append(getPrenom()).append("\n");
        sb.append("  Pseudo  : ").append(getPseudo()).append("\n");
        sb.append("  Albums  : \n");
        if (albums != null && !albums.isEmpty()) {
            for (Album album : albums) {
                sb.append("    - ").append(album.getTitreAlbum()).append("\n");
            }
        } else {
            sb.append("    Aucun album\n");
        }
        sb.append("]");
        return sb.toString();
    }

    // --- equals / hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artiste)) return false;
        Artiste artiste = (Artiste) o;
        return Objects.equals(nom, artiste.nom)
                && Objects.equals(prenom, artiste.prenom)
                && Objects.equals(pseudo, artiste.pseudo);
        // ⚠ volontairement on ne compare pas la liste d'albums
        // pour éviter des comparaisons profondes/cycles
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom, prenom, pseudo);
    }
}
