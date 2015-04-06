package net.mathdoku.plus.sharedpuzzle;

import android.content.res.Resources;

import net.mathdoku.plus.R;

public class MathdokuOriginalShareUri extends ShareUri {
    private static final int VERSION = 2;

    public MathdokuOriginalShareUri(Resources resources) {
        super(resources.getString(R.string.shared_puzzle_scheme_mathdoku_original),
              resources.getString(R.string.shared_puzzle_host_mathdoku_original),
              resources.getString(R.string.shared_puzzle_path_prefix_mathdoku_original), VERSION);
    }
}