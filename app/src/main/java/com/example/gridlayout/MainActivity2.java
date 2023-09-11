package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();
        boolean wonGame = intent.getBooleanExtra("wonGame", false);

        if (wonGame) {
            String timePassed = Integer.toString(intent.getIntExtra("time", 0));
            // change text display
            TextView textView = findViewById(R.id.textViewResult);
            textView.setText("You Won! Passed in " + timePassed + "seconds");
        } else {
            String timePassed = Integer.toString(intent.getIntExtra("time", 0));
            // change text display
            TextView textView = findViewById(R.id.textViewResult);
            textView.setText("You Lost :(. Used up " + timePassed + "seconds");

        }

        findViewById(R.id.buttonNavigateToMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the second activity
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}