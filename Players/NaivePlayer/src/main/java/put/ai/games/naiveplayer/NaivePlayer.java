/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.ai.games.naiveplayer;

import java.util.List;
import java.util.Random;

import put.ai.games.game.Board;
import put.ai.games.game.Move;
import put.ai.games.game.Player;

public class NaivePlayer extends Player {

    class CalculationHelper implements Runnable {
        private List<Move> moves;
        private List<Move> enemyMoves;
        private int threadId;
        private volatile boolean flag = true;
        private Random threadRandom;
        public CalculationHelper(List<Move> moves, List<Move> enemyMoves, int threadId) {
            this.moves = moves;
            this.enemyMoves = enemyMoves;
            this.threadId = threadId;
        }
        public void shutdown(){
            this.flag = false;
        }

        @Override
        public void run() {
            Move randomMove;
            threadRandom = new Random(0xdeadbeef + threadId);
            threadValue[threadId] = currentValue;
            threadMove[threadId] = null;
            Integer[][] minimax = null;
            float res;
            Integer[] intMove;
            while(flag){
                //some calculations
                randomMove = moves.get(threadRandom.nextInt(moves.size()));
                intMove = convertStrMoveToInts(randomMove);
                if (!flag)
                    break;
                minimax = implementMove(map, intMove);
                if (!flag)
                    break;
                res = mapValue(minimax, pointsMap);
                if (res > threadValue[threadId]){
                    threadValue[threadId] = res;
                    threadMove[threadId] = randomMove;
                }
            }
        }
    }

    private Random random = new Random(0xdeadbeef);

    // miliseconds that program has to response with final move
    private int maximumResponseTime = 750;
    // number of calculation thread helpers
    private int threadsNo = 3;
    // depth of minmax tree
    private int depth = 2; // not used :/
    // id of calculation thread
    private static int id = 0;
    // final move of thread
    private Move[] threadMove = new Move[threadsNo];
    // value of thread's move
    private float[] threadValue = new float[threadsNo];

    // current value of move
    private float currentValue = 0;
    // final move which will be response
    private Move finalMove = null;

    private Integer[][] map = null;
    private Integer[][] pointsMap = null;

    Color goodColor = null;
    Color badColor = null;

    public Integer[][] implementMove(Integer[][] map, Integer[] move) {
        Integer[][] result = new Integer[map.length][];
        for(int i = 0; i < map.length; i++)
            result[i] = map[i].clone();
        result[move[0]][move[1]] = 1;

        boolean clock; // true -> CLOCK ; false -> CLOCKWISE
        int quarter;
        /* quarter: 1 = [ *  ]   2 = [  * ]  3 = [    ]   4 = [    ]
                        [    ]       [    ]      [ *  ]       [  * ]
         */
        if (move[2] < map.length / 2 && move[3] < map.length / 2)
            quarter = 1;
        else if (move[2] >= map.length / 2 && move[3] < map.length / 2)
            quarter = 2;
        else if (move[2] < map.length / 2 && move[3] >= map.length / 2)
            quarter = 3;
        else
            quarter = 4;
        clock = move[2] < move[4];

        result = doRotation(result, quarter, clock);
        return result;
    }

    public Integer[][] rotate(Integer[][] matrix, boolean clock){
        /* Rotate given matrix */
        int N = matrix.length;
        Integer[][] result = new Integer[N][];
        for(int i = 0; i < N; i++)
            result[i] = matrix[i].clone();
        if (!clock){
            for (int i = 0; i < N / 2; i++) {
                for (int j = 0; j < N - 1 - 2 * i; j++) {
                    int temp = result[j + i][N - 1 - i];
                    result[j + i][N - 1 - i] = result[i][j + i];
                    result[i][j + i] = result[N - 1 - j - i][i];
                    result[N - 1 - j - i][i] = result[N - 1 - i][N - 1 - j - i];
                    result[N - 1 - i][N - 1 - j - i] = temp;
                }
            }
        }
        else {
            for (int x = 0; x < N / 2; x++) {
                for (int y = x; y < N - x - 1; y++) {
                    int temp = result[x][y];
                    result[x][y] = result[y][N - 1 - x];
                    result[y][N - 1 - x] = result[N - 1 - x][N - 1 - y];
                    result[N - 1 - x][N - 1 - y] = result[N - 1 - y][x];
                    result[N - 1 - y][x] = temp;
                }
            }
        }
        return result;
    }

