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
        this.status = new Status(seq, state);
        setStatusType();
    }

    private void setStatusType(){
        this.setType("STATUS");
    }

    private void setResponseType(){
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

    private Status status;

    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public class Status {

        public Status(int seq, State state){
            this.state = state.getValue();
            this.eventSequence = seq;
        }

        private int eventSequence, state;
        private String contentId, title, imageUrl;
        private double duration, volume;
        private boolean muted, timeProgress;

        public int getEventSequence() {
            return eventSequence;
        }
        public void setEventSequence(int eventSequence) {
            this.eventSequence = eventSequence;
        }

        public int getState() {
            return state;
        }
        public void setState(int state) {
            this.state = state;
        }

        public String getContentId() {
            return contentId;
        }
        public void setContentId(String contentId) {
            this.contentId = contentId;
        }

        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }

        public String getImageUrl() {
            return imageUrl;
        }
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public double getDuration() {
            return duration;
        }
        public void setDuration(double duration) {
            this.duration = duration;
        }

        public double getVolume() {
            return volume;
        }
        public void setVolume(double volume) {
            this.volume = volume;
        }

        public boolean isMuted() {
            return muted;
        }
        public void setMuted(boolean muted) {
            this.muted = muted;
        }

        public boolean isTimeProgress() {
            return timeProgress;
        }
        public void setTimeProgress(boolean timeProgress) {
            this.timeProgress = timeProgress;
        }
    }
}
