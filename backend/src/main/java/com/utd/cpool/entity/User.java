package com.utd.cpool.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;


@Entity
@Table(name = "users")
public class User{
    @Id
     private UUID id;

     private String name;

     private String email;

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

     public String getName() {
         return name;
     }

     public String getEmail() {
         return email;
     }

     public UUID getId() {
         return id;
     }


}