package com.weshape3d.messengerdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import static com.weshape3d.messengerdemo.MainActivity.*;

public class MessengerService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
     return mMessenger.getBinder();
    }
    private static class MessengerHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_FROM_CLIENT:
                  String str_msg = (String) msg.getData().getString("msg");
                    Log.d("drummor","获取了来自client的msg："+str_msg);
                    break;
            }
            super.handleMessage(msg);
        }
    }
    private final Messenger mMessenger = new Messenger(new MessengerHandler());
}
