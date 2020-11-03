package com.example.appartners;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class favorites extends AppCompatActivity {

    private DatabaseReference mDatabasePartner;
    private DatabaseReference mDatabaseApartment;

    private FirebaseAuth fAuto;
    private Bundle bundle;
    private Query query;

    private boolean isPartner;
    private DataSnapshot myData;

   private Partner currentPartner;
   private Apartment currentApartment;

   private UserImage currentShow;
   private ArrayList<UserImage> allFav;
   private String searchingFor;

   private String imgUrl;

    private ImageView imgView;
    private ImageButton mRightButton;
    private ImageButton mLeftButton;
    private ImageButton mDelButton;
    private TextView mZeroItemsText;

    int index;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        imgView = findViewById(R.id.image_view);
        mRightButton = findViewById(R.id.rightBtn);
        mLeftButton = findViewById((R.id.leftBtn));
        mDelButton= findViewById(R.id.delButton2);
        mZeroItemsText=findViewById(R.id.zeroItemsText);
        mZeroItemsText.setVisibility(View.INVISIBLE);

        bundle = getIntent().getExtras();
        searchingFor = bundle.getString("searchingFor");

        fAuto = FirebaseAuth.getInstance();

        mDatabasePartner = FirebaseDatabase.getInstance().getReference("Partner");
        mDatabaseApartment = FirebaseDatabase.getInstance().getReference("Apartment");


        index=0;


        if(searchingFor.equals("Searching partner")){

            isPartner=false;
            query=mDatabaseApartment.orderByChild("email").equalTo(fAuto.getCurrentUser().getEmail());

        }else{

            isPartner=true;
            query=mDatabasePartner.orderByChild("email").equalTo(fAuto.getCurrentUser().getEmail());

        }


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    myData=data;
                    if(isPartner){

                        currentPartner=myData.getValue(Partner.class);
                        allFav=currentPartner.getMyFav();
                    }else{

                        currentApartment=myData.getValue(Apartment.class);
                        allFav=currentApartment.getMyFav();
                    }

                }
                if(allFav.size()>0){

                    index=0;
                   show();
                }else{

                    mRightButton.setVisibility(View.INVISIBLE);
                    mLeftButton.setVisibility(View.INVISIBLE);
                    mDelButton.setVisibility(View.INVISIBLE);
                    mZeroItemsText.setVisibility(View.VISIBLE);
                    mZeroItemsText.setText("no favorites for you");
                    Picasso.with(favorites.this) // show first User img
                            .load("https://aworshipersjournal.files.wordpress.com/2018/11/no-favorites.jpg?w=300&h=300")
                            .into(imgView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRightButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                index++;
                if(index>=allFav.size()){

                    index=0;

                }

              show();


            }
        }));

        mLeftButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                index--;
                if(index<0){

                    index=allFav.size()-1;

                }

               show();


            }
        }));

        imgView.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {

                Toast.makeText(favorites.this, "img clicked", Toast.LENGTH_SHORT).show();

                if(!isPartner){

                    Intent intent = new Intent(favorites.this, picsUserInfo.class);
                    intent.putExtra("userEmail", currentShow.getEmail());
                    startActivity(intent);
                } else{

                    Intent intent = new Intent(favorites.this, picsApartInfo.class);
                    intent.putExtra("userEmail", currentShow.getEmail());
                    startActivity(intent);
                }

            }
        });

        mDelButton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isPartner){

                    currentPartner.getMyFav().remove(currentShow);
                    allFav.remove(currentShow);
                    mDatabasePartner.child(currentPartner.getId()).setValue(currentPartner);

                }else{
                    currentApartment.getMyFav().remove(currentShow);
                    allFav.remove(currentShow);
                    mDatabaseApartment.child(currentApartment.getId()).setValue(currentApartment);

                }


                if(allFav.size()==0){

                    if(!isPartner){

                        startActivity(new Intent(getApplicationContext(), partners_scan.class));

                    }else{

                        startActivity(new Intent(getApplicationContext(), apartments_scan.class));
                    }
                }else{

                    index=0;
                    show();

                }
            }
        }));

    }

    @Override
    public void onBackPressed() {


        if(isPartner){
            startActivity(new Intent(this,apartments_scan.class));

        }else{
            startActivity(new Intent(this,partners_scan.class));
        }
        return;
    }

    public void show(){

        imgUrl=allFav.get(index).getImgUser();
        if(imgUrl.equals(""))
            imgUrl="https://firebasestorage.googleapis.com/v0/b/appartners-2735b.appspot.com/o/uploads%2FnoPhoto.png?alt=media&token=8fb5f8d9-a80f-4360-843d-7aae13546d13";

        Picasso.with(favorites.this) // show first User img
                .load(imgUrl)
                .into(imgView);
        currentShow =allFav.get(index);
    }

}
