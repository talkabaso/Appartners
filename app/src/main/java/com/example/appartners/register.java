package com.example.appartners;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class register extends AppCompatActivity {

    private EditText mFullName, mEmail, mPassword;
    private Button mRegisterBtn;
    private TextView mLoginBtn;
    private FirebaseAuth fAuto;
    private ProgressBar progressBar;
    private boolean chosenCity;
    private boolean chosenGender;
    private RadioGroup genderGroup;
    private RadioButton genderBtn;
    private boolean chosenAprPrt;
    private boolean searchPartners, searchRoom;
    private RadioGroup searchingFor;
    private RadioButton aprPrtBtn;
    private TextView mDateOfBirth;
    private int age;
    private String name="";
    private String gender="";
    private String aprPrt="";
    private String password="";
    private String email="";
    private String yourCity;

    private   ArrayAdapter<String> cityAdapter;

     DatePickerDialog.OnDateSetListener mDataSetListener;
     DatabaseReference mDatabasePartner;
    DatabaseReference mDatabaseApartment;


    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDatabasePartner = FirebaseDatabase.getInstance().getReference("Partner");
        mDatabaseApartment = FirebaseDatabase.getInstance().getReference("Apartment");


        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.createText);
        genderGroup=findViewById(R.id.radioGroup);
        searchingFor =findViewById(R.id.radioGroup2);
        mDateOfBirth = findViewById(R.id.Birthday);


        fAuto = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

          cityAdapter = new ArrayAdapter<>
                (this,android.R.layout.select_dialog_item,getResources().getStringArray(R.array.city_Array));

final AutoCompleteTextView actv =  findViewById(R.id.autoCompleteTextView);
        actv.setThreshold(1); // Will start working from first character
        actv.setAdapter(cityAdapter); // Setting the cityAdapter data into the AutoCompleteTextView

        if(fAuto.getCurrentUser() != null){ // check If it works *********
            FirebaseAuth.getInstance().signOut();
        }

        mDateOfBirth.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        register.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDataSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        }));

        mDataSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                age = 2021-year;
                String birthday =  day + "/" + month + "/" + year;
                mDateOfBirth.setText(birthday);
            }
        };

        mRegisterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                name = mFullName.getText().toString().trim();
                email = mEmail.getText().toString().toLowerCase().trim();
                password = mPassword.getText().toString().trim();
                gender="";
                  yourCity = actv.getText().toString().trim();

                int selectedRadioButtonID = genderGroup.getCheckedRadioButtonId();

                int selectedRadioButtonID2 = searchingFor.getCheckedRadioButtonId();

                if (selectedRadioButtonID2 != -1) {
                    aprPrtBtn = findViewById(selectedRadioButtonID2);
                    aprPrt = aprPrtBtn.getText().toString().trim();
                    chosenAprPrt=true;
                    if(aprPrt.equals("Searching partner"))
                        searchPartners =true;
                    else if(aprPrt.equals("Searching apartment"))
                        searchRoom =true;
                }
                else{
                    chosenAprPrt=false;
                }
                // If nothing is selected from Radio Group, then it return -1
                if (selectedRadioButtonID != -1) {
                    genderBtn = findViewById(selectedRadioButtonID);
                    gender = genderBtn.getText().toString();
                    chosenGender=true;
                }
                else{
                    chosenGender=false;
                }

                if (yourCity.equals("") || !foundCity() ){
                    actv.setError("City is Required");
                    chosenCity=false;
                }
                else{
                    chosenCity=true;
                }


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


                if(searchingFor.equals("Searching partner")){

                    id = mDatabaseApartment.push().getKey();
                }else{

                    id= mDatabasePartner.push().getKey();
                }


                progressBar.setVisibility(View.VISIBLE);

                //register the User in firebase
                if (chosenCity && chosenGender && age!=0 && chosenAprPrt) {
                    fAuto.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                if(searchRoom){

                                    Partner p = new Partner(id,name,gender,yourCity,age,email);
                                    mDatabasePartner.child(id).setValue(p);
                                    Toast.makeText(register.this, "User Created "+email, Toast.LENGTH_SHORT).show();
                                    // intent put Extra
                                    Intent intent = new Intent(register.this, personal_details.class);
                                    intent.putExtra("searchingFor", "Searching apartment");
                                    startActivity(intent);

                                }

                                else if(searchPartners) {

                                    Apartment a=new Apartment(id,name,gender,yourCity,age,email);
                                    mDatabaseApartment.child(id).setValue(a);
                                    Toast.makeText(register.this, "User Created "+email, Toast.LENGTH_SHORT).show();

                                    // intent put Extra
                                    Intent intent = new Intent(register.this, personal_details.class);
                                    intent.putExtra("searchingFor", "Searching partner");
                                    startActivity(intent);

                                }
                            } else {
                                Toast.makeText(register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                } else {
                    Toast.makeText(register.this, "please fill up all details", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), login.class));
            }
        });
    }

    public boolean foundCity(){

        int size=cityAdapter.getCount();
        for(int i=0;i<size;i++){

            if(cityAdapter.getItem(i).equals(yourCity)){

                return true;
            }
        }
        return false;
    }
}