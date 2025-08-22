package DiskHouse.Controller;

import DiskHouse.view.AddMusic;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * Contrôleur de la vue AddMusic
 * - Branche les placeholders
 * - Clique sur l’aperçu pour choisir une image (JFileChooser)
 * - Validation simple
 * - Touche Entrée = valider (comme "bouton par défaut")
 */
public class AddMusicController {

    private final AddMusic view;

    // Placeholders centralisés
    private static final String PH_TITRE   = "Titre";
    private static final String PH_ARTISTE = "Artiste";
    private static final String PH_ALBUM   = "Album";
    private static final String PH_DUREE   = "Durée (mm:ss)";

    private String selectedImagePath; // chemin choisi (si besoin côté modèle)

    public AddMusicController(AddMusic view) {
        this.view = Objects.requireNonNull(view);
    }

    /** À appeler après new AddMusic() */
    public void initController() {
        // Placeholders (visuels)
        addPlaceholder(view.getTitreField(),   PH_TITRE);
        addPlaceholder(view.getArtisteField(), PH_ARTISTE);
        addPlaceholder(view.getAlbumField(),   PH_ALBUM);
        addPlaceholder(view.getDureeField(),   PH_DUREE);

        // Clic sur l'aperçu -> choisit une image
        JLabel preview = view.getImagePreviewLabel();
        preview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        preview.setToolTipText("Cliquer pour choisir une image (png/jpg/jpeg)");
        preview.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { chooseImage(); }
        });

        // Entrée = valider (comme si un bouton par défaut existait)
        // On attache un Key Binding sur la root pane
        JRootPane root = view.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "do-validate");
        am.put("do-validate", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    JOptionPane.showMessageDialog(view, "Formulaire OK ✅");
                }
            }
        });
    }

    /* =========================
       Actions simples d'UI
       ========================= */
    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une image");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Images (png, jpg, jpeg)", "png", "jpg", "jpeg")
        );

        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            selectedImagePath = chooser.getSelectedFile().getAbsolutePath();
            ImageIcon icon = new ImageIcon(selectedImagePath);
            view.setPreviewImage(icon.getImage());   // la vue s'occupe d'afficher proprement
        }
    }

    // Placeholder générique pour JTextField (même logique que Login)
    private void addPlaceholder(JTextField field, String placeholder) {
        Color placeholderColor = new Color(0x9F9393);
        Color normalColor = new Color(0x333333);

        field.setText(placeholder);
        field.setForeground(placeholderColor);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getForeground().equals(placeholderColor)) {
                    field.setText("");
                    field.setForeground(normalColor);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(placeholderColor);
                }
            }
        });
    }

    /* =========================
       Getters utiles (modèle)
       ========================= */
    public String getSelectedImagePath() { return selectedImagePath; }

    public String getTitre() {
        String v = view.getTitreField().getText().trim();
        return v.equals(PH_TITRE) ? "" : v;
    }
    public String getArtiste() {
        String v = view.getArtisteField().getText().trim();
        return v.equals(PH_ARTISTE) ? "" : v;
    }
    public String getAlbum() {
        String v = view.getAlbumField().getText().trim();
        return v.equals(PH_ALBUM) ? "" : v;
    }
    public String getDuree() {
        String v = view.getDureeField().getText().trim();
        return v.equals(PH_DUREE) ? "" : v;
    }

    /** Validation très simple (BAC2) */
    public boolean validateForm() {
        if (getTitre().isEmpty() || getArtiste().isEmpty() || getAlbum().isEmpty() || getDuree().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Tous les champs sont requis.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!getDuree().matches("^\\d{1,2}:[0-5]\\d$")) {
            JOptionPane.showMessageDialog(view, "Durée invalide. Utilise le format mm:ss (ex: 03:25).", "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /** Convertit mm:ss en secondes (utile côté modèle) */
    public int getDurationInSeconds() {
        String d = getDuree();
        if (!d.matches("^\\d{1,2}:[0-5]\\d$")) return 0;
        String[] p = d.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }
}
