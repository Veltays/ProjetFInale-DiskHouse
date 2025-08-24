package DiskHouse.Controller;

import DiskHouse.view.MusicEditor;
import DiskHouse.view.PlaylistEditor;
import DiskHouse.view.MainPage;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class MainPageController implements IController<MainPage> {

    private final MainPage view;

    public MainPageController(MainPage view) {
        this.view = Objects.requireNonNull(view);
    }

    @Override
    public MainPage getView() { return view; }

    @Override
    public void initController() {
        // Musique
        view.getAjouterMusique().addActionListener(e -> onOpenMusicEditor());
        view.getModifierMusique().addActionListener(e -> onOpenMusicEditor());
        view.getSupprimerMusique().addActionListener(e -> onDeleteMusic());

        // Playlist
        view.getAjouterPlaylist().addActionListener(e -> onOpenPlaylistEditor());
        view.getModifierPlaylist().addActionListener(e -> onOpenPlaylistEditor());
        view.getSupprimerPlaylist().addActionListener(e -> onDeletePlaylist());
    }

    /* ================= Actions ================= */

    private void onOpenMusicEditor() {
        SwingUtilities.invokeLater(() -> {
            try {
                MusicEditor w = new MusicEditor();
                safeShow(w);
            } catch (Throwable ex) {
                showError(view, "Impossible d'ouvrir la fenêtre AddMusic\n" + ex.getClass().getSimpleName()
                        + ": " + (ex.getMessage() == null ? "(pas de message)" : ex.getMessage()), "Erreur");
            }
        });
    }

    private void onOpenPlaylistEditor() {
        SwingUtilities.invokeLater(() -> {
            try {
                PlaylistEditor w = new PlaylistEditor();
                safeShow(w);
            } catch (Throwable ex) {
                showError(view, "Impossible d'ouvrir la fenêtre AddPlaylist\n" + ex.getClass().getSimpleName()
                        + ": " + (ex.getMessage() == null ? "(pas de message)" : ex.getMessage()), "Erreur");
            }
        });
    }

    private void onDeleteMusic() {
        showInfo(view,
                "Suppression d'une musique : logique à implémenter plus tard.",
                "Supprimer musique");
    }

    private void onDeletePlaylist() {
        showInfo(view,
                "Suppression d'une playlist : logique à implémenter plus tard.",
                "Supprimer playlist");
    }

    /* ================= Helpers ================= */

    private void safeShow(JFrame frame) {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (frame.getWidth() == 0 || frame.getHeight() == 0) {
            frame.pack();
        }
        Window owner = view.isVisible() ? view : null;
        frame.setLocationRelativeTo(owner);
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
    }
}
