package com.example.school_life.ui.edu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school_life.LoginActivity;
import com.example.school_life.R;
import com.example.school_life.net.ApiClient;
import com.example.school_life.net.model.EduResult;
import com.example.school_life.net.model.Grade;
import com.example.school_life.util.GpaCalculator;
import com.example.school_life.util.Prefs;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 教务：学生信息 + GPA + 成绩列表 + 退出登录 */
public class EduFragment extends Fragment {

    private View layoutNeedLogin;
    private View layoutContent;
    private TextView tvProfile;
    private TextView tvGpa;
    private TextView tvCredits;
    private GradeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        layoutNeedLogin = v.findViewById(R.id.layout_need_login);
        layoutContent = v.findViewById(R.id.layout_content);
        tvProfile = v.findViewById(R.id.tv_profile);
        tvGpa = v.findViewById(R.id.tv_gpa);
        tvCredits = v.findViewById(R.id.tv_credits);

        v.findViewById(R.id.btn_go_login).setOnClickListener(x ->
                startActivity(new Intent(getContext(), LoginActivity.class)));

        RecyclerView rv = v.findViewById(R.id.rv_grades);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setNestedScrollingEnabled(false);
        adapter = new GradeAdapter();
        rv.setAdapter(adapter);

        v.findViewById(R.id.btn_logout).setOnClickListener(x -> {
            Prefs.clearLogin(getContext());
            refresh();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        String no = Prefs.getStudentNo(getContext());
        boolean logged = no != null;
        layoutNeedLogin.setVisibility(logged ? View.GONE : View.VISIBLE);
        layoutContent.setVisibility(logged ? View.VISIBLE : View.GONE);
        if (!logged) return;

        tvProfile.setText("加载中...");
        ApiClient.api().edu(no).enqueue(new Callback<EduResult>() {
            @Override
            public void onResponse(@NonNull Call<EduResult> call, @NonNull Response<EduResult> resp) {
                if (!isAdded()) return;
                EduResult r = resp.body();
                if (r == null || r.profile == null) {
                    tvProfile.setText("无数据");
                    return;
                }
                render(r);
            }

            @Override
            public void onFailure(@NonNull Call<EduResult> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                tvProfile.setText("无法连接服务器");
            }
        });
    }

    private void render(EduResult r) {
        tvProfile.setText("姓名：" + r.profile.name
                + "\n学号：" + r.profile.studentNo
                + "\n年级：" + nullToDash(r.profile.grade)
                + "\n学院：" + nullToDash(r.profile.college)
                + "\n专业：" + nullToDash(r.profile.major)
                + "\n班级：" + nullToDash(r.profile.clazz));

        List<Grade> grades = r.grades == null ? java.util.Collections.<Grade>emptyList() : r.grades;
        adapter.setData(grades);

        double[] credits = new double[grades.size()];
        double[] scores = new double[grades.size()];
        double creditSum = 0;
        for (int i = 0; i < grades.size(); i++) {
            credits[i] = Double.parseDouble(grades.get(i).credit);
            scores[i] = Double.parseDouble(grades.get(i).score);
            creditSum += credits[i];
        }
        tvGpa.setText("GPA " + GpaCalculator.gpa(credits, scores));
        tvCredits.setText(String.format(java.util.Locale.CHINA, "总学分 %.1f", creditSum));
    }

    private String nullToDash(String s) {
        return s == null ? "--" : s;
    }

    /** 成绩列表适配器 */
    static class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.Holder> {

        private List<Grade> data;

        void setData(List<Grade> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_grade, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Grade g = data.get(position);
            h.tvCourse.setText(g.courseName);
            h.tvSemester.setText(g.semester);
            h.tvCredit.setText(g.credit + "学分");
            h.tvScore.setText(g.score);
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvCourse, tvSemester, tvCredit, tvScore;

            Holder(@NonNull View v) {
                super(v);
                tvCourse = v.findViewById(R.id.tv_course);
                tvSemester = v.findViewById(R.id.tv_semester);
                tvCredit = v.findViewById(R.id.tv_credit);
                tvScore = v.findViewById(R.id.tv_score);
            }
        }
    }
}
