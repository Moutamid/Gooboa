package com.app.gobooa.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.app.gobooa.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//This java/activity file is used to perform sign in operation in the application..
//This file contains code related to firebase authentication email and password sign in..
public class SignInActivity extends BaseActivity {

    private static final String TAG = "EmailPasswordActivity";

    EditText edtEmail, editPassword;
    Button btnLogin;
    String email, passowrd;
    FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = edtEmail.getText().toString().trim();
                passowrd = editPassword.getText().toString().trim();

                if(!isConnectionAvailable(SignInActivity.this)){
                    Toast.makeText(SignInActivity.this, "Check your network!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    edtEmail.setError("Required!");
                    edtEmail.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(passowrd)){
                    editPassword.setError("Required!");
                    editPassword.requestFocus();
                    return;
                }

                //Call signIn method to sign in user using firebase authentication..
                signIn();
            }
        });
    }

    private void signIn() {
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, passowrd).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                } else {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(getApplicationContext(), "Incorrect details", Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            }
        });
    }

    //This function checks mobile data connection or wifi is on or off..
    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    private void sendVerification() {
        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Verification email sent to " + firebaseUser.getEmail(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(isOnline()){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }
    boolean isOnline(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            return true;
        }
        return false;
    }
}