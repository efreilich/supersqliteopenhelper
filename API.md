# Constants #
## Import return values ##
Returned in the import functions below to indicate possible results.<br>
<ul><li>IMPORT_RESULT_SUCCESS = 0<br>
</li><li>IMPORT_RESULT_FILE_NOT_FOUND = 1<br>
</li><li>IMPORT_RESULT_ERROR_IN_FILE = 2<br>
</li><li>IMPORT_RESULT_ERROR_READING = 3<br>
</li><li>IMPORT_RESULT_ERROR_INSERTING = 4<br>
</li><li>IMPORT_RESULT_NO_DATA = 5</li></ul>

<br>

<h1>Public Methods</h1>
<pre><code>public SuperSQLiteOpenHelper(Context context, String DB_Name, int DB_Version, String[][][] DB_Schema)<br>
</code></pre>
Constructor.  Creates a new version of the database helper.  Since this class is abstract, this is where the database information such as name, version, and schema are initialized.<br>
<b>Inputs:</b>
<table><thead><th> context (Context) </th><th> the active context of the app </th></thead><tbody>
<tr><td> DB_Name (String)) </td><td> name of the database </td></tr>
<tr><td> DB_Version (int)) </td><td> version of the database </td></tr>
<tr><td> DB_Schema (String<a href='.md'>.md</a><a href='.md'>.md</a><a href='.md'>.md</a>)) </td><td> schema of the database (syntax above) </td></tr></tbody></table>

<b>Output:</b> The new object<br>

<b>Throws:</b> None<br>
<br>
<br>
<pre><code>public void onCreate(SQLiteDatabase db)<br>
</code></pre>
This function handles the creation of a new database. This is done by simply creating all tables listed in the schema. The createTable function handles versioning.<br>
This function is automatically by Android when useWritableDatabase or useReadableDatabase is called (not called directly).<br>
<b>Inputs:</b>
<table><thead><th> db (SQLiteDatabase) </th><th> the database </th></thead><tbody></tbody></table>

<b>Output:</b> None<br>
<b>Throws:</b> SQLException if error in database schema<br>
<br>
<br>
<pre><code>public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)<br>
</code></pre>
This function handles the upgrade to a newer version of the database.  If a table already existed in a previous version, new columns are added to it.  If not, the table is created from scratch.<br>
This function is automatically by Android when useWritableDatabase or useReadableDatabase is called (not called directly).<br>
<b>Inputs:</b>
<table><thead><th> db (SQLiteDatabase) </th><th> the database </th></thead><tbody>
<tr><td> oldVersion (int) </td><td> the current version (lower) </td></tr>
<tr><td> newVersion (int) </td><td> the target version (higher) </td></tr></tbody></table>

<b>Output:</b> None<br>
<b>Throws:</b> SQLException if error in database schema<br>
<br>
<br>
<pre><code>public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)<br>
</code></pre>
This function handles the downgrade to an older version of the database.  Since dropping columns is not allowed in SQLite, each existing table is renamed to a temporary name.  Then, the old version of the tables is created and all old data is moved to the old versions.  Lastly, the temp tables are deleted.<br>
This function is automatically by Android when useWritableDatabase or useReadableDatabase is called (not called directly).<br>
<b>Inputs:</b>
<table><thead><th> db (SQLiteDatabase) </th><th> the database </th></thead><tbody>
<tr><td> oldVersion (int) </td><td> the current version (higher) </td></tr>
<tr><td> newVersion (int) </td><td> the target version (lower) </td></tr></tbody></table>

