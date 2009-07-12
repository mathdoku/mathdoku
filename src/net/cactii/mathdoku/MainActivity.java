package net.cactii.mathdoku;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {
    GridView kenKenGrid;
    TextView solvedText;
    Button newGame;
    RadioGroup radio;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.kenKenGrid = (GridView)findViewById(R.id.gridView);
        this.radio = (RadioGroup)findViewById(R.id.radio);
        this.solvedText = (TextView)findViewById(R.id.solvedText);
        this.kenKenGrid.animText = this.solvedText;
        this.newGame = (Button)findViewById(R.id.newGame);
        this.newGame.setOnClickListener(new OnClickListener() {
    			public void onClick(View v) {
    				MainActivity.this.startNewGame();
    			}
        });
        
        this.kenKenGrid.setSolvedHandler(this.kenKenGrid.new OnSolvedListener() {
    			@Override
    			public void puzzleSolved() {
    				// TODO Auto-generated method stub
    				Log.d("kenKen", "Solved!!!");
    				animText("Solved!!", 0xFF002F00);
    				MainActivity.this.newGame.setVisibility(View.VISIBLE);
    			}
        });
        this.kenKenGrid.setFocusable(true);
        this.kenKenGrid.setFocusableInTouchMode(true);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean supRetVal = super.onCreateOptionsMenu(menu);
    	SubMenu newGame = menu.addSubMenu(0, 0, 0, "New game");
    	SubMenu solveGame = menu.addSubMenu(0, 1, 0, "Solve");
    	return supRetVal;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	boolean supRetVal = super.onOptionsItemSelected(menuItem);
    	switch (menuItem.getItemId()) {
	    	case 0 :
	    		this.startNewGame();
				break;
	    	case 1:
	    		this.kenKenGrid.Solve();
	    		MainActivity.this.newGame.setVisibility(View.VISIBLE);
	    		break;
    	}
    	return supRetVal;
    }
    
    public void startNewGame() {
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
        case R.id.radio7:
            kenKenGrid.mGridSize = 7;
            break; 
            }
		this.solvedText.setVisibility(View.INVISIBLE);
		this.newGame.setVisibility(View.INVISIBLE);
		this.kenKenGrid.reCreate();
		this.kenKenGrid.invalidate();
    }
    
    private void animText(String text, int color) {
  	    this.solvedText.setText(text);
  	  this.solvedText.setTextColor(color);
  	  this.solvedText.setVisibility(View.VISIBLE);
  	    final float SCALE_FROM = (float) 0;
  	    final float SCALE_TO = (float) 1.0;
  	    ScaleAnimation anim = new ScaleAnimation(SCALE_FROM, SCALE_TO, SCALE_FROM, SCALE_TO,
  	    		this.kenKenGrid.mCurrentWidth/2, this.kenKenGrid.mCurrentWidth/2);
  	    anim.setDuration(1000);
  	    //animText.setAnimation(anim);
  	  this.solvedText.startAnimation(anim);
  	}
}