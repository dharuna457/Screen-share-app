package com.example.screenshare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton shareButton = findViewById(R.id.shareButton);
        MaterialButton viewButton = findViewById(R.id.viewButton);

        shareButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                startActivity(new Intent(MainActivity.this, ShareScreenActivity.class));
            } else {
                requestPermissions();
            }
        });

        viewButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ViewScreenActivity.class));
        });
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(MainActivity.this, ShareScreenActivity.class));
            } else {
                Toast.makeText(this, "Permissions required for screen sharing",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
