package DiskHouse.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Vue (JDialog) d'édition de playlist (GridLayoutManager only).
 * - Cover cliquable (changée par le contrôleur)
 * - Gros titre (champ nom)
 * - Boutons + / 🗑 pour gérer les musiques
 * - JTable centrée (ID & CoverURL cachés)
 *
 * La logique est gérée par PlaylistEditorController (MVC).
 */
public class PlaylistEditor extends JDialog {

    // === UI elements (accessibles par le contrôleur) ===
    private JPanel content;
    private JLabel coverLabel;
    private JTextField nameField;
    private JTable musicTable;
    private JButton addMusicBtn;
    private JButton delMusicBtn;
    private JButton saveBtn;
    private JButton cancelBtn;

    public PlaylistEditor(Window owner) {
        super(owner, "Playlist", ModalityType.APPLICATION_MODAL);
        buildUI();
        setContentPane(content);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        content = new JPanel();
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        content.setLayout(new GridLayoutManager(
                4,  // 1: header (cover + titre + actions) | 2: spacer | 3: table | 4: footer
                1,
                new Insets(0, 0, 0, 0),
                -1, -1
        ));

        // ===== Header (ligne 0) : Grid 1x1 -> panel composite =====
        JPanel header = new JPanel();
        header.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 8, 0), 12, 8));
        content.add(header, new GridConstraints(
                0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null
        ));

        // -- Cover à gauche (row 0..1, col 0)
        coverLabel = new JLabel();
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setOpaque(true);
        coverLabel.setBackground(new Color(245, 247, 252));
        coverLabel.setPreferredSize(new Dimension(160, 160));
        coverLabel.setMinimumSize(new Dimension(160, 160));
        coverLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        coverLabel.setToolTipText("Cliquer pour choisir une image…");
        header.add(coverLabel, new GridConstraints(
                0, 0, 2, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(160, 160), null
        ));

        // -- Gros titre (row 0, col 1)
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), 0, 6));
        header.add(titlePanel, new GridConstraints(
                0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));

        JLabel titleLabel = new JLabel("Nom de la playlist");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        titlePanel.add(titleLabel, new GridConstraints(
                0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));

        nameField = new JTextField();
        nameField.setFont(nameField.getFont().deriveFont(32f)); // aspect "La goat"
        nameField.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        titlePanel.add(nameField, new GridConstraints(
                1, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));

        // -- Boutons actions (row 0, col 2)
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), 0, 8));
        header.add(actionPanel, new GridConstraints(
                0, 2, 1, 1,
                GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));

        addMusicBtn = new JButton("+ Musique");
        delMusicBtn = new JButton("Supprimer musique");
        actionPanel.add(addMusicBtn, new GridConstraints(
                0, 0, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));
        actionPanel.add(delMusicBtn, new GridConstraints(
                1, 0, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));

        // -- (row 1, col 1..2) petit espace/placeholder si besoin
        header.add(Box.createGlue(), new GridConstraints(
                1, 1, 1, 2,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null
        ));

        // ===== Spacer (ligne 1)
        content.add(Box.createVerticalStrut(8), new GridConstraints(
                1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));

        // ===== Tableau musiques (ligne 2)
        String[] cols = {"ID", "Titre", "Artistes", "Album", "Durée (mm:ss)", "CoverURL"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return String.class; }
        };
        musicTable = new JTable(model);
        musicTable.setRowHeight(28);
        musicTable.setFillsViewportHeight(true);
        JScrollPane sp = new JScrollPane(musicTable);
        content.add(sp, new GridConstraints(
                2, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null
        ));

        // ===== Footer (ligne 3) : Annuler / Enregistrer
        JPanel footer = new JPanel();
        footer.setLayout(new GridLayoutManager(1, 3, new Insets(8, 0, 0, 0), 8, 0));
        content.add(footer, new GridConstraints(
                3, 0, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));

        footer.add(Box.createHorizontalStrut(0), new GridConstraints(
                0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));

        cancelBtn = new JButton("Annuler");
        saveBtn = new JButton("Enregistrer");
        footer.add(cancelBtn, new GridConstraints(
                0, 1, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));
        footer.add(saveBtn, new GridConstraints(
                0, 2, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null
        ));

        // cacher colonnes techniques (ID, CoverURL) après création du modèle
        SwingUtilities.invokeLater(() -> {
            if (musicTable.getColumnModel().getColumnCount() >= 6) {
                musicTable.getColumnModel().getColumn(0).setMinWidth(0);
                musicTable.getColumnModel().getColumn(0).setMaxWidth(0);
                musicTable.getColumnModel().getColumn(5).setMinWidth(0);
                musicTable.getColumnModel().getColumn(5).setMaxWidth(0);
            }
        });
    }

    // === Getters pour le contrôleur ===
    public JLabel getCoverLabel() { return coverLabel; }
    public JTextField getNameField() { return nameField; }
    public JTable getMusicTable() { return musicTable; }
    public JButton getAddMusicBtn() { return addMusicBtn; }
    public JButton getDelMusicBtn() { return delMusicBtn; }
    public JButton getSaveBtn() { return saveBtn; }
    public JButton getCancelBtn() { return cancelBtn; }
}
