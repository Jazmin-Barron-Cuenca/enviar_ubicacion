package com.example.enviar_ubicacion;

        import android.annotation.SuppressLint;
        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.app.admin.DevicePolicyManager;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.media.AudioManager;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Handler;
        import android.os.IBinder;
        import android.os.Looper;
        import android.preference.PreferenceManager;
        import android.telephony.PhoneStateListener;
        import android.telephony.SmsManager;
        import android.telephony.TelephonyManager;
        import android.util.Log;
        import android.widget.Toast;

        import androidx.annotation.RequiresApi;
        import androidx.core.app.ActivityCompat;
        import androidx.core.app.NotificationCompat;

public class ServicioSegundoPlano extends Service  {

    public static String telefono;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    private CallStateListener callStateListener;

    private ProveedorUbicacion locationProvider;
    public double latitudeG;
    public double longitudG;

    private AudioManager audioManager;




    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener = new CallStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        }
        locationProvider = new ProveedorUbicacion(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "servico iniciado", Toast.LENGTH_SHORT).show();




        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("LLamadasGPS")
                .setContentText("Rastreando llamada")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, builder.build());

        Location currentLocation = locationProvider.getCurrentLocation();
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            latitudeG = latitude;
            longitudG = longitude;
            Toast.makeText(this, "Latitud: " + latitude + ", Longitud: " + longitude, Toast.LENGTH_SHORT).show();
        }

        return START_STICKY;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Foreground Service Channel";
            String channelDescription = "Channel for foreground service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
        }

        locationProvider.stopLocationUpdates();
    }

    public  void llamada(String numeroAllamar){
        Toast.makeText(getApplicationContext(), "Llamando", Toast.LENGTH_LONG).show();
        String phoneNumber = numeroAllamar;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else {
            Toast.makeText(this, "No tiene permiso para llamar", Toast.LENGTH_SHORT).show();
        }
    }

    public void BloquerPantallaMetodo() {
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, AdministradorDispositivos.class);

        if (policyManager.isAdminActive(adminComponent)) {
                policyManager.lockNow();

        } else {
            Toast.makeText(this, "Error bloqueo", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRingerModeSilent() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        Toast.makeText(this, "silencio activado", Toast.LENGTH_SHORT).show();
    }

    public void Enviarsms(String incomingNumber){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String mensaje = "Hola, no puedo contestar mira mi ubicacion: ";
            mensaje += "https://maps.google.com/?q=" + latitudeG + "," + longitudG;
            smsManager.sendTextMessage(incomingNumber, null, mensaje, null, null);

            Toast.makeText(getApplicationContext(), "SMS enviado: "+incomingNumber, Toast.LENGTH_LONG).show();
            Log.d("smserror","SMS enviado: "+incomingNumber);

        }catch (Exception e){
            Log.d("smserror",e.toString());
            Toast.makeText(getApplicationContext(), "Numero no valido.", Toast.LENGTH_LONG).show();
        }

    }


    public class CallStateListener extends PhoneStateListener {

        Handler handler = new Handler(Looper.getMainLooper());
        private boolean incomingCall = false;
        private int ringCount = 0;

        @SuppressWarnings("deprecation")
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tel = sharedPreferences.getString("telefono", "");

        @SuppressWarnings("deprecation")
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Toast.makeText(ServicioSegundoPlano.this, "Numero entrante: "+incomingNumber, Toast.LENGTH_SHORT).show();

                    if (incomingNumber.equals(tel)) {
                        incomingCall = true;
                        ringCount = 1;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (incomingCall) {
                        incomingCall = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:

                    if (incomingCall && ringCount == 1) {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    setRingerModeSilent();
                                    Log.d("handlerrun","1. silenciado: "+incomingNumber);

                                    Toast.makeText(getApplicationContext(), "envio sms 4s", Toast.LENGTH_LONG).show();
                                    Thread.sleep(4000);
                                    Enviarsms(incomingNumber);
                                    Log.d("handlerrun","2. mensaje enviado: "+incomingNumber);

                                    Thread.sleep(4000);
                                    llamada(incomingNumber);
                                    Log.d("handlerrun","3. llamando: "+incomingNumber);

                                    Thread.sleep(3000);
                                    BloquerPantallaMetodo();
                                    Log.d("handlerrun","4. bloqueado: "+incomingNumber);

                                    Thread.sleep(3000);
                                    BloquerPantallaMetodo();
                                    Log.d("handlerrun","5. bloqueado: "+incomingNumber);

                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "Error de procesos", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                    incomingCall = false;
                    ringCount = 0;
                    break;
            }
        }
    }
}

