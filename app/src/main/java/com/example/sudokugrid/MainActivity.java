package com.example.sudokugrid;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sudokugrid.classes.SudokuSolver;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 9; // 9x9 grid
    private int[][] board = {
            {7, 0, 2, 0, 5, 0, 6, 0, 0},
            {0, 0, 0, 0, 0, 3, 0, 0, 0},
            {1, 0, 0, 0, 0, 9, 5, 0, 0},
            {8, 0, 0, 0, 0, 0, 0, 9, 0},
            {0, 4, 3, 0, 0, 0, 7, 5, 0},
            {0, 9, 0, 0, 0, 0, 0, 0, 8},
            {0, 0, 9, 7, 0, 0, 0, 0, 5},
            {0, 0, 0, 2, 0, 0, 0, 0, 0},
            {0, 0, 7, 0, 4, 0, 2, 0, 3}
    };

    private TextView[][] cells = new TextView[GRID_SIZE][GRID_SIZE];
    private int[][] solvedBoard = new int[GRID_SIZE][GRID_SIZE];
    private int selectedRow = -1;
    private int selectedCol = -1;
    private int mistakeCount = 0;

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

        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(board[i], 0, solvedBoard[i], 0,GRID_SIZE);
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
                    if (solvedBoard[selectedRow][selectedCol] == number) {
                        cells[selectedRow][selectedCol].setTextColor(Color.BLUE);
                        cells[selectedRow][selectedCol].setBackgroundResource(R.drawable.cell_border);
                    } else {
                        cells[selectedRow][selectedCol].setTextColor(Color.WHITE);
                        cells[selectedRow][selectedCol].setBackgroundColor(Color.RED);
                        mistakeCount += 1;
                        Toast.makeText(this, "Total Mistake: " + mistakeCount, Toast.LENGTH_SHORT).show();
                    }
                    board[selectedRow][selectedCol] = number;
                    cells[selectedRow][selectedCol].setText(String.valueOf(number));
                } else {
                    Toast.makeText(this, "Select a cell first!", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
