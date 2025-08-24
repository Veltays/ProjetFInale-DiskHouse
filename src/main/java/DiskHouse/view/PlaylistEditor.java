package DiskHouse.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Vue (JDialog) d'édition de playlist.
 * - Image cliquable (pour changer la cover)
 * - Champ nom
 * - Tableau des musiques avec boutons + / supprimer
 *
 * La logique (ajout/suppression musiques, choix image, sauvegarde, etc.)
 * est gérée dans PlaylistEditorController.
 */
public class PlaylistEditor extends JDialog {

    // --- UI ---
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
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        content = new JPanel(new BorderLayout(16, 16));
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(content);

        // Haut: cover + nom + actions musiques
        JPanel top = new JPanel(new BorderLayout(16, 0));
        content.add(top, BorderLayout.NORTH);

        // Cover
        coverLabel = new JLabel();
        coverLabel.setOpaque(true);
        coverLabel.setBackground(new Color(245, 247, 252));
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setPreferredSize(new Dimension(160, 160));
        coverLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
        coverLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        coverLabel.setToolTipText("Cliquer pour choisir une image…");
        top.add(coverLabel, BorderLayout.WEST);

        // Centre: nom
        JPanel namePanel = new JPanel(new BorderLayout(8, 8));
        JLabel nameLbl = new JLabel("Nom de la playlist");
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 18f));
        nameField = new JTextField();
        nameField.setFont(nameField.getFont().deriveFont(18f));
        namePanel.add(nameLbl, BorderLayout.NORTH);
        namePanel.add(nameField, BorderLayout.CENTER);
        top.add(namePanel, BorderLayout.CENTER);

        // Droite : boutons musiques
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        addMusicBtn = new JButton("+ Musique");
        delMusicBtn = new JButton("Supprimer musique");
        addMusicBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        delMusicBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(addMusicBtn);
        right.add(Box.createVerticalStrut(8));
        right.add(delMusicBtn);
        top.add(right, BorderLayout.EAST);

        // Centre : tableau musiques
        String[] cols = {"ID", "Titre", "Artistes", "Album", "Durée (mm:ss)", "CoverURL"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        musicTable = new JTable(model);
        musicTable.setRowHeight(28);
        musicTable.getColumnModel().getColumn(0).setMinWidth(0);
        musicTable.getColumnModel().getColumn(0).setMaxWidth(0);
        musicTable.getColumnModel().getColumn(5).setMinWidth(0);
        musicTable.getColumnModel().getColumn(5).setMaxWidth(0);
        content.add(new JScrollPane(musicTable), BorderLayout.CENTER);

        // Bas : boutons enregistrer/annuler
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Enregistrer");
        cancelBtn = new JButton("Annuler");
        bottom.add(cancelBtn);
        bottom.add(saveBtn);
        content.add(bottom, BorderLayout.SOUTH);

        // focus pratique
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if (nameField.getText().isBlank()) nameField.requestFocusInWindow();
            }
        });

        // petit effet visuel sur cover
        coverLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { coverLabel.setBackground(new Color(235, 239, 252)); }
            @Override public void mouseExited(MouseEvent e)  { coverLabel.setBackground(new Color(245, 247, 252)); }
        });
    }

    /* === Getters pour le contrôleur === */

    public JLabel getCoverLabel() { return coverLabel; }
    public JTextField getNameField() { return nameField; }
    public JTable getMusicTable() { return musicTable; }
    public JButton getAddMusicBtn() { return addMusicBtn; }
    public JButton getDelMusicBtn() { return delMusicBtn; }
    public JButton getSaveBtn() { return saveBtn; }
    public JButton getCancelBtn() { return cancelBtn; }
}