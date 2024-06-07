package br.gmacspm.mdtbattery.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import br.gmacspm.mdtbattery.R;

public class CustomDialog extends Dialog {
    public interface onResult {
        void done(float max);
    }

    public onResult listener;

    public void setListener(onResult listener) {
        this.listener = listener;
    }

    public void setPositiveWord(String positive) {
        ((TextView) findViewById(R.id.dialog_button_apply)).setText(positive);
    }

    public void setNegativeWord(String negative){
        ((TextView) findViewById(R.id.dialog_button_cancel)).setText(negative);
    }

    public CustomDialog(@NonNull Context context, String title, String hint) {
        super(context);
        Window window = this.getWindow();
        if (window != null) {
            this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);

        TextView textTitle = findViewById(R.id.dialog_text_title);
        textTitle.setText(title);

        EditText editContent = findViewById(R.id.dialog_edit);
        editContent.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String value = editContent.getText().toString();
                if (!value.isEmpty() && !value.contentEquals(".")) {
                    listener.done(Float.parseFloat(value));
                }
                dismiss();
            }
            return false;
        });
        editContent.setHint(hint);

        Button buttonApply = findViewById(R.id.dialog_button_apply);
        buttonApply.setOnClickListener(v -> {
            String value = editContent.getText().toString();
            if (!value.isEmpty() && !value.contentEquals(".")) {
                listener.done(Float.parseFloat(value));
            }
            dismiss();
        });

        Button buttonDeny = findViewById(R.id.dialog_button_cancel);
        buttonDeny.setOnClickListener(v -> dismiss());
    }

    public CustomDialog(@NonNull Context context, String title, float value) {
        super(context);
        Window window = this.getWindow();
        if (window != null) {
            this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);

        TextView textTitle = findViewById(R.id.dialog_text_title);
        textTitle.setText(title);

        EditText editContent = findViewById(R.id.dialog_edit);
        editContent.setVisibility(View.GONE);

        Button buttonApply = findViewById(R.id.dialog_button_apply);
        buttonApply.setOnClickListener(v -> {
            listener.done(value);
            dismiss();
        });

        Button buttonDeny = findViewById(R.id.dialog_button_cancel);
        buttonDeny.setOnClickListener(v -> dismiss());
    }

    public CustomDialog(Context context, String title) {
        super(context);
        Window window = this.getWindow();
        if (window != null) {
            this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom);

        TextView textTitle = findViewById(R.id.dialog_text_title);
        textTitle.setText(title);

        EditText editContent = findViewById(R.id.dialog_edit);
        editContent.setVisibility(View.GONE);

        Button buttonApply = findViewById(R.id.dialog_button_apply);
        buttonApply.setOnClickListener(v -> {
            listener.done(0);
            dismiss();
        });

        Button buttonDeny = findViewById(R.id.dialog_button_cancel);
        buttonDeny.setOnClickListener(v -> dismiss());
    }

}
