package edu.osu.sphs.soundmap.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import edu.osu.sphs.soundmap.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textView = findViewById(R.id.made_with_heart);
        String madeWithHeart = "Made with " + new String(Character.toChars(0x2764)) + " at\nThe Ohio State University";
        textView.setText(madeWithHeart);
    }
}
