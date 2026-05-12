package com.example.wowlookup.dto;

public class RaidProgressInfo {
    private String raidName;
    private int completedRaidDifficulty;
    private String highestDifficultyProgress;
    private int progressDifficulty;

    public RaidProgressInfo() {
    }

    public RaidProgressInfo(String raidName, int completedRaidDifficulty, String highestDifficultyProgress, int progressDifficulty) {
        this.raidName = raidName;
        this.completedRaidDifficulty = completedRaidDifficulty;
        this.highestDifficultyProgress = highestDifficultyProgress;
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

    public String getHighestDifficultyProgress() {
        return highestDifficultyProgress;
    }

    public void setHighestDifficultyProgress(String highestDifficultyProgress) {
        this.highestDifficultyProgress = highestDifficultyProgress;
    }

    public int getProgressDifficulty() {
        return progressDifficulty;
    }

    public void setProgressDifficulty(int progressDifficulty) {
        this.progressDifficulty = progressDifficulty;
    }

    @Override
    public String toString() {
        return "RaidProgressInfo{" +
                "raidName='" + raidName + '\'' +
                ", completedRaidDifficulty=" + completedRaidDifficulty +
                ", highestDifficultyProgress='" + highestDifficultyProgress + '\'' +
                ", progressDifficulty=" + progressDifficulty +
                '}';
    }
}