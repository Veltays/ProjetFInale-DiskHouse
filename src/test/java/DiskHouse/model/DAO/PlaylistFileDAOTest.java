package DiskHouse.model.DAO;

import DiskHouse.model.entity.Playlist;
import org.junit.jupiter.api.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlaylistFileDAOTest {
    private static final String TEST_FILE = "test_playlists.dat";
    private PlaylistFileDAO dao;

    @BeforeEach
    void setUp() {
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        dao = new PlaylistFileDAO(TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }

    @Test
    void testCRUD() {
        Playlist playlist = new Playlist("TestPlaylist", Collections.emptyList());
        // CREATE
        dao.add(playlist);
        List<Playlist> all = dao.getAll();
        assertEquals(1, all.size());
        assertEquals("TestPlaylist", all.get(0).getNomPlaylist());

        // READ by ID
        Playlist byId = dao.getById(String.valueOf(playlist.getId()));
        assertNotNull(byId);
        assertEquals("TestPlaylist", byId.getNomPlaylist());

        // UPDATE (simulate)
        byId = new Playlist("UpdatedPlaylist", byId.getMusiques());
        List<Playlist> playlists = dao.getAll();
        playlists.removeIf(p -> p.getId() == playlist.getId());
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        PlaylistFileDAO dao2 = new PlaylistFileDAO(TEST_FILE);
        for (Playlist p : playlists) dao2.add(p);
        dao2.add(byId);
        Playlist updated = dao2.getById(String.valueOf(byId.getId()));
        assertNotNull(updated);
        assertEquals("UpdatedPlaylist", updated.getNomPlaylist());

        // DELETE
        playlists = dao2.getAll();
        playlists.removeIf(p -> p.getId() == byId.getId());
        f = new File(TEST_FILE);
        if (f.exists()) f.delete();
        PlaylistFileDAO dao3 = new PlaylistFileDAO(TEST_FILE);
        for (Playlist p : playlists) dao3.add(p);
        assertNull(dao3.getById(String.valueOf(byId.getId())));
    }
}
package DiskHouse.model.DAO;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class AlbumFileDAOTest {
    @Test void testCreate() {}
    @Test void testRead() {}
    @Test void testUpdate() {}
    @Test void testDelete() {}
}
