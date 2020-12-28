package com.swayam.uberclone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.nikartm.button.FitButton;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

public class MainActivity extends AppCompatActivity {
    private Mode currentMode;
    private FitButton loginSignupButton;
    private EditText username,password,passangerOrDriverEditText;
    private ScrollView scrollView;
    
    enum Mode{
        SIGNUP,LOGIN
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginSignupButton = findViewById(R.id.loginSignupButton);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        passangerOrDriverEditText = findViewById(R.id.passangerOrDriverEditText);
        scrollView = findViewById(R.id.scrollView);

        currentMode = Mode.LOGIN;

        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (isOpen){
                    if (passangerOrDriverEditText.hasFocus()){
                        scrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(View.FOCUS_DOWN);
                            }
                        });
                    }
                }
            }
        });
    }



    public void switchMode(MenuItem view){
        if (currentMode == Mode.LOGIN){
            currentMode = Mode.SIGNUP;
            view.setTitle("Log In");
            loginSignupButton.setText("Sign Up");
        }else if (currentMode == Mode.SIGNUP){
            currentMode = Mode.LOGIN;
            view.setTitle("Sign Up");
            loginSignupButton.setText("Log In");
        }
        TextView textView = (TextView) loginSignupButton.getChildAt(0);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }
}