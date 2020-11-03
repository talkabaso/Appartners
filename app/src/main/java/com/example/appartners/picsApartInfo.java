package com.example.appartners;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class picsApartInfo extends AppCompatActivity {

    private DatabaseReference mDatabaseApartment;
    private Bundle bundle;
    private String apartmentEmail;
    private String imgUrl;
    private Apartment currentApartment;
    private LinearLayout gallery;
    private LayoutInflater inflater;
    private TextView mEmail, mName, mPrice, mOccupants, mStreet, mRoomNums, mTypeOfRoom;

    private ArrayList<String> allImages;
    private ImageView mPicApart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pics_apart_info);

        gallery=findViewById(R.id.gallery);
        inflater=LayoutInflater.from(this);

        mEmail = findViewById( R.id.emailText );
        mName = findViewById( R.id.nameText );
        mPrice = findViewById( R.id.priceText );
        mOccupants = findViewById( R.id.occupantsText );
        mStreet = findViewById( R.id.streetText );
        mRoomNums = findViewById( R.id.roomsNumText );
        mTypeOfRoom = findViewById( R.id.typeOfRoomText );

        mDatabaseApartment = FirebaseDatabase.getInstance().getReference("Apartment");

         bundle = getIntent().getExtras();
         apartmentEmail = bundle.getString("userEmail"); //  the email of the Apartment's User

        Query query= mDatabaseApartment.orderByChild("email").equalTo(apartmentEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    currentApartment = data.getValue(Apartment.class);

                    mName.setText( "Name: " + currentApartment.getName() );
                    mEmail.setText( "Email: " + currentApartment.getEmail() );
                    mPrice.setText("Price: " + currentApartment.getPrice());
                    mOccupants.setText("Occupants: " + currentApartment.getOccupants());
                    mStreet.setText("Street: " + currentApartment.getStreet());
                    mRoomNums.setText("Number Rooms: " + currentApartment.getNumOfRooms());
                    mTypeOfRoom.setText("Type Of Room: " + currentApartment.getRoomType());

                    allImages = currentApartment.getImagesUri();

                }


                if (allImages.size() > 0) {

                    for(int i=0;i<allImages.size();i++){

                        imgUrl = allImages.get(i);
                        View view = inflater.inflate(R.layout.item, gallery, false);
                        mPicApart=view.findViewById(R.id.showPicApart);
                        Picasso.with(picsApartInfo.this).load(imgUrl).resize(295,185).into(mPicApart);
                        gallery.addView(view);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }
}
