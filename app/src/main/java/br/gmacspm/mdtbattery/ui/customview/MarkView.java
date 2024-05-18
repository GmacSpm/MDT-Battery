package br.gmacspm.mdtbattery.ui.customview;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.Locale;

import br.gmacspm.mdtbattery.R;

public class MarkView extends MarkerView {
    private final TextView text;

    public MarkView(Context context) {
        super(context, R.layout.item_marker_view);
        text = findViewById(R.id.item_marker_view_text);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String result;
        float yAxix = e.getY();
        if ((int) yAxix == yAxix) {
            result = String.valueOf((int) yAxix);
        } else {
            result = String.format(Locale.getDefault(), "%.1f", yAxix);
        }
        text.setText(result);
        super.refreshContent(e, highlight);
    }

}
