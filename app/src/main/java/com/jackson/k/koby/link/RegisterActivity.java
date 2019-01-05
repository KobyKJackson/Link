package com.jackson.k.koby.link;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity
{
    private EditText UserEmail, UserPassword, UserConfirmationPassword;
    private Button CreateAccountButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = findViewById(R.id.register_email_EditText);
        UserPassword = findViewById(R.id.register_password_EditText);
        UserConfirmationPassword = findViewById(R.id.register_confirm_password_EditText);
        CreateAccountButton = findViewById(R.id.register_create_account_Button);
        loadingBar = new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void CreateNewAccount()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmationPassword.getText().toString();

        if(validateForm())
        {
            loadingBar.setTitle("Create New Account");
            loadingBar.setMessage("Please wait, creating your new Account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendUserToSetupActivity();
                                Toast.makeText(RegisterActivity.this, "You are authenticated successfully....", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    }
            );
        }
    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private boolean validateForm()
    {
        boolean result = true;
        if (TextUtils.isEmpty(UserEmail.getText().toString()))
        {
            UserEmail.setError("Required");
            result = false;
        }
        else
        {
            UserEmail.setError(null);
        }

        if (TextUtils.isEmpty(UserPassword.getText().toString()))
        {
            UserPassword.setError("Required");
            result = false;
        }
        else
        {
            UserPassword.setError(null);
        }

        if (TextUtils.isEmpty(UserConfirmationPassword.getText().toString()))
        {
            UserConfirmationPassword.setError("Required");
            result = false;
        }
        else
        {
            UserConfirmationPassword.setError(null);
        }

        if(!UserConfirmationPassword.getText().toString().equals(UserPassword.getText().toString()))
        {
            UserConfirmationPassword.setError("Passwords do not match");
            UserPassword.setError("Passwords do not match");
            result = false;
        }

        //TODO: If the user is deleted from the firebase database via the console you can still login unless the data of the app is cleared. User should not be stored in memory but should be checked vs database

        return result;
    }
}
