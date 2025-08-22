package DiskHouse.view;

import javax.swing.*;

import DiskHouse.Controller.LoginController;

public class Login extends JFrame {


    // Champ UI
    private JPanel fond;
    private JLabel Logo;
    private JLabel LoginTexte;


    // champ TextField
    private JTextField TextField_Username;
    private JPasswordField TextField_Password;


    // Champ Button
    private JButton Button_NotRegister;
    private JButton Button_Login;


    // Champ Controller
    private final LoginController controller;



    public Login() {
        super("cccccc");

        // === Initialisation contrôleur ===
        controller = new LoginController(this);

        // === UI ===
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setSize(400, 250);
        setLocationRelativeTo(null);

        // === Création du JPanel en le placant dans le JFrame ===
        setContentPane(fond);                 // le JPanel généré par le designer
        pack();                               // respecte le layout de la Form
        setLocationRelativeTo(null);


        controller.initController();


        // showDialog(false); // si tu veux la montrer au démarrage sans bloquer
        setVisible(true);

        // pour évité le focus
        SwingUtilities.invokeLater(() -> fond.requestFocusInWindow());

    }



    // fonction qui permettent au contoleur d'accéder aux composants de la vue

    public JButton getLoginButton() { return Button_Login; }
    public JButton getNotRegisterButton() { return Button_NotRegister; }
    public JTextField getTextfieldusername() { return TextField_Username; }
    public JPasswordField getTextfieldpassword() { return TextField_Password; }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new); // lance sur l'EDT
    }
}













//    private void showDialog(boolean modal) {
//        JDialog dialog = new JDialog(this, "Message", modal);
//        dialog.setSize(250, 100);
//        dialog.setLocationRelativeTo(this);
//        dialog.setLayout(new FlowLayout());
//
//        JLabel message = new JLabel("Fqbzhridjohzef");
//        JButton closeButton = new JButton("Fermer");
//        closeButton.addActionListener(ev -> dialog.dispose());
//
//        dialog.add(message);
//        dialog.add(closeButton);
//        dialog.setVisible(true);
//    }
