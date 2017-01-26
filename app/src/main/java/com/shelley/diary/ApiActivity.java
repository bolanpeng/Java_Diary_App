package com.shelley.diary;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.shelley.diary.backend.myApi.MyApi;

import java.io.IOException;

public class ApiActivity {

    public ApiActivity() {

    }

    public MyApi buildApiService() {

        MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // options for running on App Engine
                .setRootUrl("https://diary-151121.appspot.com/_ah/api/");

                // options for running against local devappserver
                //.setRootUrl("http://10.0.2.2:8080/_ah/api/")
                //.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                //   @Override
                //    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                //       abstractGoogleClientRequest.setDisableGZipContent(true);

        return builder.build();
    }
}
