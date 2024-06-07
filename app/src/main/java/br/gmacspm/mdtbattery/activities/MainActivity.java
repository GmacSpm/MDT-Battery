package br.gmacspm.mdtbattery.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import br.gmacspm.mdtbattery.R;
import br.gmacspm.mdtbattery.constants.ServiceConstants;
import br.gmacspm.mdtbattery.dialog.CustomDialog;
import br.gmacspm.mdtbattery.interfaces.ServiceCallback;
import br.gmacspm.mdtbattery.models.TimeModel;
import br.gmacspm.mdtbattery.services.BatteryMonitorService;
import br.gmacspm.mdtbattery.ui.pages.HomePage;
import br.gmacspm.mdtbattery.ui.pages.ListPage;
import br.gmacspm.mdtbattery.ui.pages.MeterPage;
import br.gmacspm.mdtbattery.viewmodel.DataViewModel;

public class MainActivity extends AppCompatActivity implements ServiceCallback {
    private static final String SELECTED_ITEM_ID = "selected_item_id";
    private BatteryMonitorService batteryMonitorService;
    private boolean isBound = false;
    private int lastClickedId;
    private DataViewModel viewModel;
    private Handler handler;
    private BottomNavigationView bottomNavigationView;

    // Strings from R.string
    private String dialogMessage;
    private String yes;
    private String no;
    private String titleMeter, titleMain, titleList;

    private void getStrings() {
        dialogMessage = getString(R.string.dialog_reset_message);
        yes = getString(R.string.yes);
        no = getString(R.string.no);

        titleMeter = getString(R.string.action_bar_meter);
        titleMain = getString(R.string.action_bar_general);
        titleList = getString(R.string.action_bar_list);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handler = new Handler(Looper.getMainLooper());
        viewModel = new ViewModelProvider(this).get(DataViewModel.class);
        getStrings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (lastClickedId != item.getItemId()) { // Prevent multiclick in same nav
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    if (item.getItemId() == R.id.nav_meter) {
                        actionBar.setTitle(titleMeter);
                        openFragment(MeterPage.newInstance());
                    } else if (item.getItemId() == R.id.nav_home) {
                        actionBar.setTitle(titleMain);
                        openFragment(HomePage.newInstance());
                    } else if (item.getItemId() == R.id.nav_list) {
                        actionBar.setTitle(titleList);
                        openFragment(ListPage.newInstance());
                    }
                }
                lastClickedId = itemId;
            }
            return true;
        });

        if (savedInstanceState != null) {
            int selectedItemId = savedInstanceState.getInt(SELECTED_ITEM_ID, R.id.nav_home);
            bottomNavigationView.setSelectedItemId(selectedItemId);
        } else {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, bottomNavigationView.getSelectedItemId());
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!BatteryMonitorService.isServiceRunning) {
            Intent startServiceIntent = new Intent(this, BatteryMonitorService.class);
            startServiceIntent.setAction(ServiceConstants.START_SERVICE);
            startService(startServiceIntent);
        }

        Intent intent = new Intent(this, BatteryMonitorService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        super.onStop();
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            isBound = true;
            BatteryMonitorService.MonitorBinder binder = (BatteryMonitorService.MonitorBinder) service;
            batteryMonitorService = binder.getService();
            batteryMonitorService.setServiceCallback(MainActivity.this);
            updateUsageData();
            updateMah();
            updateTemperature();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            isBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        batteryMonitorService.setServiceCallback(null);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_reset) {
            CustomDialog customDialog = new CustomDialog(MainActivity.this, dialogMessage);
            customDialog.setPositiveWord(yes);
            customDialog.setNegativeWord(no);
            customDialog.setListener(result ->
                    batteryMonitorService.resetMonitoring()
            );
            customDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateUsageData() {
        viewModel.setTimeLevelModel(new TimeModel(
                batteryMonitorService.getCurrentLevel(),
                batteryMonitorService.getTotalScreenTime(true),
                batteryMonitorService.getTotalScreenTime(false),
                batteryMonitorService.getAverageString(true),
                batteryMonitorService.getAverageString(false),
                batteryMonitorService.getRemainingCharge(true),
                batteryMonitorService.getRemainingCharge(false),
                batteryMonitorService.getTimeEnd(true),
                batteryMonitorService.getTimeEnd(false)
        ));
        viewModel.setDataList(batteryMonitorService.getDischargeList());
        viewModel.setIsCharging(batteryMonitorService.isCharging());

        Log.d("MainActivity", "updateUsageData");
    }


    @Override
    public void updateTemperature() {
        if (isBound) {
            viewModel.setTempMin(batteryMonitorService.getBatteryTempMin());
            viewModel.setTempActual(batteryMonitorService.getBatteryTemp());
            viewModel.setTempMax(batteryMonitorService.getBatteryTempMax());
        }
    }

    @Override
    public void resetTemp() {
        viewModel.resetTemp();
    }

    @Override
    public void updateMah() {
        if (isBound) {
            // background thread from service see: @private void startTimer()
            // it uses a timer to update mah, so we use handler here.
            handler.post(() -> {
                viewModel.setCurrentMin(batteryMonitorService.getBatteryCurrentMin());
                viewModel.setCurrentNow(batteryMonitorService.getBatteryCurrent());
                viewModel.setCurrentMax(batteryMonitorService.getBatteryCurrentMax());
            });
        }
    }

    @Override
    public void resetMah() {
        viewModel.resetMah();
    }

}