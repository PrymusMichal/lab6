package com.example.lab6;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONObject;

//4 in Row gameplay
public class xo extends AppCompatActivity {

    //Constant variables for communicate whit other componets
    public static final String STATUS = "Status";
    public static final String MOVES = "Moves";
    public static final String GAME_ID = "Game_id";
    public static final String PLAYER = "Player";
    public static final int NEW_GAME = 0;
    public static final int YOUR_TURN = 1;
    public static final int WAIT = 2;
    public static final int ERROR = 3;
    public static final int CONNECTION = 4;
    public static final int NETWORK_ERROR = 5;
    public static final int WIN = 6;
    public static final int LOSE = 7;

    //Actual status
    private int status;
    //Game id
    private int game_id;
    //game moves
    private String moves;
    //player number
    private int player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xo);

        //Geting actual game status, or 0
        status = getIntent().getIntExtra(inRow.STATUS, inRow.NEW_GAME);
        //Geting actual game id, or 0
        game_id = getIntent().getIntExtra(inRow.GAME_ID, inRow.NEW_GAME);
        //Geting player number, or 1
        player = getIntent().getIntExtra(inRow.PLAYER, 1);
        //show appropriate message above game board
        hints(status);

        //Seting game board as adapter of xo_gridView
        GridView gv = findViewById(R.id.xo_gridView);
        //in inRowBoard adapter constructor we put History of Moves as initialization
        moves = getIntent().getStringExtra(inRow.MOVES);
        gv.setAdapter(new xoBoard(this, moves));
        //Listner for clicking on element
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //You can move only if Your turn (not Waiting Status)
                if (status != xo.WAIT) {
                    //User cannot move
                    status = xo.WAIT;
                    //Show hint about sending move
                    hints(xo.CONNECTION);

                    int position = arg2+1;
                    //Getting game (inRowBoard Adapter)
                    GridView gv = findViewById(R.id.xo_gridView);
                    xoBoard game = (xoBoard) gv.getAdapter();
                    //Make Move
                    if (game.add(position) != null)
                        gv.setAdapter(game);
                    else
                        hints(inRow.ERROR);

                    //Creating intent for custom Service - sending Move to server
                    Intent intencja = new Intent(
                            getApplicationContext(),
                            HttpService.class);
                    //Creating PendingIntent - for response
                    PendingIntent pendingResult = createPendingResult(HttpService.XO_GAME, new Intent(), 0);

                    if (game_id == xo.NEW_GAME) {
                        //new game
                        //Set data - URL
                        intencja.putExtra(HttpService.URL, HttpService.XO);
                        //Set data - method of request
                        intencja.putExtra(HttpService.METHOD, HttpService.POST);
                    } else {
                        //existing game
                        //Set data - URL
                        intencja.putExtra(HttpService.URL, HttpService.XO + game_id);
                        //Set data - method of request
                        intencja.putExtra(HttpService.METHOD, HttpService.PUT);
                    }

                    //Set data - parameters
                    intencja.putExtra(HttpService.PARAMS, "moves=" + moves + position);
                    //Set data - intent for result
                    intencja.putExtra(HttpService.RETURN, pendingResult);
                    //Start unBound Service in another Thread
                    startService(intencja);
                }
            }
        });
    }

    //When Service return answer from server
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Result moves
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HttpService.XO_GAME) {
            try {
                JSONObject response = new JSONObject(data.getStringExtra(HttpService.RESPONSE));
                if (resultCode == 200) {
                    //ok
                    if (game_id == 0)
                        game_id = response.getInt("game_id");

                    //check game status
                    GridView gv = findViewById(R.id.xo_gridView);
                    xoBoard game = (xoBoard) gv.getAdapter();
                    int game_status = game.checkWin();
                    if (game_status == 0)
                        //next turn
                        hints(inRow.WAIT);
                    else {
                        if (game_status == player)
                            //You win
                            hints(inRow.WIN);
                        else
                            //You Lose
                            hints(inRow.LOSE);
                    }

                } else {
                    //Network error
                    if (resultCode == 500)
                        hints(inRow.NETWORK_ERROR);
                    else
                        //other error
                        hints(inRow.ERROR);
                    //Show serwer error message
                    Log.d("DEBUG", response.getString("http_status"));
                }
                //set refresh after 5 sec
                Thread.sleep(1000);
                refresh(null);

            } catch (Exception ex) {
                //Json handler error
                hints(inRow.ERROR);
                ex.printStackTrace();
            }

        } else if (requestCode == HttpService.REFRESH) {
            //Refresh board
            try {
                //Parse response from server
                JSONObject response = new JSONObject(data.getStringExtra(HttpService.RESPONSE));
                //new adapter with moves
                GridView gv = findViewById(R.id.xo_gridView);
                //Create new board with geted moves
                moves = response.getString("moves");
                xoBoard game = new xoBoard(this, moves);
                gv.setAdapter(game);

                //check whose turn
                if (response.getInt("status") == player) {
                    if (game.checkWin() == player) {
                        hints(xo.WIN);
                    } else if (game.checkWin() != 0) {
                        hints(xo.LOSE);
                    } else {
                        status = xo.YOUR_TURN;
                        hints(status);
                    }
                } else {
                    //Cal refresh again after 5sec, because it's not your turn
                    Thread.sleep(2000);
                    refresh(null);
                }

            } catch (Exception ex) {
                //Json handler error
                ex.printStackTrace();
            }
        }
    }


    //Set status into TextView (Hint) from String Resource
    private void hints(int status) {
        TextView hint = findViewById(R.id.xoHint);
        switch (status) {
            case inRow.YOUR_TURN:
                hint.setText(getString(R.string.your_turn));
                break;
            case inRow.WAIT:
                hint.setText(getString(R.string.wait));
                break;
            case inRow.ERROR:
                hint.setText(getString(R.string.error));
                break;
            case inRow.CONNECTION:
                hint.setText(getString(R.string.connection));
                break;
            case inRow.NETWORK_ERROR:
                hint.setText(getString(R.string.network_error));
                break;
            case inRow.WIN:
                hint.setText(getString(R.string.win));
                break;
            case inRow.LOSE:
                hint.setText(getString(R.string.lose));
                break;
            default:
                hint.setText(getString(R.string.new_game));
                break;
        }
    }

    //Menu creating
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    //Refresh game
    public void refresh(MenuItem item) {
        //Creating intent for custom Service - sending request to serwer
        Intent intencja = new Intent(
                getApplicationContext(),
                HttpService.class);
        //Creating PendingIntent - for getting result
        PendingIntent pendingResult = createPendingResult(HttpService.REFRESH, new Intent(), 0);
        //Set data - URL
        intencja.putExtra(HttpService.URL, HttpService.XO + game_id);
        //Set data - method of request
        intencja.putExtra(HttpService.METHOD, HttpService.GET);
        //Set data - intent for result
        intencja.putExtra(HttpService.RETURN, pendingResult);
        //Start unBound Service in another Thread
        startService(intencja);
    }
}