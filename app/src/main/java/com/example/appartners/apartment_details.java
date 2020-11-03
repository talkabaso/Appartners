package com.example.appartners;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import java.util.ArrayList;
import java.util.List;

public class apartment_details extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TakedPicture=2;
    private int uploadFrom=0; // 0 image not Choosed yet / 1 is gallery / 2 is camera

    private Button mButtonChooseImage;
    private Button mButtonTakePic;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private CardView updateCardView;

    private Uri mImageUri;
    private String urlGallery;
    private String urlCaptured;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseApartment;

    private StorageReference mountainsRef; // for camera capture
    private StorageReference mountainImagesRef; // for camera capture

    private Apartment currentUser;

    private FirebaseAuth fAuth;
    private Query query;
    private Bitmap bitmap; // the image will save as bitmap in order to show it
    private StorageTask mUploadTask;
    private String imageEncoded;
    private List<String> imagesEncodedList;
    private ArrayList<Uri> mArrayUri;

    // details
    private EditText mNumOfRooms;
    private EditText mOccupants;
    private EditText mRoomType;
    private EditText mStreet;
    private EditText mPrice;

    private String numOfRooms;
    private String numOfOccupants;
    private String roomType;
    private String street;
    private String Price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartment_details);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonTakePic = findViewById(R.id.takePicButton);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);
        updateCardView=findViewById(R.id.updateCard);

        fAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads"); // save in storage
        mDatabaseApartment = FirebaseDatabase.getInstance().getReference("Apartment");

        query= mDatabaseApartment.orderByChild("email").equalTo(fAuth.getCurrentUser().getEmail());

        mountainsRef = mStorageRef.child(""+System.currentTimeMillis()+".jpg"); // dell
        // Create a reference to 'images/mountains.jpg'
        StorageReference mountainImagesRef = mStorageRef.child("images/mountains.jpg");

        // While the file names are the same, the references point to different files
        mountainsRef.getName().equals(mountainImagesRef.getName());    // true
        mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false
        mArrayUri = new ArrayList<Uri>();

        // details
        mNumOfRooms =findViewById(R.id.roomsNumText);
        mOccupants =findViewById(R.id.occupantsText);
        mRoomType =findViewById(R.id.typeOfRoomText);
        mStreet =findViewById(R.id.streetText);
        mPrice =findViewById(R.id.priceText);


        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });


        query.addListenerForSingleValueEvent (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    currentUser=data.getValue(Apartment.class);

                    // get the details from db

                    if(currentUser.getNumOfRooms()!=0){
                        mNumOfRooms.setText(""+currentUser.getNumOfRooms());
                    }
                    if(currentUser.getOccupants()!=0){
                        mOccupants.setText(""+currentUser.getOccupants());
                    }
                    if(currentUser.getPrice()!=0){
                        mPrice.setText(""+currentUser.getPrice());
                    }
                    mRoomType.setText(currentUser.getRoomType());
                    mStreet.setText(currentUser.getStreet());


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
                    Toast.makeText(apartment_details.this, "upload in progress", Toast.LENGTH_SHORT).show();
                } else {

                     numOfRooms=mNumOfRooms.getText().toString();
                     numOfOccupants=mOccupants.getText().toString();
                     roomType = mRoomType.getText().toString();
                     street=mStreet.getText().toString();
                     Price = mPrice.getText().toString();

                    if(checkFields()) {

                        currentUser.setNumOfRooms(Integer.parseInt(numOfRooms));
                        currentUser.setOccupants(Integer.parseInt(numOfOccupants));
                        currentUser.setRoomType(roomType);
                        currentUser.setStreet(street);
                        currentUser.setPrice(Double.parseDouble(Price));

                        mDatabaseApartment.child(currentUser.getId()).setValue(currentUser);

                        if(uploadFrom==1){
                            uploadFileFromGallery();
                            moveToPartnersScan();
                        }else if (uploadFrom==2){
                            uploadFromCapturedImage();
                            moveToPartnersScan();
                        } else {

                            if (currentUser.getImagesUri().size() == 0) {

                                Toast.makeText(apartment_details.this, "please upload image", Toast.LENGTH_SHORT).show();
                            } else {

                                moveToPartnersScan();
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

        mArrayUri.clear();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // called when we picked our file/take picture
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK // successfully picked image
                && data != null) {

            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            imagesEncodedList = new ArrayList<String>();
            if(data.getData()!=null) {
               // mImageView.setVisibility(View.VISIBLE);

                mImageUri = data.getData(); // the ui of the image we picked

                mArrayUri.add(mImageUri); // aric

                Picasso.with(this).load(mImageUri).into(mImageView); // load the image to the image view

                Cursor cursor = getContentResolver().query(mImageUri,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imageEncoded = cursor.getString(columnIndex);
                cursor.close();
            }
            else {
                if (data.getClipData() != null) {

                    mImageView.setVisibility(View.INVISIBLE);
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {

                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        mArrayUri.add(uri);
                        Toast.makeText(this, ""+uri, Toast.LENGTH_SHORT).show();
                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imageEncoded  = cursor.getString(columnIndex);
                        imagesEncodedList.add(imageEncoded);
                        cursor.close();
                    }
                    Toast.makeText(this, mArrayUri.size()+" images chosen", Toast.LENGTH_SHORT).show();
                }
            }
            uploadFrom=1;
        }else{

            if (requestCode == TakedPicture && resultCode==RESULT_OK) {

                mImageView.setVisibility(View.VISIBLE);
                bitmap = (Bitmap) data.getExtras().get("data");
                mImageView.setImageBitmap(bitmap);

                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){ //not sure
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

    public void showPermissionExplanation() // not sure
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
        if(!mArrayUri.isEmpty()){
            for(int i=0; i<mArrayUri.size(); i++){
                mImageUri=mArrayUri.get(i);
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

                                Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        urlGallery=uri.toString();


                                        currentUser.addImg(urlGallery);
                                        //call updateField(currentApart) function that set the data from the page into currentApart object
                                        mDatabaseApartment.child(currentUser.getId()).setValue(currentUser);
                                    }
                                });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(apartment_details.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        }
           else { // no pictures

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
                Toast.makeText(apartment_details.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

                Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();
                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        urlCaptured=uri.toString();
                        Toast.makeText(apartment_details.this, ""+urlCaptured, Toast.LENGTH_LONG).show();

                        currentUser.addImg(urlCaptured);
                        mDatabaseApartment.child(currentUser.getId()).setValue(currentUser);

                    }
                });

                 Toast.makeText(apartment_details.this, "upload successful", Toast.LENGTH_LONG).show();

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

    public void moveToPartnersScan(){

        startActivity(new Intent(getApplicationContext(), partners_scan.class));

    }

    public boolean checkFields(){

        boolean isValid=true;

        if(TextUtils.isEmpty(numOfRooms)) {
            mNumOfRooms.setError("no details");
            isValid=false;
        }

        if(TextUtils.isEmpty(numOfOccupants)) {
            mOccupants.setError("no details");
            isValid=false;
        }

        if(TextUtils.isEmpty(roomType)) {
            mRoomType.setError("no details");
            isValid=false;
        }

        if(TextUtils.isEmpty(street)) {
            mStreet.setError("no details");
            isValid=false;
        }

        if(TextUtils.isEmpty(Price)) {
            mPrice.setError("no details");
            isValid=false;
        }
        return isValid;
    }
}