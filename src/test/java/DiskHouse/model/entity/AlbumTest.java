package DiskHouse.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;


import static org.junit.jupiter.api.Assertions.*;

class AlbumTest {
    private Album album;
    private Musique musique;
    private Date dateSortie;

    @BeforeEach
    void setUp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2023);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        dateSortie = cal.getTime();

        List<Musique> musiques = new ArrayList<>();
        musique = new Musique("Test Song", 3.5f, null, new ArrayList<>());
        musiques.add(musique);
        album = new Album("Test Album", dateSortie, musiques);
    }

    @Test
    void setTitreAlbum() {
        album.setTitreAlbum("New Album");
        assertEquals("New Album", album.getTitreAlbum(), "Le titre de l'album devrait être 'New Album'");
    }

    @Test
    void setDateSortie() {
        Calendar cal = Calendar.getInstance();
        cal.set(2022, Calendar.JANUARY, 1);
        Date newDate = cal.getTime();
        album.setDateSortie(newDate);
        assertEquals(newDate, album.getDateSortie(), "La date de sortie devrait être mise à jour");
    }

    @Test
    void setMusiques() {
        List<Musique> newMusiques = new ArrayList<>();
        Musique newMusique = new Musique("New Song", 3.0f, album, new ArrayList<>());
        newMusiques.add(newMusique);
        album.setMusiques(newMusiques);
        assertEquals(newMusiques, album.getMusiques(), "Les musiques de l'album devraient être mises à jour");
    }

    @Test
    void getTitreAlbum() {
        assertEquals("Test Album", album.getTitreAlbum(), "Le titre de l'album devrait être 'Test Album'");
    }

    @Test
    void getDateSortie() {
        assertEquals(dateSortie, album.getDateSortie(), "La date de sortie de l'album devrait être 2023-01-01");
    }

    @Test
    void getMusiques() {
        assertTrue(album.getMusiques().contains(musique), "La musique devrait être présente dans la liste de musiques de l'album");
    }

    @Test
    void testToString() {
        // Format de la date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String formattedDate = sdf.format(album.getDateSortie());

        // Création du format attendu pour l'album
        String expectedString = "Album [\n" +
                "  Id    : 9\n" +
                "  Titre : Test Album\n" +
                "  Date  : " + formattedDate + "\n" +
                "  Musiques : \n   - Test Song\n" + // Remplacer "Test Song" par les titres des musiques
                "]";

        // Vérification du résultat
        assertEquals(expectedString, album.toString(), "La méthode toString ne retourne pas la chaîne attendue");
    }

    @Test
    void testEquals() {
        Album sameAlbum = new Album("Test Album", dateSortie, album.getMusiques());
        assertTrue(album.equals(sameAlbum), "Les albums devraient être égaux");
    }
}
