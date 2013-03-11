package net.cactii.mathdoku;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.DiscretePathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.preference.PreferenceManager;

public class GridCell {
	private final String SAVE_GAME_CELL_VERSION_01 = "CELL";
	private final String SAVE_GAME_CELL_VERSION_02 = "CELL.v2";

	// Index of the cell (left to right, top to bottom, zero-indexed)
	private int mCellNumber;
	// X grid position, zero indexed
	private int mColumn;
	// Y grid position, zero indexed
	private int mRow;
	// Value of the digit in the cell
	private int mCorrectValue;
	// User's entered value
	private int mUserValue;
	// Id of the enclosing cage
	private int mCageId;
	// String of the cage
	private String mCageText;
	// User's candidate digits
	private ArrayList<Integer> mPossibles;

	// X pixel position
	public float mPosX;
	// Y pixel position
	public float mPosY;

	// View context
	public GridView mContext;
	// Whether to show warning background (duplicate value in row/col)
	public boolean mShowWarning;
	// Whether to show cell as selected
	public boolean mSelected;
	// Player cheated (revealed this cell)
	public boolean mCheated;
	// Highlight user input isn't correct value
	private boolean mInvalidHighlight;

	public static final int BORDER_NONE = 0;
	public static final int BORDER_SOLID = 1;
	public static final int BORDER_WARN = 3;
	public static final int BORDER_CAGE_SELECTED = 4;

	public int[] mBorderTypes;

	private Paint mValuePaint;
	private Paint mBorderPaint;
	private Paint mCageSelectedPaint;

	private Paint mWrongBorderPaint;
	private Paint mCageTextPaint;
	private Paint mPossiblesPaint;
	private Paint mWarningPaint;
	private Paint mCheatedPaint;
	private Paint mSelectedPaint;

	public int mTheme;

	public GridCell(GridView context, int cell) {
		int gridSize = context.mGridSize;
		this.mContext = context;
		this.mCellNumber = cell;
		this.mColumn = cell % gridSize;
		this.mRow = (int) (cell / gridSize);
		this.mCageText = "";
		this.mCageId = -1;
		this.mCorrectValue = 0;
		this.mUserValue = 0;
		this.mShowWarning = false;
		this.mCheated = false;
		this.mInvalidHighlight = false;

		this.mPosX = 0;
		this.mPosY = 0;

		this.mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// this.mValuePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF,
		// Typeface.BOLD));

		this.mBorderPaint = new Paint();

		this.mWrongBorderPaint = new Paint();

		this.mCageSelectedPaint = new Paint();

		this.mWarningPaint = new Paint();

		this.mCheatedPaint = new Paint();

		this.mSelectedPaint = new Paint();

		this.mCageTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// this.mCageTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF,
		// Typeface.BOLD));

		this.mPossiblesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		this.mPossibles = new ArrayList<Integer>();

		this.setBorders(BORDER_NONE, BORDER_NONE, BORDER_NONE, BORDER_NONE);
	}

