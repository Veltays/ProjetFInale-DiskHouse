package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.ArtisteFileDAO;
import DiskHouse.model.DAO.MusicFileDAO;
import DiskHouse.model.DAO.PlaylistFileDAO;
import DiskHouse.model.entity.Playlist;
import DiskHouse.view.MainPage;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainPageController implements IController<MainPage> {

    private final MainPage view;
    private final List<Playlist> playlists = new ArrayList<>();

    // ===== DAO =====
    private final PlaylistFileDAO playlistDAO = new PlaylistFileDAO("data/playlists.dat");
    private final MusicFileDAO    musicDAO    = new MusicFileDAO("data/musiques.dat");
    private final ArtisteFileDAO  artisteDAO  = new ArtisteFileDAO("data/artistes.dat");
    private final AlbumFileDAO    albumDAO    = new AlbumFileDAO("data/albums.dat");

    // Sous-contrôleurs
    private MainPagePlaylistController playlistController;
    private MainPageMusiqueController  musiqueController;
    private MainPageMenuController     menuController;

    /* ===================== CONSTRUCTEUR ===================== */

    public MainPageController(MainPage view) {
        this.view = Objects.requireNonNull(view);
    }

    /* ===================== INIT CONTROLLER ===================== */

    @Override
    public MainPage getView() {
        return view;
    }

    @Override
    public void initController() {
        // Sous-contrôleurs
        playlistController = new MainPagePlaylistController(this);
        musiqueController  = new MainPageMusiqueController(this);
        menuController     = new MainPageMenuController(this); // placeholder

        // Initialisation des tables
        if (view.getTablePlaylist() != null) {
            playlistController.initPlaylistTable(view.getTablePlaylist());
        }
        if (view.getTableMusicInPlaylistSelected() != null) {
            musiqueController.initMusicTable(view.getTableMusicInPlaylistSelected());
        }

        // === Chargement depuis les DAO ===
        loadAllFromDAO();

        // Lier playlists -> musiques (selection change)
        if (view.getTablePlaylist() != null) {
            playlistController.loadPlaylistsInto(
                    view.getTablePlaylist(),
                    playlists,
                    selected -> {
                        if (view.getTableMusicInPlaylistSelected() != null) {
                            if (selected != null) {
                                // la playlist sélectionnée contient déjà ses musiques (réhydratées)
                                musiqueController.loadMusicsForPlaylist(view.getTableMusicInPlaylistSelected(), selected);
                            } else {
                                musiqueController.clearMusicTable(view.getTableMusicInPlaylistSelected());
                            }
                        }
                    }
            );
        }

        // Boutons
        playlistController.wireButtons();
        musiqueController.wireButtons();
    }

    /* ===================== DAO LOAD ===================== */

    private void loadAllFromDAO() {
        playlists.clear();

        // On réhydrate les playlists avec leurs musiques
        List<Playlist> fromDao = playlistDAO.getAllWithSongs(musicDAO);
        playlists.addAll(fromDao);

        // Si tu veux (optionnel) précharger artistes/albums pour des combos/menus :
        // var allArtistes = artisteDAO.getAll();
        // var allAlbums   = albumDAO.getAll();
        // -> passe-les à tes sous-contrôleurs si besoin
    }

    /* ===================== ACTIONS ===================== */

    /** Affiche correctement une nouvelle fenêtre (pack + centrage). */
    void showFrame(JFrame frame) {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (frame.getWidth() == 0 || frame.getHeight() == 0) {
            frame.pack();
        }
        Window parent = view.isVisible() ? view : null;
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
    }

    /* ===================== UTILS ===================== */

    @Override
    public void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }
}
