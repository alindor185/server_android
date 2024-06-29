package com.example.youtubepart1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvLoginError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvLoginError = findViewById(R.id.tvLoginError);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvRegister = findViewById(R.id.tvRegister);

        setSpannableText(tvForgotPassword, "Forgot your password? ", "Click Here", ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.youtubeRed), v -> {
            // Handle forgot password click
        });

        setSpannableText(tvRegister, "Don't have an account? ", "Sign Up", ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.youtubeRed), v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    User existingUser = AppDatabase.getDatabase(MainActivity.this).userDao().getUser(username);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (existingUser != null && existingUser.password.equals(password)) {
                                tvLoginError.setVisibility(TextView.GONE);  // Hide error message
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                intent.putExtra("user", existingUser);
                                startActivity(intent);
                                finish();
                            } else {
                                tvLoginError.setVisibility(TextView.VISIBLE);  // Show error message
                            }
                        }
                    });
                }
            }).start();
        });
    }

    private void setSpannableText(TextView textView, String normalText, String clickableText, int normalTextColor, int clickableTextColor, View.OnClickListener onClickListener) {
        SpannableString spannableString = new SpannableString(normalText + clickableText);

        spannableString.setSpan(new ForegroundColorSpan(normalTextColor), 0, normalText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                onClickListener.onClick(widget);
            }
        };

        spannableString.setSpan(clickableSpan, normalText.length(), spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(clickableTextColor), normalText.length(), spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());  // This makes the link clickable
    }
}
