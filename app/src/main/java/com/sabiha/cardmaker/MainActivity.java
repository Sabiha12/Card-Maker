package com.sabiha.cardmaker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sabiha.cardmaker.databinding.ActivityMainBinding;
import com.sabiha.cardmaker.util.Constant;

public class MainActivity extends BaseActivity {
    ActivityMainBinding activityMainBinding;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        init();
        email = sharedPrefStore.getString(Constant.EMAIL);

        if (email != null) {
            gotoHomeActivity();
        }

        // sharedPrefStore.getString(Constant.PASSWORD);
        // hideSoftKeyboard();
    }

    private void init() {
        activityMainBinding.btnLogin.setOnClickListener(this);
        activityMainBinding.btnSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                goToLogInActivity();
                break;
            case R.id.btnSignup:
                goToSignUpActivity();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //hideSoftKeyboard();
        //hideSoftwareKeyboard(MainActivity.this);
    }

    private void gotoHomeActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    private void goToLogInActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToSignUpActivity() {
        Intent intent = new Intent(MainActivity.this, SignupActivity.class);
        startActivity(intent);
    }

}