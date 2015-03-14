If you have any questions of need any clarification on any of the following, leave a comment and I will update as necessary.  This should handle almost all simple uses for this library but may not include everything you can do.

## Schema syntax ##
```
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
```

## Example implementation ##
```
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
```
This bit of code will create two tables, **people** and **companies**.  Here is the timeline for how this was done:

**People** was introduced in version 1 of the database with only the _first`_`name_ and _last`_`name_ columns.  A unique was constraint was put on the table based on these two columns.  On conflict, insert and update statements are simply ignored.

In version 2, the developer added _middle`_`init_.

In version 3, the developer added the **Companies** table with _`_`id_ and _name_ columns.  A link to this table was also added to the **People** table so they could be tied together (a foreign key).

In version 4, the developer added _description_ to the **Companies** table.

In version 5, the developer added _salary_ to the **People** table.

## Other Considerations: ##
### Primary key columns ###
These should normally be called _id as most Android-provided helper functions use this as a hard-coded primary key.
### Column declarations ###
Can follow any of the syntax listed here: [http://sqlite.org/syntaxdiagrams.html#column-def](http://sqlite.org/syntaxdiagrams.html#column-def)
### Table constraints ###
Can follow any of the syntax listed here: [http://sqlite.org/syntaxdiagrams.html#table-constraint](http://sqlite.org/syntaxdiagrams.html#table-constraint)_

If you want a combination of columns to define a unique record, as with first\_name and last\_name in the example, this is where you do it.  Plan and code for the conflict cause if applicable.

**Note:** table constraints can only be created when the table is first implemented.  They can never up changed once the table exists.  This is a limitation of SQLite.  Developers can create an onUpgrade override to handle this though.
### Check all syntax ###
Most if not all functions will fail if correct SQLite syntax is not used for all columns and constraints.
### You can plan ahead ###
This library gives you the ability to create the schema for future versions but not implement them.  This is not necessarily recommended as it can get confusing but you should be protected if you wish to do so.