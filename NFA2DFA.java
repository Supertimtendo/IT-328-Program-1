import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

public class NFA2DFA {

    private static final String INPUTS = "-- Input strings for testing -----------";

    Map<Character, Integer> Alpha2Int = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        // File input
        // No input file
        if (args.length == 0) {
            System.out.println("Need to specify input file");
            System.exit(1);
        }
        NFA2DFA obj = new NFA2DFA();
        // TODO: Input file validation
        // TODO: Fix hardcoded file
        //File input = obj.fileInput(args[0]);
        File input = obj.fileInput("Data Files/C.nfa");
        // TEST DATA
        //int states[][] = obj.dfaGetter(input);

        NFA nfa = readNFAFromFile("Data Files/C.nfa");
        //TODO: Uncomment this
        //printNFA(nfa);

        DFA dfa = convertNFAtoDFA(nfa);
        dfa.printDFA();

        //Part C
        boolean outputs[] = obj.stringParser(input, dfa);
        obj.inputPrinter(outputs, input);
    }

    /**
     * Checks if file name exists
     * 
     * @param name Name of file
     * @return Returns file
     */
    private File fileInput(String name) {
        File input = new File(name);
        if (!input.isFile()) {
            System.out.println("File does not exist");
            System.exit(2);
        }
        return input;
    }

    /**
     * Test method to get 2d array of states from dfa file
     * Only used if DFA cannot be produced from NFA
     * @param dfa DFA file
     * @return Returns 2D array of states
     * @throws FileNotFoundException
     */
    private int[][] dfaGetter(File dfa) throws FileNotFoundException {
        Scanner scan = new Scanner(dfa);
        // Get number of states
        String line = scan.nextLine();
        int numStates = 0;
        char[] chars = line.toCharArray();
        for (char c : chars) {
            if (Character.isDigit(c)) {
                numStates = Character.getNumericValue(c);
                break;
            }
        }
        line = scan.nextLine();
        // Stores alphabet
        String alpha[] = line.split("     ");
        // Put corresponding chars as values in map
        for (int i = 1; i < alpha.length; i++) {
            Alpha2Int.put(alpha[i].charAt(0), i - 1);
        }
        // Remove ---- line
        scan.nextLine();
        // Number of alphabets
        int numAlpha = alpha.length - 1;
        int[][] states = new int[numStates][numAlpha];
        for (int i = 0; i < numStates; i++) {
            line = scan.nextLine();
            String n[] = line.split("     ");

            for (int j = 1; j <= numAlpha; j++) {
                states[i][j - 1] = Integer.parseInt(n[j]);
            }
        }
        return states;
    }



    /**
     * Parses inputs in file
     * @param inputFile File
     * @return Returns boolean array of results to inputs
     */
    private boolean[] stringParser(File inputFile, DFA dfa) throws FileNotFoundException {
        Scanner scan = new Scanner(inputFile);
        // TODO: Replace this if input strings ALWAYS start at same line number
        while (scan.hasNextLine()) {
            // If start of input is found
            if (scan.nextLine().equals(INPUTS)) {
                break;
            }
        }
        boolean[] outputs = new boolean[30];
        // Read all 30 inputs, tests each one, inputs it into output array
        for (int i = 0; i < 30; i++) {
            String in = scan.nextLine();
            outputs[i] = inputTester(in, dfa);
        }
        return outputs;
    }

    /**
     * Tests a given string against the DFA
     * 
     * @param input Input string
     * @return Returns T/F if passed or not
     */
    private boolean inputTester(String input, DFA dfa) {
        // Get each individual input letter
        char[] letters = input.toCharArray();
        int currentState = dfa.initialState;
        // TODO: Placeholder location, holds the accepting states in int form

        for (int i = 0; i < letters.length; i++) {
            //int letterNo = Alpha2Int.get(letters[i]);
            int letterNo = dfa.alphabet.indexOf(String.valueOf(letters[i]));
            List<Integer> test = dfa.transitionTable.get(currentState);
            currentState = test.get(letterNo);
            //currentState = states[currentState][letterNo];
        }

        // Checks last state against the list of accepting states
        boolean accepted = false;
        for (int i = 0; i < dfa.acceptingStates.size(); i++) {
            if (dfa.acceptingStates.get(i) == currentState) {
                accepted = true;
                break;
            }
        }

        return accepted;
    }

    /**
     * Prints output of inputs as according to assignment format
     * 
     * @param outputs   Output array from inputs
     * @param inputFile Input file to get name from
     */
    private void inputPrinter(boolean[] outputs, File inputFile) {
        System.out.println("Parsing results of strings attached in " + inputFile.getName() + "\n");
        int numYes = 0;
        int numNo = 0;
        for (int i = 0; i < 30; i++) {
            String result;
            // Convert boolean to String, increment number of Yes/No respectively
            if (outputs[i]) {
                result = "Yes";
                numYes++;
            } else {
                result = "No";
                numNo++;
            }

            // Formatting output according to assignment
            if (i != 29) {
                if (i == 14) {
                    System.out.print(result + "\n");
                } else {
                    System.out.print(result + " ");
                }
            }

            else {
                System.out.print(result + "\n");
            }
        }
        System.out.print("Yes: " + numYes + " No: " + numNo);
    }

    // minimizeDFA(dfa, initialState, acceptingStates, sigma, args[1])
    private Map<Set<Integer>, Map<String, Set<Integer>>> minimizeDFA(
            Map<Set<Integer>, Map<String, Set<Integer>>> origDFA, int initialState, String[] acceptingStates,
            String[] sigma, String args) {
        // Initialize stuff for minimization
        int numStates = origDFA.size();
        Map<Set<Integer>, Map<String, Set<Integer>>> minDFA = new HashMap<>();
        int[][] matrix = new int[numStates][numStates];

        // Determine initial distinguishable states
        // Based on initial state, any accepting state is distinguishable
        for (int i = 0; i < acceptingStates.length; i++) {
            int acceptingState = Integer.parseInt(acceptingStates[i]);
            if (initialState == acceptingState) {
                for (int j = 0; j < numStates; j++) { // Set all non accepting states to 1
                    if (j != acceptingState) { // If j is not an accepting state, it must be distinguishable from the
                                               // initial state
                        matrix[0][j] = 1;
                        matrix[j][0] = 1;
                    }
                }
            } else { // Initial state is not an accepting state
                for (int j = 0; j < acceptingStates.length; j++) { // Set all accepting states to 1 because they're
                                                                   // distinguishable
                    int accepting = Integer.parseInt(acceptingStates[j]);
                    matrix[0][accepting] = 1;
                    matrix[accepting][0] = 1;
                }
            }
        }

        // loop through indices to determine rest of matrix
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < numStates; i++) {
                for (int j = 0; j < numStates; j++) {
                    // If matrix[i][j] == 1, check each column for i or j
                    if (matrix[i][j] == 1) {
                        // Check each map's sigma at k (inputs)
                        for (String symbol : sigma) {
                            int nextI = origDFA.get(origDFA.keySet().toArray()[i]).get(symbol).iterator().next();
                            int nextJ = origDFA.get(origDFA.keySet().toArray()[j]).get(symbol).iterator().next();
                            if (matrix[nextI][nextJ] == 0 || matrix[nextJ][nextI] == 0) {
                                matrix[i][j] = 0;
                                matrix[j][i] = 0;
                                changed = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Build the new map based on the matrix
        for (int i = 0; i < numStates; i++) {
            for (int j = i + 1; j < numStates; j++) {
                if (matrix[i][j] == 0) {
                    Set<Integer> stateI = (Set<Integer>) origDFA.keySet().toArray()[i];
                    Set<Integer> stateJ = (Set<Integer>) origDFA.keySet().toArray()[j];
                    Map<String, Set<Integer>> transitions = new HashMap<>();

                    for (String symbol : sigma) {
                        int nextStateI = origDFA.get(stateI).get(symbol).iterator().next();
                        int nextStateJ = origDFA.get(stateJ).get(symbol).iterator().next();

                        // Find the index of nextStateI in the original DFA
                        int nextStateIndexI = -1;
                        int nextStateIndexJ = -1;

                        for (int k = 0; k < numStates; k++) {
                            Set<Integer> currentState = (Set<Integer>) origDFA.keySet().toArray()[k];
                            if (currentState.equals(nextStateI)) {
                                nextStateIndexI = k;
                            } else if (currentState.equals(nextStateJ)) {
                                nextStateIndexJ = k;
                            }
                        }

                        Set<Integer> nextStateSetI = (Set<Integer>) origDFA.keySet().toArray()[nextStateIndexI];
                        Set<Integer> nextStateSetJ = (Set<Integer>) origDFA.keySet().toArray()[nextStateIndexJ];

                        if (matrix[nextStateIndexI][nextStateIndexJ] == 0) {
                            transitions.put(symbol, nextStateSetI);
                        } else {
                            transitions.put(symbol, nextStateSetJ);
                        }
                    }
                    minDFA.put(stateI, transitions);
                }
            }
        }

        // Print output
        System.out.println("Minimized DFA from " + args + ":");
        System.out.print(" Sigma: ");
        for (int i = 0; i < sigma.length; i++) {
            System.out.print(sigma[i] + " ");
        }
        System.out.println("");
        System.out.println(" ------------------------------");

        // Print minimized DFA
        for (Set<Integer> state : minDFA.keySet()) {
            int currentStateIndex = 0;
            for (int i = 0; i < minDFA.keySet().size(); i++) {
                if (minDFA.keySet().toArray()[i].equals(state)) {
                    currentStateIndex = i;
                    break;
                }
            }

            System.out.print(currentStateIndex + ": ");
            for (String s : sigma) {
                Set<Integer> nextStateSet = minDFA.get(state).get(s);
                int nextStateIndex = 0;
                for (int i = 0; i < minDFA.keySet().size(); i++) {
                    if (minDFA.keySet().toArray()[i].equals(nextStateSet)) {
                        nextStateIndex = i;
                        break;
                    }
                }
                System.out.print(nextStateIndex + " ");
            }
            System.out.println();
        }

        System.out.println(" ------------------------------");
        System.out.println(initialState + ":  Initial State");
        for (int i = 0; i < acceptingStates.length - 1; i++) {
            System.out.print(acceptingStates[i] + ", ");
        }
        System.out.println(": Accepting State(s)");
        System.out.println(":  Initial State");

        return minDFA;
    }

    static class DFA {
        int numStates;
        List<String> alphabet = new ArrayList<>();
        List<List<Integer>> transitionTable = new ArrayList<>();
        int initialState;
        List<Integer> acceptingStates = new ArrayList<>();

        public void printDFA() {
            System.out.println("|Q|: " + numStates);
            System.out.print("Sigma: ");

            for (String symbol : alphabet) {
                System.out.print("    " + symbol);
            }

            System.out.println("\n------------------------------");

            for (int i = 0; i < numStates; ++i) {
                System.out.print("    " + i + ":");

                for (int transition : transitionTable.get(i)) {
                    System.out.print("    " + transition);
                }

                System.out.println();
            }

            System.out.println("------------------------------");
            System.out.println("Initial State: " + initialState);
            System.out.print("Accepting State(s): ");

            Iterator<Integer> iterator = acceptingStates.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                System.out.print(iterator.next());

                if (i < acceptingStates.size() - 1) {
                    System.out.print(",");
                }
                i++;
            }

            System.out.println("\n");
        }

    }

    // Data structure to hold NFA
    static class NFA {
        int numStates;
        List<String> alphabet = new ArrayList<>();
        List<List<List<Integer>>> transitionTable = new ArrayList<>();
        int initialState;
        List<String> inputStrings = new ArrayList<>();;
        List<Integer> acceptingStates = new ArrayList<>();

    }

    public static NFA readNFAFromFile(String filename) {
        NFA nfa = new NFA();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            // Parse number of states
            line = br.readLine();
            nfa.numStates = Integer.parseInt(line.split(":")[1].trim());

            // Parse alphabet
            line = br.readLine();
            nfa.alphabet = Arrays.asList(line.split(":")[1].trim().split("\\s+"));

            // Skip the dashed line
            br.readLine();

            // Parse transition table
            for (int i = 0; i < nfa.numStates; i++) {
                line = br.readLine();
                String[] parts = line.split(":");
                String[] transitions = parts[1].trim().split("\\s+");
                List<List<Integer>> stateTransitions = new ArrayList<>();

                for (int j = 0; j < transitions.length; j++) {
                    List<Integer> targets = new ArrayList<>();
                    // Handle lambda transitions
                    if (j == transitions.length - 1) { // This assumes lambda transition is last
                        if (!transitions[j].equals("{}")) { // Check if lambda transitions are not empty
                            // Add lambda transitions without adding the state itself
                            for (String target : transitions[j].substring(1, transitions[j].length() - 1).split(",")) {
                                targets.add(Integer.parseInt(target));
                            }
                        }
                    } else {
                        // Regular transitions
                        for (String target : transitions[j].substring(1, transitions[j].length() - 1).split(",")) {
                            if (!target.isEmpty()) {
                                targets.add(Integer.parseInt(target));
                            }
                        }
                    }
                    stateTransitions.add(targets);
                }
                nfa.transitionTable.add(stateTransitions);
            }

            // Parse initial state
            line = br.readLine(); // skip over the dashes
            line = br.readLine();
            nfa.initialState = Integer.parseInt(line.split(":")[1].trim());

            // Parse accepting states
            line = br.readLine();
            for (String state : line.split(":")[1].trim().split(",")) {
                nfa.acceptingStates.add(Integer.parseInt(state));
            }

            // Read the dashed line and the comment (skip them)
            br.readLine(); // skip dashed line
            br.readLine(); // skip comment

            // Parse input strings for testing until end of file
            List<String> inputs = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                inputs.add(line.trim());
            }
            nfa.inputStrings = inputs;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return nfa;
    }

    public static DFA convertNFAtoDFA(NFA nfa) {
        DFA dfa = new DFA();
        dfa.alphabet = nfa.alphabet.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList()); // filter out the
                                                                                                     // empty string
                                                                                                     // (lambda)

        // Map to keep track of new DFA states to NFA state sets
        Map<Set<Integer>, Integer> stateMap = new HashMap<>();

        // Start with the lambda closure of the initial state of the NFA
        Set<Integer> startSet = lambdaClosure(nfa, new HashSet<>(Collections.singleton(nfa.initialState)));
        stateMap.put(startSet, 0);

        // Queue to keep track of new DFA states to be processed
        Queue<Set<Integer>> queue = new LinkedList<>();
        queue.add(startSet);

        // Initialize transition table for DFA
        while (!queue.isEmpty()) {
            Set<Integer> currentStateSet = queue.poll();
            List<Integer> dfaTransitions = new ArrayList<>(Collections.nCopies(dfa.alphabet.size(), 0));

            for (int i = 0; i < dfa.alphabet.size(); i++) {
                String symbol = dfa.alphabet.get(i);
                Set<Integer> nextStates = new HashSet<>();
                for (int state : currentStateSet) {
                    int symbolIndex = nfa.alphabet.indexOf(symbol);
                    nextStates.addAll(nfa.transitionTable.get(state).get(symbolIndex));
                }

                // Compute lambda closure for the set of next states
                Set<Integer> nextStatesClosure = lambdaClosure(nfa, nextStates);

                if (!stateMap.containsKey(nextStatesClosure)) {
                    stateMap.put(nextStatesClosure, stateMap.size());
                    queue.add(nextStatesClosure);
                }

                dfaTransitions.set(i, stateMap.get(nextStatesClosure));
            }

            dfa.transitionTable.add(dfaTransitions);
        }

        // Set the number of DFA states, initial state, and accepting states
        dfa.numStates = stateMap.size();
        dfa.initialState = stateMap.get(startSet);

        // Every set of NFA states that contains at least one accepting state becomes an
        // accepting state in the DFA
        for (Set<Integer> stateSet : stateMap.keySet()) {
            for (int state : stateSet) {
                if (nfa.acceptingStates.contains(state)) {
                    dfa.acceptingStates.add(stateMap.get(stateSet));
                    break;
                }
            }
        }

        return dfa;
    }

    public static Set<Integer> lambdaClosure(NFA nfa, Set<Integer> stateSet) {
        Set<Integer> closure = new HashSet<>(stateSet);
        Queue<Integer> queue = new LinkedList<>(stateSet);
        int lambdaIndex = nfa.alphabet.indexOf(""); // Attempt to find the index of lambda (empty string)
        if (lambdaIndex == -1) {
            // If lambda transitions are not explicitly represented in the alphabet, you
            // might need to handle them separately.
            // For this example, let's assume that lambda transitions are always the last in
            // the transition lists.
            lambdaIndex = nfa.transitionTable.get(0).size() - 1; // Assuming the lambda transitions are the last
        }
        while (!queue.isEmpty()) {
            int state = queue.poll();
            List<Integer> lambdas = nfa.transitionTable.get(state).get(lambdaIndex); // Access the lambda transitions
            for (int nextState : lambdas) {
                if (closure.add(nextState)) {
                    queue.add(nextState);
                }
            }
        }
        return closure;
    }

    public static void printNFA(NFA nfa) {
        System.out.println("Number of states: " + nfa.numStates);
        System.out.println("Alphabet: " + nfa.alphabet);
        System.out.println("Transition Table: " + nfa.transitionTable);
        System.out.println("Initial State: " + nfa.initialState);
        System.out.println("Accepting States: " + nfa.acceptingStates);
        // System.out.println("Input String for Testing: " + nfa.inputString);
    }

    public static void printDFA(DFA dfa) {
        System.out.println("DFA");
        System.out.println("Number of states: " + dfa.numStates);
        System.out.println("Alphabet: " + dfa.alphabet);
        System.out.println("Transition Table: " + dfa.transitionTable);
        System.out.println("Initial State: " + dfa.initialState);
        System.out.println("Accepting States: " + dfa.acceptingStates);
    }

}