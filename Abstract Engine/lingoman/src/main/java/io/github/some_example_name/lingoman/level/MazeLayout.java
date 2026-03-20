package io.github.some_example_name.lingoman.level;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.lingoman.model.GameState;

public final class MazeLayout {

    public static final class Layout {
        private final int cols;
        private final int rows;
        private final int tileSize;
        private final float offsetX;
        private final float offsetY;
        private final String[] map;
        private final GridPoint2 playerSpawn;
        private final GridPoint2[] ghostSpawns;
        private final List<GridPoint2> waypointRoute;

        private Layout(
            int cols,
            int rows,
            int tileSize,
            float offsetX,
            float offsetY,
            String[] map,
            GridPoint2 playerSpawn,
            GridPoint2[] ghostSpawns,
            List<GridPoint2> waypointRoute
        ) {
            this.cols = cols;
            this.rows = rows;
            this.tileSize = tileSize;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.map = map;
            this.playerSpawn = new GridPoint2(playerSpawn);
            this.ghostSpawns = copyPoints(ghostSpawns);
            this.waypointRoute = copyList(waypointRoute);
        }

        public int getCols() {
            return cols;
        }

        public int getRows() {
            return rows;
        }

        public int getTileSize() {
            return tileSize;
        }

        public float getOffsetX() {
            return offsetX;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public String[] getMap() {
            return map.clone();
        }

        public GridPoint2 getPlayerSpawn() {
            return new GridPoint2(playerSpawn);
        }

        public GridPoint2[] getGhostSpawns() {
            return copyPoints(ghostSpawns);
        }

        public List<GridPoint2> getWaypointRoute() {
            return copyList(waypointRoute);
        }
    }

    private static final Layout EASY_LAYOUT = createLayout(23, 17, 24, 101L, 8, 2);
    private static final Layout MEDIUM_LAYOUT = createLayout(27, 19, 20, 202L, 18, 5);
    private static final Layout HARD_LAYOUT = createLayout(29, 21, 18, 303L, 30, 8);

    private MazeLayout() {
    }

    public static Layout forDifficulty(GameState.Difficulty difficulty) {
        return switch (difficulty) {
            case MEDIUM -> MEDIUM_LAYOUT;
            case HARD -> HARD_LAYOUT;
            default -> EASY_LAYOUT;
        };
    }

    public static boolean isWall(Layout layout, int col, int row) {
        if (layout == null || row < 0 || row >= layout.rows || col < 0 || col >= layout.cols) {
            return true;
        }
        return layout.map[row].charAt(col) == '#';
    }

    public static boolean isOpen(Layout layout, int col, int row) {
        return !isWall(layout, col, row);
    }

    public static boolean isTunnelRow(Layout layout, int row) {
        return layout != null && row == layout.rows / 2;
    }

    public static List<GridPoint2> collectReachableOpenCells(Layout layout, GridPoint2 start) {
        List<GridPoint2> reachable = new ArrayList<>();
        if (layout == null || start == null || isWall(layout, start.x, start.y)) {
            return reachable;
        }

        boolean[][] visited = new boolean[layout.rows][layout.cols];
        Queue<GridPoint2> queue = new ArrayDeque<>();
        queue.add(new GridPoint2(start));
        visited[start.y][start.x] = true;

        while (!queue.isEmpty()) {
            GridPoint2 current = queue.remove();
            reachable.add(current);

            for (GridPoint2 neighbor : getOpenNeighbors(layout, current)) {
                if (visited[neighbor.y][neighbor.x]) {
                    continue;
                }

                visited[neighbor.y][neighbor.x] = true;
                queue.add(neighbor);
            }
        }

        return reachable;
    }

    public static List<GridPoint2> getOpenNeighbors(Layout layout, GridPoint2 cell) {
        if (layout == null || cell == null) {
            return List.of();
        }

        List<GridPoint2> neighbors = new ArrayList<>(4);
        addNeighbor(layout, neighbors, cell.x + 1, cell.y);
        addNeighbor(layout, neighbors, cell.x - 1, cell.y);
        addNeighbor(layout, neighbors, cell.x, cell.y + 1);
        addNeighbor(layout, neighbors, cell.x, cell.y - 1);
        return neighbors;
    }

    public static GridPoint2 findNextStepTowards(Layout layout, GridPoint2 start, GridPoint2 goal) {
        if (layout == null || start == null || goal == null) {
            return null;
        }
        if (isWall(layout, start.x, start.y) || isWall(layout, goal.x, goal.y)) {
            return null;
        }
        if (start.equals(goal)) {
            return new GridPoint2(goal);
        }

        boolean[][] visited = new boolean[layout.rows][layout.cols];
        GridPoint2[][] previous = new GridPoint2[layout.rows][layout.cols];
        Queue<GridPoint2> queue = new ArrayDeque<>();
        queue.add(new GridPoint2(start));
        visited[start.y][start.x] = true;

        boolean found = false;
        while (!queue.isEmpty() && !found) {
            GridPoint2 current = queue.remove();
            for (GridPoint2 neighbor : getOpenNeighbors(layout, current)) {
                if (visited[neighbor.y][neighbor.x]) {
                    continue;
                }

                visited[neighbor.y][neighbor.x] = true;
                previous[neighbor.y][neighbor.x] = current;
                if (neighbor.equals(goal)) {
                    found = true;
                    break;
                }
                queue.add(neighbor);
            }
        }

        if (!found) {
            return null;
        }

        GridPoint2 step = new GridPoint2(goal);
        GridPoint2 prev = previous[step.y][step.x];
        while (prev != null && !prev.equals(start)) {
            step = prev;
            prev = previous[step.y][step.x];
        }
        return step;
    }

