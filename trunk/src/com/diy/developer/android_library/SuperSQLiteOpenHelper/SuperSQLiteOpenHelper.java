/*
 * SuperSQLiteOpenHelper is a Android library that extends the basic
 * 	SQLiteOpenHelper  for ease of use and automatic management.
 * Copyright(C) 2011 Evan Freilich (http://diy-developer.blogspot.com/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diy.developer.android_library.SuperSQLiteOpenHelper;

// Includes
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class SuperSQLiteOpenHelper extends SQLiteOpenHelper{
	// Database data variables to be overridden when this abstract class
	//		is implemented.
	private static String DATABASE_NAME;
	private static int DATABASE_VERSION;
	private final String[][][] DATABASE_SCHEMA;
	/*	Schema syntax
	 * 	DATABASE_SCHEMA = {
	 * 		{	{"table_name","first_version","table_constrains"},
	 * 			// v1
	 * 			{"column1 TYPE",
	 * 			...
	 * 			},
	 * 			// v2
	 * 			{"column4 TYPE"},
	 * 			...
	 * 		},
	 * 		...
	 * 	};
	 *
	 * 	Example implementation
	 * 	DATABASE_SCHEMA = {
	 * 		{	{"people","1","CONSTRAINT (first_name,last_name) ON CONFLICT IGNORE"},
	 * 			// v1
	 * 			{"_id INTEGER primary key autoincrement",
	 * 			"first_name TEXT",
	 * 			"last_name TEXT"},
	 * 			// v2
	 * 			{"middle_init TEXT"},
	 * 			// v3
	 * 			{"company_id INTEGER"},
	 * 			// v4
	 * 			{},
	 * 			//v5
	 * 			{"salary INTEGER"}
	 * 		},
	 * 		{	{"companies","3"},
	 * 			// v1-2
	 * 			{},{},
	 * 			// v3
	 * 			{"_id INTEGER primary key autoincrement",
	 * 			"name TEXT"},
	 * 			// v4
	 * 			{"description TEXT"}
	 * 		}
	 * 	};
	 *
	 * 	Other Considerations:
	 * 		Primary key columns
	 * 			These should normally be called _id as most Android-provided
	 * 			helper functions use this as a hard-coded primary key.
	 * 		Column declarations
	 * 			Can follow any of the syntax listed here:
	 * 				http://sqlite.org/syntaxdiagrams.html#column-def
	 * 		Table constraints
	 * 			Can follow any of the syntas listed here:
	 * 				http://sqlite.org/syntaxdiagrams.html#table-constraint
	 * 			If you want a combination of columns to define a unique record,
	 * 			as with first_name and last_name in the example, this is where
	 * 			you do it.  Plan and code for the conflict cause if applicable.
	 * 		Check all syntax
	 * 			Most if not all functions will fail if correct SQLite syntax
	 * 			is not used for all columns and constraints.
	 * 		You can plan ahead
	 * 			This library gives you the ability to create the schema for
	 * 			future versions but not implement them.  This is not
	 * 			necessarily recommended as it can get confusing but you should
	 * 			be protected if you wish to do so.
	 */

	// Constants
	// Import return values
		static final int IMPORT_RESULT_SUCCESS = 0;
		static final int IMPORT_RESULT_FILE_NOT_FOUND = 1;
		static final int IMPORT_RESULT_ERROR_IN_FILE = 2;
		static final int IMPORT_RESULT_ERROR_READING = 3;
		static final int IMPORT_RESULT_ERROR_INSERTING = 4;
		static final int IMPORT_RESULT_NO_DATA = 5;


	/*	Function: SuperSQLiteOpenHelper (Constructor)
	 * 	Description: Creates a new version of the database helper.  Since this
	 * 		class is abstract, this is where the database information such as
	 * 		name, version, and schema are initialized.
	 * 	Inputs:
	 * 		context (Context)			- the active context of the app
	 * 		DB_Name (String)			- name of the database
	 * 		DB_Version (int)			- version of the database
	 * 		DB_Schema (String[][][])	- schema of the database (syntax above)
	 * 	Output: The new object
	 * 	Throws: None
	 */
	public SuperSQLiteOpenHelper(Context context, String DB_Name, int DB_Version, String[][][] DB_Schema) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		DATABASE_NAME = DB_Name;
		DATABASE_VERSION = DB_Version;
		DATABASE_SCHEMA = DB_Schema;
	}


	/*	Function: onCreate
	 * 	Description: This function handles the creation of a new database.
	 * 		This is done by simply creating all tables listed in the schema.
	 * 		The createTable function handles versioning.
	 *
	 * 		This function is automatically by Android when useWritableDatabase
	 * 		or useReadableDatabase is called.
	 * 	Inputs:
	 * 		db (SQLiteDatabase)	- the database
	 * 	Output: None
	 * 	Throws: SQLException if error in database schema
	 */
	@Override
	public void onCreate(SQLiteDatabase db){
		for( int x = 0; x<DATABASE_SCHEMA.length; ++x){
			createTable(db, x);
		}
	}


	/*	Function: onUpgrade
	 * 	Description: This function handles the upgrade to a newer version
	 * 		of the database.  If a table already existed in a previous
	 * 		version, new columns are added to it.  If not, the table is
	 * 		created from scratch.
	 *
	 * 		This function is automatically by Android when useWritableDatabase
	 * 		or useReadableDatabase is called.
	 * 	Inputs:
	 * 		db (SQLiteDatabase)	- the database
	 * 		oldVersion (int)	- the current version (lower)
	 * 		newVersion (int)	- the target version (higher)
	 * 	Output: None
	 * 	Throws: SQLException if error in database schema
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		if( oldVersion>newVersion ){
			onDowngrade(db, oldVersion, newVersion);
			return;
		}

		String tableName;
		int tableRev;
		int end;

		for( int x = 0; x<DATABASE_SCHEMA.length; ++x){
			tableName = DATABASE_SCHEMA[x][0][0];
			tableRev = Integer.parseInt(DATABASE_SCHEMA[x][0][1]);

			// has the table been created before
			if( tableRev <= oldVersion){
				end = (newVersion+1<DATABASE_SCHEMA[x].length)? newVersion+1 : DATABASE_SCHEMA[x].length;
				for( int y = oldVersion+1; y<end; ++y){
					if( DATABASE_SCHEMA[x][y] != null ){
						for( int z = 0; z<DATABASE_SCHEMA[x][y].length; ++z){
							db.execSQL( "ALTER TABLE " + tableName +
									" ADD COLUMN " + DATABASE_SCHEMA[x][y][z] + ";" );
						}
					}
				}
			}else{
				createTable(db, x);
			}
		}
	}


	/*	Function: onDowngrade
	 * 	Description: This function handles the downgrade to an older version
	 * 		of the database.  Since dropping columns is not allowed in SQLite,
	 * 		each existing table is renamed to a temporary name.  Then, the old
	 * 		version of the tables are created and all old data is moved to the
	 * 		old versions.  Lastly, the temp tables are deleted.
	 *
	 * 		This function is automatically by Android when useWritableDatabase
	 * 		or useReadableDatabase is called.
	 * 	Inputs:
	 * 		db (SQLiteDatabase)	- the database
	 * 		oldVersion (int)	- the current version (higher)
	 * 		newVersion (int)	- the target version (lower)
	 * 	Output: None
	 * 	Throws: SQLException if error in database schema
	 */
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
		List<String> allTables = new LinkedList<String>();
		String oldCols;
		String tableName;
		boolean created;
		int end;

		Cursor cur = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name",null);
		while (cur.moveToNext()) {
			allTables.add(cur.getString(cur.getColumnIndex("name")));
		}

		// create temp tables to save data
		for( String thisTable : allTables){
			db.execSQL( "ALTER TABLE " + thisTable + " RENAME TO __" + thisTable + ";" );
		}

		for( int x = 0; x<DATABASE_SCHEMA.length; ++x){
			tableName = DATABASE_SCHEMA[x][0][0];

			// create old version of table
			created = createTable(db, x);

			// is the table in the old version
			if( created ){
				// get list of old columns and copy data from temp table
				oldCols = "";
				end = (newVersion+1<DATABASE_SCHEMA[x].length)? newVersion+1 : DATABASE_SCHEMA[x].length;
				for( int y = 1; y<end; ++y){
					if( DATABASE_SCHEMA[x][y] != null ){
						for( int z = 0; z<DATABASE_SCHEMA[x][y].length; ++z){
							oldCols += DATABASE_SCHEMA[x][y][z].split(" ")[0] + ",";
						}
					}
				}

				// trim off trailing comma
				oldCols = oldCols.substring(0,oldCols.length()-1);

				// copy old data back in
				db.execSQL( "INSERT INTO " + tableName +
						" SELECT " + oldCols + " FROM __" + DATABASE_SCHEMA[x][0][0] + ";" );

			}
		}

		// delete temp tables
		for( String thisTable : allTables){
			db.execSQL( "DROP TABLE __" + thisTable + ";" );
		}
	}

	// START Helper Functions
	// START Helper Functions
	/*	Function: createTable
	 * 	Description: This function is a helper function used by the onCreate,
	 * 		onUpgrade/Downgrade functions to create individual tables.
	 * 		This function takes an index into the schema array to the specific
	 * 		table to be created.  This creates all columns and adds any listed
	 * 		constraints.  If created successfully, the function returns true.
	 * 		If the table does not exist in the current version, it is skipped
	 * 		and the function returns false.
	 * 	Inputs:
	 * 		db (SQLiteDatabase)	- the database
	 * 		index (int)			- position in schema array of table to create
	 * 	Output: boolean
	 * 		True if table was created
	 * 		False if table was not created (table does not exist in current version)
	 * 	Throws: SQLException if error in database schema
	 */
	private boolean createTable( SQLiteDatabase db, int index) throws SQLException{
		String tableName = DATABASE_SCHEMA[index][0][0];
		int tableRev = Integer.parseInt(DATABASE_SCHEMA[index][0][1]);
		boolean rval = false;

		// create the table structure
		String insertStr = "";
		String tmp;
		int end = (DATABASE_VERSION+1<DATABASE_SCHEMA[index].length)? DATABASE_VERSION+1 : DATABASE_SCHEMA[index].length;
		for( int y = tableRev; y<end; ++y){
			if( DATABASE_SCHEMA[index][y] != null ){
				tmp = Arrays.toString(DATABASE_SCHEMA[index][y]);
				// trim off []
				insertStr += tmp.substring(1,tmp.length()-1) + ",";
			}
		}

		// detect and construct constraint statement
		String constraintStr = "";
		if( DATABASE_SCHEMA[index][0].length >= 3
			&& DATABASE_SCHEMA[index][0][2].length() > 0){
			constraintStr = "," + DATABASE_SCHEMA[index][0][2];
		}

		// run the command
		if( insertStr.length() > 0){
			// trim off trailing comma
			insertStr = insertStr.substring(0,insertStr.length()-1);
			db.execSQL( "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
					insertStr + constraintStr + ");" );
			rval = true;
		}

		return rval;
	}
	// END Helper Functions

	// START Database Helpers
	// 		These functions help the developer interact with the actual database.
	// The active database object used to interact with data
	private SQLiteDatabase activeDB;

	// Returns the database object in case the developer wants to interact in a
	//	non-supported way
	public SQLiteDatabase getActiveDB(){
		return activeDB;
	}

	// Get a writeable version of the database
	public void useWritableDatabase(){
		close();
		activeDB = this.getWritableDatabase();
	}
	// Get a readable version of the database
	public void useReadableDatabase(){
		close();
		activeDB = this.getReadableDatabase();
	}

	// Called when closing the database connection
	@Override
	public void close(){
		if( activeDB.isOpen() )
			activeDB.close();
		super.close();
	}

	/*	Function: cleanDatabase
	 * 	Description: This function cleans the database by deleted
	 * 		all records in all tables.  This is not done in a
	 * 		transaction and is not rolled back if there is an error.
	 * 	Inputs:
	 * 		activeDB_ (SQLiteDatabase)	- the database to clean
	 * 	Output: boolean
	 * 		True if cleaned completely
	 * 		False if an error occurred
	 * 	Throws: SQLException if error in database schema
	 */
	public boolean cleanDatabase(SQLiteDatabase activeDB_) {
		String[] tableList;
		int x = 0;
		boolean allDone = false;
		int tablesEmptied = 0;

		Cursor tblCur = activeDB.rawQuery("SELECT name FROM sqlite_master " +
				"WHERE type='table' AND name != 'android_metadata'",null);
		tableList = new String[ tblCur.getCount() ];
		while (tblCur.moveToNext()){
			tableList[x++] = tblCur.getString(tblCur.getColumnIndex("name"));
		}

		while( !allDone ){
			allDone = true;

			for( x=0; x<tableList.length; ++x){
				try{
					activeDB_.delete(tableList[x], "1", null);
					++tablesEmptied;
				}catch(Exception e){
					allDone = false;
				}
			}
		}

		return (tablesEmptied == tableList.length);
	}

	/*	Function: cleanDatabase
	 * 	Description: This function cleans the database by deleted
	 * 		all records in all tables.  This is not done in a
	 * 		transaction and is not rolled back if there is an error.
	 * 	Inputs: None
	 * 	Output: boolean
	 * 		True if cleaned completely
	 * 		False if an error occurred
	 * 	Throws: SQLException if error in database schema
	 */
	public boolean cleanDatabase(){
		useWritableDatabase();
		return cleanDatabase( activeDB );
	}
	// END Database Helpers

	// START Import/Export functions
	/*	Function: exportToFile
	 * 	Description: This function will export this database to a given file
	 * 		in an xml format.
	 * 	Inputs:
	 * 		dest (File)	- the file to export to
	 * 	Output: boolean
	 * 		True if successful
	 * 		False if not
	 * 	Throws: None
	 */
	public boolean exportToFile( File dest ){
		boolean success = false;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return false;
		}

		//root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Application_Export");
		doc.appendChild(rootElement);

		//Database elements
		Element theDB = exportToElement();
		if( theDB != null ){
			rootElement.appendChild(theDB);

			//write the document into xml file
			try {
				FileWriter writer = new FileWriter( dest );
				writer.write( docNodeToXMLString( doc.getFirstChild() ) );
				writer.close();
				success = true;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return success;
	}

	/*	Function: exportToElement
	 * 	Description: This function will export this database document element.
	 * 	Inputs: None
	 * 	Output: Element
	 * 		The document element containing the database
	 * 		Null if error
	 * 	Throws: None
	 */
	public Element exportToElement(){
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return null;
		}

		//Database elements
		Document doc = docBuilder.newDocument();
		Element DBElement = doc.createElement("Data");

		try{
			useReadableDatabase();
			// Get a list of all current tables
			Cursor tblCur = activeDB.rawQuery("SELECT name FROM sqlite_master " +
					"WHERE type='table' AND name != 'android_metadata'",null);
			// get all data from each table and append it to the document
			String thisTable;
			Cursor thisCur;
			while (tblCur.moveToNext()){
				thisTable = tblCur.getString(tblCur.getColumnIndex("name"));
				Element thisTableElement = doc.createElement(thisTable);

				thisCur = activeDB.rawQuery("SELECT * FROM " + thisTable,null);
				int colCount = thisCur.getColumnCount();
				while (thisCur.moveToNext()){

					Element thisRecord = doc.createElement("record");
					for( int i=0; i<colCount; ++i){
						Element thisCol = doc.createElement(
								thisCur.getColumnName(i) );
						thisCol.appendChild(doc.createTextNode(
								thisCur.getString(i) ));

						thisRecord.appendChild(thisCol);
					}

					thisTableElement.appendChild(thisRecord);
				}

				DBElement.appendChild(thisTableElement);
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}

		return DBElement;
	}

	/*	Function: docNodeToXMLString
	 * 	Description: This is a recursive function that will convert an entire
	 * 		document to an xml string.  If necessary, the function will call
	 * 		itself for any children nodes.  If called on the root node, an
	 * 		xml header will be inserted.  Attributes are supported but are
	 * 		not used in the exportToFile function.
	 * 	Inputs:
	 * 		root (Node)	- the node at which to start building xml
	 * 	Output: String
	 * 		The xml string representing the document
	 * 	Throws: None
	 */
	private static String docNodeToXMLString(Node root){
		StringBuilder result = new StringBuilder();
		if (root.getNodeType() == 3){ // TEXT_NODE
			result.append(root.getNodeValue());
		}else{
			if (root.getNodeType() != 9) { // not DOCUMENT_NODE
				StringBuffer attrs = new StringBuffer();
				for (int k = 0; k < root.getAttributes().getLength(); ++k) {
					attrs.append(" ").append(
					root.getAttributes().item(k).getNodeName()).append(
					"=\"").append(
					root.getAttributes().item(k).getNodeValue())
					.append("\" ");
				}
				result	.append("<")
							.append(root.getNodeName())
							.append(" ")
							.append(attrs)
						.append(">");
			} else {
				result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			}

			NodeList nodes = root.getChildNodes();
			for (int i = 0, j = nodes.getLength(); i < j; i++) {
				Node node = nodes.item(i);
				result.append(docNodeToXMLString(node));
			}

			if (root.getNodeType() != 9) { // not DOCUMENT_NODE
				result	.append("</")
						.append(root.getNodeName())
						.append(">");
			}
		}
		return result.toString();
	}


	/*	Function: importFromFile
	 * 	Description: This function will import data from a given xml file
	 * 		into this database.  If append is false, the database will be
	 * 		cleaned before import.  The import will only import the
	 * 		tables/columns that exist in the database and will ignore
	 * 		extra data.  Missing columns (in case of import from older
	 * 		version) will be inserted as null.  If these columns are
	 * 		non-null, the import will fail and an error will be returned.
	 * 		If a constraint is violated, the import will fail and an
	 * 		error will be returned (this can be avoided if 'ON CONFLICT'
	 * 		is specified as IGNORE or REPLACE - see SQLite documentation).
	 * 		It is up to the developer to handle these reported errors
	 * 		as appropriate.
	 *
	 * 		For now, non-null and constraint violations are indistinguishable.
	 * 		However, this may be fixed in the future (insertWithOnConflict is
	 * 		available in Android API Level 8).
	 * 	Inputs:
	 * 		source (File) 		- the file to import from
	 * 		append (boolean)	- append to database (false to clean it first)
	 * 	Output: int
	 * 		return code
	 * 	Throws: None
	 */
	public int importFromFile( File source, boolean append ){
		int rval;
		InputStream in;
		DocumentBuilder builder;
		Document doc;
		Element dbTables = null;

		try {
			// Read and parse the file (these functions throw all of the specific
			//	exceptions listed below)
			in = new FileInputStream(source);
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(in);

			// Get database element, then get each table and input
			dbTables = (Element) doc.getElementsByTagName("Data").item(0);
		} catch (FileNotFoundException e) {
			rval = IMPORT_RESULT_FILE_NOT_FOUND;
		} catch (ParserConfigurationException e) {
			rval = IMPORT_RESULT_ERROR_IN_FILE;
		} catch (FactoryConfigurationError e) {
			rval = IMPORT_RESULT_ERROR_IN_FILE;
		} catch (SAXException e) {
			rval = IMPORT_RESULT_ERROR_IN_FILE;
		} catch (IOException e) {
			rval = IMPORT_RESULT_ERROR_READING;
		}

		// if successful so far, import from the Data Element
		if( dbTables != null ){
			rval = importFromElement( dbTables, append );
		}else{
			rval = IMPORT_RESULT_NO_DATA;
		}

		return rval;
	}

	/*	Function: importFromElement
	 * 	Description: This function will import data from a given document
	 * 		element.  If append is false, the database will be
	 * 		cleaned before import.  The import will only import the
	 * 		tables/columns that exist in the database and will ignore
	 * 		extra data.  Missing columns (in case of import from older
	 * 		version) will be inserted as null.  If these columns are
	 * 		non-null, the import will fail and an error will be returned.
	 * 		If a constraint is violated, the import will fail and an
	 * 		error will be returned (this can be avoided if 'ON CONFLICT'
	 * 		is specified as IGNORE or REPLACE - see SQLite documentation).
	 * 		It is up to the developer to handle these reported errors
	 * 		as appropriate.
	 *
	 * 		For now, non-null and constraint violations are indistinguishable.
	 * 		However, this may be fixed in the future (insertWithOnConflict is
	 * 		available in Android API Level 8).
	 * 	Inputs:
	 * 		dbTables (Element)	- document with data in it
	 * 		append (boolean)	- append to database (false to clean it first)
	 * 	Output: int
	 * 		return code
	 * 	Throws: None
	 */
	public int importFromElement( Element dbTables, boolean append ){
		int rval = IMPORT_RESULT_SUCCESS;
		boolean noErrors = true;
		Cursor tblCur, thisCur;
		String thisTable;
		String[] columnNames;
		NodeList myRecords;

		// Start a database transaction
		useWritableDatabase();
		activeDB.beginTransaction();
		if( !append )
			cleanDatabase( activeDB );

		// get tables and columns to import
		tblCur = activeDB.rawQuery("SELECT name FROM sqlite_master " +
				"WHERE type='table' AND name != 'android_metadata'",null);
		while (tblCur.moveToNext()){
			thisTable = tblCur.getString(tblCur.getColumnIndex("name"));

			// get the columns in the table
			thisCur = activeDB.rawQuery("SELECT * FROM " + thisTable + " WHERE FALSE",null);
			columnNames = thisCur.getColumnNames();
			thisCur.close();

			// get all data to import and do it
			myRecords = dbTables.getElementsByTagName(thisTable).item(0).getChildNodes();
			if( myRecords.getLength() > 0 ){
				try {
					fillTableFromXML( activeDB, thisTable, columnNames, myRecords );
				} catch (Exception e) {
					noErrors = false;
				}
			}

			if( !noErrors )
				break;
		}
		tblCur.close();

		// Automatically rolled back if not marked successful
		if( noErrors )
			activeDB.setTransactionSuccessful();
		else
			rval = IMPORT_RESULT_ERROR_INSERTING;
		activeDB.endTransaction();

		return rval;
	}

	/*	Function: fillTableFromXML
	 * 	Description: This function will insert data into a database. The
	 * 		database to be inserted into, the table and columns in insert
	 * 		into, and the data to insert.
	 * 	Inputs:
	 * 		db (SQLiteDatabase) 	- the database to insert into
	 * 		thisTable (String)		- the table to insert into
	 * 		theseColumns (String[])	- the columns to insert into the table
	 * 		myRecords (NodeList)	- the data to fill the table
	 * 	Output: None
	 * 	Throws: None
	 */
	private void fillTableFromXML(SQLiteDatabase db, String thisTable, String[] theseColumns, NodeList myRecords){
		Element thisRecord;
		NodeList myCols;
		ContentValues myValues;

		// each element in the nodelist is a single record
		for (int x = 0; x < myRecords.getLength(); x++){
			thisRecord = (Element) myRecords.item(x);
			myValues = new ContentValues();

			// get all values for each column in the column list
			for (int y = 0; y < theseColumns.length; y++){
				myCols = thisRecord.getChildNodes();

				// get the column from the data
				myCols = thisRecord.getElementsByTagName( theseColumns[y] );
				// was the column present
				if( myCols.getLength() > 0 ){
					myValues.put(	theseColumns[y],
									myCols.item(y).getNodeValue());
				}
			}

			db.insert( thisTable, null, myValues );
		}
	}
	// START Import/Export functions
}
