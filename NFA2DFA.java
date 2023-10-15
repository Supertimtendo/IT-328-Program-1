import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class NFA2DFA {

    private static final String INPUTS = "-- Input strings for testing -----------";
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
        int currentState = 0;
        for(int i=0;i<letters.length;i++){
            //TODO: Add functionality to get next state from current input, don't know how that will look like yet
            // Change current state to next one
        }
        //if(currentState==An accepting state){
        // return true;
        // }
        //else{
        // return false;
        // }
        return true;
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