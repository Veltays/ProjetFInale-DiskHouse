package DiskHouse.Controller;

import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Playlist;
import DiskHouse.view.PlaylistEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contrôleur de l'éditeur de playlist (compatible MVC).
 * - Mode création (editing == null) ou édition (editing != null)
 * - Choix cover, ajout/suppression musiques, validation
 * - Résultat renvoyé via Listener
 */
public class PlaylistEditorController {

    // === Callback pour la page principale ===
    public interface Listener {
        void onPlaylistCreated(Playlist created);
        void onPlaylistUpdated(Playlist updated);
    }

    private final PlaylistEditor view;
    private final Listener listener;

    private Playlist editing;   // null => création
    private String coverURL;    // file:/… ou http(s)://…

    public PlaylistEditorController(PlaylistEditor view, Listener listener) {
        this.view = Objects.requireNonNull(view);
        this.listener = Objects.requireNonNull(listener);
        wireUI();
    }

    private void wireUI() {
        view.getCoverLabel().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { onChooseImage(); }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { view.getCoverLabel().setBackground(new Color(235,239,252)); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { view.getCoverLabel().setBackground(new Color(245,247,252)); }
        });
        view.getAddMusicBtn().addActionListener(e -> onAddMusic());
        view.getDelMusicBtn().addActionListener(e -> onDeleteMusic());
        view.getSaveBtn().addActionListener(e -> onSave());
        view.getCancelBtn().addActionListener(e -> view.dispose());
    }

    // === API publique ===
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

    // === Actions ===
    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une image");
        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                coverURL = f.toURI().toString(); // file:/…
                setCoverIcon(coverURL);
            }
        }
    }

    private void onAddMusic() {
        // saisie simple (tu pourras remplacer par ton MusicEditor)
        JTextField tTitre = new JTextField();
        JTextField tArtistes = new JTextField();
        JTextField tAlbum = new JTextField();
        JTextField tDuree = new JTextField(); // mm:ss

        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.add(new JLabel("Titre :"));   p.add(tTitre);
        p.add(new JLabel("Artistes (séparés par ,) :")); p.add(tArtistes);
        p.add(new JLabel("Album :"));   p.add(tAlbum);
        p.add(new JLabel("Durée (mm:ss) :")); p.add(tDuree);

        if (JOptionPane.showConfirmDialog(view, p, "Ajouter une musique",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String titre = tTitre.getText().trim();
        String artists = tArtistes.getText().trim();
        String album = tAlbum.getText().trim();
        String duree = tDuree.getText().trim();

        if (titre.isBlank()) {
            JOptionPane.showMessageDialog(view, "Titre obligatoire.", "Musique", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Artiste> as = parseArtists(artists);
        Album al = album.isBlank() ? null : new Album(album, LocalDate.now(), null);
        float minutes = parseMmSs(duree);

        Musique m = new Musique(titre, minutes, al, as);

        DefaultTableModel model = (DefaultTableModel) view.getMusicTable().getModel();
        model.addRow(new Object[]{
                String.valueOf(m.getId()),
                m.getTitre(),
                joinArtists(as),
                (al != null ? al.getTitreAlbum() : ""),
                formatMmSs(minutes),
                (m.getCoverImageURL() == null ? "" : m.getCoverImageURL())
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

        // Reconstitue la liste depuis la JTable
        List<Musique> musiques = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) view.getMusicTable().getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String titre   = asString(model.getValueAt(i, 1));
            String artists = asString(model.getValueAt(i, 2));
            String album   = asString(model.getValueAt(i, 3));
            String duree   = asString(model.getValueAt(i, 4));
            String trackCover = asString(model.getValueAt(i, 5));

            List<Artiste> as = parseArtists(artists);
            Album al = album.isBlank() ? null : new Album(album, LocalDate.now(), null);
            Musique m = new Musique(titre, parseMmSs(duree), al, as);
            if (!trackCover.isBlank()) m.setCoverImageURL(trackCover);
            musiques.add(m);
        }

        if (editing == null) {
            Playlist created = new Playlist(name, musiques);
            if (coverURL != null && !coverURL.isBlank()) created.setCoverImageURL(coverURL);
            listener.onPlaylistCreated(created);
        } else {
            editing.setNomPlaylist(name);
            editing.setMusiques(musiques);
            if (coverURL != null) editing.setCoverImageURL(coverURL);
            listener.onPlaylistUpdated(editing);
        }

        view.dispose();
    }

    // === Helpers ===
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
            model.addRow(new Object[]{
                    String.valueOf(m.getId()),
                    m.getTitre(),
                    joinArtists(m.getArtistes()),
                    album,
                    formatMmSs(m.getDuree()),
                    (m.getCoverImageURL() == null ? "" : m.getCoverImageURL())
            });
        }
    }

    private String joinArtists(List<Artiste> as) {
        if (as == null || as.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        for (Artiste a : as) {
            String prenom = safe(a.getPrenom());
            String nom = safe(a.getNom());
            String full = (prenom + " " + nom).trim();
            if (full.isBlank()) full = safe(a.getPseudo());
            if (full.isBlank()) full = "Inconnu";
            names.add(full);
        }
        return String.join(", ", names);
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
        if (img == null) img = placeholder(160, 160);
        Image scaled = img.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        view.getCoverLabel().setIcon(new ImageIcon(scaled));
    }

    private Image placeholder(int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(240, 243, 252));
        g.fillRoundRect(0, 0, w, h, 24, 24);
        g.setColor(new Color(100, 120, 180));
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 12f));
        String l1 = "Cliquer pour";
        String l2 = "choisir l'image";
        FontMetrics fm = g.getFontMetrics();
        int y = h / 2 - 4;
        g.drawString(l1, (w - fm.stringWidth(l1)) / 2, y);
        g.drawString(l2, (w - fm.stringWidth(l2)) / 2, y + fm.getHeight());
        g.dispose();
        return bi;
    }
}
