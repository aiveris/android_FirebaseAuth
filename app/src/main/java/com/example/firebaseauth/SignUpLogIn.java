package com.example.firebaseauth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseauth.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.concurrent.TimeUnit;

public class SignUpLogIn extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editPhone, editCode;
    private Button btnSignup, btnLogin, btnSendCode, btnConfirmCode;
    private TextView textViewStatus;
    private ProgressDialog progressDialog;

    FirebaseAuth auth;
    FirebaseUser user;
    //=========================

    private static final String TAG = "GoogleSIGN_IN";
    private static final int G_SIGN_IN = 12345;
    GoogleSignInClient mGoogleSignInClient;
    //=========================

    String codeReceived;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_log_in);

        auth = FirebaseAuth.getInstance();

        editTextEmail =  findViewById(R.id.editTextEmail);
        editTextPassword =  findViewById(R.id.editTextPassword);
        editPhone =  findViewById(R.id.editPhone);
        editCode =  findViewById(R.id.editVerifyCode);

        textViewStatus =  findViewById(R.id.textViewSignin);
        btnSignup =  findViewById(R.id.buttonSignup);
        btnLogin =  findViewById(R.id.buttonLogin);
        progressDialog = new ProgressDialog(this);

        //====================================================
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //===================================================




    }//onCreate ends

    public void signUP(View v){
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();


        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SignUpLogIn.this ,"Your Registration was SUCCESSFUL!",Toast.LENGTH_SHORT).show();
                    //... enterUserInFirebase()
                    user = auth.getCurrentUser();
                    makeUserCredentials();
                    Log.e("onCOmplete", "User Added: " + user.getEmail());
                }
                else {
                    Toast.makeText(SignUpLogIn.this,"Registration Error",Toast.LENGTH_LONG).show();
                    textViewStatus.setText(task.getException().getMessage());
                }

                progressDialog.dismiss();
            }
        });

        Log.e("SignUp","Method() ends.... xxxxxxxxxxx");
    }

    private void makeUserCredentials() {
        UserProfileChangeRequest updateUserProfile = new UserProfileChangeRequest.Builder()
                .setDisplayName("Mindaugas")
                //.setPhotoUri(Uri.parse("https://XYZ.com/ABC/myIcon.jpg"))//...or even from your phone storage
                .build();

        user.updateProfile(updateUserProfile)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.e("UpdatedName", "DisplayName added to User's Profile");
                        }
                    }
                });
    }


    public void Login(View v) {
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //checking if email and passwords are empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog
        progressDialog.setMessage("Signing-In, Please Wait...");
        progressDialog.show();

        //logging in the user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpLogIn.this, "Log-In Successful.", Toast.LENGTH_SHORT).show();

                        }
                        else {
                            Toast.makeText(SignUpLogIn.this, "Sign-In Error", Toast.LENGTH_LONG).show();
                            textViewStatus.setText(task.getException().getMessage());
                        }
                    }
                });
    }

    //==========================================================================

    public void GoogleSignIn(View v){
        Log.e(TAG, "CLICK" );
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, G_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "00000000000" );
        // Result returned
        if (requestCode == G_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.e(TAG, "11111111111111" );
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);//if we get an account, means success
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.e(TAG,"From OnResult(): " + e.toString() );
                textViewStatus.setText(e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.e(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //showProgressDialog();


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpLogIn.this,"Google SignIn SUCCESS", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = auth.getCurrentUser();
                            textViewStatus.setText("Logged: " + user.getEmail());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, task.getException().getMessage());
                            Toast.makeText(SignUpLogIn.this,"SORRY....", Toast.LENGTH_SHORT).show();
                            textViewStatus.setText(task.getException().getMessage());
                        }


                        //hideProgressDialog();

                    }
                });
    }
    //==========================================================================

    public void sendCode(View v){

        String phone = editPhone.getText().toString();

        if((phone.isEmpty()) || (phone.length() < 10 )){
            editPhone.setError("Please Enter a Valid Phone Number");
            editPhone.requestFocus();
            return;
        }

        //==================================for fakeNumber
        //phone = "+37010000000";//this is the fakeNumber you enetered in the console WhiteNumbers
        //==================================for fakeNumber

        Log.e("Sending","1111111111");
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,60, TimeUnit.SECONDS, // Phone number to verify // Timeout duration // Unit of timeout
                this,                  // Activity (for callback binding)
                mCallbackMethod);            // OnVerificationStateChangedCallbacks

        Log.e("Sending","22222222");
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbackMethod = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.e("FAILED", e.toString());
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.e("onCodeSent()","s: " + s);
            codeReceived = s;

        }
    };


    public void verifyCode(View v){
        String codetxt  = editCode.getText().toString();
        //Please check if string code is valid/not empty

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeReceived,codetxt);

        //begin signIn with the credential
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"Login Successful", Toast.LENGTH_LONG).show();
                        }
                        else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),"Verification ERROR", Toast.LENGTH_LONG).show();
                                Log.e("PhoneAuth",task.getException().getMessage());
                            }
                        }
                    }
                });
    }
}


