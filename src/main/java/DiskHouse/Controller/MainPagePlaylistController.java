//************************************************//
//************** MAIN PAGE PLAYLIST CONTROLLER ****//
//************************************************//
// Contrôleur pour la gestion des playlists sur la page principale.
// Gère l'initialisation de la table, les actions CRUD, le rendu personnalisé et la synchronisation avec le modèle.

package DiskHouse.Controller;

import DiskHouse.model.DAO.PlaylistFileDAO;
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

public class MainPagePlaylistController {

    //************************************************//
    //************** ATTRIBUTS ************************//
    //************************************************//
    private final MainPageController root;
    private Consumer<Playlist> onSelectionChanged;

    //************************************************//
    //************** CONSTRUCTEUR *********************//
    //************************************************//
    public MainPagePlaylistController(MainPageController root) {
        this.root = root;
    }

    //************************************************//
    //************** INITIALISATION TABLE *************//
    //************************************************//
    public void initPlaylistTable(JTable table) {
        String[] columns = {"ID", "Nom", "# Titres", "Image (URL)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2) return Integer.class;
                return String.class;
            }
        };
        table.setModel(model);

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(96);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setPreferredSize(new Dimension(0, 0));
            header.setVisible(false);
        }

        TableColumnModel cm = table.getColumnModel();
        if (cm.getColumnCount() >= 4) {
            hideColumn(cm.getColumn(0));
            hideColumn(cm.getColumn(3));
        }

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

        // Sélection → callback orchestrateur
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Playlist selected = getSelectedPlaylist(table);
            if (onSelectionChanged != null) onSelectionChanged.accept(selected);
        });
    }

    public void loadPlaylistsInto(JTable table, List<Playlist> playlists, Consumer<Playlist> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Playlist p : playlists) {
            int count = (p.getMusiques() != null) ? p.getMusiques().size() : 0;
            model.addRow(new Object[]{p.getId(), p.getNomPlaylist(), count, p.getCoverImageURL()});
        }

        // Sélectionner première ligne si dispo
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

        for (Playlist p : root.getPlaylists()) {
            if (Objects.equals(p.getId(), id)) return p;
        }
        return null;
    }

    private void hideColumn(TableColumn column) {
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
        column.setResizable(false);
    }

    //************************************************//
    //************** ACTIONS CRUD *********************//
    //************************************************//
    public void wireButtons() {
        if (root.getView().getAjouterPlaylistButton() != null) {
            root.getView().getAjouterPlaylistButton().addActionListener(e -> onAddPlaylist());
        }
        if (root.getView().getModifierPlaylistButton() != null) {
            root.getView().getModifierPlaylistButton().addActionListener(e -> onEditPlaylist());
        }
        if (root.getView().getSupprimerPlaylistButton() != null) {
            root.getView().getSupprimerPlaylistButton().addActionListener(e -> onDeletePlaylist());
        }
    }

    private void onAddPlaylist() {
        JTable playlistTable = root.getView().getTablePlaylist();
        if (playlistTable == null) return;

        Window owner = root.getView().isVisible() ? root.getView() : null;
        PlaylistEditor editorView = new PlaylistEditor(owner);

        PlaylistEditorController ctrl = new PlaylistEditorController(
                editorView,
                new PlaylistEditorController.Listener() {
                    @Override public void onPlaylistCreated(Playlist created) {
                        // Persist
                        root.getPlaylistDAO().add(created);

                        // Mémoire + UI
                        root.getPlaylists().add(created);
                        loadPlaylistsInto(playlistTable, root.getPlaylists(), onSelectionChanged);

                        int last = playlistTable.getRowCount() - 1;
                        if (last >= 0) playlistTable.setRowSelectionInterval(last, last);
                    }
                    @Override public void onPlaylistUpdated(Playlist updated) { /* not used in create */ }
                }
        );
        ctrl.openForCreate(owner);
    }

    private void onEditPlaylist() {
        JTable playlistTable = root.getView().getTablePlaylist();
        if (playlistTable == null) return;

        Playlist selected = getSelectedPlaylist(playlistTable);
        if (selected == null) {
            root.showInfo(root.getView(), "Sélectionne une playlist à modifier.", "Modifier playlist");
            return;
        }

        Window owner = root.getView().isVisible() ? root.getView() : null;
        PlaylistEditor editorView = new PlaylistEditor(owner);

        PlaylistEditorController ctrl = new PlaylistEditorController(
                editorView,
                new PlaylistEditorController.Listener() {
                    @Override public void onPlaylistCreated(Playlist created) { /* not used in edit */ }

                    @Override public void onPlaylistUpdated(Playlist updated) {
                        // Persist
                        root.getPlaylistDAO().update(updated);

                        // UI refresh
                        loadPlaylistsInto(playlistTable, root.getPlaylists(), onSelectionChanged);

                        // Reselect updated row
                        for (int r = 0; r < playlistTable.getModel().getRowCount(); r++) {
                            Integer rid = (Integer) playlistTable.getModel().getValueAt(r, 0);
                            if (Objects.equals(rid, updated.getId())) {
                                int vr = playlistTable.convertRowIndexToView(r);
                                playlistTable.setRowSelectionInterval(vr, vr);
                                break;
                            }
                        }
                    }
                }
        );
        ctrl.openForEdit(owner, selected);
    }

    private void onDeletePlaylist() {
        JTable playlistTable = root.getView().getTablePlaylist();
        if (playlistTable == null) return;

        Playlist selected = getSelectedPlaylist(playlistTable);
        if (selected == null) {
            root.showInfo(root.getView(), "Sélectionne une playlist à supprimer.", "Supprimer playlist");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                root.getView(),
                "Supprimer la playlist \"" + selected.getNomPlaylist() + "\" ?",
                "Confirmation",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.OK_OPTION) return;

        // Persist
        root.getPlaylistDAO().delete(String.valueOf(selected.getId()));

        // Mémoire + UI
        root.getPlaylists().remove(selected);
        loadPlaylistsInto(playlistTable, root.getPlaylists(), onSelectionChanged);

        if (onSelectionChanged != null) onSelectionChanged.accept(getSelectedPlaylist(playlistTable));
    }

    //************************************************//
    //************** RENDERERS ************************//
    //************************************************//
    static class PlaylistCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel cover = new JLabel();
        private final JLabel title = new JLabel();
        private final Image defaultImg;

        PlaylistCellRenderer(JTable table) {
            setLayout(new BorderLayout(12, 0));
            setOpaque(true);

            cover.setOpaque(false);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

            add(cover, BorderLayout.WEST);
            add(title, BorderLayout.CENTER);
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
            String name = (value == null) ? "" : value.toString();
            String url = null;
            try {
                int modelRow = table.convertRowIndexToModel(row);
                url = String.valueOf(table.getModel().getValueAt(modelRow, 3));
            } catch (Exception ignored) {}

            cover.setIcon(loadScaledIcon(url, 72, 72, defaultImg));
            title.setText(name);
            setBackground(isSelected ? new Color(232, 239, 255) : Color.WHITE);
            return this;
        }
    }

    static class RightHintRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        RightHintRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(new EmptyBorder(0, 0, 0, 16));
            setForeground(new Color(90, 90, 90));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "" : value.toString());
            setBackground(isSelected ? new Color(232, 239, 255) : Color.WHITE);
            return this;
        }
    }

    //************************************************//
    //************** HELPERS / UTILS ******************//
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
