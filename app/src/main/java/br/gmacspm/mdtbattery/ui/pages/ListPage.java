package br.gmacspm.mdtbattery.ui.pages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import br.gmacspm.mdtbattery.R;
import br.gmacspm.mdtbattery.adapter.ListAdapter;
import br.gmacspm.mdtbattery.config.Settings;
import br.gmacspm.mdtbattery.constants.ServiceConstants;
import br.gmacspm.mdtbattery.dialog.CustomDialog;
import br.gmacspm.mdtbattery.models.DischargeModel;
import br.gmacspm.mdtbattery.models.UsageModel;
import br.gmacspm.mdtbattery.services.BatteryMonitorService;
import br.gmacspm.mdtbattery.utils.TimeConverter;
import br.gmacspm.mdtbattery.viewmodel.DataViewModel;

public class ListPage extends Fragment {
    private Context context;
    private Settings settings;
    private boolean isVisible = false;
    private final ArrayList<DischargeModel> discharges = new ArrayList<>();
    private final ArrayList<UsageModel> usageModel = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = requireContext();
        settings = new Settings(context);
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CardView cardView = view.findViewById(R.id.list_cardview);
        ListView batteryList = view.findViewById(R.id.battery_list);

        TextView listTotal = view.findViewById(R.id.list_total);
        TextView listTotalOn = view.findViewById(R.id.list_total_ON);
        ProgressBar listTotalProgress = view.findViewById(R.id.list_total_progress);
        TextView listTotalOff = view.findViewById(R.id.list_total_OFF);

        ListAdapter adapter = new ListAdapter(discharges, context);
        batteryList.setAdapter(adapter);

        batteryList.setOnItemLongClickListener((parent, view1, position, id) -> {
            float mahMultiplier = getMahMultiplier(position);
            CustomDialog customDialog = new CustomDialog(
                    requireContext(),
                    String.format(
                            getString(R.string.dialog_mah_multiplier_set),
                            mahMultiplier
                    ),
                    mahMultiplier
            );
            customDialog.setListener(multiplier -> {
                settings.setMahMultiplier(multiplier);
                updateService();
            });
            customDialog.show();
            return false;
        });

        DataViewModel viewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);

        viewModel.getDataList().observe(getViewLifecycleOwner(), (Observer<? super ArrayList<UsageModel>>) usageModel -> {
            if (!usageModel.isEmpty()) {
                this.usageModel.clear();
                this.usageModel.addAll(usageModel);
            }
        });

        viewModel.getDataList().observe(getViewLifecycleOwner(), (Observer<? super ArrayList<UsageModel>>) dataList -> {
            if (!dataList.isEmpty()) {
                discharges.clear();
                long totalTimeActive = 0;
                long totalTimeInactive = 0;
                int totalProgress;
                for (UsageModel usageModel : dataList) {
                    long totalOnOff = ((usageModel.timeOn + usageModel.timeOff) > 0) ? (usageModel.timeOn + usageModel.timeOff) : 1;
                    int progress = (int) ((usageModel.timeOn * 100) / totalOnOff);

                    String humanTimeON = TimeConverter.getHumanTime(usageModel.timeOn, true);
                    String humanTimeOFF = TimeConverter.getHumanTime(usageModel.timeOff, true);
                    discharges.add(new DischargeModel(String.valueOf(usageModel.level), humanTimeON, humanTimeOFF, progress));

                    totalTimeActive = totalTimeActive + usageModel.timeOn;
                    totalTimeInactive = totalTimeInactive + usageModel.timeOff;
                }
                totalProgress = (int) ((totalTimeActive * 100) / (totalTimeActive + totalTimeInactive));
                if (!isVisible) {
                    cardView.setVisibility(View.VISIBLE);
                    isVisible = true;
                }
                adapter.notifyDataSetChanged();

                listTotal.setText(TimeConverter.getHumanTime(totalTimeActive + totalTimeInactive, true));
                listTotalOn.setText(TimeConverter.getHumanTime(totalTimeActive, true));
                listTotalProgress.setProgress(totalProgress);
                listTotalOff.setText(TimeConverter.getHumanTime(totalTimeInactive, true));
            } else {
                cardView.setVisibility(View.INVISIBLE);
                listTotal.setText(R.string.default_time);
                listTotalOn.setText(R.string.default_time);
                listTotalProgress.setProgress(50);
                listTotalOff.setText(R.string.default_time);
            }
        });
    }

    private float getMahMultiplier(int position) {
        float mahMultiplier = 1.0f;
        int progress = discharges.get(position).getProgress();
        float batteryCapacity = settings.getBatteryCapacity() / 100.0f;
        if (progress == 100 && batteryCapacity != 0.0f) {
            float mahHour = (batteryCapacity / TimeConverter.getSeconds(usageModel.get(position).timeOn)) * 3600.0f;
            mahMultiplier = mahHour / (float) usageModel.get(position).mah;
        } else if (progress == 0 && batteryCapacity != 0.0f) {
            float mahHour = (batteryCapacity / TimeConverter.getSeconds(usageModel.get(position).timeOff)) * 3600.0f;
            mahMultiplier = mahHour / (float) usageModel.get(position).mah;
        }
        return mahMultiplier;
    }

    private void updateService() {
        if (BatteryMonitorService.isServiceRunning) {
            Intent updateServiceIntent = new Intent(context, BatteryMonitorService.class);
            updateServiceIntent.setAction(ServiceConstants.UPDATE_VARIABLES);
            context.startService(updateServiceIntent);
        }
    }

    public static ListPage newInstance() {
        return new ListPage();
    }
}
