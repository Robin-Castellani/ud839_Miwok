package com.example.android.miwok;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhrasesFragment extends Fragment {

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

    public PhrasesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.word_list, container, false);

        ArrayList<Word> words = new ArrayList<Word>();
        words.add(new Word("minto wuksus", "Where are you going?", null, R.raw.phrase_where_are_you_going));
        words.add(new Word("tinnә oyaase'nә", "What is your name?", null, R.raw.phrase_what_is_your_name));
        words.add(new Word("oyaaset...", "My name is...", null, R.raw.phrase_my_name_is));
        words.add(new Word("michәksәs?", "How are you feeling?", null, R.raw.phrase_how_are_you_feeling));
        words.add(new Word("kuchi achit", "I’m feeling good.", null, R.raw.phrase_im_feeling_good));
        words.add(new Word("әәnәs'aa?", "Are you coming?", null, R.raw.phrase_are_you_coming));
        words.add(new Word("hәә’ әәnәm", "Yes, I’m coming.", null, R.raw.phrase_yes_im_coming));
        words.add(new Word("әәnәm", "I’m coming.", null, R.raw.phrase_im_coming));
        words.add(new Word("yoowutis", "Let’s go.", null, R.raw.phrase_lets_go));
        words.add(new Word("әnni'nem", "Come here.", null, R.raw.phrase_come_here));

        // create an ArrayAdapter, useful to handle data in a RecyclerView
        // it puts every element of words into an Android standard View, the
        //  simple_list_item_1
        // the ArrayAdapter is a concrete implementation of the ListAdapter interface
        WordAdapter adapter = new WordAdapter(
                getActivity(), words, R.color.category_phrases
        );

        // get the id of the ListView
        final ListView listView = (ListView) rootView.findViewById(R.id.word_list);

        // attach the ArrayAdapter to the ListView
        listView.setAdapter(adapter);

        // get the audio system service and request the audio focus (deprecated call)
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
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
                        Log.v("PhrasesActivity", "Current word" + currentWord);

                        // if no audio, display a Toast and exit
                        if (audioID == null) {
                            Toast.makeText(
                                    getActivity(),
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
                                    getActivity(), audioID
                            );
                            pronunciationAudio.start();

                            // release resources when the audio has finished playing
                            pronunciationAudio.setOnCompletionListener(mCompletionListener);
                        }
                    }
                }
        );

        return rootView;
    }

    @Override
    public void onPause() {
        // Release audio resources when the app enters the Pause mode (app not visible anymore)
        releaseMediaPlayer();
        super.onPause();
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
