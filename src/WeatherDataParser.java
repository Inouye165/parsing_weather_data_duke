import edu.duke.*;           // Imports FileResource, DirectoryResource and other Duke library classes
import org.apache.commons.csv.*; // Imports CSVParser and CSVRecord
import java.io.*;               // Imports File class for handling files
import java.util.ArrayList;     // To store selected files
import java.util.List;          // Interface for List

/**
 * WeatherDataParser processes CSV weather data to find specific information.
 * It includes methods for:
 * 1. Finding the coldest temperature in a file.
 * 2. Finding the file with the coldest temperature among multiple selected files.
 * 3. Finding the lowest humidity in a file.
 * 4. Finding the record with the lowest humidity among multiple selected files.
 * 5. Calculating the average temperature in a file.
 * 6. Calculating the average temperature when humidity meets a threshold in a file.
 * 7. Finding the record with the absolute coldest temperature among multiple selected files. // <-- Added
 *
 * File selection is done once in main, and methods reuse this selection.
 */
public class WeatherDataParser {

    // === Core Logic Methods ===

    /**
     * Finds the CSVRecord with the coldest temperature in a given CSV file.
     * Ignores records where the temperature is -9999.
     *
     * @param parser The CSVParser object representing the file to be analyzed.
     * @return The CSVRecord corresponding to the coldest valid temperature,
     * or null if no valid records are found.
     */
    public CSVRecord coldestHourInFile(CSVParser parser) {
        CSVRecord coldestRecord = null;
        for (CSVRecord currentRecord : parser) {
            String tempString = currentRecord.get("TemperatureF");
            // Ignore bogus temperature values
            if (!tempString.equals("-9999")) {
                try {
                    double currentTemp = Double.parseDouble(tempString);
                    if (coldestRecord == null) {
                        coldestRecord = currentRecord;
                    } else {
                        double lowestTemp = Double.parseDouble(coldestRecord.get("TemperatureF"));
                        if (currentTemp < lowestTemp) {
                            coldestRecord = currentRecord;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Could not parse temperature value: "
                                       + tempString + " in record " + currentRecord.getRecordNumber());
                }
            }
        }
        return coldestRecord;
    }

    /**
     * Finds the CSVRecord with the lowest humidity in a given CSV file.
     * Skips records where the humidity is "N/A". Returns the first record in case of a tie.
     *
     * @param parser The CSVParser object representing the file to be analyzed.
     * @return The CSVRecord corresponding to the lowest valid humidity,
     * or null if no valid humidity readings are found.
     */
    public CSVRecord lowestHumidityInFile(CSVParser parser) {
        CSVRecord lowestHumidityRecord = null;
        for (CSVRecord currentRecord : parser) {
            String humidityString = currentRecord.get("Humidity");
            if (humidityString.equals("N/A")) {
                continue; // Skip "N/A" values
            }
            try {
                double currentHumidity = Double.parseDouble(humidityString);
                if (lowestHumidityRecord == null) {
                    lowestHumidityRecord = currentRecord;
                } else {
                    double lowestHumidity = Double.parseDouble(lowestHumidityRecord.get("Humidity"));
                    // Find lower humidity, return first record in case of tie (< comparison)
                    if (currentHumidity < lowestHumidity) {
                        lowestHumidityRecord = currentRecord;
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("Warning: Could not parse humidity value: "
                                   + humidityString + " in record " + currentRecord.getRecordNumber());
            }
        }
        return lowestHumidityRecord;
    }

    /**
     * Finds the File object representing the file with the coldest temperature
     * among a list of files.
     *
     * @param selectedFiles A list of File objects to analyze.
     * @return The File object corresponding to the file with the overall coldest temperature,
     * or null if the list is empty or no valid temperatures are found.
     */
    public File fileWithColdestTemperature(List<File> selectedFiles) {
        File coldestFile = null;
        CSVRecord coldestRecordOverall = null; // Internal variable to track lowest record

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return null; // No files to process
        }

        for (File f : selectedFiles) {
            FileResource fr = new FileResource(f);
            CSVParser parser = fr.getCSVParser();
            CSVRecord currentColdest = coldestHourInFile(parser); // Use existing method

            if (currentColdest != null) {
                // Initialize if this is the first valid record found
                if (coldestRecordOverall == null) {
                    coldestRecordOverall = currentColdest;
                    coldestFile = f;
                } else {
                    // Compare with the current overall coldest
                    try {
                         double currentTemp = Double.parseDouble(currentColdest.get("TemperatureF"));
                         double overallLowestTemp = Double.parseDouble(coldestRecordOverall.get("TemperatureF"));

                        if (currentTemp < overallLowestTemp) {
                            coldestRecordOverall = currentColdest; // Update record tracking
                            coldestFile = f;                     // Update file tracking
                        }
                    } catch (NumberFormatException e) {
                         // This shouldn't happen if coldestHourInFile worked, but good practice
                         System.err.println("Error comparing temperatures from file: " + f.getName());
                    }
                }
            } else {
                System.out.println("Note: No valid temperature data found in file: " + f.getName());
            }
        }
        return coldestFile; // Returns the FILE, not the record
    }

    /**
     * Finds the CSVRecord with the absolute coldest temperature across multiple files.
     *
     * @param selectedFiles A list of File objects to analyze.
     * @return The CSVRecord with the overall coldest temperature,
     * or null if the list is empty or no valid temperature is found.
     */
    public CSVRecord coldestHourInManyFiles(List<File> selectedFiles) {
        CSVRecord coldestRecordOverall = null;

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return null; // No files to process
        }

        for (File f : selectedFiles) {
            FileResource fr = new FileResource(f);
            CSVParser parser = fr.getCSVParser();
            // Find the coldest record in the current file
            CSVRecord currentColdest = coldestHourInFile(parser);

            if (currentColdest != null) {
                // Initialize if this is the first valid record found
                if (coldestRecordOverall == null) {
                    coldestRecordOverall = currentColdest;
                } else {
                    // Compare with the current overall coldest
                    try {
                        double currentTemp = Double.parseDouble(currentColdest.get("TemperatureF"));
                        double overallLowestTemp = Double.parseDouble(coldestRecordOverall.get("TemperatureF"));

                        if (currentTemp < overallLowestTemp) {
                            coldestRecordOverall = currentColdest; // Update record tracking
                        }
                    } catch (NumberFormatException e) {
                        // This shouldn't happen if coldestHourInFile worked
                        System.err.println("Error comparing temperature from file: " + f.getName());
                    }
                }
            }
            // No need for an else here, just means this file had no valid data
        }
        return coldestRecordOverall; // Returns the actual record
    }


    /**
     * Finds the CSVRecord with the lowest humidity across multiple files.
     * If there is a tie, returns the first such record encountered.
     *
     * @param selectedFiles A list of File objects to analyze.
     * @return The CSVRecord with the overall lowest humidity,
     * or null if the list is empty or no valid humidity is found.
     */
    public CSVRecord lowestHumidityInManyFiles(List<File> selectedFiles) {
        CSVRecord lowestHumidityRecordOverall = null;

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return null; // No files to process
        }

        for (File f : selectedFiles) {
            FileResource fr = new FileResource(f);
            CSVParser parser = fr.getCSVParser();
            // Find the lowest humidity record in the current file
            CSVRecord currentLowestHumidityRecord = lowestHumidityInFile(parser);

            if (currentLowestHumidityRecord != null) {
                // Initialize if this is the first valid record found
                if (lowestHumidityRecordOverall == null) {
                    lowestHumidityRecordOverall = currentLowestHumidityRecord;
                } else {
                    // Compare with the current overall lowest humidity
                    try {
                        double currentHumidity = Double.parseDouble(currentLowestHumidityRecord.get("Humidity"));
                        double overallLowestHumidity = Double.parseDouble(lowestHumidityRecordOverall.get("Humidity"));

                        // Note: < comparison ensures the first record wins in a tie
                        if (currentHumidity < overallLowestHumidity) {
                            lowestHumidityRecordOverall = currentLowestHumidityRecord;
                        }
                    } catch (NumberFormatException e) {
                        // This shouldn't happen if lowestHumidityInFile worked
                        System.err.println("Error comparing humidity from file: " + f.getName());
                    }
                }
            } else {
                 System.out.println("Note: No valid humidity data found in file: " + f.getName());
            }
        }
        return lowestHumidityRecordOverall;
    }

