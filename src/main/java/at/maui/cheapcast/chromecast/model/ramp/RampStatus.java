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

package at.maui.cheapcast.chromecast.model.ramp;

import at.maui.cheapcast.chromecast.model.State;

public class RampStatus extends RampMessage {

    public RampStatus(int seq, State state){
        this.status = new RampStatusInternal(seq, state);
        setStatusType();
    }

    public void setStatusType(){
        this.setType("STATUS");
    }

    public void setResponseType(){
        this.setType("RESPONSE");
    }

    @Override
    public void setCmdId(int cmdId){
        super.setCmdId(cmdId);
        if(cmdId!=0)
            setResponseType();
        else
            setStatusType();
    }

    private RampStatusInternal status;

    public RampStatusInternal getStatus() {
        return status;
    }
    public void setStatus(RampStatusInternal status) {
        this.status = status;
    }

}
