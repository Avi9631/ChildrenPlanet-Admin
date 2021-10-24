package com.example.adminchildplanet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button postbtn, catbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        postbtn= findViewById(R.id.postbtn);
        catbtn= findViewById(R.id.catbtn);

        postbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent p = new Intent(MainActivity.this,PostActivity.class);
                startActivity(p);
            }
        });

        catbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent c = new Intent(MainActivity.this,CategoryActivity.class);
                startActivity(c);
            }
        });


    }
}
