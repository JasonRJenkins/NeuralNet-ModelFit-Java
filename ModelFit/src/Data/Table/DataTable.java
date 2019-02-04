/////////////////////////////////////////////////////////////////////
//
// Implements the Data Table class
//
// Author: Jason Jenkins
//
// This class is a representation of a database table that can be
// easily manipulated for use in mathematical or statistical analysis.
//
// A DataTable object can be created from a file representation of
// a table in .CSV format or by adding rows individually.
//
// Non-numeric values are automatically assigned a numeric alias to 
// help facilitate mathematical analysis of the data. These values
// can be overridden if desired.
//
/////////////////////////////////////////////////////////////////////

package Data.Table;

/////////////////////////////////////////////////////////////////////

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.io.FileOutputStream;
import java.io.IOException;

/////////////////////////////////////////////////////////////////////
/**
 * This class is a representation of a database table that can be
 * easily manipulated for use in mathematical or statistical analysis.
 * 
 * @author Jason Jenkins
 */
public class DataTable
{
    /////////////////////////////////////////////////////////////////////
    // Private Data Members
    /////////////////////////////////////////////////////////////////////
    /**
     * the number of rows in the table
     */
    private int mRows = 0;
    
    /**
     * the number of columns in the table
     */
    private int mCols = 0;
    
    /**
     * true if the table has a header row containing column names
     */
    private boolean mHeader = false;

    /**
     * the column names (if supplied)
     */
    private final ArrayList<String> mColumnNames;

    /**
     * the raw table data - each row is a vector of column values in string format
     * and the table consists of a vector of rows
     */
    private final ArrayList<ArrayList<String>> mRawData;

    /**
     * keeps track of the automatic alias values used by each column
     */
    private final ArrayList<Double> mAliasVec;

    /**
     * maps a string column name to a corresponding numeric vector index
     */
    private final Map<String, Integer> mColIdx;

    /**
     * maps a string name to a numeric value (in string format)
     */
    private final Map<String, String> mAliases;
    
    /////////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////////
    /**
     * default constructor
     */
    public DataTable()
    {
        mColumnNames = new ArrayList<>();
        mRawData = new ArrayList<>();
        mAliasVec = new ArrayList<>();
        mColIdx = new HashMap<>();
        mAliases = new HashMap<>();
    }
    