	public void setTheme(int theme) {
		this.mTheme = theme;

		this.mValuePaint.setColor(0xFF000000);
		this.mBorderPaint.setColor(0xFF000000);
		this.mBorderPaint.setStrokeWidth(2);
		this.mWrongBorderPaint.setColor(0xFFBB0000);
		this.mWrongBorderPaint.setStrokeWidth(2);
		// this.mCageSelectedPaint.setColor(0xFF9BCF00);
		this.mCageSelectedPaint.setStrokeWidth(3);
		this.mWarningPaint.setColor(0x50FF0000);
		this.mWarningPaint.setStyle(Paint.Style.FILL);
		this.mCheatedPaint.setColor(0x90ffcea0);
		this.mCheatedPaint.setStyle(Paint.Style.FILL);
		this.mSelectedPaint.setColor(0xD0F0D042);
		this.mSelectedPaint.setStyle(Paint.Style.FILL);
		this.mCageTextPaint.setColor(0xFF0000A0);
		this.mCageTextPaint.setTextSize(14);
		this.mPossiblesPaint.setColor(0xFF000000);
		this.mPossiblesPaint.setTextSize(10);
		this.mPossiblesPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF,
				Typeface.NORMAL));

		if (theme == GridView.THEME_CARVED) {
			this.mBorderPaint.setAntiAlias(true);
			this.mBorderPaint.setPathEffect(new DiscretePathEffect(20, 1));
			this.mWrongBorderPaint.setAntiAlias(true);
			this.mWrongBorderPaint.setPathEffect(new DiscretePathEffect(20, 1));
			this.mValuePaint.setTypeface(this.mContext.mFace);
			this.mCageTextPaint.setTypeface(this.mContext.mFace);
		} else if (theme == GridView.THEME_NEWSPAPER) {
			this.mBorderPaint.setAntiAlias(false);
			this.mBorderPaint.setPathEffect(null);
			this.mWrongBorderPaint.setAntiAlias(true);
			this.mWrongBorderPaint.setPathEffect(null);
			this.mValuePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.NORMAL));
			this.mCageTextPaint.setTypeface(Typeface.create(
					Typeface.SANS_SERIF, Typeface.NORMAL));
		} else if (theme == GridView.THEME_INVERT) {
			this.mBorderPaint.setColor(0xFFFFFFFF);
			this.mCageTextPaint.setColor(0xFFFFFFC0);
			this.mPossiblesPaint.setColor(0xFFFFFFFF);
			this.mBorderPaint.setStrokeWidth(3);
			this.mValuePaint.setColor(0xFFFFFFFF);
			this.mSelectedPaint.setColor(0xB0A0A030);
			this.mSelectedPaint.setStyle(Paint.Style.STROKE);
			this.mSelectedPaint.setStrokeWidth(5);
			this.mCageSelectedPaint.setStrokeWidth(5);
			this.mCageSelectedPaint.setColor(0xFF000000);

			// this.mBorderPaint.setPathEffect(new DiscretePathEffect(20, 1));
			// this.mWrongBorderPaint.setPathEffect(new DiscretePathEffect(20,
			// 1));
			this.mBorderPaint.setPathEffect(null);
			this.mWrongBorderPaint.setPathEffect(null);
			this.mWrongBorderPaint.setStrokeWidth(5);
			this.mBorderPaint.setAntiAlias(true);
			this.mWrongBorderPaint.setAntiAlias(true);

			// this.mBorderPaint.setAntiAlias(false);
			// this.mBorderPaint.setPathEffect(null);
			// this.mWrongBorderPaint.setAntiAlias(true);
			// this.mWrongBorderPaint.setPathEffect(null);
			this.mValuePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF,
					Typeface.NORMAL));
			this.mCageTextPaint.setTypeface(Typeface.create(
					Typeface.SANS_SERIF, Typeface.NORMAL));
		}
	}

	public String toString() {
		String str = "<cell:" + this.mCellNumber + " col:" + this.mColumn
				+ " row:" + this.mRow + " posX:" + this.mPosX + " posY:"
				+ this.mPosY + " val:" + this.mCorrectValue + ", userval: "
				+ this.mUserValue + ">";
		return str;
	}

	/*
	 * Sets the cells border type to the given values.
	 * 
	 * Border is BORDER_NONE, BORDER_SOLID, BORDER_WARN or BORDER_CAGE_SELECTED.
	 */
	public void setBorders(int north, int east, int south, int west) {
		int[] borders = new int[4];
		borders[0] = north;
		borders[1] = east;
		borders[2] = south;
		borders[3] = west;
		this.mBorderTypes = borders;
	}

	/* Returns the Paint object for the given border of this cell. */
	private Paint getBorderPaint(int border) {
		switch (this.mBorderTypes[border]) {
		case BORDER_NONE:
			return null;
		case BORDER_SOLID:
			return this.mBorderPaint;
		case BORDER_WARN:
			return this.mWrongBorderPaint;
		case BORDER_CAGE_SELECTED:
			return this.mCageSelectedPaint;
		}
		return null;
	}

	public int countPossibles() {
		return this.mPossibles.size();
	}

	public void clearPossibles() {
		this.mPossibles.clear();
	}

	public int getFirstPossible() {
		return this.mPossibles.get(0);
	}

	public void togglePossible(int digit) {
		if (this.mPossibles.indexOf(new Integer(digit)) == -1)
			this.mPossibles.add(digit);
		else
			this.mPossibles.remove(new Integer(digit));
		Collections.sort(mPossibles);
	}

	public int getUserValue() {
		return mUserValue;
	}

	public boolean isUserValueSet() {
		return mUserValue != 0;
	}

	public void setUserValue(int digit) {
		this.mPossibles.clear();
		this.mUserValue = digit;
		mInvalidHighlight = false;
	}

	public void clearUserValue() {
		setUserValue(0);
	}

	public boolean isUserValueCorrect() {
		return mUserValue == mCorrectValue;
	}

	/* Returns whether the cell is a member of any cage */
	public boolean CellInAnyCage() {
		return mCageId != -1;
	}

	public void setInvalidHighlight(boolean value) {
		this.mInvalidHighlight = value;
	}

	public boolean getInvalidHighlight() {
		return this.mInvalidHighlight;
	}

	/* Draw the cell. Border and text is drawn. */
	public void onDraw(Canvas canvas, boolean onlyBorders) {

		// Calculate x and y for the cell origin (topleft)
		float cellSize = (float) this.mContext.getMeasuredWidth()
				/ (float) this.mContext.mGridSize;
		this.mPosX = cellSize * this.mColumn;
		this.mPosY = cellSize * this.mRow;

		float north = this.mPosY;
		float south = this.mPosY + cellSize;
		float east = this.mPosX + cellSize;
		float west = this.mPosX;
		GridCell cellAbove = this.mContext.getCellAt(this.mRow - 1,
				this.mColumn);
		GridCell cellLeft = this.mContext
				.getCellAt(this.mRow, this.mColumn - 1);
		GridCell cellRight = this.mContext.getCellAt(this.mRow,
				this.mColumn + 1);
		GridCell cellBelow = this.mContext.getCellAt(this.mRow + 1,
				this.mColumn);

		// North
		Paint borderPaint = this.getBorderPaint(0);
		if (!onlyBorders && this.mBorderTypes[0] > 2)
			borderPaint = this.mBorderPaint;
		if (borderPaint != null) {
			canvas.drawLine(west, north, east, north, borderPaint);
		}

		// East
		borderPaint = this.getBorderPaint(1);
		if (!onlyBorders && this.mBorderTypes[1] > 2)
			borderPaint = this.mBorderPaint;
		if (borderPaint != null)
			canvas.drawLine(east, north, east, south, borderPaint);

		// South
		borderPaint = this.getBorderPaint(2);
		if (!onlyBorders && this.mBorderTypes[2] > 2)
			borderPaint = this.mBorderPaint;
		if (borderPaint != null)
			canvas.drawLine(west, south, east, south, borderPaint);

		// West
		borderPaint = this.getBorderPaint(3);
		if (!onlyBorders && this.mBorderTypes[3] > 2)
			borderPaint = this.mBorderPaint;
		if (borderPaint != null) {
			canvas.drawLine(west, north, west, south, borderPaint);
		}

		if (!onlyBorders) {
			if ((this.mShowWarning && this.mContext.mDupedigits)
					|| this.mInvalidHighlight)
				canvas.drawRect(west + 1, north + 1, east - 1, south - 1,
						this.mWarningPaint);
			if (this.mSelected)
				// canvas.drawRect(west + 1, north + 1, east - 1, south - 1,
				canvas.drawRect(west + 3, north + 3, east - 3, south - 3,
						this.mSelectedPaint);
			if (this.mCheated)
				canvas.drawRect(west + 1, north + 1, east - 1, south - 1,
						this.mCheatedPaint);
		} else {
			if (this.mBorderTypes[0] > 2)
				if (cellAbove == null)
					north += 2;
				else
					north += 1;
			if (this.mBorderTypes[3] > 2)
				if (cellLeft == null)
					west += 2;
				else
					west += 1;
			if (this.mBorderTypes[1] > 2)
				if (cellRight == null)
					east -= 3;
				else
					east -= 2;
			if (this.mBorderTypes[2] > 2)
				if (cellBelow == null)
					south -= 3;
				else
					south -= 2;
		}

		if (onlyBorders)
			return;

		// Cell value
		if (this.isUserValueSet()) {
			int textSize = (int) (cellSize * 3 / 4);
			this.mValuePaint.setTextSize(textSize);
			float leftOffset = cellSize / 2 - textSize / 4;
			float topOffset;
			if (this.mTheme == GridView.THEME_NEWSPAPER) {
				topOffset = cellSize / 2 + textSize * 2 / 5;
			} else {
				topOffset = cellSize / 2 + textSize / 3;
			}
			canvas.drawText("" + this.mUserValue, this.mPosX + leftOffset,
					this.mPosY + topOffset, this.mValuePaint);
		}
		// Cage text
		int cageTextSize = (int) (cellSize / 3);
		this.mCageTextPaint.setTextSize(cageTextSize);
		if (!this.mCageText.equals("")) {
			canvas.drawText(this.mCageText, this.mPosX + 2, this.mPosY
					+ cageTextSize, this.mCageTextPaint);

			// canvas.drawText(this.mCageText, this.mPosX + 2, this.mPosY + 13,
			// this.mCageTextPaint);
		}

		// Draw pencilled in digits.
		if (mPossibles.size() > 0) {
			Activity activity = mContext.mContext;
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(activity);
			if (prefs.getBoolean("maybe3x3", true)) {
				this.mPossiblesPaint.setFakeBoldText(true);
				this.mPossiblesPaint.setTextSize((int) (cellSize / 4.5));
				int xOffset = (int) (cellSize / 3);
				int yOffset = (int) (cellSize / 2) + 1;
				float xScale = (float) 0.21 * cellSize;
				float yScale = (float) 0.21 * cellSize;
				for (int i = 0; i < mPossibles.size(); i++) {
					int possible = mPossibles.get(i);
					float xPos = mPosX + xOffset + ((possible - 1) % 3)
							* xScale;
					float yPos = mPosY + yOffset + ((int) (possible - 1) / 3)
							* yScale;
					canvas.drawText(Integer.toString(possible), xPos, yPos,
							this.mPossiblesPaint);
				}
			} else {
				this.mPossiblesPaint.setFakeBoldText(false);
				mPossiblesPaint.setTextSize((int) (cellSize / 4));
				String possibles = "";
				for (int i = 0; i < mPossibles.size(); i++)
					possibles += Integer.toString(mPossibles.get(i));
				canvas.drawText(possibles, mPosX + 3, mPosY + cellSize - 5,
						mPossiblesPaint);
			}
		}
	}

	/**
	 * Write cell information to file.
	 * 
	 * @param writer
	 *            The buffered writer which is used to store information for
	 *            this cell.
	 * @throws IOException
	 */
	public void writeToFile(BufferedWriter writer) throws IOException {
		// Cell information will only be written in latest version. Order in
		// which fields are written must correspond with order of cellParts in
		// method restoreFromFile.
		writer.write(SAVE_GAME_CELL_VERSION_02
				+ SaveGame.FIELD_DELIMITER_LEVEL1);
		writer.write(mCellNumber + SaveGame.FIELD_DELIMITER_LEVEL1);
		writer.write(mRow + SaveGame.FIELD_DELIMITER_LEVEL1);
		writer.write(mColumn + SaveGame.FIELD_DELIMITER_LEVEL1);
		writer.write(mCageText + SaveGame.FIELD_DELIMITER_LEVEL1);
		writer.write(mCorrectValue + SaveGame.FIELD_DELIMITER_LEVEL1);
		writer.write(getUserValue() + SaveGame.FIELD_DELIMITER_LEVEL1);

		for (int possible : mPossibles) {
			writer.write(possible + SaveGame.FIELD_DELIMITER_LEVEL2);
		}
		writer.write(SaveGame.FIELD_DELIMITER_LEVEL1);

		writer.write(Boolean.toString(mInvalidHighlight) + SaveGame.FIELD_DELIMITER_LEVEL1);
		writer.write(Boolean.toString(mCheated) + SaveGame.FIELD_DELIMITER_LEVEL1);
		writer.write(Boolean.toString(mSelected));
		writer.write("\n");
	}

	/**
	 * Read cell information for a saved game file which was created before.
	 * 
	 * @param line
	 *            The line containing the cell information.
	 * @return True in case the given line contain cell information and is
	 *         processed correctly. False otherwise.
	 */
	public boolean restoreFromFile(String line) {
		String[] cellParts = line.split(SaveGame.FIELD_DELIMITER_LEVEL1);

		int cellInformationVersion = 0;
		if (cellParts[0].equals(SAVE_GAME_CELL_VERSION_01)) {
			cellInformationVersion = 1;
		} else if (cellParts[0].equals(SAVE_GAME_CELL_VERSION_02)) {
			cellInformationVersion = 2;
		} else {
			return false;
		}
		mCellNumber = Integer.parseInt(cellParts[1]);
		mRow = Integer.parseInt(cellParts[2]);
		mColumn = Integer.parseInt(cellParts[3]);
		mCageText = cellParts[4];
		mCorrectValue = Integer.parseInt(cellParts[5]);
		mUserValue = Integer.parseInt(cellParts[6]);
		if ((cellInformationVersion == 1 && cellParts.length == 8)
				|| cellInformationVersion > 1) {
			if (!cellParts[7].equals("")) {
				for (String possible : cellParts[7]
						.split(SaveGame.FIELD_DELIMITER_LEVEL2)) {
					togglePossible(Integer.parseInt(possible));
				}
			}
		}
		if (cellInformationVersion > 1) {
			mInvalidHighlight = Boolean.parseBoolean(cellParts[8]);
			mCheated = Boolean.parseBoolean(cellParts[9]);
			mSelected = Boolean.parseBoolean(cellParts[10]);
		}
		return true;
	}

	public int getCellNumber() {
		return mCellNumber;
	}

	public int getColumn() {
		return mColumn;
	}

	public int getRow() {
		return mRow;
	}

	public int getCorrectValue() {
		return mCorrectValue;
	}

	public void setCorrectValue(int newValue) {
		mCorrectValue = newValue;
	}

	public int getCageId() {
		return mCageId;
	}

	public void setCageId(int newCageId) {
		mCageId = newCageId;
	}

	public void clearCage() {
		mCageId = -1;
		mCageText = "";
	}

	public String getCageText() {
		return mCageText;
	}

	public void setCageText(String newCageText) {
		mCageText = newCageText;
	}

	public void SaveUndoInformation() {
		// Store old values of this cell
		Move move = new Move(this, this.mUserValue, this.mPossibles);
		this.mContext.AddMove(move);
	}

	public void Undo(int previousUserValue,
			ArrayList<Integer> previousPossibleValues) {
		mUserValue = previousUserValue;
		mPossibles.clear();
		if (previousPossibleValues != null) {
			for (int previousPossibleValue : previousPossibleValues) {
				mPossibles.add(previousPossibleValue);
			}
			Collections.sort(mPossibles);
		}
	}

	public void Select() {
		mContext.SetSelectedCell(this);
	}

	/**
	 * Check if the given digit is registered as a posibble value for this cell.
	 * 
	 * @param digit
	 *            The digit which is to be checked.
	 * @return True in case the digit is registered as a possible value for this
	 *         cell. False otherwise.
	 */
	public boolean hasPossible(int digit) {
		return (this.mPossibles.indexOf(new Integer(digit)) >= 0);
	}
}