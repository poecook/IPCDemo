package com.weshape3d.ipcdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerService extends Service {
    private  CopyOnWriteArrayList<Headphones> headphonesCopyOnWriteArrayList = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IArrivedListener> arrivedListenerRemoteCallbackList = new RemoteCallbackList<>();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    Binder binder = new HeadphonesManager.Stub() {
        @Override
        public void addHeadphones(Headphones hp) throws RemoteException {
           haveNewHeadphones(hp);
        }

        @Override
        public List<Headphones> getHeadphoneList() throws RemoteException {
            return headphonesCopyOnWriteArrayList;
        }

        @Override
        public void registListener(IArrivedListener listener) throws RemoteException {
                arrivedListenerRemoteCallbackList.register(listener);
        }

        @Override
        public void unRegistListener(IArrivedListener listener) throws RemoteException {
            arrivedListenerRemoteCallbackList.unregister(listener);
        }
    };
    /**
     *添加新耳机
     * @param headphones
     */
    private void haveNewHeadphones(Headphones headphones){
        headphonesCopyOnWriteArrayList.add(headphones);
        int n = arrivedListenerRemoteCallbackList.beginBroadcast();
        for(int i = 0;i<n;i++){
           IArrivedListener listener =  arrivedListenerRemoteCallbackList.getBroadcastItem(i);
            try {
                listener.onArrived(headphones);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        arrivedListenerRemoteCallbackList.finishBroadcast();
    }
}
