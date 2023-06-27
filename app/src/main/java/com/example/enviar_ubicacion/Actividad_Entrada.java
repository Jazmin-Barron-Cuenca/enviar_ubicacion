package com.example.enviar_ubicacion;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

public class Actividad_Entrada extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada);

        new Handler().postDelayed(new Runnable(){
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            public void run(){


                Intent intent = new Intent(Actividad_Entrada.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();

            }
        }, 6000);
    }
}