package net.mathdoku.plus.sharedpuzzle;

import android.content.res.Resources;

import net.mathdoku.plus.R;

public class MathdokuPlusShareUri extends ShareUri {
    private static final int VERSION = 2;

    public MathdokuPlusShareUri(Resources resources) {
        super(resources.getString(R.string.shared_puzzle_scheme_mathdoku_plus),
              resources.getString(R.string.shared_puzzle_host_mathdoku_plus),
              resources.getString(R.string.shared_puzzle_path_prefix_mathdoku_plus), VERSION);
    }
}