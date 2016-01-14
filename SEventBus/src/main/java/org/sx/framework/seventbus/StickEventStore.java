package org.sx.framework.seventbus;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shenx on 16/1/14.
 */
public class StickEventStore {


    protected ConcurrentHashMap<String,Object> eventMap=new ConcurrentHashMap<>();

    public void newStickEvent(String eventType,Object event){
        eventMap.put(eventType,event);
    }

    public Object getStickEvent(String eventType){
        return eventMap.get(eventType);
    }
}
