package com.example.wowlookup.dto;

public class RaidInfo {
    private String name;
    private int bossCount;

    public RaidInfo() {
    }

    public RaidInfo(String name, int bossCount) {
        this.name = name;
        this.bossCount = bossCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBossCount() {
        return bossCount;
    }

    public void setBossCount(int bossCount) {
        this.bossCount = bossCount;
    }
}