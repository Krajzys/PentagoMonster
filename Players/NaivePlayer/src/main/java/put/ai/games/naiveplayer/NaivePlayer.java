/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.ai.games.naiveplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import put.ai.games.game.Board;
import put.ai.games.game.Move;
import put.ai.games.game.Player;

public class NaivePlayer extends Player {

    private Random random = new Random(0xdeadbeef);


    @Override
    public String getName() {
        return "Krzysztof Charlikowski 136689 Maciej Leszczyk 136759";
    }

    public int maxInLine(Board b, Color color) {
        int currMax = 0;
        int tempMax = 0;

        // Search max in line in rows
        for (int row = 0; row < b.getSize(); row++) {
            for (int col = 0; col < b.getSize(); col++) {
                if (b.getState(row, col) == color) {
                    tempMax++;
                }
                else {
                    if (tempMax > currMax) {
                        currMax = tempMax;
                    }
                    tempMax = 0;
                }
            }
            if (tempMax > currMax) {
                currMax = tempMax;
            }
            tempMax = 0;
        }

        currMax = 0;
        tempMax = 0;

        // Search max in line in columns
        for (int row = 0; row < b.getSize(); row++) {
            for (int col = 0; col < b.getSize(); col++) {
                if (b.getState(col, row) == color) {
                    tempMax++;
                }
                else {
                    if (tempMax > currMax) {
                        currMax = tempMax;
                    }
                    tempMax = 0;
                }
            }
            if (tempMax > currMax) {
                currMax = tempMax;
            }
            tempMax = 0;
        }

        return currMax;
    }



    @Override
    public Move nextMove(Board b) {
        int half = b.getSize() / 2;

        Color ourColor = getColor();
        Color enemyColor = (ourColor == Color.PLAYER1) ? Color.PLAYER2 : Color.PLAYER1;
        List<Move> moves = b.getMovesFor(ourColor);
        List<Move> goodMoves = new ArrayList<>();
        List<Move> notLoseMoves = new ArrayList<>();
        List<Move> bestMoves = new ArrayList<>();

        // Trying to win here
        int maxInLinePrior = maxInLine(b, ourColor);
        for (Move move : moves) {
            try {
                b.doMove(move);
            } catch (IllegalArgumentException exe) {
                continue;
            }
            int maxInLineAfter = maxInLine(b, ourColor);

            // If we get closer to win its good move
            if (maxInLineAfter >= (half + (half + 1) / 2)){
                b.undoMove(move);
                return move;
            }
            if (maxInLineAfter > maxInLinePrior) {
                maxInLinePrior = maxInLineAfter;
                goodMoves.clear();
                bestMoves.clear();
            }
            if (maxInLineAfter == maxInLinePrior) {
                goodMoves.add(move);
            }

            boolean canLose = false;

            List<Move> enemyMoves = b.getMovesFor(enemyColor);
            // If enemy can win after our move its not very good move
            for (Move emove: enemyMoves) {
                try {
                    b.doMove(emove);
                } catch (IllegalArgumentException exe) {
                    continue;
                }

                if (maxInLine(b, enemyColor) >= (half + (half + 1) / 2)) {
                    canLose = true;

                    b.undoMove(emove);
                    break;
                }

                b.undoMove(emove);
            }
            // If we cant lose and get closer to win its the best outcome
            if (!canLose && goodMoves.contains(move)) {
                bestMoves.add(move);
            }
            // If we cant lose but wont get closer to win its quite important
            else if (!canLose) {
                notLoseMoves.add(move);
            }

            b.undoMove(move);
        }

        // We randomly select one of the moves in order: Best, NotLose, Good, any
        Move finalMove;
        if (!bestMoves.isEmpty()) {
            finalMove = bestMoves.get(random.nextInt(bestMoves.size()));
            System.out.println("BEST");
        }
        else if (!notLoseMoves.isEmpty()) {
            finalMove = notLoseMoves.get(random.nextInt(notLoseMoves.size()));
            System.out.println("LOSE PREVENTION");
            System.out.println(finalMove);
        }
        else if (!goodMoves.isEmpty()) {
            finalMove = goodMoves.get(random.nextInt(goodMoves.size()));
            System.out.println("GOOD");
        }
        else {
            finalMove = moves.get(random.nextInt(moves.size()));
            System.out.println("BAD");
        }

        return finalMove;
    }

    static public void main(String args[]) {

    }
}
