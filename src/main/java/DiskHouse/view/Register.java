package DiskHouse.view;

import DiskHouse.Controller.RegisterController;

import javax.swing.*;

public class Register extends JFrame {

    // === Composants générés par l'UI Designer (ne pas renommer côté .form) ===
    private JPanel Fond;
    private JPanel Logo;
    private JPanel TexteRegister;

    private JTextField TextFieldUsername;
    private JPanel ChampUsername;

    private JPasswordField passwordField;
    private JPasswordField passwordFieldConfirmPassword;

    private JPanel ChampPassword;
    private JPanel ChampConfirmPassword;
    private JLabel LogoLabel;
    private JLabel LabelTexteRegister;
    private JButton sInscrireButton;
    private JButton vousAvezDejaUnButton;

    // === Wiring MVC dans le constructeur (pas dans main) ===
    public Register() {
        setTitle("Registre");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(Fond);    // respecte le GridLayoutManager défini dans .form
        pack();
        setLocationRelativeTo(null);

        // -> Bind du contrôleur ICI
        new RegisterController(this).initController();

        setVisible(true);
        // optionnel : éviter le focus auto sur un champ
        SwingUtilities.invokeLater(() -> Fond.requestFocusInWindow());
    }

    // === Getters pour le contrôleur ===
    public JButton getSInscrireButton() { return sInscrireButton; }
    public JButton getVousAvezDejaUnButton() { return vousAvezDejaUnButton; }
    public JTextField getTextFieldUsername() { return TextFieldUsername; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JPasswordField getPasswordFieldConfirmPassword() { return passwordFieldConfirmPassword; }
    public JPanel getRootPanel() { return Fond; }

    // Si tu veux tester en standalone : laisse vide, le wiring est dans le ctor
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Register::new);
    }
}
