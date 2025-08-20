package DiskHouse.model.entity;

import java.util.List;

public class Playlist {

    private List<Musique> musique;
    private String nomPlaylist;
    private int nombreMusique;

    /*------------------------------*/
    /*-------- Constructeur --------*/
    /*------------------------------*/

    public Playlist(List<Musique> musique, String nomPlaylist)
    {
        this.nomPlaylist = nomPlaylist;
        this.musique = musique;
        this.nombreMusique = (musique != null) ? musique.size() : 0;
    }

    /*------------------------------*/
    /*----------- Getters ----------*/
    /*------------------------------*/

    public List<Musique> getMusique() {
        return musique;
    }

    public String getNomPlaylist() {
        return nomPlaylist;
    }

    public int getNombreMusique() {
        return nombreMusique;
    }

    /*------------------------------*/
    /*----------- Setters ----------*/
    /*------------------------------*/

    public void setMusique(List<Musique> musique) {
        this.musique = musique;
        this.nombreMusique = (musique != null) ? musique.size() : 0;
    }

    public void setNomPlaylist(String nomPlaylist) {
        this.nomPlaylist = nomPlaylist;
    }

    public void setNombreMusique(int nombreMusique) {
        this.nombreMusique = nombreMusique;
    }

    /*------------------------------*/
    /*------------ toString --------*/
    /*------------------------------*/

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Playlist [\n");
        sb.append("  Nom : ").append(nomPlaylist).append("\n");
        sb.append("  Nombre de musiques : ").append(nombreMusique).append("\n");
        sb.append("  Musiques : ");
        if (musique != null && !musique.isEmpty()) {
            for (Musique m : musique) {
                sb.append("\n    - ").append(m.getTitre());
            }
        } else {
            sb.append("aucune");
        }
        sb.append("\n]");
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Playlist playlist = (Playlist) o;

        return getNombreMusique() == playlist.getNombreMusique() &&
                getNomPlaylist().equals(playlist.getNomPlaylist()) &&
                getMusique().equals(playlist.getMusique());


    }


}

