package faceapp.com.myapplication.ui;

/**
 * Created by Admin on 12/18/2016.
 */
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import faceapp.com.myapplication.R;

public class DisplayActivity extends AppCompatActivity {
    public ImageView mDisplayImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mDisplayImageView = (ImageView) findViewById(R.id.iv_dis);
        String compoundPath = getIntent().getStringExtra("image");
        if (!TextUtils.isEmpty(compoundPath)) {
            mDisplayImageView.setImageURI(Uri.parse(compoundPath));
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}
