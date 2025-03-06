package me.jin.note.activity.login;

import static me.jin.note.activity.MainActivity.ip;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import me.jin.note.R;
import me.jin.note.activity.MainActivity;
import me.jin.note.model.LoginResult;
import me.jin.note.bean.UserInfo;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private ProgressDialog mDialog;
    private Button btnLogin;
    private TextView txtCode;
    private TextView txtRegister;
    private TextView txtForget;
    private EditText edtUserId;
    private EditText edtPassword;
    private ImageView eyeImageView;
    private String password;
    private String url="http://"+ip+"/Note/user/login";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String result = (String) msg.obj;
                    Gson gson = new Gson();
                    LoginResult loginResult = gson.fromJson(result, LoginResult.class);
                    int resultCode = loginResult.getResultCode();
                    String message = loginResult.getMsg();
                    // 根据 resultCode 判断登录是否成功
                    if (resultCode == 1) {
                        // 登录成功，跳转到 MainActivity 页面

                        //存id
                        String userId = loginResult.getUserId();
                        String userName = loginResult.getUserName();
                        String userPhoneNumber=loginResult.getUserPhoneNumber();
                        String email=loginResult.getEmail();
                        SharedPreferences sharedPreferences = getSharedPreferences("userName_and_userId", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userName", userName);
                        editor.putString("userId", userId);
                        editor.putString("userPhoneNumber",userPhoneNumber);
                        editor.putString("email",email);
                        editor.putString("status","1");
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                        return;
                    } else {
                        // 登录失败，显示提示消息
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //绑定控件
        edtUserId =findViewById(R.id.edt_account);
        edtPassword =findViewById(R.id.edt_password);
        btnLogin =findViewById(R.id.btn_login);
        txtCode =findViewById(R.id.txt_code);
        txtRegister=findViewById(R.id.txt_register);
        txtForget=findViewById(R.id.txt_forget);
        eyeImageView = findViewById(R.id.img_eye);

        eyeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage();
            }
        });
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取用户名和密码
                String userId = edtUserId.getText().toString();
                try {
                    //MD5加密密码
                    String password1 = edtPassword.getText().toString();
                    MessageDigest messageDigest=MessageDigest.getInstance("MD5");
                    byte[] plainTextBytes=password1.getBytes();
                    messageDigest.update(plainTextBytes);
                    byte[] encryptedBytes=messageDigest.digest();
                    BigInteger bigInteger=new BigInteger(1,encryptedBytes);
                    password=String.format("%032x",bigInteger);

                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

                if (userId.length() == 0 && password.length() == 0) {
                    Toast.makeText(LoginActivity.this, "输入的用户名和密码都为空", Toast.LENGTH_LONG).show();
                } else if (userId.length() == 0) {
                    Toast.makeText(LoginActivity.this, "输入的用户名为空", Toast.LENGTH_LONG).show();
                } else if (password.length() == 0) {
                    Toast.makeText(LoginActivity.this, "输入的密码为空", Toast.LENGTH_LONG).show();
                } else if (password.length() < 6 && password.length() > 1) {
                    Toast.makeText(LoginActivity.this, "输入的密码小于六位数", Toast.LENGTH_LONG).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UserInfo user = new UserInfo(userId,password);
                            // 使用 Gson 将 User 对象转换为 JSON 数据

                            try {
                                Gson gson = new Gson();
                                String jsonString = gson.toJson(user);
                                OkHttpClient client = new OkHttpClient();//创建Http客户端
                                Request request = new Request.Builder()
                                        .url(url)//***.***.**.***为本机IP，xxxx为端口，/  /  为访问的接口后缀
                                        .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"),jsonString))
                                        .build();//创建Http请求
                                // 发送 POST 请求，并发送 JSON 数据
                                Response response = client.newCall(request).execute();//执行发送的指令
                                final String responseData = response.body().string();//获取返回的结果

                                // 通过 Handler 或者 EventBus 将结果传回主线程
                                Message message = Message.obtain();
                                message.what = 1;
                                message.obj = responseData;
                                mHandler.sendMessage(message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }
    private void changeImage() {
        // 切换密码可见性
        boolean isPasswordVisible = (edtPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
        edtPassword.setInputType(isPasswordVisible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        // 切换眼睛状态
        int eyeIconResId = isPasswordVisible ? R.drawable.baseline_visibility_off_black_48 : R.drawable.baseline_visibility_black_48;
        eyeImageView.setImageResource(eyeIconResId);

        // 将光标移到最后
        edtPassword.setSelection(edtPassword.getText().length());

        // 强制刷新视图
        eyeImageView.postInvalidate();
    }



}