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
        File input = obj.fileInput(args[0]);
        NFA nfa = readNFAFromFile(args[0]);
        //printNFA(nfa);
        String fileNameRaw = input.getName().replaceFirst("[.][^.]+$", "");
        System.out.println("NFA "+input.getName()+" to DFA "+fileNameRaw+".dfa:");
        DFA dfa = convertNFAtoDFA(nfa);
        dfa.printDFA();
        //Part C for 1st DFA
        boolean outputs[] = obj.stringParser(input, dfa);
        obj.inputPrinter(outputs, input);

        //Part B
        minDFA min = minimizeDFA(dfa);

        System.out.println("Minimized DFA from "+fileNameRaw+".dfa:");
        //Part C for minimized DFA
        minDFA mDFA = minimizeDFA(dfa);
        mDFA.printMinDFA();
        outputs = obj.stringParser(input, dfa);
        obj.inputPrinter(outputs, input);

        System.out.println("[Q] "+dfa.numStates+"-> "+min.numStates);
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
     * @param input Input string
     * @return Returns T/F if passed or not
     */
    private boolean inputTester(String input, DFA dfa) {
        // Get each individual input letter
        char[] letters = input.toCharArray();
        int currentState = dfa.initialState;

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
        System.out.println("Parsing results of strings attached in " + inputFile.getName() + ":");
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
        System.out.println("\nYes: " + numYes + " No: " + numNo+"\n");
    }

    static class minDFA{
        int numStates;
        List<String> alphabet = new ArrayList<>();
        List<List<Integer>> transitionTable = new ArrayList<>();
        int initialState;
        List<Integer> acceptingStates = new ArrayList<>();
        int[][] matrix;

        //Print Minimized DFA
        public void printMinDFA(){
            System.out.println("|Q|: " + numStates);
            System.out.print("Sigma:");
            for (String input : alphabet){
                System.out.print("     " + input);
            }
            System.out.println("\n------------------------------");
            for (int i = 0; i < numStates; ++i) {
                if(i>9){
                    System.out.print("   " + i + ":");
                }
                else {
                    System.out.print("    " + i + ":");
                }
                for (int transition : transitionTable.get(i)) {
                    if(transition>9) {
                        System.out.print("    " + transition);
                    }
                    else{
                        System.out.print("     " + transition);
                    }
                }
                System.out.println();
            }

            System.out.println("------------------------------");
            System.out.println(initialState + ": Initial State");
            Iterator<Integer> iterator = acceptingStates.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                System.out.print(iterator.next());
                if (i < acceptingStates.size() - 1) {
                    System.out.print(",");
                }
                i++;
            }
            System.out.print(": Accepting State(s)\n");
        }
    }

    public static minDFA minimizeDFA (DFA dfa){
        minDFA mDFA = new minDFA();
        // Input all data from DFA
        mDFA.initialState = dfa.initialState;
        mDFA.alphabet = dfa.alphabet;

        // Set up matrix
        // 0 = indistinguishable
        // 1 = distinguishable (default)
        // -1 = unknown
        mDFA.matrix = new int[dfa.numStates][dfa.numStates];
        for (int i = 0; i < dfa.numStates; i++){
            for (int j = 0; j < dfa.numStates; j++){
                mDFA.matrix[i][j] = -1;
                if (i == j){
                    mDFA.matrix[i][j] = 0;
                }
            }
        }

        //If two states have the exact same transitions, they are indistinguishable.
        for (int i = 0; i < dfa.numStates; i++) {
            for (int j = 0; j < dfa.numStates; j++) {
                boolean areSameTransitions = true;
        
                for (int alpha = 0; alpha < dfa.alphabet.size(); alpha++) {
                    int transitionI = dfa.transitionTable.get(i).get(alpha);
                    int transitionJ = dfa.transitionTable.get(j).get(alpha);
        
                    if (transitionI != transitionJ) {
                        areSameTransitions = false;
                        break;
                    }
                }
        
                if (i == j || areSameTransitions) {
                    mDFA.matrix[i][j] = 0;  // States are the same or have the same transitions, mark them as indistinguishable
                    mDFA.matrix[j][i] = 0;
                } else {
                    mDFA.matrix[i][j] = -1;  // Default to placeholder
                    mDFA.matrix[j][i] = -1;
                }
            }
        }

    // Determine initial distinguishable states
    // Set any non-accepting state as distinguishable from accepting states
    for (int i = 0; i < dfa.acceptingStates.size(); i++) {
        int acceptingState = dfa.acceptingStates.get(i);
        for (int j = 0; j < dfa.numStates; j++) {
            if (!dfa.acceptingStates.contains(j)) {
                mDFA.matrix[acceptingState][j] = 1;
                mDFA.matrix[j][acceptingState] = 1;
            }
        }
    }

        // Loop through indices to determine the rest of the matrix
        // Stops when nothing has been changed
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < dfa.numStates; i++) {
                for (int j = 0; j < dfa.numStates; j++) {
                    if (mDFA.matrix[i][j] == 1) {
                        // Find all possible nextI and nextJ states for this alpha symbol
                        // Mark all pairs as indistinguishable if they haven't been marked already
                        for (int alpha = 0; alpha < dfa.alphabet.size(); alpha++) {
                            List<Integer> nextIList = new ArrayList<>();
                            List<Integer> nextJList = new ArrayList<>();
                            for (int k = 0; k < dfa.numStates; k++) {
                                if (dfa.transitionTable.get(k).get(alpha).equals(i) && !nextIList.contains(k)) {
                                    nextIList.add(k);
                                }
                                if (dfa.transitionTable.get(k).get(alpha).equals(j) && !nextJList.contains(k)) {
                                    nextJList.add(k);
                                }
                            }
                            // Mark all pairs of [nextI][nextJ] as distinguishable if they haven't been marked already
                            for (int nextI : nextIList) {
                                for (int nextJ : nextJList) {
                                    if (mDFA.matrix[nextI][nextJ] == -1 && nextI != nextJ) {
                                        mDFA.matrix[nextI][nextJ] = 1;
                                        mDFA.matrix[nextJ][nextI] = 1;
                                        changed = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //Set all unfound states to 0
        for (int i = 0; i < dfa.numStates; i++){
            for (int j = 0; j < dfa.numStates; j++){
                if (mDFA.matrix[i][j] == -1){
                    mDFA.matrix[i][j] = 0;
                }
            }
        }

        // Create groups based on distinguishable or not
        mDFA.numStates = dfa.numStates;
        List<Set<Integer>> distinguishableGroups = new ArrayList<>();
        Set<Integer> processedStates = new HashSet<>(); // To keep track of processed states

        for (int i = 0; i < dfa.numStates; i++) {
            // If this state is already processed, skip it
            if (processedStates.contains(i)) {
                continue;
            }

            Set<Integer> group = new HashSet<>();
            group.add(i); // Add the current state to the group
            for (int j = i + 1; j < mDFA.numStates; j++) {
                if (mDFA.matrix[i][j] == 0) {
                    group.add(j); // Add indistinguishable states to the group
                    processedStates.add(j); // Mark them as processed
                }
            }
            distinguishableGroups.add(group);
        }
        mDFA.numStates = distinguishableGroups.size();

        Map<Integer, Integer> stateMap = new HashMap<>();
        int newStateId = 0;
        for (Set<Integer> group : distinguishableGroups) {
            for (int state : group) {
                stateMap.put(state, newStateId);
            }
            newStateId++;
        }

// Determine transition table
List<List<Integer>> minimizedTransitionTable = new ArrayList<>();
for (Set<Integer> group : distinguishableGroups) {
    List<Integer> transitionRow = new ArrayList<>();
    for (String input : mDFA.alphabet) {
        int originalState = group.iterator().next(); // Use the representative state
        int originalStateTransition = dfa.transitionTable.get(originalState).get(mDFA.alphabet.indexOf(input));
        int nextState = stateMap.get(originalStateTransition);
        transitionRow.add(nextState);
    }
    minimizedTransitionTable.add(transitionRow);
}
mDFA.transitionTable = minimizedTransitionTable;

        // Determine accepting states based on transition table
        Set<Integer> acceptingStates = new HashSet<>();
        for (Set<Integer> group : distinguishableGroups) {
            boolean foundAcceptingState = false;
            for (int originalAcceptingState : dfa.acceptingStates) {
                for (int stateInGroup : group) {
                    if (stateInGroup == originalAcceptingState) {
                        foundAcceptingState = true;
                        acceptingStates.add(stateMap.get(stateInGroup));
                        break;
                    }
                }
                if (foundAcceptingState) {
                    break;
                }
            }
        }
        mDFA.acceptingStates = new ArrayList<>(acceptingStates);

        return mDFA;
    }

    static class DFA {
        int numStates;
        List<String> alphabet = new ArrayList<>();
        List<List<Integer>> transitionTable = new ArrayList<>();
        int initialState;
        List<Integer> acceptingStates = new ArrayList<>();

        public void printDFA() {
            System.out.println("|Q|: " + numStates);
            System.out.print("Sigma:");

            for (String symbol : alphabet) {
                System.out.print("     " + symbol);
            }

            System.out.println("\n------------------------------");

            for (int i = 0; i < numStates; ++i) {
                if(i>9){
                    System.out.print("   " + i + ":");
                }
                else {
                    System.out.print("    " + i + ":");
                }
                for (int transition : transitionTable.get(i)) {
                    if(transition>9) {
                        System.out.print("    " + transition);
                    }
                    else{
                        System.out.print("     " + transition);
                    }
                }

                System.out.println();
            }

            System.out.println("------------------------------");
            System.out.println(initialState+": Initial State");

            Iterator<Integer> iterator = acceptingStates.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                System.out.print(iterator.next());

                if (i < acceptingStates.size() - 1) {
                    System.out.print(",");
                }
                i++;
            }
            System.out.print(": Accepting State(s)");
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