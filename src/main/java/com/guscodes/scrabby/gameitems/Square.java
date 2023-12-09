package com.guscodes.scrabby.gameitems;

public class Square {
    private char bonus = 'X';
    // 'X' = bonus unset (default)
    // 'O' = no bonus
    // 'l' = double letter
    // 'L' = triple letter
    // 'w' = double word
    // 'W' = triple word

    private int location = -1;
    private char contents = '_';
    private boolean isAnchorable;
    private Square above;
    private Square below;
    private Square left;
    private Square right;

    public Square(int location) {

        this.location = location;
    }

    public int getLocation() throws IllegalStateException {
        if (this.location >= 0) {
            return this.location;
        }
        else throw new IllegalStateException("Square location has not been set");
    }

    public void setBonus(char bonus) throws IllegalStateException {
        if (this.bonus == 'X') {
            this.bonus = bonus;
        }
        else throw new IllegalStateException("Square bonus has already been set");
    }

    public char getBonus() {
        return this.bonus;
    }

    public char getContents() {
        return contents;
    }

    public void setContents(char contents) throws IllegalArgumentException {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if ((this.contents == '_') && (alphabet.indexOf(String.valueOf(Character.toUpperCase(contents))) != -1)) {
            this.contents = contents;
            this.isAnchorable = true;
            updateSurroundings();
        }
    }

    public void setAnchorable() {
        this.isAnchorable = true;
    }
    public boolean isAnchorable() {
        return this.isAnchorable;
    }

    public void setAbove(Square above) {
        this.above = above;
    }

    public void setBelow(Square below) {
        this.below = below;
    }

    public void setLeft(Square left) {
        this.left = left;
    }

    public void setRight(Square right) {
        this.right = right;
    }

    private void updateSurroundings() {
        //todo sort out tile allowed letters
        if (this.contents != '_') {
            if (!(above == null)) {
                above.setAnchorable();
            }
            if (!(below == null)) {
                below.setAnchorable();
            }
            if (!(left == null)) {
                left.setAnchorable();
            }
            if (!(right == null)) {
                right.setAnchorable();
            }
        }
    }
}
