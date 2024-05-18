package br.gmacspm.mdtbattery.room.background;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.gmacspm.mdtbattery.models.UsageModel;
import br.gmacspm.mdtbattery.room.database.BatteryDatabase;

public class BackgroundDB {
    private final BatteryDatabase batteryDatabase;

    public BackgroundDB(Context context) {
        this.batteryDatabase = BatteryDatabase.getInstance(context);
    }

    private List<UsageModel> getAllUsage() {
        return batteryDatabase.usageDao().getDischargeHistory();
    }

    public interface AllUsageCallback {
        void done(List<UsageModel> usageModel);
    }

    public void setAllUsageCallback(AllUsageCallback callback) {
        new BackgroundTask(callback).execAllUsage();
    }

    private class BackgroundTask {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        private final AllUsageCallback allUsageCallback;
        private List<UsageModel> usageHistory;

        private BackgroundTask(AllUsageCallback callback) {
            allUsageCallback = callback;
        }

        public void execAllUsage() {
            executorService.execute(() -> {
                usageHistory = getAllUsage();
                handler.post(() -> allUsageCallback.done(usageHistory));
            });
        }
    }

    public void insertUsage(UsageModel usageModel) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> batteryDatabase.usageDao().insertDischargeHistory(usageModel));
    }

    public void deleteAll() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> batteryDatabase.usageDao().deleteAllHistory());
    }
}
