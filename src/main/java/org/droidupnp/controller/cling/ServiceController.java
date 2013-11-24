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

package org.droidupnp.controller.cling;

import org.droidupnp.controller.upnp.IUpnpServiceController;
import org.droidupnp.model.CObservable;
import org.droidupnp.model.cling.UpnpService;
import org.droidupnp.model.upnp.RendererDiscovery;
import org.fourthline.cling.model.meta.LocalDevice;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceController implements IUpnpServiceController
{
	private static final String TAG = "Cling.ServiceController";

	private final ServiceListener upnpServiceListener;
	protected CObservable rendererObservable;
	private final RendererDiscovery rendererDiscovery;

	private Context ctx = null;

	public ServiceController(Context ctx)
	{
		super();
		this.ctx = ctx;
		upnpServiceListener = new ServiceListener(ctx);
		rendererObservable = new CObservable();
		rendererDiscovery = new RendererDiscovery(getServiceListener());
	}

	@Override
	public RendererDiscovery getRendererDiscovery()
	{
		return rendererDiscovery;
	}

	@Override
	protected void finalize()
	{
		pause();
	}

	@Override
	public ServiceListener getServiceListener()
	{
		return upnpServiceListener;
	}

	@Override
	public void pause()
	{
		rendererDiscovery.pause();
		ctx.unbindService(upnpServiceListener.getServiceConnexion());
	}

	@Override
	public void resume()
	{
		rendererDiscovery.resume();

		Log.d(TAG, "Bind to upnp service");
		ctx.bindService(new Intent(ctx, UpnpService.class), upnpServiceListener.getServiceConnexion(),
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public void addDevice(LocalDevice localDevice) {
		upnpServiceListener.getUpnpService().getRegistry().addDevice(localDevice);
	}

	@Override
	public void removeDevice(LocalDevice localDevice) {
		upnpServiceListener.getUpnpService().getRegistry().removeDevice(localDevice);
	}
}
