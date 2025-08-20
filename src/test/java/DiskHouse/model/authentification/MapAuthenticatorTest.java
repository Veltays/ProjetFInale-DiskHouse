package DiskHouse.model.authentification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapAuthenticatorTest {

    private MapAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator = new MapAuthenticator();
    }

    @Test
    void testAuthenticate_Successful() {
        assertTrue(authenticator.authenticate("utilisateur1", "motdepasse1"),
                "L'utilisateur devrait être authentifié avec les bons identifiants");
    }

    @Test
    void testAuthenticate_WrongPassword() {
        assertFalse(authenticator.authenticate("utilisateur1", "mauvaismotdepasse"),
                "L'utilisateur ne devrait pas être authentifié avec un mauvais mot de passe");
    }

    @Test
    void testAuthenticate_NonExistentUser() {
        assertFalse(authenticator.authenticate("inexistant", "motdepasse"),
                "Un utilisateur inexistant ne devrait pas être authentifié");
    }

    @Test
    void testRegister_NewUser() {
        assertTrue(authenticator.register("nouvelUtilisateur", "1234"),
                "L'inscription d'un nouvel utilisateur devrait réussir");
        assertTrue(authenticator.authenticate("nouvelUtilisateur", "1234"),
                "L'utilisateur nouvellement inscrit devrait pouvoir se connecter");
    }

    @Test
    void testRegister_ExistingUser() {
        assertFalse(authenticator.register("utilisateur1", "autreMotDePasse"),
                "L'inscription d'un utilisateur déjà existant devrait échouer");
    }
}
