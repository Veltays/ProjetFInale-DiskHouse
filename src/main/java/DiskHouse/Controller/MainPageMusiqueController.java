//************************************************//
//************** MAIN PAGE MUSIQUE CONTROLLER *****//
//************************************************//
// Contrôleur pour la gestion des musiques dans la page principale.
// Gère l'initialisation de la table, les actions CRUD, le rendu personnalisé et la synchronisation avec le modèle.

package DiskHouse.Controller;

import DiskHouse.model.DAO.AlbumFileDAO;
import DiskHouse.model.DAO.ArtisteFileDAO;
import DiskHouse.model.DAO.MusicFileDAO;
import DiskHouse.model.DAO.PlaylistFileDAO;
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

public class MainPageMusiqueController {

    //************************************************//
    //************** ATTRIBUTS ************************//
    //************************************************//
    private final MainPageController root;
    private String dateType = "Date de sortie";
    private final MusicFileDAO musicDAO = new MusicFileDAO("data/musiques.dat");
    private final ArtisteFileDAO artisteDAO = new ArtisteFileDAO("data/artistes.dat");
    private final AlbumFileDAO albumDAO = new AlbumFileDAO("data/albums.dat");

    //************************************************//
    //************** CONSTRUCTEUR *********************//
    //************************************************//
    public MainPageMusiqueController(MainPageController root) {
        this.root = root;
        JComboBox<String> combo = root.getView().getDateTypeComboBox();
        if (combo != null) {
            combo.addActionListener(e -> {
                dateType = (String) combo.getSelectedItem();
                JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
                JTable playlistTable = root.getView().getTablePlaylist();
                Playlist playlist = getSelectedPlaylist(playlistTable);
                loadMusicsForPlaylist(musicTable, playlist);
            });
        }
    }

