package com.example.froggy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class finishView extends AppCompatActivity {
    private String userName;
    private String point;
    private TextView nameView;
    private TextView pointView;
    private Button playAgian;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TableLayout table;
    private double lan;
    private double lat;
    private LinearLayout fragmentlaout;
    private GoogleMap mMap;
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTable();
        setContentView(R.layout.activity_finish_view);
        Bundle bundle = getIntent().getExtras();
        userName = bundle.get("userName").toString();
        point = bundle.get("userPoints").toString();
        table = findViewById(R.id.table);
        nameView = findViewById(R.id.nameId);
        pointView = findViewById(R.id.finalScore);
        playAgian = findViewById(R.id.playAgainID);
        nameView.setText(userName);
        pointView.setText(point);
        fragmentlaout = findViewById(R.id.fragment);
        mapFragment = MapFragment.newInstance();
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.mapFrag, mapFragment);
        transaction.commit();

        playAgian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }


    private void initTable() {

        CollectionReference collectionReference = db.collection("usersScores");
        Query query;
        query = collectionReference.orderBy("userPoints", Query.Direction.DESCENDING).limit(10);
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        TableRow tableRow = new TableRow(getApplicationContext());

                        TextView name = new TextView(getApplicationContext());
                        name.setPadding(25, 10, 10, 0);
                        name.setTextSize(20);
                        name.setTextColor(Color.WHITE);
                        name.setText(documentSnapshot.get("userName").toString());
                        TextView score = new TextView(getApplicationContext());
                        score.setText(documentSnapshot.get("userPoints").toString());
                        score.setPadding(25, 10, 10, 0);
                        score.setTextSize(20);
                        score.setTextColor(Color.WHITE);

                        tableRow.addView(name);
                        tableRow.addView(score);
                        tableRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                lan = documentSnapshot.getDouble("userLocationLan");
                                lat = documentSnapshot.getDouble("userLocationLat");

                                mapFragment.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap googleMap) {
                                        setGoogleMap(googleMap, lat, lan);
                                    }
                                });

                                fragmentlaout.setVisibility(View.VISIBLE);

                            }
                        });
                        table.addView(tableRow);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(finishView.this, "Error table loading", Toast.LENGTH_SHORT).show();
                        Log.w("Tag", "Error table loading", e);
                    }
                });


    }

    public void setGoogleMap(GoogleMap googleMap, double lat, double lon) {
        this.mMap = googleMap;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 18f));
        googleMap.addMarker(new MarkerOptions()
                .draggable(true)
                .position(new LatLng(lat, lon)));

    }

}
