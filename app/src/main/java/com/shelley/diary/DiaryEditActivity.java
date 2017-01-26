package com.shelley.diary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shelley.diary.backend.myApi.MyApi;
import com.shelley.diary.backend.myApi.model.MyDiary;

import java.io.IOException;

public class DiaryEditActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private long diaryId;
    private String diaryTitle;
    private String diaryContent;
    private String user_id;

    private EditText mTitle;
    private EditText mContent;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mProgress = new ProgressDialog(this);

        EditText title = (EditText) findViewById(R.id.edit_title);
        EditText content = (EditText) findViewById(R.id.edit_content);

        Intent intent = getIntent();
        diaryId = intent.getExtras().getLong(MainActivity.DIARY_ID_EXTRA);
        diaryTitle = intent.getExtras().getString(MainActivity.DIARY_TITLE_EXTRA);
        diaryContent = intent.getExtras().getString(MainActivity.DIARY_CONTENT_EXTRA);

        title.setText(diaryTitle);
        content.setText(diaryContent);
    }


    private void confirmDialog() {
        AlertDialog.Builder conformBuilder = new AlertDialog.Builder(DiaryEditActivity.this);
        conformBuilder.setTitle("Are you sure?");
        conformBuilder.setMessage("Are you sure you want to make change to the diary?");

        conformBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mTitle = (EditText) findViewById(R.id.edit_title);
                mContent = (EditText) findViewById(R.id.edit_content);

                startSaving();
            }
        });

        conformBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing
            }
        });

        AlertDialog confirmDialogObject = conformBuilder.create();
        confirmDialogObject.show();
    }


    private void startSaving() {
        diaryTitle = mTitle.getText().toString().trim();
        diaryContent  = mContent.getText().toString().trim();
        user_id = mCurrentUser.getUid();

        // If title and content are both not empty, save it to database
        if (!TextUtils.isEmpty(diaryTitle) && !TextUtils.isEmpty(diaryContent)) {
            mProgress.setMessage("Saving Diary ...");
            mProgress.show();
            new EditDiaryAsyncTask(this).execute();
        }
        else {
            Toast.makeText(DiaryEditActivity.this, "Title and content cannot be empty.", Toast.LENGTH_LONG).show();
        }
    }


    private class EditDiaryAsyncTask extends AsyncTask<Void, Void, MyDiary>{
        private MyApi myApiService = null;
        private Context context;

        public EditDiaryAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected MyDiary doInBackground(Void... args) {
            MyDiary diary = new MyDiary();
            diary.setDiaryId(diaryId);
            diary.setTitle(diaryTitle);
            diary.setContent(diaryContent);
            diary.setUserId(user_id);

            if (myApiService == null) {
                myApiService = new ApiActivity().buildApiService();
            }

            try {
                return myApiService.updateDiary(diary).execute();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(MyDiary myDiary) {
            mProgress.dismiss();

            Intent intent = new Intent(DiaryEditActivity.this, DiaryDetailActivity.class);
            intent.putExtra(MainActivity.DIARY_ID_EXTRA, diaryId);
            intent.putExtra(MainActivity.DIARY_TITLE_EXTRA, diaryTitle);
            intent.putExtra(MainActivity.DIARY_CONTENT_EXTRA, diaryContent);
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done_diary) {
            confirmDialog();
        }
        return super.onOptionsItemSelected(item);
    }
}
