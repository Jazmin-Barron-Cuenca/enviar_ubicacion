package com.example.enviar_ubicacion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends AppCompatActivity {

    private Button btnguardar,btnpermisos,btn_permisosADMIN;
    private EditText NumeroEDIT;

    private Boolean PermisosOK = false;

    private static final int PERMISSION_REQUEST_CODE_ACTION = 1;
    private static final int REQUEST_CODE_PERMISOS = 1;

    private static final int PERMISSION_REQUEST_CODE_POST = 1;



    String[] permisos = {
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE
    };



    String[] permisos2 = {
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.CALL_PHONE
    };

    private AudioManager audioManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnguardar = findViewById(R.id.btn_guardar);
        btnpermisos = findViewById(R.id.btn_permisos1);

        btn_permisosADMIN = findViewById(R.id.btn_permisos2);
        NumeroEDIT = findViewById(R.id.Numeroeditext);

        btnguardar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (PermisosOK==true){
                            guardar_numero();

                        } else{
                        }
                    }

        });
        btnpermisos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "::"+PermisosOK, Toast.LENGTH_SHORT).show();
                permisoalertas();
                XIAOMI();

            }

        });

        if (Build.VERSION.SDK_INT <33) {

           requestPermissions(permisos2, REQUEST_CODE_PERMISOS);
        } else {

        }

        btn_permisosADMIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermisosPantalla();
                permisoSonido(MainActivity.this.getApplicationContext());

            }

        });

    }

    public void guardar_numero(){
        String numeroEDITst = NumeroEDIT.getText().toString().trim();
        if (esNumero(numeroEDITst) && !numeroEDITst.isEmpty()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            sharedPreferences.edit().putString("telefono", numeroEDITst).apply();
            Intent INTENTO = new Intent(MainActivity.this, ServicioSegundoPlano.class);
            startService(INTENTO);
            moveTaskToBack(true);

        } else {
            Toast.makeText(MainActivity.this, "Error de Numero", Toast.LENGTH_SHORT).show();
        }

    }

    public boolean esNumero(String texto) {
        try {
            Long.parseLong(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void send_sms(){
        String telefono = NumeroEDIT.getText().toString().trim();
        String message = "Hola, no puedo contestar mira mi ubicacion: ";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(telefono, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS enviado.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error al enviar el SMS.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISOS) {
            boolean todosConcedidos = true;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    todosConcedidos = false;

                    if (!shouldShowRequestPermissionRationale(permissions[i])) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Permisos denegados");
                        builder.setMessage("Se requieren los permisos para utilizar todas las funciones de la aplicación. Por favor, habilite los permisos en la configuración de la aplicación.");
                        builder.setPositiveButton("Abrir configuración", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Cancelar", null);
                        builder.show();
                    }
                }
            }

            if (todosConcedidos) {
                PermisosOK = true;
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, "Al menos un permiso fue denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressWarnings("deprecation")
    private void permisoalertas() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, PERMISSION_REQUEST_CODE_ACTION);
        } else {

        }
    }
    public void XIAOMI() {

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsTabActivity"));

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

        if (!activities.isEmpty()) {
            startActivity(intent);
        } else {

        }


    }

    public void PermisosPantalla() {
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, AdministradorDispositivos.class);

        if (policyManager.isAdminActive(adminComponent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Toast.makeText(this, "permiso pantalla ok", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Explanation about why this permission is needed");
            startActivity(intent);
        }
    }

    public void permisoSonido(Context context){

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(intent);
        }else{
            Toast.makeText(context, "Permiso sonido ok", Toast.LENGTH_SHORT).show();
        }
    }


}

