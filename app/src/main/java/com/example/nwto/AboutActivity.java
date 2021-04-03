package com.example.nwto;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private TextView mContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);// set drawable home icon

        mContent = (TextView) findViewById(R.id.about_NWTO);
        mContent.setMovementMethod(LinkMovementMethod.getInstance());
        mContent.setText(Html.fromHtml(getString(R.string.aboutNWTO)));
    }
}
