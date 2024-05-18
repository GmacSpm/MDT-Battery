package br.gmacspm.mdtbattery.ui.pages;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import br.gmacspm.mdtbattery.R;
import br.gmacspm.mdtbattery.models.TimeModel;
import br.gmacspm.mdtbattery.models.UsageModel;
import br.gmacspm.mdtbattery.ui.customview.MarkView;
import br.gmacspm.mdtbattery.utils.ThemeColors;
import br.gmacspm.mdtbattery.utils.TimeConverter;
import br.gmacspm.mdtbattery.viewmodel.DataViewModel;

public class HomePage extends Fragment {
    private TextView textOnTime, textOffTime, textAvgOn, textAvgOff, textRemainingOn, textRemainingOff;
    private LineChart dischargeChart;
    private CircleProgressView circleProgressView;
    private TextView textRemainingOnTitle, textRemainingOffTitle, textTimeEndOn, textTimeEndOff;
    private int graphColor, textColor;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String strChargeActive = getString(R.string.charge_active);
        String strChargeInactive = getString(R.string.charge_inactive);
        String strRemainActive = getString(R.string.remaining_active);
        String strRemainInactive = getString(R.string.remaining_inactive);

        DataViewModel viewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);
        viewModel.getDataList().observe(getViewLifecycleOwner(), (Observer<? super ArrayList<UsageModel>>) dataList -> {
            if (!dataList.isEmpty()) {
                List<Entry> entries = new ArrayList<>();
                long sumTime = 0L;
                for (UsageModel usageModel : dataList) {
                    sumTime = sumTime + (usageModel.timeOn + usageModel.timeOff);
                    entries.add(new Entry(sumTime, usageModel.level));
                }
                updateGraphData(entries);
            } else {
                updateGraphData(null);
            }
        });
        viewModel.getTimeLevel().observe(getViewLifecycleOwner(), (Observer<? super TimeModel>) timeLevel -> {
            circleProgressView.setValueAnimated(timeLevel.currentLevel);
            textOnTime.setText(timeLevel.screenOnTime);
            textOffTime.setText(timeLevel.screenOffTime);
            textAvgOn.setText(timeLevel.avgOnTime);
            textAvgOff.setText(timeLevel.avgOffTime);
            textRemainingOn.setText(timeLevel.remainingOnTime);
            textRemainingOff.setText(timeLevel.remainingOffTime);
            textTimeEndOn.setText(timeLevel.timeEndOn);
            textTimeEndOff.setText(timeLevel.timeEndOff);
        });

        viewModel.getIsCharging().observe(getViewLifecycleOwner(), (Observer<? super Boolean>) isCharging -> {
            if (isCharging) {
                textRemainingOnTitle.setText(strChargeActive);
                textRemainingOffTitle.setText(strChargeInactive);
            } else {
                textRemainingOnTitle.setText(strRemainActive);
                textRemainingOffTitle.setText(strRemainInactive);
            }
        });

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private void updateGraphData(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setColor(graphColor);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setFillColor(graphColor);
        dataSet.setDrawFilled(true);
        dataSet.setHighLightColor(Color.BLACK);
        dataSet.disableDashedLine();

        LineData lineData = new LineData(dataSet);
        dischargeChart.setData(lineData);

        MarkView markView = new MarkView(HomePage.this.getContext());
        markView.setChartView(dischargeChart);
        dischargeChart.setDrawMarkers(true);
        dischargeChart.setMarker(markView);
        dischargeChart.invalidate(); // refresh
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Context context = getContext();
        super.onViewCreated(view, savedInstanceState);
        if (context != null) {
            graphColor = ThemeColors.getGraphColor(context);
            textColor = ThemeColors.getTextColor(context);
        }
        circleProgressView = view.findViewById(R.id.circleView);

        textOnTime = view.findViewById(R.id.home_text_on_time);
        textOffTime = view.findViewById(R.id.home_text_off_time);
        textAvgOn = view.findViewById(R.id.home_text_avg_on);
        textAvgOff = view.findViewById(R.id.home_text_avg_off);

        textRemainingOnTitle = view.findViewById(R.id.home_remaining_on_title);
        textRemainingOn = view.findViewById(R.id.home_remaining_on);

        textTimeEndOn = view.findViewById(R.id.home_time_end_on);

        textRemainingOffTitle = view.findViewById(R.id.home_remaining_off_title);
        textRemainingOff = view.findViewById(R.id.home_remaining_off);

        textTimeEndOff = view.findViewById(R.id.home_time_end_off);

        dischargeChart = view.findViewById(R.id.reportingChart);
        ImageButton buttonResetZoom = view.findViewById(R.id.home_graph_reset_zoom);
        buttonResetZoom.setOnClickListener(v -> {
            // Resetar zoom do grafico
            dischargeChart.fitScreen();
        });
        setUpGraph();
    }

    private void setUpGraph() {
        dischargeChart.setTouchEnabled(true);
        dischargeChart.setPinchZoom(true);
        dischargeChart.setDescription(null);

        dischargeChart.setExtraOffsets(5, 10, 5, 5);

        dischargeChart.getAxisRight().setEnabled(false);
        dischargeChart.getAxisRight().setDrawGridLines(false);
        dischargeChart.getAxisRight().setDrawAxisLine(false);

        dischargeChart.getAxisLeft().setDrawGridLines(false);
        dischargeChart.getAxisLeft().setAxisMaximum(100);
        dischargeChart.getAxisLeft().setAxisMinimum(0);
        dischargeChart.getAxisLeft().setTextColor(textColor);
        dischargeChart.getAxisLeft().setDrawAxisLine(false);

        dischargeChart.getXAxis().setDrawGridLines(false);
        dischargeChart.getXAxis().setDrawAxisLine(false);
        dischargeChart.getXAxis().setTextColor(textColor);
        dischargeChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return TimeConverter.getHumanTime((long) value, true);
            }
        });

        dischargeChart.getLegend().setEnabled(false);
    }

    public static HomePage newInstance() {
        return new HomePage();
    }
}
