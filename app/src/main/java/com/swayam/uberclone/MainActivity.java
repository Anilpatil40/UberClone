package com.swayam.uberclone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.nikartm.button.FitButton;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.random.customdialog.CustomDialog;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

public class MainActivity extends AppCompatActivity {
    private Mode currentMode;
    private FitButton loginSignupButton;
    private EditText usernameField,passwordField,passangerOrDriverEditText;
    private ScrollView scrollView;
    private RadioGroup radioGroup;

    enum Mode{
        SIGNUP,LOGIN
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation();

        loginSignupButton = findViewById(R.id.loginSignupButton);
        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        passangerOrDriverEditText = findViewById(R.id.passangerOrDriverEditText);
        scrollView = findViewById(R.id.scrollView);
        radioGroup = findViewById(R.id.radioGroup);

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

    public void loginOrSignup(View view) {

        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String who = null;

        if (username.equals("") || password.equals("")){
            showError("username and password can not be empty");
            return;
        }

        if (radioGroup.getCheckedRadioButtonId() == R.id.passangerRadioButton){
            who = "Passanger";
        }else if (radioGroup.getCheckedRadioButtonId() == R.id.driverRadioButton){
            who = "Driver";
        }else {
            showError("choose Passanger or Driver");
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("please wait...");
        dialog.show();

        //login mode
        if (currentMode == Mode.LOGIN){
            String finalWho = who;
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    dialog.dismiss();
                    if (e == null){
                        if (finalWho.equals("Passanger"))
                            transitionToPassangerActivity();
                        else
                            transitionToDriverActivity();
                    }else{
                        showError(e.getMessage());
                    }
                }
            });
        }
        //sign up mode
        else if (currentMode == Mode.SIGNUP){
            ParseUser parseUser = new ParseUser();
            parseUser.setUsername(username);
            parseUser.setPassword(password);
            parseUser.put("who",who);

            parseUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    dialog.dismiss();
                    if (e == null){
                        showSuccessfulMessage("User created Successfully");
                    }else {
                        showError(e.getMessage());
                    }
                }
            });
        }
    }

    public void loginAnonymously(View view) {
        String who = passangerOrDriverEditText.getText().toString();
        ParseUser parseUser = new ParseUser();
        parseUser.put("who",who);

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("please wait...");
        dialog.show();

        if (who.equals("Passanger") || who.equals("Driver")){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    dialog.dismiss();
                    if (e == null){
                        if (who.equals("Passanger")){
                            transitionToPassangerActivity();
                        }else {
                            transitionToDriverActivity();
                        }
                    }else {
                        showError(e.getMessage());
                    }
                }
            });
        }
        else {
            dialog.dismiss();
            showError("type 'Passanger' or 'Driver'");
        }
    }

    private void transitionToPassangerActivity(){
        startActivity(new Intent(this,PassangerActivity.class));
        finish();
    }

    private void transitionToDriverActivity(){
        startActivity(new Intent(this,DriverActivity.class));
        finish();
    }

    private void showError(String message){
        CustomDialog dialog = new CustomDialog(this,CustomDialog.FAILURE);
        dialog.setTitle("FAILED");
        dialog.setContentText(message);
        dialog.show();
    }

    private void showSuccessfulMessage(String message){
        CustomDialog dialog = new CustomDialog(this,CustomDialog.SUCCESS);
        dialog.setTitle("SUCCESSFUL");
        dialog.setContentText(message);
        dialog.show();
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