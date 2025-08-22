package DiskHouse.Controller;

import DiskHouse.view.AddAlbum;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * Contrôleur pour la vue AlbumView
 * - Choix de pochette (clic sur l'image)
 * - Édition du nom d'album (bouton ✎)
 * - Gestion de la liste des musiques (ajout, suppression, renommage)
 * - Raccourcis : Entrée = ajouter, Suppr = supprimer, Double‑clic = renommer
 */
public class AlbumController {

    private final AddAlbum view;
    private DefaultListModel<String> songsModel;
    private String selectedCoverPath;

    public AlbumController(AddAlbum view) {
        this.view = Objects.requireNonNull(view);
    }

    public void initController() {
        // Modèle pour la liste
        if (!(view.getSongsList().getModel() instanceof DefaultListModel)) {
            songsModel = new DefaultListModel<>();
            view.getSongsList().setModel(songsModel);
        } else {
            songsModel = (DefaultListModel<String>) view.getSongsList().getModel();
        }

        // Clic pochette -> choisir image
        view.getCoverLabel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        view.getCoverLabel().setToolTipText("Cliquer pour choisir une pochette (png/jpg/jpeg)");
        view.getCoverLabel().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { chooseCover(); }
        });

        // Bouton éditer nom
        view.getEditAlbumButton().addActionListener(e -> renameAlbum());

        // Bouton ajouter / supprimer
        view.getAddSongButton().addActionListener(e -> addSong());
        view.getRemoveSongButton().addActionListener(e -> removeSelectedSong());

        // Double‑clic -> renommer morceau
        view.getSongsList().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && view.getSongsList().getSelectedIndex() >= 0) {
                    renameSelectedSong();
                }
            }
        });

        // Entrée = ajouter morceau, Suppr = supprimer
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "do-add");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "do-del");
        am.put("do-add", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { addSong(); }
        });
        am.put("do-del", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { removeSelectedSong(); }
        });
    }

    /* ===================== Actions ===================== */

    private void chooseCover() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une pochette");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));

        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            selectedCoverPath = chooser.getSelectedFile().getAbsolutePath();
            ImageIcon icon = new ImageIcon(selectedCoverPath);
            view.setCoverImage(icon.getImage());
        }
    }

    private void renameAlbum() {
        String current = view.getAlbumTitleLabel().getText();
        String name = (String) JOptionPane.showInputDialog(
                view, "Nom de l'album :", "Éditer l'album",
                JOptionPane.QUESTION_MESSAGE, null, null, current
        );
        if (name != null) {
            name = name.trim();
            if (!name.isEmpty()) {
                view.setAlbumTitle(name);
            } else {
                JOptionPane.showMessageDialog(view, "Le nom ne peut pas être vide.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addSong() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField title = new JTextField();
        JTextField duration = new JTextField(); // mm:ss
        panel.add(new JLabel("Titre :"));
        panel.add(title);
        panel.add(new JLabel("Durée (mm:ss) :"));
        panel.add(duration);

        int result = JOptionPane.showConfirmDialog(view, panel, "Ajouter une musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String t = title.getText().trim();
            String d = duration.getText().trim();
            if (t.isEmpty() || !d.matches("^\\d{1,2}:[0-5]\\d$")) {
                JOptionPane.showMessageDialog(view,
                        "Titre requis et durée au format mm:ss (ex: 03:25).",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            songsModel.addElement(t + "  (" + d + ")");
            int last = songsModel.size() - 1;
            view.getSongsList().setSelectedIndex(last);
            view.getSongsList().ensureIndexIsVisible(last);
        }
    }

    private void removeSelectedSong() {
        int idx = view.getSongsList().getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(view, "Sélectionne une musique à supprimer.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String name = songsModel.get(idx);
        int ok = JOptionPane.showConfirmDialog(view, "Supprimer \"" + name + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            songsModel.remove(idx);
        }
    }

    private void renameSelectedSong() {
        int idx = view.getSongsList().getSelectedIndex();
        if (idx < 0) return;
        String current = songsModel.get(idx);
        String nameOnly = current;
        // si format "Titre  (mm:ss)", on isole le titre
        int paren = current.lastIndexOf("  (");
        if (paren > 0) nameOnly = current.substring(0, paren);

        String newName = (String) JOptionPane.showInputDialog(
                view, "Nouveau titre :", "Renommer la musique",
                JOptionPane.QUESTION_MESSAGE, null, null, nameOnly
        );
        if (newName != null) {
            newName = newName.trim();
            if (!newName.isEmpty()) {
                // conserver la durée si présente
                String durationPart = "";
                int p = current.lastIndexOf("  (");
                if (p > 0) durationPart = current.substring(p);
                songsModel.set(idx, newName + durationPart);
            } else {
                JOptionPane.showMessageDialog(view, "Le titre ne peut pas être vide.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* ===================== Accès "modèle" ===================== */

    public DefaultListModel<String> getSongsModel() { return songsModel; }

    public String getAlbumTitle() { return view.getAlbumTitleLabel().getText(); }

    public String getSelectedCoverPath() { return selectedCoverPath; }
}
