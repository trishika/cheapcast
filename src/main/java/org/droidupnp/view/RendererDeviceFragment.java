/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * 
 * This file is part of DroidUPNP.
 * 
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.droidupnp.view;

import android.os.Bundle;
import android.util.Log;

import at.maui.cheapcast.activity.PreferenceActivity;

public class RendererDeviceFragment extends UpnpDeviceListFragment {

	protected static final String TAG = "RendererDeviceFragment";

	public RendererDeviceFragment()
	{
		super();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		PreferenceActivity.upnpServiceController.getRendererDiscovery().addObserver(this);
		Log.d(TAG, "onActivityCreated");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
        PreferenceActivity.upnpServiceController.getRendererDiscovery().removeObserver(this);
		Log.d(TAG, "onDestroy");
	}
}