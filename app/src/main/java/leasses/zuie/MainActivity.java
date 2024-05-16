package leasses.zuie;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import leasses.zuie.dev.DevActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        startActivity(new Intent(this, DevActivity.class));
        finish();
        super.onCreate(savedInstanceState);
    }
}