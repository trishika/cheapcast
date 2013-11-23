package at.maui.cheapcast.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

import at.maui.cheapcast.Const;
import at.maui.cheapcast.R;
import at.maui.cheapcast.fragment.DonationsFragment;

public class UPnPActivity extends RoboSherlockFragmentActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upnp);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_ab);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
