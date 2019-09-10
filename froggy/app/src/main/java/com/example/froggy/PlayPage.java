package com.example.froggy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PlayPage extends AppCompatActivity implements View.OnClickListener, LocationListener {
    private TextView points;
    private TextView pointsMiss;
    private TextView editTimer;
    private String name;
    private Button[] allButtons;
    private GridLayout gridLayout;
    private int random2;
    private CountDownTimer countDown;
    private ImageView heartView1;
    private ImageView heartView2;
    private ImageView heartView3;
    private LocationManager locationManager;
    private Location location = null;
    private Intent intent;
    private double lat = 0;
    private double lan = 0;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_page);
        Bundle bundle = getIntent().getExtras();
        name = bundle.get("userName").toString();
        allButtons = new Button[9];
        editTimer = findViewById(R.id.timerId);
        pointsMiss = findViewById(R.id.missPointsId);
        points = findViewById(R.id.pointsId);
        gridLayout = findViewById(R.id.gridId);

        heartView1 = findViewById(R.id.heart1);
        heartView2 = findViewById(R.id.heart2);
        heartView3 = findViewById(R.id.heart3);


        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            allButtons[i] = (Button) gridLayout.getChildAt(i);
            allButtons[i].setOnClickListener(this);
        }
        final Handler handler = new Handler();

                countDown = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                editTimer.setText("Seconds remaining: " + millisUntilFinished / 1000);
                locationSet();
                final int random = (int) ((Math.random() * 10) - 1);

                final int bombRandom = (int) ((Math.random() * 10) - 1);
                if(bombRandom%2 ==0&&(millisUntilFinished / 1000)%2==1)
                {
                    if (allButtons[bombRandom].getBackground().getConstantState() == getResources().getDrawable(R.drawable.leaf).getConstantState()) {
                        allButtons[bombRandom].setBackgroundResource(R.drawable.bom);
                    }

                }
                if (allButtons[random].getBackground().getConstantState() != getResources().getDrawable(R.drawable.frog).getConstantState()) {
                    allButtons[random].setBackgroundResource(R.drawable.frog);
                    random2 = (int) (Math.random() * 2) + 2;

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (allButtons[random].getBackground().getConstantState() == getResources().getDrawable(R.drawable.frog).getConstantState()) {
                                allButtons[random].setBackgroundResource(R.drawable.leaf);
                            }
                            if (allButtons[bombRandom].getBackground().getConstantState() == getResources().getDrawable(R.drawable.bom).getConstantState()) {
                                allButtons[bombRandom].setBackgroundResource(R.drawable.leaf);
                            }

                        }
                    }, 1000 * random2);
                }

            }

            public void onFinish() {
                editTimer.setText("Done");
                intent = new Intent(getApplicationContext(), finishView.class);
                intent.putExtra("userName", name);
                intent.putExtra("userPoints", points.getText().toString());
                intent.putExtra("userPointsMiss", pointsMiss.getText().toString());
                intent.putExtra("userLocationLat", lat);
                intent.putExtra("userLocationLan", lan);


                startActivity(intent);
                finish();

            }
        }.start();

    }
private void locationSet(){

    locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        String fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        String coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
        if (getApplicationContext().checkSelfPermission(fineLocationPermission) != PackageManager.PERMISSION_GRANTED ||
                getApplicationContext().checkSelfPermission(coarseLocationPermission) != PackageManager.PERMISSION_GRANTED) {
            // The user blocked the location services of THIS app / not yet approved
        }
    }
    if (location == null) {
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
    float metersToUpdate = 1;
    long intervalMilliseconds = 1000;
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, intervalMilliseconds, metersToUpdate,this);


}
    public void finishGame(String message) {
        countDown.cancel();
        editTimer.setText(message);

        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("userName", name);
        user.put("userPoints", points.getText().toString());
        user.put("userPointsMiss", pointsMiss.getText().toString());
        user.put("userLocationLat", location.getLatitude());
        user.put("userLocationLan", location.getLongitude());

        //write to the DB the user
        db.collection("usersScores")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("tag", "DocumentSnapshot added with ID: " + documentReference.getId());


                        intent = new Intent(getApplicationContext(), finishView.class);
                        intent.putExtra("userName", name);
                        intent.putExtra("userPoints", points.getText().toString());

                        startActivity(intent);
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PlayPage.this, "error writing to DB", Toast.LENGTH_SHORT).show();
                        Log.w("Tag", "Error adding document", e);
                    }
                });


    }

    @Override
    public void onClick(View view) {
        Button b = findViewById(view.getId());
        int currentPoints = Integer.parseInt(points.getText().toString().split(" ")[1]);
        int currentMiss = Integer.parseInt(pointsMiss.getText().toString().split(" ")[1]);
        if (b.getBackground().getConstantState() == getResources().getDrawable(R.drawable.frog).getConstantState()) {
            {
                points.setText("Points: " + (currentPoints + 1));
                startAnimation();
            }
            b.setBackgroundResource(R.drawable.leaf);

        } else if (b.getBackground().getConstantState() == getResources().getDrawable(R.drawable.leaf).getConstantState()||b.getBackground().getConstantState() == getResources().getDrawable(R.drawable.bom).getConstantState()) {


            currentMiss++;
            if (currentMiss == 1) {
                animationHeart(heartView1);

                heartView1.setVisibility(View.INVISIBLE);
            }

            if (currentMiss == 2) {
                animationHeart(heartView2);
                heartView2.setVisibility(View.INVISIBLE);
            }
            if (currentMiss == 3)
            {
                animationHeart(heartView3);
                heartView3.setVisibility(View.INVISIBLE);

            }

            pointsMiss.setText("Miss: " + currentMiss);
            startAnimationMiss();
            b.setBackgroundResource(R.drawable.leaf);


            if (currentMiss == 3) {
                finishGame("You Lose!");

            }
        }
    }

    @Override
    public void onLocationChanged(Location currentlocation) {
        location= currentlocation;
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
public void animationHeart(View b)
{
    final float FREQ = 3f;
    final float DECAY = 2f;
    TimeInterpolator decayingSineWave = new TimeInterpolator() {
        @Override
        public float getInterpolation(float input) {
            double raw = Math.sin(FREQ * input * 2 * Math.PI);
            return (float)(raw * Math.exp(-input * DECAY));
        }
    };

    b.animate()
            .yBy(-100)
            .xBy(-100)
            .setInterpolator(decayingSineWave)
            .setDuration(400)
            .start();

}

    public void startAnimation(){
    Animation ani =AnimationUtils.loadAnimation(this,R.anim.anim);
    points.startAnimation(ani);

    }


    public void startAnimationMiss(){
        Animation ani =AnimationUtils.loadAnimation(this,R.anim.missani);
        ani.setDuration(2000);
        pointsMiss.startAnimation(ani);

    }

}
