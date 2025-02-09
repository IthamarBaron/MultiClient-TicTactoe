package com.example.demo1;

public class GameState {
    private char[][] board;
    private char currentTurn; // 'X' or 'O'

    public GameState() {
        this.board = new char[3][3];  // Initialize an empty board
        this.currentTurn = 'W' ;
    }

    public char[][] getBoard() {
        return board;
    }

    public char getCurrentTurn() {
        return currentTurn;
    }

    public void switchTurn() {
        if (currentTurn == 'w')
        {
            this.currentTurn = 'X';
            return;
        }
        this.currentTurn = (currentTurn == 'X') ? 'O' : 'X';
    }
    public void makeMove(int row, int col, char symbol) {
        if (board[row][col] == '\0') {
            board[row][col] = symbol;
        }
    }

    public boolean isCellEmpty(int row, int col) {
        return board[row][col] == '\0';
    }

    public char checkWinner() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != '\0' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0];
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (board[0][i] != '\0' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return board[0][i];
            }
        }

        // Check diagonals
        if (board[0][0] != '\0' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0];
        }
        if (board[0][2] != '\0' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2];
        }

        return '\0'; // No winner yet
    }

    public boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '\0') {
                    return false;
                }
            }
        }
        return true;
    }
}
