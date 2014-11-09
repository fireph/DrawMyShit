package com.fmeyer.drawmyshit;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

/**
 * Created by fmeyer on 11/8/14.
 */
public class ColorPickerDialogFragment extends DialogFragment {
    int mColor;
    OnDialogColorChangedListener colorListener;
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static ColorPickerDialogFragment newInstance(int color) {
        ColorPickerDialogFragment f = new ColorPickerDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("color", color);
        f.setArguments(args);

        return f;
    }

    public interface OnDialogColorChangedListener{
        public void OnColorChange(int color);
    }

    public void onColorUpdated(OnDialogColorChangedListener listener) {
        colorListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColor = getArguments().getInt("color");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vDialog = inflater.inflate(R.layout.fragment_color_picker_dialog, container, false);
        getDialog().setTitle("Color Picker");
        final ColorPicker colorPicker = (ColorPicker) vDialog.findViewById(R.id.picker);
        colorPicker.setShowOldCenterColor(false);
        SVBar svBar = (SVBar) vDialog.findViewById(R.id.svbar);

        colorPicker.addSVBar(svBar);

        colorPicker.setColor(mColor);

        Button okButton = (Button) vDialog.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorListener.OnColorChange(colorPicker.getColor());
                getDialog().dismiss();
            }
        });
        Button cancelButton = (Button) vDialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return vDialog;
    }
}
