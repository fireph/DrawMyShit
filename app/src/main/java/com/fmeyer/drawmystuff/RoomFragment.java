package com.fmeyer.drawmystuff;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class RoomFragment extends Fragment {
    Button goToRoomButton;
    EditText roomNameEditText;

    public RoomFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_room, container, false);

        roomNameEditText = (EditText) rootView.findViewById(R.id.room_name);
        goToRoomButton = (Button) rootView.findViewById(R.id.go_to_room_button);
        goToRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), DrawActivity.class);
                i.putExtra("room_name", roomNameEditText.getText().toString());
                startActivity(i);
            }
        });

        return rootView;
    }
}
