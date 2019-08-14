/*
 * <!--
 *  Copyright (C) 2016 The Android Open Source Project
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 * -->
 */

package com.github.androidpirate.slicknotes.viewmodel;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import com.github.androidpirate.slicknotes.data.Note;
import com.github.androidpirate.slicknotes.repo.NoteRepository;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class NoteListViewModel extends AndroidViewModel {
    private NoteRepository repo;
    private List<Note> pinnedNotes = new ArrayList<>();
    private List<Note> nonPinnedNotes = new ArrayList<>();
    private MediatorLiveData<List<Note>> databaseNotes = new MediatorLiveData<>();
    private boolean hasAlternateMenu;
    private List<Integer> selectedNoteIds = new ArrayList<>();

    public NoteListViewModel(@NonNull Application application) {
        super(application);
        repo = new NoteRepository(application);
        initializeNotes();
    }

    public LiveData<List<Note>> getDatabaseNotes() {
        return databaseNotes;
    }

    public LiveData<List<Note>> getDatabaseTrashNotes() {
        return repo.getDatabaseTrashNotes();
    }

    public void moveNotesToTrash(List<Note> notes) {
        for (Note note:
             notes) {
            note.setTrash(true);
        }
        repo.updateDatabaseNotes(notes);
    }

    public void pinNotes(List<Note> notes) {
        for (Note note:
             notes) {
            note.setPinned(true);
        }
        repo.updateDatabaseNotes(notes);
    }

    public void deleteNotes(List<Note> notes) {
        repo.deleteDatabaseNotes(notes);
    }

    public void restoreNotes(List<Note> notes) {
        for (Note note:
             notes) {
            note.setTrash(false);
        }
        repo.updateDatabaseNotes(notes);
    }

    public boolean isHasAlternateMenu() {
        return hasAlternateMenu;
    }

    public void setHasAlternateMenu(boolean hasAlternateMenu) {
        this.hasAlternateMenu = hasAlternateMenu;
    }

    public void addToSelectedNotes(int noteId) {
        selectedNoteIds.add(noteId);
    }

    public void removeFromSelectedNotes(int noteId) {
        // A cast is required to prevent noteId to be used as an index
        selectedNoteIds.remove((Integer)noteId);
    }

    public List<Integer> getSelectedNoteIds() {
        return selectedNoteIds;
    }

    public void clearSelectedNotesIds() {
        selectedNoteIds.clear();
    }

    private void initializeNotes() {
        LiveData<List<Note>> livePinnedNotes = repo.getPinnedDatabaseNotes();
        final LiveData<List<Note>> liveNonPinnedNotes = repo.getNonPinnedDatabaseNotes();
        databaseNotes.addSource(livePinnedNotes, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                pinnedNotes = notes;
                orderNotes();
            }
        });
        databaseNotes.addSource(liveNonPinnedNotes, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                nonPinnedNotes = notes;
                orderNotes();
            }
        });
    }

    private void orderNotes() {
        List<Note> combinedNotes = new ArrayList<>();
        combinedNotes.addAll(pinnedNotes);
        combinedNotes.addAll(nonPinnedNotes);
        databaseNotes.setValue(combinedNotes);
    }
}