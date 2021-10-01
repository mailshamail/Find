package ru.mailshamail.find;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class nameActivity extends AppCompatActivity {

    Intent intent;
    EditText name;
    Button button;

    SharedPreferences settings;
    public final String Prefences = "setting";

    //public boolean IsOpenActiviti;
//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        createPermissions();

        intent = new Intent(getBaseContext(), MainActivity.class);

        name = findViewById(R.id.name);
        button = findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!name.getText().toString().equals(""))
                {
                    settings = getSharedPreferences(Prefences, Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = settings.edit();

                    editor.putString("name", name.getText().toString());
                    editor.apply();
                }
                startActivity(intent);
                Toast.makeText(getBaseContext(), "Загрузка карты", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            String permission = Manifest.permission.ACCESS_FINE_LOCATION;
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            {
                //Toast.makeText(this, "Разрешения -> разрешаем gps", Toast.LENGTH_LONG).show();

                //Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //Uri uri = Uri.fromParts("package", getPackageName(), null);
                //intent.setData(uri);
                //startActivity(intent);

                if(!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
                    requestPermissions(new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.FOREGROUND_SERVICE
                    } , 1);
                }
            }
        }
    }
}
