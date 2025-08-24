package DiskHouse.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtisteTest {
    private Artiste artiste;
    private Album album;

    private Date getDate(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    @BeforeEach
    void setUp() {
        List<Album> albums = new ArrayList<>();
        album = new Album("Test Album", getDate(2023), new ArrayList<>());
        albums.add(album);
        artiste = new Artiste("Sabi", albums);
    }

    @Test
    void setPseudo() {
        artiste.setPseudo("Dudu");
        assertEquals("Dudu", artiste.getPseudo(), "Le pseudo de l'artiste devrait être 'Dudu'");
    }

    @Test
    void setAlbums() {
        List<Album> newAlbums = new ArrayList<>();
        Album newAlbum = new Album("New Album", getDate(2023), new ArrayList<>());
        newAlbums.add(newAlbum);
        artiste.setAlbums(newAlbums);
        assertEquals(newAlbums, artiste.getAlbums(), "Les albums de l'artiste devraient être mis à jour");
    }

    @Test
    void getPseudo() {
        assertEquals("Sabi", artiste.getPseudo(), "Le pseudo de l'artiste devrait être 'Sabi'");
    }

    @Test
    void getAlbums() {
        assertTrue(artiste.getAlbums().contains(album), "L'album de l'artiste devrait être présent dans la liste d'albums");
    }

    @Test
    void testToString() {
        String expectedString = "Artiste [\n" +
                "  Id      : " + artiste.getId() + "\n" +
                "  Pseudo  : Sabi\n" +
                "  Albums  : \n    - Test Album\n]";
        assertEquals(expectedString, artiste.toString(), "La méthode toString ne retourne pas la chaîne attendue");
    }

    @Test
    void testEquals() {
        Artiste sameArtiste = new Artiste("Sabi", artiste.getAlbums());
        assertTrue(artiste.equals(sameArtiste), "Les artistes devraient être égaux");
    }
}
