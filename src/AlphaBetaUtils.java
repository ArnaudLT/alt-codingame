
public class AlphaBetaUtils {

    static class AlphaBeta<T extends World<M>, M extends Move> {

        T world;
        int maxDepth;

        AlphaBeta(T world, int maxDepth) {
            this.world = world;
            this.maxDepth = maxDepth;
        }

        /**
         * I'm the Max player (pid=1), the opponent is the Min player (pid=-1)
         * @return
         */

        M run() {
            M bestMove = null;
            double maxVal = Double.NEGATIVE_INFINITY;
            double val;
            AltArray<M> allMoves = this.world.possibleMoves(1);
            for (int i=0; i<allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                val = min(1, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
                this.world.cancel(allMoves.get(i));
                if (val > maxVal) {
                    maxVal = val;
                    bestMove = allMoves.get(i);
                }
            }
            return bestMove;
        }


        double min(int currentDepth, double alpha, double beta) {
            if (currentDepth > this.maxDepth || this.world.isOver()) {
                return world.evaluate();
            }
            double minVal = Double.MAX_VALUE;
            AltArray<M> allMoves = this.world.possibleMoves(-1);
            for (int i=0; i<allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                minVal = Utils.min(minVal,max(currentDepth+1, alpha, beta));
                this.world.cancel(allMoves.get(i));
                if (minVal <= alpha) {
                    return minVal;
                }
                beta = Utils.min(beta, minVal);
            }
            return minVal;
        }


        double max(int currentDepth, double alpha, double beta) {
            if (currentDepth > this.maxDepth || this.world.isOver()) {
                return world.evaluate();
            }
            double maxVal = Double.NEGATIVE_INFINITY;
            AltArray<M> allMoves = this.world.possibleMoves(1);
            for (int i=0; i<allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                maxVal = Utils.max(maxVal,min(currentDepth+1, alpha, beta));
                this.world.cancel(allMoves.get(i));
                if (maxVal >= beta) {
                    return maxVal;
                }
                alpha = Utils.max(alpha, maxVal);
            }
            return maxVal;
        }

    }


    interface World<M extends Move> {
        void apply(M m);
        void cancel(M m);
        AltArray<M> possibleMoves(int playerId);
        double evaluate();
        boolean isOver();
    }

    interface Move {
    }


}
