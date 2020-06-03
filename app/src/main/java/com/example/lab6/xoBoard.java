package com.example.lab6;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class xoBoard extends BaseAdapter {
    private Context context;
    private int player; //Current player (for move)
    private int[][] board = new int[3][3]; //game board

    //Constructor
    public xoBoard(Context cont, String moves) {
        context = cont;

        //Filing empty board by moves history
        int mvs = 0;
        for (String move : moves.split("(?!^)")) {
            if (!move.equals(""))
                this.move(Integer.parseInt(move), mvs++ % 2);
        }
        //Setting Current Player
        player = mvs % 2;
    }

    //Method make move
    private boolean move(int col, int player) {
        int row = 2 - ((col - 1) / 3);
        int coll = (col - 1) % 3;

        if (board[row][coll] != 0)
            return false;

        board[row][coll] = player + 1;
        return true;
    }

    //Public method making move for playing user
    public xoBoard add(long col) {
        if (this.move((int) col, player++ % 2))
            return this;
        return null;
        //return only when moves are correct
    }


    @Override //Must be in adapter
    public int getCount() {
        return 3 * 3;
    }

    @Override //Must be in adapter
    public Object getItem(int position) {
        return position % 3;
    }

    @Override //Must be in adapter
    public long getItemId(int position) {
        return position % 3;
    }

    //Method for generate view of singe element in greed
    @Override //Must be in adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        //Create element - ImageView
        ImageView iv = new ImageView(context);
        //Calculate position of element into board array
        int col = position % 3;
        int row = 2 - position / 3;

        //Set appropriate image
        switch (board[row][col]) {
            case 0:
                iv.setImageResource(R.drawable.circle);
                break;
            case 1:
                iv.setImageResource(R.drawable.player1);
                break;
            case 2:
                iv.setImageResource(R.drawable.player2);
                break;
        }

        //Seting size of image - 120x120 px
        iv.setBackgroundColor(0xFFFFFFFF);

        iv.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        return iv;
    }

    //Method to check game state, if anybody wins, return which player win (or 0)
    public int checkWin() {
        int inRow = 0;

        for (int row = 0; row < 3; row++, inRow = 0)
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2] && board[row][2] != 0)
                return board[row][0];

        for (int col = 0; col < 3; col++, inRow = 0)
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col] && board[2][col] != 0)
                return board[0][col];

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[2][0] != 0)
            return board[0][2];

        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[2][2] != 0)
            return board[0][0];

        return 0;
    }
}