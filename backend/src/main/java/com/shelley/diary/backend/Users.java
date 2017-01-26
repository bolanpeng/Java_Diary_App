package com.shelley.diary.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Users {
    @Id
    String user_id;
    String username;

    public Users() {}

    public String getUserId() {
        return  user_id;
    }

    public void setUserId(String id) {
        this.user_id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
