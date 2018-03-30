// HeadphonesManager.aidl
package com.weshape3d.ipcdemo;

import com.weshape3d.ipcdemo.Headphones;
import  com.weshape3d.ipcdemo.IArrivedListener;
interface HeadphonesManager{
   void addHeadphones(in Headphones hp);
    List<Headphones> getHeadphoneList();
    void registListener(IArrivedListener listener);
    void unRegistListener(IArrivedListener listener);
}
