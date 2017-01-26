package com.shelley.diary.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class MyDiary {
    @Id
    Long diary_id;
    String title;
    String content;
    @Index
    String user_id;

    public MyDiary() {}

    public Long getDiaryId() {
        return diary_id;
    }

    public void setDiaryId(Long id) {
        this.diary_id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }
}