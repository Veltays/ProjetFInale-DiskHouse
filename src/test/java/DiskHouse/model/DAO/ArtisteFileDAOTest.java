package DiskHouse.model.DAO;

import DiskHouse.model.entity.Artiste;
import org.junit.jupiter.api.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ArtisteFileDAOTest {
    private static final String TEST_FILE = "test_artistes.dat";
    private ArtisteFileDAO dao;

    @BeforeEach
    void setUp() {
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        dao = new ArtisteFileDAO(TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }

    @Test
    void testCRUD() {
        Artiste artiste = new Artiste("TestArtiste", Collections.emptyList(), "img.png");
        // CREATE
        dao.add(artiste);
        List<Artiste> all = dao.getAll();
        assertEquals(1, all.size());
        assertEquals("TestArtiste", all.get(0).getPseudo());

        // READ by ID
        Artiste byId = dao.getById(String.valueOf(artiste.getId()));
        assertNotNull(byId);
        assertEquals("TestArtiste", byId.getPseudo());

        // UPDATE (simulate)
        byId = new Artiste("UpdatedArtiste", byId.getAlbums(), byId.getImageURL());
        List<Artiste> artistes = dao.getAll();
        artistes.removeIf(a -> a.getId() == artiste.getId());
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        ArtisteFileDAO dao2 = new ArtisteFileDAO(TEST_FILE);
        for (Artiste a : artistes) dao2.add(a);
        dao2.add(byId);
        Artiste updated = dao2.getById(String.valueOf(byId.getId()));
        assertNotNull(updated);
        assertEquals("UpdatedArtiste", updated.getPseudo());

        // DELETE
        artistes = dao2.getAll();
        artistes.removeIf(a -> a.getId() == byId.getId());
        f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        ArtisteFileDAO dao3 = new ArtisteFileDAO(TEST_FILE);
        for (Artiste a : artistes) dao3.add(a);
        assertNull(dao3.getById(String.valueOf(byId.getId())));
    }
}
