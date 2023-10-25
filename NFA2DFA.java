import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class NFA2DFA {

    private static final String INPUTS = "-- Input strings for testing -----------";
    private static final String SIGMA = "Sigma:";
    //TODO: Hardcoded value, change
    private int initialState = 0;
    //TODO: Hardcoded value change
    int[] acceptingStates = {0,2,3,5};

    Map<Character, Integer> Alpha2Int = new HashMap<>();
    //TODO: Placeholder map for storing states
    Map<Set<Integer>, Map<String, Integer>> dfa = new HashMap<>();
    public static void main(String[] args) throws FileNotFoundException {
        // File input
        // No input file
        if(args.length==0){
            System.out.println("Need to specify input file");
            System.exit(1);
        }
        NFA2DFA obj = new NFA2DFA();
        //TODO: Input file validation
        //File input = obj.fileInput(args[0]);
        //TEST DATA
        File dfa = obj.fileInput("Data Files/X.dfa");
        int states[][] = obj.dfaGetter(dfa);
        boolean outputs[] = obj.stringParser(dfa, states);
        obj.inputPrinter(outputs,dfa);
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
     * Test method to get 2d array of states from dfa file
     * @param dfa DFA file
     * @return Returns 2D array of states
     * @throws FileNotFoundException
     */
    private int[][] dfaGetter(File dfa) throws FileNotFoundException {
        Scanner scan = new Scanner(dfa);
        //Get number of states
        String line = scan.nextLine();
        int numStates = 0;
        char[] chars = line.toCharArray();
        for(char c: chars){
            if(Character.isDigit(c)){
                numStates = Character.getNumericValue(c);
                break;
            }
        }
        line = scan.nextLine();
        //Stores alphabet
        String alpha[] = line.split("     ");
        //Put corresponding chars as values in map
        for(int i=1;i<alpha.length;i++){
            Alpha2Int.put(alpha[i].charAt(0),i-1);
        }
        //Remove ---- line
        scan.nextLine();
        //Number of alphabets
        int numAlpha = alpha.length-1;
        int[][] states = new int[numStates][numAlpha];
        for(int i=0;i<numStates;i++){
            line = scan.nextLine();
            String n[] = line.split("     ");

            for(int j=1;j<=numAlpha;j++){
                states[i][j-1] = Integer.parseInt(n[j]);
            }
        }
        return states;
    }

    /**
     * Parses inputs in file
     * @param inputFile File
     * @return Returns boolean array of results to inputs
     */
    private boolean[] stringParser(File inputFile, int[][] states) throws FileNotFoundException {
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
            outputs[i] = inputTester(in, states);
        }
        return outputs;
    }

    /**
     * Tests a given string against the DFA
     * @param input Input string
     * @return Returns T/F if passed or not
     */
    private boolean inputTester(String input, int[][] states){
        //Get each individual input letter
        char[] letters = input.toCharArray();
        int currentState = initialState;
        //TODO: Placeholder location, holds the accepting states in int form

        for(int i=0;i<letters.length;i++){
            int letterNo = Alpha2Int.get(letters[i]);
            currentState = states[currentState][letterNo];
        }

        // Checks last state against the list of accepting states
        boolean accepted = false;
        for(int i=0;i<acceptingStates.length;i++){
            if(acceptingStates[i]==currentState){
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
                if(i==14){
                    System.out.print(result+"\n");
                }
                else {
                    System.out.print(result + " ");
                }
            }

            else{
                System.out.print(result+"\n");
            }
        }
        System.out.print("Yes: "+numYes+" No: "+numNo);
    }
}