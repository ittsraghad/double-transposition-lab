import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;


public class DoubleTransposition {

    // ENGLISH DICTIONARY - Used to score decryption results (higher score = more
    // English-like)
    private static final Set<String> ENGLISH_WORDS_SET = new HashSet<>(Arrays.asList(
            "the", "and", "for", "you", "are", "this", "that", "have", "from", "with",
            "your", "will", "can", "please", "help", "come", "see", "look", "hello",
            "world", "secret", "message", "attack", "meet", "bridge", "night", "day",
            "system", "IT", "course", "computer", "network", "security", "password", "username",
            "is", "to", "of", "in", "it", "on", "at", "by", "be", "he", "she", "we",
            "yes", "no", "good", "bad", "new", "old", "big", "small"));

    // Stores each hacking result for sorting by score
    static class HackResult implements Comparable<HackResult> {
        String rowKey;
        String colKey;
        String text;
        int score;
        int rows, cols;

        HackResult(String rowKey, String colKey, String text, int score, int rows, int cols) {
            this.rowKey = rowKey;
            this.colKey = colKey;
            this.text = text;
            this.score = score;
            this.rows = rows;
            this.cols = cols;
        }

        public int compareTo(HackResult other) {
            return Integer.compare(other.score, this.score);
        }
    }

    // ENCRYPTION: Fill grid row by row, then rearrange rows and columns according to keys
    public static String encrypt(String plaintext, int rows, int cols, String rowKey, String colKey) {
        int totalCells = rows * cols;
        String text = plaintext;

        // Pad with underscores if needed
        if (text.length() < totalCells) {
            text = String.format("%-" + totalCells + "s", text).replace(' ', '_');
        } else {
            text = text.substring(0, totalCells);
        }

        // Fill grid row by row
        char[][] grid = new char[rows][cols];
        int idx = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = text.charAt(idx++);

        // Rearrange rows according to key
        char[][] afterRowSwap = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            int newRow = Integer.parseInt(String.valueOf(rowKey.charAt(r))) - 1;
            afterRowSwap[r] = grid[newRow];
        }

        // Rearrange columns according to key
        char[][] afterColSwap = new char[rows][cols];
        for (int c = 0; c < cols; c++) {
            int newCol = Integer.parseInt(String.valueOf(colKey.charAt(c))) - 1;
            for (int r = 0; r < rows; r++)
                afterColSwap[r][c] = afterRowSwap[r][newCol];
        }

        // Read ciphertext row by row
        StringBuilder result = new StringBuilder();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                result.append(afterColSwap[r][c]);

