package br.com.acerto.client;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "clients")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(nullable = false)
    private boolean active;

    private Client(String name, String phone) {
        this.name = requireName(name);
        this.phone = normalizePhone(phone);
        this.active = true;
    }

    public static Client create(String name, String phone) {
        return new Client(name, phone);
    }

    public void update(String name, String phone) {
        this.name = requireName(name);
        this.phone = normalizePhone(phone);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        return name.strip();
    }

    private static String normalizePhone(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("Phone is required");
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("Phone must contain digits");
        }
        return digits;
    }
}