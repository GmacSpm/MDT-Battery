package br.gmacspm.mdtbattery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import br.gmacspm.mdtbattery.models.TimeModel;
import br.gmacspm.mdtbattery.models.UsageModel;

public class DataViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<UsageModel>> dataList = new MutableLiveData<>();
    private final MutableLiveData<TimeModel> timeLevelModel = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCharging = new MutableLiveData<>();

    private final MutableLiveData<Integer> currentMin = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentNow = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentMax = new MutableLiveData<>();

    private final MutableLiveData<Integer> tempMin = new MutableLiveData<>();
    private final MutableLiveData<Integer> tempActual = new MutableLiveData<>();
    private final MutableLiveData<Integer> tempMax = new MutableLiveData<>();

    public void setIsCharging(Boolean charging) {
        isCharging.setValue(charging);
    }

    public LiveData<Boolean> getIsCharging() {
        return isCharging;
    }

    public void setTimeLevelModel(TimeModel timeLevelModel) {
        this.timeLevelModel.setValue(timeLevelModel);
    }

    public LiveData<TimeModel> getTimeLevel() {
        return timeLevelModel;
    }

    public void setDataList(ArrayList<UsageModel> data) {
        dataList.setValue(data);
    }

    public LiveData<ArrayList<UsageModel>> getDataList() {
        return dataList;
    }

    public void setCurrentNow(int now) {
        if (currentNow.getValue() != null) {
            if (now != currentNow.getValue()) {
                currentNow.setValue(now);
            }
        } else {
            currentNow.setValue(now);
        }
    }

    public LiveData<Integer> getCurrentNow() {
        return currentNow;
    }

    public void setCurrentMin(int min) {
        if (currentMin.getValue() != null) {
            int lastMin = currentMin.getValue();
            if (min < lastMin || lastMin == 0 || min == 0) currentMin.setValue(min);
        } else {
            currentMin.setValue(min); // Just set, it's null anyway
        }
    }

    public LiveData<Integer> getCurrentMin() {
        return currentMin;
    }

    public void setCurrentMax(int max) {
        if (currentMax.getValue() != null) {
            if (max > currentMax.getValue() || max == 0) currentMax.setValue(max);
        } else {
            currentMax.setValue(max);// Just set, it's null anyway
        }
    }

    public LiveData<Integer> getCurrentMax() {
        return currentMax;
    }

    public void resetMah() {
        currentMin.setValue(0);
        currentNow.setValue(0);
        currentMax.setValue(0);
    }

    public void setTempActual(int temp) {
        if (tempActual.getValue() != null) {
            int lastActual = tempActual.getValue();
            if (temp != lastActual) {
                tempActual.setValue(temp);
            }
        } else {
            tempActual.setValue(temp);
        }
    }

    public LiveData<Integer> getTempActual() {
        return tempActual;
    }

    public void setTempMin(int min) {
        if (tempMin.getValue() != null) {
            int lastMin = tempMin.getValue();
            if (min < lastMin || min == 0 || lastMin == 0) {
                tempMin.setValue(min);
            }
        } else {
            tempMin.setValue(min);
        }
    }

    public LiveData<Integer> getTempMin() {
        return tempMin;
    }

    public void setTempMax(int max) {
        if (tempMax.getValue() != null) {
            if (max > tempMax.getValue() || max == 0) tempMax.setValue(max);
        } else {
            tempMax.setValue(max);// Just set, it's null anyway
        }
    }

    public LiveData<Integer> getTempMax() {
        return tempMax;
    }

    public void resetTemp() {
        tempMin.setValue(0);
        tempActual.setValue(0);
        tempMax.setValue(0);
    }
}
