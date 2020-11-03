package com.example.appartners;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class personal_details extends AppCompatActivity {


    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TakedPicture = 2;
    private int uploadFrom = 0; // 0 image not Choosed yet / 1 is gallery / 2 is camera

    private EditText mTellAbout;
    private EditText mPhone;
    private Button mButtonChooseImage;
    private Button mButtonTakePic;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private CardView updateCardView;

    private Uri mImageUri;
    private String urlGallery;
    private String urlCaptured;

    private boolean isPartner;
    private DataSnapshot myData;

    private User currentUser;

    private Bundle bundle;
    private String searchingFor;

    private FirebaseAuth fAuth;
    Query query;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRefPartner;
    private DatabaseReference mDatabaseRefApartment;


    private StorageReference mountainsRef; // for camera capture
    private StorageReference mountainImagesRef; //  for camera capture

    private Bitmap bitmap; // the image will save as bitmap in order to show it

    private StorageTask mUploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        mTellAbout = findViewById(R.id.tellAboutText);
        mPhone = findViewById(R.id.phoneNumerText);
        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonTakePic = findViewById(R.id.takePicButton);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);
        updateCardView=findViewById(R.id.updateCard);

        bundle = getIntent().getExtras();
        searchingFor = bundle.getString("searchingFor");

        fAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads"); // save in storage
        mDatabaseRefPartner = FirebaseDatabase.getInstance().getReference("Partner");
        mDatabaseRefApartment=FirebaseDatabase.getInstance().getReference("Apartment");


        if(searchingFor.equals("Searching partner")){

            isPartner =false;
            mButtonTakePic.setVisibility(View.INVISIBLE);
            mButtonChooseImage.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.INVISIBLE);
        }else{

            isPartner =true;
        }

        mountainsRef = mStorageRef.child(""+System.currentTimeMillis()+".jpg");
        // Create a reference to 'images/mountains.jpg'
        StorageReference mountainImagesRef = mStorageRef.child("images/mountains.jpg");

        // While the file names are the same, the references point to different files
        mountainsRef.getName().equals(mountainImagesRef.getName()); // true
        mountainsRef.getPath().equals(mountainImagesRef.getPath()); // false


        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });


        if(isPartner){

            query= mDatabaseRefPartner.orderByChild("email").equalTo(fAuth.getCurrentUser().getEmail());
        }else{

            query= mDatabaseRefApartment.orderByChild("email").equalTo(fAuth.getCurrentUser().getEmail());
        }

        query.addListenerForSingleValueEvent (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    myData=data;
                    currentUser=data.getValue(User.class);
                    // get the details from db
                    mTellAbout.setText(currentUser.getTellAbout());
                    mPhone.setText(currentUser.getPhone());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        updateCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUploadTask != null && mUploadTask.isInProgress()) { // if not null and not already uploaded
                    Toast.makeText(personal_details.this, "upload in progress", Toast.LENGTH_SHORT).show();
                } else {

                    if(checkFields()) {

                        if(isPartner){

                            mDatabaseRefPartner.child(currentUser.getId()).child("tellAbout").setValue(mTellAbout.getText().toString());
                            mDatabaseRefPartner.child(currentUser.getId()).child("phone").setValue(mPhone.getText().toString());

                        }else{

                            mDatabaseRefApartment.child(currentUser.getId()).child("tellAbout").setValue(mTellAbout.getText().toString());
                            mDatabaseRefApartment.child(currentUser.getId()).child("phone").setValue(mPhone.getText().toString());
                            goTo();
                        }

                        if(uploadFrom==1 && isPartner){
                            uploadFileFromGallery();
                            goTo();
                        }else if (uploadFrom==2 && isPartner){

                            uploadFromCapturedImage();
                            goTo();
                        } else{

                            if(isPartner){ // if the User (partner) doesn't have img

                                Partner p =myData.getValue(Partner.class);
                                if(p.getImgUrl().equals("")){

                                    Toast.makeText(personal_details.this, "please upload image", Toast.LENGTH_SHORT).show();

                                }else{// if the User already has img and pressed upload

                                    goTo();

                                }
                            }
                        }
                    }
                }
            }
        });

        mButtonTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,TakedPicture);
            }
        });
    }

    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // called when we picked our file/take picture

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK // successfully picked image
                && data != null && data.getData()!=null ) {
            mImageUri = data.getData(); // the ui of the image we picked
            Picasso.with(this).load(mImageUri).into(mImageView); // load the image to the image view
            uploadFrom=1;
        }else{
            if (requestCode == TakedPicture && resultCode==RESULT_OK) {

                bitmap = (Bitmap) data.getExtras().get("data");
                mImageView.setImageBitmap(bitmap);

                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) { // not sure
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showPermissionExplanation();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, TakedPicture);
                    }
                }

                uploadFrom=2;
            }else{
                Toast.makeText(this, "Result canceled", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void showPermissionExplanation() // not sure we need it anymore because external flash mybe not needed
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This app need to access to your External Storage because bla bla bla");
        builder.setTitle("External Storage needed");
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, TakedPicture);
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private String getFileExtension(Uri uri) { // responsible for the file extension

        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFileFromGallery() {

        if (mImageUri != null) { // only one pic
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() // to get unique id we put current time
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri);

            mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Handler handler = new Handler(); // to delay the progress bar for 0.5 sec
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(0);
                        }
                    }, 500);

                    // urlGallery=taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                    Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            urlGallery=uri.toString();
                            Toast.makeText(personal_details.this, ""+urlGallery, Toast.LENGTH_LONG).show();


                            query=mDatabaseRefPartner.orderByChild("email").equalTo(fAuth.getCurrentUser().getEmail());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot data : dataSnapshot.getChildren()) {

                                        Partner currentPartner = data.getValue(Partner.class);
                                        currentPartner.setImgUrl(urlGallery);
                                        //call updateField(currentUser) function that set the data from the page into currentUser object
                                        mDatabaseRefPartner.child(currentPartner.getId()).setValue(currentPartner);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) { }
                            });


                        }
                    });


                    // Toast.makeText(personal_details.this, "upload successful", Toast.LENGTH_LONG).show();

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(personal_details.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        } else { // no pictures
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFromCapturedImage() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] dataArr = baos.toByteArray();
        UploadTask uploadTask = mountainsRef.putBytes(dataArr);

        mStorageRef.child( ""+System.currentTimeMillis()+".jpg"); // gives unique name
        mountainImagesRef = mStorageRef.child(""+System.currentTimeMillis()+".jpg");

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(personal_details.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                Handler handler = new Handler(); // to delay the progress bar for 0.5 sec
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setProgress(0);
                    }
                }, 500);

                //urlCaptured = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();
                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        urlCaptured=uri.toString();
                        Toast.makeText(personal_details.this, ""+urlCaptured, Toast.LENGTH_LONG).show();

                        query=mDatabaseRefPartner.orderByChild("email").equalTo(fAuth.getCurrentUser().getEmail());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    Partner currentPartner= data.getValue(Partner.class);
                                    currentPartner.setImgUrl(urlCaptured);
                                    //call updateField(currentUser) function that set the data from the page into currentUser object
                                    mDatabaseRefPartner.child(currentPartner.getId()).setValue(currentPartner);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });
                    }
                });

                // Toast.makeText(personal_details.this, "upload successful", Toast.LENGTH_LONG).show();

            }
        })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mProgressBar.setProgress((int) progress);
                    }
                });
    }

    public void goTo(){

        Toast.makeText(this, ""+searchingFor, Toast.LENGTH_SHORT).show();
        if (isPartner){

            startActivity(new Intent(getApplicationContext(), apartments_scan.class));

        }else{

            startActivity(new Intent(getApplicationContext(), apartment_details.class));

        }


    }

    public boolean checkFields(){

        boolean isValid=true;
        String tellAbout=mTellAbout.getText().toString();
        String phoneNum=mPhone.getText().toString();

        if(TextUtils.isEmpty(tellAbout)) {
            mTellAbout.setError("no details");
            isValid=false;
        }

        if(TextUtils.isEmpty(phoneNum)){
            mPhone.setError("no number");
            isValid=false;
        }

        return isValid;

    }

}
