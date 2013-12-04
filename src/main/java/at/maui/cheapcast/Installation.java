/*
 * Copyright 2013 Sebastian Mauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.maui.cheapcast;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.UUID;

public class Installation {

    private static final String LOG_TAG = "Installation";
    private static ArrayList<String> sID = new ArrayList<String>();
    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static String id(Context context, int id) {
        String uuid = null;
        try {
            uuid = sID.get(id);
        } catch (IndexOutOfBoundsException e) {
            sID.ensureCapacity(id+1);
            for(int i=sID.size(); i<=id; i++)
                sID.add(null);
        }

        // TODO get the uuid provided by the upnp server instead
        if (uuid == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION+id);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sID.set(id, readInstallationFile(installation));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID.get(id);
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}