import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadXYFile {
    public static void readFile() {
        String fileName = "example.xy"; // Replace with your file name
        double x = 0, y = 0; // Initialize variables
        
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.trim().split("\\s+"); // Split line by whitespace
                if (values.length == 2) { // Check if line has 2 values
                    x = Double.parseDouble(values[0]); // Parse first value to double and store in x
                    y = Double.parseDouble(values[1]); // Parse second value to double and store in y
                } else {
                    System.out.println("Line does not have 2 values: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("x: " + x);
        System.out.println("y: " + y);
    }
}