package com.shelley.diary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shelley.diary.backend.myApi.MyApi;
import com.shelley.diary.backend.myApi.model.Users;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private ImageButton mUserImage;
    private EditText mUsername;
    private Button mSetupBtn;
    private ProgressDialog mProgress;

    private String user_id;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mProgress = new ProgressDialog(this);

        mUserImage = (ImageButton) findViewById(R.id.user_image);
        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        mUsername = (EditText) findViewById(R.id.user_name);
        mSetupBtn = (Button) findViewById(R.id.setup_button);
        mSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetupAccount();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            mUserImage.setImageURI(imageUri);
        }
    }


    private void startSetupAccount() {
        user_id = mCurrentUser.getUid();
        username = mUsername.getText().toString().trim();

        if (!TextUtils.isEmpty(username)) {

            mProgress.setMessage("Finishing Setup ...");
            mProgress.show();

            // Store user data to database
            new newUserAsyncTask(RegisterActivity.this).execute();
        }
        else {
            Toast.makeText(RegisterActivity.this, "Name cannot be empty.", Toast.LENGTH_LONG).show();
        }
    }


    private class newUserAsyncTask extends AsyncTask<Void, Void, Users> {
        private MyApi myApiService = null;
        private Context context;

        public newUserAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Users doInBackground(Void... voids) {
            Users user = new Users();
            user.setUserId(user_id);
            user.setUsername(username);

            if (myApiService == null) {
                myApiService = new ApiActivity().buildApiService();
            }

            try {
                return myApiService.insertUser(user).execute();
            } catch (IOException e) {
                Toast.makeText(RegisterActivity.this, "Uh oh, there seems to be a problem, please try again later.", Toast.LENGTH_LONG).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Users users) {
            mProgress.dismiss();
            Intent setupIntent = new Intent(RegisterActivity.this, MainActivity.class);
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(setupIntent);
        }
    }
}
