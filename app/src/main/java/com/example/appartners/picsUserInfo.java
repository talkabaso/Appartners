package com.example.appartners;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class picsUserInfo extends AppCompatActivity {


    private DatabaseReference mDatabasePartner;
    private Bundle bundle;
    private String userEmail;
    private Partner currentPartner;

    private String imgUrl;

    private TextView mNameText, mGenderText, mAgeText, mCityText, mEmailText, mTellAboutText;
    private ImageView mImage_View;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pics_user_info);

        mDatabasePartner = FirebaseDatabase.getInstance().getReference("Partner");

        mNameText = findViewById( R.id.nameText );
        mGenderText = findViewById( R.id.genderText );
        mAgeText = findViewById( R.id.ageText );
        mCityText = findViewById( R.id.cityText );
        mEmailText = findViewById( R.id.emailText );
        mTellAboutText = findViewById( R.id.tellAboutText );
        mImage_View = findViewById( R.id.image_view );

        bundle = getIntent().getExtras();
        userEmail = bundle.getString("userEmail"); //  the email of the User

        Query query= mDatabasePartner.orderByChild("email").equalTo(userEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    currentPartner = data.getValue(Partner.class);

                    mNameText.setText( "Name: " + currentPartner.getName() );
                    mGenderText.setText( "Gender: " + currentPartner.getGender() );
                    mAgeText.setText( "Age: " +currentPartner.getAge() );
                    mCityText.setText( "City: " +currentPartner.getCity() );
                    mEmailText.setText( "Email: " +currentPartner.getEmail() );
                    mTellAboutText.setText( "about myself :\n" +currentPartner.getTellAbout() );

                    mDatabasePartner.child(currentPartner.getId()).setValue(currentPartner);

                }

                imgUrl=currentPartner.getImgUrl();
                if(imgUrl.equals(""))
                    imgUrl="https://firebasestorage.googleapis.com/v0/b/appartners-2735b.appspot.com/o/uploads%2FnoPhoto.png?alt=media&token=8fb5f8d9-a80f-4360-843d-7aae13546d13";

                    Picasso.with(picsUserInfo.this)
                            .load(imgUrl)
                            .into(mImage_View);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
