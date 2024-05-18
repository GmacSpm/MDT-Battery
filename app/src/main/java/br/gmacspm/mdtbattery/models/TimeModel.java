package br.gmacspm.mdtbattery.models;

public class TimeModel {
    public final int currentLevel;
    public final String screenOnTime;
    public final String screenOffTime;
    public final String avgOnTime;
    public final String avgOffTime;
    public final String remainingOnTime;
    public final String remainingOffTime;

    public final String timeEndOn;
    public final String timeEndOff;


    public TimeModel(int currentLevel, String screenOnTime, String screenOffTime, String avgOnTime, String avgOffTime, String remainingOnTime, String remainingOffTime, String timeEndOn, String timeEndOff) {
        this.currentLevel = currentLevel;
        this.screenOnTime = screenOnTime;
        this.screenOffTime = screenOffTime;
        this.avgOnTime = avgOnTime;
        this.avgOffTime = avgOffTime;
        this.remainingOnTime = remainingOnTime;
        this.remainingOffTime = remainingOffTime;
        this.timeEndOn = timeEndOn;
        this.timeEndOff = timeEndOff;
    }
}
