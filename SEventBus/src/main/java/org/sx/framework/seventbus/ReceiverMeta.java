package org.sx.framework.seventbus;

import java.lang.reflect.Method;

/**
 * Created by shenx on 15-6-26.
 */
public class ReceiverMeta {

    public Class<?> clazz;

    public Method method;

    public String eventType;

    public ThreadMode threadMode;

    public HasMsgParam hasMsgParam= HasMsgParam.NO;

    /**
     * 是否接收消息参数
     */
    public static enum HasMsgParam{
        YES,
        NO
    }
}
