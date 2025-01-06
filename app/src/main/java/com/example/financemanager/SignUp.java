package com.example.financemanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, confrimPassword,name;
    Button btnSignup;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    String password;
    DatabaseReference realtimeDatabase;

    private DatabaseReference database;
    private FirebaseAuth auth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email1);
        editTextPassword = findViewById(R.id.password1);
        confrimPassword = findViewById(R.id.confirm_password);
        btnSignup = findViewById(R.id.signup_btn);
        progressBar = findViewById(R.id.progressBar);

        name=findViewById(R.id.name);
        textView = findViewById(R.id.loginNow);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        realtimeDatabase = FirebaseDatabase.getInstance().getReference();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email,name1;
                email = editTextEmail.getText().toString();
                name1=name.getText().toString();
                if (editTextPassword.getText().toString().equals(confrimPassword.getText().toString())) {
                    password = editTextPassword.getText().toString();
//                    String pass =editTextPassword.getText().toString();
//                    if(pass.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9])(?=.{6,})")){
//                        password=pass;
//                    }
//                    else{
//                        editTextPassword.setError("Password is not strong");
//                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUp.this, "Passwords must match", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (TextUtils.isEmpty(email)) {
                    progressBar.setVisibility(View.GONE);
                    editTextEmail.setError("Email cannot be empty");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    progressBar.setVisibility(View.GONE);
                    editTextPassword.setError("Password cannot be empty");
                    return;
                }
                if (TextUtils.isEmpty(confrimPassword.getText().toString())) {
                    progressBar.setVisibility(View.GONE);
                    confrimPassword.setError("Password cannot be empty");
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String userId = user.getUid();
                                    pushNameToFirebase(userId,name1);
                                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //Toast.makeText(signup.this, "Verification email has been sent..", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                        }
                                    });

                                    Toast.makeText(SignUp.this, "Account created",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignUp.this, "Account creation failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

    }

    private void pushNameToFirebase(String userId,String name) {
        // Get the current user's ID (userId)
        if (userId != null) {

            DatabaseReference userRef = database.child("users").child(userId).child(name);

            // Push the name to Firebase
            userRef.setValue(name).addOnSuccessListener(aVoid -> {
                // Successfully added the name
                Toast.makeText(SignUp.this, "Name saved successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                // Failed to add the name
                Toast.makeText(SignUp.this, "Failed to save name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // No authenticated user
            Toast.makeText(SignUp.this, "User is not logged in!", Toast.LENGTH_SHORT).show();
        }
    }
}