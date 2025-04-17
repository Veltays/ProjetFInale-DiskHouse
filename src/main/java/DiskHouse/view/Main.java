package DiskHouse;

import DiskHouse.view.model.entity.Musique;
import DiskHouse.view.model.entity.Album;
import DiskHouse.view.model.entity.Artiste;
import DiskHouse.view.model.entity.Playlist;
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
        Musique musique3 = new Musique("silksong", 2.29f, albumSabrina, listeArtistes);


        // Puis nos artistes
        Artiste artiste = new Artiste("Sabrina ","Carpenter ","Sabi ",listeAlbum);
        Artiste artiste2 = new Artiste("Dua ","Lipa ","Dodo ",listeAlbum);


        //Ensuite on ajoute nos inos dans nos listes

        // ajout des musiques dans la liste
        listeMusique.add(musique);
        listeMusique.add(musique2);
        listeMusique.add(musique3);

        // ajout des albums dans la liste
        listeAlbum.add(albumSabrina);
        listeAlbum.add(albumDiss);

        // ajout des artiste dans la liste
        listeArtistes.add(artiste);
        listeArtistes.add(artiste2);


        //création playlist
        Playlist playlist1 = new Playlist(listeMusique,"Testy");


        // Affichage des détails de la musique
        System.out.println("🎤 Artistes :");
        System.out.println(artiste);
        System.out.println(artiste2);

        System.out.println("\n🎵 Musiques :");
        System.out.println(musique);
        System.out.println(musique2);

        System.out.println("\n💿 Albums :");
        System.out.println(albumSabrina);
        System.out.println(albumDiss);

        System.out.println("\n📻 Playlist :");
        System.out.println(playlist1);





    }
}
