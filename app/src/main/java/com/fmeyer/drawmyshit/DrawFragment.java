package com.fmeyer.drawmyshit;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

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

        Socket socket = null;

        try {
            socket = IO.socket("http://drawmyshit.herokuapp.com");
        } catch (URISyntaxException e) {
            Log.d("SOCKET.IO", "This shit did not work!!!");
        }

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
                drawingView.erase(true);
            }
        });

        if (socket != null) {
            final Socket nSocket = socket;
            nSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {}
            }).on("lineTo", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    float x = 0;
                    float y = 0;
                    int color = 0;
                    String id = null;
                    try {
                        id = (String) obj.getString("id");
                        x = (float) obj.getDouble("x");
                        y = (float) obj.getDouble("y");
                        color = (int) obj.getDouble("color");
                    } catch(JSONException e) {
                        Log.d("JSON", "Could not get JSON object for lineTo");
                    }
                    drawingView.lineTo(x, y, color, id);
                }
            }).on("moveTo", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    float x = 0;
                    float y = 0;
                    int color = 0;
                    String id = null;
                    try {
                        id = (String) obj.getString("id");
                        x = (float) obj.getDouble("x");
                        y = (float) obj.getDouble("y");
                        color = (int) obj.getDouble("color");
                    } catch(JSONException e) {
                        Log.d("JSON", "Could not get JSON object for moveTo");
                    }
                    drawingView.moveTo(x, y, color, id);
                }
            }).on("erase", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    drawingView.erase(false);
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                }
            });
            nSocket.connect();
            drawingView.onLine(new MainDrawingView.OnLineListener() {
                @Override
                public void onLineTo(float x, float y, Paint paint) {
                    JSONObject pathData = new JSONObject();
                    try {
                        pathData.put("x", (double) x);
                        pathData.put("y", (double) y);
                        pathData.put("color", paint.getColor());
                    } catch(JSONException e) {
                        Log.d("JSON", "Could not set JSON object for lineTo");
                    }
                    nSocket.emit("lineTo", pathData);
                }

                @Override
                public void onMoveTo(float x, float y, Paint paint) {
                    JSONObject pathData = new JSONObject();
                    try {
                        pathData.put("x", (double) x);
                        pathData.put("y", (double) y);
                        pathData.put("color", paint.getColor());
                    } catch(JSONException e) {
                        Log.d("JSON", "Could not set JSON object for moveTo");
                    }
                    nSocket.emit("moveTo", pathData);
                }

                @Override
                public void onErase() {
                    nSocket.emit("erase");
                }
            });
        }

        return rootView;
    }
}
