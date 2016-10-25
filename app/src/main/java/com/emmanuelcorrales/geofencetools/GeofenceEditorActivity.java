package com.emmanuelcorrales.geofencetools;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class GeofenceEditorActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_editor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EditText nameEt = (EditText) findViewById(R.id.geofence_name);
        EditText latitudeEt = (EditText) findViewById(R.id.latitude);
        EditText longitudeEt = (EditText) findViewById(R.id.longitude);
        EditText radiusEt = (EditText) findViewById(R.id.radius);

        if (validateEditText(nameEt) | validateEditText(latitudeEt) | validateEditText(longitudeEt)
                | validateEditText(radiusEt)) {
            String name = nameEt.getText().toString();
            double latitude = Double.valueOf(latitudeEt.getText().toString());
            double longitude = Double.valueOf(longitudeEt.getText().toString());
            float accuracy = Float.valueOf(radiusEt.getText().toString());
        } else {
            Snackbar.make(v, R.string.validation_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean validateEditText(EditText editText) {
        if (editText == null) {
            throw new IllegalArgumentException("Argument 'editText' cannot be null.");
        }
        if (editText.getText().toString().isEmpty()) {
            editText.setError(getString(R.string.validation_required));
            return false;
        }
        return true;
    }
}