    public Integer[][] doRotation(Integer[][] map, int quarter, boolean clock){
        /* Apply a rotation in given quarter */
        int N = map.length;
        Integer[][] result = new Integer[N][];
        for(int i = 0; i < N; i++)
            result[i] = map[i].clone();

        Integer[][] rotated = new Integer[N/2][N/2];
        for (int x = 0; x < N / 2; x++) {
            for (int y = 0; y < N / 2; y++) {
                if (quarter == 1){
                    rotated[x][y] = map[x][y];
                } else if (quarter == 2){
                    rotated[x][y] = map[x+(N/2)][y];
                } else if (quarter == 3){
                    rotated[x][y] = map[x][y+(N/2)];
                } else if (quarter == 4){
                    rotated[x][y] = map[x+(N/2)][y+(N/2)];
                }
            }
        }
        rotated = rotate(rotated, clock);

        for (int x = 0; x < N / 2; x++) {
            for (int y = 0; y < N / 2; y++) {
                if (quarter == 1){
                    result[x][y] = rotated[x][y];
                } else if (quarter == 2){
                    result[x+(N/2)][y] = rotated[x][y];
                } else if (quarter == 3){
                    result[x][y+(N/2)] = rotated[x][y];
                } else if (quarter == 4){
                    result[x+(N/2)][y+(N/2)] = rotated[x][y];
                }
            }
        }

        return result;
    }

    public Integer[] convertStrMoveToInts(Move move) {
        String strMove = move.toString();
        Integer[] result = new Integer[6];
        String regexed = strMove.replaceAll("[^0-9,)]", ""
            ).replaceAll("[)]",",").replaceAll(",,",",");
        String strInts[] = regexed.split(",");
        for(int i=0; i<6; i++){
            result[i] = Integer.parseInt(strInts[i]);
        }
        return result;
    }

