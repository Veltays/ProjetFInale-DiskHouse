//************************************************//
//************** MAIN PAGE CONTROLLER ************//
//************************************************//
// Contrôleur principal de la page d'accueil de l'application.
// Gère l'initialisation, la liaison des sous-contrôleurs, le chargement des données DAO,
// la gestion des interactions utilisateur et l'affichage des erreurs/informations.

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

    //************************************************//
    //************** ATTRIBUTS ************************//
    //************************************************//
    private final MainPage view;
    private final String username;
    private final List<Playlist> playlists = new ArrayList<>();
    private final PlaylistFileDAO playlistDAO;
    private final MusicFileDAO musicDAO;
    private final ArtisteFileDAO artisteDAO;
    private final AlbumFileDAO albumDAO;
    private MainPagePlaylistController playlistController;
    private MainPageMusiqueController musiqueController;
    private MainPageMenuController menuController;

    //************************************************//
    //************** CONSTRUCTEUR *********************//
    //************************************************//
    public MainPageController(MainPage view, String username) {
        this.view = Objects.requireNonNull(view);
        this.username = username;
        this.playlistDAO = new PlaylistFileDAO("data/" + username + "_playlists.dat");
        this.musicDAO = new MusicFileDAO("data/" + username + "_musiques.dat");
        this.artisteDAO = new ArtisteFileDAO("data/" + username + "_artistes.dat");
        this.albumDAO = new AlbumFileDAO("data/" + username + "_albums.dat");
    }

    //************************************************//
    //************** GETTEUR VUE **********************//
    //************************************************//
    @Override
    public MainPage getView() { return view; }

    //************************************************//
    //************** INITIALISATION *******************//
    //************************************************//
    @Override
    public void initController() {
        initSubControllers();
        initTables();
        loadAllFromDAO();
        linkPlaylistsToMusics();
        wireButtons();
    }

    private void initSubControllers() {
        playlistController = new MainPagePlaylistController(this);
        musiqueController = new MainPageMusiqueController(this);
        menuController = new MainPageMenuController(this);
        menuController.wireMenuListeners();
    }

    private void initTables() {
        if (view.getTablePlaylist() != null) {
            playlistController.initPlaylistTable(view.getTablePlaylist());
        }
        if (view.getTableMusicInPlaylistSelected() != null) {
            musiqueController.initMusicTable(view.getTableMusicInPlaylistSelected());
        }
    }

    //************************************************//
    //************** CHARGEMENT DAO *******************//
    //************************************************//
    private void loadAllFromDAO() {
        playlists.clear();
        List<Playlist> fromDao = playlistDAO.getAllWithSongs(musicDAO);
        playlists.addAll(fromDao);
        // Optionnel : précharger artistes/albums pour les sous-contrôleurs si besoin
        // var allArtistes = artisteDAO.getAll();
        // var allAlbums = albumDAO.getAll();
    }

    //************************************************//
    //************** LIAISON PLAYLISTS/MUSIQUES *******//
    //************************************************//
    private void linkPlaylistsToMusics() {
        if (view.getTablePlaylist() != null) {
            playlistController.loadPlaylistsInto(
                view.getTablePlaylist(),
                playlists,
                selected -> {
                    if (view.getTableMusicInPlaylistSelected() != null) {
                        if (selected != null) {
                            musiqueController.loadMusicsForPlaylist(view.getTableMusicInPlaylistSelected(), selected);
                        } else {
                            musiqueController.clearMusicTable(view.getTableMusicInPlaylistSelected());
                        }
                    }
                    // MAJ du logo de la playlist
                    if (view.getPhotoPlaylistLabel() != null) {
                        try {
                            if (selected != null && selected.getCoverImageURL() != null && !selected.getCoverImageURL().isBlank()) {
                                ImageIcon icon = new ImageIcon(java.net.URI.create(selected.getCoverImageURL()).toURL());
                                Image img = icon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                                view.getPhotoPlaylistLabel().setIcon(new ImageIcon(img));
                            } else {
                                java.net.URL url = getClass().getResource("/PP.png");
                                if (url != null) {
                                    ImageIcon icon = new ImageIcon(url);
                                    Image img = icon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                                    view.getPhotoPlaylistLabel().setIcon(new ImageIcon(img));
                                } else {
                                    view.getPhotoPlaylistLabel().setIcon(null);
                                }
                            }
                        } catch (Exception ex) {
                            view.getPhotoPlaylistLabel().setIcon(null);
                        }
                    }
                }
            );
        }
    }

    //************************************************//
    //************** BOUTONS **************************//
    //************************************************//
    private void wireButtons() {
        playlistController.wireButtons();
        musiqueController.wireButtons();
    }

    //************************************************//
    //************** AFFICHAGE FENÊTRE ****************//
    //************************************************//
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

    //************************************************//
    //************** REFRESH MUSIQUE TABLE (MENU) *****//
    //************************************************//
    public void refreshMusicTableFromMenu() {
        if (musiqueController != null) {
            musiqueController.refreshMusicTable();
        }
    }

    //************************************************//
    //************** HELPERS **************************//
    //************************************************//
    @Override
    public void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    //************************************************//
    //************** ACCESSEURS ************************//
    //************************************************//
    public List<Playlist> getPlaylists() {
        return playlists;
    }
    public PlaylistFileDAO getPlaylistDAO() { return playlistDAO; }
    public MusicFileDAO getMusicDAO() { return musicDAO; }
    public ArtisteFileDAO getArtisteDAO() { return artisteDAO; }
    public AlbumFileDAO getAlbumDAO() { return albumDAO; }
    public MainPagePlaylistController getPlaylistController() { return playlistController; }
    public MainPageMusiqueController getMusiqueController() { return musiqueController; }
    public MainPageMenuController getMenuController() { return menuController; }
    public String getUsername() { return username; }
}
