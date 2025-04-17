package DiskHouse;

import DiskHouse.view.model.entity.Musique;
import DiskHouse.view.model.entity.Album;
import DiskHouse.view.model.entity.Artiste;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Création des objets Artiste et Album



        List<Musique> listeMusique = new ArrayList<>();
        List<Album> listeAlbum = new ArrayList<>();
        List<Artiste> listeArtistes = new ArrayList<>();


        //On crée notre objet Album
        Album albumSabrina = new Album("SabiAlbum", 2025, listeMusique);
        Album albumDiss = new Album("DissTrackDua", 2019, listeMusique);

        // On crée d'abord tous nos objets  musique
        Musique musique = new Musique("MinecraftSong", 3.5f, albumSabrina, listeArtistes);
        Musique musique2 = new Musique("SabiSong", 2.29f, albumSabrina, listeArtistes);


        Musique musique3 = new Musique("MadamePavochko", 1.6f, albumDiss, listeArtistes);
        Musique musique4 = new Musique("SexionDassaut", 2.1f, albumSabrina, listeArtistes);

        // Puis nos artistes
        Artiste artiste = new Artiste("Sabrina ","Carpenter ","Sabi ",listeAlbum);
        Artiste artiste2 = new Artiste("Dua ","Lipa ","Dodo ",listeAlbum);


        //Ensuite on ajoute nos ino dans nos listes

        // ajout des musiques dans la liste
        listeMusique.add(musique);
        listeMusique.add(musique2);

        // ajout des albums dans la liste
        listeAlbum.add(albumSabrina);
        listeAlbum.add(albumDiss);

        // ajout des artiste dans la liste
        listeArtistes.add(artiste);
        listeArtistes.add(artiste2);


        // Affichage des détails de la musique
        System.out.println(artiste.toString());
        System.out.println(artiste2.toString());
        System.out.println(musique.toString());
        System.out.println(musique2.toString());
        System.out.println(albumSabrina.toString());
        System.out.println(albumDiss.toString());


    }
}
