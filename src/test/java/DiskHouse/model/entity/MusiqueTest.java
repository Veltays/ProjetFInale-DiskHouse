package DiskHouse.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Calendar;
import java.util.Date;


class MusiqueTest {
    private Musique musique;
    private Artiste artiste;
    private Album album;
    private List<Musique> listeMusique = new ArrayList<>();
    private List<Album> listeAlbum = new ArrayList<>();
    private List<Artiste> listeArtistes = new ArrayList<>();


    private Date getDate(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }


    @BeforeEach
    void setUp() {
        // Initialisation des listes


        // Création d'un artiste
        artiste = new Artiste("Sabrina", "Carpenter", "Sabi", listeAlbum);
        listeArtistes.add(artiste);

        // Création d'un album
        album = new Album("Album Test", getDate(2023), new ArrayList<>());
        listeAlbum.add(album);

        // Création d'une musique associée à l'album et à l'artiste
        musique = new Musique("Test Titre", 3.5f, album, listeArtistes);
        listeMusique.add(musique);
    }


    @Test
    void setTitre() {
        musique.setTitre("Nouveau Titre");
        assertEquals("Nouveau Titre", musique.getTitre(),"Le titre devrait être 'Nouvelle Titre'");
    }

    @Test
    void setDuree() {
        musique.setDuree(3.2f);
        assertEquals(3.2f,musique.getDuree(),"La durée devrait être '3.2'");
    }

    @Test
    void setAlbum() {

        Album Nvxalbum = new Album("Heart is falling", getDate(2023),null);

        musique.setAlbum(Nvxalbum);
        assertEquals(Nvxalbum,musique.getAlbum(),"L'album devrait contenir la musique Heart is down");
    }

    @Test
    void setArtistes() {
        List<Artiste> listeArtistes = new ArrayList<>();
        Artiste newartiste = new Artiste("Dua", "Lipa", "Dudu",null);

        listeArtistes.add(newartiste);

        musique.setArtistes(listeArtistes);

        assertEquals(listeArtistes,musique.getArtistes(),"On devrait avoir comme artiste DuaLipa");

    }

    @Test
    void getTitre() {
        assertEquals("Test Titre", musique.getTitre(), "Le titre devrait être 'Test Titre'");
    }

    @Test
    void getDuree() {
        assertEquals(3.5f, musique.getDuree(), "La durée devrait être 3.5");
    }

    @Test
    void getAlbum() {
        assertEquals("Album Test", musique.getAlbum().getTitreAlbum(), "L'album devrait être 'Album Test'");
    }

    @Test
    void getArtistes() {
        // Assurez-vous que la musique a bien un artiste
        Artiste artiste = musique.getArtistes().getFirst();
        assertEquals("Sabrina", artiste.getNom(), "La musique devrait avoir un artiste nommé 'Dua'");
    }


@Test
    void testToString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String formattedDate = sdf.format(musique.getAlbum().getDateSortie());

        String expectedString = "Musique [\n" +
                "  Id     : " + musique.getId() + "\n" +
                "  Titre  : Test Titre\n" +
                "  Durée  : 3.5\n" +
                "  Album  : Album Test (" + formattedDate + ")\n" +
                "  Artistes : Sabrina, \n]";

        assertEquals(expectedString, musique.toString(), "La méthode toString ne retourne pas la chaîne attendue");
    }



    @Test
    void testEquals() {

            Musique musiqueCopy = new Musique("Test Titre", 3.5f, album, listeArtistes);
            musiqueCopy.toString();
            musique.toString();
            assertTrue(musique.equals(musiqueCopy), "Les musiques devraient être égales");
    }



}