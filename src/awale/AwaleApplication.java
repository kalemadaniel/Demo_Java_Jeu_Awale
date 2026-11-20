package awale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class AwaleApplication {
    private AwaleApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
                throw new IllegalStateException("Impossible d’activer le thème Nimbus.", ex);
            }
            new AwaleFrame().setVisible(true);
        });
    }
}
