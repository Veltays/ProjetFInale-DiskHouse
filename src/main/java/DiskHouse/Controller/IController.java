package DiskHouse.Controller;

import javax.swing.*;
import java.awt.*;


public interface IController<V> {

    /** Vue associée au contrôleur. */
    V getView();

    void initController();

    /* ================= Helpers communs ================= */

    default void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    default void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    default void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
