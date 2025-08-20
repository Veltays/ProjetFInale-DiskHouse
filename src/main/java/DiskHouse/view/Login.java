package DiskHouse.view;

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


        showDialog();

        setVisible(true);


    }

    private void showDialog() {
        // Créer et afficher le JDialog
        JDialog dialog = new JDialog(Login.this, "Message", true);
        dialog.setSize(250, 100);
        dialog.setLocationRelativeTo(Login.this); // Centrer par rapport a fond
        dialog.setLayout(new FlowLayout());

        JLabel message = new JLabel("Fqbzhridjohzef");
        JButton closeButton = new JButton("ehhe ");

        // Action pour fermer le dialog
        closeButton.addActionListener(ev -> dialog.dispose());

        dialog.add(message);
        dialog.add(closeButton);
        dialog.setVisible(true);
    }


    public static void main(String[] args) {
        new Login(); // au lieu de MainPage
    }
}