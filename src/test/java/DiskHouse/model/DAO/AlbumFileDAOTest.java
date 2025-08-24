package DiskHouse.model.DAO;

import DiskHouse.model.entity.Album;
import org.junit.jupiter.api.*;
import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AlbumFileDAOTest {
    private static final String TEST_FILE = "test_albums.dat";
    private AlbumFileDAO dao;

    @BeforeEach
    void setUp() {
        // Nettoyage du fichier de test avant chaque test
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        dao = new AlbumFileDAO(TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }

    @Test
    void testCRUD() {
        Album album = new Album("Test Album", LocalDate.of(2020,1,1), Collections.emptyList(), "cover.png");
        // CREATE
        dao.add(album);
        List<Album> all = dao.getAll();
        assertEquals(1, all.size());
        assertEquals("Test Album", all.get(0).getTitreAlbum());

        // READ by ID
        Album byId = dao.getById(String.valueOf(album.getId()));
        assertNotNull(byId);
        assertEquals("Test Album", byId.getTitreAlbum());

        // UPDATE (simulate: change title, re-add)
        byId = new Album("Updated Album", byId.getDateSortie(), byId.getMusiques(), byId.getCoverImageURL());
        // Suppression manuelle puis ajout (car DAO ne gère pas update directement)
        List<Album> albums = dao.getAll();
        albums.removeIf(a -> a.getId() == album.getId());
        // Réécriture du fichier
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        AlbumFileDAO dao2 = new AlbumFileDAO(TEST_FILE);
        for (Album a : albums) dao2.add(a);
        dao2.add(byId);
        Album updated = dao2.getByName("Updated Album");
        assertNotNull(updated);
        assertEquals("Updated Album", updated.getTitreAlbum());

        // DELETE
        albums = dao2.getAll();
        albums.removeIf(a -> a.getId() == byId.getId());
        f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        AlbumFileDAO dao3 = new AlbumFileDAO(TEST_FILE);
        for (Album a : albums) dao3.add(a);
        assertNull(dao3.getById(String.valueOf(byId.getId())));
    }
}

