package com.example.school_life.ui.schedule;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school_life.R;
import com.example.school_life.net.ApiClient;
import com.example.school_life.net.model.Course;
import com.example.school_life.alarm.AlarmScheduler;
import com.example.school_life.util.Prefs;
import com.example.school_life.util.SectionTimes;
import com.example.school_life.util.TermPrefs;
import com.example.school_life.util.WeekUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 课表：12×7 网格 + 周次切换 + 增删改 + 单双周 */
public class ScheduleFragment extends Fragment {

    private static final String[] DAY_NAMES = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final int[] COURSE_COLORS = {
            R.color.course_1, R.color.course_2, R.color.course_3,
            R.color.course_4, R.color.course_5, R.color.course_6
    };

    private TextView tvWeek;
    private TextView tvStatus;
    private RecyclerView rvGrid;
    private GridAdapter adapter;
    private List<Course> courses = new ArrayList<>();
    private List<Cell> cells = new ArrayList<>();
    private int week = 1;

    /** 网格单元 */
    static class Cell {
        int day;       // 0=左侧节次列，1~7=周一~周日
        int section;   // 1~12
        String text;
        Course course; // 非空表示该格起点是课程
        boolean covered;
        boolean header;

        Cell(int day, int section, String text, Course course, boolean covered, boolean header) {
            this.day = day;
            this.section = section;
            this.text = text;
            this.course = course;
            this.covered = covered;
            this.header = header;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        tvWeek = v.findViewById(R.id.tv_week);
        tvStatus = v.findViewById(R.id.tv_status);
        rvGrid = v.findViewById(R.id.rv_grid);

        rvGrid.setLayoutManager(new GridLayoutManager(getContext(), 8));
        adapter = new GridAdapter();
        rvGrid.setAdapter(adapter);

        ImageButton prev = v.findViewById(R.id.btn_prev_week);
        ImageButton next = v.findViewById(R.id.btn_next_week);
        prev.setOnClickListener(x -> {
            if (week > 1) {
                week--;
                rebuild();
            }
        });
        next.setOnClickListener(x -> {
            week++;
            rebuild();
        });

        v.findViewById(R.id.fab_add).setOnClickListener(x ->
                showEditDialog(null, 1, 1));
    }

    @Override
    public void onResume() {
        super.onResume();
        week = WeekUtils.currentWeek(TermPrefs.getTermStart(getContext()),
                System.currentTimeMillis());
        load();
    }

