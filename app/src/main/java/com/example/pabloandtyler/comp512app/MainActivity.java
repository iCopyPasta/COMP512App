package com.example.pabloandtyler.comp512app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText wordSpace = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText wordSpace = (EditText) findViewById(R.id.type_space);
        wordSpace.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);


    }
}