    public float mapValue(Integer[][] M, Integer[][] pointsM) {
        float cubeCoefficient = 2; // coefficient in cube polynomal while checking for pawns near by
        float result = 0;
        int N = M.length;
        int winNumber = N / 2 + ((N / 2 + 1) / 2);
        // Calculate value by players pawns near middle of map
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (M[i][j] != 0) {
                    result += M[i][j] * pointsM[i][j];
                }
            }
        }
        // Calculate value by pawns near by, leading to win
        // horizontal >
        for (int y = 0; y < N; y++) {
            int me = 0, enemy = 0;
            for (int x = 0; x < N; x++) {
                if (M[x][y] > 0) {
                    me++;
                    enemy = 0;
                }
                else if (M[x][y] < 0){
                    enemy++;
                    me = 0;
                }
                else{
                    if (me > 0) {
                        if (me == winNumber)
                            result += 99999;
                        result += Math.floor(Math.exp(me));
                        me = 0;
                    }
                    else if (enemy > 0) {
                        if (enemy == winNumber)
                            result -= 99999;
                        else if (enemy == winNumber - 1)
                            result -= Math.floor(Math.exp(enemy + 1));
                        result -= Math.floor(Math.exp(enemy));
                        enemy = 0;
                    }
                }
            }
        }
        // horizontal <
        for (int y = 0; y < N; y++) {
            int me = 0, enemy = 0;
            for (int x = N - 1; x >= 0; x--) {
                if (M[x][y] > 0) {
                    me++;
                    enemy = 0;
                }
                else if (M[x][y] < 0){
                    enemy++;
                    me = 0;
                }
                else{
                    if (me > 0) {
                        if (me == winNumber)
                            result += 999999;
                        result += Math.floor(Math.exp(me));
                        me = 0;
                    }
                    else if (enemy > 0) {
                        if (enemy == winNumber)
                            result -= 999999;
                        else if (enemy == winNumber - 1)
                            result -= Math.floor(Math.exp(enemy + 1));
                        result -= Math.floor(Math.exp(enemy));
                        enemy = 0;
                    }
                }
            }
        }
        // vertical V
        for (int x = 0; x < N; x++) {
            int me = 0, enemy = 0;
            for (int y = 0; y < N; y++) {
                if (M[x][y] > 0) {
                    me++;
                    enemy = 0;
                }
                else if (M[x][y] < 0){
                    enemy++;
                    me = 0;
                }
                else{
                    if (me > 0) {
                        if (me == winNumber)
                            result += 99999;
                        result += Math.floor(Math.exp(me));
                        me = 0;
                    }
                    else if (enemy > 0) {
                        if (enemy == winNumber)
                            result -= 99999;
                        else if (enemy == winNumber - 1)
                            result -= Math.floor(Math.exp(enemy + 1));
                        result -= Math.floor(Math.exp(enemy));
                        enemy = 0;
                    }
                }
            }
        }
        // vertical ^
        for (int x = 0; x < N; x++) {
            int me = 0, enemy = 0;
            for (int y = N - 1; y >= 0; y--) {
                if (M[x][y] > 0) {
                    me++;
                    enemy = 0;
                }
                else if (M[x][y] < 0){
                    enemy++;
                    me = 0;
                }
                else{
                    if (me > 0) {
                        if (me == winNumber)
                            result += 99999;
                        result += Math.floor(Math.exp(me));
                        me = 0;
                    }
                    else if (enemy > 0) {
                        if (enemy == winNumber)
                            result -= 99999;
                        else if (enemy == winNumber - 1)
                            result -= Math.floor(Math.exp(enemy + 1));
                        result -= Math.floor(Math.exp(enemy));
                        enemy = 0;
                    }
                }
            }
        }

        return result;
    }

    public void calculateBoardValue(Board b) {
        int size = b.getSize();

        int half = (int) ((size-1)/2);
        map = new Integer[size][size];
        pointsMap = new Integer[size][size];
        for (int i=0; i < map.length / 2; i++) {
            for (int j=0; j < map[i].length / 2; j++) {
                pointsMap[i][j] = (half - Math.abs(half - i)) + (half - Math.abs(half - j)) + 1;
                if (b.getState(i, j) == goodColor) {
                    map[i][j] = 1;
                } else if (b.getState(i, j) == badColor) {
                    map[i][j] = -1;
                } else map[i][j] = 0;
            }
        }
        for (int i=map.length / 2; i < map.length; i++) {
            for (int j=0; j < map[i].length / 2; j++) {
                pointsMap[i][j] = (half - Math.abs(half - i)) + (half - Math.abs(half - j)) + 2;
                if (b.getState(i, j) == goodColor) {
                    map[i][j] = 1;
                } else if (b.getState(i, j) == badColor) {
                    map[i][j] = -1;
                } else map[i][j] = 0;
            }
        }
        for (int i=0; i < map.length / 2; i++) {
            for (int j=map.length / 2; j < map[i].length; j++) {
                pointsMap[i][j] = (half - Math.abs(half - i)) + (half - Math.abs(half - j)) + 2;
                if (b.getState(i, j) == goodColor) {
                    map[i][j] = 1;
                } else if (b.getState(i, j) == badColor) {
                    map[i][j] = -1;
                } else map[i][j] = 0;
            }
        }
        for (int i=map.length / 2; i < map.length; i++) {
            for (int j=map.length / 2; j < map[i].length; j++) {
                pointsMap[i][j] = (half - Math.abs(half - i)) + (half - Math.abs(half - j)) + 3;
                if (b.getState(i, j) == goodColor) {
                    map[i][j] = 1;
                } else if (b.getState(i, j) == badColor) {
                    map[i][j] = -1;
                } else map[i][j] = 0;
            }
        }
        currentValue = mapValue(map, pointsMap);
        System.out.println("Current value is " + currentValue);
    }

    @Override
    public String getName() {
        return "Krzysztof Charlikowski 136689 Maciej Leszczyk 136759";
    }

    @Override
    public Move nextMove(Board b) throws InterruptedException {
        long start = System.currentTimeMillis();
        long maxTime = getTime();

        goodColor = getColor();
        badColor = (goodColor == Color.PLAYER1) ? Color.PLAYER2 : Color.PLAYER1;

        List<Move> moves = b.getMovesFor(goodColor);
        List<Move> enemyMoves = b.getMovesFor(badColor);

        calculateBoardValue(b);

        CalculationHelper[] helpers = new CalculationHelper[threadsNo];
        for (int i=0; i<threadsNo; i++){
            helpers[i] = new CalculationHelper(moves, enemyMoves, id++);
            Thread th = new Thread(helpers[i]);
            th.start();
        }
        // Count time to end of turn
        while(true) {
            long currentTime = System.currentTimeMillis() - start;
            if(maxTime - currentTime <= maximumResponseTime) {
                break;
            }
        }
        for (int i=0; i<threadsNo; i++){
            helpers[i].shutdown();
        }

        for (int i=0; i<threadsNo; i++){
            if (threadValue[i] > currentValue){
                finalMove = threadMove[i];
                currentValue = threadValue[i];
            }
        }
        if (finalMove == null){
            finalMove = moves.get(random.nextInt(moves.size()));
        }
        System.out.println("Heurestic value = " + currentValue);
        id = 0;
        return finalMove;
    }
}
