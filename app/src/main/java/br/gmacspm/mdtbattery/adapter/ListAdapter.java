package br.gmacspm.mdtbattery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import br.gmacspm.mdtbattery.R;
import br.gmacspm.mdtbattery.models.DischargeModel;
import br.gmacspm.mdtbattery.utils.ThemeColors;

public class ListAdapter extends ArrayAdapter<DischargeModel> {
    private final Context context;
    private static class ViewHolder {
        TextView txtLevel;
        TextView txtON;
        TextView txtOFF;
        ProgressBar progressBar;
    }

    public ListAdapter(ArrayList<DischargeModel> data, Context context) {
        super(context, R.layout.item_discharge_list, data);
        this.context = context;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        DischargeModel dischargeModel = getItem(position);
        ViewHolder viewHolder;

        final View result;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_discharge_list, parent, false);
            viewHolder.txtLevel = convertView.findViewById(R.id.list_level);
            viewHolder.txtON = convertView.findViewById(R.id.list_ON);
            viewHolder.txtOFF = convertView.findViewById(R.id.list_OFF);
            viewHolder.progressBar = convertView.findViewById(R.id.list_progress);
            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        if (dischargeModel != null) {
            viewHolder.txtLevel.setText(dischargeModel.getLevel());
            viewHolder.txtLevel.setTextColor(ThemeColors.getTextColor(context));
            viewHolder.txtON.setText(dischargeModel.getTimeON());
            viewHolder.txtOFF.setText(dischargeModel.getTimeOFF());
            viewHolder.progressBar.setProgress(dischargeModel.getProgress());
        }
        return result;
    }

}
