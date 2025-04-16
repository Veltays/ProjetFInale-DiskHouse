package DiskHouse.view.model.entity;

public class Musique {
    List<Artiste> Artistes
    private final String Titre;
    private Album album;
    private float Durée;


    public Musique(String titres, float duree, Album album, List<Artiste> artiste) {
        SetTitle(titre);
        SetDuree(duree);
        SetAlbum(album);
        setArtistes(artiste);
    }

    /*------------------------------------------------------------------------*/
    /*----------------------------Ensemble des set----------------------------*/
    /*------------------------------------------------------------------------*/

    public void SetTitle(String titre)
    {
        Titre = titre;
    }
    public void SetDuree(float duree)
    {
        Duree = duree;
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
    public String toString()
    {
        String AllArtist = ""
        for(i = 0; i< getArtiste().size();i++)
        {
            AllArtist = AllArtist +  artiste.get(i).getNom() + ","
        }

        return "Musique" + getTitre() + "Album " + getAlbum() + "De " + Allartist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;   // Si l'objet est lui même alors il sont égaux
        if (o == null || getClass() != o.getClass()) // Si l'objet est null, ou que sa classe et différente de la mienne alors on n'est pas egaux
            return false;
        Musique song = (Musique) o;
        return Titre.equals(song.Titre) &&
                Double.compare(Duree, song.Duree) == 0 &&
                Album.equals(song.Album);
    }



}
