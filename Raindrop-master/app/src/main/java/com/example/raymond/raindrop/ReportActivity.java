package com.example.raymond.raindrop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.raymond.raindrop.persistence.AsyncWriter;
import com.example.raymond.raindrop.persistence.reports.ReportRepository;
import com.example.raymond.raindrop.persistence.reports.ReportStorable;

public class ReportActivity extends AppCompatActivity {
    private String userEmail = null;
    private String postId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        onResume();
        cancel();
        submit();
    }

    @Override
    public void onResume(){
        super.onResume();

        System.out.println("joe is one sexy mofo.");

        // Get email
        if( getIntent().getExtras() != null)
        {
            userEmail = getIntent().getStringExtra("userEmail");
            postId = getIntent().getStringExtra("postId");
        }

        System.out.println("we passed the user email:" + userEmail);
    }

    private void cancel() {
        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToMaps();
            }
        });
    }

    private void submit() {
        Button btnSubmit = (Button)findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });
    }

    public void handleSubmit() {
        System.out.println("Entered handle submit");
        AsyncWriter.write(ReportRepository.getTableName(),
                new ReportStorable( generateReportId(),
                postId,
                userEmail,
                ((EditText) findViewById(R.id.etMessage)).getText().toString())
                        .generateMapItem());
        switchToMaps();
    }

    public void switchToMaps() {
        // Sending over userEmail to access the MapActivity
        Intent intent = new Intent(ReportActivity.this, MapsActivity.class);
        intent.putExtra("userEmail", userEmail);// if its string type
        startActivity(intent);
    }

    public String generateReportId() {
        return postId + "/" + Long.toString(System.currentTimeMillis());
    }
}
