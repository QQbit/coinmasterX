package com.dev.coinmasterx;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText inviteURL = (EditText) findViewById(R.id.invite_link);
        final EditText count = (EditText) findViewById(R.id.Input_count);

        Button Go_spin = (Button) findViewById(R.id.spin_pump);

        Go_spin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link = inviteURL.getText().toString();
                int inviteCount = 0;
                    try {
                        inviteCount = Integer.parseInt(count.getText().toString());
                    }catch (NumberFormatException e){
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                if(link.isEmpty() && inviteCount == 0){
                    Toast.makeText(getApplicationContext(),"link or count is Empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                new coinmaster_api(
                        link,
                        inviteCount);
            }
        });
    }
}