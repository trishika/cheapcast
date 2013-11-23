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

package org.droidupnp.model.cling;

import java.util.Observer;

import org.droidupnp.controller.upnp.IUpnpServiceController;
import org.droidupnp.model.CObservable;
import org.droidupnp.model.upnp.IUpnpDevice;
import org.droidupnp.model.upnp.RendererDiscovery;

import android.app.Activity;
import android.util.Log;

public abstract class UpnpServiceController implements IUpnpServiceController {

	private static final String TAG = "UpnpServiceController";

	protected IUpnpDevice renderer;
	protected CObservable rendererObservable;

	private final RendererDiscovery rendererDiscovery;

	@Override
	public RendererDiscovery getRendererDiscovery()
	{
		return rendererDiscovery;
	}

	protected UpnpServiceController()
	{
		rendererObservable = new CObservable();
		rendererDiscovery = new RendererDiscovery(getServiceListener());
	}

	// Pause the service
	@Override
	public void pause()
	{
		rendererDiscovery.pause(getServiceListener());
	}

	// Resume the service
	@Override
	public void resume(Activity activity)
	{
		rendererDiscovery.resume(getServiceListener());
	}

}