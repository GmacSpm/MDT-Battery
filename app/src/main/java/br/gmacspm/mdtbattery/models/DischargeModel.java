package br.gmacspm.mdtbattery.models;

public class DischargeModel {
    String level;
    String timeON;
    String timeOFF;
    int progress;

    public DischargeModel(String level, String timeON, String timeOFF, int progress) {
        this.level = level;
        this.timeON = timeON;
        this.timeOFF = timeOFF;
        this.progress = progress;
    }

    public String getLevel() {
        return level;
    }

    public String getTimeON() {
        return timeON;
    }

    public String getTimeOFF() {
        return timeOFF;
    }

    /**
     * @return Progress from 0(full screen inactive) to 100(screen full active)
     */
    public int getProgress() {
        return progress;
    }
}
