package DiskHouse.view.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class Register extends JFrame {

    private JPanel Fond;
    private JPanel Logo;
    private JPanel TexteRegister;
    private JTextField TextFieldUsername;
    private JPanel ChampUsername;
    private JTextField passwordField;
    private JTextField passwordFieldConfirmPassword;
    private JPanel ChampPassword;
    private JPanel ChampConfirmPassword;
    private JLabel LogoLabel;
    private JLabel LabelTexteRegister;

    public Register() {
        setTitle("Registre");
        setSize(500, 450);
        setContentPane(Fond); //  on définit bien Fond comme conteneur
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setVisible(true);



    }

    public static void main(String[] args) {
        new Register(); // au lieu de MainPage
    }
}
