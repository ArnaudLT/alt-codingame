
public class TreeSearch {

    static class MinMax<T extends World<M>, M extends Move> {

        T world;
        int maxDepth;


        MinMax(T world, int maxDepth) {
            this.world = world;
            this.maxDepth = maxDepth;
        }

        /**
         * Run the minmax algorithm, the id of the max player is 1, the id of the min player is -1.
         *
         * @return Return the best move
         */
        M run() {
            M bestMove = null;
            double maxVal = Double.NEGATIVE_INFINITY;
            double val;
            AltArray<M> allMoves = this.world.possibleMoves(1);
            for (int i = 0; i < allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                val = min(1);
                this.world.cancel(allMoves.get(i));
                if (val > maxVal) {
                    maxVal = val;
                    bestMove = allMoves.get(i);
                }
            }
            return bestMove;
        }

        double min(int currentDepth) {
            if (currentDepth > this.maxDepth || this.world.isOver()) {
                return world.evaluate();
            }
            double minVal = Double.MAX_VALUE;
            double val;
            AltArray<M> allMoves = this.world.possibleMoves(-1);
            for (int i = 0; i < allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                val = max(currentDepth + 1);
                this.world.cancel(allMoves.get(i));
                if (val < minVal) {
                    minVal = val;
                }
            }
            return minVal;
        }

        double max(int currentDepth) {
            if (currentDepth > this.maxDepth || this.world.isOver()) {
                return world.evaluate();
            }
            double maxVal = Double.NEGATIVE_INFINITY;
            double val;
            AltArray<M> allMoves = this.world.possibleMoves(1);
            for (int i = 0; i < allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                val = min(currentDepth + 1);
                this.world.cancel(allMoves.get(i));
                if (val > maxVal) {
                    maxVal = val;
                }
            }
            return maxVal;
        }

    }

    static class AlphaBeta<T extends World<M>, M extends Move> {

        T world;
        int maxDepth;

        AlphaBeta(T world, int maxDepth) {
            this.world = world;
            this.maxDepth = maxDepth;
        }

        /**
         * Run the minmax algorithm with alpha-beta cuts, the id of the max player is 1, the id of the min player is -1.
         *
         * @return Return the best move for the max player.
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
        /**
         * Apply the move m to the world.
         * @param m the move to apply.
         */
        void apply(M m);

        /**
         * Cancel the move m from the world.
         * @param m the move to cancel
         */
        void cancel(M m);

        /**
         *
         * @param playerId The id of the max player is 1, the id of the min player is -1.
         * @return Return the list of possible moves for the player id playerId.
         */
        AltArray<M> possibleMoves(int playerId);

        /**
         *
         * @return Return the evaluation of the current world configuration. A high value means that the configuration
         * looks good for the max player.
         */
        double evaluate();

        /**
         *
         * @return Return true if the game is over.
         */
        boolean isOver();
    }

    interface Move {
    }

}