    private void load() {
        String no = Prefs.getStudentNo(getContext());
        if (no == null) {
            tvStatus.setVisibility(TextView.VISIBLE);
            tvStatus.setText("请先登录后使用课表");
            courses.clear();
            rebuild();
            return;
        }
        tvStatus.setVisibility(TextView.VISIBLE);
        tvStatus.setText("加载中...");
        ApiClient.api().listCourses(no).enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(@NonNull Call<List<Course>> call,
                                   @NonNull Response<List<Course>> resp) {
                if (!isAdded()) return;
                List<Course> body = resp.body();
                courses = body == null ? new ArrayList<>() : body;
                tvStatus.setVisibility(TextView.GONE);
                rebuild();
                // 课程变动后重排上课提醒
                AlarmScheduler.scheduleAll(requireContext(), courses);
            }

            @Override
            public void onFailure(@NonNull Call<List<Course>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                tvStatus.setText("无法连接服务器");
                courses.clear();
                rebuild();
            }
        });
    }

    /** 依据 courses + 当前周构建 8 列网格 */
    private void rebuild() {
        tvWeek.setText("第 " + week + " 周");
        cells = new ArrayList<>();
        cells.add(new Cell(0, 0, "节次", null, false, true));
        long monday = TermPrefs.getTermStart(getContext()) + (week - 1) * 7 * WeekUtils.DAY;
        SimpleDateFormat df = new SimpleDateFormat("M/d", Locale.CHINA);
        for (int d = 1; d <= 7; d++) {
            String date = df.format(new Date(monday + (d - 1) * WeekUtils.DAY));
            cells.add(new Cell(d, 0, DAY_NAMES[d - 1] + "\n" + date, null, false, true));
        }
        for (int s = 1; s <= 12; s++) {
            cells.add(new Cell(0, s, String.valueOf(s), null, false, true));
            for (int d = 1; d <= 7; d++) {
                Course hit = findCourse(d, s);
                if (hit != null && hit.startSection == s) {
                    cells.add(new Cell(d, s, hit.name + "\n@" + hit.room, hit, false, false));
                } else if (hit != null) {
                    cells.add(new Cell(d, s, "", hit, true, false));
                } else {
                    cells.add(new Cell(d, s, "", null, false, false));
                }
            }
        }
        adapter.setData(cells);
    }

    /** 在当前周、指定天与节，找到覆盖该格的课程 */
    private Course findCourse(int day, int section) {
        for (Course c : courses) {
            if (c.dayOfWeek == day && section >= c.startSection && section <= c.endSection
                    && WeekUtils.visible(c, week)) {
                return c;
            }
        }
        return null;
    }

    /** 新增 / 编辑课程弹窗（course 为 null 表示新增） */
    private void showEditDialog(final Course course, final int defDay, final int defSection) {
        if (Prefs.getStudentNo(getContext()) == null) {
            Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_course_edit, null);
        EditText etName = v.findViewById(R.id.et_name);
        EditText etTeacher = v.findViewById(R.id.et_teacher);
        EditText etRoom = v.findViewById(R.id.et_room);
        Spinner spDay = v.findViewById(R.id.sp_day);
        Spinner spStart = v.findViewById(R.id.sp_start_section);
        Spinner spEnd = v.findViewById(R.id.sp_end_section);
        Spinner spWeekType = v.findViewById(R.id.sp_week_type);
        EditText etStartWeek = v.findViewById(R.id.et_start_week);
        EditText etEndWeek = v.findViewById(R.id.et_end_week);
        CheckBox cbRemind = v.findViewById(R.id.cb_remind);
        EditText etRemindMinutes = v.findViewById(R.id.et_remind_minutes);

        spDay.setAdapter(spinnerAdapter(DAY_NAMES));
        String[] sections = new String[12];
        for (int i = 1; i <= 12; i++) {
            sections[i - 1] = "第" + i + "节(" + SectionTimes.LABELS[i] + ")";
        }
        spStart.setAdapter(spinnerAdapter(sections));
        spEnd.setAdapter(spinnerAdapter(sections));
        spWeekType.setAdapter(spinnerAdapter(new String[]{"每周", "单周", "双周"}));

        if (course != null) {
            etName.setText(course.name);
            etTeacher.setText(course.teacher);
            etRoom.setText(course.room);
            spDay.setSelection(course.dayOfWeek - 1);
            spStart.setSelection(course.startSection - 1);
            spEnd.setSelection(course.endSection - 1);
            spWeekType.setSelection(course.weekType);
            etStartWeek.setText(String.valueOf(course.startWeek));
            etEndWeek.setText(String.valueOf(course.endWeek));
            cbRemind.setChecked(Boolean.TRUE.equals(course.remind));
            etRemindMinutes.setText(String.valueOf(course.remindMinutes == null ? 30 : course.remindMinutes));
        } else {
            spDay.setSelection(defDay - 1);
            spStart.setSelection(defSection - 1);
            spEnd.setSelection(defSection);
            etStartWeek.setText(String.valueOf(week));
            etEndWeek.setText("16");
            etRemindMinutes.setText("30");
        }

        AlertDialog.Builder b = new AlertDialog.Builder(requireContext())
                .setTitle(course == null ? "新增课程" : "编辑课程")
                .setView(v)
                .setPositiveButton("保存", (d, w) -> {
                    Course c = course == null ? new Course() : course;
                    c.name = etName.getText().toString().trim();
                    c.teacher = etTeacher.getText().toString().trim();
                    c.room = etRoom.getText().toString().trim();
                    c.dayOfWeek = spDay.getSelectedItemPosition() + 1;
                    c.startSection = spStart.getSelectedItemPosition() + 1;
                    c.endSection = spEnd.getSelectedItemPosition() + 1;
                    c.weekType = spWeekType.getSelectedItemPosition();
                    c.startWeek = parseInt(etStartWeek.getText().toString(), 1);
                    c.endWeek = parseInt(etEndWeek.getText().toString(), 16);
                    c.remind = cbRemind.isChecked();
                    c.remindMinutes = parseInt(etRemindMinutes.getText().toString(), 30);

                    if (TextUtils.isEmpty(c.name)) {
                        Toast.makeText(getContext(), "请填写课程名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (c.endSection < c.startSection) {
                        Toast.makeText(getContext(), "结束节次不能早于开始节次", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (c.endWeek < c.startWeek) {
                        Toast.makeText(getContext(), "结束周不能早于开始周", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    save(c, course == null);
                })
                .setNegativeButton("取消", null);
        if (course != null) {
            b.setNeutralButton("删除", (d, w) -> confirmDelete(course));
        }
        b.show();
    }

    private ArrayAdapter<String> spinnerAdapter(String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private void save(Course c, boolean isNew) {
        c.studentNo = Prefs.getStudentNo(getContext());
        Call<Course> call = isNew ? ApiClient.api().addCourse(c) : ApiClient.api().updateCourse(c);
        call.enqueue(new Callback<Course>() {
            @Override
            public void onResponse(@NonNull Call<Course> call, @NonNull Response<Course> resp) {
                if (!isAdded()) return;
                load();
            }

            @Override
            public void onFailure(@NonNull Call<Course> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "保存失败：无法连接服务器", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(final Course course) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除课程")
                .setMessage("确定删除「" + course.name + "」吗？")
                .setPositiveButton("删除", (d, w) -> ApiClient.api().deleteCourse(course.id)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> resp) {
                                if (!isAdded()) return;
                                load();
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                if (!isAdded()) return;
                                Toast.makeText(getContext(), "删除失败：无法连接服务器", Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("取消", null)
                .show();
    }

    /** 网格适配器 */
    class GridAdapter extends RecyclerView.Adapter<GridAdapter.Holder> {

        private List<Cell> data = new ArrayList<>();

        void setData(List<Cell> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_schedule_cell, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            final Cell cell = data.get(position);
            h.tvCell.setText(cell.text);
            int bg;
            if (cell.course != null && !cell.covered) {
                int idx = (int) Math.abs(cell.course.id == null ? 0 : cell.course.id) % COURSE_COLORS.length;
                bg = COURSE_COLORS[idx];
            } else if (cell.covered) {
                bg = R.color.cell_covered;
            } else if (cell.header) {
                bg = R.color.cell_header;
            } else {
                bg = R.color.cell_empty;
            }
            h.tvCell.setBackgroundColor(h.itemView.getResources().getColor(bg));

            h.itemView.setOnClickListener(x -> {
                if (cell.course != null && !cell.covered) {
                    showEditDialog(cell.course, cell.day, cell.section);
                } else if (!cell.covered && !cell.header && cell.day > 0) {
                    showEditDialog(null, cell.day, cell.section);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tvCell;

            Holder(@NonNull View v) {
                super(v);
                tvCell = v.findViewById(R.id.tv_cell);
            }
        }
    }
}
