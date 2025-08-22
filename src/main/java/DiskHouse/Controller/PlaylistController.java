package DiskHouse.Controller;

import DiskHouse.view.PlaylistView;
import DiskHouse.view.PlaylistView.TrackRow;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * Contrôleur Playlist
 * - Édition du nom (✎), choix pochette (clic)
 * - Gestion liste de titres : ajouter, supprimer, renommer
 * - Raccourcis : Entrée = ajouter, Suppr = supprimer, Double‑clic = renommer
 */
public class PlaylistController {

    private final PlaylistView view;
    private DefaultListModel<TrackRow> model;
    private String selectedCoverPath;

    public PlaylistController(PlaylistView view) {
        this.view = Objects.requireNonNull(view);
    }

    public void initController() {
        // Modèle de la liste
        if (!(view.getTrackList().getModel() instanceof DefaultListModel)) {
            model = new DefaultListModel<>();
            view.getTrackList().setModel(model);
        } else {
            model = (DefaultListModel<TrackRow>) view.getTrackList().getModel();
        }

        // Pochette (clic -> choisir image)
        view.getCoverLabel().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        view.getCoverLabel().setToolTipText("Cliquer pour choisir une pochette (png/jpg/jpeg)");
        view.getCoverLabel().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { chooseCover(); }
        });

        // Bouton ✎ (nom playlist)
        view.getEditPlaylistButton().addActionListener(e -> renamePlaylist());

        // Boutons + / 🗑
        view.getAddTrackButton().addActionListener(e -> addTrack());
        view.getRemoveTrackButton().addActionListener(e -> removeSelectedTrack());

        // Double‑clic -> renommer titre
        view.getTrackList().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && view.getTrackList().getSelectedIndex() >= 0) {
                    renameSelectedTrack();
                }
            }
        });

        // Raccourcis
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "do-add");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "do-del");
        am.put("do-add", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { addTrack(); }});
        am.put("do-del", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { removeSelectedTrack(); }});
    }

    /* ===================== Seed exemple (optionnel) ===================== */
    public void addSeedExample() {
        model.addElement(new TrackRow(null, "La goat", "Sabrina Carpenter", "Short and sweet", "3 min 07"));
        model.addElement(new TrackRow(null, "Tek it", "Cafuné", "unknown", "3 min 11"));
        model.addElement(new TrackRow(null, "Feel it", "D4VD", "unknown", "2 min 35"));
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

    private void renamePlaylist() {
        String current = view.getPlaylistTitleLabel().getText();
        String name = (String) JOptionPane.showInputDialog(
                view, "Nom de la playlist :", "Éditer la playlist",
                JOptionPane.QUESTION_MESSAGE, null, null, current
        );
        if (name != null) {
            name = name.trim();
            if (!name.isEmpty()) {
                view.setPlaylistTitle(name);
            } else {
                JOptionPane.showMessageDialog(view, "Le nom ne peut pas être vide.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addTrack() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        JTextField title = new JTextField();
        JTextField artist = new JTextField();
        JTextField album  = new JTextField();
        JTextField duration = new JTextField(); // libre (ex : 3 min 07)

        panel.add(new JLabel("Titre :"));   panel.add(title);
        panel.add(new JLabel("Artiste :")); panel.add(artist);
        panel.add(new JLabel("Album :"));   panel.add(album);
        panel.add(new JLabel("Durée (ex: 3 min 07) :")); panel.add(duration);

        int result = JOptionPane.showConfirmDialog(view, panel, "Ajouter une musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String t = title.getText().trim();
            String ar = artist.getText().trim();
            String al = album.getText().trim();
            String du = duration.getText().trim();
            if (t.isEmpty() || ar.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                        "Titre et Artiste sont requis.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            model.addElement(new TrackRow(null, t, ar, al, du));
            int last = model.size() - 1;
            view.getTrackList().setSelectedIndex(last);
            view.getTrackList().ensureIndexIsVisible(last);
        }
    }

    private void removeSelectedTrack() {
        int idx = view.getTrackList().getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(view, "Sélectionne un titre à supprimer.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        TrackRow row = model.get(idx);
        int ok = JOptionPane.showConfirmDialog(view,
                "Supprimer \"" + (row.title != null ? row.title : "Titre") + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            model.remove(idx);
        }
    }

    private void renameSelectedTrack() {
        int idx = view.getTrackList().getSelectedIndex();
        if (idx < 0) return;
        TrackRow row = model.get(idx);

        JTextField title = new JTextField(row.title != null ? row.title : "");
        JTextField artist = new JTextField(row.artist != null ? row.artist : "");
        JTextField album  = new JTextField(row.album != null ? row.album : "");
        JTextField duration = new JTextField(row.duration != null ? row.duration : "");

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.add(new JLabel("Titre :"));   panel.add(title);
        panel.add(new JLabel("Artiste :")); panel.add(artist);
        panel.add(new JLabel("Album :"));   panel.add(album);
        panel.add(new JLabel("Durée :"));   panel.add(duration);

        int result = JOptionPane.showConfirmDialog(view, panel, "Modifier la musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String t = title.getText().trim();
            String ar = artist.getText().trim();
            String al = album.getText().trim();
            String du = duration.getText().trim();
            if (t.isEmpty() || ar.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                        "Titre et Artiste sont requis.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            model.set(idx, new TrackRow(row.cover, t, ar, al, du));
        }
    }

    /* ===================== Accès utiles ===================== */

    public DefaultListModel<TrackRow> getTrackListModel() { return model; }
    public String getSelectedCoverPath() { return selectedCoverPath; }
}
