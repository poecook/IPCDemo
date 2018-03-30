# IPCDemo
Android IPC多进程通信 aidl



# Android IPC 概述
## 开启进程
Android 开启多进程的方法很简单，四大组件在AndroidMenifest中给android:process属性指定值如下：

```
 <service
    android:process=":remote"
    android:name=".ServerService">
```
## 多进程带来的问题
一个应用中四大组件处于不同的进程中就意味着他们有独立的运行空间，虚拟机和Application，相应的会带来以下问题：
- 单例模式、静态变量失效。
- Application多次创建。
- 线程同步失效
- SharePreferences可靠性下降。

## 进程和线程的关系
- 线程和进程是包含和被包含的关系。
- 线程是CPU的最小调度单元。
- 一个进程中可以包含多个线程，指一个执行单元

# Android IPC 实现（AIDL）

Android的IPC方式有多种，但是毫无疑问功能性最强的就是AIDL。接下来我们就看如如何使用aidl实现进程间的通信。
## 整体的过程
![image](https://wx4.sinaimg.cn/mw690/82cd45a9ly1fpupsh6vk2j20e307qq36.jpg)
### 服务端
- 创建Service等待客户端发来连接请求。
- 创建AIDL文件，将暴露给客户端使用的接口在这个文件中声明。
- 在Service中实现这个接口。

### 客户端
- 绑定服务端的Service
- 绑定成功后讲返回的binder对象转化为AIDL接口所属的类型。
- 使用AIDL中的方法

## 具体实现
我们模拟一个存放商店里耳机的行为，包括添加耳机，查看有什么耳机，有了耳机就进行通知这些功能。

### AIDL接口创建
#### 支持的数据类型
主要支持以下六种数据
1. 基本数据类型
2. String CharSequence
3. Parcelable
4. aidl文件
5. List(只支持ArrayList)
6. Map（只支持HashMap）

#### 创建aidl注意事项
- 其中ADIL和**Parcelable**必须显示的import不管他们是不是在一个包内
- 如果aidl中用到了parceable对象，需要创建一个和它同名的aidl文件，并在文件中声明这个对象。
- 除了基本数据类型外，其他的类型参数必要指定方向。in、out、或者inout。
- aidl不支持静态常量。
//Headphones.java

```
package com.weshape3d.ipcdemo;

import android.os.Parcel;
import android.os.Parcelable;

public class Headphones implements Parcelable {
    public int id;
    public String brand;
    public String name;
    public String price;

    protected Headphones(Parcel in) {
        id = in.readInt();
        brand = in.readString();
        name = in.readString();
        price = in.readString();
    }

    public Headphones(int id, String brand, String name, String price) {
        this.id = id;
        this.brand = brand;
        this.name = name;
        this.price = price;
    }

    public static final Creator<Headphones> CREATOR = new Creator<Headphones>() {
        @Override
        public Headphones createFromParcel(Parcel in) {
            return new Headphones(in);
        }

        @Override
        public Headphones[] newArray(int size) {
            return new Headphones[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(brand);
        parcel.writeString(name);
        parcel.writeString(price);
    }
}

```


//Headphones.aidl
```
package com.weshape3d.ipcdemo;

 parcelable Headphones;

```
// HeadphonesManager.aidl

```
package com.weshape3d.ipcdemo;

import com.weshape3d.ipcdemo.Headphones;
import  com.weshape3d.ipcdemo.IArrivedListener;
interface HeadphonesManager{
   void addHeadphones(in Headphones hp);
    List<Headphones> getHeadphoneList();
    void registListener(IArrivedListener listener);
    void unRegistListener(IArrivedListener listener);
}

```
// IArrivedListener.aidl

```
package com.weshape3d.ipcdemo;
import com.weshape3d.ipcdemo.Headphones;
interface IArrivedListener {
   void onArrived(in Headphones hp);
}

```
写完这些build一下就可以在app/build/generated/aidl/debug下看到aidl工具帮我们生成好的java类了。我们就可以使用。
### 服务端具体实现
 服务端实现Binder.stub方法然后在service中返回。

#### 解注册失效
原因是，我们通过Binder传递到服务端的时候会产生两个全新的对象。对象跨进程传输的本质是反序列化的过程。

系统给提供了RemoteCallBackList用于夸进程的listener解注册。

#### 服务端注意事项
- 在服务端实现的aidl方法会运行在服务端的线程池中，当多个客户端连接服务端的时候，需要注意线程安全。
- 服务端的方法是运行在线程池当中的所以不怕开启耗时操作。

```
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

```

### 客户端具体实现

```
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

```

#### 客户端注意事项
- 调用远程方法的时候，需要注意不要放在主线程中容易一起ANR。

```
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
```
### 向服务端注册监听
按照常规的注册监听的方式向服务端注册监听，会在解除监听的时候出现问题。原因在于Binder会把客户端传递过来的对象重新转换成新的对象，其实本质上跨进程传输就是反序列化的过程。
系统给我们提供了RemoteCallbackList，专门用于注册删除跨进程的listener。

#### 使用RemoteCallbackList
RemoteCallbackList的使用必须beginBroadcast和finishBroadcast配对进行。如下

```
final int N =  mListenerList.beginBroadcast();
for（int i=0;i<N;i++）{
    Listener l = mListenerList.getBroadcastItem(i)
    if(l!=null){
        l.callBack();//调用监听
    }
}
mListenerList.finishBroadcast();
```
在我们demo中的具体使用可以上上面的代码实例或者github上的代码：https://github.com/poecook/IPCDemo


# 问题汇总
- 当aidl下的文件结构跟主文件结构不一致的时候 aidl编译不通过
