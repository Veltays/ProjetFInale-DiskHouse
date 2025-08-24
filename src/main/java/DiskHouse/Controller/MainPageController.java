package DiskHouse.Controller;

import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Playlist;
import DiskHouse.view.MainPage;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * Orchestrateur : instancie la vue, les sous-contrôleurs et partage l'état (playlists).
 * - Respect MVC : pas de logique d'affichage ici (délégué aux sous-contrôleurs).
 */
public class MainPageController implements IController<MainPage> {

    private final MainPage view;
    /** État partagé entre les sous-contrôleurs */
    final List<Playlist> playlists = new ArrayList<>();

    // Sous-contrôleurs
    private MainPagePlaylistController playlistCtrl;
    private MainPageMusiqueController  musiqueCtrl;
    private MainPageMenuController     menuCtrl; // placeholder

    public MainPageController(MainPage view) {
        this.view = Objects.requireNonNull(view);
    }

    @Override public MainPage getView() { return view; }

    @Override
    public void initController() {
        // 1) Instancier sous-contrôleurs
        playlistCtrl = new MainPagePlaylistController(this);
        musiqueCtrl  = new MainPageMusiqueController(this);
        menuCtrl     = new MainPageMenuController(this); // vide pour l’instant

        // 2) Initialiser tables
        if (view.getTablePlaylist() != null) playlistCtrl.initPlaylistTable(view.getTablePlaylist());
        if (view.getTableMusicInPlaylistSelected() != null) musiqueCtrl.initMusicTable(view.getTableMusicInPlaylistSelected());

        // 3) Boucherie de test (remplaçable par DAO)
        playlists.clear();
        playlists.addAll(buildTestPlaylists());

        // 4) Charger la table playlists + lier sélection -> musiques
        if (view.getTablePlaylist() != null) {
            playlistCtrl.loadPlaylistsInto(view.getTablePlaylist(), playlists, selected -> {
                // Callback quand la sélection change
                if (selected != null && view.getTableMusicInPlaylistSelected() != null) {
                    musiqueCtrl.loadMusicsForPlaylist(view.getTableMusicInPlaylistSelected(), selected);
                } else if (view.getTableMusicInPlaylistSelected() != null) {
                    musiqueCtrl.clearMusicTable(view.getTableMusicInPlaylistSelected());
                }
            });
        }

        // 5) Actions boutons
        playlistCtrl.wireButtons();  // Ajouter/Modifier/Supprimer playlist
        musiqueCtrl.wireButtons();   // Ajouter/Modifier/Supprimer musique + double-clic artiste
    }

    /* ========================= Utilitaires fenêtrage & dialogues ========================= */

    void safeShow(JFrame frame) {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (frame.getWidth() == 0 || frame.getHeight() == 0) frame.pack();
        Window owner = view.isVisible() ? view : null;
        frame.setLocationRelativeTo(owner);
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
    }

    @Override public void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    @Override public void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    MainPage getV() { return view; }

    /* ===================== Données de test A REMPLACER PAR UN DAO ===================== */

    private List<Playlist> buildTestPlaylists() {
        Artiste daft     = new Artiste("Bangalter", "Thomas", "Daft Punk", null);
        Artiste justice  = new Artiste("Augé", "Gaspard", "Justice", null);
        Artiste coldplay = new Artiste("Martin", "Chris",  "Coldplay",  null);

        Album homework   = new Album("Homework",   LocalDate.of(1997, 1, 20),  null);
        homework.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/9/9c/Daft_Punk_-_Homework.png");

        Album cross      = new Album("† (Cross)",  LocalDate.of(2007, 6, 11),  null);
        cross.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/2/2c/Justice_-_Cross_%282007%29.png");

        Album parachutes = new Album("Parachutes", LocalDate.of(2000, 7, 10),  null);
        parachutes.setCoverImageURL(null);

        Musique aroundTheWorld = new Musique("Around The World", 7.1f, homework,   List.of(daft));
        aroundTheWorld.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/0/0b/Daft_Punk_-_Around_the_World.jpg");

        Musique daFunk         = new Musique("Da Funk",          5.3f, homework,   List.of(daft));
        daFunk.setCoverImageURL(null);

        Musique genesis        = new Musique("Genesis",          3.5f, cross,      List.of(justice));
        genesis.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/2/25/Justice_Genesis.jpg");

        Musique stress         = new Musique("Stress",           4.5f, cross,      List.of(justice));
        stress.setCoverImageURL(null);

        Musique yellow         = new Musique("Yellow",           4.3f, parachutes, List.of(coldplay));
        yellow.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/5/5e/Coldplay_-_Yellow_%28single%29.png");

        Musique trouble        = new Musique("Trouble",          4.0f, parachutes, List.of(coldplay));
        trouble.setCoverImageURL(null);

        Playlist electro = new Playlist("Electro Classics", new ArrayList<>());
        electro.setCoverImageURL("https://images.unsplash.com/photo-1511379938547-c1f69419868d?q=80&w=1200");
        electro.ajouterMusique(aroundTheWorld);
        electro.ajouterMusique(daFunk);
        electro.ajouterMusique(genesis);
        electro.ajouterMusique(stress);

        Playlist chill = new Playlist("Chill Evenings", new ArrayList<>());
        chill.setCoverImageURL(null);
        chill.ajouterMusique(yellow);
        chill.ajouterMusique(trouble);

        return List.of(electro, chill);
    }
}
