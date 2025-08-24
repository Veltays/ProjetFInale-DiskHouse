package DiskHouse.Controller;

import DiskHouse.model.entity.Playlist;
import DiskHouse.view.PlaylistEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Gère la JTable des playlists : modèle, renderers, sélection, CRUD.
 */
public class MainPagePlaylistController {

    private final MainPageController root; // accès vue + playlists + dialogs
    private Consumer<Playlist> onSelectionChanged;

    public MainPagePlaylistController(MainPageController root) {
        this.root = root;
    }

    /* =================== Table Playlists : modèle + rendu =================== */

    public void initPlaylistTable(JTable table) {
        String[] columns = {"ID", "Nom", "# Titres", "Image (URL)"};
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

        // Sélection -> callback vers l'orchestrateur (mise à jour table musiques)
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Playlist selected = getSelectedPlaylist(table);
            if (onSelectionChanged != null) onSelectionChanged.accept(selected);
        });
    }

    public void loadPlaylistsInto(JTable table, List<Playlist> data, Consumer<Playlist> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Playlist p : data) {
            model.addRow(new Object[]{
                    p.getId(), p.getNomPlaylist(),
                    p.getMusiques() != null ? p.getMusiques().size() : 0,
                    p.getCoverImageURL()
            });
        }

        // Sélectionne la première et déclenche le callback
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            if (onSelectionChanged != null) onSelectionChanged.accept(getSelectedPlaylist(table));
        } else {
            if (onSelectionChanged != null) onSelectionChanged.accept(null);
        }
    }

    private Playlist getSelectedPlaylist(JTable table) {
        int vr = table.getSelectedRow();
        if (vr < 0) return null;
        int mr = table.convertRowIndexToModel(vr);
        Integer id = (Integer) table.getModel().getValueAt(mr, 0);
        for (Playlist p : root.playlists) if (Objects.equals(p.getId(), id)) return p;
        return null;
    }

    private void hideColumn(TableColumn column) {
        column.setMinWidth(0); column.setMaxWidth(0); column.setPreferredWidth(0); column.setResizable(false);
    }

    /* =================== Boutons Playlists (CRUD) =================== */

    public void wireButtons() {
        if (root.getV().getAjouterPlaylistButton() != null)
            root.getV().getAjouterPlaylistButton().addActionListener(e -> onAddPlaylist());
        if (root.getV().getModifierPlaylistButton() != null)
            root.getV().getModifierPlaylistButton().addActionListener(e -> onEditPlaylist());
        if (root.getV().getSupprimerPlaylistButton() != null)
            root.getV().getSupprimerPlaylistButton().addActionListener(e -> onDeletePlaylist());
    }

    private void onAddPlaylist() {
        JTable playlistTable = root.getV().getTablePlaylist();
        if (playlistTable == null) return;

        Window owner = root.getV().isVisible() ? root.getV() : null;
        PlaylistEditor editorView = new PlaylistEditor(owner);
        PlaylistEditorController ctrl = new PlaylistEditorController(editorView, new PlaylistEditorController.Listener() {
            @Override public void onPlaylistCreated(Playlist created) {
                root.playlists.add(created);
                loadPlaylistsInto(playlistTable, root.playlists, onSelectionChanged);
                int last = playlistTable.getRowCount() - 1;
                if (last >= 0) playlistTable.setRowSelectionInterval(last, last);
            }
            @Override public void onPlaylistUpdated(Playlist updated) { /* non utilisé en Add */ }
        });
        ctrl.openForCreate(owner);
    }

    private void onEditPlaylist() {
        JTable playlistTable = root.getV().getTablePlaylist();
        if (playlistTable == null) return;

        Playlist selected = getSelectedPlaylist(playlistTable);
        if (selected == null) { root.showInfo(root.getV(), "Sélectionne une playlist à modifier.", "Modifier playlist"); return; }

        Window owner = root.getV().isVisible() ? root.getV() : null;
        PlaylistEditor editorView = new PlaylistEditor(owner);
        PlaylistEditorController ctrl = new PlaylistEditorController(editorView, new PlaylistEditorController.Listener() {
            @Override public void onPlaylistCreated(Playlist created) { /* non utilisé ici */ }

            @Override public void onPlaylistUpdated(Playlist updated) {
                // Recharger la table et reselectionner
                loadPlaylistsInto(playlistTable, root.playlists, onSelectionChanged);
                for (int r = 0; r < playlistTable.getModel().getRowCount(); r++) {
                    Integer rid = (Integer) playlistTable.getModel().getValueAt(r, 0);
                    if (Objects.equals(rid, updated.getId())) {
                        int vr = playlistTable.convertRowIndexToView(r);
                        playlistTable.setRowSelectionInterval(vr, vr);
                        break;
                    }
                }
            }
        });
        ctrl.openForEdit(owner, selected);
    }

    private void onDeletePlaylist() {
        JTable playlistTable = root.getV().getTablePlaylist();
        if (playlistTable == null) return;

        Playlist selected = getSelectedPlaylist(playlistTable);
        if (selected == null) { root.showInfo(root.getV(), "Sélectionne une playlist à supprimer.", "Supprimer playlist"); return; }

        int ok = JOptionPane.showConfirmDialog(root.getV(),
                "Supprimer la playlist \"" + selected.getNomPlaylist() + "\" ?",
                "Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        root.playlists.remove(selected);
        loadPlaylistsInto(playlistTable, root.playlists, onSelectionChanged);

        // Effacer la table musiques via callback
        if (onSelectionChanged != null) onSelectionChanged.accept(getSelectedPlaylist(playlistTable));
    }

    /* ===================== Renderers (Playlists) ===================== */

    static class PlaylistCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel cover = new JLabel();
        private final JLabel title = new JLabel();
        private final Image defaultImg;

        PlaylistCellRenderer(JTable table) {
            setLayout(new BorderLayout(12, 0)); setOpaque(true);
            cover.setOpaque(false);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            add(cover, BorderLayout.WEST); add(title, BorderLayout.CENTER);
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

    static class RightHintRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
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
