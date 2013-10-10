package com.ltst.przwrd.app;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 09.10.13.
 */
public class DeviceDetails {

    static public @Nonnull String getHashDevice(@Nonnull Context context)
    {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

}
