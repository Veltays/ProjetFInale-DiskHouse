package DiskHouse.model.DAO;

import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import org.junit.jupiter.api.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MusicFileDAOTest {
    private static final String TEST_FILE = "test_musiques.dat";
    private MusicFileDAO dao;

    @BeforeEach
    void setUp() {
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        dao = new MusicFileDAO(TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }

    @Test
    void testCRUD() {
        Album album = new Album("AlbumTest", java.time.LocalDate.now(), Collections.emptyList(), "cover.png");
        Musique musique = new Musique("TitreTest", 123.0f, album, Collections.emptyList(), "cover.png");
        // CREATE
        dao.add(musique);
        List<Musique> all = dao.getAll();
        assertEquals(1, all.size());
        assertEquals("TitreTest", all.get(0).getTitre());

        // READ by ID
        Musique byId = dao.getById(String.valueOf(musique.getId()));
        assertNotNull(byId);
        assertEquals("TitreTest", byId.getTitre());

        // UPDATE (simulate)
        byId = new Musique("TitreUpdate", byId.getDuree(), byId.getAlbum(), byId.getArtistes(), byId.getCoverImageURL());
        List<Musique> musiques = dao.getAll();
        musiques.removeIf(m -> m.getId() == musique.getId());
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        MusicFileDAO dao2 = new MusicFileDAO(TEST_FILE);
        for (Musique m : musiques) dao2.add(m);
        dao2.add(byId);
        Musique updated = dao2.getById(String.valueOf(byId.getId()));
        assertNotNull(updated);
        assertEquals("TitreUpdate", updated.getTitre());

        // DELETE
        musiques = dao2.getAll();
        musiques.removeIf(m -> m.getId() == byId.getId());
        f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        MusicFileDAO dao3 = new MusicFileDAO(TEST_FILE);
        for (Musique m : musiques) dao3.add(m);
        assertNull(dao3.getById(String.valueOf(byId.getId())));
    }
}
