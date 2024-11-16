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

        GridLayout gridLayout = findViewById(R.id.gridLayout);
        int cellSize = getResources().getDisplayMetrics().widthPixels / GRID_SIZE;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                TextView cell = new TextView(this);
                cell.setLayoutParams(new ViewGroup.LayoutParams(cellSize, cellSize));
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(14);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Number (1-9)");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                int number = Integer.parseInt(inputText);
                if (number >= 1 && number <= 9) {
                    board[row][col] = number;
                    cells[row][col].setText(String.valueOf(number));
                    cells[row][col].setTextColor(Color.BLUE); // Set color for user-entered numbers
                } else {
                    Toast.makeText(this, "Please enter a number between 1 and 9", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
