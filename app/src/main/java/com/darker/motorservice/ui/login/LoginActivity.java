package com.darker.motorservice.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.ui.instruction.AboutUseActivity;
import com.darker.motorservice.ui.reset_password.ResetPasswordActivity;
import com.darker.motorservice.ui.main.MainActivity;
import com.darker.motorservice.utility.EncodedUtil;
import com.darker.motorservice.utility.LoadServiceUtil;
import com.darker.motorservice.utility.NetworkUtil;
import com.darker.motorservice.ui.login.model.UserItem;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.widget.Toast.LENGTH_LONG;
import static com.darker.motorservice.utility.Constant.EMAIL;
import static com.darker.motorservice.utility.Constant.ID;
import static com.darker.motorservice.utility.Constant.KEY_LOGIN_MOTOR_SERVICE;
import static com.darker.motorservice.utility.Constant.NAME;
import static com.darker.motorservice.utility.Constant.PASSWORD;
import static com.darker.motorservice.utility.Constant.PHOTO;
import static com.darker.motorservice.utility.Constant.SERVICE;
import static com.darker.motorservice.utility.Constant.STATUS;
import static com.darker.motorservice.utility.Constant.TYPE;
import static com.darker.motorservice.utility.Constant.USER;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPass;
    private ProgressBar progressBar, pb;
    private String username, password;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private LoginButton loginButton;
    private Button btnLogin;
    private ImageButton close;
    private TextView fb, logSer, aboutMe, howUse;

    private static final String TAG = "FacebookLogin";
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        mAuth = FirebaseAuth.getInstance();
        sharedPref = getSharedPreferences(KEY_LOGIN_MOTOR_SERVICE, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        setup();
    }

    private void setup(){
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fb = (TextView) findViewById(R.id.fb);
        logSer = (TextView) findViewById(R.id.login_service);
        aboutMe = (TextView) findViewById(R.id.about_me);
        howUse = (TextView) findViewById(R.id.how_use);

        mCallbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                on();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                on();
            }
        });

        String status = sharedPref.getString(STATUS, "");

        AccessToken token;
        token = AccessToken.getCurrentAccessToken();
        if (token != null) {
            //Means user is logged in
            loadDB();
            getProfile();
            return;
        }

        if (!status.isEmpty()) {
            loadDB();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void onFacebookClicked(View view){
        if (NetworkUtil.disable(this)){
            Toast.makeText(view.getContext(), "ข้อผิดพลาดเครือข่าย! ไม่สามารถเข้าสู่ระบบได้", LENGTH_LONG).show();
            return;
        }
        off();
        loginButton.performClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            loadDB();
                            getProfile();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "ไม่สามารถเข้าสู่ระบบได้! โปรดลองอีกครั้ง", Toast.LENGTH_SHORT).show();
                        }
                        on();
                    }
                });
    }

    private void getProfile(){
        Profile profile = Profile.getCurrentProfile();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        String uid = mAuth.getCurrentUser().getUid();
        String photo = profile.getId();
        Log.d("Facebook photo", photo);
        db.child(USER).child(uid).setValue(new UserItem(profile.getName(), photo));

        editor.putString(STATUS, "user");
        editor.putString(ID, uid);
        editor.putString(NAME, profile.getName());
        editor.putString(PHOTO, photo);
        editor.commit();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void loadDB(){
        new LoadServiceUtil(this).loadData();
        new LoadServiceUtil(this).loadAdmin();
    }

    private void on(){
        fb.setClickable(true);
        logSer.setClickable(true);
        aboutMe.setClickable(true);
        howUse.setClickable(true);
        progressBar.setVisibility(View.GONE);
    }

    private void off(){
        fb.setClickable(false);
        logSer.setClickable(false);
        aboutMe.setClickable(false);
        howUse.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void onServiceLoginClicked(View view){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View vb = inflater.inflate(R.layout.login_service, null);
        builder.setView(vb);
        builder.setCancelable(false);

        inputEmail = (EditText) vb.findViewById(R.id.txt_email);
        inputPass = (EditText) vb.findViewById(R.id.txt_password);
        pb = (ProgressBar) vb.findViewById(R.id.progressBar);
        btnLogin = (Button) vb.findViewById(R.id.btn_login);
        close = (ImageButton) vb.findViewById(R.id.close);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLogin.setClickable(false);
                close.setClickable(false);
                checkLogin();
            }
        });
        final AlertDialog d = builder.show();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
    }

    private void checkLogin(){
        username = inputEmail.getText().toString();
        password = inputPass.getText().toString();

        if (username.isEmpty()){
            String msg = password.isEmpty() ? "กรุณากรอกอีเมลและรหัสผ่าน!" : "กรุณากรอกอีเมล!";
            alert(msg);
            return;
        }

        if (password.isEmpty()){
            alert("กรุณากรอกรหัสผ่าน!");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            alert("กรุณากรอกอีเมลที่ถูกต้อง!");
            return;
        }

        if (NetworkUtil.disable(this)){
            alert("ข้อผิดพลาดเครือข่าย! ไม่สามารถเข้าสู่ระบบได้");
            inputPass.setText("");
            return;
        }

        login();
    }

    private void login(){
        pb.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            // there was an error
                            alert("อีเมลหรือรหัสผ่านไม่ถูกต้อง");
                            inputPass.setText("");
                            pb.setVisibility(View.GONE);
                            Log.w("Login : ", task.getException());
                        } else {
                            loadDB();
                            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                            db.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String id = mAuth.getCurrentUser().getUid();
                                    editor.putString(EMAIL, mAuth.getCurrentUser().getEmail());
                                    editor.putString(PASSWORD, (new EncodedUtil(password)).getResult());
                                    editor.putString(STATUS, SERVICE);
                                    editor.putString(ID, id);
                                    editor.commit();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                    pb.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        }
                    }
                });
    }

    private void alert(String msg){
        Toast.makeText(this, msg, LENGTH_LONG).show();
        btnLogin.setClickable(true);
        close.setClickable(true);
    }

    public void onBtnForgetPasswordClicked(View view){
        startActivity(new Intent(this, ResetPasswordActivity.class));
    }

    public void aboutMe(View view){
        Intent intent = new Intent(this, AboutUseActivity.class);
        intent.putExtra(TYPE, "about");
        startActivity(intent);
    }

    public void howToUse(View view){
        Intent intent = new Intent(this, AboutUseActivity.class);
        intent.putExtra(TYPE, "use");
        startActivity(intent);
    }
}