
public class MinMaxUtils {

    static class MinMax {

        World world;
        int maxDeph;


        MinMax(World world, int maxDeph) {
            this.world = world;
            this.maxDeph = maxDeph;
        }

        Move run() {
            Move bestMove = null;
            double maxVal = Double.MIN_VALUE;
            double val;
            AltArray<Move> allMoves = this.world.possibleMoves();
            for (int i=0; i<allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                val = min(1);
                if (val > maxVal) {
                    maxVal = val;
                    bestMove = allMoves.get(i);
                }
                this.world.cancel(allMoves.get(i));
            }
            return bestMove;
        }

        double min(int currentDeph) {
            if (currentDeph > this.maxDeph || this.world.isOver()) {
                return world.evaluate();
            }
            double minVal = Double.MAX_VALUE;
            double val;
            AltArray<Move> allMoves = this.world.possibleMoves();
            for (int i=0; i<allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                val = max(currentDeph+1);
                if (val < minVal) {
                    minVal = val;
                }
                this.world.cancel(allMoves.get(i));
            }
            return minVal;
        }

        double max(int currentDeph) {
            if (currentDeph > this.maxDeph || this.world.isOver()) {
                return world.evaluate();
            }
            double maxVal = Double.MIN_VALUE;
            double val;
            AltArray<Move> allMoves = this.world.possibleMoves();
            for (int i=0; i<allMoves.size; i++) {
                this.world.apply(allMoves.get(i));
                val = min(currentDeph+1);
                if (val > maxVal) {
                    maxVal = val;
                }
                this.world.cancel(allMoves.get(i));
            }
            return maxVal;
        }


    }


    interface World {
        void apply(Move m);
        void cancel(Move m);
        AltArray<Move> possibleMoves();
        double evaluate();
        boolean isOver();
    }

    interface Move {
    }




}