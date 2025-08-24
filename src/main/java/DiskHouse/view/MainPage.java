package DiskHouse.view;

import DiskHouse.Controller.MainPageController;

import javax.swing.*;

public class MainPage extends JFrame {

    // --- Composants générés par l'UI Designer (ne pas renommer sans MAJ du .form) ---
    private JPanel MainWindow;
    private JTable tablePlaylist;
    private JTable TablePlaylist;
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
        setContentPane(MainWindow);        // on garde le GridLayoutManager de l'UI Designer
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initController();                  // branche le contrôleur
        setVisible(true);
    }

    private void initController() {
        new MainPageController(this).initController();
    }

    // --- Getters utilisés par le contrôleur (simples et suffisants) ---
    public JButton getAjouterMusique()    { return AjouterMusique; }
    public JButton getSupprimerMusique()  { return SupprimerMusique; }
    public JButton getModifierMusique()   { return ModifierMusique; }
    public JButton getAjouterPlaylist()   { return AjouterPlaylist; }
    public JButton getSupprimerPlaylist() { return SupprimerPlaylist; }
    public JButton getModifierPlaylist()  { return ModifierPlaylist; }
    public JTable  getTablePlaylist()     { return (tablePlaylist != null) ? tablePlaylist : TablePlaylist; }
    public JLabel  getPhotoPlaylist()     { return PhotoPlaylist; }

    // --- Main de test optionnel ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainPage::new);
    }
}
