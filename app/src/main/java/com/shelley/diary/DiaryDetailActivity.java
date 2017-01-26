package com.shelley.diary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DiaryDetailActivity extends AppCompatActivity {

    private long diaryId;
    private String diaryTitle;
    private String diaryContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);

        TextView title = (TextView) findViewById(R.id.view_title);
        TextView content = (TextView) findViewById(R.id.view_content);

        Intent intent = getIntent();
        diaryId = intent.getExtras().getLong(MainActivity.DIARY_ID_EXTRA);
        diaryTitle = intent.getExtras().getString(MainActivity.DIARY_TITLE_EXTRA);
        diaryContent = intent.getExtras().getString(MainActivity.DIARY_CONTENT_EXTRA);

        title.setText(diaryTitle);
        content.setText(diaryContent);
    }


    private void launchEditView() {
        Intent intent = new Intent(DiaryDetailActivity.this, DiaryEditActivity.class);
        intent.putExtra(MainActivity.DIARY_ID_EXTRA, diaryId);
        intent.putExtra(MainActivity.DIARY_TITLE_EXTRA, diaryTitle);
        intent.putExtra(MainActivity.DIARY_CONTENT_EXTRA, diaryContent);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit_diary) {
            launchEditView();
        }
        return super.onOptionsItemSelected(item);
    }
}
