package com.example.lab6;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GamesList extends AppCompatActivity {

    private int game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Bundle extras =
                getIntent().getExtras();
        game = extras.getInt("gra");
        SwipeRefreshLayout swipeLayout =
                (SwipeRefreshLayout)
                        findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(
                new
                        SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                refreshGameList();
                            }
                        }
        );



    }

    public void refreshGameList(){
        ProgressBar spinner =
                (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        Snackbar.make(findViewById(R.id.main_list),
                getString(R.string.refresh),
                Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        Intent intencja = new Intent(
                getApplicationContext(),
                HttpService.class);
        PendingIntent pendingResult =
                createPendingResult(HttpService.GAMES_LIST,
                        new Intent(),0);
        if(game == R.id.inRow){
            intencja.putExtra(HttpService.URL,
                    HttpService.LINES);
        }else{
            //TODO - geting ticTacToe games list
        }
        intencja.putExtra(HttpService.METHOD,
                HttpService.GET);
        intencja.putExtra(HttpService.RETURN,
                pendingResult);
        startService(intencja);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (HttpService.GAMES_LIST==1) {
            ProgressBar spinner = (ProgressBar)
                    findViewById(R.id.progressBar1);
            spinner.setVisibility(View.GONE);
            SwipeRefreshLayout swipeLayout =
                    (SwipeRefreshLayout)
                            findViewById(R.id.swipe_container);
            swipeLayout.setRefreshing(false);
            try {
                JSONObject response = new
                        JSONObject(data.getStringExtra(HttpService.RESPONSE));
                if (response.getInt("games_count") > 0) {
                    TextView no_game =
                            (TextView) findViewById(R.id.empty);
                    no_game.setVisibility(View.GONE);
                    JSONArray games = new
                            JSONArray(response.getString("games"));
                    ArrayList<String> items = new
                            ArrayList<String>();
                    for (int i = 0;
                         i < response.getInt("games_count"); i++) {
                        JSONObject game =
                                games.getJSONObject(i);
                        items.add("ID: " + game.getString("id"));
                    }
                    ArrayAdapter<String> gamesAdapter = new ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, items);
                    ListView list =
                            (ListView) findViewById(R.id.listView);
                    list.setAdapter(gamesAdapter);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (HttpService.GAME_INFO==1) {
            ProgressBar spinner = (ProgressBar)
                    findViewById(R.id.progressBar1);
            spinner.setVisibility(View.GONE);
            if (game == R.id.inRow) {
                Intent intencja = new
                        Intent(getApplicationContext(), inRow.class);
                try {
                    JSONObject response = new
                            JSONObject(data.getStringExtra(HttpService.RESPONSE));
                    intencja.putExtra(inRow.GAME_ID,
                            response.getInt("id"));
                    if (response.getInt("status") == 0 &&
                            response.getInt("player1") == 2) {
                        intencja.putExtra(inRow.STATUS,
                                inRow.YOUR_TURN);
                    } else if (response.getInt("status")
                            == 1 && response.getInt("player1") == 1) {
                        intencja.putExtra(inRow.STATUS,
                                inRow.YOUR_TURN);
                    } else if (response.getInt("status")
                            == 2 && response.getInt("player1") == 2) {
                        intencja.putExtra(inRow.STATUS,
                                inRow.YOUR_TURN);
                    } else
                        intencja.putExtra(inRow.STATUS,
                                inRow.WAIT);
                    intencja.putExtra(inRow.PLAYER,
                            response.getInt("player1"));
                    intencja.putExtra(inRow.MOVES,
                            response.getString("moves"));
                    startActivity(intencja);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (game == R.id.ticTac) {
                //TODO - start chosen game for TicTacToe
            }
        }
    }

    public void startGame()
    {
        Intent intencja=null;
        switch (game) {
            case R.id.inRow:
                intencja = new
                        Intent(getApplicationContext(), inRow.class);
                intencja.putExtra(inRow.STATUS,
                        inRow.NEW_GAME);
                intencja.putExtra(inRow.MOVES, "");
                break;
            default:
                //TODO - when gamer choose TicTacToe game
                break;
        }
        startActivity(intencja);
    }

}