<b>Output:</b> None<br>
<b>Throws:</b> SQLException if error in database schema<br>
<br>
<br>
<pre><code>public SQLiteDatabase getActiveDB()<br>
</code></pre>
Returns the database object in case the developer wants to interact in a non-supported way.<br>
<b>Inputs:</b> None<br>
<b>Output:</b> SQLiteDatabase - the reference to the internal active database object<br>
<b>Throws:</b> None<br>
<br>
<br>
<pre><code>public void useWritableDatabase()<br>
</code></pre>
Opens a writeable copy of the database and saves it to an internal variable for later use. If there is an existing version open, it is first closed.<br>
<b>Inputs:</b> None<br>
<b>Output:</b> None<br>
<b>Throws:</b> None<br>
<br>
<br>
<pre><code>public void useReadableDatabase()<br>
</code></pre>
Opens a readable copy of the database and saves it to an internal variable for later use. If there is an existing version open, it is first closed.<br>
<b>Inputs:</b> None<br>
<b>Output:</b> None<br>
<b>Throws:</b> None<br>
<br>
<br>
<pre><code>public void close()<br>
</code></pre>
Closes the internal reference to the database.<br>
<b>Inputs:</b> None<br>
<b>Output:</b> None<br>
<b>Throws:</b> None<br>
<br>
<br>
<pre><code>public boolean cleanDatabase(SQLiteDatabase activeDB_)<br>
</code></pre>
This function cleans the database by deleted all records in all tables.  This is not done in a transaction and is not rolled back if there is an error. If you need a transaction, get a writable version of the database, put it in a transaction, and pass it to this function.  Upon result, act accordingly.<br>
<b>Inputs:</b>
<table><thead><th> activeDB<i>(SQLiteDatabase</i></th><th> the database to clean </th></thead><tbody></tbody></table>

<b>Output:</b> Boolean<br>
<ul><li>True if cleaned completely<br>
</li><li>False if an error occurred<br>
<b>Throws:</b> SQLException if error in database schema</li></ul>

<br>
<pre><code>public boolean cleanDatabase()<br>
</code></pre>
This is a convenience method to call the above version of cleanDatabase.  This function gets a writable copy of the database and passes it to the above function.<br>
<b>Inputs:</b> None<br>
<b>Output:</b> Boolean<br>
<ul><li>True if cleaned completely<br>
</li><li>False if an error occurred<br>
<b>Throws:</b> SQLException if error in database schema</li></ul>

<br>
<pre><code>public boolean exportToFile( File dest )<br>
</code></pre>
This function will export this database to a given file in an xml format.<br>
<b>Inputs:</b>
<table><thead><th> dest (File) </th><th> the file to export to </th></thead><tbody></tbody></table>

<b>Output:</b> Boolean<br>
<ul><li>True if successful<br>
</li><li>False if not<br>
<b>Throws:</b> None</li></ul>

<br>
<pre><code>public Element exportToElement()<br>
</code></pre>
This function will export this database document element.<br>
<b>Inputs:</b> None<br>
<b>Output:</b> Element<br>
<ul><li>The document element containing the database<br>
</li><li>Null if error<br>
<b>Throws:</b> None</li></ul>

<br>
<pre><code>public int importFromFile( File source, boolean append )<br>
</code></pre>
This function will import data from a given xml file into this database.  If append is false, the database will be cleaned before import.  The import will only import the tables/columns that exist in the database and will ignore extra data.  Missing columns (in case of import from older version) will be inserted as null.  If these columns are non-null, the import will fail and an error will be returned. If a constraint is violated, the import will fail and an error will be returned (this can be avoided if 'ON CONFLICT' is specified as IGNORE or REPLACE - see SQLite documentation). It is up to the developer to handle these reported errors as appropriate.<br>
For now, non-null and constraint violations are indistinguishable. However, this may be fixed in the future (insertWithOnConflict is available in Android API Level 8).<br>
<b>Inputs:</b>
<table><thead><th> source (File) </th><th> the file to import from </th></thead><tbody>
<tr><td> append (boolean) </td><td> append to database (false to clean it first) </td></tr></tbody></table>

<b>Output:</b> int - import return code<br>
<b>Throws:</b> None<br>
<br>
<br>
<pre><code>public int importFromElement( Element dbTables, boolean append )<br>
</code></pre>
This function will import data from a given document element.  If append is false, the database will be cleaned before import.  The import will only import the tables/columns that exist in the database and will ignore extra data.  Missing columns (in case of import from older version) will be inserted as null.  If these columns are non-null, the import will fail and an error will be returned. If a constraint is violated, the import will fail and an error will be returned (this can be avoided if 'ON CONFLICT' is specified as IGNORE or REPLACE - see SQLite documentation). It is up to the developer to handle these reported errors as appropriate.<br>
For now, non-null and constraint violations are indistinguishable. However, this may be fixed in the future (insertWithOnConflict is available in Android API Level 8).<br>
<b>Inputs:</b>
<table><thead><th> dbTables (Element) </th><th> document with data in it </th></thead><tbody>
<tr><td> append (boolean) </td><td> append to database (false to clean it first) </td></tr></tbody></table>

<b>Output:</b> int - import return code<br>
<b>Throws:</b> None<br>
<br>
<br>


<h1>Private Methods</h1>
<pre><code>private void fillTableFromXML(SQLiteDatabase db, String thisTable, String[] theseColumns, NodeList myRecords)<br>
</code></pre>
This function will insert data into a database. The database to be inserted into, the table and columns in insert into, and the data to insert.<br>
<b>Inputs:</b>
<table><thead><th> db (SQLiteDatabase) </th><th> the database to insert into </th></thead><tbody>
<tr><td> thisTable (String) </td><td> the table to insert into </td></tr>
<tr><td> theseColumns (String<a href='.md'>.md</a>) </td><td> the columns to insert into the table </td></tr>
<tr><td> myRecords (NodeList) </td><td> the data to fill the table </td></tr></tbody></table>

<b>Output:</b> None<br>
<b>Throws:</b> None<br>
<br>
<br>
<pre><code>private boolean createTable( SQLiteDatabase db, int index) throws SQLException<br>
</code></pre>
This function is a helper function used by the onCreate, onUpgrade/Downgrade functions to create individual tables. This function takes an index into the schema array to the specific table to be created.  This creates all columns and adds any listed constraints.  If created successfully, the function returns true. If the table does not exist in the current version, it is skipped and the function returns false.<br>
This function is automatically by other automatic functions (not called directly).<br>
<b>Inputs:</b>
<table><thead><th> db (SQLiteDatabase) </th><th> the database </th></thead><tbody>
<tr><td> index (int) </td><td> position in schema array of table to create </td></tr></tbody></table>

<b>Output:</b> Boolean<br>
<ul><li>True if table was created<br>
</li><li>False if table was not created (table does not exist in current version)<br>
<b>Throws:</b> SQLException if error in database schema</li></ul>

<br>
<pre><code>private static String docNodeToXMLString(Node root)<br>
</code></pre>
This is a recursive function that will convert an entire document to an xml string.  If necessary, the function will call itself for any children nodes.  If called on the root node, an xml header will be inserted.  Attributes are supported but are not used in the exportToFile function.<br>
<b>Inputs:</b>
Inputs:<br>
<table><thead><th> root (Node) </th><th> the node at which to start building xml </th></thead><tbody></tbody></table>

<b>Output:</b> String  - The xml string representing the document<br>
<b>Throws:</b> None<br>
<br>
<br>
<br>

<h1>Private Variables</h1>
<pre><code>private SQLiteDatabase activeDB<br>
</code></pre>
Used to store the internal reference to the database.<br>