package DiskHouse.view.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class Login extends JFrame {

    private JPanel fond;
    private JLabel logo;
    private JLabel logintexte;
    private JTextField textfieldusername;
    private JPasswordField textfieldpassword;

    public Login() {
        setTitle("Login");
        setSize(500, 450);
        setContentPane(fond); //  on définit bien Fond comme conteneur
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setVisible(true);



    }

    public static void main(String[] args) {
        new Login(); // au lieu de MainPage
    }
}