package DiskHouse.Controller;

import DiskHouse.view.Login;
import DiskHouse.view.Register;
import DiskHouse.view.MainPage;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginController {
    private final Login view;

    public LoginController(Login view) {
        this.view = view;
    }





    // ?
    // * Initialise le contrôleur en ajoutant des écouteurs d'événements aux composants de la vue.
    // * Elle récupére les composants de la vue et ajoute des écouteurs d'événements pour gérer les actions de l'utilisateur.
    // ?
    public void initController() {
        // Bouton Login
        view.getLoginButton().addActionListener(e -> handleLogin());

        // Bouton "Pas de compte ?"
        view.getNotRegisterButton().addActionListener(e -> openRegister());

        JTextField username = view.getTextfieldusername();
        addPlaceholder(username, "Nom d'utilisateur");

        // Placeholder pour Password
        JPasswordField password = view.getTextfieldpassword();
        addPlaceholder(password, "Mot de passe");

    }


    // ?
    // * Gère la connexion de l'utilisateur en vérifiant les informations d'identification.
    // *
    // ?


    private void handleLogin() {
        String user = view.getTextfieldusername().getText();
        String pass = new String(view.getTextfieldpassword().getPassword());

        // Ici tu pourrais vérifier dans ta DB
        if ("admin".equals(user) && "1234".equals(pass)) {
            JOptionPane.showMessageDialog(view, "Connexion réussie !");
            openMainPage(); // Ouvre la fenêtre principale
        } else {
            JOptionPane.showMessageDialog(view, "Utilisateur/Mot de passe incorrect !");
        }
    }


    // ?
    // * Ouvre la fenêtre d'inscription.
    // * Cette méthode est appelée lorsque l'utilisateur clique sur le bouton "Pas de compte ?
    // ?

    private void openRegister() {
        new Register(); // Ouvre la fenêtre d’inscription
        view.dispose(); // Ferme la fenêtre de login
    }


    private void openMainPage() {
        new MainPage();
        view.dispose(); // Ferme la fenêtre de login
    }





    // ?
    // * Ajoute un placeholder à un champ de texte.
    // * Le placeholder est un texte d'indication qui disparaît lorsque l'utilisateur clique sur le champ.
    // ?
    private void addPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }
}