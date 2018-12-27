package com.example.demo.model;


import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="order")
public class Order {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private int status;

}
