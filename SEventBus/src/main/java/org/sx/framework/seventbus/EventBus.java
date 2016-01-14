package org.sx.framework.seventbus;

import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shenx on 15-6-30.
 */
public class EventBus {

    /**
     * 单例
     */
    public static class Default {
        private final static EventBus singleton = new EventBus();
        public final static EventBus get() {
            return singleton;
        }
    }

    /**
     * 异常监听器
     */
    public interface ExceptionListenner{
        void onException(Exception e);
    }

    ////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////

    private final ReceiverStorage storage = new ReceiverStorage(new ReceiverStorage.ReceiverWrapperAddedListenner() {
        @Override
        public void sendStickEvent(ReceiverWrapper wrapper) {
            Object event=stickEventStore.getStickEvent(wrapper.meta.eventType);
            if(event==null)return;
            switch (wrapper.meta.threadMode) {
                case MainThread:
                    deliverToMainThread(wrapper, event);
                    break;
                case PostThread:
                case Async:
                    deliverAsynchronized(wrapper, event);
                    break;
            }
        }
    });
    private final ExecutorService executor;
    private final HandlerPoster mainThreadPoster;
    private final BackGroundBus bgPoster;
    private ExceptionListenner exListenner;
    private final StickEventStore stickEventStore=new StickEventStore();

    public EventBus() {
        executor = Executors.newCachedThreadPool();
        mainThreadPoster = new HandlerPoster(Looper.getMainLooper(),this);
        bgPoster = new BackGroundBus(this);
    }

    public EventBus(ExecutorService executor) {
        this.executor = executor;
        mainThreadPoster = new HandlerPoster(Looper.getMainLooper(),this);
        bgPoster = new BackGroundBus(this);
    }

    public void setExListenner(ExceptionListenner listenner){
        this.exListenner=listenner;
    }

    public void register(Object receiver) {
        if(receiver==null)return;
        List<ReceiverMeta> metas = ReceiverParser.parse(receiver);
        storage.addReceiver(receiver, metas);
    }

    public void unRegister(Object receiver) {
        if(receiver==null)return;
        List<ReceiverMeta> metas = ReceiverParser.parse(receiver);
        storage.removeReceiver(receiver, metas);
    }

    public void removeReceiverWrapper(ReceiverWrapper wrapper){
        if(wrapper==null)return;
        storage.removeWrapper(wrapper);
    }

    public void postDelayed(Object event, String eventType, long mills) {
        try {
            bgPoster.postEventDelayed(event,eventType, mills);
        } catch (InterruptedException ex) {
            post(event,eventType);
        }
    }

    public void postStick(Object event,String eventType){
        stickEventStore.newStickEvent(eventType,event);
        post(event,eventType);
    }

    public void post(Object event,String eventType) {
        List<ReceiverWrapper> receiverWrappers = storage.getReceivers(eventType);
        if (receiverWrappers != null) {
            for (ReceiverWrapper wrapper : receiverWrappers) {
                switch (wrapper.meta.threadMode) {
                    case PostThread:
                        deliverThroughCurThread(wrapper, event);
                        break;
                    case MainThread:
                        deliverToMainThread(wrapper, event);
                        break;
                    case Async:
                        deliverAsynchronized(wrapper, event);
                        break;
                }
            }
        }
    }

    private void deliverThroughCurThread(final ReceiverWrapper wrapper, final Object event) {
        try {
            wrapper.receive(event);
        } catch (Exception e) {
            onException(e);
        }
    }

    private void deliverToMainThread(final ReceiverWrapper wrapper, final Object event) {
        mainThreadPoster.deliverEvent(wrapper, event);
    }

    private void deliverAsynchronized(final ReceiverWrapper wrapper, final Object event) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    wrapper.receive(event);
                } catch (Exception e) {
                    onException(e);
                }
            }
        });
    }

    public void executeTaskAsync(final EventBusTask task){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(task.isCancelled()){
                        return;
                    }
                    task.doTask();
                }catch (Exception e){
                    onException(e);
                    task.onException(e);
                }
                if(task.isCancelled()){
                    return;
                }
                ThreadMode threadMode=task.getFinishThreadMode();
                if(threadMode==null){
                    threadMode=ThreadMode.PostThread;
                }
                switch (threadMode){
                    case MainThread:
                        mainThreadPoster.deliverTask(task);
                        break;
                    default:
                        task.doFinish();
                        break;
                }
            }
        });
    }

    public void onException(final Exception e){
        if(exListenner!=null){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    exListenner.onException(e);
                }
            });
        }else{
            e.printStackTrace();
        }
    }
}
