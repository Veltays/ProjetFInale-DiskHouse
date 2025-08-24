package DiskHouse.Controller;

import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Playlist;
import DiskHouse.view.MainPage;
import DiskHouse.view.MusicEditor;
import DiskHouse.view.PlaylistEditor;
import DiskHouse.view.ArtisteEditor;

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
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class MainPageController implements IController<MainPage> {

    private final MainPage view;
    private List<Playlist> playlists = new ArrayList<>();

    public MainPageController(MainPage view) { this.view = Objects.requireNonNull(view); }
    @Override public MainPage getView() { return view; }

    @Override
    public void initController() {
        // === Actions "musique"
        if (view.getAjouterMusiqueButton() != null)
            view.getAjouterMusiqueButton().addActionListener(e -> onOpenMusicEditorAdd());
        if (view.getModifierMusiqueButton() != null)
            view.getModifierMusiqueButton().addActionListener(e -> onOpenMusicEditorEdit());
        if (view.getSupprimerMusiqueButton() != null)
            view.getSupprimerMusiqueButton().addActionListener(e -> onDeleteMusic());

        // === Actions "playlist"
        if (view.getAjouterPlaylistButton() != null)
            view.getAjouterPlaylistButton().addActionListener(e -> onAddPlaylist());
        if (view.getModifierPlaylistButton() != null)
            view.getModifierPlaylistButton().addActionListener(e -> onEditPlaylist());
        if (view.getSupprimerPlaylistButton() != null)
            view.getSupprimerPlaylistButton().addActionListener(e -> onDeletePlaylist());

        JTable playlistTable = view.getTablePlaylist();
        JTable musicTable    = view.getTableMusicInPlaylistSelected();

        if (playlistTable != null) initPlaylistTable(playlistTable);
        if (musicTable != null)    initMusicTable(musicTable);

        playlists = buildTestPlaylists();
        if (playlistTable != null) {
            loadPlaylistsInto(playlistTable, playlists);
            if (playlistTable.getModel().getRowCount() > 0) {
                playlistTable.setRowSelectionInterval(0, 0);
            }
        }

        if (playlistTable != null && musicTable != null) {
            playlistTable.getSelectionModel().addListSelectionListener(e -> {
                if (e.getValueIsAdjusting()) return;
                int viewRow = playlistTable.getSelectedRow();
                if (viewRow < 0) { clearMusicTable(musicTable); return; }
                int modelRow = playlistTable.convertRowIndexToModel(viewRow);
                Integer id = (Integer) playlistTable.getModel().getValueAt(modelRow, 0);
                Playlist p = findPlaylistById(id);
                if (p != null) loadMusicsForPlaylist(musicTable, p);
                else clearMusicTable(musicTable);
            });

            if (playlistTable.getModel().getRowCount() > 0) {
                int first = playlistTable.convertRowIndexToView(0);
                playlistTable.setRowSelectionInterval(first, first);
            }
        }

        // === Double‑clic sur une musique => ouvrir ArtisteEditor
        if (musicTable != null) {
            musicTable.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        onOpenArtisteEditorForSelectedMusic();
                    }
                }
            });
        }
    }

    /* =================== Ouvrir ArtisteEditor =================== */

    private void onOpenArtisteEditorForSelectedMusic() {
        JTable playlistTable = view.getTablePlaylist();
        JTable musicTable    = view.getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        int selPL = playlistTable.getSelectedRow();
        if (selPL < 0) { showInfo(view, "Sélectionne d'abord une playlist.", "Artiste"); return; }

        int selMU = musicTable.getSelectedRow();
        if (selMU < 0) { showInfo(view, "Double‑clique une musique.", "Artiste"); return; }

        int modelPL = playlistTable.convertRowIndexToModel(selPL);
        Integer playlistId = (Integer) playlistTable.getModel().getValueAt(modelPL, 0);
        Playlist p = findPlaylistById(playlistId);
        if (p == null) { showError(view, "Playlist introuvable.", "Erreur"); return; }

        int modelMU = musicTable.convertRowIndexToModel(selMU);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelMU, 0);
        Musique m = findMusicById(p, musicId);
        if (m == null) { showError(view, "Musique introuvable.", "Erreur"); return; }

        Artiste artist = firstArtistOf(m);
        if (artist == null) { showInfo(view, "Cette musique n'a pas d'artiste associé.", "Artiste"); return; }

        List<Album> albumsOfArtist = collectAlbumsOfArtist(artist);

        Image portrait = null; // si tu ajoutes plus tard un champ URL de portrait

        SwingUtilities.invokeLater(() -> {
            ArtisteEditor artistView = new ArtisteEditor();
            ArtisteEditorController controller = new ArtisteEditorController(artistView);
            controller.loadData(artist, albumsOfArtist, portrait);
            controller.show(view);
        });
    }

    private Artiste firstArtistOf(Musique m) {
        List<Artiste> as = m.getArtistes();
        if (as == null || as.isEmpty()) return null;
        return as.get(0);
    }

    private List<Album> collectAlbumsOfArtist(Artiste target) {
        List<Album> list = new ArrayList<>();
        Set<String> seenTitles = new HashSet<>();
        for (Playlist pl : playlists) {
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

    /* =================== MusicEditor =================== */

    private void onOpenMusicEditorAdd() {
        SwingUtilities.invokeLater(() -> {
            try {
                MusicEditor w = new MusicEditor(); // vierge
                safeShow(w);
            } catch (Throwable ex) {
                showError(view, "Impossible d'ouvrir AddMusic\n" + ex.getMessage(), "Erreur");
            }
        });
    }

    private void onOpenMusicEditorEdit() {
        JTable playlistTable = view.getTablePlaylist();
        JTable musicTable    = view.getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        int selPL = playlistTable.getSelectedRow();
        if (selPL < 0) { showInfo(view, "Sélectionne d'abord une playlist.", "Modifier musique"); return; }

        int selMU = musicTable.getSelectedRow();
        if (selMU < 0) { showInfo(view, "Sélectionne une musique à modifier.", "Modifier musique"); return; }

        int modelPL = playlistTable.convertRowIndexToModel(selPL);
        Integer playlistId = (Integer) playlistTable.getModel().getValueAt(modelPL, 0);
        Playlist p = findPlaylistById(playlistId);
        if (p == null) { showError(view, "Playlist introuvable.", "Erreur"); return; }

        int modelMU = musicTable.convertRowIndexToModel(selMU);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelMU, 0); // col 0 = ID
        Musique m = findMusicById(p, musicId);
        if (m == null) { showError(view, "Musique introuvable.", "Erreur"); return; }

        String title = m.getTitre();
        String artists = joinArtists(m.getArtistes());
        String album = (m.getAlbum() != null) ? m.getAlbum().getTitreAlbum() : "";
        String duration = formatDuration(m.getDuree());
        String cover = (m.getCoverImageURL() != null) ? m.getCoverImageURL()
                : (p.getCoverImageURL() != null ? p.getCoverImageURL() : "");

        SwingUtilities.invokeLater(() -> {
            try {
                MusicEditor w = new MusicEditor();
                w.loadData(title, artists, album, duration, cover);
                safeShow(w);
            } catch (Throwable ex) {
                showError(view, "Impossible d'ouvrir l'éditeur\n" + ex.getMessage(), "Erreur");
            }
        });
    }

    /* =================== PlaylistEditor (Add/Edit/Delete) =================== */

    private void onAddPlaylist() {
        Window owner = view.isVisible() ? view : null;
        PlaylistEditor editorView = new PlaylistEditor(owner);
        PlaylistEditorController ctrl = new PlaylistEditorController(editorView, new PlaylistEditorController.Listener() {
            @Override public void onPlaylistCreated(Playlist created) {
                // ajoute au modèle en mémoire
                playlists = new ArrayList<>(playlists);
                playlists.add(created);
                // refresh UI
                JTable playlistTable = view.getTablePlaylist();
                if (playlistTable != null) {
                    loadPlaylistsInto(playlistTable, playlists);
                    // select the last one (created)
                    int last = playlistTable.getRowCount() - 1;
                    if (last >= 0) playlistTable.setRowSelectionInterval(last, last);
                }
            }
            @Override public void onPlaylistUpdated(Playlist updated) {
                // rien à faire ici (création seulement), mais l'interface l'impose
            }
        });
        ctrl.openForCreate(owner);
    }

    private void onEditPlaylist() {
        JTable playlistTable = view.getTablePlaylist();
        if (playlistTable == null) return;

        int sel = playlistTable.getSelectedRow();
        if (sel < 0) { showInfo(view, "Sélectionne une playlist à modifier.", "Modifier playlist"); return; }

        int modelRow = playlistTable.convertRowIndexToModel(sel);
        Integer id = (Integer) playlistTable.getModel().getValueAt(modelRow, 0);
        Playlist p = findPlaylistById(id);
        if (p == null) { showError(view, "Playlist introuvable.", "Erreur"); return; }

        Window owner = view.isVisible() ? view : null;
        PlaylistEditor editorView = new PlaylistEditor(owner);
        PlaylistEditorController ctrl = new PlaylistEditorController(editorView, new PlaylistEditorController.Listener() {
            @Override public void onPlaylistCreated(Playlist created) { /* non utilisé ici */ }

            @Override public void onPlaylistUpdated(Playlist updated) {
                // refresh table playlists
                JTable plTable = view.getTablePlaylist();
                if (plTable != null) {
                    loadPlaylistsInto(plTable, playlists);
                    // re-sélectionne la ligne correspondante
                    for (int r = 0; r < plTable.getModel().getRowCount(); r++) {
                        Integer rid = (Integer) plTable.getModel().getValueAt(r, 0);
                        if (Objects.equals(rid, updated.getId())) {
                            int vr = plTable.convertRowIndexToView(r);
                            plTable.setRowSelectionInterval(vr, vr);
                            break;
                        }
                    }
                }
                // refresh table musiques (si cette playlist est affichée)
                JTable muTable = view.getTableMusicInPlaylistSelected();
                if (muTable != null) loadMusicsForPlaylist(muTable, updated);
            }
        });
        ctrl.openForEdit(owner, p);
    }

    private void onDeletePlaylist() {
        JTable playlistTable = view.getTablePlaylist();
        if (playlistTable == null) return;

        int sel = playlistTable.getSelectedRow();
        if (sel < 0) { showInfo(view, "Sélectionne une playlist à supprimer.", "Supprimer playlist"); return; }

        int modelRow = playlistTable.convertRowIndexToModel(sel);
        Integer id = (Integer) playlistTable.getModel().getValueAt(modelRow, 0);
        Playlist p = findPlaylistById(id);
        if (p == null) { showError(view, "Playlist introuvable.", "Erreur"); return; }

        int ok = JOptionPane.showConfirmDialog(view,
                "Supprimer la playlist \"" + p.getNomPlaylist() + "\" ?",
                "Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        // supprime du "modèle" en mémoire
        List<Playlist> copy = new ArrayList<>(playlists);
        copy.remove(p);
        playlists = copy;

        // refresh
        loadPlaylistsInto(playlistTable, playlists);
        JTable musicTable = view.getTableMusicInPlaylistSelected();
        if (musicTable != null) clearMusicTable(musicTable);
    }

    private void onDeleteMusic() {
        JTable playlistTable = view.getTablePlaylist();
        JTable musicTable    = view.getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;

        int selPL = playlistTable.getSelectedRow();
        if (selPL < 0) { showInfo(view, "Sélectionne une playlist.", "Supprimer musique"); return; }

        int selMU = musicTable.getSelectedRow();
        if (selMU < 0) { showInfo(view, "Sélectionne une musique à supprimer.", "Supprimer musique"); return; }

        int modelPL = playlistTable.convertRowIndexToModel(selPL);
        Integer playlistId = (Integer) playlistTable.getModel().getValueAt(modelPL, 0);
        Playlist p = findPlaylistById(playlistId);
        if (p == null) { showError(view, "Playlist introuvable.", "Erreur"); return; }

        int modelMU = musicTable.convertRowIndexToModel(selMU);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelMU, 0);
        Musique m = findMusicById(p, musicId);
        if (m == null) { showError(view, "Musique introuvable.", "Erreur"); return; }

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Supprimer la musique « " + m.getTitre() + " » ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // remove data in playlist
        p.getMusiques().removeIf(mu -> Objects.equals(mu.getId(), m.getId()));
        // update music JTable
        ((DefaultTableModel) musicTable.getModel()).removeRow(modelMU);
        // update counter in playlist row
        ((DefaultTableModel) playlistTable.getModel()).setValueAt(p.getMusiques().size(), modelPL, 2);
    }

    /* ========================= Helpers fenêtre / UI ========================= */

    private void safeShow(JFrame frame) {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (frame.getWidth() == 0 || frame.getHeight() == 0) frame.pack();
        Window owner = view.isVisible() ? view : null;
        frame.setLocationRelativeTo(owner);
        frame.setVisible(true);
        frame.toFront();
        frame.requestFocus();
    }

    @Override public void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    @Override public void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /* =================== Table Playlists : modèle + rendu =================== */

    private void initPlaylistTable(JTable table) {
        String[] columns = { "ID", "Nom", "# Titres", "Image (URL)" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 2 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        table.setModel(model);

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(96);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        if (header != null) { header.setPreferredSize(new Dimension(0, 0)); header.setVisible(false); }

        TableColumnModel cm = table.getColumnModel();
        if (cm.getColumnCount() >= 4) { hideColumn(cm.getColumn(0)); hideColumn(cm.getColumn(3)); }
        if (cm.getColumnCount() >= 2) {
            TableColumn nameCol = cm.getColumn(1);
            nameCol.setPreferredWidth(420);
            nameCol.setCellRenderer(new PlaylistCellRenderer(table));
        }
        if (cm.getColumnCount() >= 3) {
            TableColumn countCol = cm.getColumn(2);
            countCol.setPreferredWidth(80);
            countCol.setMaxWidth(100);
            countCol.setCellRenderer(new RightHintRenderer());
        }
    }

    private void hideColumn(TableColumn column) {
        column.setMinWidth(0); column.setMaxWidth(0); column.setPreferredWidth(0); column.setResizable(false);
    }

    private void loadPlaylistsInto(JTable table, List<Playlist> data) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Playlist p : data) {
            model.addRow(new Object[]{ p.getId(), p.getNomPlaylist(),
                    p.getMusiques() != null ? p.getMusiques().size() : 0, p.getCoverImageURL() });
        }
    }

    /* =================== Table Musiques : modèle + rendu =================== */

    /**
     * Colonnes :
     * [0] ID (cachée)
     * [1] CoverURL (cachée)
     * [2] Cellule principale (cover + 2 lignes)
     * [3] Durée (droite)
     */
    private void initMusicTable(JTable table) {
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
        durCol.setCellRenderer(new RightHintRenderer());
    }

    private void clearMusicTable(JTable musicTable) { ((DefaultTableModel) musicTable.getModel()).setRowCount(0); }

    private void loadMusicsForPlaylist(JTable musicTable, Playlist playlist) {
        DefaultTableModel model = (DefaultTableModel) musicTable.getModel();
        model.setRowCount(0);

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

    private String safe(String s) { return s == null ? "" : s; }

    /** "mm:ss" depuis minutes (float). */
    private String formatDuration(float minutes) {
        int totalSeconds = Math.max(0, Math.round(minutes * 60f));
        int mm = totalSeconds / 60;
        int ss = totalSeconds % 60;
        return String.format("%d:%02d", mm, ss);
    }

    private Playlist findPlaylistById(Integer id) {
        if (id == null) return null;
        for (Playlist p : playlists) if (Objects.equals(p.getId(), id)) return p;
        return null;
    }

    private Musique findMusicById(Playlist p, Integer musicId) {
        if (p == null || musicId == null) return null;
        for (Musique m : p.getMusiques()) if (Objects.equals(m.getId(), musicId)) return m;
        return null;
    }

    /* ===================== Renderers ===================== */

    private static class PlaylistCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel cover = new JLabel();
        private final JLabel title = new JLabel();
        private final JTable table;
        private final Image defaultImg;

        PlaylistCellRenderer(JTable table) {
            this.table = table;
            setLayout(new BorderLayout(12, 0)); setOpaque(true);
            cover.setOpaque(false);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            add(cover, BorderLayout.WEST); add(title, BorderLayout.CENTER);
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
            String name = value == null ? "" : value.toString();
            String url = null;
            try {
                int modelRow = table.convertRowIndexToModel(row);
                url = String.valueOf(table.getModel().getValueAt(modelRow, 3)); // col Image (URL)
            } catch (Exception ignored) {}

            cover.setIcon(loadScaledIcon(url, 72, 72, defaultImg));
            title.setText(name);
            setBackground(isSelected ? new Color(232, 239, 255) : Color.WHITE);
            return this;
        }
    }

    private static class MusicCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel cover = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();
        private final JTable table;
        private final Image defaultImg;

        MusicCellRenderer(JTable table) {
            this.table = table;
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

    private static class RightHintRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        RightHintRenderer() {
            setOpaque(true); setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(new EmptyBorder(0, 0, 0, 16)); setForeground(new Color(90, 90, 90));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText(value == null ? "" : value.toString());
            setBackground(isSelected ? new Color(232, 239, 255) : Color.WHITE);
            return this;
        }
    }

    private static ImageIcon loadScaledIcon(String urlOrPath, int w, int h, Image fallback) {
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

    /* ===================== Données de test ===================== */

    private List<Playlist> buildTestPlaylists() {
        Artiste daft     = new Artiste("Bangalter", "Thomas", "Daft Punk", null);
        Artiste justice  = new Artiste("Augé", "Gaspard", "Justice", null);
        Artiste coldplay = new Artiste("Martin", "Chris",  "Coldplay",  null);

        Album homework   = new Album("Homework",   LocalDate.of(1997, 1, 20),  null);
        homework.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/9/9c/Daft_Punk_-_Homework.png");

        Album cross      = new Album("† (Cross)",  LocalDate.of(2007, 6, 11),  null);
        cross.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/2/2c/Justice_-_Cross_%282007%29.png");

        Album parachutes = new Album("Parachutes", LocalDate.of(2000, 7, 10),  null);
        parachutes.setCoverImageURL(null);

        Musique aroundTheWorld = new Musique("Around The World", 7.1f, homework,   List.of(daft));
        aroundTheWorld.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/0/0b/Daft_Punk_-_Around_the_World.jpg");

        Musique daFunk         = new Musique("Da Funk",          5.3f, homework,   List.of(daft));
        daFunk.setCoverImageURL(null);

        Musique genesis        = new Musique("Genesis",          3.5f, cross,      List.of(justice));
        genesis.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/2/25/Justice_Genesis.jpg");

        Musique stress         = new Musique("Stress",           4.5f, cross,      List.of(justice));
        stress.setCoverImageURL(null);

        Musique yellow         = new Musique("Yellow",           4.3f, parachutes, List.of(coldplay));
        yellow.setCoverImageURL("https://upload.wikimedia.org/wikipedia/en/5/5e/Coldplay_-_Yellow_%28single%29.png");

        Musique trouble        = new Musique("Trouble",          4.0f, parachutes, List.of(coldplay));
        trouble.setCoverImageURL(null);

        Playlist electro = new Playlist("Electro Classics", new ArrayList<>());
        electro.setCoverImageURL("https://images.unsplash.com/photo-1511379938547-c1f69419868d?q=80&w=1200");
        electro.ajouterMusique(aroundTheWorld);
        electro.ajouterMusique(daFunk);
        electro.ajouterMusique(genesis);
        electro.ajouterMusique(stress);

        Playlist chill = new Playlist("Chill Evenings", new ArrayList<>());
        chill.setCoverImageURL(null);
        chill.ajouterMusique(yellow);
        chill.ajouterMusique(trouble);

        return List.of(electro, chill);
    }
}
