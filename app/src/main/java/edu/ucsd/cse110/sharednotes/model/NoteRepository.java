package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.nio.channels.MulticastChannel;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NoteRepository {
    private final NoteDao dao;
    private static ScheduledFuture<?> poller; // what could this be for... hmm?

    public NoteRepository(NoteDao dao) {
        this.dao = dao;
    }

    // Synced Methods
    // ==============

    /**
     * This is where the magic happens. This method will return a LiveData object that will be
     * updated when the note is updated either locally or remotely on the server. Our activities
     * however will only need to observe this one LiveData object, and don't need to care where
     * it comes from!
     * <p>
     * This method will always prefer the newest version of the note.
     *
     * @param title the title of the note
     * @return a LiveData object that will be updated when the note is updated locally or remotely.
     */
    public LiveData<Note> getSynced(String title) {
        var note = new MediatorLiveData<Note>();

        Observer<Note> updateFromRemote = theirNote -> {
            var ourNote = note.getValue();
            if (theirNote == null) return; // do nothing
            if (ourNote == null || ourNote.version < theirNote.version) {
                upsertLocal(theirNote, false);
            }
        };

        // If we get a local update, pass it on.
        note.addSource(getLocal(title), note::postValue);
        // If we get a remote update, update the local version (triggering the above observer)
        note.addSource(getRemote(title), updateFromRemote);

        return note;
    }

    public void upsertSynced(Note note) {
        upsertLocal(note);
        upsertRemote(note);
    }

    // Local Methods
    // =============

    public LiveData<Note> getLocal(String title) {
        return dao.get(title);
    }

    public LiveData<List<Note>> getAllLocal() {
        return dao.getAll();
    }

    public void upsertLocal(Note note, boolean incrementVersion) {
        // We don't want to increment when we sync from the server, just when we save.
        if (incrementVersion) note.version = note.version + 1;
        note.version = note.version + 1;
        dao.upsert(note);
    }

    public void upsertLocal(Note note) {
        upsertLocal(note, true);
    }

    public void deleteLocal(Note note) {
        dao.delete(note);
    }

    public boolean existsLocal(String title) {
        return dao.exists(title);
    }

    // Remote Methods
    // ==============

    public LiveData<Note> getRemote(String title) {
        // TODO: Implement getRemote!
        // TODO: Set up polling background thread (MutableLiveData?)
        // TODO: Refer to TimerService from https://github.com/DylanLukes/CSE-110-WI23-Demo5-V2.

        Log.d("getRemote", "get remote called with title: " + title);

        // Cancel any previous poller if it exists.
        if (this.poller != null && !this.poller.isCancelled()) {
            poller.cancel(true);
        }

        // Set up a background thread that will poll the server every 3 seconds.



       var remoteNote = new MutableLiveData<Note>();



        var executer  = Executors.newSingleThreadScheduledExecutor();
        poller = executer.scheduleAtFixedRate(()->{

            var note = NoteAPI.provide().getNote(title);

            Log.d("Note from server contnets:", note.content);

            remoteNote.postValue(NoteAPI.provide().getNote(title));
        }, 0, 3, TimeUnit.SECONDS);



        //This code bellow is blocking. Causes the app to freeze untill server request reutrns
//        try {
//            poller.get(500,TimeUnit.MILLISECONDS);
//
//        } catch (ExecutionException e) {
//           // throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//          //  throw new RuntimeException(e);
//        } catch (TimeoutException e) {
//           // throw new RuntimeException(e);
//        }
        return remoteNote;

        // You may (but don't have to) want to cache the LiveData's for each title, so that
        // you don't create a new polling thread every time you call getRemote with the same title.
        // You don't need to worry about killing background threads.

        //throw new UnsupportedOperationException("Not implemented yet");
    }

    public void upsertRemote(Note note) {
        // TODO: Implement upsertRemote!
       //throw new UnsupportedOperationException("Not implemented yet");
//        if(note.version == 0){
//            note.version = note.version + 1;
//        }
        NoteAPI.provide().putNoteAsync(note);
//        dao.upsert(note);
    }
}
