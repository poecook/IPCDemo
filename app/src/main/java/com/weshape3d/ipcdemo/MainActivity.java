package com.weshape3d.ipcdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.List;
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt).setOnClickListener(this);
        tv_show = findViewById(R.id.tv);
        Intent intent = new Intent(this,ServerService.class);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
    }
    HeadphonesManager headphonesManager;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
             headphonesManager = HeadphonesManager.Stub.asInterface(iBinder);
            try {
                //注册监听
                headphonesManager.registListener(new IArrivedListener.Stub() {
                    @Override
                    public void onArrived(Headphones hp) throws RemoteException {
                        Log.d("drummor","有新加了书了老铁"+hp.brand);
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
               //添加DeathRecipient监听，BInder死亡时候触发
                iBinder.linkToDeath(deathRecipient,0);
                tv_show.setText("连接成功");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if(headphonesManager==null)
                return;
            headphonesManager.asBinder().unlinkToDeath(deathRecipient,0);
            headphonesManager = null;
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void onClick(View view) {
        if(headphonesManager!=null){
            Headphones headphones = new Headphones(1,"bose","","2018");
            try {
                headphonesManager.addHeadphones(headphones);
               List<Headphones> arrayList = headphonesManager.getHeadphoneList();
                tv_show.setText(arrayList.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
