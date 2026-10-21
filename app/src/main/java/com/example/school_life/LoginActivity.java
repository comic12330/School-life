package com.example.school_life;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.school_life.net.ApiClient;
import com.example.school_life.net.model.LoginRequest;
import com.example.school_life.net.model.User;
import com.example.school_life.util.Prefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etNo;
    private EditText etPw;
    private TextView tvError;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etNo = findViewById(R.id.et_student_no);
        etPw = findViewById(R.id.et_password);
        tvError = findViewById(R.id.tv_error);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String no = etNo.getText().toString().trim();
        String pw = etPw.getText().toString().trim();
        if (no.isEmpty() || pw.isEmpty()) {
            tvError.setVisibility(TextView.VISIBLE);
            tvError.setText("请输入学号和密码");
            return;
        }
        btnLogin.setEnabled(false);
        tvError.setVisibility(TextView.VISIBLE);
        tvError.setText("登录中...");

        ApiClient.api().login(new LoginRequest(no, pw)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                btnLogin.setEnabled(true);
                User u = response.body();
                if (u != null) {
                    Prefs.setStudentNo(LoginActivity.this, u.studentNo);
                    Toast.makeText(LoginActivity.this, "登录成功：" + u.name, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    tvError.setText("学号或密码错误");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnLogin.setEnabled(true);
                tvError.setText("无法连接服务器，请确认后端已启动");
            }
        });
    }
}
