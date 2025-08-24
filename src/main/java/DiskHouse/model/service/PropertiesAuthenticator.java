package DiskHouse.model.service;

import DiskHouse.model.authentification.Authenticator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Authenticator basé sur un fichier users.properties.
 * Format : username=password (clair pour l'exo ; à hasher en vrai projet).
 *
 * ⚠️ On ne modifie NI Authenticator NI MapAuthenticator.
 *    On ajoute juste des méthodes publiques de façade ici.
 */
public class PropertiesAuthenticator extends Authenticator {

    private final File usersFile;

    public PropertiesAuthenticator() {
        this(new File("users.properties"));
    }

    public PropertiesAuthenticator(File file) {
        this.usersFile = file;
    }

    // ---------- Façades PUBLIQUES demandées ----------
    /** Inscription publique qui déléguera à la template-method protégée register(..). */
    public boolean registerUser(String username, String password) {
        return register(username, password); // <-- appelle la méthode protégée de la classe abstraite
    }

    /** Optionnel : helper public pour savoir si un login existe déjà. */
    public boolean isExistingUser(String username) {
        return isLoginExists(username);
    }
    // --------------------------------------------------

    @Override
    protected boolean isLoginExists(String username) {
        Properties props = load();
        return props.containsKey(username);
    }

    @Override
    protected String getPassword(String username) {
        Properties props = load();
        return props.getProperty(username);
    }

    @Override
    protected boolean register(String username, String password) {
        Properties props = load();
        if (props.containsKey(username)) {
            return false;
        }
        props.setProperty(username, password);
        store(props);
        return true;
    }

    private Properties load() {
        Properties props = new Properties();
        if (usersFile.exists()) {
            try (InputStream in = new FileInputStream(usersFile)) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return props;
    }

    private void store(Properties props) {
        try (OutputStream out = new FileOutputStream(usersFile)) {
            props.store(new OutputStreamWriter(out, StandardCharsets.UTF_8), "DiskHouse users");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
