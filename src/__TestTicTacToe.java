import java.util.Scanner;


public class __TestTicTacToe {

    public static void main(String[] argv) {
        TicTacToe game = new TicTacToe();
        Scanner scan = new Scanner(System.in);

        TreeSearch.AlphaBeta<TicTacToe,Draw> mm = new TreeSearch.AlphaBeta<>(game,8);
        game.print();
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
                valid = game.isValid(x,y);
            } while (!valid);
            game.apply(new Draw(-1,x,y));
            game.print();
            if (game.isOver()) break;

            /* MINMAX TURN */
            Draw d = mm.run();
            game.apply(d);
            System.out.println(" _____ MinMax' turn (X) _____");
            System.out.println("Computer play : x="+d.x+" y="+d.y);
            game.print();

        } while (!game.isOver());

    }

    public static void clearScreen() {
        for (int i=0;i<50;i++) {
            System.out.println();
        }
    }

    static class TicTacToe implements TreeSearch.World<Draw> {

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

        boolean isValid(int x, int y) {
            boolean valid;
            if (!(x >= 0 && x < 3 && y >= 0 && y < 3 && board[x][y] == 0)) {
                valid = false;
            } else {
                valid = true;
            }
            return valid;
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

    static class Draw implements TreeSearch.Move {

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
