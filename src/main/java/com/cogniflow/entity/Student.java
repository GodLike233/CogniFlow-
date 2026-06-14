package com.cogniflow.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    public Student() {}

    public Student(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRoleDisplayName() {
        return "学生";
    }
}
