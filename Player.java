import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        int numSites = in.nextInt(); // Nombre de sites de construction.

        World world = new World(numSites);

        for (int i = 0; i < numSites; i++) {
            int siteId = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            int initialRadius = in.nextInt();
            world.addBuildingSite(new BuildingSite(siteId, x, y, initialRadius));
            //System.err.println("INIT ->"+world.sites[siteId]);
        }

        world.initDistanceBetweenSites();

        // game loop
        while (true) {

            world.turnCount++;
            System.err.println("turn=" + world.turnCount);

            int gold = in.nextInt();
            int touchedSite = in.nextInt(); // -1 if none

            world.gold = gold;
            world.touchedSite = touchedSite;

            for (int i = 0; i < numSites; i++) {
                int siteId = in.nextInt();
                int remainingGold = in.nextInt();
                int maxMineSize = in.nextInt();
                int structureType = in.nextInt(); // -1 = No structure, 1 = BuildTower, 2 = Barracks
                int owner = in.nextInt(); // -1 = No structure, 0 = Friendly, 1 = Enemy
                int param1 = in.nextInt();
                int param2 = in.nextInt();
                world.updateBuildingSite(siteId, remainingGold, maxMineSize,
                        StructureType.values()[structureType + 1],
                        Owner.values()[owner + 1],
                        param1,
                        param2);
                //System.err.println("UPDATE SITE ->"+world.sites[siteId]);
            }

            int numUnits = in.nextInt();
            world.units = new Unit[numUnits];

            for (int i = 0; i < numUnits; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                int owner = in.nextInt();
                int unitType = in.nextInt(); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
                int health = in.nextInt();
                world.addUnit(new Unit(i, x, y,
                        Owner.values()[owner + 1],
                        UnitType.values()[unitType + 1],
                        health));
                //System.err.println("UPDATE UNIT -> "+world.units[i]);
            }

            world.gatherStatistics();
            //System.err.println(world.statistics);

            world.findActions();

            // First line: A valid queen action
            // Second line: A set of training instructions
            System.out.println(world.firstAction.getAsString());
            System.out.println(world.secondAction.getAsString());
        }
    }

    static class World {

        int gold;
        int touchedSite;

        final double[][] distancesBetweenSites;
        BuildingSite[] sites;
        Unit[] units;
        int turnCount;

        Unit friendlyQueen;
        Unit enemyQueen;
        Statistics statistics;

        Action firstAction;
        Action secondAction;


        World(int numSites) {
            this.sites = new BuildingSite[numSites];
            this.distancesBetweenSites = new double[numSites][numSites]; // pas besoin de se faire chier avec les doublons (1s pour le premier tour.)
            this.turnCount = 0;
        }

        void addBuildingSite(BuildingSite site) {
            this.sites[site.siteId] = site;
        }

        void updateBuildingSite(int siteId, int remainingGold, int maxMineSize, StructureType structureType, Owner owner, int param1, int param2) {
            this.sites[siteId].type = structureType;
            this.sites[siteId].owner = owner;
            if (turnCount == 1 || remainingGold != -1) this.sites[siteId].remainingGold = remainingGold;
            this.sites[siteId].maximumMineSize = maxMineSize;
            if (structureType == StructureType.TOWER) {
                this.sites[siteId].health = param1;
                this.sites[siteId].radius = param2;
            } else if (structureType == StructureType.BARRACKS) {
                this.sites[siteId].turnsBeforeAlive = param1;
                this.sites[siteId].creepType = CreepType.values()[param2 + 1];
            } else if (structureType == StructureType.MINE) {
                this.sites[siteId].incomeRate = (param1 != -1) ? param1 : 3; // approx pour enemy
            }
            if ((siteId == 2)) {
                System.err.println(this.sites[2]);
            }
        }

        void addUnit(Unit unit) {
            this.units[unit.unitId] = unit;
            if (unit.unitType == UnitType.QUEEN && unit.owner == Owner.FRIENDLY) {
                friendlyQueen = unit;
            } else if (unit.unitType == UnitType.QUEEN && unit.owner == Owner.ENEMY) {
                enemyQueen = unit;
            }
        }

        void initDistanceBetweenSites() {

            for (int i = 0; i < sites.length; i++) {
                for (int j = i + 1; j < sites.length; j++) {
                    double dist = distance(this.sites[i], this.sites[j]);
                    this.distancesBetweenSites[i][j] = dist;
                    this.distancesBetweenSites[j][i] = dist;
                }
                this.distancesBetweenSites[i][i] = 0d;
            }
        }


        void gatherStatistics() {
            this.statistics = Statistics.gatherStatistics(this);
            Arrays.stream(this.sites)
                    .forEach(s -> s.distanceToFriendlyQueen = distance(s, friendlyQueen));
            Arrays.stream(this.units)
                    .forEach(u -> u.distanceToFriendlyQueen = distance(u, friendlyQueen));
        }


        // refresh each turn
        BuildingSite lastTarget = null;
        BuildingSite nearestEmpty;
        BuildingSite secondNearestEmpty;
        BuildingSite nearestFromCenter;
        BuildingSite nearestEmptyWithGold;
        Unit nearestEnemy;

        static final int STARTER_NB_MINES = 2;
        static final int STARTER_NB_BARRACKS = 1;
        static final int STARTER_NB_TOWERS = 2;
        static final int STARTER_MIN_HP_TOWER = 700;

        static final int MIDDLE_MIN_HP_TOWER = 700;
        static final int MIDDLE_CRITICAL_HP_TOWER = 350;

        boolean isStarterFinished;
        boolean prepareBigBerta;


        void findActions() {


            this.firstAction = new Wait();

            nearestEmpty = getNearestEmpty();
            secondNearestEmpty = getSecondNearestEmpty(nearestEmpty);
            //nearestFromCenter = getNearestFromCenter(nearestEmpty, secondNearestEmpty);
            nearestEmptyWithGold = getNearestEmptyWithGold();

            nearestEnemy = nearestEnemyUnit();

            if (!isStarterFinished) {

                System.err.println(" STARTER ");
                isStarterFinished = starter(STARTER_NB_MINES,
                        STARTER_NB_BARRACKS,
                        STARTER_NB_TOWERS,
                        STARTER_MIN_HP_TOWER, nearestEnemy);
            } else {

                // MID (+ END)
                System.err.println(" MIDDLE " + this.turnCount);
                if (nearestEnemy != null && nearestEnemy.distanceToFriendlyQueen < 375) {

                    /**  Whatever is the situation ... run for your life ! */
                    System.err.println("//!\\ DANGER //!\\");

                    BuildingSite nearestTower = Arrays.stream(this.sites)
                            .filter(s -> s.type == StructureType.TOWER)
                            .filter(s -> s.owner == Owner.FRIENDLY)
                            .min(Comparator.comparingDouble(s -> s.distanceToFriendlyQueen))
                            .orElse(null);

                    if (nearestTower != null && nearestEnemy.distanceToFriendlyQueen > 60) {
                        System.err.println("Turn around = " + nearestTower);
                        this.firstAction = new Move(xEscapeEnemyTower(nearestEnemy.x, nearestTower), yEscapeEnemyTower(nearestEnemy.y, nearestTower));
                    } else if (touchedSite != -1 && this.sites[touchedSite].type == StructureType.NONE) {
                        this.firstAction = new BuildTower(touchedSite);
                        this.lastTarget = this.sites[touchedSite];
                    } else {
                        this.firstAction = new Move(xEscapeEnemy(nearestEnemy.x), yEscapeEnemy(nearestEnemy.y));
                    }

                } else if (lastTarget != null && lastTarget.type == StructureType.TOWER &&
                        lastTarget.owner == Owner.FRIENDLY && lastTarget.health < MIDDLE_MIN_HP_TOWER) {

                    /** You 're safe, repair the current tower */
                    this.firstAction = new BuildTower(lastTarget.siteId);

                } else if (lastTarget != null && lastTarget.type == StructureType.MINE &&
                        lastTarget.owner == Owner.FRIENDLY && lastTarget.incomeRate < lastTarget.maximumMineSize) {

                    /** You 're safe, repair the current tower */
                    this.firstAction = new BuildMine(lastTarget.siteId);

                } else if (this.statistics.friendlyBarracksKnights == 0 ||
                        (this.statistics.friendlyBarracksKnights < 3 && prepareBigBerta)) {

                    /** You need at least one barracks dude ! */
                    //this.firstAction = new BuildBarracks(nearestEmpty.siteId, CreepType.KNIGHT);
                    //lastTarget = nearestEmpty;

                    int xCenter = 960;
                    int yCenter = 500;
                    BuildingSite idealBarracks = Arrays.stream(this.sites)
                            .filter(s -> s.owner == Owner.NONE)
                            .sorted(Comparator.comparingDouble(s -> distance(s, xCenter, yCenter)))
                            .limit(4)
                            .min(Comparator.comparingDouble(s -> s.distanceToFriendlyQueen))
                            .get();
                    this.firstAction = new BuildBarracks(idealBarracks.siteId, CreepType.KNIGHT);
                    lastTarget = idealBarracks;


                } else if (this.statistics.friendlyTowers < 4 && nearestEmpty != null) {

                    /** Having 4 towers, is a must have for now. */
                    this.firstAction = new BuildTower(nearestEmpty.siteId);
                    lastTarget = nearestEmpty;
                } else {

                    // NEW TEST (NOTHING BEFORE)
                    BuildingSite weakestTower = getFriendlyWeakestTower();
                    BuildingSite nearestEmptyWithGoldAndSafe = getNearestEmptyWithGoldAndSafe();


                    if (weakestTower != null && weakestTower.health < MIDDLE_CRITICAL_HP_TOWER) {

                        this.firstAction = new BuildTower(weakestTower.siteId);
                        lastTarget = weakestTower;
                    } else if (nearestEmptyWithGoldAndSafe != null) {

                        this.firstAction = new BuildMine(nearestEmptyWithGoldAndSafe.siteId);
                        lastTarget = nearestEmptyWithGoldAndSafe;

                    } else if (weakestTower != null && weakestTower.health < MIDDLE_MIN_HP_TOWER &&
                            (nearestEnemy == null || nearestEnemy.distanceToFriendlyQueen > 700)) {

                        this.firstAction = new BuildTower(weakestTower.siteId);
                        lastTarget = weakestTower;
                    }
                }
            }

            prepareBigBerta = (this.turnCount < 150) &&
                    (this.enemyQueen.health > this.friendlyQueen.health) &&
                    (this.statistics.friendlyIncomeRate > 0);

            System.err.println("Prepare = " + prepareBigBerta);

            this.secondAction = new Train(null);

            if (!prepareBigBerta && (this.statistics.friendlyBarracksKnights == 1 && this.gold > 80 ||
                    this.statistics.friendlyBarracksKnights == 2 && this.gold > 160
                    || this.statistics.friendlyBarracksKnights > 2)) {

                this.secondAction = new Train(
                        Arrays.stream(this.sites)
                                .filter(s -> s.type == StructureType.BARRACKS)
                                .filter(s -> s.creepType == CreepType.KNIGHT)
                                .filter(s -> s.turnsBeforeAlive <= 0)
                                .filter(s -> s.owner == Owner.FRIENDLY)
                                .sorted(Comparator.comparingDouble(s -> distance(s, enemyQueen)))
                                .limit(this.gold / 80)
                                .mapToInt(s -> s.siteId)
                                .toArray()
                );
            }

        }


        //  'nbMines' mines max / 'nbBarracks' barracks / 'nbTowers' tours with at least 'minHpTower'
        boolean starter(int nbMines, int nbBarracks, int nbTowers, int minHpTower, Unit nearestEnemy) {

            if (nearestEnemy != null && nearestEnemy.distanceToFriendlyQueen < 375 && friendlyQueen.health < 35) {

                BuildingSite nearestTower = Arrays.stream(this.sites)
                        .filter(s -> s.type == StructureType.TOWER)
                        .filter(s -> s.owner == Owner.FRIENDLY)
                        .min(Comparator.comparingDouble(s -> s.distanceToFriendlyQueen))
                        .orElse(null);

                if (nearestTower != null && nearestEnemy.distanceToFriendlyQueen > 60) {
                    System.err.println("Turn around = " + nearestTower);
                    this.firstAction = new Move(xEscapeEnemyTower(nearestEnemy.x, nearestTower), yEscapeEnemyTower(nearestEnemy.y, nearestTower));
                } else if (touchedSite != -1 && this.sites[touchedSite].type == StructureType.NONE) {
                    this.firstAction = new BuildTower(touchedSite);
                    this.lastTarget = this.sites[touchedSite];
                } else if (nearestEmpty != null) {
                    this.firstAction = new BuildTower(nearestEmpty.siteId);
                    this.lastTarget = nearestEmpty;
                }else {
                    this.firstAction = new Move(xEscapeEnemy(nearestEnemy.x), yEscapeEnemy(nearestEnemy.y));
                }

            } else if (lastTarget != null && lastTarget.type == StructureType.MINE && lastTarget.incomeRate < lastTarget.maximumMineSize) {

                // Mine en évolution
                this.firstAction = new BuildMine(lastTarget.siteId);
            } else if (this.statistics.friendlyMines < nbMines) {

                // Nouvelle mine
                if (nearestEmptyWithGold != null) {
                    System.err.println("nearestEmptyWithGold=" + nearestEmptyWithGold);
                    this.firstAction = new BuildMine(nearestEmptyWithGold.siteId);
                    lastTarget = nearestEmptyWithGold;
                } else {
                    System.err.println("nearestEmpty=" + nearestEmpty);
                    this.firstAction = new BuildMine(nearestEmpty.siteId);
                    lastTarget = nearestEmpty;
                }
            } else if (this.statistics.friendlyBarracksKnights < nbBarracks) {

                // Barracks Knight a construire
                //this.firstAction = new BuildBarracks(nearestEmpty.siteId, CreepType.KNIGHT);
                //lastTarget = nearestEmpty;
                if (this.statistics.enemyBarracksKnights == 0) {
                    int xCenter = 960;
                    int yCenter = 500;
                    BuildingSite idealBarracks = Arrays.stream(this.sites)
                            .filter(s -> s.owner == Owner.NONE)
                            .sorted(Comparator.comparingDouble(s -> distance(s, xCenter, yCenter)))
                            .limit(4)
                            .min(Comparator.comparingDouble(s -> s.distanceToFriendlyQueen))
                            .get();
                    this.firstAction = new BuildBarracks(idealBarracks.siteId, CreepType.KNIGHT);
                    lastTarget = idealBarracks;
                } else {
                    this.firstAction = new BuildBarracks(nearestEmpty.siteId, CreepType.KNIGHT);
                    lastTarget = nearestEmpty;
                }

            } else if (lastTarget != null && lastTarget.type == StructureType.TOWER && lastTarget.health < minHpTower) {

                // Tour en évolution
                this.firstAction = new BuildTower(lastTarget.siteId);
            } else if (this.statistics.friendlyTowers < nbTowers) {

                this.firstAction = new BuildTower(nearestEmpty.siteId);
                lastTarget = nearestEmpty;
            } else {
                System.err.println("lastTarget = " + lastTarget);
                System.err.println(">>> BUG START <<<");
                return true;
            }

            return this.statistics.friendlyTowers >= nbTowers && lastTarget.health + 96 >= minHpTower;
        }


        BuildingSite getNearestEmptyWithGold() {
            return Arrays.stream(this.sites)
                    .filter(s -> s.type == StructureType.NONE)
                    .filter(s -> s.remainingGold > 0 /*|| s.distanceToFriendlyQueen > 299*/)
                    .min(Comparator.comparingDouble(s -> s.distanceToFriendlyQueen))
                    .orElse(null);
        }

        BuildingSite getNearestEmptyWithGoldAndSafe() {
            return Arrays.stream(this.sites)
                    .filter(s -> s.type == StructureType.NONE)
                    .filter(s -> s.remainingGold > 0 /*|| s.distanceToFriendlyQueen > 299*/)
                    .filter(this::isSafeFromEnemyTower)
                    .min(Comparator.comparingDouble(s -> s.distanceToFriendlyQueen))
                    .orElse(null);
        }

        BuildingSite getNearestEmpty() {
            return Arrays.stream(this.sites)
                    .filter(s -> s.type == StructureType.NONE)
                    .filter(this::isSafeFromEnemyTower)
                    .min(Comparator.comparingDouble(s -> s.distanceToFriendlyQueen))
                    .orElse(null);
        }

        BuildingSite getFriendlyWeakestTower() {
            return Arrays.stream(this.sites)
                    .filter(s -> s.owner == Owner.FRIENDLY)
                    .filter(s -> s.type == StructureType.TOWER)
                    .min(Comparator.comparingDouble(s -> s.health))
                    .orElse(null);
        }

/*        BuildingSite getUnitSafeNearestEmpty(Unit nearestEnemy) {

        }*/

        boolean isSafeFromEnemyTower(BuildingSite site) {
            return Arrays.stream(this.sites)
                    .filter(s -> s.owner == Owner.ENEMY)
                    .filter(s -> s.type == StructureType.TOWER)
                    .allMatch(s -> distancesBetweenSites[s.siteId][site.siteId] > s.health - 100);
        }


        BuildingSite getSecondNearestEmpty(BuildingSite firstNearestEmpty) {
            if (firstNearestEmpty == null) return null;
            return Arrays.stream(this.sites)
                    .filter(s -> s.type == StructureType.NONE)
                    .filter(s -> s.siteId != firstNearestEmpty.siteId)
                    .min(Comparator.comparingDouble(s -> s.distanceToFriendlyQueen))
                    .orElse(null);
        }

        BuildingSite getNearestFromCenter(BuildingSite p1, BuildingSite p2) {

            if (p1 == null) return p2;
            if (p2 == null) return p1;

            int xCenter = 960;
            int yCenter = 500;

            double p1ToCenter = distance(p1, xCenter, yCenter);
            double p2ToCenter = distance(p2, xCenter, yCenter);
            BuildingSite nearestFromCenter;

            if (p1ToCenter <= p2ToCenter) {
                nearestFromCenter = p1;
            } else {
                nearestFromCenter = p2;
            }

            System.err.println(p1ToCenter + " / p1=" + p1);
            System.err.println(p2ToCenter + " / p2=" + p2);

            return nearestFromCenter;
        }


        double goldProductionScore() {
            return this.statistics.friendlyIncomeRate;
        }


        double defenseScore() {
            return this.statistics.friendlyTowers + 0.1 *
                    Arrays.stream(this.sites)
                            .filter(s -> s.owner == Owner.FRIENDLY)
                            .filter(s -> s.type == StructureType.TOWER)
                            .mapToInt(s -> s.health)
                            .sum();
        }


        double lifeScore() {
            return this.friendlyQueen.health - this.enemyQueen.health;
        }


        Unit nearestEnemyUnit() {
            return Arrays.stream(this.units)
                    .filter(u -> u.owner == Owner.ENEMY)
                    .filter(u -> u.unitType == UnitType.KNIGHT)
                    .min(Comparator.comparingDouble(u -> u.distanceToFriendlyQueen))
                    .orElse(null);
        }


        int xEscapeEnemy(int xEnemy) {
            double xDelta = Math.abs(friendlyQueen.x - xEnemy);

            double xTmp;
            if (friendlyQueen.x < xEnemy) {
                xTmp = friendlyQueen.x - xDelta;
            } else {
                xTmp = friendlyQueen.x + xDelta;
            }
            xTmp = Math.max(0, Math.min(1919, xTmp));
            return (int) xTmp;
        }


        int yEscapeEnemy(int yEnemy) {

            double yDelta = Math.abs(friendlyQueen.y + yEnemy);
            double yTmp;
            if (friendlyQueen.y < yEnemy) {
                yTmp = friendlyQueen.y - yDelta;
            } else {
                yTmp = friendlyQueen.y + yDelta;
            }
            yTmp = Math.max(0, Math.min(999, yTmp));
            return (int) yTmp;
        }

        int xEscapeEnemyTower(int xEnemy, BuildingSite closestTower) {

            double xDelta = closestTower.initialRadius + 45;

            double xTmp;
            if (closestTower.x < xEnemy) {
                xTmp = closestTower.x - xDelta;
            } else {
                xTmp = closestTower.x + xDelta;
            }
            System.err.println("xEnemy = [" + xEnemy + "], xClosestTower = [" + closestTower.x + "]");
            System.err.println("radius = " + xDelta);
            System.err.println("health = " + closestTower.health);
            System.err.println("xTmp = " + xTmp);
            xTmp = Math.max(0, Math.min(1919, xTmp));
            return (int) xTmp;
        }


        int yEscapeEnemyTower(int yEnemy, BuildingSite closestTower) {

            double yDelta = closestTower.initialRadius + 45;
            double yTmp;
            if (closestTower.y < yEnemy) {
                yTmp = closestTower.y - yDelta;
            } else {
                yTmp = closestTower.y + yDelta;
            }
            yTmp = Math.max(0, Math.min(999, yTmp));
            return (int) yTmp;
        }


        // END TESTS

    }

    static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    static double distance(BuildingSite s1, BuildingSite s2) {
        return distance(s1.x, s1.y, s2.x, s2.y);
    }

    static double distance(Unit s1, Unit s2) {
        return distance(s1.x, s1.y, s2.x, s2.y);
    }

    static double distance(BuildingSite s, Unit u) {
        return distance(s.x, s.y, u.x, u.y);
    }

    static double distance(BuildingSite s, int x, int y) {
        return distance(s.x, s.y, x, y);
    }

    static int max(int a, int b) {
        return (a >= b) ? a : b;
    }

    static int min(int a, int b) {
        return (a <= b) ? a : b;
    }

    // Given three colinear points p, q, r, the function checks if
    // point q lies on line segment ‘pr’
    static boolean onSegment(int px, int py, int qx, int qy, int rx, int ry, int qSize) {
        return qx - qSize <= max(px, rx) && qx + qSize >= min(px, rx) && qy - qSize <= max(py, ry) && qy + qSize >= min(py, ry);
    }


    static class Statistics {

        int friendlyKnights;
        int friendlyArchers;
        int friendlyGiants;
        int friendlyBarracksKnights;
        int friendlyBarracksArchers;
        int friendlyBarracksGiants;
        int friendlyTowers;
        int friendlyMines;
        int friendlyIncomeRate;
        int friendlyRemainingGold;

        int enemyKnights;
        int enemyArchers;
        int enemyGiants;
        int enemyBarracksKnights;
        int enemyBarracksArchers;
        int enemyBarracksGiants;
        int enemyTowers;
        int enemyMines;
        int enemyIncomeRate;

        private Statistics() {

        }


        static Statistics gatherStatistics(World world) {
            Statistics result = new Statistics();
            for (int i = 0; i < world.units.length; i++) {
                Unit u = world.units[i];
                if (u.owner == Owner.FRIENDLY) {
                    if (u.unitType == UnitType.KNIGHT) result.friendlyKnights++;
                    if (u.unitType == UnitType.ARCHER) result.friendlyArchers++;
                    if (u.unitType == UnitType.GIANT) result.friendlyGiants++;
                } else if (u.owner == Owner.ENEMY) {
                    if (u.unitType == UnitType.KNIGHT) result.enemyKnights++;
                    if (u.unitType == UnitType.ARCHER) result.enemyArchers++;
                    if (u.unitType == UnitType.GIANT) result.enemyGiants++;
                }
            }
            for (int i = 0; i < world.sites.length; i++) {
                BuildingSite s = world.sites[i];
                if (s.owner == Owner.FRIENDLY) {
                    if (s.type == StructureType.BARRACKS && s.creepType == CreepType.KNIGHT)
                        result.friendlyBarracksKnights++;
                    if (s.type == StructureType.BARRACKS && s.creepType == CreepType.ARCHER)
                        result.friendlyBarracksArchers++;
                    if (s.type == StructureType.BARRACKS && s.creepType == CreepType.GIANT)
                        result.friendlyBarracksGiants++;
                    if (s.type == StructureType.TOWER) result.friendlyTowers++;
                    if (s.type == StructureType.MINE) {
                        result.friendlyMines++;
                        result.friendlyIncomeRate += s.incomeRate;
                        result.friendlyRemainingGold += s.remainingGold;
                    }
                } else if (s.owner == Owner.ENEMY) {
                    if (s.type == StructureType.BARRACKS && s.creepType == CreepType.KNIGHT)
                        result.enemyBarracksKnights++;
                    if (s.type == StructureType.BARRACKS && s.creepType == CreepType.ARCHER)
                        result.enemyBarracksArchers++;
                    if (s.type == StructureType.BARRACKS && s.creepType == CreepType.GIANT)
                        result.enemyBarracksGiants++;
                    if (s.type == StructureType.TOWER) result.enemyTowers++;
                    if (s.type == StructureType.MINE) {
                        result.enemyMines++;
                        result.enemyIncomeRate += s.incomeRate;
                    }
                }
            }
            return result;
        }

        @Override
        public String toString() {
            return "Statistics{\n"
                    + "\tFriendly income rate : " + friendlyIncomeRate + "\n"
                    + "\tFriendly Units : K=" + friendlyKnights + ", A=" + friendlyArchers + ", G=" + friendlyGiants + "\n"
                    + "\tFriendly Sites : BK=" + friendlyBarracksKnights + ", BA=" + friendlyBarracksArchers + ", BG=" + friendlyBarracksGiants + ", TO=" + friendlyTowers + ", MI=" + friendlyMines + "\n"
                    + "\tEnemy income rate : " + enemyIncomeRate + "\n"
                    + "\tEnemy Units : K=" + enemyKnights + " ,A=" + enemyArchers + " ,G=" + enemyGiants + "\n"
                    + "\tEnemy Sites : BK=" + enemyBarracksKnights + ", BA=" + enemyBarracksArchers + ", BG=" + enemyBarracksGiants + ", TO=" + enemyTowers + ", MI=" + enemyMines + "\n"
                    + '}';
        }
    }


    static class BuildingSite {

        final int siteId;
        final int x;
        final int y;
        final int initialRadius;

        StructureType type;
        Owner owner;

        // mines only
        int remainingGold;
        int maximumMineSize;

        // param1
        int turnsBeforeAlive; // barracks
        int health; // tower
        int incomeRate; // mine

        // param 2
        int radius; // tower
        CreepType creepType; // barracks

        // computed data
        double distanceToFriendlyQueen; // all


        BuildingSite(int siteId, int x, int y, int initialRadius) {
            this.siteId = siteId;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.initialRadius = initialRadius;
        }

        @Override
        public String toString() {
            return "BuildingSite{" +
                    "siteId=" + siteId +
                    ", x=" + x +
                    ", y=" + y +
                    ", type=" + type +
                    ", owner=" + owner +
                    ", remainingGold=" + remainingGold +
                    ", maximumMineSize=" + maximumMineSize +
                    ", turnsBeforeAlive=" + turnsBeforeAlive +
                    ", health=" + health +
                    ", incomeRate=" + incomeRate +
                    ", radius=" + radius +
                    ", creepType=" + creepType +
                    ", distanceToFriendlyQueen=" + distanceToFriendlyQueen +
                    '}';
        }
    }


    static class Unit {

        int unitId;
        int x;
        int y;

        Owner owner;
        UnitType unitType;
        int health;

        //
        double distanceToFriendlyQueen;

        Unit(int unitId, int x, int y, Owner owner, UnitType unitType, int health) {
            this.unitId = unitId;
            this.x = x;
            this.y = y;
            this.owner = owner;
            this.unitType = unitType;
            this.health = health;
        }

        @Override
        public String toString() {
            return "Unit{" +
                    "unitId=" + unitId +
                    ", x=" + x +
                    ", y=" + y +
                    ", owner=" + owner +
                    ", unitType=" + unitType +
                    ", health=" + health +
                    ", distanceToFriendlyQueen=" + distanceToFriendlyQueen +
                    '}';
        }
    }

    enum StructureType {NONE, MINE, TOWER, BARRACKS}

    enum Owner {NONE, FRIENDLY, ENEMY}

    enum CreepType {NONE, KNIGHT, ARCHER, GIANT}

    enum UnitType {QUEEN, KNIGHT, ARCHER, GIANT}


    interface Action {

        String getAsString();
    }

    static class Wait implements Action {
        @Override
        public String getAsString() {
            return "WAIT";
        }
    }

    static class Move implements Action {
        int x;
        int y;

        Move(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String getAsString() {
            return "MOVE " + x + " " + y;
        }
    }

    static class BuildBarracks implements Action {
        int siteId;
        CreepType creepType;

        BuildBarracks(int siteId, CreepType creepType) {
            this.siteId = siteId;
            this.creepType = creepType;
        }

        @Override
        public String getAsString() {
            return "BUILD " + siteId + " BARRACKS-" + creepType;
        }
    }

    static class Train implements Action {
        int[] siteIds;

        Train(int... siteIds) {
            this.siteIds = siteIds;
        }

        @Override
        public String getAsString() {
            StringBuilder trainAct = new StringBuilder("TRAIN ");
            for (int i = 0; siteIds != null && i < siteIds.length; i++) {
                trainAct.append(siteIds[i]).append(" ");
            }
            return trainAct.toString().trim();
        }
    }

    static class BuildTower implements Action {
        int siteId;

        BuildTower(int siteId) {
            this.siteId = siteId;
        }

        @Override
        public String getAsString() {
            return "BUILD " + siteId + " TOWER";
        }
    }

    static class BuildMine implements Action {
        int siteId;

        BuildMine(int siteId) {
            this.siteId = siteId;
        }

        @Override
        public String getAsString() {
            return "BUILD " + siteId + " MINE";
        }
    }

}