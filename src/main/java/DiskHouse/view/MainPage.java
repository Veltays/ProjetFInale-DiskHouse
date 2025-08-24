package DiskHouse.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainPage extends JFrame {
    private JPanel MainWindow;
    private JTable tablePlaylist;                    // ← table playlists (Designer)
    private JTable TableMusicInPlaylistSelected;     // ← table musiques de la playlist sélectionnée
    private JPanel InfoBouton;
    private JPanel Musique;
    private JPanel ListePlaylist;
    private JPanel ProfilPlaylist;
    private JPanel PlaylistNamePanel;
    private JPanel ButtonNamePanel;
    private JButton AjouterMusique;
    private JButton SupprimerMusique;
    private JButton ModifierMusique;
    private JLabel PhotoPlaylist;
    private JButton AjouterPlaylist;
    private JButton SupprimerPlaylist;
    private JButton ModifierPlaylist;
    private JScrollBar scrollBar1;
    private JScrollBar scrollBar2;

    public MainPage() {
        setTitle("DiskHouse");
        setContentPane(MainWindow); // respect GridLayoutManager (UI Designer)
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // style visuel (pas de logique)
        styliserBouton(AjouterMusique);
        styliserBouton(SupprimerMusique);
        styliserBouton(ModifierMusique);
        styliserBouton(AjouterPlaylist);
        styliserBouton(SupprimerPlaylist);
        styliserBouton(ModifierPlaylist);

        // ❌ NE RIEN APPELER DU CONTROLEUR ICI (MVC)
        // Le contrôleur fera l'init des JTable, le rendu et le seed.
    }

    private void styliserBouton(JButton b) {
        if (b == null) return;
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /* =============================
       === Getters pour MVC ========
       ============================= */

    /** JTable des playlists (gérée par le Designer). */
    public JTable getTablePlaylist() {
        return tablePlaylist != null ? tablePlaylist : TableMusicInPlaylistSelected; // fallback ultime si renommage
    }

    /** JTable des musiques de la playlist sélectionnée. */
    public JTable getTableMusicInPlaylistSelected() {
        return TableMusicInPlaylistSelected;
    }

    public JButton getAjouterPlaylistButton() { return AjouterPlaylist; }
    public JButton getSupprimerPlaylistButton() { return SupprimerPlaylist; }
    public JButton getModifierPlaylistButton() { return ModifierPlaylist; }

    public JLabel getPhotoPlaylistLabel() { return PhotoPlaylist; }

    public JButton getAjouterMusiqueButton() { return AjouterMusique; }
    public JButton getSupprimerMusiqueButton() { return SupprimerMusique; }
    public JButton getModifierMusiqueButton() { return ModifierMusique; }
}
