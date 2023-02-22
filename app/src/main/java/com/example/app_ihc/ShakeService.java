package com.example.app_ihc;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.os.Build.*;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

    public class ShakeService extends Service implements SensorEventListener, TextToSpeech.OnInitListener, LocationListener {
        public int counter = 0;


        public static int n = 0;
        private SensorManager mSensorManager;
        private Sensor mAccelerometer;
        private static final String TAG = "BackgroundSoundService";


        private SpeechRecognizer speechRecognizer;
        private Intent intentRecognizer;
        private TextToSpeech tts;


        protected LocationManager locationManager;
        protected LocationListener locationListener;
        FusedLocationProviderClient fusedLocationProviderClient;
        Context this_context = this;

        PlacesClient placesClient;
        List placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
        private static final int REQUEST_CODE = 200;
        LatLng latLng;
        Double myLatitude;
        Double myLongitude;
        String address;
        private String placeId;
        private List placeLikelihoods;
        // Used for selecting the Current Place.
        private static final int M_MAX_ENTRIES = 5;
        private String[] mLikelyPlaceNames;
        private String[] mLikelyPlaceAddresses;
        private String[] mLikelyPlaceAttributions;
        private LatLng[] mLikelyPlaceLatLngs;

        List<Result> results;
        Result placeFocused;
        boolean interactFunctions = false;
        int sizePlaces = 7;
        int optionFocused;

        Location loc;


        postUbication send=new postUbication();


        @Override
        public void onCreate() {
            super.onCreate();




            locationListener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    try {

                        myLatitude = location.getLatitude();
                        myLongitude = location.getLongitude();
                        System.out.println(location);
                        System.out.println(send.enviar(myLatitude,myLongitude,this_context));

                        if (interactFunctions!=false) {
                            int distanceGeo = (int) Math.round(distance(myLatitude, myLongitude,placeFocused.getGeometry().getLocation().getLat(),placeFocused.getGeometry().getLocation().getLng()));

                            System.out.println("con distancia de :" + distanceGeo);
                            String toSpeach = "opcion " + optionFocused + ":" + placeFocused.getName();
                            speak(toSpeach+", estas a "+distanceGeo+" metros" );
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        //speak("estas a "+distanceGeo+" metros");

                    } catch (Exception e) {

                        System.out.println(e);
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {


                }

                @Override
                public void onProviderEnabled(String s) {


                }

                @Override
                public void onProviderDisabled(String s) {


                }
            };


            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            //*******************************************************************
            //locationManager.removeUpdates(locationListener);
            //locationListener=null;
            //*******************************************************************

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, locationListener, Looper.getMainLooper());
            //locationManager.removeUpdates(locationListener);



            if (VERSION.SDK_INT > VERSION_CODES.O) {
                startMyOwnForeground();
            } else
                startForeground(1, new Notification());


            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            tts = new TextToSpeech(this, this);
            //ActivityCompat.requestPermissions(this_context, new String[]{RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

            intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //intentRecognizer.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
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
                    speechRecognizer.cancel();

                    System.out.println("Error:" + i);

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
                    if (matches != null) {
                        string = matches.get(0);
                        String comando0_0="lista de comandos";
                        String comando1_1 = "hola";
                        String comando1_2 = "hello how are you";
                        String comando1_3 = "abrir aplicacion";
                        String comando1_4 = "detener servicio";
                        String comando2_1 = "dime mi ubicacion";
                        String comando2_2 = "lugares cerca de mi";
                        String comando2_4 = "opcion";
                        String comando2_5 = "cancelar navegacion";



                        String comando2 = "adios";
                        string = quitaDiacriticos(string).toLowerCase(Locale.ROOT);


                        if (string.contains(comando0_0)) {
                            String s="Lista de comandos. \n comando 1: saludo. \n comando 2: abrir aplicacion. \n comando 3: dime mi ubicacion." +
                                    "comando 4: lugares cerca de mi. \n comando 5: opcion de lugares cerca de mi a elegir.  \n comando 6 : cancelar ubicacion.";
                            speak(s);

                        }
                        if (string.contains(comando1_1)) {
                                speak("Hola");
                                System.out.println("coincidencia de texto");
                        }
                        if (string.contains(comando1_2)) {
                                speak("Hi aim god");
                                System.out.println("coincidencia de texto 2");
                        }

                        if (string.contains(comando1_3)) {

                                speak("Abriendo aplicacion");
                                System.out.println("coincidencia de texto");
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_MAIN);
                                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ComponentName cn = new ComponentName(this_context, MainActivity.class);
                                    intent.setComponent(cn);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    System.out.println(e);
                                }


                        }

                        if (string.contains(comando2_1)) {


                                if (ActivityCompat.checkSelfPermission(this_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(
                                        new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(Location location) {
                                                if (location != null) {
                                                    Geocoder geocoder = new Geocoder(this_context, Locale.getDefault());
                                                    try {
                                                        myLatitude=location.getLatitude();
                                                        myLongitude=location.getLongitude();
                                                        List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                                        System.out.println(addressList.get(0));
                                                        speak("te encuentras en:  "+ addressList.get(0).getSubAdminArea()+"   " +addressList.get(0).getAddressLine(0));
                                                    } catch (IOException e) {
                                                        System.out.println(e);
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                );
                                System.out.println("entra aqui");
                                speak("ubicacion actual 111111");
                                System.out.println("coincidencia de texto 2");


                        }
                        if (string.contains(comando2_2)) {
                                getPlacesNerlyMe();
                        }

                        if (string.contains(comando2_4)) {
                            System.out.println(string);
                            if (string.contains("1".toString()) && 0 < sizePlaces) {
                                initNavigation(0);
                            } else {
                                if (string.contains("2") && 1 < sizePlaces) {
                                    initNavigation(1);
                                } else {
                                    if (string.contains("3") && 2 < sizePlaces) {
                                        initNavigation(2);

                                    } else {
                                        if (string.contains("4") && 3 < sizePlaces) {
                                            initNavigation(3);

                                        } else {
                                            if (string.contains("5") && 4 < sizePlaces) {
                                                initNavigation(4);

                                            } else {
                                                if (string.contains("6") && 5 < sizePlaces) {
                                                    initNavigation(5);

                                                } else {
                                                    if (string.contains("7") && 6 < sizePlaces) {
                                                        initNavigation(6);

                                                    } else {
                                                        if (string.contains("8") && 7 < sizePlaces) {
                                                            initNavigation(7);

                                                        } else {
                                                            speak("no se encontro coincidencia en las opciones");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                        }

                        if (string.contains(comando2_5)) {
                                interactFunctions=false;
                        }

                    }
                    //speechRecognizer.stopListening();

                }

                @Override
                public void onPartialResults(Bundle bundle) {
                    ArrayList data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String word = (String) data.get(data.size() - 1);
                    if (word.contains("fin")) {

                        speechRecognizer.stopListening();
                    }
                    System.out.println(word);
                    Log.i("TEST", "partial_results: " + word);
                }

                @Override
                public void onEvent(int i, Bundle bundle) {

                }
            });


            // Inicializar lugares.
            //Places.initialize(getApplicationContext(), "AIzaSyDhgSSTzKOzLw-maUH2Q6DxF_7EBAQkdU0");

            // Crear una nueva instancia de cliente de Places.
            //placesClient = Places.createClient(this);

        }


   /* private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }*/

        public void speak(String text) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }

        ;


        @RequiresApi(VERSION_CODES.O)
        private void startMyOwnForeground() {
            int c = 1;

            String NOTIFICATION_CHANNEL_ID = "example.permanence";
            String channelName = "Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);


            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            System.out.println("evento listener" + c);


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }

        @Override
        public void onSensorChanged(@NonNull SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float accelerationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
            if (accelerationSquareRoot >= 15) {
                n = n + 1;
                System.out.println("P");

                if (n == 3) {
                    speechRecognizer.stopListening();

                    System.out.println("AAAAAAAAAAAAAAAAAAAABBBBBBBBBAAAAAAA     " + n);

                    speechRecognizer.startListening(intentRecognizer);

                    n = 0;
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }


        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, flags, startId);
            startTimer();
            return START_STICKY;
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            stoptimertask();

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, Restarter.class);
            this.sendBroadcast(broadcastIntent);
        }

        /*public void stopService() {
            super.onDestroy();
            stoptimertask();
        }*/


        private Timer timer;
        private TimerTask timerTask;

        public void startTimer() {
            timer = new Timer();
            timerTask = new TimerTask() {
                public void run() {
                    Log.i("Count", "=========  " + (counter++));
                }
            };
            timer.schedule(timerTask, 1000, 1000); //
        }

        public void stoptimertask() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onInit(int status) {
            Locale locale = new Locale("es", "ES");
            tts.setLanguage(locale);
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(locale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported");
                } else {
                    speak("para ver comandos agite el celular y diga... lista de comandos");
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
            speechRecognizer.stopListening();
        }

        @Override
        public void onLocationChanged(Location location) {
            System.out.println(location.toString());
            speak("localizacion cambiada");
            System.out.println("entra al changed location");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Latitude", "disable");
            speak("localizacion desactivada");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Latitude", "enable");
            speak("localizacion activada");

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Latitude", "status");
        }

        private void getCurrentPlace() {

            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.builder(placeFields).build();

            // Call findCurrentPlace and handle the response (first check that the user has granted permission).
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                placesClient.findCurrentPlace(request).addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {

                        if (task.isSuccessful()) {


                            FindCurrentPlaceResponse response = task.getResult();
                            placeLikelihoods = new ArrayList<>();
                            placeLikelihoods.addAll(response.getPlaceLikelihoods());
                            System.out.println(response);
                            int count;
                            if (response.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                                count = response.getPlaceLikelihoods().size();
                            } else {
                                count = M_MAX_ENTRIES;
                            }

                            int i = 0;
                            mLikelyPlaceNames = new String[count];
                            mLikelyPlaceAddresses = new String[count];
                            mLikelyPlaceAttributions = new String[count];
                            mLikelyPlaceLatLngs = new LatLng[count];

                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                Place currPlace = placeLikelihood.getPlace();
                                mLikelyPlaceNames[i] = currPlace.getName();
                                mLikelyPlaceAddresses[i] = currPlace.getAddress();
                                mLikelyPlaceAttributions[i] = (currPlace.getAttributions() == null) ?
                                        null : TextUtils.join(" ", currPlace.getAttributions());
                                mLikelyPlaceLatLngs[i] = currPlace.getLatLng();

                                String currLatLng = (mLikelyPlaceLatLngs[i] == null) ?
                                        "" : mLikelyPlaceLatLngs[i].toString();

                                Log.i(TAG, String.format("Place " + currPlace.getName()
                                        + " has likelihood: " + placeLikelihood.getLikelihood()
                                        + " at " + currLatLng));

                                i++;
                                if (i > (count - 1)) {
                                    break;
                                }
                            }
                            //response.getPlaceLikelihoods() will return list of PlaceLikelihood
                            //we need to create a custom comparator to sort list by likelihoods
                        /*Collections.sort(placeLikelihoods, new Comparator() {
                            @Override
                            public int compare(PlaceLikelihood placeLikelihood, PlaceLikelihood t1) {
                                return new Double(placeLikelihood.getLikelihood()).compareTo(t1.getLikelihood());
                            }
                        });*/

                            //After sort ,it will order by ascending , we just reverse it to get first item as nearest place
                            //Collections.reverse(placeLikelihoods);
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(this_context, "Place not found: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

        }

        public void getPlacesNerlyMe() {
            String key = "AIzaSyDhgSSTzKOzLw-maUH2Q6DxF_7EBAQkdU0";
            String currentLocation = myLatitude + "," + myLongitude;
            int radius = 50;
            GoogleMapAPI googleMapAPI = APIClient.getClient().create(GoogleMapAPI.class);
            googleMapAPI.getNearBy(currentLocation, radius, key).enqueue(new Callback<PlacesResults>() {
                @Override
                public void onResponse(Call<PlacesResults> call, Response<PlacesResults> response) {
                    if (response.isSuccessful()) {
                        results = response.body().getResults();
                        speak("Hay " + results.size() + " lugares cerca");
                        sizePlaces = results.size();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        listaDeLugares();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<PlacesResults> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            System.out.println("print in method of utils" + results);
        }

        private Double distance(double lat1, double lon1, double lat2, double lon2) {
            double theta = lon1 - lon2;
            double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;

            dist = dist * 1.609344;

            return Math.floor(dist * 1000);
        }

        /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
        /*::  This function converts decimal degrees to radians             :*/
        /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
        private double deg2rad(double deg) {
            return (deg * Math.PI / 180.0);
        }

        /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
        /*::  This function converts radians to decimal degrees             :*/
        /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
        private double rad2deg(double rad) {
            return (rad * 180.0 / Math.PI);
        }

        private void listaDeLugares() {
            int i = 1;
            for (Result r : results) {

                System.out.println("result=" + r);
                int distanceGeo = (int) Math.round(distance(myLatitude, myLongitude, r.getGeometry().getLocation().getLat(), r.getGeometry().getLocation().getLng()));
                System.out.println("con distancia de :" + distanceGeo);
                String toSpeach = "opcion " + i + ":" + r.getName() + " a " + distanceGeo + "metros";
                speak(toSpeach);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i = i + 1;
            }
        }



        public void initNavigation(int a) {
            placeFocused = results.get(a);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            interactFunctions=true;
            int distanceGeo = (int) Math.round(distance(myLatitude, myLongitude, placeFocused.getGeometry().getLocation().getLat(), placeFocused.getGeometry().getLocation().getLng()));
            System.out.println("con distancia de :" + distanceGeo);
            optionFocused=a+1;
            String toSpeach = "opcion " + (a+1) + ":" + placeFocused.getName() + " a " + distanceGeo + "metros";
            speak(toSpeach);
            speak("estas a "+distanceGeo+" metros");



            //while (interactFunctions==true){

            //}

        }




    }


/*import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.Nullable;

public class ShakeService extends Service {

    // declaring object of MediaPlayer
    private MediaPlayer player;



    @Override

    // execution of service will start
    // on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {

        // creating a media player which
        // will play the audio of Default
        // ringtone in android device
        player = MediaPlayer.create( this, Settings.System.DEFAULT_RINGTONE_URI );

        // providing the boolean
        // value as true to play
        // the audio on loop
        player.setLooping( true );

        // starting the process
        player.start();

        // returns the status
        // of the program
        return START_STICKY;
    }

    @Override

    // execution of the service will
    // stop on calling this method
    public void onDestroy() {
        super.onDestroy();

        // stopping the process
        player.stop();
        Intent intent = new Intent("com.android.techtrainner");
        sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}*/