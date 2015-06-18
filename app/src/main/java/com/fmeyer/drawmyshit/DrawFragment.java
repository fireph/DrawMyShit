package com.fmeyer.drawmyshit;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DrawFragment extends Fragment {

    private static final int DRAW_ITEMS_TO_CONSUME = 10;
    private static final String PREFERENCES_FILE = "DRAWMYSHIT_PREFERENCES";

    MainDrawingView drawingView;
    ImageButton colorPickerButton;
    ImageButton eraseButton;
    ImageButton refreshButton;
    ToggleButton toggleButton;

    private Socket mSocket;
    private final Handler handler = new Handler();
    private static Runnable drawingRunnable;
    private boolean mCurrentlyDrawing = false;
    private ConcurrentLinkedQueue<JSONArray> mDrawingQueue = new ConcurrentLinkedQueue<JSONArray>();
    private boolean animationsEnabled = true;

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

        final SharedPreferences settings = getActivity().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();
        animationsEnabled = settings.getBoolean("animations", true);

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

        refreshButton = (ImageButton) rootView.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingQueue.clear();
                drawingView.erase(false);
                if (mSocket != null) {
                    mSocket.emit("getData");
                }
            }
        });

        toggleButton = (ToggleButton) rootView.findViewById(R.id.animation_switch);
        toggleButton.setChecked(animationsEnabled);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                animationsEnabled = isChecked;
                editor.putBoolean("animations", animationsEnabled);
                editor.apply();
            }
        });

        drawingRunnable = new Runnable() {
            @Override
            public void run() {
                mCurrentlyDrawing = true;
                try {
                    int i = DRAW_ITEMS_TO_CONSUME;
                    while ((animationsEnabled && i > 0 && !mDrawingQueue.isEmpty()) ||
                            (!animationsEnabled && !mDrawingQueue.isEmpty())) {
                        if (!mDrawingQueue.isEmpty()) {
                            drawSegment(mDrawingQueue.remove());
                        }
                        if (mDrawingQueue.isEmpty()) {
                            mCurrentlyDrawing = false;
                        }
                        i--;
                    }
                    if (mCurrentlyDrawing) {
                        handler.post(drawingRunnable);
                    }
                } catch(JSONException e){
                    Log.d("JSON", "Could not get JSON object for consuming");
                }
            }
        };

        return rootView;
    }

    public void drawSegment(JSONArray item) throws JSONException {
        String msgType = (String) item.get(0);
        JSONObject obj = (JSONObject) item.get(1);
        if (msgType.equals("lineTo")) {
            drawingView.lineTo(
                    (float) obj.getDouble("x"),
                    (float) obj.getDouble("y"),
                    (int) obj.getDouble("color"),
                    obj.getString("id"));
        } else if (msgType.equals("moveTo")) {
            drawingView.moveTo(
                    (float) obj.getDouble("x"),
                    (float) obj.getDouble("y"),
                    (int) obj.getDouble("color"),
                    obj.getString("id"));
        } else {
            Log.d("MSG", "Invalid msg type");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSocket.disconnect();
        mSocket = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSocket == null) {
            try {
                mSocket = IO.socket(getResources().getString(R.string.server_address));
            } catch (URISyntaxException e) {
                Log.d("SOCKET.IO", "This shit did not work!!!");
            }
        }

        final String roomName = getArguments().getString("room_name");
        if (mSocket != null) {
            final Socket nSocket = mSocket;
            nSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {}
            }).on("lineTo", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray arr = new JSONArray();
                    try {
                        arr.put(0, "lineTo");
                        arr.put(1, (JSONObject) args[0]);
                    } catch (JSONException e) {
                        Log.d("JSON", "Could not get JSON object for lineTo");
                    }
                    mDrawingQueue.add(arr);
                    if (!mCurrentlyDrawing) {
                        handler.post(drawingRunnable);
                    }
                }
            }).on("moveTo", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray arr = new JSONArray();
                    try {
                        arr.put(0, "moveTo");
                        arr.put(1, (JSONObject) args[0]);
                    } catch(JSONException e) {
                        Log.d("JSON", "Could not get JSON object for moveTo");
                    }
                    mDrawingQueue.add(arr);
                    if (!mCurrentlyDrawing) {
                        handler.post(drawingRunnable);
                    }
                }
            }).on("batch", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray arr = (JSONArray)args[0];
                    try {
                        for (int i = 0; i < arr.length(); i++) {
                            mDrawingQueue.add((JSONArray) arr.get(i));
                        }
                        if (!mCurrentlyDrawing) {
                            handler.post(drawingRunnable);
                        }
                    } catch(JSONException e) {
                        Log.d("JSON", "Could not get JSON object from batch");
                    }
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
            nSocket.emit("joinRoom", roomName);
            nSocket.emit("getData");
            drawingView.onLine(new MainDrawingView.OnLineListener() {
                @Override
                public void onLineTo(float x, float y, Paint paint) {
                    JSONObject pathData = new JSONObject();
                    try {
                        pathData.put("room", roomName);
                        pathData.put("type", "lineTo");
                        pathData.put("x", (double) x);
                        pathData.put("y", (double) y);
                        pathData.put("color", paint.getColor());
                    } catch (JSONException e) {
                        Log.d("JSON", "Could not set JSON object for lineTo");
                    }
                    nSocket.emit("lineTo", pathData);
                }

                @Override
                public void onMoveTo(float x, float y, Paint paint) {
                    JSONObject pathData = new JSONObject();
                    try {
                        pathData.put("room", roomName);
                        pathData.put("type", "moveTo");
                        pathData.put("x", (double) x);
                        pathData.put("y", (double) y);
                        pathData.put("color", paint.getColor());
                    } catch (JSONException e) {
                        Log.d("JSON", "Could not set JSON object for moveTo");
                    }
                    nSocket.emit("moveTo", pathData);
                }

                @Override
                public void onErase() {
                    nSocket.emit("erase", roomName);
                }
            });
        }
    }
}
