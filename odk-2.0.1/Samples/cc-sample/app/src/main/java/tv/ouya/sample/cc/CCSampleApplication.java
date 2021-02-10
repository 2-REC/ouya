package tv.ouya.sample.cc;

import android.app.Application;
import android.os.Bundle;
import tv.ouya.console.api.OuyaController;
import tv.ouya.console.api.OuyaFacade;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CCSampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final InputStream in = getAssets().open("key.der");
            final byte[] buffer = new byte[1024];
            while(in.read(buffer) > 0) {
                out.write(buffer);
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        final Bundle devInfo = new Bundle();
        devInfo.putString(OuyaFacade.OUYA_DEVELOPER_ID, "f03cfdf5-09f5-4d31-8850-e71a66c8f85e");
        devInfo.putByteArray(OuyaFacade.OUYA_DEVELOPER_PUBLIC_KEY, out.toByteArray());

        OuyaFacade.getInstance().init(this, devInfo);
        OuyaController.init(this);
    }
}
