package DiskHouse.Controller;

import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Playlist;
import DiskHouse.view.ArtisteEditor;
import DiskHouse.view.MusicEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gère la JTable des musiques : modèle, renderers, CRUD, double-clic pour ArtisteEditor.
 */
public class MainPageMusiqueController {

    final MainPageController root;

    public MainPageMusiqueController(MainPageController root) {
        this.root = root;
    }

    /* =================== Table Musiques : modèle + rendu =================== */

    /**
     * Colonnes :
     * [0] ID (cachée)
     * [1] CoverURL (cachée)
     * [2] Cellule principale (cover + 2 lignes)
     * [3] Durée (droite)
     */
    public void initMusicTable(JTable table) {
        String[] columns = { "ID", "CoverURL", "Titre / Artistes / Album", "Durée" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return String.class; }
        };
        table.setModel(model);

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(72);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        if (header != null) { header.setPreferredSize(new Dimension(0, 0)); header.setVisible(false); }

        TableColumnModel cm = table.getColumnModel();
        hideColumn(cm.getColumn(0)); // ID
        hideColumn(cm.getColumn(1)); // CoverURL

        TableColumn mainCol = cm.getColumn(2);
        mainCol.setPreferredWidth(600);
        mainCol.setCellRenderer(new MusicCellRenderer(table));

        TableColumn durCol = cm.getColumn(3);
        durCol.setPreferredWidth(80);
        durCol.setMaxWidth(100);
        durCol.setCellRenderer(new MainPagePlaylistController.RightHintRenderer());

