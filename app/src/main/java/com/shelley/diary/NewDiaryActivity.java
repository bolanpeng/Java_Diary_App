package com.shelley.diary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shelley.diary.backend.myApi.MyApi;
import com.shelley.diary.backend.myApi.model.MyDiary;

import java.io.IOException;

public class NewDiaryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private EditText mTitle;
    private EditText mContent;
    private Button mSubmitBtn;
    private ProgressDialog mProgress;

    private String title_val;
    private String content_val;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_diary);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mProgress = new ProgressDialog(this);

        mTitle = (EditText) findViewById(R.id.diary_title);
        mContent = (EditText) findViewById(R.id.diary_content);
        mSubmitBtn = (Button) findViewById(R.id.saveDiary);

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
    }


    private void startPosting() {
        title_val = mTitle.getText().toString().trim();
        content_val = mContent.getText().toString().trim();
        user_id = mCurrentUser.getUid();

        // If title and content are both not empty, save it to database
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(content_val)) {
            mProgress.setMessage("Saving Diary ...");
            mProgress.show();
            new newDiaryAsyncTask(this).execute();
        }
        else {
            Toast.makeText(NewDiaryActivity.this, "Title and content cannot be empty.", Toast.LENGTH_LONG).show();
        }
    }


    private class newDiaryAsyncTask extends AsyncTask<Void, Void, MyDiary>{
        private MyApi myApiService = null;
        private Context context;

        public newDiaryAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected MyDiary doInBackground(Void... args) {

            MyDiary diary = new MyDiary();
            diary.setTitle(title_val);
            diary.setContent(content_val);
            diary.setUserId(user_id);

            if (myApiService == null) {
                myApiService = new ApiActivity().buildApiService();
            }

            try {
                return myApiService.insertDiary(diary).execute();
            } catch (IOException e) {
                Toast.makeText(NewDiaryActivity.this, "Uh oh, there seems to be a problem, please try again later.", Toast.LENGTH_LONG).show();
                return null;
            }
        }


        @Override
        protected void onPostExecute(MyDiary myDiary) {
            mProgress.dismiss();
            startActivity(new Intent(NewDiaryActivity.this, MainActivity.class));
        }
    }
}
