package com.sevenheaven.curtain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by caifangmao on 15/3/26.
 */
public class CurtainMenuActivity extends AppCompatActivity {

    private CurtainContentLayout contentLayout;


    private void initialActionbar(ActionBar actionBar) {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            try {// api version
            } catch (NoSuchMethodError e) {
            }
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        } else {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            contentLayout.toggle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        contentLayout = (CurtainContentLayout) findViewById(R.id.root);
        contentLayout.setContent(findViewById(R.id.mdedit_text));
        contentLayout.setCallback(new Callback() {
            @Override
            public void onCurtainOpen() {

            }

            @Override
            public void onOpenStart() {
//                Intent intent = new Intent(CurtainMenuActivity.this, CurtainMenuActivity2.class);
//                startActivity(intent);
//                overridePendingTransition(0, 0);
                Log.e("yangxj", "onOpenStart(CurtainMenuActivity.java:55) ");
            }

            @Override
            public void onCloseStart() {
                Log.e("yangxj", "onCloseStart(CurtainMenuActivity.java:61) ");
            }

            @Override
            public void onCurtainClose() {

            }
        });

        initialActionbar(getSupportActionBar());
    }
}
