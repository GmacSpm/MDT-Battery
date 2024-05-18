package br.gmacspm.mdtbattery.ui.pages;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;
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

import br.gmacspm.mdtbattery.R;
import br.gmacspm.mdtbattery.config.Settings;
import br.gmacspm.mdtbattery.models.UsageModel;
import br.gmacspm.mdtbattery.ui.customview.MarkView;
import br.gmacspm.mdtbattery.utils.ThemeColors;
import br.gmacspm.mdtbattery.utils.TimeConverter;
import br.gmacspm.mdtbattery.viewmodel.DataViewModel;

public class MeterPage extends Fragment {
    private DataViewModel viewModel;
    private LineChart mahTempChart;
    private LineDataSet mahDataSet, tempDataSet;
    private boolean useMah = true;
    private int graphColor, textColor;
    private Settings settings;
    private float mahMultiplier;

    public static Fragment newInstance() {
        return new MeterPage();
    }

    private void updateGraphData(List<Entry> mahList, List<Entry> tempList) {
        mahDataSet = new LineDataSet(mahList, "mAh");
        mahDataSet.setColor(graphColor);
        mahDataSet.setDrawCircles(false);
        mahDataSet.setDrawValues(false);
        mahDataSet.setFillColor(graphColor);
        mahDataSet.setDrawFilled(true);
        mahDataSet.setHighLightColor(Color.BLACK);
        mahDataSet.disableDashedLine();

        tempDataSet = new LineDataSet(tempList, "temp");
        tempDataSet.setColor(graphColor);
        tempDataSet.setDrawCircles(false);
        tempDataSet.setDrawValues(false);
        tempDataSet.setFillColor(graphColor);
        tempDataSet.setHighLightColor(Color.BLACK);
        tempDataSet.setDrawFilled(true);
        tempDataSet.disableDashedLine();

        changeGraphData();
    }

    private void changeGraphData() {
        LineData lineData;
        if (useMah) {
            lineData = new LineData(mahDataSet);
        } else {
            lineData = new LineData(tempDataSet);
        }
        mahTempChart.setData(lineData);
        mahTempChart.invalidate(); // refresh
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);
        return inflater.inflate(R.layout.fragment_meter, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mahMultiplier = settings.getMahMultiplier();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        settings = new Settings(context);
        super.onViewCreated(view, savedInstanceState);
        graphColor = ThemeColors.getGraphColor(context);
        textColor = ThemeColors.getTextColor(context);

        TextView textMinCurrent = view.findViewById(R.id.meter_min_current_text);
        TextView textCurrentNow = view.findViewById(R.id.meter_current_text);
        TextView textMaxCurrent = view.findViewById(R.id.meter_max_current_text);

        TextView textMinTemp = view.findViewById(R.id.meter_min_temp_text);
        TextView textActualTemp = view.findViewById(R.id.meter_actual_temp_text);
        TextView textMaxTemp = view.findViewById(R.id.meter_max_temp_text);

        mahTempChart = view.findViewById(R.id.currentTempChart);

        ImageButton buttonResetZoom = view.findViewById(R.id.meter_graph_reset_zoom);
        buttonResetZoom.setOnClickListener(v -> {
            mahTempChart.fitScreen();
        });

        RadioGroup radioGroup = view.findViewById(R.id.meter_radiogp);
        radioGroup.check(useMah ? R.id.meter_radio_button_mah : R.id.meter_radio_button_temp);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.meter_radio_button_mah) {
                useMah = true;
            } else if (checkedId == R.id.meter_radio_button_temp) {
                useMah = false;
            }
            changeGraphData();
        });

        viewModel.getDataList().observe(getViewLifecycleOwner(), (Observer<? super ArrayList<UsageModel>>) usageModel -> {
            if (!usageModel.isEmpty()) {
                List<Entry> mahList = new ArrayList<>();
                List<Entry> tempList = new ArrayList<>();
                long sumTime = 0L;
                for (UsageModel model : usageModel) {
                    sumTime = sumTime + (model.timeOff + model.timeOn);
                    mahList.add(new Entry(sumTime, model.mah * mahMultiplier));
                    tempList.add(new Entry(sumTime, model.temp));
                }
                updateGraphData(mahList, tempList);
            } else {
                updateGraphData(null, null);
            }
        });

        viewModel.getCurrentMin().observe(getViewLifecycleOwner(), (Observer<? super Integer>) min -> {
            min = (int) (min * mahMultiplier);
            textMinCurrent.setText((min == 0) ? "--" : String.valueOf(min));
        });
        viewModel.getCurrentNow().observe(getViewLifecycleOwner(), (Observer<? super Integer>) currentNow -> {
            currentNow = (int) (currentNow * mahMultiplier);
            textCurrentNow.setText((currentNow == 0) ? "--" : String.valueOf(currentNow));
        });
        viewModel.getCurrentMax().observe(getViewLifecycleOwner(), (Observer<? super Integer>) max -> {
            max = (int) (max * mahMultiplier);
            textMaxCurrent.setText((max == 0) ? "--" : String.valueOf(max));
        });

        viewModel.getTempMin().observe(getViewLifecycleOwner(), (Observer<? super Integer>) min -> {
            textMinTemp.setText((min == 0) ? "--" : String.valueOf((float) min / 10));
        });
        viewModel.getTempActual().observe(getViewLifecycleOwner(), (Observer<? super Integer>) actual -> {
            textActualTemp.setText((actual == 0) ? "--" : String.valueOf((float) actual / 10));
        });

        viewModel.getTempMax().observe(getViewLifecycleOwner(), (Observer<? super Integer>) max -> {
            textMaxTemp.setText((max == 0) ? "--" : String.valueOf((float) max / 10));
        });
        setUpGraph();
    }

    private void setUpGraph() {
        MarkView markView = new MarkView(MeterPage.this.getContext());
        markView.setChartView(mahTempChart);
        mahTempChart.setMarker(markView);
        mahTempChart.setTouchEnabled(true);
        mahTempChart.setPinchZoom(true);
        mahTempChart.setDescription(null);

        mahTempChart.setExtraOffsets(5, 10, 5, 5);

        mahTempChart.getAxisRight().setEnabled(false);
        mahTempChart.getAxisRight().setDrawGridLines(false);
        mahTempChart.getAxisRight().setDrawAxisLine(false);

        mahTempChart.getAxisLeft().setDrawGridLines(false);
        mahTempChart.getAxisLeft().setAxisMinimum(0);
        mahTempChart.getAxisLeft().setTextColor(textColor);
        mahTempChart.getAxisLeft().setDrawAxisLine(false);

        mahTempChart.getXAxis().setDrawGridLines(false);
        mahTempChart.getXAxis().setDrawAxisLine(false);
        mahTempChart.getXAxis().setTextColor(textColor);
        mahTempChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return TimeConverter.getHumanTime((long) value, true);
            }
        });

        mahTempChart.getLegend().setEnabled(false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
