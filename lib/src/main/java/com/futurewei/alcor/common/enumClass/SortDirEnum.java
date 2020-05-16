package com.futurewei.alcor.common.enumClass;

public enum SortDirEnum {

    ASC("asc"),
    DESC("desc");

    private String sortDir;

    SortDirEnum (String env) {
        this.sortDir = env;
    }

    public String getSortDir () {
        return sortDir;
    }
}
