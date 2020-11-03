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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

public class apartments_scan extends AppCompatActivity {

    private DatabaseReference mDatabaseApartment;
    private DatabaseReference mDatabasePartner;

    private FirebaseAuth fAuto;

    private ImageView imgView;
    private ImageButton mLeft, mRight,mHeart;
    private TextView mCityText;
    private TextView mZeroItemsText;
    private EditText mMaxPrice;
    private ImageButton mSearch;

    private ArrayList<Apartment> copyAllApartments;
    private DataSnapshot allApartmentList;

    private String imgUrl;

    private Partner currentUser;
    private Apartment currentApartment;
    private ArrayList<Apartment> allApartments;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartments_scan);

        imgView = findViewById(R.id.image_view);
        mHeart = findViewById( R.id.heartBtn );
        mLeft = findViewById( R.id.leftBtn );
        mRight = findViewById( R.id.rightBtn );
        mCityText = findViewById( R.id.cityText );
        mZeroItemsText=findViewById(R.id.zeroItemsText);
        mMaxPrice=findViewById(R.id.maxPriceFilter);
        mSearch=findViewById(R.id.searchButton);
        mZeroItemsText.setVisibility(View.INVISIBLE);

        fAuto = FirebaseAuth.getInstance();
        mDatabaseApartment = FirebaseDatabase.getInstance().getReference("Apartment");
        mDatabasePartner = FirebaseDatabase.getInstance().getReference("Partner");
        allApartments = new ArrayList<Apartment>();


        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this,android.R.layout.select_dialog_item,getResources().getStringArray(R.array.city_Array));

        final AutoCompleteTextView actv =  findViewById(R.id.autoCompleteFilter);
        actv.setThreshold(1); // Will start working from first character
        actv.setAdapter(adapter); // Setting the adapter data into the AutoCompleteTextView

        mDatabaseApartment.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allApartmentList=dataSnapshot;
                Apartment roomHolder=null;
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    roomHolder=data.getValue(Apartment.class);

                    allApartments.add(roomHolder);
                }

                copyAllApartments=new ArrayList<Apartment>(allApartments);

                if(allApartments.size()>0){

                    index=0;
                    show();
                }else{

                    mHeart.setVisibility(View.INVISIBLE);
                    mLeft.setVisibility(View.INVISIBLE);
                    mRight.setVisibility(View.INVISIBLE);
                    mCityText.setVisibility(View.INVISIBLE);
                    mZeroItemsText.setVisibility(View.VISIBLE);
                    mZeroItemsText.setText(" no apartments to show");

                    Picasso.with(apartments_scan.this)
                            .load("https://firebasestorage.googleapis.com/v0/b/appartners-2735b.appspot.com/o/uploads%2FnoMore.jpg?alt=media&token=1a4d7a69-d6c7-43a0-b888-1e810925ca0c")
                            .into(imgView);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query currentUserQuery= mDatabasePartner.orderByChild("email").equalTo(fAuto.getCurrentUser().getEmail());

        currentUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    currentUser = data.getValue(Partner.class);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String yourCity=actv.getText().toString().trim();
                int maxPriceInt=-1;
                String maxPriceText= mMaxPrice.getText().toString().trim();
                if(!maxPriceText.equals("")){ // only for convert to Integer

                    maxPriceInt=Integer.parseInt(maxPriceText);
                }

                if(!yourCity.equals("") || maxPriceInt!=-1) { // we need to filter by at least one parameter

                    allApartments.clear();
                    for (DataSnapshot data : allApartmentList.getChildren()) {

                        Apartment temp = data.getValue(Apartment.class);

                        if (!yourCity.equals("") && maxPriceInt == -1) { // only city filter

                            if (temp.getCity().equals(yourCity)) {

                                allApartments.add(temp);
                            }

                        } else if (yourCity.equals("") && maxPriceInt != -1) { // only price filter

                            if (temp.getPrice() <= maxPriceInt) {

                                allApartments.add(temp);
                            }

                        } else if (!yourCity.equals("") && maxPriceInt != -1) { // filter by price and city

                            if (temp.getCity().equals(yourCity) && temp.getPrice() <= maxPriceInt) {
                                allApartments.add(temp);
                            }
                        }

                    }
                    if(allApartments.size()>0){

                        index=0;
                        show();

                    }

                }
                else { // the fields are empty


                    allApartments.clear();
                    allApartments.addAll(copyAllApartments);
                    index = 0;
                    Toast.makeText(apartments_scan.this, "show all results", Toast.LENGTH_LONG).show();
                    show();
                }

                if(allApartments.size()==0) { // if we filtered but there is no any result

                    allApartments.addAll(copyAllApartments);
                    index=0;
                    Toast.makeText(apartments_scan.this, "There is no result for your search", Toast.LENGTH_LONG).show();
                    show();

                }

                mMaxPrice.setText("");
            }
        });

        mHeart.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(!cheackContains(currentApartment.getEmail())) {

                    Toast.makeText(apartments_scan.this, currentApartment.getName() + " added to your favorites", Toast.LENGTH_SHORT).show();
                    UserImage ui = new UserImage(currentApartment, currentApartment.getImg(0));
                    currentUser.addFav(ui);
                    mDatabasePartner.child(currentUser.getId()).setValue(currentUser);

                }else{

                    Toast.makeText(apartments_scan.this, currentApartment.getName()+" already in your favorites", Toast.LENGTH_SHORT).show();
                }
            }
        }));

        mRight.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                index++;
                if(index>= allApartments.size()){

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

                    index= allApartments.size()-1;

                }

                show();


            }
        }));

        imgView.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                Toast.makeText(apartments_scan.this, "img clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(apartments_scan.this, picsApartInfo.class);

                 intent.putExtra("userEmail", currentApartment.getEmail());
                 startActivity(intent);

            }
        });

    }

    @Override
    public void onBackPressed() {
        //startActivity(new Intent(this,login.class));
        return;
    }

    // menu code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.apartment_scan_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()) {

            case R.id.personal_details_item:
                intent=new Intent(this,personal_details.class);
                intent.putExtra("searchingFor", "Searching apartment");
                startActivity(intent);
                return true;

            case R.id.my_fav_item:
                intent=new Intent(this,favorites.class);
                intent.putExtra("searchingFor", "Searching apartment");
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

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                FirebaseAuth.getInstance().getCurrentUser().delete(); // remove from Authentication
                                if(!currentUser.getImgUrl().equals("")){

                                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser.getImgUrl());
                                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // File deleted successfully
                                            Log.i("delete files","file deleted");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Uh-oh, an error occurred!
                                        }
                                    });
                                }

                                mDatabasePartner.child(currentUser.getId()).setValue(null); // remove from Database
                                Toast.makeText( apartments_scan.this, "User is Deleted", Toast.LENGTH_LONG ).show();
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


        imgUrl=allApartments.get(index).getImg(0);
        if(imgUrl.equals(""))
            imgUrl="https://firebasestorage.googleapis.com/v0/b/appartners-2735b.appspot.com/o/uploads%2FnoPhoto.png?alt=media&token=8fb5f8d9-a80f-4360-843d-7aae13546d13";

        Picasso.with(apartments_scan.this) // show first User img
                .load(imgUrl)
                .into(imgView);
        currentApartment =allApartments.get(index);
        mCityText.setText( "City " + currentApartment.getCity() );

    }


}

