import edu.duke.*;               // Imports FileResource, DirectoryResource and other Duke library classes
import org.apache.commons.csv.*;  // Imports CSVParser and CSVRecord
import java.io.*;                // Imports File class for handling files

/**
 * WeatherDataParser processes CSV weather data to find specific information.
 * It includes methods for:
 *  1. Finding the coldest temperature in a file (using the first file selected).
 *  2. Finding the file with the coldest temperature among multiple selected files.
 *  3. Finding the lowest humidity in a file (using the first file selected).
 */
public class WeatherDataParser {

    /**
     * Finds the CSVRecord with the coldest temperature in a given CSV file.
     * Ignores records where the temperature is -9999.
     *
     * @param parser The CSVParser object representing the file to be analyzed.
     * @return The CSVRecord corresponding to the coldest valid temperature,
     *         or null if no valid records are found.
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
     * Tests the coldestHourInFile method.
     * Uses a DirectoryResource to allow selecting multiple files,
     * but then processes only the first file selected.
     */
    public void testColdestHourInFile() {
        DirectoryResource dr = new DirectoryResource();
        File firstFile = null;
        for (File f : dr.selectedFiles()) {
            firstFile = f;  // Use only the first file
            break;
        }
        if (firstFile != null) {
            FileResource fr = new FileResource(firstFile);
            CSVParser parser = fr.getCSVParser();
            CSVRecord coldest = coldestHourInFile(parser);
            if (coldest != null) {
                System.out.println("Coldest temperature in file " + firstFile.getName() 
                                   + " was " + coldest.get("TemperatureF") + " F");
                String time = "N/A";
                if (coldest.isSet("DateUTC"))
                    time = coldest.get("DateUTC");
                else {
                    if (coldest.isSet("TimeEST"))
                        time = coldest.get("TimeEST");
                    else if (coldest.isSet("TimeEDT"))
                        time = coldest.get("TimeEDT");
                }
                System.out.println("Coldest temperature occurred at " + time);
            } else {
                System.out.println("No valid temperature readings in file " + firstFile.getName());
            }
        } else {
            System.out.println("No files selected.");
        }
    }

    /**
     * Finds the File object representing the file with the coldest temperature
     * among selected files. Allows the user to select multiple files.
     *
     * @return The File object corresponding to the file with the overall coldest temperature,
     *         or null if no files are selected or no valid temperatures are found.
     */
    public File fileWithColdestTemperature() {
        DirectoryResource dr = new DirectoryResource();
        File coldestFile = null;
        CSVRecord coldestRecordOverall = null;
        for (File f : dr.selectedFiles()) {
            FileResource fr = new FileResource(f);
            CSVParser parser = fr.getCSVParser();
            CSVRecord currentColdest = coldestHourInFile(parser);
            if (currentColdest != null) {
                if (coldestRecordOverall == null ||
                    Double.parseDouble(currentColdest.get("TemperatureF")) < 
                    Double.parseDouble(coldestRecordOverall.get("TemperatureF"))) {
                    coldestRecordOverall = currentColdest;
                    coldestFile = f;
                }
            } else {
                System.out.println("Note: No valid temperature data found in file: " + f.getName());
            }
        }
        return coldestFile;
    }

    /**
     * Tests the fileWithColdestTemperature method.
     * Uses DirectoryResource to select multiple files, finds the file with the coldest temperature,
     * and then prints all temperature readings from that file along with the coldest reading details.
     */
    public void testFileWithColdestTemperature() {
        File theColdestFile = fileWithColdestTemperature();
        if (theColdestFile != null) {
            System.out.println("Coldest day was in file " + theColdestFile.getPath());
            FileResource frColdest = new FileResource(theColdestFile);
            CSVParser parserColdest = frColdest.getCSVParser();
            CSVRecord coldestRecordInFile = coldestHourInFile(parserColdest);
            if (coldestRecordInFile != null) {
                System.out.println("Coldest temperature on that day was " 
                                   + coldestRecordInFile.get("TemperatureF") + " F");
                System.out.println("All the Temperatures on the coldest day were:");
                // Re-create parser to iterate over the file again.
                FileResource frAgain = new FileResource(theColdestFile);
                CSVParser parserAgain = frAgain.getCSVParser();
                int recordCount = 0;
                for (CSVRecord record : parserAgain) {
                    if (!record.get("TemperatureF").equals("-9999")) {
                        System.out.println(record.get("DateUTC") + ": " + record.get("TemperatureF"));
                    }
                    recordCount++;
                }
                System.out.println("Total records processed: " + recordCount);
            } else {
                System.out.println("Could not re-read coldest temperature details from file: " 
                                   + theColdestFile.getName());
            }
        } else {
            System.out.println("Unable to find file with coldest temperature (no files selected or no valid data).");
        }
    }

    /**
     * Finds the CSVRecord with the lowest humidity in a given CSV file.
     * Skips records where the humidity is "N/A".
     *
     * @param parser The CSVParser object representing the file to be analyzed.
     * @return The CSVRecord corresponding to the lowest valid humidity,
     *         or null if no valid humidity readings are found.
     */
    public CSVRecord lowestHumidityInFile(CSVParser parser) {
        CSVRecord lowestHumidityRecord = null;
        for (CSVRecord currentRecord : parser) {
            String humidityString = currentRecord.get("Humidity");
            if (humidityString.equals("N/A")) {
                continue;
            }
            try {
                double currentHumidity = Double.parseDouble(humidityString);
                if (lowestHumidityRecord == null) {
                    lowestHumidityRecord = currentRecord;
                } else {
                    double lowestHumidity = Double.parseDouble(lowestHumidityRecord.get("Humidity"));
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
     * Tests the lowestHumidityInFile method.
     * Uses DirectoryResource to allow selecting multiple files,
     * but then processes only the first file selected.
     */
    public void testLowestHumidityInFile() {
        DirectoryResource dr = new DirectoryResource();
        File firstFile = null;
        for (File f : dr.selectedFiles()) {
            firstFile = f;  // Use only the first file
            break;
        }
        if (firstFile != null) {
            FileResource fr = new FileResource(firstFile);
            CSVParser parser = fr.getCSVParser();
            CSVRecord lowestHumidity = lowestHumidityInFile(parser);
            if (lowestHumidity != null) {
                System.out.println("Lowest Humidity in file " + firstFile.getName() 
                                   + " was " + lowestHumidity.get("Humidity") +
                                   " at " + lowestHumidity.get("DateUTC"));
            } else {
                System.out.println("No valid humidity readings in file " + firstFile.getName());
            }
        } else {
            System.out.println("No files selected.");
        }
    }

    /**
     * Main method to run tests for:
     *  1. Coldest hour in a file (using first file from multiple selection)
     *  2. File with the overall coldest temperature among selected files
     *  3. Lowest humidity in a file (using first file from multiple selection)
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Adjust working directory as needed
        System.setProperty("user.dir", "C:/Users/inouy/Downloads/nc_weather/nc_weather/2014");
        WeatherDataParser tester = new WeatherDataParser();

        System.out.println("=== Test: Coldest Hour in File (First Selected File) ===");
        tester.testColdestHourInFile();
        System.out.println();

        System.out.println("=== Test: File with Coldest Temperature (Across Multiple Files) ===");
        tester.testFileWithColdestTemperature();
        System.out.println();

        System.out.println("=== Test: Lowest Humidity in File (First Selected File) ===");
        tester.testLowestHumidityInFile();
    }
}
