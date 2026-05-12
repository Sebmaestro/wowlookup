package com.example.wowlookup.dto;

public class RaidProgressInfo {
    private String raidName;
    private int completedRaidDifficulty;
    private int progressBossCount;
    private int totalBossCount;
    private int progressDifficulty;
    

    public RaidProgressInfo() {
    }

    public RaidProgressInfo(String raidName, int completedRaidDifficulty, int progressBossCount, int totalBossCount, int progressDifficulty) {
        this.raidName = raidName;
        this.completedRaidDifficulty = completedRaidDifficulty;
        this.progressBossCount = progressBossCount;
        this.totalBossCount = totalBossCount;
        this.progressDifficulty = progressDifficulty;
    }

    public String getRaidName() {
        return raidName;
    }

    public void setRaidName(String raidName) {
        this.raidName = raidName;
    }

    public int getCompletedRaidDifficulty() {
        return completedRaidDifficulty;
    }

    public void setCompletedRaidDifficulty(int completedRaidDifficulty) {
        this.completedRaidDifficulty = completedRaidDifficulty;
    }

    public int getProgressBossCount() {
        return progressBossCount;
    }

    public void setProgressBossCount(int progressBossCount) {
        this.progressBossCount = progressBossCount;
    }

    public int getTotalBossCount() {
        return totalBossCount;
    }

    public void setTotalBossCount(int totalBossCount) {
        this.totalBossCount = totalBossCount;
    }

    public int getProgressDifficulty() {
        return progressDifficulty;
    }

    public void setProgressDifficulty(int progressDifficulty) {
        this.progressDifficulty = progressDifficulty;
    }


}