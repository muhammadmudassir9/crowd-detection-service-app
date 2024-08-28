import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.crowddetectionservice.R;
import com.example.crowddetectionservice.database.InventoryDatabase;
import com.example.crowddetectionservice.database.InventoryRecord;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    private ListView listView;
    public InventoryDatabase inventoryDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        listView = findViewById(R.id.listView);
        inventoryDatabase = new InventoryDatabase(this);

        displayInventory();
    }

    private void displayInventory() {
        ArrayList<String> dates = inventoryDatabase.getDates();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dates);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String date = dates.get(position);
                ArrayList<InventoryRecord> records = inventoryDatabase.getRecordsByDate(date);
                displayRecords(records);
            }
        });
    }

    private void displayRecords(ArrayList<InventoryRecord> records) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Records");
        String[] recordNames = new String[records.size()];
        for (int i = 0; i < records.size(); i++) {
            recordNames[i] = records.get(i).getFileName();
        }
        builder.setItems(recordNames, (dialog, which) -> {
            InventoryRecord record = records.get(which);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(record.getFilePath()), record.getFileType().equals("image") ? "image/*" : "video/*");
            startActivity(intent);
        });
        builder.show();
    }
}