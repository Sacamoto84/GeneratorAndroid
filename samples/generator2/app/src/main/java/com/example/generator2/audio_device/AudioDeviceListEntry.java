package com.example.generator2.audio_device;

import android.annotation.TargetApi;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Vector;

/**
 * POJO which represents basic information for an audio device.
 *
 * Example: id: 8, deviceName: "built-in speaker"
 */
public class AudioDeviceListEntry {

    private int mId;
    private String mName;

    public AudioDeviceListEntry(int deviceId, String deviceName){
        mId = deviceId;
        mName = deviceName;
    }

    public int getId() {
        return mId;
    }

    public String getName(){
        return mName;
    }

    @NonNull
    public String toString(){
        return "| "+mId+":"+getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioDeviceListEntry that = (AudioDeviceListEntry) o;

        if (mId != that.mId) return false;
        return mName != null ? mName.equals(that.mName) : that.mName == null;
    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        return result;
    }

    /**
     * Create a list of AudioDeviceListEntry objects from a list of AudioDeviceInfo objects.
     *
     * @param devices A list of {@Link AudioDeviceInfo} objects
     * @param directionType Only audio devices with this direction will be included in the list.
     *                      Valid values are GET_DEVICES_ALL, GET_DEVICES_OUTPUTS and
     *                      GET_DEVICES_INPUTS.
     * @return A list of AudioDeviceListEntry objects
     */
    static List<AudioDeviceListEntry> createListFrom(AudioDeviceInfo[] devices, int directionType){

        List<AudioDeviceListEntry> listEntries = new Vector<>();
        for (AudioDeviceInfo info : devices) {
            if (directionType == AudioManager.GET_DEVICES_ALL ||
                    (directionType == AudioManager.GET_DEVICES_OUTPUTS && info.isSink()) ||
                    (directionType == AudioManager.GET_DEVICES_INPUTS && info.isSource())) {
                listEntries.add(new AudioDeviceListEntry(info.getId(), info.getProductName() + " " +
                                AudioDeviceInfoConverter.typeToString(info.getType())));
            }
        }
        return listEntries;
    }
}