        return result.toString();
    }

    // DECRYPTION: Reverse of encryption process
    public static String decrypt(String ciphertext, int rows, int cols, String rowKey, String colKey) {
        try {
            int totalCells = rows * cols;
            String text = ciphertext;

            if (text.length() < totalCells) {
                text = String.format("%-" + totalCells + "s", text).replace(' ', '_');
            } else if (text.length() > totalCells) {
                text = text.substring(0, totalCells);
            }

            // Single row case
            if (rows == 1) {
                char[] chars = text.toCharArray();
                char[] result = new char[cols];
                for (int i = 0; i < cols && i < colKey.length(); i++) {
                    int pos = Integer.parseInt(String.valueOf(colKey.charAt(i))) - 1;
                    if (pos >= 0 && pos < cols) {
                        result[pos] = chars[i];
                    } else {
                        result[i] = chars[i];
                    }
                }
                return new String(result).replace('_', ' ').trim();
            }

            // Single column case
            if (cols == 1) {
                char[] chars = text.toCharArray();
                char[] result = new char[rows];
                for (int i = 0; i < rows && i < rowKey.length(); i++) {
                    int pos = Integer.parseInt(String.valueOf(rowKey.charAt(i))) - 1;
                    if (pos >= 0 && pos < rows) {
                        result[pos] = chars[i];
                    } else {
                        result[i] = chars[i];
                    }
                }
                return new String(result).replace('_', ' ').trim();
            }

            // Fill grid row by row
            char[][] grid = new char[rows][cols];
            int idx = 0;
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    grid[r][c] = text.charAt(idx++);

            // Reverse column swap
            char[][] revCols = new char[rows][cols];
            for (int c = 0; c < cols && c < colKey.length(); c++) {
                int originalCol = Integer.parseInt(String.valueOf(colKey.charAt(c))) - 1;
                if (originalCol >= 0 && originalCol < cols) {
                    for (int r = 0; r < rows; r++)
                        revCols[r][originalCol] = grid[r][c];
                } else {
                    for (int r = 0; r < rows; r++)
                        revCols[r][c] = grid[r][c];
                }
            }

            // Reverse row swap
            char[][] revRows = new char[rows][cols];
            for (int r = 0; r < rows && r < rowKey.length(); r++) {
                int originalRow = Integer.parseInt(String.valueOf(rowKey.charAt(r))) - 1;
                if (originalRow >= 0 && originalRow < rows) {
                    revRows[originalRow] = revCols[r];
                } else {
                    revRows[r] = revCols[r];
                }
            }

            // Read plaintext row by row
            StringBuilder result = new StringBuilder();
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    result.append(revRows[r][c]);

            return result.toString().replace('_', ' ').trim();

        } catch (Exception e) {
            return "";
        }
    }

    // Score text based on English word count (0-100)
    private static int calculateTextScore(String text) {
        if (text == null || text.length() < 3)
            return 0;

        String[] words = text.toLowerCase().split(" ");
        int validWords = 0;
        int totalWords = 0;

        for (String w : words) {
            StringBuilder cleanBuilder = new StringBuilder();
            for (char c : w.toCharArray()) {
                if (c >= 'a' && c <= 'z') {
                    cleanBuilder.append(c);
                }
            }
            String clean = cleanBuilder.toString();

            if (clean.length() >= 2) {
                totalWords++;
                if (ENGLISH_WORDS_SET.contains(clean)) {
                    validWords++;
                }
            }
        }

        if (totalWords == 0)
            return 0;
        return (validWords * 100) / totalWords;
    }

    // Generates all permutations of 1..n sequentially
    static class PermutationGenerator implements Iterator<String> {
        private final int n;
        private final int[] indices;
        private boolean hasNext;
        private String current;

        public PermutationGenerator(int n) {
            this.n = n;
            this.indices = new int[n];
            for (int i = 0; i < n; i++)
                indices[i] = i;
            this.hasNext = n > 0;
            generateCurrent();
        }

        private void generateCurrent() {
            if (!hasNext) {
                current = null;
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++)
                sb.append(indices[i] + 1);
            current = sb.toString();
        }

        public boolean hasNext() {
            return hasNext;
        }

        public String next() {
            String result = current;
            moveToNext();
            return result;
        }

        // Standard algorithm for next lexicographic permutation
        private void moveToNext() {
            if (!hasNext)
                return;

            int i = n - 2;
            while (i >= 0 && indices[i] >= indices[i + 1])
                i--;

            if (i < 0) {
                hasNext = false;
                current = null;
                return;
            }

            int j = n - 1;
            while (indices[j] <= indices[i])
                j--;

            int temp = indices[i];
            indices[i] = indices[j];
            indices[j] = temp;

            int left = i + 1, right = n - 1;
            while (left < right) {
                temp = indices[left];
                indices[left] = indices[right];
                indices[right] = temp;
                left++;
                right--;
            }
            generateCurrent();
        }
    }

    // TASK 2: Hack with known dimensions - brute force all row! × col! permutations
    public static void hackKnownDimensions(String ciphertext, int rows, int cols) {
        long rowPerms = factorial(rows);
        long colPerms = factorial(cols);
        long totalCombos = rowPerms * colPerms;

        System.out.println("\n" + "=".repeat(60));
        System.out.println("TASK #2: Hacking With Known Dimensions");
        System.out.println("=".repeat(60));
        System.out.println("Ciphertext: " + ciphertext);
        System.out.println("Dimensions: " + rows + " x " + cols);
        System.out.println("Number of Row Permutations (" + rows + "!): " + formatNumber(rowPerms));
        System.out.println("Number of Column Permutations (" + cols + "!): " + formatNumber(colPerms));
        System.out.println("Total Key Combinations Tested: " + formatNumber(totalCombos));
        System.out.println("=".repeat(60));
        System.out.println("Searching...\n");

        java.util.List<HackResult> allRawResults = new ArrayList<>();

        PermutationGenerator rowIter = new PermutationGenerator(rows);
        while (rowIter.hasNext()) {
            String rKey = rowIter.next();
            PermutationGenerator colIter = new PermutationGenerator(cols);
            while (colIter.hasNext()) {
                String cKey = colIter.next();
                String result = decrypt(ciphertext, rows, cols, rKey, cKey);
                int score = calculateTextScore(result);
                allRawResults.add(new HackResult(rKey, cKey, result, score, rows, cols));
            }
        }

        Collections.sort(allRawResults);
        displayResultsInWindow(allRawResults, rows, cols, totalCombos, rowPerms, colPerms, "Task #2");
    }

    // TASK 3: Hack with unknown dimensions - finds both grid size and keys
    public static void hackUnknownDimensions(String ciphertext) {
        int length = ciphertext.length();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("TASK #3: Hacking With Unknown Dimensions");
        System.out.println("=".repeat(70));
        System.out.println("Ciphertext: \"" + ciphertext + "\"");
        System.out.println("Length: " + length);
        System.out.println("=".repeat(70));

        // Find all factor pairs where rows × cols = length
        java.util.List<int[]> allDimensions = new ArrayList<>();
        for (int rows = 1; rows <= length; rows++) {
            if (length % rows == 0) {
                int cols = length / rows;
                if (rows <= cols) {
                    allDimensions.add(new int[] { rows, cols });
                }
            }
        }

        // Filter out impractical dimensions
        java.util.List<int[]> validDimensions = new ArrayList<>();

        System.out.println("\nAnalyzing possible dimensions...");

        for (int[] dim : allDimensions) {
            int rows = dim[0];
            int cols = dim[1];
            long totalCombos = factorial(rows) * factorial(cols);

            if (rows == 1 || cols == 1) {
                System.out.println("  [SKIP] " + rows + " x " + cols + " -> too slow");
                continue;
            }

            if (rows > 10 || cols > 10) {
                System.out.println("  [SKIP] " + rows + " x " + cols + " -> dimensions too large");
                continue;
            }

            if (totalCombos > 10_000_000) {
                System.out.println("  [SKIP] " + rows + " x " + cols + " -> " + formatNumber(totalCombos)
                        + " combinations (too many)");
                continue;
            }

            System.out
                    .println("  [KEEP] " + rows + " x " + cols + " -> " + formatNumber(totalCombos) + " combinations");
            validDimensions.add(dim);
        }

        if (validDimensions.isEmpty()) {
            System.out.println("\n[ERROR] No valid dimensions found!");
            return;
        }

        // Sort by total combinations (fastest first)
        validDimensions.sort((a, b) -> {
            long combosA = factorial(a[0]) * factorial(a[1]);
            long combosB = factorial(b[0]) * factorial(b[1]);
            return Long.compare(combosA, combosB);
        });

        System.out.println("\nDimensions to test (fastest to slowest):");
        for (int[] d : validDimensions) {
            long combos = factorial(d[0]) * factorial(d[1]);
            System.out.println("  " + d[0] + " x " + d[1] + " -> " + formatNumber(combos) + " combinations");
        }

        java.util.List<HackResult> allRawResults = new ArrayList<>();
        long grandTotalCombos = 0;

        // Test each valid dimension
        for (int[] dim : validDimensions) {
            int rows = dim[0];
            int cols = dim[1];
            long rowPerms = factorial(rows);
            long colPerms = factorial(cols);
            long totalCombos = rowPerms * colPerms;

            grandTotalCombos += totalCombos;

            PermutationGenerator rowIter = new PermutationGenerator(rows);
            while (rowIter.hasNext()) {
                String rKey = rowIter.next();
                PermutationGenerator colIter = new PermutationGenerator(cols);
                while (colIter.hasNext()) {
                    String cKey = colIter.next();
                    String result = decrypt(ciphertext, rows, cols, rKey, cKey);
                    int score = calculateTextScore(result);
                    allRawResults.add(new HackResult(rKey, cKey, result, score, rows, cols));
                }
            }
        }

        Collections.sort(allRawResults);
        displayResultsInWindow(allRawResults, validDimensions.size(), grandTotalCombos, "Task #3");
    }

    // Display Task 2 results in popup window
    private static void displayResultsInWindow(java.util.List<HackResult> allResults, int rows, int cols,
            long totalCombos, long rowPerms, long colPerms, String taskName) {

        java.util.List<HackResult> goodResults = new ArrayList<>();
        java.util.List<HackResult> badResults = new ArrayList<>();

        for (HackResult r : allResults) {
            if (r.score >= 30) {
                goodResults.add(r);
            } else {
                badResults.add(r);
            }
        }

        StringBuilder output = new StringBuilder();
        output.append("=".repeat(75)).append("\n");
        output.append("                    HACKING RESULTS - ").append(taskName).append("\n");
        output.append("=".repeat(75)).append("\n\n");

        output.append("=== KEY COMBINATIONS STATISTICS ===\n");
        output.append("Dimensions: ").append(rows).append(" rows x ").append(cols).append(" columns\n");
        output.append("Number of Row Permutations (").append(rows).append("!): ").append(formatNumber(rowPerms))
                .append("\n");
        output.append("Number of Column Permutations (").append(cols).append("!): ").append(formatNumber(colPerms))
                .append("\n");
        output.append("Total Key Combinations Tested: ").append(formatNumber(totalCombos)).append("\n");
        output.append("Good results: ").append(goodResults.size()).append("\n");
        output.append("=".repeat(75)).append("\n\n");

        if (!goodResults.isEmpty()) {
            output.append("=== READABLE RESULTS ===\n");
            output.append("-".repeat(75)).append("\n\n");

            int displayCount = Math.min(50, goodResults.size());
            for (int i = 0; i < displayCount; i++) {
                HackResult r = goodResults.get(i);
                output.append("RESULT #").append(i + 1).append("\n");
                output.append("   Row Key: ").append(r.rowKey).append("\n");
                output.append("   Column Key: ").append(r.colKey).append("\n");
                output.append("   Decrypted Text: \"").append(r.text).append("\"\n");
                output.append("-".repeat(50)).append("\n\n");
            }

            if (goodResults.size() > displayCount) {
                output.append("... and ").append(goodResults.size() - displayCount).append(" more good results\n");
            }
            output.append("\n");
        }

        if (goodResults.isEmpty() && !badResults.isEmpty()) {
            output.append("=== WARNING: No readable English results found (Score < 30) ===\n");
            output.append("Showing ALL raw results (including gibberish) for diagnosis:\n");
            output.append("-".repeat(75)).append("\n\n");

            int displayCount = Math.min(100, badResults.size());
            for (int i = 0; i < displayCount; i++) {
                HackResult r = badResults.get(i);
                output.append("RESULT #").append(i + 1).append("\n");
                output.append("   Row Key: ").append(r.rowKey).append("\n");
                output.append("   Column Key: ").append(r.colKey).append("\n");
                output.append("   Text: \"").append(r.text).append("\"\n");
                output.append("-".repeat(50)).append("\n\n");
            }

            if (badResults.size() > displayCount) {
                output.append("... and ").append(badResults.size() - displayCount).append(" more raw results\n");
            }
        } else if (goodResults.isEmpty() && badResults.isEmpty()) {
            output.append("No results were found. Something went wrong.\n");
        }

        output.append("\n").append("=".repeat(75)).append("\n");
        output.append("                    END OF RESULTS\n");
        output.append("=".repeat(75)).append("\n");

        showResultsWindow(output.toString(), "Hacking Results - " + taskName);
    }

    // Display Task 3 results in popup window
    private static void displayResultsInWindow(java.util.List<HackResult> allResults, int totalDims,
            long grandTotalCombos, String taskName) {

        java.util.List<HackResult> goodResults = new ArrayList<>();
        java.util.List<HackResult> badResults = new ArrayList<>();

        for (HackResult r : allResults) {
            if (r.score >= 30) {
                goodResults.add(r);
            } else {
                badResults.add(r);
            }
        }

        StringBuilder output = new StringBuilder();
        output.append("=".repeat(75)).append("\n");
        output.append("                    HACKING RESULTS - ").append(taskName).append("\n");
        output.append("=".repeat(75)).append("\n\n");

        output.append("=== KEY COMBINATIONS STATISTICS ===\n");
        output.append("Number of dimensions tested: ").append(totalDims).append("\n");
        output.append("Total Key Combinations Tested: ").append(formatNumber(grandTotalCombos)).append("\n");
        output.append("Good results: ").append(goodResults.size()).append("\n");
        output.append("=".repeat(75)).append("\n\n");

        if (!goodResults.isEmpty()) {
            output.append("=== READABLE RESULTS (Score >= 30) ===\n");
            output.append("-".repeat(75)).append("\n\n");

            int displayCount = Math.min(50, goodResults.size());
            for (int i = 0; i < displayCount; i++) {
                HackResult r = goodResults.get(i);
                output.append("RESULT #").append(i + 1).append("\n");
                output.append("   Dimensions: ").append(r.rows).append(" x ").append(r.cols).append("\n");
                output.append("   Row Key: ").append(r.rowKey).append("\n");
                output.append("   Column Key: ").append(r.colKey).append("\n");
                output.append("   Decrypted Text: \"").append(r.text).append("\"\n");
                output.append("-".repeat(50)).append("\n\n");
            }

            if (goodResults.size() > displayCount) {
                output.append("... and ").append(goodResults.size() - displayCount).append(" more good results\n");
            }
            output.append("\n");
        }

        if (goodResults.isEmpty() && !badResults.isEmpty()) {
            output.append("=== WARNING: No readable English results found (Score < 30) ===\n");
            output.append("Showing ALL raw results (including gibberish) for diagnosis:\n");
            output.append("-".repeat(75)).append("\n\n");

            int displayCount = Math.min(100, badResults.size());
            for (int i = 0; i < displayCount; i++) {
                HackResult r = badResults.get(i);
                output.append("RESULT #").append(i + 1).append("\n");
                output.append("   Dimensions: ").append(r.rows).append(" x ").append(r.cols).append("\n");
                output.append("   Row Key: ").append(r.rowKey).append("\n");
                output.append("   Column Key: ").append(r.colKey).append("\n");
                output.append("   Text: \"").append(r.text).append("\"\n");
                output.append("-".repeat(50)).append("\n\n");
            }

            if (badResults.size() > displayCount) {
                output.append("... and ").append(badResults.size() - displayCount).append(" more raw results\n");
            }
        } else if (goodResults.isEmpty() && badResults.isEmpty()) {
            output.append("No results were found. Something went wrong.\n");
        }

        output.append("\n").append("=".repeat(75)).append("\n");
        output.append("                    END OF RESULTS\n");
        output.append("=".repeat(75)).append("\n");

        showResultsWindow(output.toString(), "Hacking Results - " + taskName);
    }

    // Display text in scrollable popup window
    private static void showResultsWindow(String content, String title) {
        JFrame resultFrame = new JFrame(title);
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resultFrame.setSize(850, 650);
        resultFrame.setLocationRelativeTo(null);

        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        resultFrame.add(scrollPane);
        resultFrame.setVisible(true);
    }

    // Direct decryption when user knows all keys
    public static void decryptWithKey(String ciphertext, int rows, int cols, String rowKey, String colKey) {
        String result = decrypt(ciphertext, rows, cols, rowKey, colKey);
        System.out.println("\n" + "=".repeat(50));
        System.out.println("DECRYPTION RESULT");
        System.out.println("=".repeat(50));
        System.out.println("Ciphertext: " + ciphertext);
        System.out.println("Dimensions: " + rows + " x " + cols);
        System.out.println("Row Key: " + rowKey);
        System.out.println("Column Key: " + colKey);
        System.out.println("Plaintext: \"" + result + "\"");
        System.out.println("=".repeat(50));
    }

    // Calculate factorial (returns -1 if n > 20)
    private static long factorial(int n) {
        if (n > 20)
            return -1;
        long result = 1;
        for (int i = 2; i <= n; i++)
            result *= i;
        return result;
    }

    // Format large numbers (K, M, B)
    private static String formatNumber(long n) {
        if (n == -1)
            return "Very Large";
        if (n < 1000)
            return String.valueOf(n);
        if (n < 1000000)
            return String.format("%.2fK", n / 1000.0);
        if (n < 1000000000)
            return String.format("%.2fM", n / 1000000.0);
        return String.format("%.2fB", n / 1000000000.0);
    }

    // =====================================================================
    // WEB INTERFACE (added)
    // A tiny built-in HTTP server (no libraries needed). The website in the
    // "web" folder sends requests here, and the SAME encrypt/decrypt/hack
    // logic above does the work. Results are returned as JSON instead of
    // being printed or shown in Swing windows.
    // =====================================================================

    private static final long MAX_COMBOS = 10_000_000L; // same limit as Task #3
    private static final int MAX_SIZE = 9; // keys are written as single digits (1-9)

    // Key must be a permutation of 1..n written as digits, e.g. "3142"
    private static String checkKey(String key, int n, String name) {
        if (key == null || key.length() != n)
            return name + " must have exactly " + n + " digits.";
        boolean[] seen = new boolean[n];
        for (char ch : key.toCharArray()) {
            int d = ch - '0';
            if (d < 1 || d > n || seen[d - 1])
                return name + " must use each digit from 1 to " + n + " exactly once.";
            seen[d - 1] = true;
        }
        return null;
    }

    // The 3 grid stages of encryption (same steps as encrypt()), flattened row by row
    public static String[] encryptStages(String plaintext, int rows, int cols, String rowKey, String colKey) {
        int totalCells = rows * cols;
        String text = plaintext;
        if (text.length() < totalCells) {
            text = String.format("%-" + totalCells + "s", text).replace(' ', '_');
        } else {
            text = text.substring(0, totalCells);
        }
        char[][] grid = new char[rows][cols];
        int idx = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = text.charAt(idx++);

        char[][] afterRows = new char[rows][cols];
        for (int r = 0; r < rows; r++)
            afterRows[r] = grid[rowKey.charAt(r) - '1'];

        char[][] afterCols = new char[rows][cols];
        for (int c = 0; c < cols; c++)
            for (int r = 0; r < rows; r++)
                afterCols[r][c] = afterRows[r][colKey.charAt(c) - '1'];

        return new String[] { flatten(grid), flatten(afterRows), flatten(afterCols) };
    }

    private static String flatten(char[][] g) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : g)
            sb.append(row);
        return sb.toString();
    }

    // ---------------------------------------------------------------------
    // ENGINE v2 (added): smarter scoring, multithreaded search, security analysis
    // ---------------------------------------------------------------------

    private static final int GOOD_SCORE = 50; // "readable" threshold for smartScore

    // Extra common English words (the original list is kept and reused too)
    private static final Set<String> EXTRA_WORDS = new HashSet<>(Arrays.asList((
            "a i about after again all also always am an another any around as ask away back because been before "
            + "being between both bridge but buy call came castle come could cyber data dawn day did do does done "
            + "down each end evening even every first get give go going got great had has her here him his home how "
            + "just know last late let life like little long look made make many may me meeting meet message midnight "
            + "more morning most much must my need never next night noon not now off one only open or other our out "
            + "over own part people place plan please point report right said same say secret send sent should show so "
            + "some still such take team tell than their them then there these they thing think those three through "
            + "time today tomorrow too two under until up us use very want was way well were what when where which "
            + "while who why will without word work would").split(" ")));

    // The most frequent English letter pairs (bigrams)
    private static final Set<String> COMMON_BIGRAMS = new HashSet<>(Arrays.asList((
            "th he in er an re on at en nd ti es or te of ed is it al ar st to nt ng se ha as ou io le ve co me de "
            + "hi ri ro ic ne ea ra ce li ch ll be ma si om ur ca el ta la ns di fo ho pe ec pr no ct us ac ot il "
            + "tr ly nc et ut ss so rs un lo wa ge ie wh ee wi em ad ol rt po we na ul ni ts mo ow pa im mi ai sh "
            + "ir su id os iv ia am fi ci vi pl ig tu ev ld ry mp fe bl ab gh ty op wo sa ay ex ke fr oo av ag if "
            + "ap gr od bo sp rd do uc bu ei ov by rm ep tt").split(" ")));

    // Score 0-100: 60% = how many letters belong to real English words,
    // 40% = how many letter pairs inside words look like English.
    // (Transposition keeps letter frequencies, so pairs + words are what reveal the plaintext.)
    private static int smartScore(String text) {
        if (text == null || text.length() < 3)
            return 0;
        int letters = 0, inWords = 0, pairs = 0, pairHits = 0;
        for (String w : text.toLowerCase().split(" ")) {
            StringBuilder sb = new StringBuilder();
            for (char c : w.toCharArray())
                if (c >= 'a' && c <= 'z')
                    sb.append(c);
            String clean = sb.toString();
            letters += clean.length();
            if (clean.length() >= 2 && (ENGLISH_WORDS_SET.contains(clean) || EXTRA_WORDS.contains(clean)))
                inWords += clean.length();
            for (int i = 0; i + 1 < clean.length(); i++) {
                pairs++;
                if (COMMON_BIGRAMS.contains(clean.substring(i, i + 2)))
                    pairHits++;
            }
        }
        if (letters == 0)
            return 0;
        double wordPart = (double) inWords / letters;
        double pairPart = pairs == 0 ? 0 : Math.min(1.0, (pairHits / (double) pairs) / 0.8);
        return (int) Math.round(100 * (0.6 * wordPart + 0.4 * pairPart));
    }

    // A background search. The website polls it for live progress.
    static class Job {
        final long total;
        final java.util.concurrent.atomic.AtomicLong tested = new java.util.concurrent.atomic.AtomicLong();
        final java.util.concurrent.atomic.AtomicLong good = new java.util.concurrent.atomic.AtomicLong();
        final java.util.List<HackResult> top = new ArrayList<>(); // best 15, guarded by synchronized(this)
        final long start = System.currentTimeMillis();
        volatile long end = 0;
        volatile boolean cancelled = false;
        volatile int minScore = -1;
        volatile String dim = "";
        String logJson = "[]";

        Job(long total) {
            this.total = total;
        }

        boolean done() {
            return end != 0;
        }
    }

    private static final Map<String, Job> JOBS = new java.util.concurrent.ConcurrentHashMap<>();

    // Keep only the best 15 results in memory (instead of millions)
    private static void offer(Job j, HackResult r) {
        synchronized (j) {
            if (j.top.size() < 15) {
                j.top.add(r);
            } else {
                int worst = 0;
                for (int i = 1; i < j.top.size(); i++)
                    if (j.top.get(i).score < j.top.get(worst).score)
                        worst = i;
                if (r.score <= j.top.get(worst).score)
                    return;
                j.top.set(worst, r);
            }
            int m = Integer.MAX_VALUE;
            for (HackResult h : j.top)
                m = Math.min(m, h.score);
            j.minScore = j.top.size() < 15 ? -1 : m;
        }
    }

    // Starts a brute-force search on all CPU cores and returns the job id
    private static String startJob(String ciphertext, java.util.List<int[]> dims, String logJson) {
        long total = 0;
        for (int[] d : dims)
            total += factorial(d[0]) * factorial(d[1]);
        long now = System.currentTimeMillis();
        int running = 0;
        for (Job j : JOBS.values()) if (!j.done()) running++;
        if (running >= 3) throw new IllegalArgumentException("Server is busy, try again in a few seconds.");
        JOBS.entrySet().removeIf(e -> e.getValue().done() && now - e.getValue().end > 600_000);

        String id = Long.toString(System.nanoTime(), 36);
        Job job = new Job(total);
        job.logJson = logJson;
        JOBS.put(id, job);

        Thread worker = new Thread(() -> {
            try {
                for (int[] d : dims) {
                    if (job.cancelled)
                        break;
                    final int rows = d[0], cols = d[1];
                    job.dim = rows + "x" + cols;
                    java.util.List<String> rowKeys = new ArrayList<>();
                    PermutationGenerator g = new PermutationGenerator(rows);
                    while (g.hasNext())
                        rowKeys.add(g.next());
                    // each row key is processed by a different CPU core
                    rowKeys.parallelStream().forEach(rKey -> {
                        PermutationGenerator colIter = new PermutationGenerator(cols);
                        long t = 0, gd = 0;
                        while (colIter.hasNext() && !job.cancelled) {
                            String cKey = colIter.next();
                            String text = decrypt(ciphertext, rows, cols, rKey, cKey);
                            int score = smartScore(text);
                            t++;
                            if (score >= GOOD_SCORE)
                                gd++;
                            if (score > job.minScore)
                                offer(job, new HackResult(rKey, cKey, text, score, rows, cols));
                            if (t == 4096) {
                                job.tested.addAndGet(t);
                                job.good.addAndGet(gd);
                                t = 0;
                                gd = 0;
                            }
                        }
                        job.tested.addAndGet(t);
                        job.good.addAndGet(gd);
                    });
                }
            } finally {
                job.end = System.currentTimeMillis();
            }
        });
        worker.setDaemon(true);
        worker.start();
        return id;
    }

    private static String jobJson(Job job) {
        java.util.List<HackResult> copy;
        synchronized (job) {
            copy = new ArrayList<>(job.top);
        }
        Collections.sort(copy);
        long ms = (job.done() ? job.end : System.currentTimeMillis()) - job.start;
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"done\":").append(job.done()).append(",\"cancelled\":").append(job.cancelled)
                .append(",\"tested\":").append(job.tested.get()).append(",\"total\":").append(job.total)
                .append(",\"good\":").append(job.good.get()).append(",\"ms\":").append(ms)
                .append(",\"dim\":").append(q(job.dim)).append(",\"threads\":")
                .append(Runtime.getRuntime().availableProcessors()).append(",\"log\":").append(job.logJson)
                .append(",\"results\":[");
        for (int i = 0; i < copy.size(); i++) {
            HackResult r = copy.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"rows\":").append(r.rows).append(",\"cols\":").append(r.cols)
                    .append(",\"rowKey\":").append(q(r.rowKey)).append(",\"colKey\":").append(q(r.colKey))
                    .append(",\"text\":").append(q(r.text)).append(",\"score\":").append(r.score).append("}");
        }
        return sb.append("]}").toString();
    }

    // Measures how many keys this computer can test per second (all cores), once
    private static volatile double benchRate = 0;

    private static double keysPerSecond() {
        if (benchRate > 0)
            return benchRate;
        String sample = "meet at the bridge night";
        long t0 = System.nanoTime(), n = 0;
        while (System.nanoTime() - t0 < 200_000_000L) {
            smartScore(decrypt(sample, 4, 6, "3142", "415263"));
            n++;
        }
        double perCore = n / ((System.nanoTime() - t0) / 1e9);
        benchRate = perCore * Runtime.getRuntime().availableProcessors();
        return benchRate;
    }

    // Security analysis of a grid size: key space, entropy, time to exhaust it
    private static String analyzeJson(int rows, int cols) {
        long rp = factorial(rows), cp = factorial(cols), total = rp * cp;
        double bits = Math.log(total) / Math.log(2);
        double rate = keysPerSecond();
        String verdict = bits < 32 ? "Trivial" : bits < 48 ? "Weak" : bits < 64 ? "Moderate" : "Strong";
        java.util.Locale L = java.util.Locale.ROOT;
        return "{\"rows\":" + rows + ",\"cols\":" + cols + ",\"rowPerms\":" + rp + ",\"colPerms\":" + cp
                + ",\"total\":" + total + ",\"bits\":" + String.format(L, "%.2f", bits) + ",\"aesBits\":128"
                + ",\"verdict\":" + q(verdict) + ",\"rate\":" + Math.round(rate) + ",\"scenarios\":["
                + "{\"name\":\"This computer (measured)\",\"seconds\":" + String.format(L, "%.6e", total / rate) + "},"
                + "{\"name\":\"One GPU (about 1 billion keys/s)\",\"seconds\":" + String.format(L, "%.6e", total / 1e9) + "},"
                + "{\"name\":\"Large cluster (about 1 trillion keys/s)\",\"seconds\":" + String.format(L, "%.6e", total / 1e12) + "}]}";
    }

    // ---- tiny JSON helpers ----
    private static String q(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
            }
        }
        return sb.append("\"").toString();
    }

    private static int intParam(Map<String, String> p, String name) {
        try {
            return Integer.parseInt(p.getOrDefault(name, "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + name + "' must be a whole number.");
        }
    }

    private static int sizeParam(Map<String, String> p, String name) {
        int v = intParam(p, name);
        if (v < 1 || v > MAX_SIZE)
            throw new IllegalArgumentException("Rows and columns must be between 1 and " + MAX_SIZE + ".");
        return v;
    }

    private static String need(Map<String, String> p, String name) {
        String v = p.getOrDefault(name, "");
        if (v.trim().isEmpty())
            throw new IllegalArgumentException("Please fill in the " + name + ".");
        return v;
    }

    // ---- API routes: each one calls the original methods of this class ----
    private static String handleApi(String path, Map<String, String> p) {
        switch (path) {
            case "/api/encrypt": {
                int rows = sizeParam(p, "rows"), cols = sizeParam(p, "cols");
                String rk = p.getOrDefault("rowKey", "").trim(), ck = p.getOrDefault("colKey", "").trim();
                String err = checkKey(rk, rows, "Row key");
                if (err == null) err = checkKey(ck, cols, "Column key");
                if (err != null) throw new IllegalArgumentException(err);
                String pt = need(p, "plaintext");
                String[] st = encryptStages(pt, rows, cols, rk, ck);
                return "{\"result\":" + q(encrypt(pt, rows, cols, rk, ck)) + ",\"fill\":" + q(st[0])
                        + ",\"afterRows\":" + q(st[1]) + ",\"afterCols\":" + q(st[2])
                        + ",\"truncated\":" + (pt.length() > rows * cols) + "}";
            }
            case "/api/decrypt": {
                int rows = sizeParam(p, "rows"), cols = sizeParam(p, "cols");
                String rk = p.getOrDefault("rowKey", "").trim(), ck = p.getOrDefault("colKey", "").trim();
                String err = checkKey(rk, rows, "Row key");
                if (err == null) err = checkKey(ck, cols, "Column key");
                if (err != null) throw new IllegalArgumentException(err);
                return "{\"result\":" + q(decrypt(need(p, "ciphertext"), rows, cols, rk, ck)) + "}";
            }
            case "/api/hack": { // Task #2 - known dimensions (runs in background)
                int rows = sizeParam(p, "rows"), cols = sizeParam(p, "cols");
                String ct = need(p, "ciphertext");
                long combos = factorial(rows) * factorial(cols);
                if (combos > MAX_COMBOS)
                    throw new IllegalArgumentException(rows + " x " + cols + " has " + formatNumber(combos)
                            + " key combinations, which is too many (limit " + formatNumber(MAX_COMBOS) + ").");
                java.util.List<int[]> one = new ArrayList<>();
                one.add(new int[] { rows, cols });
                return "{\"id\":" + q(startJob(ct, one, "[]")) + "}";
            }
            case "/api/hack-auto": { // Task #3 - unknown dimensions (runs in background)
                String ct = need(p, "ciphertext");
                int length = ct.length();
                java.util.List<int[]> keep = new ArrayList<>();
                StringBuilder log = new StringBuilder("[");
                boolean first = true;
                for (int r = 1; r <= length; r++) {
                    if (length % r != 0) continue;
                    int c = length / r;
                    if (r > c) continue;
                    String status = "keep", note;
                    if (r == 1 || c == 1) { status = "skip"; note = "degenerate grid"; }
                    else if (r > MAX_SIZE || c > MAX_SIZE) { status = "skip"; note = "dimensions too large"; }
                    else {
                        long combos = factorial(r) * factorial(c);
                        if (combos > MAX_COMBOS) { status = "skip"; note = formatNumber(combos) + " combinations (too many)"; }
                        else { note = formatNumber(combos) + " combinations"; keep.add(new int[] { r, c }); }
                    }
                    if (!first) log.append(",");
                    first = false;
                    log.append("{\"rows\":").append(r).append(",\"cols\":").append(c)
                            .append(",\"status\":").append(q(status)).append(",\"note\":").append(q(note)).append("}");
                }
                log.append("]");
                if (keep.isEmpty())
                    throw new IllegalArgumentException("No searchable grid sizes for a ciphertext of length " + length + ".");
                keep.sort((a, b) -> Long.compare(factorial(a[0]) * factorial(a[1]), factorial(b[0]) * factorial(b[1])));
                return "{\"id\":" + q(startJob(ct, keep, log.toString())) + "}";
            }
            case "/api/job": { // live progress + best results so far
                Job job = JOBS.get(p.getOrDefault("id", ""));
                if (job == null) throw new IllegalArgumentException("Unknown or expired search.");
                return jobJson(job);
            }
            case "/api/cancel": {
                Job job = JOBS.get(p.getOrDefault("id", ""));
                if (job != null) job.cancelled = true;
                return "{\"ok\":true}";
            }
            case "/api/analyze": { // security analysis of a grid size
                return analyzeJson(sizeParam(p, "rows"), sizeParam(p, "cols"));
            }
            default:
                return null;
        }
    }

    private static void send(com.sun.net.httpserver.HttpExchange ex, int code, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    // Starts the web server on http://localhost:<port>
    public static com.sun.net.httpserver.HttpServer startWebServer(int port) {
        java.nio.file.Path page = java.nio.file.Paths.get("web", "index.html");
        if (!java.nio.file.Files.exists(page)) {
            System.out.println("Cannot find web/index.html. Run this command from the project folder.");
            return null;
        }
        try {
            com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer
                    .create(System.getenv("PORT") != null ? new java.net.InetSocketAddress(port) : new java.net.InetSocketAddress("127.0.0.1", port), 0);
            server.createContext("/", ex -> {
                try {
                    String path = ex.getRequestURI().getPath();
                    if (ex.getRequestMethod().equals("GET") && (path.equals("/") || path.equals("/index.html"))) {
                        send(ex, 200, "text/html; charset=utf-8", java.nio.file.Files.readAllBytes(page));
                    } else if (ex.getRequestMethod().equals("POST") && path.startsWith("/api/")) {
                        String body = new String(ex.getRequestBody().readNBytes(20000), java.nio.charset.StandardCharsets.UTF_8);
                        Map<String, String> params = new HashMap<>();
                        for (String pair : body.split("&")) {
                            if (pair.isEmpty()) continue;
                            int i = pair.indexOf('=');
                            String k = java.net.URLDecoder.decode(i < 0 ? pair : pair.substring(0, i),
                                    java.nio.charset.StandardCharsets.UTF_8);
                            String v = i < 0 ? "" : java.net.URLDecoder.decode(pair.substring(i + 1),
                                    java.nio.charset.StandardCharsets.UTF_8);
                            params.put(k, v);
                        }
                        String json;
                        int code = 200;
                        try {
                            json = handleApi(path, params);
                            if (json == null) { code = 404; json = "{\"error\":\"Unknown endpoint\"}"; }
                        } catch (IllegalArgumentException e) {
                            code = 400;
                            json = "{\"error\":" + q(e.getMessage()) + "}";
                        }
                        send(ex, code, "application/json; charset=utf-8", json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    } else {
                        send(ex, 404, "text/plain", "Not found".getBytes());
                    }
                } catch (Exception e) {
                    send(ex, 500, "application/json", ("{\"error\":" + q(String.valueOf(e)) + "}").getBytes());
                }
            });
            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
            server.start();
            String url = "http://localhost:" + port;
            System.out.println("\nWeb interface running at " + url);
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                    Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception ignored) {
                // If the browser does not open automatically, open the link above manually.
            }
            return server;
        } catch (IOException e) {
            System.out.println("Could not start the server on port " + port + ": " + e.getMessage());
            return null;
        }
    }

    // MAIN MENU
    public static void main(String[] args) {
        // Quick start:  java DoubleTransposition web [port]
        if (args.length > 0 && args[0].equalsIgnoreCase("web")) {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : (System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080);
            if (startWebServer(port) != null)
                System.out.println("Press Ctrl+C to stop the server.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("       DOUBLE TRANSPOSITION SYSTEM");
        System.out.println("=".repeat(60));

        while (true) {
            System.out.println("\n" + "-".repeat(40));
            System.out.println("MAIN MENU");
            System.out.println("-".repeat(40));
            System.out.println("1. Encryption");
            System.out.println("2. Decryption");
            System.out.println("3. Hacking Task #2 (known dimensions)");
            System.out.println("4. Hacking Task #3 (unknown dimensions)");
            System.out.println("5. Exit");
            System.out.println("6. Start Web Interface (opens in your browser)");
            System.out.println("-".repeat(40));
            System.out.print("Choose (1-6): ");

            int mode;
            try {
                mode = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }
            scanner.nextLine();

            if (mode == 1) {
                System.out.print("Enter plaintext: ");
                String plaintext = scanner.nextLine();
                System.out.print("Number of rows: ");
                int rows = scanner.nextInt();
                System.out.print("Number of columns: ");
                int cols = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Row order: ");
                String rowKey = scanner.nextLine();
                System.out.print("Column order: ");
                String colKey = scanner.nextLine();

                String encrypted = encrypt(plaintext, rows, cols, rowKey, colKey);
                System.out.println("\nEncrypted: \"" + encrypted + "\"");

            } else if (mode == 2) {
                System.out.print("Enter ciphertext: ");
                String ciphertext = scanner.nextLine();
                System.out.print("Number of rows: ");
                int rows = scanner.nextInt();
                System.out.print("Number of columns: ");
                int cols = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Row key: ");
                String rowKey = scanner.nextLine();
                System.out.print("Column key: ");
                String colKey = scanner.nextLine();

                decryptWithKey(ciphertext, rows, cols, rowKey, colKey);

            } else if (mode == 3) {
                System.out.print("Enter ciphertext to hack: ");
                String ciphertext = scanner.nextLine();

                if (ciphertext == null || ciphertext.trim().isEmpty()) {
                    System.out.println("Error: Ciphertext cannot be empty!");
                    continue;
                }

                System.out.print("Enter number of rows: ");
                int rows = scanner.nextInt();
                System.out.print("Enter number of columns: ");
                int cols = scanner.nextInt();
                scanner.nextLine();

                System.out.println("\nUsing dimensions: " + rows + " x " + cols);
                System.out.println("Starting brute force attack...");
                hackKnownDimensions(ciphertext, rows, cols);

            } else if (mode == 4) {
                System.out.print("Enter ciphertext to hack: ");
                String ciphertext = scanner.nextLine();

                if (ciphertext == null || ciphertext.trim().isEmpty()) {
                    System.out.println("Error: Ciphertext cannot be empty!");
                    continue;
                }

                System.out.println("\nStarting brute force attack...");
                hackUnknownDimensions(ciphertext);

            } else if (mode == 5) {
                System.out.println("\nThank you!");
                break;

            } else if (mode == 6) {
                com.sun.net.httpserver.HttpServer server = startWebServer(8080);
                if (server != null) {
                    System.out.println("Press ENTER to stop the server and return to the menu...");
                    scanner.nextLine();
                    server.stop(0);
                    System.out.println("Server stopped.");
                }

            } else {
                System.out.println("Invalid choice. Please enter 1-6");
            }
        }
        scanner.close();
    }
}