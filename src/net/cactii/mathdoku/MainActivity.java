package net.cactii.mathdoku;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    GridView kenKenGrid;
    TextView solvedText;
    TextView newGame;
    ProgressDialog mProgressDialog;
    
    LinearLayout topLayout;
    LinearLayout digitSelector;
    Button digit1;
    Button digit2;
    Button digit3;
    Button digit4;
    Button digit5;
    Button digit6;
    Button digit7;
    Button digit8;
    Button clearDigit;
    CheckBox maybeButton;
	private Animation outAnimation;
	private Animation solvedAnimation;
	
	public SharedPreferences preferences;
    
    final Handler mHandler = new Handler();
	private WakeLock wakeLock;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Mathdoku");
        
        this.topLayout = (LinearLayout)findViewById(R.id.topLayout);
        this.kenKenGrid = (GridView)findViewById(R.id.gridView);
        this.kenKenGrid.mContext = this;
        this.solvedText = (TextView)findViewById(R.id.solvedText);
        this.kenKenGrid.animText = this.solvedText;
        this.newGame = (TextView)findViewById(R.id.newGame);
        this.digitSelector = (LinearLayout)findViewById(R.id.digitSelector);
        this.digit1 = (Button)findViewById(R.id.digitSelect1);
        this.digit2 = (Button)findViewById(R.id.digitSelect2);
        this.digit3 = (Button)findViewById(R.id.digitSelect3);
        this.digit4 = (Button)findViewById(R.id.digitSelect4);

        this.digit5 = (Button)findViewById(R.id.digitSelect5);
        this.digit6 = (Button)findViewById(R.id.digitSelect6);
        this.digit7 = (Button)findViewById(R.id.digitSelect7);
        this.digit8 = (Button)findViewById(R.id.digitSelect8);
        this.clearDigit = (Button)findViewById(R.id.clearButton);
        this.maybeButton = (CheckBox)findViewById(R.id.maybeButton);
       

        solvedAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.solvedanim);
        solvedAnimation.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
              solvedText.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
          });
        
        outAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.selectorzoomout);
        outAnimation.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
              digitSelector.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
          });
        
        this.kenKenGrid.setOnGridTouchListener(this.kenKenGrid.new OnGridTouchListener() {
			@Override
			public void gridTouched(GridCell cell) {
				if (digitSelector.getVisibility() == View.VISIBLE) {
					// digitSelector.setVisibility(View.GONE);
					digitSelector.startAnimation(outAnimation);
					//cell.mSelected = false;
					kenKenGrid.mSelectorShown = false;
					kenKenGrid.requestFocus();
				} else {
					digitSelector.setVisibility(View.VISIBLE);
					Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.selectorzoomin);
					digitSelector.startAnimation(animation);
					kenKenGrid.mSelectorShown = true;
					maybeButton.setChecked((cell.mPossibles.size() > 0));
					digitSelector.requestFocus();
				}
			}
		});
        
    	this.kenKenGrid.mFace=Typeface.createFromAsset(this.getAssets(), "fonts/font.ttf");
    	this.solvedText.setTypeface(this.kenKenGrid.mFace);
        
        this.kenKenGrid.setSolvedHandler(this.kenKenGrid.new OnSolvedListener() {
    			@Override
    			public void puzzleSolved() {
    				// TODO Auto-generated method stub
    				if (kenKenGrid.mActive)
    					animText("Solved!!", 0xFF002F00);
    				MainActivity.this.digitSelector.setVisibility(View.GONE);
    				MainActivity.this.newGame.setVisibility(View.VISIBLE);
    			}
        });

        
        this.digit1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(v.getId());
			}
        });
        this.digit2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(v.getId());
			}
        });
        this.digit3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(v.getId());
			}
        });
        this.digit4.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(v.getId());
			}
        });
        this.digit5.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(v.getId());
			}
        });
        this.digit6.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(v.getId());
			}
        });
        this.digit7.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(v.getId());
			}
        });
        this.digit8.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(v.getId());
			}
        });
        this.clearDigit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.digitSelected(v.getId());
			}
        });
        
        newVersionCheck();
        this.kenKenGrid.setFocusable(true);
        this.kenKenGrid.setFocusableInTouchMode(true);
        SaveGame saver = new SaveGame();
        if (saver.Restore(this.kenKenGrid)) {
        	this.setButtonVisibility(this.kenKenGrid.mGridSize);
        	this.kenKenGrid.mActive = true;
        }
    }
    
    public void onPause() {
    	if (this.kenKenGrid.mGridSize > 3) {
	    	SaveGame saver = new SaveGame();
	    	saver.Save(this.kenKenGrid);
    	}
        if (this.wakeLock.isHeld())
            this.wakeLock.release();
    	super.onPause();
    }
    
    public void onResume() {
	    if (this.preferences.getBoolean("wakelock", true))
	        this.wakeLock.acquire();
	    if (this.preferences.getBoolean("alternatetheme", false)) {
	    	this.topLayout.setBackgroundDrawable(null);
	    	this.topLayout.setBackgroundColor(0xFFE0E0FF);
	    } else
	    	this.topLayout.setBackgroundResource(R.drawable.background);
	    this.kenKenGrid.mDupedigits = this.preferences.getBoolean("dupedigits", true);
	    this.kenKenGrid.mBadMaths = this.preferences.getBoolean("badmaths", true);
	    super.onResume();
	}
    
    protected void onActivityResult(int requestCode, int resultCode,
    	      Intent data) {
	    if (requestCode != 7 || resultCode != Activity.RESULT_OK)
	      return;
	    Bundle extras = data.getExtras();
	    String filename = extras.getString("filename");
    	Log.d("Mathdoku", "Loading game: " + filename);
    	SaveGame saver = new SaveGame(filename);
        if (saver.Restore(this.kenKenGrid)) {
        	this.setButtonVisibility(this.kenKenGrid.mGridSize);
        	this.kenKenGrid.mActive = true;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean supRetVal = super.onCreateOptionsMenu(menu);
    	SubMenu newGame = menu.addSubMenu(4, 0, 0, "New game");
    	newGame.add("4x4  (easy)");
    	newGame.add("5x5  (medium)");
    	newGame.add("6x6  (hard)");
    	newGame.add("7x7  (harder)");
    	newGame.add("8x8  (hardest)");
    	SubMenu load = menu.addSubMenu(0, 1, 0, "Load/Save");
    	SubMenu solveGame = menu.addSubMenu(1, 2, 0, "Solve");
    	
    	SubMenu options = menu.addSubMenu(3, 3, 0, "Options");
    	SubMenu about = menu.addSubMenu(2, 4, 0, "Help");
    	
    	return supRetVal;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	boolean supRetVal = super.onOptionsItemSelected(menuItem);
    	String title = (String) menuItem.getTitle();
    	switch (menuItem.getItemId()) {
	    	case 0 :
	    		if (title.startsWith("4x4"))
	    			this.kenKenGrid.mGridSize = 4;
	    		else if (title.startsWith("5x5"))
	    			this.kenKenGrid.mGridSize = 5;
	    		else if (title.startsWith("6x6"))
	    			this.kenKenGrid.mGridSize = 6;
	    		else if (title.startsWith("7x7"))
	    			this.kenKenGrid.mGridSize = 7;
	    		else if (title.startsWith("8x8"))
	    			this.kenKenGrid.mGridSize = 8;
	    		else
	    			break;
				this.startNewGame(this.kenKenGrid.mGridSize);
				break;
	    	case 2:
	    		if (this.kenKenGrid.mActive)
	    			this.kenKenGrid.Solve();
	    		this.newGame.setVisibility(View.VISIBLE);
	    		break;
	        case 3 :
	            startActivityForResult(new Intent(
	                MainActivity.this, OptionsActivity.class), 0);
	            break;
	    	case 4:
	    		this.openHelpDialog();
	    		break;
	    	case 1:
	            Intent i = new Intent(this, SavedGameList.class);
	            startActivityForResult(i, 7);
		            break;
    	}
    	return supRetVal;
    }
    
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (event.getAction() == KeyEvent.ACTION_DOWN &&
    	  keyCode == KeyEvent.KEYCODE_BACK &&
    	  this.kenKenGrid.mSelectorShown) {
      	this.digitSelector.setVisibility(View.GONE);
    	this.kenKenGrid.requestFocus();
    	this.kenKenGrid.mSelectorShown = false;
    	this.kenKenGrid.invalidate();
    	return true;
      }
  	  return super.onKeyDown(keyCode, event);
    }
    
    
    public void digitSelected(int digitId) {
    	int value = 0;
    	switch (digitId) {
    	case R.id.digitSelect1 :
    		value = 1;
    		break;
    	case R.id.digitSelect2 :
    		value = 2;
    		break;
    	case R.id.digitSelect3 :
    		value = 3;
    		break;
    	case R.id.digitSelect4 :
    		value = 4;
    		break;
    	case R.id.digitSelect5 :
    		value = 5;
    		break;
    	case R.id.digitSelect6 :
    		value = 6;
    		break;
    	case R.id.digitSelect7 :
    		value = 7;
    		break;
    	case R.id.digitSelect8 :
    		value = 8;
    		break;
    	case R.id.clearButton :
    		value = 0;
    		break;
    	}
    	if (this.maybeButton.isChecked())
    		if (value == 0)
    			this.kenKenGrid.mSelectedCell.mPossibles.clear();
    		else
    			this.kenKenGrid.mSelectedCell.togglePossible(value);
    	else {
    		this.kenKenGrid.mSelectedCell.setUserValue(value);
    		this.kenKenGrid.mSelectedCell.mPossibles.clear();
    	}
    	this.digitSelector.setVisibility(View.GONE);
    	// this.kenKenGrid.mSelectedCell.mSelected = false;
    	this.kenKenGrid.requestFocus();
    	this.kenKenGrid.mSelectorShown = false;
    	this.kenKenGrid.invalidate();
    }
    
    
    // Create runnable for posting
    final Runnable newGameReady = new Runnable() {
        public void run() {
        	MainActivity.this.dismissDialog(0);
        	MainActivity.this.kenKenGrid.invalidate();
        }
    };
    
    public void startNewGame(int gridSize) {
    	this.setButtonVisibility(kenKenGrid.mGridSize);
    	showDialog(0);
    	Thread t = new Thread() {
			public void run() {
				MainActivity.this.kenKenGrid.reCreate();
				MainActivity.this.mHandler.post(newGameReady);
			}
    	};
    	t.start();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
	    mProgressDialog = new ProgressDialog(this);
	    mProgressDialog.setTitle("Building puzzle");
	    mProgressDialog.setMessage("Please wait...");
	    mProgressDialog.setIndeterminate(false);
	    mProgressDialog.setCancelable(false);
	    return mProgressDialog;
    }
    
    public void setButtonVisibility(int gridSize) {
        switch(gridSize) {
        case 4:
          this.digit5.setVisibility(View.GONE);
          this.digit6.setVisibility(View.GONE);
          this.digit7.setVisibility(View.GONE);
          this.digit8.setVisibility(View.GONE);
          break;
        case 5:
          this.digit5.setVisibility(View.VISIBLE);
          this.digit6.setVisibility(View.GONE);
          this.digit7.setVisibility(View.GONE);
          this.digit8.setVisibility(View.GONE);
          break;
        case 6:
          this.digit5.setVisibility(View.VISIBLE);
          this.digit6.setVisibility(View.VISIBLE);
          this.digit7.setVisibility(View.GONE);
          this.digit8.setVisibility(View.GONE);
          break; 
        case 7:
            this.digit5.setVisibility(View.VISIBLE);
            this.digit6.setVisibility(View.VISIBLE);
            this.digit7.setVisibility(View.VISIBLE);
            this.digit8.setVisibility(View.GONE);
            break; 
        case 8:
            this.digit5.setVisibility(View.VISIBLE);
            this.digit6.setVisibility(View.VISIBLE);
            this.digit7.setVisibility(View.VISIBLE);
            this.digit8.setVisibility(View.VISIBLE);
            break; 
        }
		this.solvedText.setVisibility(View.GONE);
		this.newGame.setVisibility(View.GONE);
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
  	  this.solvedText.startAnimation(this.solvedAnimation);
  	}
    
    private void openHelpDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.aboutview, null); 
        new AlertDialog.Builder(MainActivity.this)
        .setTitle("Mathdoku Help")
        .setIcon(R.drawable.about)
        .setView(view)
        .setNeutralButton("Changes", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
              MainActivity.this.openChangesDialog();
          }
        })
        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
              //
          }
        })
        .show();  
    }
    
    private void openChangesDialog() {
      LayoutInflater li = LayoutInflater.from(this);
      View view = li.inflate(R.layout.changeview, null); 
      new AlertDialog.Builder(MainActivity.this)
      .setTitle("Changelog")
      .setIcon(R.drawable.about)
      .setView(view)
      .setNegativeButton("Close", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            //
        }
      })
      .show();  
    }
    public void newVersionCheck() {
        int pref_version = preferences.getInt("currentversion", -1);
        Editor prefeditor = preferences.edit();
        int current_version = getVersionNumber();
        if (pref_version == -1 || pref_version != current_version) {
          //new File(SaveGame.saveFilename).delete();
          prefeditor.putInt("currentversion", current_version);
          prefeditor.commit();
          this.openChangesDialog();
          return;
        }

    }
    public int getVersionNumber() {
        int version = -1;
          try {
              PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
              version = pi.versionCode;
          } catch (Exception e) {
              Log.e("Mathdoku", "Package name not found", e);
          }
          return version;
      }
}