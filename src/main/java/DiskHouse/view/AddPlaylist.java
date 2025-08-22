package DiskHouse.view;

import DiskHouse.Controller.PlaylistController;

import javax.swing.*;

/**
 * Wrapper concret pour résoudre l’erreur :
 * "incompatible types: DiskHouse.view.AddPlaylist cannot be converted to DiskHouse.view.PlaylistView"
 *
 * AddPlaylist est une vue spécifique qui RÉUTILISE PlaylistView en l’étendant.
 * Comme elle hérite de PlaylistView, on peut la passer au PlaylistController.
 */
public class AddPlaylist extends PlaylistView {

    public AddPlaylist() {
        super(); // construit la vue PlaylistView telle quelle
    }

    // Garde un main ici si tu veux lancer directement cet écran
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddPlaylist view = new AddPlaylist();
            // (optionnel) logo
            // view.setLogoFromAbsolutePath("chemin/vers/LogoMini.png", 180, 48);

            // Contrôleur
            PlaylistController controller = new PlaylistController(view);
            controller.initController();
            controller.addSeedExample(); // démo (retire si inutile)

            view.setVisible(true);
        });
    }
}
