<h1>SuperSQLiteOpenHelper</h1>
SuperSQLiteOpenHelper is a Android library that extends the basic SQLiteOpenHelper for ease of use and automatic management.

Automatic database version management is handled from a simple to read, standard format. It also includes build in functions to import and export the database to and from a DOM Element (or directly to/from a file). It also includes several database helpers for general use. This library was written for Android 2.1 (API 7).

To use, a developer can extend the class (as with the normal SQLiteOpenHelper) and add custom interface methods specific to their database.

<h1>Example Implementation</h1>

This is the source code for an example implementation for this library.

    import com.diy.developer.android_library.SuperSQLiteOpenHelper;

    import android.content.ContentValues;
    import android.content.Context;
    import android.database.Cursor;

    public class myDB extends SuperSQLiteOpenHelper {
        private static final String DATABASE_NAME = "My_Database";
        private static final int DATABASE_VERSION = 1;
        private static final String[][][] DATABASE_SCHEMA = {
                {       {"table1","1"},
                        // v1
                        {"_id INTEGER primary key autoincrement",
                        "col1 TEXT",
                        "col2 TEXT",
                        "col3 TEXT"}
                }
        };
        
        myDB (Context context) {
                super(context, DATABASE_NAME, DATABASE_VERSION, DATABASE_SCHEMA);
        }
    }

There, it's that simple. I recommend that you also add custom interfaces to your database as you normally would do but this certainly cuts down on the initial development. Here is an example of custom interfaces that could apply to the database above. Here, I use Widget as some generic object you might be trying to represent/save using the database.

    public long insertWidget( Widget item ) {
      ContentValues initialValues = new ContentValues();
      // get values from object to store in table
      initialValues.put("col1", item.someProperty() );
      // ...
                
      useWritableDatabase();
      return getActiveDB().insert("table1", null, initialValues);
      close();
    }
        
    public Object getWidget(long myID) {
        useReadableDatabase();
        Cursor results = getActiveDB().query(
          "table1",
          null,
          "_id = (?)",
          new String[]{ Long.toString(myID) },
          null,
          null,
          "CarID"
    );
    
    // turn record into Widget object
    Widget rval = new Widget();
      rval.setSomeProperty( valueFromDatabase );
      
      close();
      
      return rval;
    }
        
    public long deleteWidget(long myID) {
      useWritableDatabase();
      long tmp = getActiveDB().delete("table1", "_id = " + Long.toString(myID), null);
      close();
      
      return tmp;
    }

If you have any questions of need any clarification on any of the following, leave a comment and I will update as necessary.  This should handle almost all simple uses for this library but may not include everything you can do.

<h1>Schema syntax</h1>

    DATABASE_SCHEMA = {
    	{	{"table_name","first_version","table_constrains"},
    		// v1
    		{"column1 TYPE",
    		...
    		},
    		// v2
    		{"column4 TYPE"},
    		...
    	},
    	...
    };


<h2>Detailed Example</h2>
    DATABASE_SCHEMA = {
	    {	{"people","1","CONSTRAINT (first_name,last_name) ON CONFLICT IGNORE"},
    		// v1
    		{"_id INTEGER primary key autoincrement",
    		"first_name TEXT",
    		"last_name TEXT"},
    		// v2
    		{"middle_init TEXT"},
    		// v3
    		{"company_id INTEGER"},
    		// v4
    		{},
    		//v5
    		{"salary INTEGER"}
	    },
	    {	{"companies","3"},
		    // v1-2
		    {},{},
		    // v3
		    {"_id INTEGER primary key autoincrement",
		    "name TEXT"},
		    // v4
		    {"description TEXT"}
	    }
    };

This bit of code will create two tables, <b>people</b> and <b>companies</b>.  Here is the timeline for how this was done:

<b>People</b> was introduced in version 1 of the database with only the *first_name* and *last_name* columns.  A unique was constraint was put on the table based on these two columns.  On conflict, insert and update statements are simply ignored.

In version 2, the developer added *middle_init*.

In version 3, the developer added the <b>Companies</b> table with *id* and *name* columns.  A link to this table was also added to the <b>People</b> table so they could be tied together (a foreign key).

In version 4, the developer added *description* to the <b>Companies</b> table.

In version 5, the developer added *salary* to the <b>People</b> table.

<h2>Other Considerations</h2>
<h3>Primary key columns</h3>
These should normally be called *id* as most Android-provided helper functions use this as a hard-coded primary key.

<h3>Column declarations</h3>
Can follow any of the syntax listed here: http://sqlite.org/syntaxdiagrams.html#column-def

<h3>Table constraints</h3>
Can follow any of the syntax listed here: http://sqlite.org/syntaxdiagrams.html#table-constraint

If you want a combination of columns to define a unique record, as with *first_name* and *last_name* in the example, this is where you do it.  Plan and code for the conflict cause if applicable.

<b>Note:</b> table constraints can only be created when the table is first implemented.  They can never up changed once the table exists.  This is a limitation of SQLite.  Developers can create an onUpgrade override to handle this though.

<h3>Check all syntax</h3>
Most if not all functions will fail if correct SQLite syntax is not used for all columns and constraints.

<h3>You can plan ahead</h3>
This library gives you the ability to create the schema for future versions but not implement them.  This is not necessarily recommended as it can get confusing but you should be protected if you wish to do so.
