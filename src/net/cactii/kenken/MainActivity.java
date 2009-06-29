package net.cactii.kenken;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;

public class MainActivity extends Activity {
    Button refreshButton;
    GridView kenKenGrid;
    RadioGroup radio;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.refreshButton = (Button)findViewById(R.id.refresh);
        this.kenKenGrid = (GridView)findViewById(R.id.gridView);
        this.radio = (RadioGroup)findViewById(R.id.radio);
        this.refreshButton.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            switch(radio.getCheckedRadioButtonId()) {
              case R.id.radio4:
                kenKenGrid.mGridSize = 4;
                break;
              case R.id.radio5:
                kenKenGrid.mGridSize = 5;
                break;
              case R.id.radio6:
                kenKenGrid.mGridSize = 6;
                break; 
            }
            MainActivity.this.kenKenGrid.reCreate();
            MainActivity.this.kenKenGrid.invalidate();
          }
        });
    }
}