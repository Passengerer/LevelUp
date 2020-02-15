package com.laowuren.levelup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.laowuren.levelup.utils.BitmapManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button createButton = (Button)findViewById(R.id.button_create);
        Button joinButton = (Button)findViewById(R.id.button_join);
        createButton.setOnClickListener(this);
        joinButton.setOnClickListener(this);

        BitmapManager bm = new BitmapManager();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_create:
                Intent genIntent = new Intent(MainActivity.this, CreateActivity.class);
                startActivity(genIntent);
                break;
            case R.id.button_join:
                Intent joinIntent = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(joinIntent);
                break;
            default:
        }
    }
}