    /**
     * constructs a DataTable object from a .CSV file representation
     * 
     * @param fName  the name of the .CSV file containing the data
     * @param header set it to true if the data has a header row 
     *               containing column names otherwise set it to false
     */
    public DataTable(String fName, boolean header)
    {
        mRows = 0;
        mCols = 0;
        mHeader = header;
        mColumnNames = new ArrayList<>();
        mRawData = new ArrayList<>();
        mAliasVec = new ArrayList<>();
        mColIdx = new HashMap<>();
        mAliases = new HashMap<>();

        // read in the data from the file stream
        readFromStream(fName, header);

        // initialise the alias vector values to zero
        for(int i = 0; i < mCols; i++)
        {
            mAliasVec.add(0.0);
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////////////////
    /**
     * returns the number of rows in the data table
     * 
     * @return the number of rows in the table
     */
    public int getNumRows()
    {
        return mRows;
    }

    /**
     * returns the number of columns in the data table
     * 
     * @return the number of columns in the table
     */
    public int getNumCols()
    {
        return mCols;
    }

    /**
     * returns true if the data file has a header row
     * 
     * @return true if the data file has a header row
     */
    public boolean getHeader()
    {
        return mHeader;
    }
    
    /**
     * sets the header value for the table
     * 
     * @param header 
     */
    public void setHeader(boolean header)
    {
        mHeader = header;
    }
    
    /////////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////////
    /**
     * clears a DataTable object ready for re-use
     */
    public void clearTable()
    {
        mRows = 0;
        mCols = 0;
        mHeader = false;

        mColumnNames.clear();
        mRawData.clear();
        mColIdx.clear();
        mAliases.clear();
        mAliasVec.clear();
    }

    /**
     * adds a new row to the DataTable
     * 
     * @param row a list of column values in string format
     * 
     * @return 0 if successful otherwise -1
     */
    public int addRawRow(ArrayList<String> row)
    {
        int retVal = 0;

        // check for the very first row to be added
        if (mRows <= 0 && mCols <= 0)
        {
            mRows = 0;
            mCols = row.size();

            // initialise the alias vector values to zero
            for (int i = 0; i < mCols; i++)
            {
                // this keeps track of the automatic alias values used by each column
                mAliasVec.add(0.0);
            }
        }

        if (mCols == row.size())
        {
            mRawData.add(row);
            mRows++;
        }
        else
        {            
            System.out.print("ERROR: You cannot insert a row with: ");
            System.out.println(row.size());
            System.out.print(" columns into a table with: ");
            System.out.print(mCols);
            System.out.println(" columns!");

            retVal = -1;
        }
        
        return retVal;
    }

    /**
     * gets a data row with the values in string format
     * 
     * @param nRow the numeric index of the row to be returned
     * @param row  the row data as a list of string values
     *
     * @return 0 if successful otherwise -1
     */
    public int getRawRow(int nRow, ArrayList<String> row)
    {
        int retVal = 0;

        if ((nRow < 0) || ((nRow + 1) > mRows))
        {
            System.out.print("ERROR: The requested row index: ");
            System.out.print(nRow);
            System.out.println(" is out of bounds!");
            System.out.print("INFORMATION: The table has: ");
            System.out.print(mRows);
            System.out.print(" rows, indexed from 0 to ");
            System.out.print(mRows - 1);
            System.out.println(".");

            retVal = -1;
        }
        else
        {
            row.clear();
            row.addAll(mRawData.get(nRow));
        }

        return retVal;
    }

    /**
     * gets a data row with the values in double format
     * <p>
     * Non-numeric data is automatically assigned a numeric alias if an
     * alias has not already been set up. The first non-numeric entry in 
     * a column is assigned the value 0 the next distinct entry is 
     * assigned the value 1 and the next 2 and so on. You can set your 
     * own alias values using the setAlias method
     * 
     * @param nRow the numeric index of the row to be returned
     * @param row  the row data as a list of double values
     * 
     * @return 0 if successful otherwise -1
     */
    public int getNumericRow(int nRow, ArrayList<Double> row)
    {
        int retVal = 0;

        if ((nRow < 0) || ((nRow + 1) > mRows))
        {
            System.out.print("ERROR: The requested row index: ");
            System.out.print(nRow);
            System.out.println(" is out of bounds!");
            System.out.print("INFORMATION: The table has: ");
            System.out.print(mRows);
            System.out.print(" rows, indexed from 0 to ");
            System.out.print(mRows - 1);
            System.out.println(".");

            retVal = -1;
        }
        else
        {
            // get the row data in raw (string) format
            ArrayList<String> rawRow = mRawData.get(nRow);
            int len = rawRow.size();

            // convert each row element from a string to a numeric value
            for (int i = 0; i < len; i++)
            {
                double value;
                String sVal = rawRow.get(i);

                // check to see if we already have an alias set up
                String sCol = String.valueOf(i);

                // use the string column value and the column index as the alias key
                // this allows multiple aliases for the same value to be set up e.g.
                // in one column you may have values: 'red', 'blue' and 'green' aliased
                // to values 0, 1 and 2 respectively and in a second column you may have 
                // values: 'black', 'yellow' and 'red' aliased to 0, 1 and 2 respectively
                // i.e. two aliases for 'red' are set up, one for each column
                String sKey = sVal + " " + sCol;

                if (mAliases.containsKey(sKey))
                {
                    sVal = mAliases.get(sKey);
                }

                // try the string to value conversion
                try
                {
                    value = Double.parseDouble(sVal);
                }
                catch (NumberFormatException ex)
                {
                    // assign non-numeric column values integer values starting with zero
                    value = mAliasVec.get(i);       // this vector has initial values set to 0
                    mAliasVec.set(i, value + 1);    // the next value to use

                    // create and add the value-alias to the aliases map
                    String strAlias = String.valueOf(value);
                    mAliases.put(sKey, strAlias);
                }

                row.add(value);
            }
        }

        return retVal;
    }

    /**
     * gets a data column with the values in string format
     * 
     * @param nCol the numeric index of the column to be returned
     * @param col  the column data as a list of string values
     * 
     * @return 0 if successful otherwise -1
     */
    public int getRawCol(int nCol, ArrayList<String> col)
    {
        int retVal = 0;

        if ((nCol < 0) || ((nCol + 1) > mCols))
        {
            System.out.print("ERROR: The requested column index: ");
            System.out.print(nCol);
            System.out.println(" is out of bounds!");
            System.out.print("INFORMATION: The table has: ");
            System.out.print(mCols);
            System.out.print(" columns, indexed from 0 to ");
            System.out.print(mCols - 1);
            System.out.println(".");

            retVal = -1;
        }
        else
        {
            // extract the column data
            for (int rows = 0; rows < mRows; rows++)
            {
                ArrayList<String> rowData = mRawData.get(rows);
                col.add(rowData.get(nCol));
            }
        }

        return retVal;
    }

    /**
     * gets a data column with the values in string format
     * 
     * @param sCol the name of the column to be returned
     * @param col  the column data as a list of string values
     * 
     * @return 0 if successful otherwise -1
     */
    public int getRawCol(String sCol, ArrayList<String> col)
    {
        int iCol;
        int retVal = -1;

        // map the string "index" to the actual integer index
        if (mColIdx.containsKey(sCol))
        {
            iCol = mColIdx.get(sCol);

            // use the numeric indexed version of getRawCol
            retVal = getRawCol(iCol, col);
        }
        else
        {
            System.out.print("ERROR: The requested column index string: ");
            System.out.print(sCol);
            System.out.println(" does not have a corresponding numeric index assigned to it!");
        }

        return retVal;
    }

    /**
     * gets a data column with the values in double format
     * <p>
     * Non-numeric data is automatically assigned a numeric alias if an
     * alias has not already been set up. The first non-numeric entry in 
     * a column is assigned the value 0 the next distinct entry is 
     * assigned the value 1 and the next 2 and so on. You can set your 
     * own alias values using the setAlias method
     * 
     * @param nCol the numeric index of the column to be returned
     * @param col  the column data as a list of double values
     * 
     * @return 0 if successful otherwise -1
     */
    public int getNumericCol(int nCol, ArrayList<Double> col)
    {
        int retVal = 0;

        if ((nCol < 0) || ((nCol + 1) > mCols))
        {
            System.out.print("ERROR: The requested column index: ");
            System.out.print(nCol);
            System.out.println(" is out of bounds!");
            System.out.print("INFORMATION: The table has: ");
            System.out.print(mCols);
            System.out.print(" columns, indexed from 0 to ");
            System.out.print(mCols - 1);
            System.out.println(".");

            retVal = -1;
        }
        else
        {
            for (int rows = 0; rows < mRows; rows++)
            {
                double value;
                ArrayList<String> rowData = mRawData.get(rows);
                String sVal = rowData.get(nCol);

                // check to see if we already have an alias set up
                String sCol = String.valueOf(nCol);

                // use the string column value and the column index as the alias key
                // this allows multiple aliases for the same value to be set up e.g.
                // in one column you may have values: 'red', 'blue' and 'green' aliased
                // to values 0, 1 and 2 respectively and in a second column you may have 
                // values: 'black', 'yellow' and 'red' aliased to 0, 1 and 2 respectively
                // i.e. two aliases for 'red' are set up, one for each column
                String sKey = sVal + " " + sCol;

                if (mAliases.containsKey(sKey))
                {
                    sVal = mAliases.get(sKey);
                }

                // try the string to value conversion
                try
                {                    
                    value = Double.parseDouble(sVal);
                }
                catch (NumberFormatException ex)
                {
                    // assign non-numeric column values integer values starting with zero
                    value = mAliasVec.get(nCol);        // this vector has initial values set to 0
                    mAliasVec.set(nCol, value + 1);     // the next value to use

                    // create and add the value-alias to the aliases map
                    String strAlias = String.valueOf(value);
                    mAliases.put(sKey, strAlias);
                }

                col.add(value);
            }
        }

        return retVal;
    }

    /**
     * gets a data column with the values in double format
     * 
     * @param sCol the name of the column to be returned
     * @param col  the column data as a list of double values
     * 
     * @return 0 if successful otherwise -1
     */
    public int getNumericCol(String sCol, ArrayList<Double> col)
    {
        int iCol;
        int retVal = -1;

        // map the string "index" to the actual integer index
        if (mColIdx.containsKey(sCol))
        {
            iCol = mColIdx.get(sCol);

            // use the numeric indexed version of getRawCol
            retVal = getNumericCol(iCol, col);
        }
        else
        {
            System.out.print("ERROR: The requested column index string: ");
            System.out.print(sCol);
            System.out.println(" does not have a corresponding numeric index assigned to it!");
        }

        return retVal;
    }

    /**
     * sets a numeric alias for a given string value and column index
     * 
     * @param sValue the value to be given an alias
     * @param dAlias the alias to be applied
     * @param nCol   the column index of the column containing the value
     */
    public void setAlias(String sValue, double dAlias, int nCol)
    {
        String sAlias = String.valueOf(dAlias);

        // check to see if we have an alias set up
        String sCol = String.valueOf(nCol);

        // using the string value and the column index as the alias key
        // allows multiple aliases for the same value to be set up e.g.
        // in one column you may have values: 'red', 'blue' and 'green' aliased
        // to values 0, 1 and 2 respectively and in a second column you may have 
        // values: 'black', 'yellow' and 'red' aliased to 0, 1 and 2 respectively
        // i.e. two aliases for 'red' are set up, one for each column
        String sKey = sValue + " " + sCol;

        if (mAliases.containsKey(sKey))
        {
            // replace the pre-existing alias
            mAliases.remove(sKey);
            mAliases.put(sKey, sAlias);
        }
        else
        {
            mAliases.put(sKey, sAlias);
        }
    }

    /**
     * returns the numeric alias for a given string value and column index
     * 
     * @param sValue the string value whose alias is required
     * @param nCol   the column index of the column containing the value
     * 
     * @return the alias value if it exists otherwise -1e10
     */
    public double getAliasValue(String sValue, int nCol)
    {
        double value = -1e10;

        String sCol = String.valueOf(nCol);

        // using the string value and the column index as the alias key
        // allows multiple aliases for the same value to be set up e.g.
        // in one column you may have values: 'red', 'blue' and 'green' aliased
        // to values 0, 1 and 2 respectively and in a second column you may have 
        // values: 'black', 'yellow' and 'red' aliased to 0, 1 and 2 respectively
        // i.e. two aliases for 'red' are set up, one for each column
        String sKey = sValue + " " + sCol;

        if (mAliases.containsKey(sKey))
        {
            String sVal = mAliases.get(sKey);

            // try the string to value conversion
            try
            {
                value = Double.parseDouble(sVal);
            }
            catch (NumberFormatException ex)
            {
                // the alias has been set up incorrectly
                System.out.print("ERROR: The alias value: ");
                System.out.print(sVal);
                System.out.print(" for the string: ");
                System.out.print(sValue);
                System.out.println(" cannot be converted to a numeric value!");
            }
        }
        else
        {
            // an alias has not been set up
            System.out.print("ERROR: The string: ");
            System.out.print(sValue);
            System.out.println(" does not have an alias!");
        }

        return value;
    }

    /**
     * returns the column index for a given column name
     * 
     * @param sColName the name of the column to be returned
     * 
     * @return the integer index of the column or -1 if it doesn't exist
     */
    public int getColIndex(String sColName)
    {
        int iCol = -1;

        // map the string "index" to the actual integer index
        if (mColIdx.containsKey(sColName))
        {
            iCol = mColIdx.get(sColName);
        }

        return iCol;
    }

    /**
     * gets the column names list
     * 
     * @param colNames the list containing the column names
     */
    public void getColumnNames(ArrayList<String> colNames)
    {
        if (mHeader == true)
        {
            colNames.clear();
            colNames.addAll(mColumnNames);
        }
    }

    /**
     * sets the column names list
     * 
     * @param colNames the list containing the column names
     */
    public void setColumnNames(ArrayList<String> colNames)
    {
        int size = colNames.size();

        if (mCols <= 0)
        {
            // we have an empty table
            mColumnNames.clear();
            mColumnNames.addAll(colNames);
            mCols = size;
            mHeader = true;
        }
        else if (mCols == size)
        {
            // a straight replacement for the column names
            mColumnNames.clear();
            mColumnNames.addAll(colNames);
            mHeader = true;
        }
        else
        {
            System.out.print("ERROR: The number of supplied column names: ");
            System.out.print(size);
            System.out.print(" is not compatible with the number of columns in the table: ");
            System.out.print(mCols);
            System.out.println("!");
        }
    }

    /**
     * write this DataTable object to a .CSV file
     * 
     * @param fName the name of the file to write the data to
     * 
     * @return 0 if successful otherwise -1
     */
    public int writeToFile(String fName)
    {
        try (FileOutputStream ofstream = new FileOutputStream(fName))
        {
            writeToStream(ofstream);

            // tidy up
            ofstream.close();
        }
        catch (IOException ex)
        {
            System.out.print("ERROR: Writing to file - unable to open or create the file: ");
            System.out.println(fName);
            System.out.println();
            System.out.println(ex.getMessage());
            System.out.println();
            
            return -1;
        }

        return 0;
    }

    /**
     * clears and re-instantiates this DataTable object from a .CSV file representation
     * 
     * @param fName  the name of the .CSV file containing the data
     * @param header true if the data has a header row containing 
     *               column names otherwise set to false
     * 
     * @return 0 if successful otherwise -1
     */
    public int readFromFile(String fName, boolean header)
    {
        int retVal = 0;

        clearTable();
        readFromStream(fName, header);

        // set the table header parameter
        setHeader(header);
        
        // initialise the alias vector values to zero
        for (int i = 0; i < mCols; i++)
        {
            mAliasVec.add(0.0);
        }

        // check to see if there was an error reading the file
        if (mRows < 0 || mCols < 0)
        {
            retVal = -1;
        }

        return retVal;
    }

    /////////////////////////////////////////////////////////////////////
    // Private Methods
    /////////////////////////////////////////////////////////////////////
    /**
     * outputs the table data via a file output stream
     * 
     * @param ofstream the given file output stream
     */
    private void writeToStream(FileOutputStream ofstream)
    {
        String comma = ",";
        String newLine = "\n";
        
        try
        {
            // output the header
            if (mHeader == true)
            {
                int size = mColumnNames.size();

                for (int i = 0; i < (size - 1); i++)
                {
                    // the data is comma delimited
                    ofstream.write(mColumnNames.get(i).getBytes());
                    ofstream.write(comma.getBytes());
                }

                ofstream.write(mColumnNames.get(size - 1).getBytes());
                ofstream.write(newLine.getBytes());
            }

            // output the data
            for (int row = 0; row < mRows; row++)
            {
                ArrayList<String> sRow =  mRawData.get(row);

                for (int col = 0; col < mCols; col++)
                {
                    String sCol = sRow.get(col);

                    ofstream.write(sCol.getBytes());

                    if (col < (mCols - 1))
                    {
                        // the data is comma delimited
                        ofstream.write(comma.getBytes());
                    }
                }

                ofstream.write(newLine.getBytes());
            }

            ofstream.close();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    /**
     * reads a newline as a string from the given input stream
     * 
     * @param fis the given file input stream
     * 
     * @return a string containing the line read from the stream
     */
    private String readLine(FileInputStream fis)
    {
        boolean read = false;
        StringBuilder outStr = new StringBuilder();

        try
        {
            while (!read)
            {
                int ch = fis.read();
                
                // check for eof
                if (ch >= 0)
                {
                    char character = (char)ch;

                    if (character == '\n')
                    {
                        read = true;
                    }
                    else
                    {
                        outStr.append(character);
                    }
                }
                else
                {
                    read = true;
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
            outStr = new StringBuilder();
        }

        return outStr.toString();
    }
    
    /**
     * reads in the table data from a file input stream
     * 
     * @param fName  the name of the file to read the data from
     * @param header set to true if the input file has a header row 
     *               containing column names otherwise set to false
     */
    private void readFromStream(String fName, boolean header)
    {            
        File file = new File(fName);

        if (file.length() == 0)            
        {
            System.out.println();
            System.out.print("ERROR: Reading from file - unable to open the file: ");
            System.out.println(fName);
            
            // set the number of rows and columns to invalid values to signify an error
            mRows = -1;
            mCols = -1;
        }
        else
        {
            try (FileInputStream inStream = new FileInputStream(file))
            {
                // read in the header data if it is available
                if (header == true)
                {
                    String headline = readLine(inStream).trim();
                    
                    if (headline.length() != 0)
                    { 
                        boolean nextLine = false;
                        int idx = 0;
                        int sLen = headline.length();

                        if (sLen > 0)
                        {                            
                            while (!nextLine)
                            {
                                boolean nextValue = false;
                                String sValue = new String();
                                
                                while (!nextValue)
                                {
                                    String sChar = headline.substring(idx, idx + 1);

                                    // the data is comma delimited
                                    if (",".equals(sChar))
                                    {
                                        nextValue = true;
                                    }
                                    else if(!"\"".equals(sChar))
                                    {
                                        // quotes can be used to delimit strings - so ignore them
                                        sValue += sChar;
                                    }

                                    idx++;

                                    if (idx >= sLen)
                                    {
                                        nextLine = true;
                                        nextValue = true;
                                    }
                                }

                                // trim leading and trailing whitespace
                                if (sValue.length() != 0)
                                {
                                    sValue = sValue.trim();
                                }

                                mColumnNames.add(sValue);
                                mColIdx.put(sValue, mCols);
                                mCols++;
                            }
                        }
                    }
                }

                // read in the data
                String line = readLine(inStream).trim();
                
                while (line.length() != 0)
                {			
                    ArrayList<String> row;
                    row = new ArrayList<>();

                    boolean nextLine = false;
                    int idx = 0;
                    int sLen = line.length();

                    if (sLen > 0)
                    {		
                        boolean discardLine = false;
                        String sValue = "";

                        while (!nextLine)
                        {
                            boolean nextValue = false;

                            while (!nextValue)
                            {
                                String sChar = line.substring(idx, idx + 1);

                                // the data is comma delimited
                                if (",".equals(sChar))
                                {
                                    nextValue = true;
                                }
                                else if (!"\"".equals(sChar))
                                {
                                    // quotes can be used to delimit strings - so ignore them
                                    sValue += sChar;
                                }

                                idx++;

                                // check for end of line
                                if (idx >= sLen)
                                {
                                    nextLine = true;
                                    nextValue = true;
                                }
                            }

                            // trim leading and trailing whitespace
                            if (sValue.length() != 0)
                            {
                                sValue = sValue.trim();
                            }

                            // reject data rows with missing data - identified by "?"
                            if (!"?".equals(sValue))
                            {
                                row.add(sValue);
                                sValue = "";
                            }
                            else
                            {
                                discardLine = true;
                                nextLine = true;
                            }
                        }

                        if (!discardLine)
                        {
                            mRawData.add(row);
                            mRows++;
                        }

                        // if a header is not supplied set the number of columns
                        if (mCols == 0 && header == false && !discardLine)
                        {
                            mCols = row.size();
                        }

                        // check that the row sizes are consistent
                        if (mCols != row.size() && !discardLine)
                        {
                            System.out.print("ERROR: Reading from file - the data in the file: ");
                            System.out.print(fName);
                            System.out.println(" does not maintain a consistent number of columns!");

                            // set the number of rows and columns to invalid values to signify an error
                            mRows = -1;
                            mCols = -1;

                            return;
                        }
                    }

                    // read the next line
                    line = readLine(inStream).trim();
                }

                // tidy up
                inStream.close();
            }
            catch (IOException ex)
            {
                System.out.print("ERROR: Reading from file: ");
                System.out.println(fName);
                System.out.println();
                System.out.println(ex.getMessage());
                System.out.println();

                // set the number of rows and columns to invalid values to signify an error
                mRows = -1;
                mCols = -1;            
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////
}

/////////////////////////////////////////////////////////////////////
