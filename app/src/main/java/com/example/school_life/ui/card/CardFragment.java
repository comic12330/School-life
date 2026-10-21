package com.example.school_life.ui.card;

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
import com.example.school_life.net.model.CardResult;
import com.example.school_life.net.model.CardTransaction;
import com.example.school_life.net.model.CampusCard;
import com.example.school_life.util.Prefs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 校园卡：余额 + 挂失/解挂 + 流水 */
public class CardFragment extends Fragment {

    private View layoutNeedLogin;
    private View layoutContent;
    private TextView tvBalance;
    private TextView tvCardInfo;
    private TextView tvStatus;
    private Button btnToggleLost;
    private TransactionAdapter adapter;
    private SimpleDateFormat fmt = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        layoutNeedLogin = v.findViewById(R.id.layout_need_login);
        layoutContent = v.findViewById(R.id.layout_content);
        tvBalance = v.findViewById(R.id.tv_balance);
        tvCardInfo = v.findViewById(R.id.tv_card_info);
        tvStatus = v.findViewById(R.id.tv_card_status);
        btnToggleLost = v.findViewById(R.id.btn_toggle_lost);

        v.findViewById(R.id.btn_go_login).setOnClickListener(x ->
                startActivity(new Intent(getContext(), LoginActivity.class)));

        RecyclerView rv = v.findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setNestedScrollingEnabled(false);
        adapter = new TransactionAdapter();
        rv.setAdapter(adapter);

        btnToggleLost.setOnClickListener(x -> toggleLost());
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

        tvBalance.setText("加载中...");
        ApiClient.api().card(no).enqueue(new Callback<CardResult>() {
            @Override
            public void onResponse(@NonNull Call<CardResult> call, @NonNull Response<CardResult> resp) {
                if (!isAdded()) return;
                CardResult r = resp.body();
                if (r == null || r.card == null) {
                    tvBalance.setText("无数据");
                    return;
                }
                render(r.card, r.transactions);
            }

            @Override
            public void onFailure(@NonNull Call<CardResult> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                tvBalance.setText("--");
                tvStatus.setText("无法连接服务器");
                tvStatus.setTextColor(getResources().getColor(R.color.red));
            }
        });
    }

    private void render(CampusCard card, List<CardTransaction> transactions) {
        tvBalance.setText(String.format(Locale.CHINA, "￥%.2f", Double.parseDouble(card.balance)));
        tvCardInfo.setText("卡号 " + card.cardNo + " / 持卡人 " + card.name);
        boolean lost = Boolean.TRUE.equals(card.lost);
        tvStatus.setText(lost ? "已挂失" : "状态正常");
        tvStatus.setTextColor(getResources().getColor(lost ? R.color.red : R.color.green));
        btnToggleLost.setText(lost ? "解挂" : "挂失");
        adapter.setData(transactions);
    }

    private void toggleLost() {
        String no = Prefs.getStudentNo(getContext());
        if (no == null) return;
        boolean lostNow = "挂失".contentEquals(btnToggleLost.getText());
        btnToggleLost.setEnabled(false);
        ApiClient.api().setLost(no, lostNow).enqueue(new Callback<CampusCard>() {
            @Override
            public void onResponse(@NonNull Call<CampusCard> call, @NonNull Response<CampusCard> resp) {
                if (!isAdded()) return;
                btnToggleLost.setEnabled(true);
                refresh();
            }

            @Override
            public void onFailure(@NonNull Call<CampusCard> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnToggleLost.setEnabled(true);
                tvStatus.setText("操作失败：无法连接服务器");
                tvStatus.setTextColor(getResources().getColor(R.color.red));
            }
        });
    }

    /** 流水列表适配器 */
    static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.Holder> {

        private List<CardTransaction> data;

        void setData(List<CardTransaction> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            CardTransaction t = data.get(position);
            h.tvPlace.setText(t.place);
            h.tvTime.setText(new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date(t.time)));
            h.tvType.setText(t.type);
            double amount = Double.parseDouble(t.amount);
            h.tvAmount.setText(String.format(Locale.CHINA, "%s%.2f", amount > 0 ? "+" : "", amount));
            int color;
            if (amount > 0) {
                color = R.color.green;
            } else if (amount < 0) {
                color = R.color.red;
            } else {
                color = R.color.gray_text;
            }
            h.tvAmount.setTextColor(h.itemView.getResources().getColor(color));
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvPlace, tvTime, tvType, tvAmount;

            Holder(@NonNull View v) {
                super(v);
                tvPlace = v.findViewById(R.id.tv_place);
                tvTime = v.findViewById(R.id.tv_time);
                tvType = v.findViewById(R.id.tv_type);
                tvAmount = v.findViewById(R.id.tv_amount);
            }
        }
    }
}
