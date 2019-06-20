package com.example.firebaseauth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    TextView dispTxt;
    ImageView imgV;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dispTxt = findViewById(R.id.textView);
        imgV = findViewById(R.id.imageView);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        if(user==null){
            Intent i = new Intent(this,SignUpLogIn.class);
            startActivity(i);
        }
        else{
            dispTxt.setText("Welcome " + user.getDisplayName());
        }

        if (user==null) {
            Log.e("onCreate", "--------NULL user----------");
        }
        else{
            Log.e("USER??", user.getEmail()+" | "+ user.isEmailVerified());;
        }



    }

    public void sendEmailPasswordChange(View v){
        String email = user.getEmail();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this ,"Please Check your Email",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this ,"Error: Server Busy...",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void sendEmailVerification(View v){
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this ,"Please Check your Email",Toast.LENGTH_SHORT).show();

                        }
                        else{
                            Toast.makeText(MainActivity.this ,"Error: Server Busy...",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    public void signOut(View v){
        auth.signOut();
        finish();
    }


    public void LoadPic(View v){
        String urldisplay;
        try {
            urldisplay = user.getPhotoUrl().toString();
        }
        catch (Exception e){
            Toast.makeText(this,"No Image Found",Toast.LENGTH_SHORT).show();
            Log.e("ERROR",e.getMessage());
            return;
        }
        //============================================
        /*Bitmap bmp = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            bmp = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        imgV.setImageBitmap(bmp);*/
        //===========================================this will crash the app since you cant run this on UI thread

        Picasso.get().load(urldisplay).into(imgV);
    }




}
//for Listening to SIgnIn and SignOut results initialize this listener in onCreate()
    /*FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if (firebaseAuth.getCurrentUser() == null){
                //user just signed OUT
            }
            else {
                //user just signed IN
            }
        }
    };*/