package com.example.app_ihc;

import static android.Manifest.permission.RECORD_AUDIO;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity /*implements TextToSpeech.OnInitListener*/{
    /*private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private TextView textView;

    private TextToSpeech tts;*/
    Intent mServiceIntent;
    private ShakeService mYourService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*tts = new TextToSpeech(this, this);
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        textView = findViewById(R.id.textView);
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                System.out.println("Ready for speach");

            }

            @Override
            public void onBeginningOfSpeech() {
                System.out.println("Reginning of speach");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                System.out.println("End of speach");



            }

            @Override
            public void onError(int i) {
                System.out.println("Error:"+i);
                speechRecognizer.stopListening();

                speechRecognizer.startListening(intentRecognizer);
            }

            public String quitaDiacriticos(String s) {
                s = Normalizer.normalize(s, Normalizer.Form.NFD);
                s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                return s;
            }

            @Override
            public void onResults(Bundle bundle) {
                System.out.println("write the results for speak");
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String string = "";
                if(matches!=null){
                    string = matches.get(0);
                    String comando1_1="hola";
                    String comando1_2="hello how are you";

                    String comando2="adios";
                    string=quitaDiacriticos(string).toLowerCase(Locale.ROOT);


                    if (string.contains(comando1_1)){
                        speak("Hola");
                        System.out.println("coincidencia de texto");
                    }
                    if (string.contains(comando1_2)){
                        speak("Hi aim god");
                        System.out.println("coincidencia de texto");
                    }
                    if (string.contains(comando2)){
                        speak("Hasta luego");
                        System.out.println("coincidencia de texto");
                        cerrarApp();
                    }
                    textView.setText(string);

                }
                speechRecognizer.stopListening();

                speechRecognizer.startListening(intentRecognizer);

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        speechRecognizer.startListening(intentRecognizer);

*/


        mYourService = new ShakeService();
        mServiceIntent = new Intent(this, mYourService.getClass());
        if (!isMyServiceRunning(mYourService.getClass())) {
            startService(mServiceIntent);
        }

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                1);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    /*@Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.ITALIAN);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported");
            } else {
                speak("Escuchando");
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }


    public void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    };*/


    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }


}