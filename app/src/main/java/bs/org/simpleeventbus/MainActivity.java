package bs.org.simpleeventbus;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.sx.framework.seventbus.EventBus;
import org.sx.framework.seventbus.OnEvent;
import org.sx.framework.seventbus.ThreadMode;


public class MainActivity extends Activity {

    private TextView tvMainThread;
    private TextView tvPostThread;
    private TextView tvBGThread;

    private EventBus eventBus;

    private int count=0;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventBus=EventBus.Default.get();
        setContentView(R.layout.activity_main);

        tvMainThread=(TextView)findViewById(R.id.tvMain);
        tvPostThread=(TextView)findViewById(R.id.tvPost);
        tvBGThread=(TextView)findViewById(R.id.tvBackGround);

        Button btnPost=(Button)findViewById(R.id.btnPost);
        Button btnPostDelayed=(Button)findViewById(R.id.btnPostDelayed);

        eventBus.register(this);

        handler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==1){
                    tvBGThread.setText((String)msg.obj);
                }else if(msg.what==2){
                    tvPostThread.setText((String)msg.obj);
                }
            }
        };

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyEvent event=new MyEvent();
                event.count=++count;
                eventBus.post(event,"aaa");
            }
        });

        btnPostDelayed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyEvent event=new MyEvent();
                event.count=++count;
                eventBus.postDelayed(event,"aaa",2000);
            }
        });

    }


    @OnEvent(threadMode= ThreadMode.MainThread,eventType="aaa")
    public void onEventMainThread(Object e){
        MyEvent event=null;
        if(e!=null&&e instanceof MyEvent) {
            event=(MyEvent)e;
            String msgStr = "MSG:" + event.count + ":\tThread" + Thread.currentThread().hashCode();
            tvMainThread.setText(msgStr);
        }
    }

    @OnEvent(threadMode= ThreadMode.Async,eventType=EventName.EVENT_A)
    public void onEventBackGround(Object e){
        MyEvent event=null;
        if(e!=null&&e instanceof MyEvent) {
            event=(MyEvent)e;
            String msgStr = "MSG:" + event.count + ":\tThread" + Thread.currentThread().hashCode();
            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.obj = msgStr;
            handler.sendMessage(msg);
        }
    }

    @OnEvent(threadMode= ThreadMode.PostThread,eventType="aaa")
    public void onEventPost(Object e) {
        MyEvent event=null;
        if(e!=null&&e instanceof MyEvent) {
            event=(MyEvent)e;
            String msgStr = "MSG:" + event.count + ":\tThread" + Thread.currentThread().hashCode();
            Message msg = handler.obtainMessage();
            msg.what = 2;
            msg.obj = msgStr;
            handler.sendMessage(msg);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventBus.unRegister(this);
    }
}
