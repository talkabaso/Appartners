package com.example.appartners;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class myUploads extends AppCompatActivity {


    private DatabaseReference mDatabaseApartment;
    private FirebaseAuth fAuto;
    private String myEmail;
    private String imgUrl;
    private Apartment myApartment;
    private LinearLayout uploadsGallery;
    private LayoutInflater inflater;
    private TextView mTitleText;
    private Button mDelButton;


   private String currImg;

    private ArrayList<String> allImages;
    private ImageView mPicApart;

    private boolean[] checkedItems;
    private String[] indexes;
    private ArrayList<String> selectedImages;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_uploads);

        uploadsGallery =findViewById(R.id.uploadsGallery);
        inflater=LayoutInflater.from(this);
        selectedImages = new ArrayList<>();

        mTitleText=findViewById(R.id.titleText);
        mDelButton =findViewById(R.id.deleteButton);

        fAuto = FirebaseAuth.getInstance();

        mDatabaseApartment = FirebaseDatabase.getInstance().getReference("Apartment");

        myEmail = fAuto.getCurrentUser().getEmail();//  the email of the Apartment's User

        Query query= mDatabaseApartment.orderByChild("email").equalTo(myEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    myApartment = data.getValue(Apartment.class);
                    allImages = myApartment.getImagesUri();

                }


                if (allImages.size() > 0) {

                    mTitleText.setText("press delete and choose the indexes of the photos you want to remove");
                    checkedItems=new boolean[allImages.size()];
                    indexes =new String[allImages.size()];

                    for(int i=0;i<allImages.size();i++){

                        // show the images
                        imgUrl = allImages.get(i);
                        View view = inflater.inflate(R.layout.item, uploadsGallery, false);
                        mPicApart=view.findViewById(R.id.showPicApart);
                        Picasso.with(myUploads.this).load(imgUrl).resize(295,185).into(mPicApart);
                        uploadsGallery.addView(view);
                        // initialize the list to delete
                        indexes[i]=""+(i+1);
                    }
                }else{

                    mDelButton.setVisibility(View.INVISIBLE);
                    mTitleText.setText("you don't have any upload");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                displayMultiSelectDialog();

            }
        });


    }
    private void displayMultiSelectDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Select images indexes");
        dialogBuilder.setMultiChoiceItems(indexes,checkedItems,new DialogInterface.OnMultiChoiceClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if(isChecked){

                            selectedImages.add(indexes[which]);
                        }
                        else {
                            selectedImages.remove(indexes[which]);
                        }
                    }
                }

           );

        dialogBuilder.setPositiveButton("delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String[] copyAllImg=new String[allImages.size()];
                for(int i=0; i<allImages.size(); i++){
                    copyAllImg[i]=allImages.get(i);
                }

                int size= selectedImages.size();
                for(int i=0;i<size;i++){

                    currImg = copyAllImg[Integer.parseInt(selectedImages.get(i))-1];
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

                    allImages.remove(copyAllImg[Integer.parseInt(selectedImages.get(i))-1]);

                }

                mDatabaseApartment.child(myApartment.getId()).child("imagesUri").setValue(allImages);

                finish();
                startActivity(getIntent());

            }
        });
        dialogBuilder.create().show();
    }

    private void showSelectedColors() {
        // do whatever you want to do with the user choice(s)
//        Toast.makeText(this, selectedImages.toString(), Toast.LENGTH_SHORT).show();
    }


}
