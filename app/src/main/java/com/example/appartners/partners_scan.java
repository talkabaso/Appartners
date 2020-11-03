package com.example.appartners;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class partners_scan extends AppCompatActivity {


    String currImg;

    private DatabaseReference mDatabaseApartment;
    private DatabaseReference mDatabasePartner;

    private FirebaseAuth fAuto;

    private Apartment currentUser;

    private String myPhoto;
    private ImageView imgView;
    private ImageButton mHeart, mLeft, mRight;
    private TextView mNameText, mAgeText;
    private TextView mZeroItemsText;

    private EditText mMaxAge,mGender;
    private ImageButton mSearch;
    //private boolean isFiltered;
    private DataSnapshot allPartnerList;


    private String imgUrl;


    private Partner currentPartner;
    private ArrayList<Partner> allPartners;
    private ArrayList<Partner> copyAllPartners;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partners_scan);

        imgView = findViewById(R.id.image_view);
        mHeart = findViewById(R.id.heartBtn);
        mLeft = findViewById( R.id.leftBtn );
        mRight = findViewById( R.id.rightBtn );
        mZeroItemsText=findViewById(R.id.zeroItemsText);
        mZeroItemsText.setVisibility(View.INVISIBLE);

        mNameText = findViewById( R.id.nameText );
        mAgeText = findViewById( R.id.ageText );

        mMaxAge =findViewById(R.id.maxPriceFilter);
        mGender=findViewById(R.id.genderText);
        mSearch=findViewById(R.id.searchButton);



        fAuto = FirebaseAuth.getInstance();
        mDatabaseApartment = FirebaseDatabase.getInstance().getReference("Apartment");
        mDatabasePartner = FirebaseDatabase.getInstance().getReference("Partner");
        allPartners = new ArrayList<Partner>();

        mDatabasePartner.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allPartnerList=dataSnapshot;
                Partner partnerUser=null;
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    partnerUser=data.getValue(Partner.class);
                    allPartners.add(partnerUser);

                }
                copyAllPartners = new ArrayList<>(allPartners);

                if(allPartners.size()>0){

                    index=0;
                    show();

                }else{

                    mHeart.setVisibility(View.INVISIBLE);
                    mLeft.setVisibility(View.INVISIBLE);
                    mRight.setVisibility(View.INVISIBLE);
                    mAgeText.setVisibility(View.INVISIBLE);
                    mNameText.setVisibility(View.INVISIBLE);
                    mZeroItemsText.setVisibility(View.VISIBLE);
                    mGender.setVisibility(View.INVISIBLE);
                    mMaxAge.setVisibility(View.INVISIBLE);
                    mZeroItemsText.setText(" no apartments to show");

                    Picasso.with(partners_scan.this)
                            .load("https://firebasestorage.googleapis.com/v0/b/appartners-2735b.appspot.com/o/uploads%2FnoMore.jpg?alt=media&token=1a4d7a69-d6c7-43a0-b888-1e810925ca0c")
                            .into(imgView);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int maxAgeInt=-1;
                String gender=mGender.getText().toString().trim();
                String maxAgeText= mMaxAge.getText().toString().trim();
                if(!maxAgeText.equals("")){ // only for convert to Integer

                    maxAgeInt=Integer.parseInt(maxAgeText);
                }

                if(!gender.equals("") || maxAgeInt!=-1) { // we need to filter by at least one parameter

                    allPartners.clear();
                    for (DataSnapshot data : allPartnerList.getChildren()) {

                        Partner temp = data.getValue(Partner.class);

                        if (!gender.equals("") && maxAgeInt == -1) { // only gender

                            if (temp.getGender().toUpperCase().equals(gender.toUpperCase())) {

                                allPartners.add(temp);
                            }

                        } else if (gender.equals("") && maxAgeInt != -1) { // only age

                            if (temp.getAge() <= maxAgeInt) {

                                allPartners.add(temp);
                            }

                        } else if (!gender.equals("") && maxAgeInt != -1) { // filter by age and gender

                            if (temp.getGender().toUpperCase().equals(gender.toUpperCase()) && temp.getAge() <= maxAgeInt) {
                                allPartners.add(temp);
                            }
                        }

                    }
                    if(allPartners.size()>0){

                        index=0;
                        show();

                    }

                }
                else { // the fields are empty


                    allPartners.clear();
                    allPartners.addAll(copyAllPartners);
                    index = 0;
                    Toast.makeText(partners_scan.this, "show all results", Toast.LENGTH_LONG).show();
                    show();
                }

                if(allPartners.size()==0) { // if we filtered but there is no any result

                    allPartners.addAll(copyAllPartners);
                    index=0;
                    Toast.makeText(partners_scan.this, "There is no result for your search", Toast.LENGTH_LONG).show();
                    show();

                }

                mGender.setText("");
                mMaxAge.setText("");
            }
        });



        Query currentUserQuery=mDatabaseApartment.orderByChild("email").equalTo(fAuto.getCurrentUser().getEmail());

        currentUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    currentUser = data.getValue(Apartment.class);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mHeart.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if(!cheackContains(currentPartner.getEmail())){

                    Toast.makeText(partners_scan.this, currentPartner.getName()+" added to your favorites", Toast.LENGTH_SHORT).show();
                    UserImage ui = new UserImage(currentPartner,currentPartner.getImgUrl());
                    currentUser.addFav(ui);
                    mDatabaseApartment.child(currentUser.getId()).setValue(currentUser);
                }else{

                    Toast.makeText(partners_scan.this, currentPartner.getName()+" already in your favorites", Toast.LENGTH_SHORT).show();

                }

            }
        }));


        // listener for right and left button if clicked right index++ and show the image of the allPartners.get(index)

        mRight.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                index++;
                if(index>= allPartners.size()){

                    index=0;

                }

                show();

            }

        }));


        mLeft.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                index--;
                if(index<0){

                    index=allPartners.size()-1;

                }

                show();
            }
        }));

        imgView.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                Toast.makeText(partners_scan.this, "img clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(partners_scan.this, picsUserInfo.class);

                intent.putExtra("userEmail", currentPartner.getEmail());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {

        return;
    }

    // menu code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.partners_scan_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {

            case R.id.personal_details_item:
                intent=new Intent(this,personal_details.class);
                intent.putExtra("searchingFor", "Searching partner");
                startActivity(intent);
                return true;

            case R.id.my_fav_item:
                intent=new Intent(this,favorites.class);
                intent.putExtra("searchingFor", "Searching partner");
                startActivity(intent);
                return true;

            case R.id.apartment_details_Item:
                intent=new Intent(this, apartment_details.class);
                startActivity(intent);
                return true;

            case R.id.my_uploads_items:
                intent=new Intent(this, myUploads.class);
                startActivity(intent);
                return true;

            case R.id.LogOutItem:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), login.class));
                finish();
                return true;

            case R.id.RemoveItem:

                new AlertDialog.Builder(this)
                        .setTitle("Delete User")
                        .setMessage("Are you sure you want to delete this User?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("want to delete","want to delete");
                                // Continue with delete operation
                                FirebaseAuth.getInstance().getCurrentUser().delete(); // remove from Authentication
                                int size=currentUser.getImagesUri().size();
                                for(int i=0; i<size; i++){ // remove all images from storage
                                    currImg = currentUser.getImg(i);
                                    if(!currImg.equals("")){

                                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(currImg);
                                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i("delete files",""+currImg);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                Log.i(" cant delete ",""+currImg);
                                            }
                                        });
                                    }
                                }
                                Toast.makeText( partners_scan.this, "User is Deleted", Toast.LENGTH_LONG ).show();
                                mDatabaseApartment.child(currentUser.getId()).setValue(null); // remove from Database
                                startActivity(new Intent(getApplicationContext(), login.class));
                                finish();

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public boolean cheackContains(String email){

        int size=currentUser.getMyFav().size();
        for(int i=0;i<size;i++){

            if(currentUser.getFav(i).getEmail().equals(email)){

                return true;
            }
        }
        return false;

    }


    public void show(){


        imgUrl=allPartners.get(index).getImgUrl();
        if(imgUrl.equals(""))
            imgUrl="https://firebasestorage.googleapis.com/v0/b/appartners-2735b.appspot.com/o/uploads%2FnoPhoto.png?alt=media&token=8fb5f8d9-a80f-4360-843d-7aae13546d13";

        Picasso.with(partners_scan.this) // show first User img
                .load(imgUrl)
                .into(imgView);
        currentPartner =allPartners.get(index);
        mNameText.setText( currentPartner.getName() );
        mAgeText.setText( "  Age: " + currentPartner.getAge() );
    }

}