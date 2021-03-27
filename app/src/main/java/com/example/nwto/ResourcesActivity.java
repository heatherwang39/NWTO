package com.example.nwto;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ResourcesActivity extends AppCompatActivity {

    private TextView mContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        mContent = (TextView) findViewById(R.id.resources_content);
        mContent.setMovementMethod(LinkMovementMethod.getInstance());
        mContent.setText(Html.fromHtml(getString(R.string.resourceContent)));
    }
}
