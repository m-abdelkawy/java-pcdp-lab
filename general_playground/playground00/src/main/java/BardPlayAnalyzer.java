import java.text.BreakIterator;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This program implements a sequential app that uses the {@link
 * Array} class to count the number of lines in each of Shakespeare's
 * plays.  This program also demonstrates the use of Java
 * object-oriented features (such as classes and interfaces) and
 * functional programming features (such as lambda expressions, method
 * references, and functional interfaces).
 */
public class BardPlayAnalyzer
    implements Runnable {
    /**
     * A {@link Map} that associates the titles (key) of Shakespeare
     * plays with their content (value).
     */
    private static final Map<String, String> mBardMap = FileUtils
        .loadMapFromFolder("plays", ".txt");

    /**
     * This is the main entry point into the program.
     */
    static public void main(String[] args) {
        System.out.println("Starting Sequential BardPlayAnalyzer");

        // Run the line analysis sequentially.
        new BardPlayAnalyzer().run();

        System.out.println("Ending Sequential BardPlayAnalyzer");
    }

    /**
     * Run the line counting analysis sequentially.
     */
    @Override
    public void run() {
        // Run the analysis sequentially and print the results.
        var results = runAndReturnResults();

        // Sort and print the results.
        printResults(results);
    }

    /**
     * Run the line count analysis and return an {@link Array} of results.
     *
     * @return An {@link Array} of results
     */
    public Array<String> runAndReturnResults() {
        // Create an empty Array.
        Array<String> results = new Array<>();

        // Process each play sequentially and update the Array of
        // results.
        mBardMap
            // Convert the Map into a Set.
            .entrySet()

            // Process each Bard play in the Map and add the
            // output to the Array of results,
            .forEach(entry ->
                results.add(processInput(entry)));

        // Return the results;
        return results;
    }

    /**
     * This method processes a Bard play to count the number
     * of lines it contains.
     *
     * @param entry The Bard {@link Map.Entry} to process
     * @return A {@link String} indicating how many lines are
     *         in the Bard play
     */
    private String processInput(Map.Entry<String, String> entry) {
        // Get the play's title.
        var play = entry.getKey();

        // Get the play's contents.
        var contents = entry.getValue();

        // Return the formatted results of line count for this play.
        return countLines(contents)
            + " is the number of lines in "
            + entry.getKey();
    }

    /**
     * Count the number of lines in the text.
     *
     * @param text The text of a Bard play to count the lines in
     * @return A count of the number of lines in the text
     */
    private int countLines(String text) {
        // Create a BreakIterator that splits the text by lines.
        BreakIterator iterator = BreakIterator
            .getLineInstance();

        // Set the text to be split.
        iterator.setText(text);

        int linecount = 0;

        // Iterate through the text and count the lines.
        for (int start = iterator.first(),
             end = iterator.next();
             end != BreakIterator.DONE;
             end = iterator.next()) {
            if (text.charAt(end - 1) == '\n')
                linecount++;
            start = end;
        }

        // Return the line count.
        return linecount;
    }

    /**
     * Print the results of the line counting analysis sorted by line
     * count.
     *
     * @param results An {@link Array} of results to print
     */
    private void printResults(Array<String> results) {
        // Convert the Array into a List.
        List<String> list = results.asList();

        // Sort the results.
        list.sort(Collections.reverseOrder());

        list
            // Print the sorted results of the calculation.
            .forEach(BardPlayAnalyzer::display);
    }

    /**
     * Display the {@link String} to the output.
     *
     * @param string The {@link String} to display
     */
    private static void display(String string) {
        System.out.println("["
            + Thread.currentThread().threadId()
            + "] "
            + string);
    }
}

