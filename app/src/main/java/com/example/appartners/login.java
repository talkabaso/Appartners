package com.example.appartners;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {


 //   VideoView mVideo;
    EditText mEmail, mPassword;
    CardView mLoginBtn;
    TextView mCreateBtn, mforgotPass;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    DatabaseReference databaseLoginForPartner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

      //  mVideo = findViewById( R.id.video );
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.loginBtn);
        mCreateBtn = findViewById(R.id.createText);
        mforgotPass = findViewById( R.id.forgotPass );
//        progressBar.setVisibility(View.INVISIBLE);

//        String path = "android.resource://com.example.appartners/"+R.raw.video;
//        Uri u = Uri.parse( path );
//        mVideo.setVideoURI( u );
//        mVideo.start();

 //       mVideo.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mp.setLooping( true );
//            }
//        } );

        databaseLoginForPartner = FirebaseDatabase.getInstance().getReference("Partner");

        mLoginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6){
                    mPassword.setError("Password Must be >= 6 Characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);


                //authenticate the User

                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(login.this, "Logged in Successfuly", Toast.LENGTH_LONG).show();

                            Query query= databaseLoginForPartner.orderByChild("email").equalTo(fAuth.getCurrentUser().getEmail());

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChildren()){
                                        startActivity(new Intent(getApplicationContext(), apartments_scan.class));
                                    }else{
                                        startActivity(new Intent(getApplicationContext(), partners_scan.class));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }else{
                            Toast.makeText(login.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        mCreateBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), register.class));
            }
        });

        mforgotPass.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent( login.this, resetPassword.class ) );
            }
        } );


    }
 //   @Override
//    protected void onResume(){
//        mVideo.resume();
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause(){
//        mVideo.suspend();
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy(){
//        mVideo.stopPlayback();
//        super.onDestroy();
//    }
}