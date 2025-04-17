package DiskHouse.view.model.entity;

import java.util.List;

public class Playlist {

    private List<Musique> Musique;
    private String NomPlaylist;
    private int NombreMusique;

    /*------------------------------*/
    /*-------- Constructeur --------*/
    /*------------------------------*/

    public Playlist(List<Musique> musique,String nomPlaylist)
    {
        this.NomPlaylist = nomPlaylist;
        this.Musique = musique;
        this.NombreMusique = (musique != null) ? musique.size() : 0;
    }

    /*------------------------------*/
    /*----------- Getters ----------*/
    /*------------------------------*/

    public List<Musique> getMusique() {
        return Musique;
    }

    public String getNomPlaylist() {
        return NomPlaylist;
    }

    public int getNombreMusique() {
        return NombreMusique;
    }

    /*------------------------------*/
    /*----------- Setters ----------*/
    /*------------------------------*/

    public void setMusique(List<Musique> musique) {
        this.Musique = musique;
        this.NombreMusique = (musique != null) ? musique.size() : 0;
    }

    public void setNomPlaylist(String nomPlaylist) {
        this.NomPlaylist = nomPlaylist;
    }

    public void setNombreMusique(int nombreMusique) {
        this.NombreMusique = nombreMusique;
    }

    /*------------------------------*/
    /*------------ toString --------*/
    /*------------------------------*/

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Playlist [\n");
        sb.append("  Nom : ").append(NomPlaylist).append("\n");
        sb.append("  Nombre de musiques : ").append(NombreMusique).append("\n");
        sb.append("  Musiques : ");
        if (Musique != null && !Musique.isEmpty()) {
            for (Musique m : Musique) {
                sb.append("\n    - ").append(m.getTitre());
            }
        } else {
            sb.append("aucune");
        }
        sb.append("\n]");
        return sb.toString();
    }
}
