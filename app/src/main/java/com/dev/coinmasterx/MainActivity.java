package com.dev.coinmasterx;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    // We want at least 2 threads and at most 4 threads in the core pool,
    // preferring to have 1 less than the CPU count to avoid saturating
    // the CPU with background work
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText inviteURL = (EditText) findViewById(R.id.invite_link);
        final EditText count = (EditText) findViewById(R.id.Input_count);
        inviteURL.setText("https://GetCoinMaster.com/~r0MZh"); //fix inviteLink
        Button Go_spin = (Button) findViewById(R.id.spin_pump);

        Go_spin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String link =  inviteURL.getText().toString();
                int inviteCount = 0;
                    try {
                        inviteCount = Integer.parseInt(count.getText().toString());
                    }catch (NumberFormatException e){
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                if(link.isEmpty() && inviteCount == 0){
                    Snackbar.make(getWindow().getDecorView().getRootView(), "link or count is Empty", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                new coinmaster_api(link);


                AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("ปั้มสปินเรียบร้อย")
                        .setMessage("ขอบคุณที่ใช้บริการ")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_dialer)
                        .show();

            }
        });
    }
}