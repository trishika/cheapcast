package org.droidupnp.model.upnp.didl;

public class SimpleDIDLItem implements IDIDLItem {

    private final String mUri;

    public SimpleDIDLItem(String uri)
    {
        mUri = uri;
    }

    public String getURI(){
        return mUri;
    }

    public String getType() { return "audioItem"; }

    public String getTitle(){
        return "";
    }

    public String getArtist(){
        return "";
    }

    public String getParentID(){
        return "";
    }

    public String getId(){
        return "";
    }
}
