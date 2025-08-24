package DiskHouse.view;

import javax.swing.*;
import DiskHouse.Controller.LoginController;

public class Login extends JFrame {

    // --- Composants (liés via l'UI Designer / GridLayoutManager) ---
    private JPanel fond;
    private JLabel Logo;
    private JLabel LoginTexte;
    private JTextField TextField_Username;
    private JPasswordField TextField_Password;
    private JButton Button_NotRegister;
    private JButton Button_Login;

    public Login() {
        setTitle("DiskHouse - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(fond);   // panel généré par l'UI Designer
        pack();                 // respecte le layout .form
        setLocationRelativeTo(null);

        wireController();       // branche le contrôleur
        setVisible(true);

        // évite le focus auto sur un champ texte au lancement
        SwingUtilities.invokeLater(() -> fond.requestFocusInWindow());
    }

    private void wireController() {
        // Version simple : factory par défaut (MapAuthenticator en mémoire)
        new LoginController(this).initController();

        // Si tu veux injecter explicitement une autre implémentation :
        // Authenticator auth = Authenticators.inMemoryDefault();
        // new LoginController(this, auth).initController();
    }

    // --- Getters pour le contrôleur ---
    public JButton getLoginButton()          { return Button_Login; }
    public JButton getNotRegisterButton()    { return Button_NotRegister; }
    public JTextField getTextfieldusername() { return TextField_Username; }
    public JPasswordField getTextfieldpassword() { return TextField_Password; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}