package org.sx.framework.seventbus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shenx on 15-6-30.
 */
public class ReceiverParser {
    private static final ConcurrentHashMap<Class<?>,List<ReceiverMeta>> META_CACHE=new ConcurrentHashMap<Class<?>,List<ReceiverMeta>>();

    public static List<ReceiverMeta> parse(Object obj){
        Class<?> clazz=obj.getClass();
        List<ReceiverMeta> metas=META_CACHE.get(clazz);
        if(metas!=null){
            return metas;
        }else{
            metas=new ArrayList<ReceiverMeta>();
            Method[] methods=  clazz.getMethods();
            for(Method method:methods){
                OnEvent annotation= method.getAnnotation(OnEvent.class);
                if(annotation!=null){
                    int modifiers = method.getModifiers();
                    if((modifiers& Modifier.PUBLIC)==0){
                        continue;//not a public method
                    }
                    Class<?>[] paramTypes=method.getParameterTypes();
                    ReceiverMeta meta=new ReceiverMeta();
                    meta.clazz=clazz;
                    meta.eventType=annotation.eventType();
                    meta.threadMode=annotation.threadMode();
                    if(paramTypes==null||paramTypes.length==0){
                        meta.method=method;
                        meta.hasMsgParam= ReceiverMeta.HasMsgParam.NO;
                        metas.add(meta);
                    }else if(paramTypes!=null&&paramTypes.length==1){
                        if(paramTypes[0].equals(Object.class)){
                            meta.method=method;
                            meta.hasMsgParam= ReceiverMeta.HasMsgParam.YES;
                            metas.add(meta);
                        }
                    }
                }
            }
            List<ReceiverMeta> temp= META_CACHE.putIfAbsent(clazz, metas);
            if(temp!=null)return temp;
            return metas;
        }
    }
}
