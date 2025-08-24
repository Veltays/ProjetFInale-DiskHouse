//************************************************//
//************** LOGIN CONTROLLER ****************//
//************************************************//
// Contrôleur pour la gestion de la connexion utilisateur.
// Gère la logique d'UI, la validation, l'authentification et la navigation.

package DiskHouse.Controller;

import DiskHouse.model.authentification.Authenticator;
import DiskHouse.model.service.PropertiesAuthenticator;
import DiskHouse.view.Login;
import DiskHouse.view.MainPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;

public class LoginController implements IController<Login> {

    //************************************************//
    //************** ATTRIBUTS ************************//
    //************************************************//
    private final Login view;
    private final Authenticator authenticator;

    //************************************************//
    //************** CONSTRUCTEUR *********************//
    //************************************************//
    public LoginController(Login view) {
        this.view = Objects.requireNonNull(view);
        this.authenticator = new PropertiesAuthenticator();
    }

    //************************************************//
    //************** GETTEUR VUE **********************//
    //************************************************//
    @Override
    public Login getView() { return view; }

    //************************************************//
    //************** INITIALISATION *******************//
    //************************************************//
    @Override
    public void initController() {
        view.getLoginButton().addActionListener(e -> onLogin());
        view.getNotRegisterButton().addActionListener(e -> onOpenRegister());
        addPlaceholder(view.getTextfieldusername(), "Nom d'utilisateur");
        addPlaceholder(view.getTextfieldpassword(), "Mot de passe");
    }

    //************************************************//
    //************** ACTIONS **************************//
    //************************************************//
    private void onLogin() {
        String username = view.getTextfieldusername().getText().trim();
        String password = new String(view.getTextfieldpassword().getPassword());
        if (authenticator.authenticate(username, password)) {
            showInfo(view, "Connexion réussie !", "Succès");
            openMainPage(username);
        } else {
            showError(view, "Utilisateur ou mot de passe incorrect.", "Erreur");
        }
    }

    private void onOpenRegister() {
        new DiskHouse.view.Register();
        view.dispose();
    }

    private void openMainPage(String username) {
        MainPage mainPage = new MainPage();
        MainPageController controller = new MainPageController(mainPage, username);
        controller.initController();
        mainPage.setVisible(true);
        view.dispose();
    }

    //************************************************//
    //************** HELPERS **************************//
    //************************************************//
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

    @Override
    public void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
