package org.sx.framework.seventbus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by shenx on 15-6-27.
 */
public final class HandlerPoster{

    public final static class PostHandler extends Handler {

        private final EventBus curBus;
        public PostHandler(Looper looper,EventBus curBus){
            super(looper);
            this.curBus=curBus;
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.obj!=null&&msg.obj instanceof EventMessage){
                EventMessage eventMessage=(EventMessage)msg.obj;
                try {
                    eventMessage.wrapper.receive(eventMessage.event);
                } catch (Exception e) {
                    curBus.onException(e);
                }
            }else if(msg.obj!=null&&msg.obj instanceof EventBusTask){
                EventBusTask task=(EventBusTask)msg.obj;
                try {
                    task.doFinish();
                } catch (Exception e) {
                    curBus.onException(e);
                }
            }
        }
    }

    public final static class EventMessage{
        public EventMessage(ReceiverWrapper wrapper,Object event){
            this.wrapper=wrapper;
            this.event=event;
        }
        public ReceiverWrapper wrapper;
        public Object event;
    }


    private final PostHandler handler;
    private final Looper looper;


    public HandlerPoster(Looper looper,EventBus curBus){
        this.looper=looper;
        handler=new PostHandler(looper,curBus);
    }

    public void deliverEvent(ReceiverWrapper wrapper,Object event){
        Message msg=handler.obtainMessage();
        msg.obj=new EventMessage(wrapper,event);
        handler.sendMessage(msg);
    }

    public void deliverTask(EventBusTask task){
        Message msg=handler.obtainMessage();
        msg.obj=task;
        handler.sendMessage(msg);
    }
}
