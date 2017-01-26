package com.shelley.diary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.shelley.diary.backend.myApi.MyApi;
import com.shelley.diary.backend.myApi.model.Users;
import com.vstechlab.easyfonts.EasyFonts;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "MAIN_ACTIVITY";
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private TextView appBrand;
    private SignInButton mGoogleBtn;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set the style for the app brand
        appBrand = (TextView) findViewById(R.id.app_brand);
        appBrand.setTypeface(EasyFonts.caviarDreams(this));

        // Get the Sign In related variables
        mAuth = FirebaseAuth.getInstance();

        mGoogleBtn = (SignInButton) findViewById(R.id.google_button);

        mProgress = new ProgressDialog(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "You Got an Error", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                mProgress.setMessage("Logging in ...");
                mProgress.show();

                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            mProgress.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            checkUserExists();
                        }
                    }
                });
    }

    private void checkUserExists() {
        if (mAuth.getCurrentUser() != null) {

            String user_id = mAuth.getCurrentUser().getUid();

            // Call API to check user ID in backend database
            new checkUserAsyncTask(this).execute(user_id);
        }
    }

    private class checkUserAsyncTask extends AsyncTask<String, Void, Users> {
        private MyApi myApiService = null;
        private Context context;

        public checkUserAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Users doInBackground(String... args) {
            String userId = args[0];

            if (myApiService == null) {
                myApiService = new ApiActivity().buildApiService();
            }

            try {
                return myApiService.verifyUser(userId).execute();
            } catch (IOException e) {
                Log.e("User ID not exist: ", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Users user) {
            // Check to see if users, if it does, redirect to Main page
            if (user != null) {
                mProgress.dismiss();
                Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
            }
            // If it doesn't, let user setup an account associated with this app
            else {
                mProgress.dismiss();
                Intent setupIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(setupIntent);
            }
        }
    }
}
