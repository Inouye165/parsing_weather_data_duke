import edu.duke.*; // Imports FileResource and other Duke library classes
import org.apache.commons.csv.*; // Imports CSVParser and CSVRecord
/**
 * WeatherDataParser processes CSV weather data to find specific information.
 * This class focuses on finding the coldest temperature in a given file.
 */
public class WeatherDataParser {

    /**
     * Finds the CSVRecord with the coldest temperature in a given CSV file.
     * Ignores records where the temperature is -9999.
     *
     * @param parser The CSVParser object representing the file to be analyzed.
     * @return The CSVRecord corresponding to the coldest valid temperature.
     * Returns null if the parser has no valid records or an error occurs.
     */
    public CSVRecord coldestHourInFile(CSVParser parser) {
        // Initialize coldestRecord to null. It will hold the record with the coldest temperature.
        CSVRecord coldestRecord = null;
        // Initialize lowestTemp to a very large number to ensure the first valid temp is lower.
        double lowestTemp = Double.POSITIVE_INFINITY;

        try {
            // Iterate through each record in the CSV file
            for (CSVRecord currentRecord : parser) {
                // Get the temperature string from the "TemperatureF" column
                String tempString = currentRecord.get("TemperatureF");

                // Check for the bogus value "-9999"
                if (tempString.equals("-9999")) {
                    // Skip this record if the temperature is invalid
                    continue;
                }

                try {
                    // Convert the temperature string to a double
                    double currentTemp = Double.parseDouble(tempString);

                    // Compare the current temperature with the lowest found so far
                    if (currentTemp < lowestTemp) {
                        // If current temperature is colder, update lowestTemp and coldestRecord
                        lowestTemp = currentTemp;
                        coldestRecord = currentRecord;
                    }
                } catch (NumberFormatException e) {
                    // Handle cases where TemperatureF is not a valid number (besides -9999)
                    System.err.println("Warning: Could not parse temperature value: " + tempString + " in record " + currentRecord.getRecordNumber());
                    // Optionally, skip this record or handle the error differently
                }
            }
        } catch (Exception e) {
            // Handle potential exceptions during parsing (e.g., IO errors)
            System.err.println("An error occurred while parsing the CSV file: " + e.getMessage());
            return null; // Return null in case of a major parsing error
        }

        // Return the record that had the coldest valid temperature
        return coldestRecord;
    }

    /**
     * Tests the coldestHourInFile method.
     * Prompts the user to select a weather data file, finds the coldest hour,
     * and prints the temperature and time of occurrence.
     */
    public void testColdestHourinFile() {
        // Create a FileResource to allow the user to select a file.
        FileResource fr = new FileResource();
        // Get the CSVParser object from the selected file.
        CSVParser parser = fr.getCSVParser();

        // Call coldestHourInFile to get the record with the coldest temperature.
        CSVRecord coldest = coldestHourInFile(parser);

        // Check if a valid coldest record was found
        if (coldest != null) {
            // Print the coldest temperature found.
            System.out.println("Coldest temperature was " + coldest.get("TemperatureF") + " F");

            // Determine which time column to use. The PDF mentions TimeEST/TimeEDT
            // but later suggests DateUTC. For this specific method, we'll try TimeEST/TimeEDT first.
            String time = "";
            if (coldest.isSet("TimeEST")) {
                time = coldest.get("TimeEST");
            } else if (coldest.isSet("TimeEDT")) {
                time = coldest.get("TimeEDT");
            } else if (coldest.isSet("DateUTC")) {
                // Fallback to DateUTC if TimeEST/EDT are not present
                time = coldest.get("DateUTC");
                 System.out.println("Note: Using DateUTC for time.");
            } else {
                time = "N/A (Time column not found)";
            }
             // Print the time when the coldest temperature occurred.
            System.out.println("Coldest temperature occurred at " + time);

        } else {
            // Inform the user if no valid coldest temperature record was found.
            System.out.println("No valid temperature readings found or an error occurred.");
        }
    }

    /**
     * Main method to run the test.
     * @param args Command line arguments (not used).
     */
     public static void main(String[] args) {
         WeatherDataParser tester = new WeatherDataParser();
         tester.testColdestHourinFile();
     }
}