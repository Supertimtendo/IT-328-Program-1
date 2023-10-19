import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class NFA2DFA {

    private static final String INPUTS = "-- Input strings for testing -----------";

    //TODO: Placeholder variable for integration later
    private int initialState;
    //TODO: Placeholder variable for list of states
    String[] acceptingStates;

    //TODO: Placeholder map for storing states
    Map<Set<Integer>, Map<String, Set<Integer>>> dfa = new HashMap<>();
    public static void main(String[] args) {
        // File input
        // No input file
        if(args.length==0){
            System.out.println("Need to specify input file");
            System.exit(1);
        }
        NFA2DFA obj = new NFA2DFA();
        File input = obj.fileInput(args[1]);

    }

    /**
     * Checks if file name exists
     * @param name Name of file
     * @return Returns file
     */
    private File fileInput(String name){
        File input = new File(name);
        if (!input.isFile()){
            System.out.println("File does not exist");
            System.exit(2);
        }
        return input;
    }

    /**
     * Parses inputs in file
     * @param inputFile File
     * @return Returns boolean array of results to inputs
     */
    private boolean[] stringParser(File inputFile) throws FileNotFoundException {
        Scanner scan = new Scanner(inputFile);
        //TODO: Replace this if input strings ALWAYS start at same line number
        while(scan.hasNextLine()){
            //If start of input is found
            if(scan.nextLine().equals(INPUTS)){
                break;
            }
        }
        boolean[] outputs = new boolean[30];
        // Read all 30 inputs, tests each one, inputs it into output array
        for(int i=0;i<30;i++){
            String in = scan.nextLine();
            outputs[i] = inputTester(in);
        }
        return outputs;
    }

    /**
     * Tests a given string against the DFA
     * @param input Input string
     * @return Returns T/F if passed or not
     */
    private boolean inputTester(String input){
        //Get each individual input letter
        char[] letters = input.toCharArray();
        //TODO: Change to initial state
        int currentState = initialState;
        //TODO: Placeholder location, holds the accepting states in int form
        int[] acceptingStatesInt = new int [acceptingStates.length];
        for(int i=0;i<acceptingStatesInt.length;i++){
            acceptingStatesInt[i] = Integer.parseInt(acceptingStates[i]);
        }
        for(int i=0;i<letters.length;i++){
            Map next = dfa.get(currentState);
            //TODO: Fix this line probably? Points to the wrong thing
            currentState = (int) next.get(letters[i]);
        }
        // Checks last state against the list of accepting states
        boolean accepted = false;
        for(int i=0;i<acceptingStatesInt.length;i++){
            if(acceptingStatesInt[i]==currentState){
                accepted = true;
                break;
            }
        }

        return accepted;
    }

    /**
     * Prints output of inputs as according to assignment format
     * @param outputs Output array from inputs
     * @param inputFile Input file to get name from
     */
    private void inputPrinter(boolean[] outputs, File inputFile){
        System.out.println("Parsing results of strings attached in "+inputFile.getName()+"\n");
        int numYes = 0;
        int numNo = 0;
        for(int i=0;i<30;i++){
            String result;
            // Convert boolean to String, increment number of Yes/No respectively
            if(outputs[i]){
                result = "Yes";
                numYes++;
            }
            else{
                result = "No";
                numNo++;
            }

            //Formatting output according to assignment
            if(i!=29) {
                System.out.print(result + " ");
            }
            else{
                System.out.print(result+"\n");
            }
        }
        System.out.print("Yes: "+numYes+" No: "+numNo);
    }
}