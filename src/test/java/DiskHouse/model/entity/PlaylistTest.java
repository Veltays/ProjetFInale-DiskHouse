package DiskHouse.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PlaylistTest {
    private Playlist playlist;
    private Musique musique;

    @BeforeEach
    void setUp() {
        List<Musique> musiques = new ArrayList<>();
        musique = new Musique("Test Song", 3.5f, null, new ArrayList<>());
        musiques.add(musique);
        playlist = new Playlist(musiques, "My Playlist");
    }

    @Test
    void setNomPlaylist() {
        playlist.setNomPlaylist("New Playlist");
        assertEquals("New Playlist", playlist.getNomPlaylist(), "Le nom de la playlist devrait être 'New Playlist'");
    }

    @Test
    void setMusique() {
        List<Musique> newMusiques = new ArrayList<>();
        Musique newMusique = new Musique("New Song", 3.0f, null, new ArrayList<>());
        newMusiques.add(newMusique);
        playlist.setMusique(newMusiques);
        assertEquals(newMusiques, playlist.getMusique(), "Les musiques de la playlist devraient être mises à jour");
    }

    @Test
    void setNombreMusique() {
        playlist.setNombreMusique(1);
        assertEquals(1, playlist.getNombreMusique(), "Le nombre de musiques de la playlist devrait être 1");
    }

    @Test
    void getNomPlaylist() {
        assertEquals("My Playlist", playlist.getNomPlaylist(), "Le nom de la playlist devrait être 'My Playlist'");
    }

    @Test
    void getNombreMusique() {
        assertEquals(1, playlist.getNombreMusique(), "Le nombre de musiques dans la playlist devrait être 1");
    }

    @Test
    void getMusique() {
        assertTrue(playlist.getMusique().contains(musique), "La musique devrait être présente dans la liste de la playlist");
    }


    @Test
    void testToString() {
        String expectedString = "Playlist [\n" +
                "  Nom : My Playlist\n" +
                "  Nombre de musiques : 1\n" +
                "  Musiques : \n    - Test Song\n]";
        assertEquals(expectedString, playlist.toString(), "La méthode toString ne retourne pas la chaîne attendue");
    }

    @Test
    void testEquals() {
        Playlist samePlaylist = new Playlist(playlist.getMusique(), "My Playlist");
        assertTrue(playlist.equals(samePlaylist), "Les playlists devraient être égales");
    }
}