    /**
     * Calculates the average temperature from valid readings in a CSV file.
     * Ignores temperatures of -9999.
     *
     * @param parser The CSVParser object representing the file to be analyzed.
     * @return The average temperature as a double, or Double.NaN if no valid
     * temperature readings are found.
     */
    public double averageTemperatureInFile(CSVParser parser) {
        double sum = 0.0;
        int count = 0;
        for (CSVRecord record : parser) {
            String tempString = record.get("TemperatureF");
            if (!tempString.equals("-9999")) {
                try {
                    double temp = Double.parseDouble(tempString);
                    sum += temp;
                    count++;
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Could not parse temperature value: "
                                       + tempString + " in record " + record.getRecordNumber());
                }
            }
        }
        if (count > 0) {
            return sum / count;
        } else {
            return Double.NaN; // Use NaN to indicate no valid data
        }
    }

    /**
     * Calculates the average temperature for records where humidity is
     * greater than or equal to a specified value.
     * Ignores temperatures of -9999 and humidity values of "N/A".
     *
     * @param parser The CSVParser object representing the file to be analyzed.
     * @param value The minimum humidity threshold (inclusive).
     * @return The average temperature as a double for the matching records,
     * or Double.NaN if no such records are found.
     */
    public double averageTemperatureWithHighHumidityInFile(CSVParser parser, int value) {
        double sum = 0.0;
        int count = 0;
        for (CSVRecord record : parser) {
            String humidityString = record.get("Humidity");
            String tempString = record.get("TemperatureF");

            if (humidityString.equals("N/A") || tempString.equals("-9999")) {
                continue; // Skip invalid readings
            }

            try {
                double humidity = Double.parseDouble(humidityString);
                if (humidity >= value) {
                    // Only parse temperature if humidity condition met
                     try {
                         double temp = Double.parseDouble(tempString);
                         sum += temp;
                         count++;
                     } catch (NumberFormatException tempE) {
                          System.err.println("Warning: Could not parse temperature value: "
                                        + tempString + " in record " + record.getRecordNumber());
                     }
                }
            } catch (NumberFormatException humE) {
                 System.err.println("Warning: Could not parse humidity value: "
                                   + humidityString + " in record " + record.getRecordNumber());
            }
        }

        if (count > 0) {
            return sum / count;
        } else {
            return Double.NaN; // Use NaN to indicate no valid data meeting criteria
        }
    }


    // === Test Methods ===
    // Updated to accept File/List<File> parameters

    /**
     * Tests the coldestHourInFile method using a specific file.
     *
     * @param fileToTest The File object to analyze.
     */
    public void testColdestHourInFile(File fileToTest) {
        if (fileToTest == null) {
             System.out.println("No file provided for testColdestHourInFile.");
             return;
        }
        FileResource fr = new FileResource(fileToTest);
        CSVParser parser = fr.getCSVParser();
        CSVRecord coldest = coldestHourInFile(parser);

        if (coldest != null) {
            System.out.println("Coldest temperature in file " + fileToTest.getName()
                               + " was " + coldest.get("TemperatureF") + " F");
            String time = "N/A";
             // Prefer DateUTC as per instructions
            if (coldest.isSet("DateUTC")) {
                 time = coldest.get("DateUTC");
            } else {
                 // Fallback logic (though DateUTC should usually exist in these files)
                 System.out.println("Warning: DateUTC column not found, attempting fallbacks.");
                 if (coldest.isSet("TimeEST")) time = coldest.get("TimeEST");
                 else if (coldest.isSet("TimeEDT")) time = coldest.get("TimeEDT");
            }
            System.out.println("Coldest temperature occurred at " + time);
        } else {
            System.out.println("No valid temperature readings found in file " + fileToTest.getName());
        }
    }

    /**
     * Tests the fileWithColdestTemperature method using a list of files.
     *
     * @param filesToTest A list of File objects to analyze.
     */
    public void testFileWithColdestTemperature(List<File> filesToTest) {
         if (filesToTest == null || filesToTest.isEmpty()) {
             System.out.println("No files provided for testFileWithColdestTemperature.");
             return;
         }

        File theColdestFile = fileWithColdestTemperature(filesToTest); // Pass the list here

        if (theColdestFile != null) {
            System.out.println("Coldest day was in file " + theColdestFile.getName()); // Use getName for cleaner output

            // Process that specific file again to print details
            FileResource frColdest = new FileResource(theColdestFile);
            CSVParser parserColdest = frColdest.getCSVParser();
            CSVRecord coldestRecordInFile = coldestHourInFile(parserColdest); // Find coldest again in this specific file

            if (coldestRecordInFile != null) {
                 System.out.println("Coldest temperature on that day was "
                                    + coldestRecordInFile.get("TemperatureF") + " F");

                // Print all the temperatures from the coldest day's file
                System.out.println("All the Temperatures on the coldest day were:");
                // Re-create the FileResource and parser to iterate again.
                FileResource frColdestAgain = new FileResource(theColdestFile);
                CSVParser parserPrint = frColdestAgain.getCSVParser();
                // int recordCount = 0; // Don't need count here based on assignment output
                for (CSVRecord record : parserPrint) {
                    // Only print if temperature is valid
                    if (!record.get("TemperatureF").equals("-9999")) {
                        // Use DateUTC for timestamp
                        String time = record.isSet("DateUTC") ? record.get("DateUTC") : "Unknown Time";
                        System.out.println(time + ": " + record.get("TemperatureF"));
                    }
                    // recordCount++;
                }
                // System.out.println("Total records processed: " + recordCount);

            } else {
                System.out.println("Could not re-read coldest temperature details from file: " + theColdestFile.getName());
            }

        } else {
            System.out.println("Unable to find file with coldest temperature among the selected files.");
        }
    }

    /**
     * Tests the lowestHumidityInFile method using a specific file.
     *
     * @param fileToTest The File object to analyze.
     */
    public void testLowestHumidityInFile(File fileToTest) {
        if (fileToTest == null) {
             System.out.println("No file provided for testLowestHumidityInFile.");
             return;
        }
        FileResource fr = new FileResource(fileToTest);
        CSVParser parser = fr.getCSVParser();
        CSVRecord lowestHumidity = lowestHumidityInFile(parser);

        if (lowestHumidity != null) {
            System.out.println("Lowest Humidity in file " + fileToTest.getName()
                               + " was " + lowestHumidity.get("Humidity") +
                               " at " + lowestHumidity.get("DateUTC")); // Use DateUTC as requested
        } else {
            System.out.println("No valid humidity readings found in file " + fileToTest.getName());
        }
    }

     /**
     * Tests the lowestHumidityInManyFiles method using a list of files.
     *
     * @param filesToTest A list of File objects to analyze.
     */
    public void testLowestHumidityInManyFiles(List<File> filesToTest) {
        if (filesToTest == null || filesToTest.isEmpty()) {
             System.out.println("No files provided for testLowestHumidityInManyFiles.");
             return;
         }

        CSVRecord lowestOverall = lowestHumidityInManyFiles(filesToTest);

        if (lowestOverall != null) {
             System.out.println("Lowest Humidity was " + lowestOverall.get("Humidity") +
                                " at " + lowestOverall.get("DateUTC"));
        } else {
             System.out.println("No valid humidity readings found in any of the selected files.");
        }
    }

    /**
     * Tests the averageTemperatureInFile method using a specific file.
     *
     * @param fileToTest The File object to analyze.
     */
    public void testAverageTemperatureInFile(File fileToTest) {
        if (fileToTest == null) {
             System.out.println("No file provided for testAverageTemperatureInFile.");
             return;
        }
        FileResource fr = new FileResource(fileToTest);
        CSVParser parser = fr.getCSVParser();
        double averageTemp = averageTemperatureInFile(parser);

        if (!Double.isNaN(averageTemp)) {
            System.out.println("Average temperature in file " + fileToTest.getName() + " is " + averageTemp);
        } else {
            System.out.println("No valid temperature readings found in file " + fileToTest.getName());
        }
    }

    /**
     * Tests the averageTemperatureWithHighHumidityInFile method using a specific file.
     *
     * @param fileToTest The File object to analyze.
     */
    public void testAverageTemperatureWithHighHumidityInFile(File fileToTest) {
         if (fileToTest == null) {
             System.out.println("No file provided for testAverageTemperatureWithHighHumidityInFile.");
             return;
        }
        FileResource fr = new FileResource(fileToTest);
        CSVParser parser = fr.getCSVParser();
        int humidityThreshold = 80; // As specified in the example
        double averageTemp = averageTemperatureWithHighHumidityInFile(parser, humidityThreshold);

        System.out.print("Testing average temperature with humidity >= " + humidityThreshold + " in file " + fileToTest.getName() + ": ");
        if (!Double.isNaN(averageTemp)) {
            System.out.println("Average Temp when high Humidity is " + averageTemp);
        } else {
            // Match the required output format
            System.out.println("No temperatures with that humidity");
        }
    }

    /**
     * NEW: Tests the coldestHourInManyFiles method using a list of files.
     *
     * @param filesToTest A list of File objects to analyze.
     */
    public void testColdestHourInManyFiles(List<File> filesToTest) {
        if (filesToTest == null || filesToTest.isEmpty()) {
             System.out.println("No files provided for testColdestHourInManyFiles.");
             return;
        }

        CSVRecord coldestOverall = coldestHourInManyFiles(filesToTest);

        if (coldestOverall != null) {
             System.out.println("Overall coldest temperature was " + coldestOverall.get("TemperatureF") + "F" +
                                " at " + coldestOverall.get("DateUTC"));
        } else {
             System.out.println("No valid temperature readings found in any of the selected files.");
        }
    }


    // === Main Method (Refactored for single file selection) ===

    /**
     * Main method to run tests. Selects files ONCE and passes them to test methods.
     * Runs tests for:
     * 1. Coldest hour in the first selected file.
     * 2. File with the overall coldest temperature among selected files.
     * 3. Lowest humidity in the first selected file.
     * 4. Lowest humidity among all selected files.
     * 5. Average temperature in the first selected file.
     * 6. Average temperature with high humidity in the first selected file.
     * 7. Absolute coldest hour among all selected files. // <-- Added
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Adjust working directory if needed (or remove if running from correct dir)
        System.setProperty("user.dir", "C:\\Users\\inouy\\Downloads\\nc_weather\\nc_weather\\2014"); // Example path structure
        WeatherDataParser tester = new WeatherDataParser();

        // --- Select Files ONCE ---
        System.out.println("Please select the weather data file(s) for analysis...");
        DirectoryResource dr = new DirectoryResource();
        List<File> selectedFiles = new ArrayList<>();
        for (File f : dr.selectedFiles()) {
            selectedFiles.add(f);
        }

        if (selectedFiles.isEmpty()) {
            System.out.println("No files were selected. Exiting.");
            return; // Stop if no files are chosen
        }

        // Get the first file for tests that need only one
        File firstFile = selectedFiles.get(0);

        // --- Run Tests ---

        System.out.println("\n=== Test 1: Coldest Hour in File (First Selected File) ===");
        tester.testColdestHourInFile(firstFile);
        System.out.println();

        System.out.println("=== Test 2: File with Coldest Temperature (Across All Selected Files) ===");
        tester.testFileWithColdestTemperature(selectedFiles); // Pass the whole list
        System.out.println();

        System.out.println("=== Test 3: Lowest Humidity in File (First Selected File) ===");
        tester.testLowestHumidityInFile(firstFile);
        System.out.println();

        System.out.println("=== Test 4: Lowest Humidity in Many Files (Across All Selected Files) ===");
        tester.testLowestHumidityInManyFiles(selectedFiles); // Pass the whole list
        System.out.println();

        System.out.println("=== Test 5: Average Temperature in File (First Selected File) ===");
        tester.testAverageTemperatureInFile(firstFile);
        System.out.println();

        System.out.println("=== Test 6: Average Temperature with High Humidity (First Selected File) ===");
        tester.testAverageTemperatureWithHighHumidityInFile(firstFile);
        System.out.println();

        // --- Added Test 7 ---
        System.out.println("=== Test 7: Absolute Coldest Hour (Across All Selected Files) ===");
        tester.testColdestHourInManyFiles(selectedFiles); // Pass the whole list
        System.out.println();

        System.out.println("=== All tests complete. ===");
    }
}