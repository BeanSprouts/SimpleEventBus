package org.sx.framework.seventbus;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by shenx on 15-6-30.
 */
public class ReceiverWrapper {


    public WeakReference<Object> receiverRef;

    public ReceiverMeta meta;

    public boolean receive(Object event) throws InvocationTargetException, IllegalAccessException {
        Object receiver = receiverRef.get();
        if(receiver==null)return false;
        switch (meta.hasMsgParam) {
            case YES:
                meta.method.invoke(receiver, event);
                break;
            case NO:
                meta.method.invoke(receiver);
                break;
        }
        return true;
    }
}
