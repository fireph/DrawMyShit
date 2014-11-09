package com.fmeyer.drawmyshit;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.larswerkman.holocolorpicker.ColorPicker;

/**
 * Created by fmeyer on 11/8/14.
 */
public class DrawFragment extends Fragment {
    MainDrawingView drawingView;
    ImageButton colorPickerButton;
    ImageButton eraseButton;

    int currentColor = Color.BLUE;

    void showDialog(int color) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = ColorPickerDialogFragment.newInstance(color);
        ColorPickerDialogFragment cpf = (ColorPickerDialogFragment) newFragment;
        cpf.onColorUpdated(new ColorPickerDialogFragment.OnDialogColorChangedListener() {
            @Override
            public void OnColorChange(int color) {
                currentColor = color;
                drawingView.setColor(currentColor);
            }
        });
        newFragment.show(ft, "dialog");
    }

    public DrawFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_draw, container, false);

        drawingView = (MainDrawingView) rootView.findViewById(R.id.single_touch_view);
        drawingView.setColor(currentColor);
        colorPickerButton = (ImageButton) rootView.findViewById(R.id.color_picker_button);
        colorPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(currentColor);
            }
        });

        eraseButton = (ImageButton) rootView.findViewById(R.id.erase_button);
        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.erase();
            }
        });
        return rootView;
    }
}
