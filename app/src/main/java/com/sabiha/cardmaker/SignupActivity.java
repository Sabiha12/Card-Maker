package com.sabiha.cardmaker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sabiha.cardmaker.databinding.ActivitySignupBinding;
import com.sabiha.cardmaker.firebase_model.Users;
import com.sabiha.cardmaker.util.Constant;

public class SignupActivity extends BaseActivity {
    public ActivitySignupBinding activitySignUpBinding;
    String name, email, password, emailPattern, hashPassword;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySignUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        init();
    }

    private void init() {
        setSupportActionBar(activitySignUpBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activitySignUpBinding.btnSignup.setOnClickListener(this);
        activitySignUpBinding.rlSignUp.setOnClickListener(this);

        activitySignUpBinding.etUserName.addTextChangedListener(textWatcher);
        activitySignUpBinding.etEmail.addTextChangedListener(textWatcher);
        activitySignUpBinding.etPassword.addTextChangedListener(textWatcher);
        //showKeyboard(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignup:
                if (formValidation()) {
                    createAccount();
                    //showToast("checked");
                }
                break;
        }
    }


    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            name = (activitySignUpBinding.etUserName.getText().toString().trim());
            email = (activitySignUpBinding.etEmail.getText().toString().trim());
            password = (activitySignUpBinding.etPassword.getText().toString().trim());
            if (name.length() > 0) {
                activitySignUpBinding.etNameInputLayout.setError(null);
            }
            if (email.length() > 0) {
                activitySignUpBinding.etEmailInputLayout.setError(null);
            }
            if (password.length() >= 6) {
                activitySignUpBinding.etPasswordInputLayout.setError(null);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            hideSoftKeyboard();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void createAccount() {
        startProgressDialog();
        name = activitySignUpBinding.etUserName.getText().toString();
        email = activitySignUpBinding.etEmail.getText().toString();
        password = activitySignUpBinding.etPassword.getText().toString();
        hashPassword = getSha256Pass(password);

        signUp(name, email, hashPassword);
    }

    private void signUp(String name, String email, String password) {
        final Users users = new Users();
        users.setName(name);
        users.setEmail(email);
        users.setPassword(password);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();//To set Display name
                    firebaseUser.updateProfile(profileUpdates);
                    users.setUser_id(firebaseUser.getUid());
                    sharedPrefStore.saveString(Constant.UID, firebaseUser.getUid());
                    mDatabase.child("users").child(firebaseUser.getUid()).setValue(users);
                    stopProgressDialog();
                    showToast("User is added");
                    mAuth.signOut();
                    gotoLoginActivity();
                } else {
                    stopProgressDialog();
                    messageAlert("Error occurred : " + task.getException());
                    mAuth.signOut();
                }
            }

        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    users.setName(user.getDisplayName());//To set Display name after user added
                } else {
                    finish();
                }
            }
        };
    }

    private void messageAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setMessage(message);
        builder.setTitle("Message: ");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private boolean formValidation() {
        email = activitySignUpBinding.etEmail.getText().toString();
        password = activitySignUpBinding.etPassword.getText().toString();
        emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (activitySignUpBinding.etUserName.getText().toString().equals("")) {
            activitySignUpBinding.etNameInputLayout.setError(getString(R.string.name_required));
            activitySignUpBinding.etUserName.requestFocus();
            return false;
        }
        if (activitySignUpBinding.etEmail.getText().toString().equals("")) {
            activitySignUpBinding.etEmailInputLayout.setError(getString(R.string.email_required));
            activitySignUpBinding.etEmail.requestFocus();
            return false;
        }
        if (!email.matches(emailPattern) && email.length() > 0) {
            activitySignUpBinding.etEmailInputLayout.setError(getString(R.string.email_invalid));
            activitySignUpBinding.etEmail.requestFocus();
            return false;
        }
        if (activitySignUpBinding.etPassword.getText().toString().equals("")) {
            activitySignUpBinding.etPasswordInputLayout.setError(getString(R.string.pass_required));
            activitySignUpBinding.etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            activitySignUpBinding.etPasswordInputLayout.setError(getString(R.string.pass_length));
            activitySignUpBinding.etPassword.requestFocus();
            return false;
        }
        return true;
    }
}