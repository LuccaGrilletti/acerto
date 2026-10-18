package br.com.acerto.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClientTest {

    @Test
    @DisplayName("Creates an active client with valid phone normalized to only digits")
    void createsActiveClientWithNormilizedPhone() {
        Client client = Client.create("   John Doe  ", "+1 (23) 456-7890");

        assertEquals("John Doe", client.getName());
        assertEquals("1234567890", client.getPhone());
        assertTrue(client.isActive());
    }

    @Test
    @DisplayName("Rejects the creation of a client with a blank name")
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> Client.create(" ", "+1 (23) 456-7890"));
    }

    @Test
    @DisplayName("Rejects the creation of a client with a null name")
    void rejectsNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> Client.create(null, "+1 (23) 456-7890"));
    }

    @Test
    @DisplayName("Rejects blank phone in the creation of a client")
    void rejectsBlankPhone() {
        assertThrows(IllegalArgumentException.class,
                () -> Client.create("John Doe", " "));
    }

    @Test
    @DisplayName("Rejects null phone in the creation of a client")
    void rejectsNullPhone() {
        assertThrows(IllegalArgumentException.class,
                () -> Client.create("John Doe", null));
    }

    @Test
    @DisplayName("Succesfully updates a client")
    void updatesClient() {
        Client client = Client.create("John Doe", "+1 (23) 456-7890");

        client.update("Kermit", "+2 (34) 5467-8910");

        assertEquals("Kermit", client.getName());
        assertEquals("23454678910", client.getPhone());
    }

    @Test
    @DisplayName("Deactivates an active client")
    void deactivatesClient() {
        Client client = Client.create("John Doe", "+1 (23) 456-7890");

        client.deactivate();

        assertFalse(client.isActive());
    }

    @Test
    @DisplayName("Activates an inactive client")
    void activatesClient() {
        Client client = Client.create("John Doe", "+1 (23) 456-7890");

        client.activate();

        assertTrue(client.isActive());
    }
}
