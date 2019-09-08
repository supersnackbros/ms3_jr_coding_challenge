csv_to_sqlite is a Java application that takes a comma-separated data set <input_filename>.csv as input. Valid rows are copied to output SQLite database file <input_filename>.db, invalid rows are copied to output <input_filename>_bad.csv, and the number of valid, invalid, and total rows are written to output file <input_filename>.log.

Usage:
csv_to_sqlite.jar is an executable .jar. To run, copy it and the target .csv to the same directory and run passing the file name as an argument. Within a GNU-Linux based environment it may be run from the shell with a command like "java -jar input_filename.csv."
csv_to_sqlite does not yet support pathnames, only then names of files in the same directory.

Approach:
Developed, complied and exported using Eclipse.
CSV parsing handled using open-source library OpenCSV and all dependencies.
SQLite database creation handled using open-source library SQLJet and all dependencies.

Assumptions:
Support for pathnames not implemented due to time and knowledge constraints.
Operation within a Windows-based environment not tested due to time constraints.
Assumption made that all values in the target database should have text type.
