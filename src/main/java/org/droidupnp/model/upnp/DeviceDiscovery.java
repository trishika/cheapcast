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

package org.droidupnp.model.upnp;

import java.util.ArrayList;
import java.util.Collection;

import android.util.Log;

import at.maui.cheapcast.activity.PreferenceActivity;

public abstract class DeviceDiscovery {

	protected static final String TAG = "DeviceDiscovery";

	private final BrowsingRegistryListener browsingRegistryListener;

	private final ArrayList<IDeviceDiscoveryObserver> observerList;

	private IServiceListener serviceListener;

	public DeviceDiscovery(IServiceListener serviceListener)
	{
        this.serviceListener = serviceListener;
		browsingRegistryListener = new BrowsingRegistryListener();
		observerList = new ArrayList<IDeviceDiscoveryObserver>();
	}

	public void resume()
	{
		serviceListener.addListener(browsingRegistryListener);
	}

	public void pause()
	{
		serviceListener.removeListener(browsingRegistryListener);
	}

	public class BrowsingRegistryListener implements IRegistryListener {

		@Override
		public void deviceAdded(final IUpnpDevice device)
		{
			Log.i(TAG, "New device detected : " + device.getDisplayString());

			if (device.isFullyHydrated() && filter(device))
			{
				notifyAdded(device);
			}
		}

		@Override
		public void deviceRemoved(final IUpnpDevice device)
		{
			Log.i(TAG, "Device removed : " + device.getFriendlyName());

			if (filter(device))
			{
				notifyRemoved(device);
			}
		}
	}

	public void addObserver(IDeviceDiscoveryObserver o)
	{
		observerList.add(o);

		final Collection<IUpnpDevice> upnpDevices = serviceListener.getFilteredDeviceList(getCallableFilter());
		for (IUpnpDevice d : upnpDevices)
			o.addedDevice(d);
	}

	public void removeObserver(IDeviceDiscoveryObserver o)
	{
		observerList.remove(o);
	}

	public void notifyAdded(IUpnpDevice device)
	{
		for (IDeviceDiscoveryObserver o : observerList)
			o.addedDevice(device);
	}

	public void notifyRemoved(IUpnpDevice device)
	{
		for (IDeviceDiscoveryObserver o : observerList)
			o.removedDevice(device);
	}

	/**
	 * Filter device you want to add to this device list fragment
	 * 
	 * @param device
	 *            the device to test
	 * @return add it or not
	 * @throws Exception
	 */
	protected boolean filter(IUpnpDevice device)
	{
		ICallableFilter filter = getCallableFilter();
		filter.setDevice(device);
		try
		{
			return filter.call();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Get a callable device filter
	 * 
	 * @return
	 */
	protected abstract ICallableFilter getCallableFilter();
}
