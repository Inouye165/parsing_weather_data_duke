import edu.duke.*; // Imports FileResource, DirectoryResource and other Duke library classes
import org.apache.commons.csv.*; // Imports CSVParser and CSVRecord
import java.io.*; // Imports File class for handling files

/**
 * WeatherDataParser processes CSV weather data to find specific information.
 * This class focuses on finding the coldest temperature in a given file
 * and across multiple files.
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
        CSVRecord coldestRecord = null;

        try {
            for (CSVRecord currentRecord : parser) {
                String tempString = currentRecord.get("TemperatureF");
                double currentTemp = -9999; // Initialize with bogus value

                if (!tempString.equals("-9999")) {
                    try {
                        currentTemp = Double.parseDouble(tempString);
                        if (coldestRecord == null || currentTemp < Double.parseDouble(coldestRecord.get("TemperatureF"))) {
                            coldestRecord = currentRecord;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse temperature value: " + tempString + " in record " + currentRecord.getRecordNumber());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while parsing the CSV file: " + e.getMessage());
            return null;
        }
        return coldestRecord;
    }

    /**
     * Tests the coldestHourInFile method.
     * Prompts the user to select a weather data file, finds the coldest hour,
     * and prints the temperature and time of occurrence.
     */
    public void testColdestHourInFile() {
        FileResource fr = new FileResource();
        CSVParser parser = fr.getCSVParser();
        CSVRecord coldest = coldestHourInFile(parser);

        if (coldest != null) {
            System.out.println("Coldest temperature was " + coldest.get("TemperatureF") + " F");
            String time = "N/A";
            if (coldest.isSet("DateUTC")) {
                 time = coldest.get("DateUTC");
            } else {
                 System.out.println("Warning: DateUTC column not found.");
                 if (coldest.isSet("TimeEST")) time = coldest.get("TimeEST");
                 else if (coldest.isSet("TimeEDT")) time = coldest.get("TimeEDT");
            }
            System.out.println("Coldest temperature occurred at " + time);
        } else {
            System.out.println("No valid temperature readings found or an error occurred in the selected file.");
        }
    }

    /**
     * Finds the File object representing the file with the coldest temperature
     * among selected files.
     * Allows the user to select multiple files using a directory chooser.
     *
     * @return The File object corresponding to the file with the overall coldest temperature,
     * or null if no files are selected or no valid temperatures are found.
     */
    public File fileWithColdestTemperature() {
        DirectoryResource dr = new DirectoryResource();
        File coldestFile = null;
        CSVRecord coldestRecordOverall = null;

        for (File f : dr.selectedFiles()) {
            FileResource fr = new FileResource(f); // Use the File object directly
            CSVParser parser = fr.getCSVParser();
            CSVRecord coldestInCurrentFile = coldestHourInFile(parser);

            if (coldestInCurrentFile != null) {
                if (coldestRecordOverall == null ||
                    Double.parseDouble(coldestInCurrentFile.get("TemperatureF")) < Double.parseDouble(coldestRecordOverall.get("TemperatureF")))
                {
                    coldestRecordOverall = coldestInCurrentFile;
                    coldestFile = f; // Keep track of the File object itself
                }
            } else {
                 System.out.println("Note: No valid temperature data found in file: " + f.getName());
            }
        }
        // Close the parser and associated resources if necessary (often handled by Duke libs)
        return coldestFile; // Return the File object
    }

     /**
     * Tests the fileWithColdestTemperature method.
     * Prints the name/path of the file with the coldest temperature among the selected files.
     * Then, it prints all data from that coldest file and the specific coldest reading details.
     */
    public void testFileWithColdestTemperature() {
        // Call the method to find the file object with the coldest temperature
        File theColdestFile = fileWithColdestTemperature(); // Variable type is File

        if (theColdestFile != null) {
            // Use getPath() to show the full path, or getName() for just the filename
            System.out.println("Coldest day was in file " + theColdestFile.getPath());

            // Process that specific file using the File object directly
            FileResource frColdest = new FileResource(theColdestFile); // Use the File object here
            CSVParser parserColdest = frColdest.getCSVParser();
            CSVRecord coldestRecordInFile = coldestHourInFile(parserColdest); // Find coldest again in this file

            if (coldestRecordInFile != null) {
                 System.out.println("Coldest temperature on that day was " + coldestRecordInFile.get("TemperatureF") + " F");

                // Print all the temperatures from the coldest day's file
                System.out.println("All the Temperatures on the coldest day were:");
                // Re-create the FileResource and parser using the File object again
                // because a parser can typically only be iterated once.
                FileResource frColdestAgain = new FileResource(theColdestFile); // Use the File object here too
                CSVParser parserPrint = frColdestAgain.getCSVParser();
                int recordCount = 0;
                for (CSVRecord record : parserPrint) {
                    // Only print if temperature is valid, prevents printing -9999 lines here
                    if (!record.get("TemperatureF").equals("-9999")) {
                        System.out.println(record.get("DateUTC") + ": " + record.get("TemperatureF"));
                    }
                    recordCount++;
                }
                System.out.println("Total records processed: " + recordCount);

                 // Print the specific coldest hour details
                 String time = coldestRecordInFile.isSet("DateUTC") ? coldestRecordInFile.get("DateUTC") : "N/A";
                 System.out.println("Final coldest reading: " + time + ": " + coldestRecordInFile.get("TemperatureF"));

            } else {
                // This might happen if the file identified as coldest somehow has no valid data when re-read,
                // which is unlikely but possible if coldestHourInFile logic changes.
                System.out.println("Could not re-read coldest temperature details from file: " + theColdestFile.getName());
            }

        } else {
            System.out.println("Unable to find file with coldest temperature (no files selected or no valid data).");
        }
    }

    /**
     * Main method to run tests.
     * @param args Command line arguments (not used).
     */
      public static void main(String[] args) {
          System.setProperty("user.dir", "C:/Users/inouy/Downloads/nc_weather/nc_weather/2014");
          WeatherDataParser tester = new WeatherDataParser();

          // --- Choose which test to run ---
          // tester.testColdestHourInFile();
          tester.testFileWithColdestTemperature();
      }
}