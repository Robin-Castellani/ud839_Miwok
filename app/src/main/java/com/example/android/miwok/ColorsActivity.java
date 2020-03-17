/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.miwok;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ColorsActivity extends AppCompatActivity {

    private MediaPlayer pronunciationAudio;

    private AudioManager audioManager;

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
        }
    };

    /**
     * Listener for the audio focus request
     */
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // another app has taken the audio focus; audio must stop
                releaseMediaPlayer();
            } else if (
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                            focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
            ) {
                // as each word counts, when audio focus is temporary loosen the audio pauses and
                // goes back to the beginning
                pronunciationAudio.pause();
                pronunciationAudio.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // audio focus is back and audio is resumed
                pronunciationAudio.start();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_list);

        ArrayList<Word> words = new ArrayList<Word>();
        words.add(new Word("weṭeṭṭi", "red", R.drawable.color_red, R.raw.color_red));
        words.add(new Word("chokokki", "green", R.drawable.color_green, R.raw.color_green));
        words.add(new Word("ṭakaakki", "brown", R.drawable.color_brown, R.raw.color_brown));
        words.add(new Word("ṭopoppi", "gray", R.drawable.color_gray, R.raw.color_gray));
        words.add(new Word("kululli", "black", R.drawable.color_black, R.raw.color_black));
        words.add(new Word("kelelli", "white", R.drawable.color_white, R.raw.color_white));
        words.add(new Word("ṭopiisә", "dusty yellow", R.drawable.color_dusty_yellow, R.raw.color_dusty_yellow));
        words.add(new Word("chiwiiṭә", "mustard yellow", R.drawable.color_mustard_yellow, R.raw.color_mustard_yellow));

        // create an ArrayAdapter, useful to handle data in a RecyclerView
        // it puts every element of words into an Android standard View, the
        //  simple_list_item_1
        // the ArrayAdapter is a concrete implementation of the ListAdapter interface
        WordAdapter adapter = new WordAdapter(
                this, words, R.color.category_colors
        );

        // get the id of the ListView
        final ListView listView = (ListView) findViewById(R.id.word_list);

        // attach the ArrayAdapter to the ListView
        listView.setAdapter(adapter);

        // get the audio system service and request the audio focus (deprecated call)
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final Integer requestResult = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        // set the behaviour when a list item is clicked
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // get the Word at the current position and get the pronunciation audio ID
                        Word currentWord = (Word) listView.getItemAtPosition(position);
                        Integer audioID = currentWord.getPronunciationResourceID();

                        // verbose log the word played
                        Log.v("ColorsActivity", "Current word" + currentWord);

                        // if no audio, display a Toast and exit
                        if (audioID == null) {
                            Toast.makeText(
                                    ColorsActivity.this,
                                    "No pronunciation to play",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        // check the result of the audio focus request and proceed if audio focus is gained
                        if (requestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            // the audio focus is gained and audio can be played
                            
                            // before playing any audio, release other possibly playing MediaPlayers
                            releaseMediaPlayer();

                            // instantiate the MediaPlayer object and play the pronunciation
                            pronunciationAudio = MediaPlayer.create(
                                    getApplicationContext(), audioID
                            );
                            pronunciationAudio.start();

                            // release resources when the audio has finished playing
                            pronunciationAudio.setOnCompletionListener(mCompletionListener);
                        }
                    }
                }
        );
    }

    @Override
    protected void onStop() {
        // Release audio resources when the app enters the Pause mode (app not visible anymore)
        releaseMediaPlayer();
        super.onStop();
    }

    /**
     * Clean up the media player by releasing its resources.
     */
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (pronunciationAudio != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            pronunciationAudio.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            pronunciationAudio = null;

            // abandon the audio focus at the end of the pronunciation playing
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }
}
