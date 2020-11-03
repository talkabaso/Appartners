package com.example.appartners;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class resetPassword extends AppCompatActivity {

    private Button mResetPassBtn;
    private EditText mEmail;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_reset_password );

        mResetPassBtn = findViewById( R.id.resetPassBtn );
        mEmail = findViewById( R.id.Email );

        mAuth = FirebaseAuth.getInstance();

        mResetPassBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = mEmail.getText().toString();

                if(TextUtils.isEmpty( userEmail )){
                    mEmail.setError( "Please write your valid email address first.." );
                    return;
                }
                else{
                    mAuth.sendPasswordResetEmail( userEmail ).addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText( resetPassword.this, "Please check your Email Account, If you want to reset your password...", Toast.LENGTH_LONG ).show();
                                startActivity( new Intent( resetPassword.this, login.class ) );
                            }
                            else{
                                String message = task.getException().getMessage();
                                Toast.makeText( resetPassword.this, "Error Occured: " + message, Toast.LENGTH_SHORT ).show();
                            }
                        }
                    } );
                }
            }
        } );
    }
}