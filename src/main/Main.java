package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class Main {
	public static void main(String[] args) {
		
		// Check for one argument.
		if (args.length != 1) {
			System.out.println("Error: only one argument accepted");
			System.exit(1);
		}
		
		// Get input file name, check for just a filename and .csv filename extension.
		String inputFileName = args[0];
		
		if (inputFileName.contains("/") || inputFileName.contains("\\")) {
			System.out.println("Error: pathnames not supported. Copy target .csv into this " +
							   "directory.");
			System.exit(1);
		}
		
		if (!inputFileName.endsWith(".csv")) {
			System.out.println("Error: input file name is not a .csv");
			System.exit(1);
		}
		String fileNamePrefix = inputFileName.substring(0, inputFileName.lastIndexOf(".csv"));
		
		try {
			// Open CSV input and output streams.
			System.out.println("Opening " + inputFileName);
			CSVReader inputReader = new CSVReader(new FileReader(inputFileName));
			String badEntryFileName = fileNamePrefix + "_bad.csv";
			System.out.println("Creating " + badEntryFileName);
			CSVWriter badEntryWriter = new CSVWriter(new FileWriter(badEntryFileName));
			
			// Delete existing output DB if it exists, then create a new one.
			String dbFileName = fileNamePrefix + ".db";
			System.out.println("Creating " + dbFileName);
			File dbFile = new File(dbFileName);
			dbFile.delete();
			SqlJetDb db = SqlJetDb.open(dbFile, true);
			
			// Read first line of input and create corresponding line in bad entries CSV. Use as
			// basis for number of columns.
			String [] currLine = inputReader.readNext();
			badEntryWriter.writeNext(currLine);
			
			int numColumns = currLine.length;
			int lastColumnIndex = numColumns - 1;
			
			// Create DB table creation query for corresponding columns.
			System.out.println("Preparing database table");
			String tableCreationStatement = "CREATE TABLE " + fileNamePrefix + " (";
			for (int i = 0; i < lastColumnIndex; i++)
				tableCreationStatement += String.format("%s TEXT NOT NULL, ", currLine[i]);
			tableCreationStatement += String.format("%s TEXT NOT NULL)",
					                                currLine[lastColumnIndex]);
			
			// Begin DB transaction and create table.
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			db.createTable(tableCreationStatement);
			ISqlJetTable table = db.getTable(fileNamePrefix);
			
			// Read remaining lines and insert into DB or bad entries CSV.
			int goodEntries = 0;
			int badEntries = 0;
			
			System.out.println("Parsing entries from " + inputFileName);
			while ((currLine = inputReader.readNext()) != null) {
				try {
					// Throw exception for a bad number of values.
					if (currLine.length != numColumns)
						throw new BadLineException();
					
					// Iterate through line and throw exception for emtpy value.
					for (String currVal : currLine)
						if (currVal.isEmpty())
							throw new BadLineException();
					
					// No exception thrown. Insert into database.
					table.insert((Object[]) currLine);
					goodEntries++;
					
				} catch (BadLineException ble) {
					// Caught exception writes line to bad entries CSV instead of DB table.
					badEntryWriter.writeNext(currLine);
					badEntries++;
				}
			}
			
			// End transaction and close database.
			db.commit();
			db.close();
			
			// Close CSV streams.
			inputReader.close();
			badEntryWriter.close();
				
			// Open and write results to log.
			String logFileName = fileNamePrefix + ".log";
			System.out.println("Writing log file " + logFileName);
			BufferedWriter logWriter = new BufferedWriter(new FileWriter(fileNamePrefix + ".log"));
			logWriter.write(String.format("Records received: %d\n" +
										  "Successful records: %d\n" +
										  "Failed records: %d",
										  goodEntries + badEntries, goodEntries, badEntries));
			logWriter.close();
			
		} catch (Exception e) {
			System.out.println(e.toString());
			System.out.println("Terminated unsuccessfully");
			System.exit(1);
		}
		System.out.println("Terminated successfully");
		System.exit(0);
	}
}
