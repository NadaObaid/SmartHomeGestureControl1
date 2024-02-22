package com.example.smarthomegesturecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner smartHomeGesturesSpinner = findViewById(R.id.smartHomeGesterList);
        smartHomeGesturesSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> smartHomeGesturesMyAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.smart_home_gestures));
        smartHomeGesturesMyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        smartHomeGesturesSpinner.setAdapter(smartHomeGesturesMyAdapter);
        //i want to create a drop-down menu for the list of the 17 gestures which are listed in the strings xml file and called from there as shown below
    }


    //i will retrieve the list items of gestures and when a gestures is chosen the app takes us to the WatchGestureActivity
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

        if (!item.equals("Select a Gesture")) {
            Intent watchGestureActivityIntent = new Intent(MainActivity.this, WatchGestureActivity.class);
            watchGestureActivityIntent.putExtra("gesture_name", item);
            startActivity(watchGestureActivityIntent);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }


}