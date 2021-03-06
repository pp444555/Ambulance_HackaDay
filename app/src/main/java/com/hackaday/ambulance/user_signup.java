package com.hackaday.ambulance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class user_signup extends AppCompatActivity {

    EditText name, email, phone, pass, rePass;
    Button user_signup;
    ProgressBar progressBar;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private void startMap(){
        //Method to switch activity

        Intent user_map=new Intent(user_signup.this, User_Map.class);
        startActivity(user_map);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_signup);
        name=findViewById(R.id.signup_Name);
        email=findViewById(R.id.signup_email);
        phone=findViewById(R.id.signup_phone);
        pass=findViewById(R.id.signup_Password);
        rePass=findViewById(R.id.signup_rePassword);
        progressBar=findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Toast.makeText(user_signup.this, "Login after signup Success with Firebase\nUser Signup: Dont't Judge Work in progress", Toast.LENGTH_LONG).show();
                    startMap();
                }
            }
        };

        user_signup=findViewById(R.id.bt_user_submit);
        user_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(user_signup.this, "User Signup: Coming Soon!", Toast.LENGTH_SHORT).show();

                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                progressBar.setVisibility(View.VISIBLE);

                final String Name=name.getText().toString();
                final String Email=email.getText().toString();
                final String Phone=phone.getText().toString();
                final String Password=pass.getText().toString();
                final String RePassword=rePass.getText().toString();
                if(Name.length()>=3){

                    if(Email.length()>0 && Phone.length()>=10){

                        if(Password.length()>0 && Password.equals(RePassword)){

                            Query emailQuery=FirebaseDatabase.getInstance().getReference().child("Members").child("Users").orderByChild("Email").equalTo(Email);
                            emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getChildrenCount()>0){
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(user_signup.this, "Email already exists!!", Toast.LENGTH_LONG).show();
                                    }

                                    else {

                                        mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(user_signup.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {

                                                progressBar.setVisibility(View.GONE);
                                                if(task.isSuccessful()){
                                                    String user_id= mAuth.getCurrentUser().getUid();
                                                    DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Members").child("Users").child(user_id);
                                                    Map newUser = new HashMap();
                                                    newUser.put("Name", Name);
                                                    newUser.put("Phone", Phone);
                                                    newUser.put("Email", Email);

                                                    current_user_db.setValue(newUser);

                                                }
                                                else{
                                                    Toast.makeText(user_signup.this, "User SignUp: Failed!! PLease Try Again", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                        else if (Password.length()<8){
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(user_signup.this, "Password Must be atleast 8 characters long", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(user_signup.this, "Password don't match please Re-enter", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(user_signup.this, "Please enter valid Email and Phone nmuber", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(user_signup.this, "Enter Valid Name 1st", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
