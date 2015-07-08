package org.sx.framework.seventbus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by shenx on 15-6-30.
 */
public class ReceiverStorage {

    /**
     * key:EventType
     * value:List of receivers
     */
    private final ConcurrentHashMap<String,CopyOnWriteArrayList<ReceiverWrapper>> receiverMap=new ConcurrentHashMap<String,CopyOnWriteArrayList<ReceiverWrapper>>();

    public void addReceiver(Object receiver,List<ReceiverMeta> metas){
        WeakReference<Object> receiverRef=new WeakReference<Object>(receiver);
        for(ReceiverMeta meta:metas){
            ReceiverWrapper wrapper=new ReceiverWrapper();
            wrapper.meta=meta;
            wrapper.receiverRef=receiverRef;
            addReceiverWrapper(wrapper);
        }
    }

    private void addReceiverWrapper(ReceiverWrapper wrapper){
        CopyOnWriteArrayList<ReceiverWrapper> list=receiverMap.get(wrapper.meta.eventType);
        if(list==null){
            list=new CopyOnWriteArrayList<ReceiverWrapper>();
            list.add(wrapper);
        }
        CopyOnWriteArrayList<ReceiverWrapper> temp= receiverMap.putIfAbsent(wrapper.meta.eventType, list);
        if(temp!=null){
            temp.add(wrapper);
        }
    }

    public void removeReceiver(Object receiver,List<ReceiverMeta> metas){
        for(ReceiverMeta meta:metas){
            CopyOnWriteArrayList<ReceiverWrapper> temp=receiverMap.get(meta.eventType);
            List<ReceiverWrapper> removeList=new ArrayList<ReceiverWrapper>();
            if(temp!=null) {
                for (ReceiverWrapper warpper : temp) {
                    Object receiverTemp=warpper.receiverRef.get();
                    if(receiverTemp==null) {
                        removeList.add(warpper);
                    }else if (receiverTemp.equals(receiver)) {
                        removeList.add(warpper);
                    }
                }
                for (ReceiverWrapper warpper : removeList) {
                    temp.remove(warpper);
                }
            }
        }
    }

    public void removeWrapper(ReceiverWrapper wrapper){
        CopyOnWriteArrayList<ReceiverWrapper> temp=receiverMap.get(wrapper.meta.eventType);
        if(temp!=null){
            temp.remove(wrapper);
        }
    }

    public List<ReceiverWrapper> getReceivers(String eventType){
        return  receiverMap.get(eventType);
    }
}
