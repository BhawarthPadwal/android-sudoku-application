package com.example.sudokugrid;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sudokugrid.classes.Move;
import com.example.sudokugrid.classes.SudokuSolver;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 9; // 9x9 grid
    private int[][] board = {
            {1, 0, 0, 4, 8, 9, 0, 0, 6},
            {7, 3, 0, 0, 0, 0, 0, 4, 0},
            {0, 0, 0, 0, 0, 1, 2, 9, 5},
            {0, 0, 7, 1, 2, 0, 6, 0, 0},
            {5, 0, 0, 7, 0, 3, 0, 0, 8},
            {0, 0, 6, 0, 9, 5, 7, 0, 0},
            {9, 1, 4, 6, 0, 0, 0, 0, 0},
            {0, 2, 0, 0, 0, 0, 0, 3, 7},
            {8, 0, 0, 5, 1, 2, 0, 0, 4}
    };

    /*private int[][] board = {
            {7, 0, 2, 0, 5, 0, 6, 0, 0},
            {0, 0, 0, 0, 0, 3, 0, 0, 0},
            {1, 0, 0, 0, 0, 9, 5, 0, 0},
            {8, 0, 0, 0, 0, 0, 0, 9, 0},
            {0, 4, 3, 0, 0, 0, 7, 5, 0},
            {0, 9, 0, 0, 0, 0, 0, 0, 8},
            {0, 0, 9, 7, 0, 0, 0, 0, 5},
            {0, 0, 0, 2, 0, 0, 0, 0, 0},
            {0, 0, 7, 0, 4, 0, 2, 0, 3}
    };*/

    private TextView[][] cells = new TextView[GRID_SIZE][GRID_SIZE];
    private int[][] solvedBoard = new int[GRID_SIZE][GRID_SIZE];
    private Stack<Move> moveStack = new Stack<>();
    private int selectedRow = -1;
    private int selectedCol = -1;
    private int mistakeCount = 0;
    private int hintCount = 3;
    private CardView undoBtn, hintBtn, pauseBtn;
    private TextView hintCountTv, mistakesTv, timerTv;
    ImageView pausePlayIv;
    private long startTime = 0;
    private boolean isRunning = false;
    private long pauseOffset = 0;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsedMillis = System.currentTimeMillis() - startTime;
            int seconds = (int) (elapsedMillis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timerTv.setText(String.format("%02d:%02d", minutes, seconds));

            // Run the timer again after 1 second
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        callAllIds();

        startTimer();


        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(board[i], 0, solvedBoard[i], 0, GRID_SIZE);
        }
        /// Alternative Method for copying array
        /*for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                solvedBoard[i][j] = board[i][j];
            }
        }*/
        SudokuSolver.solvedBoard(solvedBoard);
        setUpGridValues();
        setUpNumberButtons();
        setUpUndoButton();
        setUpHintButton();
        pause_play_timer();

        if (isBoardSolved()) {
            Toast.makeText(this, "Congratulations! You've solved the Sudoku!", Toast.LENGTH_SHORT).show();
            clearUserInputs(); // Clear the inputs
            stopTimer(); // Stop the timer if needed
        }

    }

    private void callAllIds() {
        undoBtn = findViewById(R.id.undo_btn);
        hintBtn = findViewById(R.id.hint_btn);
        pauseBtn = findViewById(R.id.pause_btn);
        hintCountTv = findViewById(R.id.hint_text);
        mistakesTv = findViewById(R.id.mistake_count);
        timerTv = findViewById(R.id.stop_watch);
        pausePlayIv = findViewById(R.id.pause_play_ic);
    }

    private void pause_play_timer() {
        pauseBtn.setOnClickListener(view -> {
            if (isRunning) {
                pauseTimer(); // Pause the timer
                pausePlayIv.setImageResource(R.drawable.baseline_play_arrow_24); // Change to play icon
            } else {
                resumeTimer(); // Resume the timer
                pausePlayIv.setImageResource(R.drawable.baseline_pause_24); // Change to pause icon
            }
            isRunning = !isRunning; // Toggle the flag
        });
    }

    private void setUpHintButton() {
        hintBtn.setOnClickListener(view -> {
            if (selectedRow != -1 && selectedCol != -1) {
                if (cells[selectedRow][selectedCol].getText().toString().isEmpty()) {
                    if (hintCount > 0) {
                        int hintValue = solvedBoard[selectedRow][selectedCol];
                        board[selectedRow][selectedCol] = hintValue;
                        cells[selectedRow][selectedCol].setText(String.valueOf(hintValue));
                        cells[selectedRow][selectedCol].setTextColor(Color.BLUE); // Use blue for hints
                        cells[selectedRow][selectedCol].setEnabled(false); // Disable editing for hint cells
                        hintCount -= 1;
                        hintCountTv.setText(String.valueOf(hintCount));
                    } else {
                        Toast.makeText(this, "Oops! you have consumed all your hints!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Please remove entered number first!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Select a cell first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpUndoButton() {
        undoBtn.setOnClickListener(view -> {
            if (!moveStack.isEmpty()) {
                Move lastMove = moveStack.pop();
                board[lastMove.row][lastMove.col] = lastMove.previousValue;
                cells[lastMove.row][lastMove.col].setText(lastMove.previousValue == 0 ? "" : String.valueOf(lastMove.previousValue));

                if (lastMove.previousValue == 0 || lastMove.previousValue == solvedBoard[lastMove.row][lastMove.col]) {
                    cells[selectedRow][selectedCol].setTextColor(Color.BLUE);
                    cells[selectedRow][selectedCol].setBackgroundResource(R.drawable.cell_border);
                } else {
                    cells[selectedRow][selectedCol].setTextColor(Color.WHITE);
                    cells[selectedRow][selectedCol].setBackgroundColor(Color.RED);
                }
            }
        });

    }

    private void setUpGridValues() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        int cellSize = getResources().getDisplayMetrics().widthPixels / GRID_SIZE;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                TextView cell = new TextView(this);
                cell.setLayoutParams(new ViewGroup.LayoutParams(cellSize, cellSize));
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(20);

                int cellValue = board[row][col];
                if (cellValue != 0) {
                    cell.setText(String.valueOf(cellValue)); // Display initial number
                    cell.setTextColor(Color.BLACK); // Color for immutable cells
                    cell.setEnabled(false); // Make initial cells unclickable
                } else {
                    cell.setText(""); // Show blank for empty cells
                    cell.setTextColor(Color.BLUE); // Editable cell color
                    int finalRow = row;
                    int finalCol = col;
                    cell.setOnClickListener(view -> onCellClick(finalRow, finalCol)); // Allow editing
                }

                // Set border for each cell
                cell.setBackgroundResource(R.drawable.cell_border);

                gridLayout.addView(cell);
                cells[row][col] = cell;
            }
        }
    }

    private void onCellClick(int row, int col) {

        /*To check whether selected cell is not the one in which you have already entered
        an input which is incorrect*/

        if (selectedRow != -1 && selectedCol != -1) {
            if (cells[selectedRow][selectedCol].getCurrentTextColor() != Color.WHITE) {
                cells[selectedRow][selectedCol].setBackgroundResource(R.drawable.cell_border);
            }
        }

        selectedRow = row;
        selectedCol = col;

        /*To keep the uncorrected cell's background colour as it is even after selecting.
        It will only change after correct input is entered into it*/

        if (cells[selectedRow][selectedCol].getCurrentTextColor() == Color.WHITE) {
            cells[selectedRow][selectedCol].setBackgroundColor(Color.RED);
        } else {
            // The current selected cell will be indicated in light gray
            cells[selectedRow][selectedCol].setBackgroundColor(Color.LTGRAY);
        }

        //Toast.makeText(this, "Selected cell (" + (row + 1) + ", " + (col + 1) + ")", Toast.LENGTH_SHORT).show();
    }

    private void setUpNumberButtons() {
        int[] buttonIds = {
                R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9
        };

        for (int i = 0; i < buttonIds.length; i++) {
            int number = i + 1;
            findViewById(buttonIds[i]).setOnClickListener(view -> {
                if (selectedRow != -1 && selectedCol != -1) {
                    // To store last move for undo functionality
                    int previousValue = board[selectedRow][selectedCol];
                    moveStack.push(new Move(selectedRow, selectedCol, previousValue, number));
                    if (solvedBoard[selectedRow][selectedCol] == number) {
                        cells[selectedRow][selectedCol].setTextColor(Color.BLUE);
                        cells[selectedRow][selectedCol].setBackgroundResource(R.drawable.cell_border);
                    } else {
                        cells[selectedRow][selectedCol].setTextColor(Color.WHITE);
                        cells[selectedRow][selectedCol].setBackgroundColor(Color.RED);
                        mistakeCount += 1;
                        String mistakes = mistakeCount + " / 3";
                        mistakesTv.setText(mistakes);
                        Toast.makeText(this, "Total Mistake: " + mistakeCount, Toast.LENGTH_SHORT).show();
                        if (mistakeCount >= 3) {
                            Toast.makeText(this, "Game Over! You made 3 mistakes.", Toast.LENGTH_SHORT).show();
                            clearUserInputs(); // Clear the inputs
                            stopTimer(); // Stop the timer if needed
                        }
                    }
                    board[selectedRow][selectedCol] = number;
                    cells[selectedRow][selectedCol].setText(String.valueOf(number));

                    // Check if the board is solved after the move
                    if (isBoardSolved()) {
                        Toast.makeText(this, "Congratulations! You've solved the Sudoku!", Toast.LENGTH_SHORT).show();
                        clearUserInputs(); // Clear the inputs
                        stopTimer(); // Stop the timer if needed
                    }
                } else {
                    Toast.makeText(this, "Select a cell first!", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private boolean isBoardSolved() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (board[row][col] != solvedBoard[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void clearUserInputs() {
        mistakeCount = 0;
        String mistakes = mistakeCount + " / 3";
        mistakesTv.setText(mistakes);
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (cells[row][col].isEnabled()) {
                    cells[row][col].setText("");
                    board[row][col] = 0;
                    cells[row][col].setBackgroundResource(R.drawable.cell_border);
                    cells[row][col].setTextColor(Color.BLUE);
                }
            }
        }
        Toast.makeText(this, "Game reset!", Toast.LENGTH_SHORT).show();
    }


    private void startTimer() {
        startTime = System.currentTimeMillis();
        isRunning = true;
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        isRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void pauseTimer() {
        pauseOffset = System.currentTimeMillis() - startTime;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void resumeTimer() {
        startTime = System.currentTimeMillis() - pauseOffset;
        startTimer();
    }

}
