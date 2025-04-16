package DiskHouse.view.view;

import javax.swing.*;


public class MainPage extends JFrame {
    private JPanel MainWindow;
    private JTable tablePlaylist;
    private JTable TablePlaylist;
    private JPanel InfoBouton;
    private JPanel Musique;
    private JPanel ListePlaylist;
    private JPanel ProfilPlaylist;
    private JPanel PlaylistNamePanel;
    private JPanel ButtonNamePanel;
    private JButton Ajouter;
    private JButton Supprimer;
    private JButton Modifier;
    private JLabel PhotoProrfil;
    private JButton Ajouter1;
    private JButton Supprimer1;
    private JButton Modifier1;
    private JScrollBar scrollBar1;
    private JScrollBar scrollBar2;


    public MainPage() {
        setTitle("main ");
        setContentPane(MainWindow); //  on définit bien Fond comme conteneur
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setVisible(true);


        styliserBouton(Ajouter);
        styliserBouton(Supprimer);
        styliserBouton(Modifier);

        styliserBouton(Ajouter1);
        styliserBouton(Supprimer1);
        styliserBouton(Modifier1);

    }
    private void styliserBouton(JButton bouton) {
        bouton.setContentAreaFilled(false);   // Enlève le fond
        bouton.setBorderPainted(false);       // Enlève le contour
        bouton.setFocusPainted(false);        // Enlève le focus (le carré)
        bouton.setOpaque(false);              // Rend transparent
    }



    public static void main(String[] args) {
        new MainPage();
    }
}
