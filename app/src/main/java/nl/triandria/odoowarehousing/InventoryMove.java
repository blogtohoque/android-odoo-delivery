package nl.triandria.odoowarehousing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import nl.triandria.odoowarehousing.utilities.BarcodeScan;

public class InventoryMove extends AppCompatActivity {

    private static final String TAG = InventoryMove.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_move);
        Toolbar toolbar = findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        Button scanProduct = findViewById(R.id.button_scan_product_inventory_move);
        BarcodeScan onClickListener = new BarcodeScan(this);
        scanProduct.setOnClickListener(onClickListener);
        Button selectProduct = findViewById(R.id.button_select_product_inventory_move);
        Button scanFromLocation = findViewById(R.id.button_scan_from_location_inventory_move);
        scanFromLocation.setOnClickListener(onClickListener);
        Button selectFromLocation = findViewById(R.id.button_select_from_location_inventory_move);
        Button stockMoveOk = findViewById(R.id.button_inventory_move_ok);
    }


}