    public static float toWorldX(Layout layout, int col) {
        return layout.offsetX + col * layout.tileSize;
    }

    public static float toWorldY(Layout layout, int row) {
        return layout.offsetY + (layout.rows - 1 - row) * layout.tileSize;
    }

    public static float toWorldXCentered(Layout layout, int col, float size) {
        return toWorldX(layout, col) + (layout.tileSize - size) * 0.5f;
    }

    public static float toWorldYCentered(Layout layout, int row, float size) {
        return toWorldY(layout, row) + (layout.tileSize - size) * 0.5f;
    }

    public static Vector2 toCellCenter(Layout layout, GridPoint2 cell) {
        float centerX = toWorldX(layout, cell.x) + layout.tileSize * 0.5f;
        float centerY = toWorldY(layout, cell.y) + layout.tileSize * 0.5f;
        return new Vector2(centerX, centerY);
    }

    public static GridPoint2 toCell(Layout layout, float x, float y, float size) {
        if (layout == null) {
            return new GridPoint2();
        }

        float centerX = x + size * 0.5f;
        float centerY = y + size * 0.5f;
        int col = (int) ((centerX - layout.offsetX) / layout.tileSize);
        int rowFromBottom = (int) ((centerY - layout.offsetY) / layout.tileSize);
        int row = layout.rows - 1 - rowFromBottom;
        col = Math.max(0, Math.min(layout.cols - 1, col));
        row = Math.max(0, Math.min(layout.rows - 1, row));
        return new GridPoint2(col, row);
    }

    public static float wrapTunnelX(Layout layout, float x, float y, float width, float height) {
        if (layout == null) {
            return x;
        }

        float centerY = y + height * 0.5f;
        int rowFromBottom = (int) ((centerY - layout.offsetY) / layout.tileSize);
        int row = layout.rows - 1 - rowFromBottom;
        if (!isTunnelRow(layout, row)) {
            return x;
        }

        float leftLimit = layout.offsetX - width;
        float rightLimit = layout.offsetX + layout.cols * layout.tileSize;
        if (x <= leftLimit) {
            return rightLimit;
        }
        if (x >= rightLimit) {
            return leftLimit;
        }
        return x;
    }

    private static Layout createLayout(int cols, int rows, int tileSize, long seed, int extraOpenings, int roomCount) {
        char[][] grid = generateMaze(cols, rows, seed);
        addLoops(grid, extraOpenings, seed + 13);
        addRooms(grid, roomCount, seed + 29);

        float offsetX = (640f - cols * tileSize) * 0.5f;
        float offsetY = (480f - rows * tileSize) * 0.5f;

        GridPoint2 playerSpawn = new GridPoint2(1, rows - 2);
        GridPoint2[] ghostSpawns = {
            new GridPoint2(cols - 2, 1),
            new GridPoint2(cols - 2, rows - 2),
            new GridPoint2(1, 1),
            new GridPoint2(makeOdd(cols / 2), 1)
        };

        carveSpawnBuffer(grid, playerSpawn);
        for (GridPoint2 ghostSpawn : ghostSpawns) {
            carveSpawnBuffer(grid, ghostSpawn);
        }

        List<GridPoint2> route = List.of(
            new GridPoint2(1, 1),
            new GridPoint2(cols - 2, 1),
            new GridPoint2(cols - 2, rows - 2),
            new GridPoint2(1, rows - 2),
            new GridPoint2(makeOdd(cols / 2), makeOdd(rows / 2)),
            new GridPoint2(makeOdd(cols / 3), makeOdd(rows / 2)),
            new GridPoint2(makeOdd((cols * 2) / 3), makeOdd(rows / 2))
        );

        return new Layout(cols, rows, tileSize, offsetX, offsetY, toStrings(grid), playerSpawn, ghostSpawns, route);
    }

    private static char[][] generateMaze(int cols, int rows, long seed) {
        char[][] grid = new char[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid[row][col] = '#';
            }
        }

        Random rng = new Random(seed);
        boolean[][] visited = new boolean[rows][cols];
        ArrayDeque<GridPoint2> stack = new ArrayDeque<>();
        GridPoint2 start = new GridPoint2(1, 1);
        stack.push(start);
        visited[start.y][start.x] = true;
        grid[start.y][start.x] = '.';

