import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;



public class NFA2DFA {

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

    public static void main(String[] args) {
        // Read NFA from text file
        NFA nfa = readNFAFromFile("G.nfa");

        // Print NFA to verify
         printNFA(nfa);

        // Convert NFA to DFA
        DFA dfa = convertNFAtoDFA(nfa);

        // Print DFA to verify
        // printDFA(dfa);

        dfa.printDFA();
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
    dfa.alphabet = nfa.alphabet.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList()); // filter out the empty string (lambda)

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

    // Every set of NFA states that contains at least one accepting state becomes an accepting state in the DFA
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
        // If lambda transitions are not explicitly represented in the alphabet, you might need to handle them separately.
        // For this example, let's assume that lambda transitions are always the last in the transition lists.
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
