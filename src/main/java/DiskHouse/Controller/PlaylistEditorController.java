package DiskHouse.Controller;

import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Playlist;
import DiskHouse.view.PlaylistEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contrôleur de l'éditeur de playlist.
 * - Peut fonctionner en mode création (playlist == null) ou édition (playlist existante)
 * - Gère choix d'image, ajout/suppression de musiques, et validation.
 *
 * -> Le résultat est renvoyé via le listener (onPlaylistCreated / onPlaylistUpdated).
 */
public class PlaylistEditorController {

    /** Callback pour renvoyer le résultat au MainPageController (MVC). */
    public interface Listener {
        void onPlaylistCreated(Playlist created);
        void onPlaylistUpdated(Playlist updated);
    }

    private final PlaylistEditor view;
    private final Listener listener;

    /** null => création ; sinon édition */
    private Playlist editing;

    /** image choisie (URL externe ou file:…) */
    private String coverURL;

    public PlaylistEditorController(PlaylistEditor view, Listener listener) {
        this.view = Objects.requireNonNull(view);
        this.listener = Objects.requireNonNull(listener);
        wireUI();
    }

    private void wireUI() {
        view.getCoverLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { onChooseImage(); }
        });
        view.getAddMusicBtn().addActionListener(e -> onAddMusic());
        view.getDelMusicBtn().addActionListener(e -> onDeleteMusic());
        view.getSaveBtn().addActionListener(e -> onSave());
        view.getCancelBtn().addActionListener(e -> view.dispose());
    }

    /* ===================== Public API ===================== */

    public void openForCreate(Window owner) {
        editing = null;
        coverURL = null;
        setCoverIcon(null);
        view.getNameField().setText("");
        clearTable();
        show(owner);
    }

    public void openForEdit(Window owner, Playlist playlist) {
        editing = Objects.requireNonNull(playlist);
        coverURL = playlist.getCoverImageURL();
        setCoverIcon(coverURL);
        view.getNameField().setText(playlist.getNomPlaylist());
        reloadFromPlaylist(playlist);
        show(owner);
    }

    /* ===================== Core actions ===================== */

    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une image");
        int res = chooser.showOpenDialog(view);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                coverURL = f.toURI().toString();       // file:/… pour être compatible partout
                setCoverIcon(coverURL);
            }
        }
    }

    private void onAddMusic() {
        // petite boite de saisie rapide (tu peux remplacer plus tard par ton MusicEditor)
        JTextField tTitre = new JTextField();
        JTextField tArtistes = new JTextField();
        JTextField tAlbum = new JTextField();
        JTextField tDuree = new JTextField(); // mm:ss

        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.add(new JLabel("Titre :"));   p.add(tTitre);
        p.add(new JLabel("Artistes (séparés par ,) :")); p.add(tArtistes);
        p.add(new JLabel("Album :"));  p.add(tAlbum);
        p.add(new JLabel("Durée (mm:ss) :")); p.add(tDuree);

        int ok = JOptionPane.showConfirmDialog(view, p, "Ajouter une musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        String titre = tTitre.getText().trim();
        String artists = tArtistes.getText().trim();
        String album = tAlbum.getText().trim();
        String duree = tDuree.getText().trim();

        if (titre.isBlank()) {
            JOptionPane.showMessageDialog(view, "Titre obligatoire.", "Musique", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // construction entités minimales
        List<Artiste> as = new ArrayList<>();
        if (!artists.isBlank()) {
            for (String part : artists.split(",")) {
                String n = part.trim();
                as.add(new Artiste("", n, n, null)); // prénom vide, nom/pseudo = n (simple et suffisant ici)
            }
        }
        Album al = album.isBlank() ? null : new Album(album, java.time.LocalDate.now(), null);
        float minutes = parseMmSs(duree);

        Musique m = new Musique(titre, minutes, al, as);
        // cover de la track : on laisse vide ici ; la playlist fera fallback visuel

        // ajoute au tableau
        DefaultTableModel model = (DefaultTableModel) view.getMusicTable().getModel();
        model.addRow(new Object[] {
                m.getId(), m.getTitre(), joinArtists(as), (al != null ? al.getTitreAlbum() : ""),
                formatMmSs(minutes), m.getCoverImageURL() == null ? "" : m.getCoverImageURL()
        });
    }

    private void onDeleteMusic() {
        JTable t = view.getMusicTable();
        int row = t.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view, "Sélectionne une musique.", "Supprimer", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = t.convertRowIndexToModel(row);
        ((DefaultTableModel) t.getModel()).removeRow(modelRow);
    }

    private void onSave() {
        String name = view.getNameField().getText().trim();
        if (name.isBlank()) {
            JOptionPane.showMessageDialog(view, "Le nom de la playlist est obligatoire.", "Playlist", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Reconstituer la liste des musiques à partir du tableau
        List<Musique> musiques = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) view.getMusicTable().getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            Integer id = (Integer) model.getValueAt(i, 0);
            String titre = asString(model.getValueAt(i, 1));
            String artists = asString(model.getValueAt(i, 2));
            String album = asString(model.getValueAt(i, 3));
            String duree = asString(model.getValueAt(i, 4));
            String trackCover = asString(model.getValueAt(i, 5));

            List<Artiste> as = parseArtists(artists);
            Album al = album.isBlank() ? null : new Album(album, java.time.LocalDate.now(), null);
            Musique m = new Musique(titre, parseMmSs(duree), al, as);
            if (trackCover != null && !trackCover.isBlank()) m.setCoverImageURL(trackCover);
            // NOTE: l’ID vient de super(); pas besoin de setId
            musiques.add(m);
        }

        if (editing == null) {
            // création
            Playlist created = new Playlist(name, musiques);
            if (coverURL != null && !coverURL.isBlank()) created.setCoverImageURL(coverURL);
            listener.onPlaylistCreated(created);
        } else {
            // mise à jour en place
            editing.setNomPlaylist(name);
            editing.setMusiques(musiques);
            if (coverURL != null) editing.setCoverImageURL(coverURL);
            listener.onPlaylistUpdated(editing);
        }

        view.dispose();
    }

    /* ===================== Helpers ===================== */

    private void show(Window owner) {
        if (owner != null) view.setLocationRelativeTo(owner);
        view.pack();
        view.setVisible(true);
    }

    private void clearTable() {
        ((DefaultTableModel) view.getMusicTable().getModel()).setRowCount(0);
    }

    private void reloadFromPlaylist(Playlist p) {
        clearTable();
        DefaultTableModel model = (DefaultTableModel) view.getMusicTable().getModel();
        for (Musique m : p.getMusiques()) {
            String album = (m.getAlbum() != null ? m.getAlbum().getTitreAlbum() : "");
            model.addRow(new Object[] {
                    m.getId(), m.getTitre(), joinArtists(m.getArtistes()), album,
                    formatMmSs(m.getDuree()), m.getCoverImageURL() == null ? "" : m.getCoverImageURL()
            });
        }
    }

    private String joinArtists(List<Artiste> as) {
        if (as == null || as.isEmpty()) return "";
        List<String> n = new ArrayList<>();
        for (Artiste a : as) {
            String prenom = safe(a.getPrenom());
            String nom = safe(a.getNom());
            String full = (prenom + " " + nom).trim();
            if (full.isBlank()) full = safe(a.getPseudo());
            if (full.isBlank()) full = "Inconnu";
            n.add(full);
        }
        return String.join(", ", n);
    }

    private List<Artiste> parseArtists(String s) {
        List<Artiste> list = new ArrayList<>();
        if (s == null || s.isBlank()) return list;
        for (String part : s.split(",")) {
            String n = part.trim();
            list.add(new Artiste("", n, n, null));
        }
        return list;
    }

    private String formatMmSs(float minutes) {
        int total = Math.max(0, Math.round(minutes * 60f));
        int mm = total / 60, ss = total % 60;
        return String.format("%d:%02d", mm, ss);
    }

    private float parseMmSs(String mmss) {
        if (mmss == null || mmss.isBlank()) return 0f;
        try {
            String[] p = mmss.trim().split("[:m ]+");
            int mm = Integer.parseInt(p[0]);
            int ss = (p.length > 1) ? Integer.parseInt(p[1]) : 0;
            return (mm * 60 + ss) / 60f;
        } catch (Exception e) {
            return 0f;
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private String asString(Object o) { return (o == null) ? "" : o.toString(); }

    private void setCoverIcon(String urlOrFile) {
        Image img = null;
        try {
            if (urlOrFile != null && !urlOrFile.isBlank()) {
                if (urlOrFile.startsWith("http") || urlOrFile.startsWith("file:")) {
                    img = new ImageIcon(URI.create(urlOrFile).toURL()).getImage();
                } else {
                    img = new ImageIcon(urlOrFile).getImage();
                }
            }
        } catch (Exception ignored) {}
        if (img == null) {
            // carré vide
            BufferedImagePlaceholder ph = new BufferedImagePlaceholder(160,160, new Color(240,243,252));
            img = ph.getImage();
        }
        Image scaled = img.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        view.getCoverLabel().setIcon(new ImageIcon(scaled));
    }

    /** Petit placeholder simple (évite dépendance) */
    private static class BufferedImagePlaceholder {
        private final java.awt.image.BufferedImage img;
        BufferedImagePlaceholder(int w, int h, Color bg) {
            img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(bg); g.fillRoundRect(0,0,w,h,24,24);
            g.setColor(new Color(100,120,180));
            g.drawString("Cliquer\npour l'image", 24, h/2);
            g.dispose();
        }
        Image getImage() { return img; }
    }
}
