package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }


    public Note getNote(String title) {
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("GET", null)
                .build();


        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();

            return Note.fromJSON(body);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     */
    public void echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        msg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
