package DiskHouse.Controller;

import DiskHouse.view.AddArtist;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * Contrôleur AddArtist
 * - Placeholders sur Pseudo/Nom/Prenom
 * - Gestion de la liste des albums (JList + DefaultListModel)
 * - Boutons + / 🗑
 * - Entrée = "ajouter album" (comme bouton par défaut)
 */
public class AddArtistController {

    private final AddArtist view;

    // Placeholders
    private static final String PH_PSEUDO = "Pseudo";
    private static final String PH_NOM = "Nom";
    private static final String PH_PRENOM = "Prenom";

    private DefaultListModel<String> model; // modèle de la JList Albums

    public AddArtistController(AddArtist view) {
        this.view = Objects.requireNonNull(view);
    }

    public void initController() {
        // Placeholders
        addPlaceholder(view.getPseudoField(), PH_PSEUDO);
        addPlaceholder(view.getNomField(), PH_NOM);
        addPlaceholder(view.getPrenomField(), PH_PRENOM);

        // Modèle de la liste Albums
        if (!(view.getAlbumList().getModel() instanceof DefaultListModel)) {
            model = new DefaultListModel<>();
            view.getAlbumList().setModel(model);
        } else {
            model = (DefaultListModel<String>) view.getAlbumList().getModel();
        }

        // Bouton Ajouter
        view.getAddAlbumButton().addActionListener(e -> addAlbum());

        // Bouton Supprimer
        view.getRemoveAlbumButton().addActionListener(e -> removeSelectedAlbum());

        // Entrée = ajouter album (si le focus est dans la fenêtre)
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "do-add");
        am.put("do-add", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                addAlbum();
            }
        });

        // Double-clic sur la liste = renommer l'album
        view.getAlbumList().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && view.getAlbumList().getSelectedIndex() >= 0) {
                    renameSelectedAlbum();
                }
            }
        });
    }

    /* ===================== Actions Albums ===================== */

    private void addAlbum() {
        String name = JOptionPane.showInputDialog(view, "Nom de l'album :", "Ajouter un album", JOptionPane.QUESTION_MESSAGE);
        if (name != null) {
            name = name.trim();
            if (!name.isEmpty()) {
                model.addElement(name);
                // scroll vers le bas
                int last = model.size() - 1;
                view.getAlbumList().setSelectedIndex(last);
                view.getAlbumList().ensureIndexIsVisible(last);
            } else {
                JOptionPane.showMessageDialog(view, "Le nom ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeSelectedAlbum() {
        int idx = view.getAlbumList().getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(view, "Sélectionne un album à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String name = model.getElementAt(idx);
        int confirm = JOptionPane.showConfirmDialog(view, "Supprimer \"" + name + "\" ?", "Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            model.remove(idx);
        }
    }

    private void renameSelectedAlbum() {
        int idx = view.getAlbumList().getSelectedIndex();
        if (idx < 0) return;
        String current = model.getElementAt(idx);
        String name = (String) JOptionPane.showInputDialog(
                view,
                "Nouveau nom :",
                "Renommer l'album",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                current
        );
        if (name != null) {
            name = name.trim();
            if (!name.isEmpty()) {
                model.set(idx, name);
            } else {
                JOptionPane.showMessageDialog(view, "Le nom ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /* ===================== Placeholders (JTextField) ===================== */

    private void addPlaceholder(JTextField field, String placeholder) {
        Color placeholderColor = new Color(0x9F9393);
        Color normalColor = new Color(0x333333);

        field.setText(placeholder);
        field.setForeground(placeholderColor);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getForeground().equals(placeholderColor)) {
                    field.setText("");
                    field.setForeground(normalColor);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(placeholderColor);
                }
            }
        });
    }

    /* ===================== Accès modèle pour la couche supérieure ===================== */

    public String getPseudo() {
        String v = view.getPseudoField().getText().trim();
        return v.equals(PH_PSEUDO) ? "" : v;
    }

    public String getNom() {
        String v = view.getNomField().getText().trim();
        return v.equals(PH_NOM) ? "" : v;
    }

    public String getPrenom() {
        String v = view.getPrenomField().getText().trim();
        return v.equals(PH_PRENOM) ? "" : v;
    }

    public DefaultListModel<String> getAlbumListModel() {
        return model;
    }

    /** Validation simplifiée */
    public boolean validateForm() {
        if (getPseudo().isEmpty() || getNom().isEmpty() || getPrenom().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Pseudo, Nom et Prenom sont requis.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
