package br.gmacspm.mdtbattery.room.database;

import br.gmacspm.mdtbattery.models.UsageModel;
import br.gmacspm.mdtbattery.room.dao.UsageDao;

import android.content.Context;

import androidx.room.Room;
import androidx.room.Database;
import androidx.room.RoomDatabase;

// Annotation para nossas entities da database e db version.
@Database(entities = {UsageModel.class}, version = 3, exportSchema = false)
public abstract class BatteryDatabase extends RoomDatabase {
    // Criar instância
    private static BatteryDatabase database;

    // Definir nome para o database
    private static final String DATABASE_NAME = "battery_database";

    // Pegar instância para nosso database
    public static synchronized BatteryDatabase getInstance(Context context) {

        // Verificar se instância já existe
        if (database == null) {
            // Criar nova instância se null.
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
