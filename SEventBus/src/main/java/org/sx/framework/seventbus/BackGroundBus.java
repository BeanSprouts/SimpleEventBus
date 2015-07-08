package org.sx.framework.seventbus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.CountDownLatch;

/**
 * Created by shenx on 15-6-27.
 */
public class BackGroundBus {
    private Looper looper;
    private final Thread thread;
    private Handler handler;
    private final EventBus eventBus;
    private final CountDownLatch cdl=new CountDownLatch(1);

    public BackGroundBus(final EventBus eventBus){
        this.eventBus=eventBus;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                BackGroundBus.this.looper = Looper.myLooper();
                BackGroundBus.this.handler=new Handler(looper){
                    @Override
                    public void handleMessage(Message msg) {
                        if(msg.obj!=null&&msg.obj instanceof DelayedEvent){
                            DelayedEvent delayedEvent=(DelayedEvent)msg.obj;
                            eventBus.post(delayedEvent.event,delayedEvent.eventType);
                        }
                    }
                };
                cdl.countDown();
                Looper.loop();
            }
        });
        thread.start();
    }

    public void postEventDelayed(Object event,String eventType,long millis) throws InterruptedException{
        cdl.await();
        DelayedEvent delayedEvent=new DelayedEvent();
        delayedEvent.event=event;
        delayedEvent.eventType=eventType;
        Message msg=handler.obtainMessage();
        msg.obj=delayedEvent;
        handler.sendMessageDelayed(msg, millis);
    }

    public void stop() throws InterruptedException {
        cdl.await();
        looper.quit();
    }
}
