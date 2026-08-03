package com.carpenter.business.supplier;

import com.carpenter.business.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class Supplier extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 140)
    private String name;

    @Column(length = 160)
    private String contactName;

    @Column(length = 254)
    private String email;

    @Column(length = 30)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean active = true;

    protected Supplier() { }

    public Supplier(String name, String contactName, String email, String phoneNumber) {
        this.name = name;
        this.contactName = contactName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getContactName() { return contactName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public boolean isActive() { return active; }

    public void update(String name, String contactName, String email, String phoneNumber, boolean active) {
        this.name = name;
        this.contactName = contactName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.active = active;
    }
}
