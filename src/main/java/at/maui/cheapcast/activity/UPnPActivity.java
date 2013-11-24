package at.maui.cheapcast.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

import org.droidupnp.view.RendererDeviceFragment;
import at.maui.cheapcast.R;

public class UPnPActivity extends RoboSherlockFragmentActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upnp);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_ab);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        RendererDeviceFragment fragment = new RendererDeviceFragment();
        ft.replace(R.id.content, fragment, "UPnPFragment");
        ft.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
