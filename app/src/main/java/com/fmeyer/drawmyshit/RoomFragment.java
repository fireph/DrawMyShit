package com.fmeyer.drawmyshit;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class RoomFragment extends Fragment {
    Button goToRoomButton;

    public RoomFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_room, container, false);

        goToRoomButton = (Button) rootView.findViewById(R.id.go_to_room_button);
        goToRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), DrawActivity.class);
                startActivity(i);
            }
        });

        return rootView;
    }
}