        // Double‑clic gauche => ouvrir ArtisteEditor (avec données)
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onOpenArtisteEditorForSelectedMusic();
                }
            }
        });
    }

    private void hideColumn(TableColumn column) {
        column.setMinWidth(0); column.setMaxWidth(0); column.setPreferredWidth(0); column.setResizable(false);
    }

    public void clearMusicTable(JTable musicTable) { ((DefaultTableModel) musicTable.getModel()).setRowCount(0); }

    public void loadMusicsForPlaylist(JTable musicTable, Playlist playlist) {
        DefaultTableModel model = (DefaultTableModel) musicTable.getModel();
        model.setRowCount(0);

        if (playlist == null) return;
        String defaultCoverFromPlaylist = playlist.getCoverImageURL();

        for (Musique m : playlist.getMusiques()) {
            String title = m.getTitre();
            String artists = joinArtists(m.getArtistes());
            String album = (m.getAlbum() != null) ? m.getAlbum().getTitreAlbum() : "";
            String subtitle = (artists.isBlank() ? "" : artists) + (album.isBlank() ? "" : " • " + album);
            String duration = formatDuration(m.getDuree());

            String coverURL = (m.getCoverImageURL() != null && !m.getCoverImageURL().isBlank())
                    ? m.getCoverImageURL()
                    : defaultCoverFromPlaylist;

            model.addRow(new Object[]{
                    m.getId(),                 // [0] ID cachée
                    coverURL,                  // [1] cachée
                    title + "\n" + subtitle,   // [2] principale
                    duration                   // [3] durée
            });
        }
    }

    /* =================== Boutons Musiques (CRUD + Editors) =================== */

    public void wireButtons() {
        if (root.getV().getAjouterMusiqueButton() != null)
            root.getV().getAjouterMusiqueButton().addActionListener(e -> onOpenMusicEditorAdd());
        if (root.getV().getModifierMusiqueButton() != null)
            root.getV().getModifierMusiqueButton().addActionListener(e -> onOpenMusicEditorEdit());
        if (root.getV().getSupprimerMusiqueButton() != null)
            root.getV().getSupprimerMusiqueButton().addActionListener(e -> onDeleteMusic());
    }

    private void onOpenMusicEditorAdd() {
        SwingUtilities.invokeLater(() -> {
            try {
                MusicEditor dlg = new MusicEditor(root.getV());
                dlg.setVisible(true);
            } catch (Throwable ex) {
                root.showError(root.getV(), "Impossible d'ouvrir AddMusic\n" + ex.getMessage(), "Erreur");
            }
        });
    }

    private void onOpenMusicEditorEdit() {
        JTable playlistTable = root.getV().getTablePlaylist();
        JTable musicTable    = root.getV().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        Playlist p = getSelectedPlaylist(playlistTable);
        if (p == null) { root.showInfo(root.getV(), "Sélectionne d'abord une playlist.", "Modifier musique"); return; }

        int selMU = musicTable.getSelectedRow();
        if (selMU < 0) { root.showInfo(root.getV(), "Sélectionne une musique à modifier.", "Modifier musique"); return; }

        int modelMU = musicTable.convertRowIndexToModel(selMU);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelMU, 0); // col 0 = ID
        Musique m = findMusicById(p, musicId);
        if (m == null) { root.showError(root.getV(), "Musique introuvable.", "Erreur"); return; }

        String title = m.getTitre();
        String artists = joinArtists(m.getArtistes());
        String album = (m.getAlbum() != null) ? m.getAlbum().getTitreAlbum() : "";
        String duration = formatDuration(m.getDuree());
        String cover = (m.getCoverImageURL() != null) ? m.getCoverImageURL()
                : (p.getCoverImageURL() != null ? p.getCoverImageURL() : "");

        SwingUtilities.invokeLater(() -> {
            try {
                MusicEditor dlg = new MusicEditor(root.getV());
                dlg.loadData(title, artists, album, duration, cover);
                dlg.setVisible(true);
            } catch (Throwable ex) {
                root.showError(root.getV(), "Impossible d'ouvrir l'éditeur\n" + ex.getMessage(), "Erreur");
            }
        });
    }

    private void onDeleteMusic() {
        JTable playlistTable = root.getV().getTablePlaylist();
        JTable musicTable    = root.getV().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        Playlist p = getSelectedPlaylist(playlistTable);
        if (p == null) { root.showInfo(root.getV(), "Sélectionne une playlist.", "Supprimer musique"); return; }

        int selMU = musicTable.getSelectedRow();
        if (selMU < 0) { root.showInfo(root.getV(), "Sélectionne une musique à supprimer.", "Supprimer musique"); return; }

        int modelMU = musicTable.convertRowIndexToModel(selMU);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelMU, 0);
        Musique m = findMusicById(p, musicId);
        if (m == null) { root.showError(root.getV(), "Musique introuvable.", "Erreur"); return; }

        int confirm = JOptionPane.showConfirmDialog(
                root.getV(),
                "Supprimer la musique « " + m.getTitre() + " » ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // remove data in playlist
        p.getMusiques().removeIf(mu -> Objects.equals(mu.getId(), m.getId()));
        // update UI
        ((DefaultTableModel) musicTable.getModel()).removeRow(modelMU);
        JTable playlistJTable = root.getV().getTablePlaylist();
        if (playlistJTable != null) {
            int vr = playlistJTable.getSelectedRow();
            if (vr >= 0) {
                int mr = playlistJTable.convertRowIndexToModel(vr);
                ((DefaultTableModel) playlistJTable.getModel()).setValueAt(p.getMusiques().size(), mr, 2);
            }
        }
    }

    /* =================== Double‑clic : ouvrir ArtisteEditor (avec données) =================== */

    private void onOpenArtisteEditorForSelectedMusic() {
        JTable playlistTable = root.getV().getTablePlaylist();
        JTable musicTable    = root.getV().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        Playlist p = getSelectedPlaylist(playlistTable);
        if (p == null) { root.showInfo(root.getV(), "Sélectionne d'abord une playlist.", "Artiste"); return; }

        int selMU = musicTable.getSelectedRow();
        if (selMU < 0) { root.showInfo(root.getV(), "Double‑clique une musique.", "Artiste"); return; }

        int modelMU = musicTable.convertRowIndexToModel(selMU);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelMU, 0);
        Musique m = findMusicById(p, musicId);
        if (m == null) { root.showError(root.getV(), "Musique introuvable.", "Erreur"); return; }

        // === Données à injecter ===
        Artiste artiste = firstArtistOf(m);
        if (artiste == null) { root.showInfo(root.getV(), "Aucun artiste associé à cette musique.", "Artiste"); return; }

        List<Album> albumsDeCetArtiste = collectAlbumsOfArtist(artiste);

        // Portrait : cover musique, sinon cover playlist (optionnel)
        String portraitUrl =
                (m.getCoverImageURL() != null && !m.getCoverImageURL().isBlank()) ? m.getCoverImageURL()
                        : (p.getCoverImageURL() != null && !p.getCoverImageURL().isBlank()) ? p.getCoverImageURL()
                        : null;
        Image portrait = ArtisteEditorController.tryLoadImage(portraitUrl);

        // === Ouvrir le dialog MVC ===
        SwingUtilities.invokeLater(() -> {
            try {
                ArtisteEditor view = new ArtisteEditor(root.getV());
                ArtisteEditorController ctrl = new ArtisteEditorController(view);
                ctrl.initController();
                ctrl.loadData(artiste, albumsDeCetArtiste, portrait);
                ctrl.show(root.getV());
            } catch (Throwable ex) {
                root.showError(root.getV(), "Impossible d'ouvrir ArtisteEditor\n" + ex.getMessage(), "Erreur");
            }
        });
    }

    /* =================== Helpers données =================== */

    private Playlist getSelectedPlaylist(JTable playlistTable) {
        int sel = playlistTable.getSelectedRow();
        if (sel < 0) return null;
        int mr = playlistTable.convertRowIndexToModel(sel);
        Integer id = (Integer) playlistTable.getModel().getValueAt(mr, 0);
        for (Playlist p : root.playlists) if (Objects.equals(p.getId(), id)) return p;
        return null;
    }

    private Musique findMusicById(Playlist p, Integer musicId) {
        if (p == null || musicId == null) return null;
        for (Musique m : p.getMusiques()) if (Objects.equals(m.getId(), musicId)) return m;
        return null;
    }

    private Artiste firstArtistOf(Musique m) {
        List<Artiste> as = m.getArtistes();
        if (as == null || as.isEmpty()) return null;
        return as.get(0);
    }

    private List<Album> collectAlbumsOfArtist(Artiste target) {
        List<Album> list = new ArrayList<>();
        java.util.Set<String> seenTitles = new java.util.HashSet<>();
        for (Playlist pl : root.playlists) {
            for (Musique mu : pl.getMusiques()) {
                if (mu.getArtistes() == null) continue;
                boolean hasArtist = mu.getArtistes().stream().anyMatch(a -> sameArtist(a, target));
                if (hasArtist && mu.getAlbum() != null) {
                    String title = mu.getAlbum().getTitreAlbum();
                    if (title == null || seenTitles.add(title)) list.add(mu.getAlbum());
                }
            }
        }
        return list;
    }

    private boolean sameArtist(Artiste a, Artiste b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        String ak = (safe(a.getPrenom()) + "|" + safe(a.getNom()) + "|" + safe(getPseudo(a))).toLowerCase();
        String bk = (safe(b.getPrenom()) + "|" + safe(b.getNom()) + "|" + safe(getPseudo(b))).toLowerCase();
        return ak.equals(bk);
    }
    private String getPseudo(Artiste a) { try { return a.getPseudo(); } catch (Throwable ignored) { return ""; } }

    private String joinArtists(List<Artiste> artistes) {
        if (artistes == null || artistes.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        for (Artiste a : artistes) {
            if (a == null) continue;
            String prenom = safe(a.getPrenom());
            String nom    = safe(a.getNom());
            String full   = (prenom + " " + nom).trim();
            if (full.isBlank()) full = "Inconnu";
            names.add(full);
        }
        return String.join(", ", names);
    }

    /** "mm:ss" depuis minutes (float). */
    private String formatDuration(float minutes) {
        int totalSeconds = Math.max(0, Math.round(minutes * 60f));
        int mm = totalSeconds / 60;
        int ss = totalSeconds % 60;
        return String.format("%d:%02d", mm, ss);
    }

    private String safe(String s) { return s == null ? "" : s; }

    /* ===================== Renderer (Musiques) ===================== */

    static class MusicCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel cover = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();
        private final Image defaultImg;

        MusicCellRenderer(JTable table) {
            setLayout(new BorderLayout(12, 0)); setOpaque(true);

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            textPanel.setOpaque(false);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
            subtitle.setForeground(new Color(110, 110, 110));
            textPanel.add(title); textPanel.add(subtitle);

            add(cover, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
            setBorder(new EmptyBorder(8, 12, 8, 12));

            Image d = null;
            try { URL u = MainPageController.class.getResource("/PP.png"); if (u != null) d = new ImageIcon(u).getImage(); }
            catch (Exception ignored) {}
            defaultImg = d;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String raw = value == null ? "" : value.toString();
            String[] parts = raw.split("\n", 2);
            String t = parts.length > 0 ? parts[0] : "";
            String st = parts.length > 1 ? parts[1] : "";

            String url = null;
            try {
                int modelRow = table.convertRowIndexToModel(row);
                url = String.valueOf(table.getModel().getValueAt(modelRow, 1)); // CoverURL cachée
            } catch (Exception ignored) {}

            cover.setIcon(loadScaledIcon(url, 56, 56, defaultImg));
            title.setText(t);
            subtitle.setText(st);
            setBackground(isSelected ? new Color(232, 239, 255) : Color.WHITE);
            return this;
        }
    }

    /* ===== Utils images ===== */
    static ImageIcon loadScaledIcon(String urlOrPath, int w, int h, Image fallback) {
        Image img = null;
        try {
            if (urlOrPath != null && !urlOrPath.isBlank()) {
                if (urlOrPath.startsWith("http") || urlOrPath.startsWith("jar:") || urlOrPath.startsWith("file:")) {
                    img = new ImageIcon(URI.create(urlOrPath).toURL()).getImage();
                } else {
                    img = new ImageIcon(urlOrPath).getImage();
                }
            }
        } catch (Exception ignored) {}
        if (img == null) img = fallback;
        if (img == null) return null;
        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
