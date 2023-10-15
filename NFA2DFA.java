import java.io.File;

public class NFA2DFA {
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

    private File fileInput(String name){
        File input = new File(name);
        if (!input.isFile()){
            System.out.println("File does not exist");
            System.exit(2);
        }
        return input;
    }
}