    //************************************************//
    //************** INITIALISATION TABLE *************//
    //************************************************//
    public void initMusicTable(JTable table) {
        String[] columns = {"ID", "CoverURL", "Titre / Artistes / Album", "Durée", "Date"};
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
        if (header != null) {
            header.setPreferredSize(new Dimension(0, 0));
            header.setVisible(false);
        }
        TableColumnModel cm = table.getColumnModel();
        hideColumn(cm.getColumn(0)); // ID
        hideColumn(cm.getColumn(1)); // CoverURL
        cm.getColumn(2).setPreferredWidth(350);
        cm.getColumn(2).setMaxWidth(400);
        cm.getColumn(2).setCellRenderer(new MusicCellRenderer(table));
        cm.getColumn(3).setPreferredWidth(80);
        cm.getColumn(3).setMaxWidth(100);
        cm.getColumn(3).setCellRenderer(new MainPagePlaylistController.RightHintRenderer());
        cm.getColumn(4).setPreferredWidth(200);
        cm.getColumn(4).setMaxWidth(250);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onOpenArtisteEditorForSelectedMusic();
                }
            }
        });
    }

    private void hideColumn(TableColumn column) {
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
        column.setResizable(false);
    }

    public void clearMusicTable(JTable table) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
    }

    public void refreshMusicTable() {
        JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
        JTable playlistTable = root.getView().getTablePlaylist();
        Playlist playlist = getSelectedPlaylist(playlistTable);
        loadMusicsForPlaylist(musicTable, playlist);
    }

    public void loadMusicsForPlaylist(JTable table, Playlist playlist) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        if (playlist == null) return;
        String defaultCover = playlist.getCoverImageURL();
        for (Musique m : playlist.getMusiques()) {
            String title = m.getTitre();
            String artists = joinArtists(m.getArtistes());
            String album = (m.getAlbum() != null) ? m.getAlbum().getTitreAlbum() : "";
            String subtitle = "";
            if (!artists.isBlank()) subtitle += artists;
            if (!album.isBlank()) subtitle += (subtitle.isBlank() ? "" : " • ") + album;
            String duration = formatDuration(m.getDuree());
            String cover = (m.getCoverImageURL() != null && !m.getCoverImageURL().isBlank()) ? m.getCoverImageURL() : defaultCover;
            String date = "";
            if ("Date d'ajout".equals(dateType)) {
                if (m.getDateAjout() != null) {
                    date = m.getDateAjout().format(root.getMenuController().getDateFormatter());
                }
            } else if (m.getAlbum() != null && m.getAlbum().getDateSortie() != null) {
                date = m.getAlbum().getDateSortie().format(root.getMenuController().getDateFormatter());
            }
            model.addRow(new Object[]{m.getId(), cover, title + "\n" + subtitle, duration, date});
        }
    }

    //************************************************//
    //************** BOUTONS CRUD *********************//
    //************************************************//
    public void wireButtons() {
        if (root.getView().getAjouterMusiqueButton() != null) {
            root.getView().getAjouterMusiqueButton().addActionListener(e -> onAddMusicPersisted());
        }
        if (root.getView().getModifierMusiqueButton() != null) {
            root.getView().getModifierMusiqueButton().addActionListener(e -> onEditMusicPersisted());
        }
        if (root.getView().getSupprimerMusiqueButton() != null) {
            root.getView().getSupprimerMusiqueButton().addActionListener(e -> onDeleteMusicPersisted());
        }
    }

    private void onAddMusicPersisted() {
        JTable playlistTable = root.getView().getTablePlaylist();
        JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;
        Playlist playlist = getSelectedPlaylist(playlistTable);
        if (playlist == null) {
            root.showInfo(root.getView(), "Sélectionne d'abord une playlist.", "Ajouter musique");
            return;
        }
        MusicEditor dlg = new MusicEditor(root.getView(), false, root.getUsername());
        MusicEditorController ctrl = dlg.wireControllerWith(root.getUsername(), new MusicEditorController.Listener() {
            @Override
            public void onMusicCreated(Musique created) {
                playlist.ajouterMusique(created);
                root.getPlaylistDAO().update(playlist);
                loadMusicsForPlaylist(musicTable, playlist);
                int last = musicTable.getModel().getRowCount() - 1;
                if (last >= 0) musicTable.setRowSelectionInterval(last, last);
            }
            @Override
            public void onMusicUpdated(Musique updated) { }
        });
        ctrl.openForCreate(root.getView());
    }

    private void onEditMusicPersisted() {
        JTable playlistTable = root.getView().getTablePlaylist();
        JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;
        Playlist playlist = getSelectedPlaylist(playlistTable);
        if (playlist == null) {
            root.showInfo(root.getView(), "Sélectionne d'abord une playlist.", "Modifier musique");
            return;
        }
        int sel = musicTable.getSelectedRow();
        if (sel < 0) {
            root.showInfo(root.getView(), "Sélectionne une musique.", "Modifier musique");
            return;
        }
        int modelRow = musicTable.convertRowIndexToModel(sel);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelRow, 0);
        Musique music = findMusicById(playlist, musicId);
        if (music == null) {
            root.showError(root.getView(), "Musique introuvable.", "Erreur");
            return;
        }
        MusicEditor dlg = new MusicEditor(root.getView(), false, root.getUsername());
        MusicEditorController ctrl = dlg.wireControllerWith(root.getUsername(), new MusicEditorController.Listener() {
            @Override
            public void onMusicCreated(Musique created) { }
            @Override
            public void onMusicUpdated(Musique updated) {
                root.getPlaylistDAO().update(playlist);
                loadMusicsForPlaylist(musicTable, playlist);
                int rowToSelect = Math.min(modelRow, musicTable.getModel().getRowCount() - 1);
                if (rowToSelect >= 0) musicTable.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        });
        ctrl.openForEdit(root.getView(), music);
    }

    private void onDeleteMusicPersisted() {
        JTable playlistTable = root.getView().getTablePlaylist();
        JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;
        Playlist playlist = getSelectedPlaylist(playlistTable);
        if (playlist == null) {
            root.showInfo(root.getView(), "Sélectionne d'abord une playlist.", "Supprimer musique");
            return;
        }
        int sel = musicTable.getSelectedRow();
        if (sel < 0) {
            root.showInfo(root.getView(), "Sélectionne une musique.", "Supprimer musique");
            return;
        }
        int modelRow = musicTable.convertRowIndexToModel(sel);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelRow, 0);
        Musique music = findMusicById(playlist, musicId);
        if (music == null) {
            root.showError(root.getView(), "Musique introuvable.", "Erreur");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                root.getView(),
                "Supprimer la musique « " + music.getTitre() + " » ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;
        musicDAO.delete(String.valueOf(music.getId()));
        playlist.getMusiques().removeIf(m -> Objects.equals(m.getId(), music.getId()));
        root.getPlaylistDAO().update(playlist);
        ((DefaultTableModel) musicTable.getModel()).removeRow(modelRow);
    }

    //************************************************//
    //************** DOUBLE CLIC ARTISTE **************//
    //************************************************//
    private void onOpenArtisteEditorForSelectedMusic() {
        JTable playlistTable = root.getView().getTablePlaylist();
        JTable musicTable = root.getView().getTableMusicInPlaylistSelected();
        if (playlistTable == null || musicTable == null) return;
        Playlist playlist = getSelectedPlaylist(playlistTable);
        if (playlist == null) {
            root.showInfo(root.getView(), "Sélectionne d'abord une playlist.", "Artiste");
            return;
        }
        int sel = musicTable.getSelectedRow();
        if (sel < 0) {
            root.showInfo(root.getView(), "Double-clique une musique.", "Artiste");
            return;
        }
        int modelRow = musicTable.convertRowIndexToModel(sel);
        Integer musicId = (Integer) musicTable.getModel().getValueAt(modelRow, 0);
        Musique music = findMusicById(playlist, musicId);
        if (music == null) {
            root.showError(root.getView(), "Musique introuvable.", "Erreur");
            return;
        }
        Artiste artist = firstArtistOf(music);
        if (artist == null) {
            root.showInfo(root.getView(), "Aucun artiste associé.", "Artiste");
            return;
        }
        List<Album> albums = collectAlbumsOfArtist(artist);
        String portraitUrl = (music.getCoverImageURL() != null && !music.getCoverImageURL().isBlank())
                ? music.getCoverImageURL()
                : (playlist.getCoverImageURL() != null ? playlist.getCoverImageURL() : null);
        Image portrait = ArtisteEditorController.tryLoadImage(portraitUrl);
        SwingUtilities.invokeLater(() -> {
            ArtisteEditor dlg = new ArtisteEditor(root.getView());
            ArtisteEditorController ctrl = new ArtisteEditorController(dlg, root.getUsername());
            ctrl.initController();
            ctrl.loadArtisteData(artist, albums, portrait);
            ctrl.showDialog(root.getView());
        });
    }

    //************************************************//
    //************** HELPERS **************************//
    //************************************************//
    private Playlist getSelectedPlaylist(JTable table) {
        int sel = table.getSelectedRow();
        if (sel < 0) return null;
        int mr = table.convertRowIndexToModel(sel);
        Integer id = (Integer) table.getModel().getValueAt(mr, 0);
        for (Playlist p : root.getPlaylists()) {
            if (Objects.equals(p.getId(), id)) return p;
        }
        return null;
    }
    private Musique findMusicById(Playlist p, Integer id) {
        if (p == null || id == null) return null;
        for (Musique m : p.getMusiques()) {
            if (Objects.equals(m.getId(), id)) return m;
        }
        return null;
    }
    private Artiste firstArtistOf(Musique m) {
        if (m.getArtistes() == null || m.getArtistes().isEmpty()) return null;
        return m.getArtistes().get(0);
    }
    private List<Album> collectAlbumsOfArtist(Artiste artist) {
        List<Album> albums = new ArrayList<>();
        for (Playlist p : root.getPlaylists()) {
            for (Musique mu : p.getMusiques()) {
                if (mu.getArtistes() == null) continue;
                for (Artiste a : mu.getArtistes()) {
                    if (sameArtist(a, artist) && mu.getAlbum() != null) {
                        albums.add(mu.getAlbum());
                    }
                }
            }
        }
        return albums;
    }
    private boolean sameArtist(Artiste a, Artiste b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        String ak = safeTrim(a.getPseudo()).toLowerCase();
        String bk = safeTrim(b.getPseudo()).toLowerCase();
        return ak.equals(bk);
    }
    private String joinArtists(List<Artiste> artistes) {
        if (artistes == null || artistes.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        for (Artiste a : artistes) {
            String pseudo = safeTrim(a.getPseudo());
            if (pseudo.isBlank()) pseudo = "Inconnu";
            names.add(pseudo);
        }
        return String.join(", ", names);
    }
    private String formatDuration(float minutes) {
        int totalSeconds = Math.max(0, Math.round(minutes * 60));
        int mm = totalSeconds / 60;
        int ss = totalSeconds % 60;
        return String.format("%d:%02d", mm, ss);
    }
    private static float mmSsToFloatMinutes(String mmss) {
        String[] parts = mmss.split(":");
        int m = Integer.parseInt(parts[0]);
        int s = Integer.parseInt(parts[1]);
        return m + (s / 60f);
    }
    private String safeTrim(String s) { return (s == null) ? "" : s.trim(); }
    //************************************************//
    //************** RENDERER *************************//
    //************************************************//
    static class MusicCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel cover = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();
        private final Image defaultImg;
        MusicCellRenderer(JTable table) {
            setLayout(new BorderLayout(12, 0));
            setOpaque(true);
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            textPanel.setOpaque(false);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
            subtitle.setForeground(new Color(110, 110, 110));
            textPanel.add(title);
            textPanel.add(subtitle);
            add(cover, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
            setBorder(new EmptyBorder(8, 12, 8, 12));
            Image d = null;
            try {
                URL u = MainPageController.class.getResource("/PP.png");
                if (u != null) d = new ImageIcon(u).getImage();
            } catch (Exception ignored) {}
            defaultImg = d;
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String raw = (value == null) ? "" : value.toString();
            String[] parts = raw.split("\n", 2);
            String t = (parts.length > 0) ? parts[0] : "";
            String st = (parts.length > 1) ? parts[1] : "";
            String url = null;
            try {
                int modelRow = table.convertRowIndexToModel(row);
                url = String.valueOf(table.getModel().getValueAt(modelRow, 1));
            } catch (Exception ignored) {}
            cover.setIcon(loadScaledIcon(url, 56, 56, defaultImg));
            title.setText(t);
            subtitle.setText(st);
            setBackground(isSelected ? new Color(232, 239, 255) : Color.WHITE);
            return this;
        }
    }
    //************************************************//
    //************** UTILS IMAGES *********************//
    //************************************************//
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
