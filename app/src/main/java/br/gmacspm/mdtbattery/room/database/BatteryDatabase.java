package br.gmacspm.mdtbattery.room.database;

import br.gmacspm.mdtbattery.models.UsageModel;
import br.gmacspm.mdtbattery.room.dao.UsageDao;

import android.content.Context;

import androidx.room.Room;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {UsageModel.class}, version = 3, exportSchema = false)
public abstract class BatteryDatabase extends RoomDatabase {
    private static BatteryDatabase database;
    private static final String DATABASE_NAME = "battery_database";

    public static synchronized BatteryDatabase getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(
                            context.getApplicationContext(),
                            BatteryDatabase.class, DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }
    public abstract UsageDao usageDao();
}
