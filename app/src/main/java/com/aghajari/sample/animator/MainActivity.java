package com.aghajari.sample.animator;

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

    public void onClick(View view) {
        try {
            startActivity(new Intent(this,
                    Class.forName("com.aghajari.sample.animator.activity." + view.getTag())));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}