package uk.co.appoly.sceneform_example;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

/**
 * @Author: EchoZhou
 * @Date: 2018-10-09 10:01
 * @Description:
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        try {
            Session session = new Session(this);
        } catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        findViewById(R.id.btn_location).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 24)
                startActivity(new Intent(this, LocationActivity.class));
        });
    }
}
