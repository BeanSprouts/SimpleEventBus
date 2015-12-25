package org.sx.framework.seventbus;

/**
 * Created by shenx on 15-6-30.
 */
public enum ThreadMode {
    /**
     * deliver event in main thread(UI thread)
     */
    MainThread,
    /**
     * deliver event in thread that post the event.
     *
     */
    PostThread,
    /**
     * deliver event in background threads,
     * not the post one and main thread
     */
    Async
}
