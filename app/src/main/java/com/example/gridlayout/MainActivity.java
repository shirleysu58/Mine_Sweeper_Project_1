package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private TextView[][] mineSweeperGrid;
    private boolean[][] isBomb;
    private boolean[][] hasRevealed;

    private boolean[][] placeFlag;
    private int revealedTiles = 0;
    private boolean isStateFlag = false;
    private int flagsLeft = 4;
    private int clock = 0;
    private boolean running = false;


    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<TextView>();
        mineSweeperGrid = new TextView[12][10];
        isBomb = new boolean[12][10];
        hasRevealed = new boolean[12][10];
        placeFlag = new boolean[12][10];

        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        LayoutInflater li = LayoutInflater.from(this);
        for (int i = 0; i<12; i++) {
            for (int j=0; j<10; j++) {
                TextView tv = (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.parseColor("lime"));
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cell_tvs.add(tv);
                mineSweeperGrid[i][j] = tv;
            }
        }

        TextView flagOrPickView = (TextView) findViewById(R.id.textViewFlag);
        flagOrPickView.setOnClickListener(this::onClickFlagOrDig);
        flagOrPickView.setText(getResources().getString(R.string.pick));

        placeRandomMines();
        runTimer();

    }
    private void placeRandomMines() { // part of mine sweeper set up
        int uniqueBombCnt = 0;
        while (uniqueBombCnt < 4) {
            int r= (int)(Math.random() * 12);
            int c = (int)(Math.random() * 10);

            if (isBomb[r][c]) { // no repeat bombs
                continue;
            }

            isBomb[r][c] = true;
            uniqueBombCnt++;
        }

    }

    private int numAdjacentMine(int i, int j) { // get adjacent bombs
        int[] row = {1,-1,0,0,1,-1,-1,1};
        int[] col = {0,0,1,-1,1,-1,1,-1};

        int adjBombCnt = 0;

        for (int k = 0; k < 8; k++) {
            int r1 = i + row[k];
            int c1 = j + col[k];

            if (r1 >= 0 && r1 < 12 && c1 >= 0 && c1 < 10 && isBomb[r1][c1]) {
                adjBombCnt++;
            }
        }
        return adjBombCnt;
    }
    private void revealAllAdjacentBFS(int i, int j) { // if adjcant has bomb, then stop there
        Queue<int[]> queue = new LinkedList();
        int[] row = {1,-1,0,0,1,-1,-1,1};
        int[] col = {0,0,1,-1,1,-1,1,-1};

        boolean[][] hasVisited = new boolean[12][10];
        queue.add(new int[]{i, j}); // add to queue
        hasVisited[i][j] = true; //
        while (!queue.isEmpty()) {
            int[] curr = queue.poll(); // pop it out
            for (int k = 0; k < 8; k++) { // check all adjacent
                int r1 = curr[0] + row[k];
                int c1 = curr[1] + col[k];

                if (r1 >= 0 && r1 < 12 && c1 >= 0 && c1 < 10 && !hasVisited[r1][c1] && !placeFlag[r1][c1]) {
                    int adjBomb = numAdjacentMine(r1, c1);
                    if (adjBomb == 0) {
                        queue.add(new int[]{r1, c1}); // add no adj bomb to queue
                    }
                    // reveal tile (bomb adj)
                    revealTile(mineSweeperGrid[r1][c1], adjBomb);
                    hasRevealed[r1][c1] = true;

                    // update has visited grid
                    hasVisited[r1][c1] = true;
                }
            }
        }
    }


    private void revealTile(TextView tv, int numAdjBomb) {
        tv.setTextColor(Color.GRAY);
        tv.setBackgroundColor(Color.LTGRAY);
        tv.setText(String.valueOf(numAdjBomb));
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }


    public void onEndGame(boolean wonGame) {
        running = false;
        Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra("wonGame", wonGame);
        intent.putExtra("time", clock);
        startActivity(intent);
    }

    private void runTimer() {

        final TextView timeView = (TextView) findViewById(R.id.textView);
        final Handler handler = new Handler();
        running = true;

        handler.post(new Runnable() {
            @Override
            public void run() {
                timeView.setText(String.valueOf(clock));

                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void onUpdateFlagCount(int count) {
        TextView flagCountTv = findViewById(R.id.textViewFlagCount);
        flagCountTv.setText(String.valueOf(count));
    }

    private void onClickFlagOrDig(View view) {
        TextView tv = (TextView) view;
        if (isStateFlag) { // if is currently flag --> transform into dig
            isStateFlag = false;
            tv.setText(getResources().getString(R.string.pick));
        } else {
            isStateFlag = true;
            tv.setText(getResources().getString(R.string.flag)); // flag icon
        }
    }

    private boolean hasWon() {
        int count = 0;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 10; j++) {
                if (hasRevealed[i][j]) {
                    count++;
                }
            }
        }

        if (count == 116) {
            return true;
        }
        return false;
    }

    public void onClickTV(View view){
        TextView tv = (TextView) view;

        int n = findIndexOfCellTextView(tv);
        int i = n/COLUMN_COUNT; // row idx
        int j = n%COLUMN_COUNT; // col idx

        if (isStateFlag) {
            if (!hasRevealed[i][j] && flagsLeft > 0 && !placeFlag[i][j] ) {
                tv.setText(getResources().getString(R.string.flag)); // set it to flag emoji, if it is
                flagsLeft--;
                onUpdateFlagCount(flagsLeft);
                placeFlag[i][j] = true;
            } else if (placeFlag[i][j]) {
                placeFlag[i][j] = false;
                flagsLeft++;
                tv.setText("");
                onUpdateFlagCount(flagsLeft);
            }
        } else { // digging
            if (!placeFlag[i][j]) { // cannot click on a flag tile
                if (isBomb[i][j]) {
                    tv.setText(getResources().getString(R.string.mine));
                    // tv.setTextColor(Color.GREEN);
                    tv.setTextColor(Color.GRAY);
                    tv.setBackgroundColor(Color.LTGRAY);
                    onEndGame(false);
                    // has lost transition
                } else {
                    hasRevealed[i][j] = true;
                    int numAdjBomb = numAdjacentMine(i, j);
                    revealTile(mineSweeperGrid[i][j], numAdjBomb);
                    if (numAdjBomb == 0) {
                        revealAllAdjacentBFS(i,j);
                    }
                    if (hasWon()) {
                        onEndGame(true);
                        // transition to winning page
                    }
                }
            }

        }
    }
}
