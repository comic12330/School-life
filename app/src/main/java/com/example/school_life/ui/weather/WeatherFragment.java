package com.example.school_life.ui.weather;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.school_life.Constants;
import com.example.school_life.R;
import com.example.school_life.net.ApiClient;
import com.example.school_life.net.model.DailyResponse;
import com.example.school_life.net.model.GeoResponse;
import com.example.school_life.net.model.NowResponse;
import com.example.school_life.util.Prefs;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 天气：城市搜索 + 实时天气 + 7 天预报（直连和风天气 API） */
public class WeatherFragment extends Fragment {

    private TextView tvStatus;
    private LinearLayout layoutNow;
    private TextView tvCity;
    private TextView tvNowText;
    private TextView tvNowTemp;
    private DayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        tvStatus = v.findViewById(R.id.tv_status);
        layoutNow = v.findViewById(R.id.layout_now);
        tvCity = v.findViewById(R.id.tv_city);
        tvNowText = v.findViewById(R.id.tv_now_text);
        tvNowTemp = v.findViewById(R.id.tv_now_temp);

        RecyclerView rv = v.findViewById(R.id.rv_daily);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DayAdapter();
        rv.setAdapter(adapter);

        EditText etCity = v.findViewById(R.id.et_city);
        etCity.setText(Prefs.getCity(getContext()));
        Button btnSearch = v.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(x ->
                load(etCity.getText().toString().trim()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Constants.QWEATHER_KEY.isEmpty() && tvCity.getText().length() == 0) {
            showStatus("请先在 Constants.java 中配置和风天气 QWEATHER_KEY");
        } else if (layoutNow.getVisibility() == View.GONE
                && Constants.QWEATHER_KEY.length() > 0 && tvCity.getText().length() == 0) {
            load(Prefs.getCity(getContext()));
        }
    }

    private void showStatus(String s) {
        tvStatus.setVisibility(TextView.VISIBLE);
        tvStatus.setText(s);
    }

    private void hideStatus() {
        tvStatus.setVisibility(TextView.GONE);
    }

    /** 第一步：城市名 -> LocationID */
    private void load(String city) {
        if (Constants.QWEATHER_KEY.isEmpty()) {
            showStatus("请先在 Constants.java 中配置和风天气 QWEATHER_KEY");
            return;
        }
        if (city.isEmpty()) {
            showStatus("请输入城市名");
            return;
        }
        showStatus("查询中...");
        ApiClient.weather().geoLookup(Constants.WEATHER_GEO_URL, city, Constants.QWEATHER_KEY)
                .enqueue(new Callback<GeoResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GeoResponse> call,
                                           @NonNull Response<GeoResponse> resp) {
                        if (!isAdded()) return;
                        GeoResponse g = resp.body();
                        if (g == null || g.location == null || g.location.isEmpty()) {
                            showStatus("未找到城市：" + city);
                            return;
                        }
                        GeoResponse.GeoLocation loc = g.location.get(0);
                        Prefs.setCity(getContext(), loc.name);
                        tvCity.setText(loc.name + "（" + loc.adm1 + " " + loc.adm2 + "）");
                        loadNow(loc.id);
                        loadDaily(loc.id);
                    }

                    @Override
                    public void onFailure(@NonNull Call<GeoResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        showStatus("天气查询失败，请检查网络");
                    }
                });
    }

    /** 第二步：实时天气 */
    private void loadNow(String locationId) {
        ApiClient.weather().now(Constants.WEATHER_NOW_URL, locationId, Constants.QWEATHER_KEY)
                .enqueue(new Callback<NowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<NowResponse> call,
                                           @NonNull Response<NowResponse> resp) {
                        if (!isAdded()) return;
                        hideStatus();
                        NowResponse r = resp.body();
                        if (r == null || r.now == null) {
                            showStatus("实时天气获取失败（code: " + (r == null ? "null" : r.code) + "）");
                            return;
                        }
                        layoutNow.setVisibility(View.VISIBLE);
                        tvNowTemp.setText(r.now.temp + "°");
                        tvNowText.setText(r.now.text + "｜" + r.now.windDir + r.now.windScale + "级"
                                + "｜湿度 " + r.now.humidity + "%");
                    }

                    @Override
                    public void onFailure(@NonNull Call<NowResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        showStatus("实时天气查询失败");
                    }
                });
    }

    /** 第三步：7 天预报 */
    private void loadDaily(String locationId) {
        ApiClient.weather().daily(Constants.WEATHER_7D_URL, locationId, Constants.QWEATHER_KEY)
                .enqueue(new Callback<DailyResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DailyResponse> call,
                                           @NonNull Response<DailyResponse> resp) {
                        if (!isAdded()) return;
                        DailyResponse r = resp.body();
                        if (r == null || r.daily == null) return;
                        adapter.setData(r.daily);
                    }

                    @Override
                    public void onFailure(@NonNull Call<DailyResponse> call, @NonNull Throwable t) {
                        // 状态条已由实时天气处理
                    }
                });
    }

    /** 7 天预报适配器 */
    static class DayAdapter extends RecyclerView.Adapter<DayAdapter.Holder> {

        private List<DailyResponse.Daily> data;

        void setData(List<DailyResponse.Daily> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_weather_day, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            DailyResponse.Daily d = data.get(position);
            String date = d.fxDate == null ? "--" : d.fxDate;
            h.tvDate.setText(date.length() >= 10 ? date.substring(5) : date);
            h.tvText.setText(d.textDay == null ? "--" : d.textDay);
            h.tvTemp.setText((d.tempMin == null ? "--" : d.tempMin) + "~"
                    + (d.tempMax == null ? "--" : d.tempMax) + "°");
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvDate, tvText, tvTemp;

            Holder(@NonNull View v) {
                super(v);
                tvDate = v.findViewById(R.id.tv_date);
                tvText = v.findViewById(R.id.tv_text);
                tvTemp = v.findViewById(R.id.tv_temp);
            }
        }
    }
}
