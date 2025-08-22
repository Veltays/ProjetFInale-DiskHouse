package DiskHouse.Controller;

import DiskHouse.view.ArtisteView;
import DiskHouse.view.ArtisteView.AlbumRow;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * Contrôleur de la vue ArtisteView
 * - Édition du nom d'artiste (✎)
 * - Gestion de la liste d'albums (ajout, suppression, renommage)
 * - Raccourcis : Entrée = ajouter, Suppr = supprimer, Double‑clic = renommer
 */
public class ArtisteController {

    private final ArtisteView view;
    private DefaultListModel<AlbumRow> model;

    public ArtisteController(ArtisteView view) {
        this.view = Objects.requireNonNull(view);
    }

    public void initController() {
        // Modèle JList
        if (!(view.getAlbumList().getModel() instanceof DefaultListModel)) {
            model = new DefaultListModel<>();
            view.getAlbumList().setModel(model);
        } else {
            model = (DefaultListModel<AlbumRow>) view.getAlbumList().getModel();
        }

        // Bouton ✎ (nom d'artiste)
        view.getEditArtistButton().addActionListener(e -> renameArtist());

        // Boutons +
        view.getAddAlbumButton().addActionListener(e -> addAlbum());
        view.getRemoveAlbumButton().addActionListener(e -> removeSelectedAlbum());

        // Double‑clic pour renommer un album
        view.getAlbumList().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && view.getAlbumList().getSelectedIndex() >= 0) {
                    renameSelectedAlbum();
                }
            }
        });

        // Entrée = ajouter, Suppr = supprimer
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "do-add");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "do-del");
        am.put("do-add", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { addAlbum(); }
        });
        am.put("do-del", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { removeSelectedAlbum(); }
        });

        // (Optionnel) Démo : insérer 3 lignes pour matcher la maquette
        // demoSeed();
    }

    /* ===================== Actions ===================== */

    private void renameArtist() {
        String current = view.getArtistNameLabel().getText();
        String name = (String) JOptionPane.showInputDialog(
                view, "Nom de l'artiste :", "Éditer l'artiste",
                JOptionPane.QUESTION_MESSAGE, null, null, current
        );
        if (name != null) {
            name = name.trim();
            if (!name.isEmpty()) {
                view.setArtistName(name);
            } else {
                JOptionPane.showMessageDialog(view, "Le nom ne peut pas être vide.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addAlbum() {
        // Petit formulaire
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        JTextField title = new JTextField("Album");
        JTextField subtitle = new JTextField();
        JSpinner tracks = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
        panel.add(new JLabel("Titre :"));
        panel.add(title);
        panel.add(new JLabel("Sous‑titre :"));
        panel.add(subtitle);
        panel.add(new JLabel("Nombre de titres :"));
        panel.add(tracks);

        int result = JOptionPane.showConfirmDialog(view, panel, "Ajouter un album",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        // Choix pochette (optionnelle)
        Image img = chooseImageOrNull();

        String t = title.getText().trim();
        String s = subtitle.getText().trim();
        int n = (Integer) tracks.getValue();
        if (t.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Le titre est requis.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        model.addElement(new AlbumRow(img, t, s, n));
        int last = model.size() - 1;
        view.getAlbumList().setSelectedIndex(last);
        view.getAlbumList().ensureIndexIsVisible(last);
    }

    private void removeSelectedAlbum() {
        int idx = view.getAlbumList().getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(view, "Sélectionne un album à supprimer.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        AlbumRow row = model.get(idx);
        int ok = JOptionPane.showConfirmDialog(view,
                "Supprimer \"" + (row.title != null ? row.title : "Album") + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            model.remove(idx);
        }
    }

    private void renameSelectedAlbum() {
        int idx = view.getAlbumList().getSelectedIndex();
        if (idx < 0) return;
        AlbumRow row = model.get(idx);

        JTextField title = new JTextField(row.title != null ? row.title : "");
        JTextField subtitle = new JTextField(row.subtitle != null ? row.subtitle : "");
        JSpinner tracks = new JSpinner(new SpinnerNumberModel(Math.max(0, row.trackCount), 0, 1000, 1));

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Titre :"));
        panel.add(title);
        panel.add(new JLabel("Sous‑titre :"));
        panel.add(subtitle);
        panel.add(new JLabel("Nombre de titres :"));
        panel.add(tracks);

        int result = JOptionPane.showConfirmDialog(view, panel, "Modifier l'album",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String t = title.getText().trim();
            String s = subtitle.getText().trim();
            int n = (Integer) tracks.getValue();
            if (t.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Le titre est requis.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // On conserve l'image existante
            model.set(idx, new AlbumRow(row.cover, t, s, n));
        }
    }

    /* ===================== Utilitaires ===================== */

    private Image chooseImageOrNull() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une pochette (optionnel)");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "Images (png, jpg, jpeg)", "png", "jpg", "jpeg"));

        int res = chooser.showOpenDialog(view);
        if (res == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            return new ImageIcon(chooser.getSelectedFile().getAbsolutePath()).getImage();
        }
        return null;
    }

    // Démo pour remplir la liste comme la maquette (désactivée par défaut)
    @SuppressWarnings("unused")
    private void demoSeed() {
        model.addElement(new AlbumRow(null, "Album", "Short and sweet delux", 17));
        model.addElement(new AlbumRow(null, "Album", "Short and sweet", 14));
        model.addElement(new AlbumRow(null, "Album", "Emails I can't send", 22));
    }

    /* ===================== Accès modèle ===================== */

    public DefaultListModel<AlbumRow> getAlbumListModel() { return model; }
}
