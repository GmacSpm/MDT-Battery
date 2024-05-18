package br.gmacspm.mdtbattery.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * This class contains mahAverage, tempAverage, timeOff, timeON and batteryLevel
 */
@Entity(tableName = "usage_database")
public class UsageModel {
    @PrimaryKey()
    public final int level;
    @ColumnInfo(name = "timeOff")
    public final long timeOff;
    @ColumnInfo(name = "timeOn")
    public final long timeOn;
    @ColumnInfo(name = "mah")
    public final int mah;
    @ColumnInfo(name = "temp")
    public final float temp;

    public UsageModel(int level, long timeOn, long timeOff, int mah, float temp) {
        this.level = level;
        this.timeOn = timeOn;
        this.timeOff = timeOff;
        this.mah = mah;
        this.temp = temp;
    }
}