        int[][] directions = {
            {2, 0},
            {-2, 0},
            {0, 2},
            {0, -2}
        };

        while (!stack.isEmpty()) {
            GridPoint2 current = stack.peek();
            List<int[]> choices = new ArrayList<>();
            for (int[] direction : directions) {
                int nextCol = current.x + direction[0];
                int nextRow = current.y + direction[1];
                if (nextCol <= 0 || nextCol >= cols - 1 || nextRow <= 0 || nextRow >= rows - 1) {
                    continue;
                }
                if (visited[nextRow][nextCol]) {
                    continue;
                }
                choices.add(direction);
            }

            if (choices.isEmpty()) {
                stack.pop();
                continue;
            }

            int[] chosen = choices.get(rng.nextInt(choices.size()));
            int betweenCol = current.x + chosen[0] / 2;
            int betweenRow = current.y + chosen[1] / 2;
            int nextCol = current.x + chosen[0];
            int nextRow = current.y + chosen[1];

            grid[betweenRow][betweenCol] = '.';
            grid[nextRow][nextCol] = '.';
            visited[nextRow][nextCol] = true;
            stack.push(new GridPoint2(nextCol, nextRow));
        }

        return grid;
    }

    private static void addLoops(char[][] grid, int extraOpenings, long seed) {
        List<GridPoint2> candidates = new ArrayList<>();
        int rows = grid.length;
        int cols = grid[0].length;

        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < cols - 1; col++) {
                if (grid[row][col] != '#') {
                    continue;
                }

                boolean opensHorizontal = grid[row][col - 1] == '.' && grid[row][col + 1] == '.';
                boolean opensVertical = grid[row - 1][col] == '.' && grid[row + 1][col] == '.';
                if (opensHorizontal || opensVertical) {
                    candidates.add(new GridPoint2(col, row));
                }
            }
        }

        Collections.shuffle(candidates, new Random(seed));
        int openings = Math.min(extraOpenings, candidates.size());
        for (int i = 0; i < openings; i++) {
            GridPoint2 cell = candidates.get(i);
            grid[cell.y][cell.x] = '.';
        }
    }

    private static void addRooms(char[][] grid, int roomCount, long seed) {
        if (roomCount <= 0) {
            return;
        }

        int rows = grid.length;
        int cols = grid[0].length;
        List<GridPoint2> openCells = new ArrayList<>();
        for (int row = 2; row < rows - 2; row++) {
            for (int col = 2; col < cols - 2; col++) {
                if (grid[row][col] == '.') {
                    openCells.add(new GridPoint2(col, row));
                }
            }
        }

        Collections.shuffle(openCells, new Random(seed));
        int rooms = Math.min(roomCount, openCells.size());
        for (int i = 0; i < rooms; i++) {
            GridPoint2 center = openCells.get(i);
            carveRoom(grid, center.x, center.y);
        }
    }

    private static void carveRoom(char[][] grid, int centerCol, int centerRow) {
        for (int row = centerRow - 1; row <= centerRow + 1; row++) {
            for (int col = centerCol - 1; col <= centerCol + 1; col++) {
                if (row <= 0 || row >= grid.length - 1 || col <= 0 || col >= grid[0].length - 1) {
                    continue;
                }
                grid[row][col] = '.';
            }
        }
    }

    private static void carveSpawnBuffer(char[][] grid, GridPoint2 spawn) {
        if (spawn == null) {
            return;
        }

        openIfInside(grid, spawn.x, spawn.y);
        openIfInside(grid, spawn.x - 1, spawn.y);
        openIfInside(grid, spawn.x + 1, spawn.y);
        openIfInside(grid, spawn.x, spawn.y - 1);
        openIfInside(grid, spawn.x, spawn.y + 1);
    }

    private static void openIfInside(char[][] grid, int col, int row) {
        if (row <= 0 || row >= grid.length - 1 || col <= 0 || col >= grid[0].length - 1) {
            return;
        }
        grid[row][col] = '.';
    }

    private static void addNeighbor(Layout layout, List<GridPoint2> neighbors, int col, int row) {
        if (!isWall(layout, col, row)) {
            neighbors.add(new GridPoint2(col, row));
        }
    }

    private static String[] toStrings(char[][] grid) {
        String[] lines = new String[grid.length];
        for (int row = 0; row < grid.length; row++) {
            lines[row] = new String(grid[row]);
        }
        return lines;
    }

    private static int makeOdd(int value) {
        if (value <= 1) {
            return 1;
        }
        return (value % 2 == 0) ? value - 1 : value;
    }

    private static GridPoint2[] copyPoints(GridPoint2[] source) {
        GridPoint2[] copy = new GridPoint2[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = new GridPoint2(source[i]);
        }
        return copy;
    }

    private static List<GridPoint2> copyList(List<GridPoint2> source) {
        List<GridPoint2> copy = new ArrayList<>(source.size());
        for (GridPoint2 point : source) {
            copy.add(new GridPoint2(point));
        }
        return copy;
    }
}
