package com.shelley.diary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shelley.diary.backend.myApi.MyApi;
import com.shelley.diary.backend.myApi.model.MyDiary;

import com.google.android.gms.auth.api.Auth;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String DIARY_ID_EXTRA = "com.shelley.mydiary.Identifier";
    public static final String DIARY_TITLE_EXTRA = "com.shelley.mydiary.Title";
    public static final String DIARY_CONTENT_EXTRA = "com.shelley.mydiary.Content";

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private ListView listView;
    private MyApi myApiService = null;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgress = new ProgressDialog(this);

        /*************************** Google Sign In Log Out ***************************************/
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        /**********************************Add New Diary*******************************************/
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NewDiaryActivity.class));
            }
        });


        /**********************************Display Diaries*****************************************/
        new ListDiariesAsyncTask(this).execute();

    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_log_out) {
            // Firebase sign out
            mAuth.signOut();
            // Google sign out
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.long_press_menu, menu);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MyDiary diary = (MyDiary) listView.getItemAtPosition(info.position);

        mProgress.setMessage("Deleting Diary ...");
        mProgress.show();
        new DeleteDiariesAsyncTask(this).execute(diary.getDiaryId());

        return super.onContextItemSelected(item);
    }


    private class ListDiariesAsyncTask extends AsyncTask<Void, Void, List<MyDiary>> {
        private Context context;

        public ListDiariesAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected List<MyDiary> doInBackground(Void... params) {
            if (myApiService == null) {
                myApiService = new ApiActivity().buildApiService();
            }

            try {
                return myApiService.listDiaries(mCurrentUser.getUid()).execute().getItems();
            } catch (IOException e) {
                return Collections.EMPTY_LIST;
            }
        }

        @Override
        protected void onPostExecute(List<MyDiary> result) {
            listView = (ListView) findViewById(R.id.list);

            // Display empty view
            if (result == null) {
                listView.setEmptyView(findViewById(R.id.emptyView));
                listView.setVisibility(View.VISIBLE);
            }
            // Display diaries list
            else {
                final DiaryListAdapter diaryListAdapter = new DiaryListAdapter(context, result);
                listView.setAdapter(diaryListAdapter);


                /********************************Edit Diaries************************************/
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(MainActivity.this, DiaryDetailActivity.class);

                        // Get Information for the diary we clicked on and pass along
                        MyDiary diary = diaryListAdapter.getItem(position);
                        intent.putExtra(MainActivity.DIARY_ID_EXTRA, diary.getDiaryId());
                        intent.putExtra(MainActivity.DIARY_TITLE_EXTRA, diary.getTitle());
                        intent.putExtra(MainActivity.DIARY_CONTENT_EXTRA, diary.getContent());

                        startActivity(intent);

                    }
                });
            }
            registerForContextMenu(listView);
        }
    }


    private class DeleteDiariesAsyncTask extends AsyncTask<Long, Void, Void> {
        private Context context;

        public DeleteDiariesAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Long... args) {
            Long diaryId = args[0];

            if (myApiService == null) {
                myApiService = new ApiActivity().buildApiService();
            }

            try {
                return myApiService.removeDiary(diaryId).execute();
            } catch (IOException e) {
                Log.e("Error Deleting: ", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgress.dismiss();
            new ListDiariesAsyncTask(MainActivity.this).execute();
        }
    }
}
