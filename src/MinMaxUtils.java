
public class MinMaxUtils {

    static class MinMax<T extends World<M>, M extends Move> {

        T world;
        int maxDepth;


        MinMax(T world, int maxDepth) {
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
            for (int i=0; i<allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                val = max(currentDepth+1);
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
            for (int i=0; i<allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                val = min(currentDepth+1);
                this.world.cancel(allMoves.get(i));
                if (val > maxVal) {
                    maxVal = val;
                }
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
