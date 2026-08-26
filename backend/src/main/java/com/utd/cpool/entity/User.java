package com.utd.cpool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "users")
public class User{
    @Id
     private UUID id;

     private String name;

     private String email;

     @Column(name="password_hash")
     private String passwordHash;

     @Column(name="created_at")
     private LocalDateTime createdAt;
     
     @PrePersist
     protected void onCreate(){
        this.createdAt=LocalDateTime.now();
        if(this.id==null)
        {
            this.id=UUID.randomUUID();
        }
     }

     public User(){

     }

     public void setId(UUID id) {
         this.id = id;
     }

     public void setName(String name) {
         this.name = name;
     }

     public void setEmail(String email) {
         this.email = email;
     }

     public void setPassword(String password){
        this.passwordHash=password;
     }

     public String getName() {
         return name;
     }

     public String getEmail() {
         return email;
     }

     public UUID getId() {
         return id;
     }

     public String getPassword(){
        return passwordHash;
     }

}