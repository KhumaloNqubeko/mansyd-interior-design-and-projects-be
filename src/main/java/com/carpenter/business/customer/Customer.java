package com.carpenter.business.customer;

import com.carpenter.business.common.AuditableEntity;
import com.carpenter.business.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "address_line_1", nullable = false, length = 160)
    private String addressLine1;

    @Column(name = "address_line_2", length = 160)
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    protected Customer() { }

    public Customer(User user, String fullName, String phoneNumber, String addressLine1,
                    String addressLine2, String city, String postalCode) {
        this.user = user;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.postalCode = postalCode;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }

    public void updateProfile(String fullName, String phoneNumber, String addressLine1,
                              String addressLine2, String city, String postalCode) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.postalCode = postalCode;
    }
}
