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

package com.github.androidpirate.slicknotes.data;

import android.content.Context;

import com.github.androidpirate.slicknotes.util.FakeData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

// TODO 2: New entities, Label and NoteLabelCrossRef are added into database
@Database(entities = {Note.class, Label.class, NoteLabelCrossRef.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class NoteDatabase extends RoomDatabase {
    private static NoteDatabase INSTANCE;
    public abstract NoteDao dao();

    public static NoteDatabase getInstance(final Context context) {
        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    NoteDatabase.class,
                    "notes-database")
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executor executor = Executors.newSingleThreadExecutor();
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    insertFakeData(context);
                                }
                            });
                        }
                    })
                    // TODO create real migrations, don't publish with this option
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    // TODO 3: This method still uses NoteDao's original insertDatabaseNote(Note note) method
    // TODO 3: May result in notes with no labels
    private static void insertFakeData(Context context) {
        List<NoteWithLabels> fakeNotes = FakeData.getNotes();
        NoteDao dao = getInstance(context).dao();
        for(NoteWithLabels noteWtLabels: fakeNotes) {
            dao.insertDatabaseNote(noteWtLabels.getNote());
        }
    }
}
