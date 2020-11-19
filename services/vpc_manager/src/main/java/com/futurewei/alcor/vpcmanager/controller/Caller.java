package com.futurewei.alcor.vpcmanager.controller;

public class Caller {

    private final long id;
    private final String content;

    public Caller(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}