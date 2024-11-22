package org.example;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Board extends JPanel {
    private static final long serialVersionUID = 6195235521361212179L;

    private final int NUM_IMAGES = 13;
    
    private final int CELL_SIZE = 15;

    private final int COVER_FOR_CELL = 10;

    private final int MARK_FOR_CELL = 10;

    private final int EMPTY_CELL = 0;

    private final int MINE_CELL = 9;

    private final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;

    private final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

    private final int DRAW_MINE = 9;

    private final int DRAW_COVER = 10;

    private final int DRAW_MARK = 11;

    private final int DRAW_WRONG_MARK = 12;

    private int[] field;
    
    private boolean inGame;

    private int mines_left;

    private Image[] img;

    private int mines = 40;

    private int rows = 16;

    private int cols = 16;

    private int all_cells;

    private JLabel statusbar;

    private final Random random;

    public Board(JLabel statusbar) {
        this.statusbar = statusbar;

        img = new Image[NUM_IMAGES];
        String basePath = "/Users/Nada/Downloads/mineGame/images/";

        for (int i = 0; i < NUM_IMAGES; i++) {
            img[i] = new ImageIcon(basePath + i + ".gif").getImage();
        }

        setDoubleBuffered(true);
        addMouseListener(new MinesAdapter());
        random = new Random();
        newGame();
    }

    public void newGame() {
        initializeField();
        placeMines();
        statusbar.setText(Integer.toString(mines_left));
    }

    private void initializeField() {
        inGame = true;
        mines_left = mines;
        all_cells = rows * cols;
        field = new int[all_cells];

        for (int i = 0; i < all_cells; i++) {
            field[i] = COVER_FOR_CELL;
        }
    }

    private void placeMines() {
        int i = 0;

        while (i < mines) {
            int position = random.nextInt(all_cells);

            if (field[position] != COVERED_MINE_CELL) {
                field[position] = COVERED_MINE_CELL;
                updateSurroundingCells(position);
                i++;
            }
        }
    }

    private void updateSurroundingCells(int position) {
        int current_col = position % cols;

        if (current_col > 0) {
            incrementCell(position - 1 - cols); // Haut-gauche
            incrementCell(position - 1);       // Gauche
            incrementCell(position + cols - 1); // Bas-gauche
        }

        incrementCell(position - cols);        // Haut
        incrementCell(position + cols);        // Bas

        if (current_col < (cols - 1)) {
            incrementCell(position - cols + 1); // Haut-droite
            incrementCell(position + cols + 1); // Bas-droite
            incrementCell(position + 1);        // Droite
        }
    }

    private void incrementCell(int cell) {
        if (cell >= 0 && cell < all_cells && field[cell] != COVERED_MINE_CELL) {
            field[cell] += 1;
        }
    }

    public void find_empty_cells(int j) {
        int current_col = j % cols;

        if (current_col > 0) {
            processCell(j - cols - 1); // Haut-gauche
            processCell(j - 1);       // Gauche
            processCell(j + cols - 1); // Bas-gauche
        }

        processCell(j - cols);        // Haut
        processCell(j + cols);        // Bas

        if (current_col < (cols - 1)) {
            processCell(j - cols + 1); // Haut-droite
            processCell(j + cols + 1); // Bas-droite
            processCell(j + 1);        // Droite
        }
    }

    private void processCell(int cell) {
        if (cell >= 0 && cell < all_cells && field[cell] > MINE_CELL) {
            field[cell] -= COVER_FOR_CELL;
            if (field[cell] == EMPTY_CELL) {
                find_empty_cells(cell);
            }
        }
    }

    public void paint(Graphics g) {
        int cell = 0;
        int uncover = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cell = field[(i * cols) + j];

                if (inGame && cell == MINE_CELL)
                    inGame = false;

                if (!inGame) {
                    if (cell == COVERED_MINE_CELL) {
                        cell = DRAW_MINE;
                    } else if (cell == MARKED_MINE_CELL) {
                        cell = DRAW_MARK;
                    } else if (cell > COVERED_MINE_CELL) {
                        cell = DRAW_WRONG_MARK;
                    } else if (cell > MINE_CELL) {
                        cell = DRAW_COVER;
                    }
                } else {
                    if (cell > COVERED_MINE_CELL)
                        cell = DRAW_MARK;
                    else if (cell > MINE_CELL) {
                        cell = DRAW_COVER;
                        uncover++;
                    }
                }

                g.drawImage(img[cell], (j * CELL_SIZE), (i * CELL_SIZE), this);
            }
        }

        if (uncover == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame)
            statusbar.setText("Game lost");
    }

    class MinesAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int cCol = x / CELL_SIZE;
            int cRow = y / CELL_SIZE;

            boolean rep = false;

            if (!inGame) {
                newGame();
                repaint();
            }

            if ((x < cols * CELL_SIZE) && (y < rows * CELL_SIZE)) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (field[(cRow * cols) + cCol] > MINE_CELL) {
                        rep = true;

                        if (field[(cRow * cols) + cCol] <= COVERED_MINE_CELL) {
                            if (mines_left > 0) {
                                field[(cRow * cols) + cCol] += MARK_FOR_CELL;
                                mines_left--;
                                statusbar.setText(Integer.toString(mines_left));
                            } else
                                statusbar.setText("No marks left");
                        } else {
                            field[(cRow * cols) + cCol] -= MARK_FOR_CELL;
                            mines_left++;
                            statusbar.setText(Integer.toString(mines_left));
                        }
                    }
                } else {
                    if (field[(cRow * cols) + cCol] > COVERED_MINE_CELL) {
                        return;
                    }

                    if ((field[(cRow * cols) + cCol] > MINE_CELL) && (field[(cRow * cols) + cCol] < MARKED_MINE_CELL)) {
                        field[(cRow * cols) + cCol] -= COVER_FOR_CELL;
                        rep = true;

                        if (field[(cRow * cols) + cCol] == MINE_CELL)
                            inGame = false;
                        if (field[(cRow * cols) + cCol] == EMPTY_CELL)
                            find_empty_cells((cRow * cols) + cCol);
                    }
                }

                if (rep)
                    repaint();
            }
        }
    }
}