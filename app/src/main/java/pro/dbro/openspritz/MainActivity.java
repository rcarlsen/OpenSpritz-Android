package pro.dbro.openspritz;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.KeyEvent;

import nl.siegmann.epublib.domain.Book;

public class MainActivity extends ActionBarActivity implements WpmDialogFragment.OnWpmSelectListener, ChapterListDialogFragment.OnChapterSelectListener, SpritzFragment.SpritzFragmentListener {
    private static final String TAG = "MainActivity";
    private static final String PREFS = "ui_prefs";
    private static final int THEME_LIGHT = 0;
    private static final int THEME_DARK = 1;

    private int mWpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int theme = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt("THEME", 0);
        switch (theme) {
            case THEME_LIGHT:
                setTheme(R.style.Light);
                break;
            case THEME_DARK:
                setTheme(R.style.Dark);
                break;
        }
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new SpritzFragment(), "spritsfrag")
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        if (getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().getData() != null) {
            SpritzFragment frag = ((SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag"));
            frag.feedEpubToSpritzer(getIntent().getData());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_speed) {
            if (mWpm == 0) {
                if (((SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag")).getSpritzer() != null) {
                    mWpm = ((SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag")).getSpritzer().mWPM;
                } else {
                    mWpm = 500;
                }
            }
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DialogFragment newFragment = WpmDialogFragment.newInstance(mWpm);
            newFragment.show(ft, "dialog");
            return true;
        } else if (id == R.id.action_theme) {
            int theme = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getInt("THEME", THEME_DARK);
            if (theme == THEME_LIGHT) {
                applyDarkTheme();
            } else {
                applyLightTheme();
            }
        } else if (id == R.id.action_open) {
            ((SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag")).chooseEpub();
        }
        return super.onOptionsItemSelected(item);
    }

    // added to support Glass interaction
    // TODO: add the GDK and use the GestureDetector class for better interacation.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // tap
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // on Glass, this will reveal the options as cards.
            openOptionsMenu();
            return true;
        }
        // swipes
        if (keyCode == KeyEvent.KEYCODE_TAB) {
            if (((SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag")).getSpritzer() != null) {
                // we get multiple taps for long swipes
                // since the spritzer does not (yet) support fast forward / rewind
                // let's filter the events to only send a click if changing direction.
                boolean shouldSendClick = false;
                SpritzFragment sf = (SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag");
                // backwards swipe and playing, then stop
                if (event.isShiftPressed() ) {
                    if ( sf.getSpritzer().isPlaying() == true ) {
                        shouldSendClick = true;
                    }
                }
                // forwards swipe and not playing, then play
                else if ( sf.getSpritzer().isPlaying() == false ) {
                    shouldSendClick = true;
                }

                // this is received as a toggle, so only send the event if an edge has been detected
                // (change in swipe direction, change is playback)
                if (shouldSendClick) {
                    sf.getSpritzView().performClick();
                }
                return true;
            }
        }

        // down - leave the activity
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onWpmSelected(int wpm) {
        if (((SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag")).getSpritzer() != null) {
            ((SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag")).getSpritzer()
                    .setWpm(wpm);
        }
        mWpm = wpm;
    }

    private void applyDarkTheme() {
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt("THEME", THEME_DARK)
                .commit();
        recreate();

    }

    private void applyLightTheme() {
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt("THEME", THEME_LIGHT)
                .commit();
        recreate();
    }

    @Override
    public void onChapterSelected(int chapter) {
        SpritzFragment frag = ((SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag"));
        if (frag.getSpritzer() != null) {
            frag.getSpritzer().printChapter(chapter);
            frag.updateMetaUi();
        } else {
            Log.e(TAG, "SpritzFragment not available for chapter selection");
        }
    }

    @Override
    public void onChapterSelectRequested() {
        SpritzFragment frag = ((SpritzFragment) getSupportFragmentManager().findFragmentByTag("spritsfrag"));
        if (frag.getSpritzer() != null) {
            Book book = frag.getSpritzer().getBook();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DialogFragment newFragment = ChapterListDialogFragment.newInstance(book);
            newFragment.show(ft, "dialog");
        } else {
            Log.e(TAG, "SpritzFragment not available for chapter selection");
        }
    }
}
