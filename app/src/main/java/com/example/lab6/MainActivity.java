package com.example.lab6;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //Method invoke by clicking the Button
    public void gameList(View v) {
        Intent intencja = new Intent(getApplicationContext(), GamesList.class);
        //Sending message what game was choose
        intencja.putExtra("gra", v.getId());
        startActivity(intencja);
    }
}
