package com.cogniflow.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    public Admin() {}

    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRoleDisplayName() {
        return "管理员";
    }
}
