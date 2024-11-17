package com.example.sudokugrid.classes;

public class Move {

    public int row;
    public int col;
    public int previousValue;
    public int newValue;

    public Move(int row, int col, int previousValue, int newValue) {
        this.row = row;
        this.col = col;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }
}
