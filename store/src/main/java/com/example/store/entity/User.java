package com.example.store.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", schema = "store")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "bank_account_id")
    private String bankAccountId;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<Order> orders = new ArrayList<>();

    public User() {

    }

    private User(Builder builder) {
        id = builder.userId;
        setUsername(builder.username);
        setPasswordHash(builder.passwordHash);
        setEmail(builder.email);
        setFirstName(builder.firstName);
        setLastName(builder.lastName);
        setBankAccountId(builder.bankAccountId);
        createdAt = builder.createdAt;
    }

    public void addOrder(Order order) {
        if (order == null) { return;}
        if (orders == null) { orders = new ArrayList<>();}
        if (!orders.contains(order)) {
            orders.add(order);
        }
        if (order.getUser() != this) {
            order.setUser(this);
        }
    }

    public void removeOrder(Order order) {
        if (order == null || orders == null) { return;}
        if (orders.remove(order)) {
            if (order.getUser() == this) {
                order.setUser(null);
            }
        }
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(String bankAccountId) {
        this.bankAccountId = bankAccountId;
    }


    public static final class Builder {
        private Integer userId;
        private String username;
        private String passwordHash;
        private String email;
        private String firstName;
        private String lastName;
        private String bankAccountId;
        private LocalDateTime createdAt;

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder userId(Integer val) {
            userId = val;
            return this;
        }

        public Builder username(String val) {
            username = val;
            return this;
        }

        public Builder passwordHash(String val) {
            passwordHash = val;
            return this;
        }

        public Builder email(String val) {
            email = val;
            return this;
        }

        public Builder firstName(String val) {
            firstName = val;
            return this;
        }

        public Builder lastName(String val) {
            lastName = val;
            return this;
        }

        public Builder bankAccountId(String val) {
            bankAccountId = val;
            return this;
        }

        public Builder createdAt(LocalDateTime val) {
            createdAt = val;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
