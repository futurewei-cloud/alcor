package com.futurewei.alioth.controller.web;

public class DebugInfo {

    private final long id;
    private final String content;

    public DebugInfo(long id, String content) {
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