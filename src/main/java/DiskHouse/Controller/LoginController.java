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
    private final Login view;
    private final Authenticator authenticator;

    public LoginController(Login view) {
        this.view = Objects.requireNonNull(view);
        this.authenticator = new PropertiesAuthenticator();
    }

    @Override
    public Login getView() { return view; }

    @Override
    public void initController() {
        view.getLoginButton().addActionListener(e -> onLogin());
        view.getNotRegisterButton().addActionListener(e -> onOpenRegister());

        addPlaceholder(view.getTextfieldusername(), "Nom d'utilisateur");
        addPlaceholder(view.getTextfieldpassword(), "Mot de passe");
    }

    /* ================= Actions ================= */

    private void onLogin() {
        String user = view.getTextfieldusername().getText().trim();
        String pass = new String(view.getTextfieldpassword().getPassword());

        if (authenticator.authenticate(user, pass)) {
            showInfo(view, "Connexion réussie !", "Succès");
            onOpenMainPage();
        } else {
            showError(view, "Utilisateur/Mot de passe incorrect !", "Erreur");
        }
    }

    private void onOpenRegister() {
        new DiskHouse.view.Register();
        view.dispose();
    }

    private void onOpenMainPage() {
        MainPage mainPage = new MainPage();
        MainPageController controller = new MainPageController(mainPage);
        controller.initController();
        mainPage.setVisible(true);
        view.dispose();
    }

    /* ================= Helpers ================= */

    private void addPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }
}
