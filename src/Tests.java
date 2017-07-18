package main;

import java.util.Scanner;
import main.Algorithms.AlphaBeta;
import main.Algorithms.MinMax;
import main.Algorithms.Move;
import main.Algorithms.World;

public class Tests {

    public static void main(String[] argv) {
        TicTacToe t = new TicTacToe();
        Scanner scan = new Scanner(System.in);

        AlphaBeta<TicTacToe,Draw> mm = new AlphaBeta<>(t,8);
        t.print();
        do {
            /* HUMAN TURN */
            boolean valid;
            int x, y;
            System.out.println(" _____ Your turn (O)_____");
            do {
                System.out.print("column : ");
                x = scan.nextInt();
                System.out.print("line : ");
                y = scan.nextInt();
                if (!(x >= 0 && x < 3 && y >= 0 && y < 3 && t.board[x][y] == 0)) {
                    valid = false;
                } else {
                    valid = true;
                }
            } while (!valid);
            t.apply(new Draw(-1,x,y));
            t.print();
            if (t.isOver()) break;

            /* MINMAX TURN */
            System.out.println(" _____ MinMax' turn (X) _____");
            Draw d = mm.run();
            System.out.println("Computer play : x="+d.x+" y="+d.y);
            t.apply(d);
            t.print();

        } while (!t.isOver());

    }


    static class TicTacToe implements World<Draw> {

        int[][] board = new int[3][3];

        @Override
        public void apply(Draw c) {
            this.board[c.x][c.y] = c.player;
        }

        @Override
        public void cancel(Draw c) {
            this.board[c.x][c.y] = 0;
        }

        @Override
        public AltArray<Draw> possibleMoves(int playerId) {
            AltArray<Draw> c = new AltArray<>(new Draw[3*3],0);
            for (int y=0;y<3;y++) {
                for (int x=0;x<3;x++) {
                    if (this.board[x][y] == 0) {
                        c.add(new Draw(playerId,x,y));
                    }
                }
            }
            return c;
        }

        @Override
        public double evaluate() {
            int winner;
            for (int i=0;i<3;i++) {
                winner = playerWinOnLine(i);
                if (winner != 0) return winner;
                winner = playerWinOnCol(i);
                if (winner != 0) return winner;
            }
            winner = playerWinOnDiag1();
            if (winner != 0) return winner;
            winner = playerWinOnDiag2();
            if (winner != 0) return winner;
            return 0;
        }

        @Override
        public boolean isOver() {
            if (this.boardIsFull()) return true;
            for (int i=0;i<3;i++) {
                if (playerWinOnLine(i) != 0 || playerWinOnCol(i) != 0) {
                    return true;
                }
            }
            if (playerWinOnDiag1() != 0 || playerWinOnDiag2() != 0 ) return true;
            return false;
        }

        // TODO caca !! suffit de compter les tours idiot :-)
        boolean boardIsFull() {
            for (int y=0;y<3;y++) {
                for (int x = 0; x < 3; x++) {
                    if (this.board[x][y] == 0) return false;
                }
            }
            return true;
        }

        int playerWinOnLine(int line) {
            int pid = board[0][line];
            for (int i=1;i<3;i++) {
                if (pid == 0 || pid != board[i][line]) return 0;
            }
            return pid;
        }

        int playerWinOnCol(int column) {
            int pid = board[column][0];
            for (int i=1;i<3;i++) {
                if (pid == 0 || pid != board[column][i]) return 0;
            }
            return pid;
        }

        int playerWinOnDiag1() {
            int pid = board[0][0];
            for (int i=1;i<3;i++) {
                if (pid == 0 || pid != board[i][i]) return 0;
            }
            return pid;
        }

        int playerWinOnDiag2() {
            int pid = board[2][0];
            for (int i=1;i<3;i++) {
                if (pid == 0 || pid != board[2-i][i]) return 0;
            }
            return pid;
        }

        void print() {
            System.out.println();
            for (int y=0;y<3;y++) {
                for (int x = 0; x < 3; x++) {
                    if (board[x][y] == 1) {
                        System.out.print("X ");
                    } else if (board[x][y] == -1) {
                        System.out.print("O ");
                    } else {
                        System.out.print("_ ");
                    }
                }
                System.out.println();
            }
        }
    }

    static class Draw implements Move {

        int player;
        int x;
        int y;

        Draw(int player, int x, int y) {
            this.player = player;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Draw{" +
                "player=" + player +
                ", x=" + x +
                ", y=" + y +
                '}';
        }
    }

}
