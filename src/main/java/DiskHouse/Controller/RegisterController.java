package DiskHouse.Controller;

import DiskHouse.model.service.PropertiesAuthenticator;
import DiskHouse.view.Login;
import DiskHouse.view.Register;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;

public class RegisterController implements IController<Register> {

    private final Register view;
    private final PropertiesAuthenticator authenticator;

    /* ===================== CONSTRUCTEUR ===================== */

    public RegisterController(Register view) {
        this.view = Objects.requireNonNull(view);
        this.authenticator = new PropertiesAuthenticator();
    }

    /* ===================== INIT CONTROLLER ===================== */

    @Override
    public Register getView() {
        return view;
    }

    @Override
    public void initController() {
        view.getSInscrireButton().addActionListener(e -> onRegister());
        view.getVousAvezDejaUnButton().addActionListener(e -> onOpenLogin());

        addPlaceholder(view.getTextFieldUsername(), "Entrez votre nom d’utilisateur");
        addPlaceholder(view.getPasswordField(), "Mot de passe");
        addPlaceholder(view.getPasswordFieldConfirmPassword(), "Confirmer le mot de passe");
    }

    /* ===================== ACTIONS ===================== */

    private void onRegister() {
        String username = safeText(view.getTextFieldUsername(), "Entrez votre nom d’utilisateur");
        String password = safePassword(view.getPasswordField(), "Mot de passe");
        String confirm = safePassword(view.getPasswordFieldConfirmPassword(), "Confirmer le mot de passe");

        if (username.isBlank()) {
            showError(view, "Le nom d’utilisateur est vide.", "Erreur");
            return;
        }
        if (password.isEmpty() || confirm.isEmpty()) {
            showError(view, "Veuillez entrer les deux mots de passe.", "Erreur");
            return;
        }
        if (!password.equals(confirm)) {
            showError(view, "Les mots de passe ne correspondent pas.", "Erreur");
            return;
        }
        if (password.length() < 6) {
            showError(view, "Mot de passe trop court (min 6 caractères).", "Erreur");
            return;
        }

        boolean created = authenticator.registerUser(username, password);
        if (!created) {
            showInfo(view, "Ce nom d’utilisateur existe déjà.", "Information");
            return;
        }

        showInfo(view, "Compte créé avec succès !", "Succès");
        onOpenLogin();
    }

    private void onOpenLogin() {
        SwingUtilities.invokeLater(() -> {
            new Login();
            view.dispose();
        });
    }

    /* ===================== HELPERS ===================== */

    private void addPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                } else {
                    field.selectAll();
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isBlank()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void addPlaceholder(JPasswordField field, String placeholder) {
        final char normalEcho = field.getEchoChar();

        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.setEchoChar((char) 0);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String txt = new String(field.getPassword());
                if (txt.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar(normalEcho);
                } else {
                    field.selectAll();
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                String txt = new String(field.getPassword()).trim();
                if (txt.isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                    field.setEchoChar((char) 0);
                }
            }
        });
    }

    private String safeText(JTextField tf, String placeholder) {
        String val = tf.getText().trim();
        return val.equals(placeholder) ? "" : val;
    }

    private String safePassword(JPasswordField pf, String placeholder) {
        String val = new String(pf.getPassword());
        return val.equals(placeholder) ? "" : val;
    }
}
