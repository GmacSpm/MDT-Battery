package br.gmacspm.mdtbattery.interfaces;

public interface ServiceCallback {
    void updateUsageData();

    void resetTemp();

    void resetMah();

    void updateTemperature();

    void updateMah();
}
