package org.sx.framework.seventbus;

/**
 * Created by shenx on 15/12/11.
 */
public abstract class EventBusTask {

    private Exception e;
    private volatile boolean cancelled=false;

    /**
     * 需要异步执行的具体工作
     */
    public abstract  void doTask();

    /**
     * {@link #finish()} 在什么线程下执行
     * @return
     */
    public abstract ThreadMode getFinishThreadMode();

    /**
     * {@link #doTask()}在异步线程中执行完成之后需要执行的函数
     */
    public abstract void finish();

    public abstract void finishWithError(Exception e);

    protected final void onException(Exception e){
        this.e=e;
    }

    public final void cancel(){
        cancelled=true;
    }

    public final boolean isCancelled(){
        return cancelled;
    }

    public final void doFinish(){
        if(isCancelled()){
            return;
        }
        if(e==null){
            finish();
        }else{
            finishWithError(e);
        }
    }
}
