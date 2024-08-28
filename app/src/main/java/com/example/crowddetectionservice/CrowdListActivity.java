package com.example.crowddetectionservice;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrowdListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CrowdAdapter crowdAdapter;
    private List<CrowdItem> crowdItemList = new ArrayList<>();
//    private static final String API_URL = "http://localhost:8000/api/crowd/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowd_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        crowdAdapter = new CrowdAdapter(crowdItemList);
        recyclerView.setAdapter(crowdAdapter);

        fetchCrowdData();
    }

    private void fetchCrowdData() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(MainActivity.apiBaseUrl + "/crowd/")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CrowdListActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int id = jsonObject.getInt("id");
                            int crowdCount = jsonObject.getInt("crowd_count");
                            String time = jsonObject.getString("time");
                            String alert = jsonObject.getString("alert");

                            CrowdItem crowdItem = new CrowdItem(id, crowdCount, time, alert);
                            crowdItemList.add(crowdItem);
                        }

                        runOnUiThread(() -> crowdAdapter.notifyDataSetChanged());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
