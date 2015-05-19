import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;




public class InitializeSoar {
	/**
	 * If value is m,means m*m squares(grid) with m*m elements in each square(grid),
	 * m*m rows,m*m columns and m*m elements in each row/column and values possible in
	 * each cell is [1..m*m]. If this variable is changed, change CREATE_PRINT_FILE
	 * boolean variable true to true for the first execution.
	 */
	public static final int SUDOKU_INT = 3;
	
	/**
	 * No of rows = No of columns = No of grids
	 */
	public static final int SUDOKU_INTXINT = (int) Math.pow(SUDOKU_INT, 2);
	
	/**
	 * Print file creation only required if SUDOKU_INT or SOAR_RULE_FOLDER is changed. ie previous sudoku test file
	 * was a 9x9 and current is not 9x9 or destination soar rule folder is changed. If changed, change false to true.
	 */
	public static final Boolean CREATE_PRINT_FILE = false;
	
	/**
	 * Please make changes here to specify the test case and destination file/folder locations.
	 */
	//File and Folder Locations
	/**
	 * Folder where the test case and headers/footers for generating soar files are kept. 
	 */
	public static final String TEST_CASE_AND_HEADERS_FOOTERS_FOLDER = "/home/nithin/Study/Eclipse workspace/soar/src/";
	/**
	 * The folder where soar rules are kept. If this variable is changed, change CREATE_PRINT_FILE
	 * boolean variable true to true for the first execution.
	 */
	public static final String SOAR_RULE_FOLDER = "/home/nithin/Study/KRR/SOAR/SoarSuite_9.4.0-Linux_32bit/Agents/Sudoku/";
	/**
	 * The name of the file used to initialize sudoku kept in SOAR_RULE_FOLDER
	 */
	public static final String DEST_SOAR_INITIALIZE_RULE_FILE_NAME = "initialize-Sudoku.soar";
	/**
	 * The name of the file used to print final sudoku contents kept in SOAR_RULE_FOLDER/elaborations
	 */
	public static final String DEST_SOAR_PRINT_RULE_FILE_NAME = "print_Sudoku.soar";
	/**
	 * The name of the test case file kept in TEST_CASE_AND_HEADERS_FOOTERS_FOLDER
	 */
	public static final String TEST_CASE_FILE_NAME = "test0.txt";
	
	//End of variables that can be changed by user.
	
	public static final Integer NO_INT = 0;
	
	public static final String NEW_LINE = System.getProperty("line.separator");
	public static final String STATE_VARIABLE ="<state>";
	public static final String MSG_FORMAT_CELL = "(" + STATE_VARIABLE +  " ^cell <cell{0}>)";

	public static final String MSG_FORMAT_COUNTER_REGION = "(" + STATE_VARIABLE +  " ^counter <counter-{0}-{1}-digit-{2}" + ">)";
	
	public static final String MSG_FORMAT_CELL_WITH_ATTRS = "(<cell{0}> ^row {1} ^col {2} ^grid {3} {4})";
	
	
	/**
	 * 0 - row/col/grid
	 * 1 - which row/col/grid
	 * 2 - digit
	 * 3 - row/col/grid keyword attribute
	 * 4 - which row/col/grid
	 * 5 - digit
	 * 6 - count
	 */
	public static final String MSG_FORMAT_COUNTER_WITH_ATTRS = "(<counter-{0}-{1}-digit-{2}> ^{3} {4} ^digit {5} ^count {6})";
	public static final String VALUE_ATTRIBUTE_KEYWORD = "^value";
	public static final String PROBABLE_ATTRIBUTE_KEYWORD = "^probable";
	public static final String COUNT_ATTRIBUTE_KEYWORD = "^count";
	
	public static final String MSG_FORMAT_CELL_WITH_ATTRS_PRINTING = "(<cell{0}> ^row {1} ^col {2} " +
			VALUE_ATTRIBUTE_KEYWORD + " <value{3}>)";
	
	public static final String PRINT_LINE_MSG_FORMAT = "(write (crlf)  | {0} |  )";
	
	public static Map<Integer, Set<Integer>> possibleValues = new LinkedHashMap<Integer, Set<Integer>>();
	public static Map<Integer, Boolean> isChangeOccured = new LinkedHashMap<Integer, Boolean>();
	
	public static void main(String[] args) {
		handleInitializeSudoku();
		if(CREATE_PRINT_FILE)
			handlePrintSudoku();
	}

	private static void handlePrintSudoku() {
		BufferedWriter writer =null;
		try {
			writer = new BufferedWriter( new FileWriter(SOAR_RULE_FOLDER+"elaborations/"+DEST_SOAR_PRINT_RULE_FILE_NAME));
			StringBuilder builder = new StringBuilder();
			
			//write header
			File headerPrintFile = new File(TEST_CASE_AND_HEADERS_FOOTERS_FOLDER + "header_printSudoku.txt");
			String fileContents = getFileContents(headerPrintFile);
			builder.append(fileContents);
			builder.append(NEW_LINE);
			builder.append(NEW_LINE);
			
			//write cell contents
			builder.append(getCellContentsToWriteForParentState());
			
			builder.append(NEW_LINE);
			builder.append(NEW_LINE);
			
			//write cell contents for each cell
			builder.append(getCellContentsWithAttributesForPrinting());
			builder.append(NEW_LINE);
			
			//arrow seperator
			builder.append("-->");
			builder.append(NEW_LINE);
			builder.append(NEW_LINE);
			
			builder.append(getPrintStatements());
			
			builder.append(NEW_LINE);
			
			File footerPrintFile = new File(TEST_CASE_AND_HEADERS_FOOTERS_FOLDER + "footer_printSudoku.txt");
			fileContents = getFileContents(footerPrintFile);
			//String dimension = SUDOKU_INTXINT + "X" + SUDOKU_INTXINT;
			//String testCaseFileName = getFileNameWithoutExtension(TEST_CASE_FILE_NAME);
			//String format = MessageFormat.format(fileContents, dimension,testCaseFileName);
			builder.append(fileContents);
			
			writer.write(builder.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getPrintStatements() {
		StringBuilder builder = new StringBuilder();
		for(int row = 0;row < SUDOKU_INTXINT;row++) {
			StringBuilder lineBuilder = new StringBuilder();
			
			for(int col = 0;col < SUDOKU_INTXINT;col++) {
				String str = row + "" + col;
				lineBuilder.append("|");
				lineBuilder.append("<value");
				
				lineBuilder.append(str);
				
				lineBuilder.append(">");
				lineBuilder.append("|");
				lineBuilder.append("  ");
			}
			String format = MessageFormat.format(PRINT_LINE_MSG_FORMAT, lineBuilder.toString());
			builder.append(format);
			builder.append(NEW_LINE);
		}
		return builder.toString();
	}

	private static void handleInitializeSudoku() {
		readFileForInitializeSudoku();
		eliminateRedundantValues();
		writeFileForInitializeSudoku();
	}

	public static void writeFileForInitializeSudoku() {
		BufferedWriter writer =null;
		try {
			writer = new BufferedWriter( new FileWriter(SOAR_RULE_FOLDER+DEST_SOAR_INITIALIZE_RULE_FILE_NAME));
			StringBuilder builder = new StringBuilder();
			
			//write header
			File headerInitializeSudoku = new File(TEST_CASE_AND_HEADERS_FOOTERS_FOLDER + "header_InitializeSudoku.txt");
			String fileContents = getFileContents(headerInitializeSudoku);
			builder.append(fileContents);
			
			//builder.append(" ");
			builder.append(" ^filename ");
			builder.append(getFileNameWithoutExtension(TEST_CASE_FILE_NAME));
			
			builder.append(" ^dimension ");
			builder.append(SUDOKU_INTXINT + "X" + SUDOKU_INTXINT);
			builder.append(")");
			
			builder.append(NEW_LINE);
			builder.append(NEW_LINE);
			
			//write cell contents
			builder.append(getCellContentsToWriteForParentState());
			
			builder.append(NEW_LINE);
			builder.append(NEW_LINE);
			
			//write counter contents for row/column/grid
			for (Region region : Region.values()) {
				builder.append(getCounterContentsForRegionForParentState(region));
				builder.append(NEW_LINE);
				builder.append(NEW_LINE);
			}
			
			builder.append(NEW_LINE);
			
			//write cell contents for each cell
			builder.append(getCellContentsWithAttributes());
			builder.append(NEW_LINE);
			builder.append(NEW_LINE);
			
			//write counter contents  with attributes
			
			for (Region region : Region.values()) {
				builder.append(getCounterContentsWithAttributesForRegion(region));
				builder.append(NEW_LINE);
				builder.append(NEW_LINE);
			}
			
			builder.append("}");
			writer.write(builder.toString());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final String getCellContentsWithAttributes() {
		StringBuilder builder = new StringBuilder();
		for(int row = 0;row < SUDOKU_INTXINT;row++) {
			for(int col = 0;col < SUDOKU_INTXINT;col++) {
				String str = row + "" + col;
				String valueAndProbableStr = getValueAndCountAndProbableStr(row, col);
				String formattedStr = MessageFormat.format(MSG_FORMAT_CELL_WITH_ATTRS,
						str,row,col,getGridNo(row, col),valueAndProbableStr);
				builder.append(formattedStr);
				builder.append(NEW_LINE);
			}
			builder.append(NEW_LINE);
		}
		return builder.toString();
	}
	
	public static final String getCellContentsWithAttributesForPrinting() {
		StringBuilder builder = new StringBuilder();
		for(int row = 0;row < SUDOKU_INTXINT;row++) {
			for(int col = 0;col < SUDOKU_INTXINT;col++) {
				String str = row + "" + col;
				String formattedStr = MessageFormat.format(MSG_FORMAT_CELL_WITH_ATTRS_PRINTING,
						str,row,col,str);
				builder.append(formattedStr);
				builder.append(NEW_LINE);
			}
			builder.append(NEW_LINE);
		}
		return builder.toString();
	}
	
	public static String getValueAndCountAndProbableStr(int row,int column) {
		StringBuilder builder = new StringBuilder();
		Integer cellNo = getCellNo(row, column);
		Set<Integer> set = possibleValues.get(cellNo);
		if(set.size() == 1) {
			//fixed value
			builder.append(VALUE_ATTRIBUTE_KEYWORD);
			builder.append(" ");
			for (Integer integer : set) {
				builder.append(integer);
			}
			
			builder.append(" ");
			builder.append(COUNT_ATTRIBUTE_KEYWORD);
			builder.append(" ");
			builder.append(0);
		} else {
			builder.append(VALUE_ATTRIBUTE_KEYWORD);
			builder.append(" ");
			builder.append(NO_INT);
			
			builder.append(" ");
			builder.append(COUNT_ATTRIBUTE_KEYWORD);
			builder.append(" ");
			builder.append(set.size());
			
			for (Integer integer : set) {
				builder.append(" ");
				builder.append(PROBABLE_ATTRIBUTE_KEYWORD);
				builder.append(" ");
				builder.append(integer);
			}
		}
		return builder.toString();
	}
	
	public static String getCellContentsToWriteForParentState() {
		StringBuilder builder = new StringBuilder();
		for(int row = 0;row < SUDOKU_INTXINT;row++) {
			for(int col = 0;col < SUDOKU_INTXINT;col++) {
				String str = row + "" + col;
				String formattedStr = MessageFormat.format(MSG_FORMAT_CELL, str);
				builder.append(formattedStr);
				builder.append(NEW_LINE);
			}
			builder.append(NEW_LINE);
		}
		return builder.toString();
	}
	
	public static String getCounterContentsForRegionForParentState(Region region) {
		StringBuilder builder = new StringBuilder();
		for(int regionItr = 0;regionItr < SUDOKU_INTXINT;regionItr++) {
			for(int digit = 1;digit <= SUDOKU_INTXINT;digit++) {
				String formattedStr = MessageFormat.format(MSG_FORMAT_COUNTER_REGION, region.name,regionItr,digit);
				builder.append(formattedStr);
				builder.append(NEW_LINE);
			}
			builder.append(NEW_LINE);
		}
		return builder.toString();
	}
	
	public static String getCounterContentsWithAttributesForRegion(Region region) {
		//<counter-col-8-digit-1>
		
		StringBuilder builder = new StringBuilder();
		for(int regionItr = 0;regionItr < SUDOKU_INTXINT;regionItr++) {
			for(int digit = 1;digit <= SUDOKU_INTXINT;digit++) {
				int count = 0;
				switch (region) {
				case COLUMN:
					//regionItr = columnNo
					//iterate thru row
					for(int rowNo = 0;rowNo < SUDOKU_INTXINT;rowNo++) {
						Integer cellNo = getCellNo(rowNo, regionItr);
						Set<Integer> set = possibleValues.get(cellNo);
						if(set.contains(digit)) {
							count++;
						}
					}
					break;
				case GRID:
					//regionItr = gridNo
					int rowStart = (regionItr/SUDOKU_INT)*SUDOKU_INT;
					int colStart = (regionItr%SUDOKU_INT)*SUDOKU_INT;
					for (int rowItr = rowStart; rowItr < rowStart + SUDOKU_INT; rowItr++) {
						for (int colItr = colStart; colItr < colStart + SUDOKU_INT; colItr++) {
							Integer cellInGrid = getCellNo(rowItr, colItr);
							Set<Integer> possibleCellValuesGridItr = possibleValues.get(cellInGrid);
							if(possibleCellValuesGridItr.contains(digit)) {
								count++;
							}
							//possibleCellValuesGridItr.remove(intInCell);
							
						}
					}
					break;
				case ROW:
					//regionItr = rowNo
					//iterate thru column
					for(int colNo = 0;colNo < SUDOKU_INTXINT;colNo++) {
						Integer cellNo = getCellNo(regionItr, colNo);
						Set<Integer> set = possibleValues.get(cellNo);
						if(set.contains(digit)) {
							count++;
						}
					}
					break;
				}
				/**
				 * 0 - row/col/grid
				 * 1 - which row/col/grid
				 * 2 - digit
				 * 3 - row/col/grid keyword attribute
				 * 4 - which row/col/grid
				 * 5 - digit
				 * 6 - count
				 */
				String formattedStr = MessageFormat.format(MSG_FORMAT_COUNTER_WITH_ATTRS,
						region.name,regionItr,digit,
						region.name,regionItr,digit,count);
				builder.append(formattedStr);
				builder.append(NEW_LINE);
			}
			builder.append(NEW_LINE);
		}
		return builder.toString();
	}
	/**
	 * Function is not called.
	 */
	public static void eliminateRedundantValues() {
		for(int row = 0;row < SUDOKU_INTXINT;row++) {
			for(int col = 0;col < SUDOKU_INTXINT;col++) {
				eliminateRedundantValues(row,col);
			}
		}
	}

	public static void eliminateRedundantValues(int row, int col) {
		Integer cellNo = getCellNo(row, col);
		if(!isChangeOccured.get(cellNo)) {
			return;
		}
		//System.out.println("eliminateRedundantValues for row:"+row+" colNo:"+col);
		
		Set<Integer> possibleCellValues = possibleValues.get(cellNo);
		Set<Integer> changedCellNos = new HashSet<Integer>();
		if(possibleCellValues.size() == 1) {
			Integer intInCell = possibleCellValues.iterator().next();
			//go thru other row values
			for (int i = 0; i < SUDOKU_INTXINT; i++) {
				if(i == col) {
					continue;
				}
				Integer rowCellItr = getCellNo(row, i);
				Set<Integer> possibleCellValuesRowItr = possibleValues.get(rowCellItr);
				if(possibleCellValuesRowItr.contains(intInCell)) {
					possibleCellValuesRowItr.remove(intInCell);
					isChangeOccured.put(rowCellItr, true);
					if(possibleCellValuesRowItr.size() == 1) {
						changedCellNos.add(rowCellItr);
					}
				}
				
			}
			//go thru other col values
			for (int i = 0; i < SUDOKU_INTXINT; i++) {
				if(i == row) {
					continue;
				}
				Integer colCellItr = getCellNo(i, col);
				Set<Integer> possibleCellValuesColItr = possibleValues.get(colCellItr);
				if(possibleCellValuesColItr.contains(intInCell)) {
					possibleCellValuesColItr.remove(intInCell);
					isChangeOccured.put(colCellItr, true);
					if(possibleCellValuesColItr.size() == 1) {
						changedCellNos.add(colCellItr);
					}
				}
				//possibleCellValuesColItr.remove(intInCell);
				
			}
			//go thru other grid values
			int gridNo = getGridNo(row, col);
			int rowStart = (gridNo/SUDOKU_INT)*SUDOKU_INT;
			int colStart = (gridNo%SUDOKU_INT)*SUDOKU_INT;
			for (int rowItr = rowStart; rowItr < rowStart + SUDOKU_INT; rowItr++) {
				for (int colItr = colStart; colItr < colStart + SUDOKU_INT; colItr++) {
					if(rowItr == row && colItr == col) {
						continue;
					}
					Integer cellInGrid = getCellNo(rowItr, colItr);
					Set<Integer> possibleCellValuesGridItr = possibleValues.get(cellInGrid);
					if(possibleCellValuesGridItr.contains(intInCell)) {
						possibleCellValuesGridItr.remove(intInCell);
						isChangeOccured.put(cellInGrid, true);
						if(possibleCellValuesGridItr.size() == 1) {
							changedCellNos.add(cellInGrid);
						}
					}
					//possibleCellValuesGridItr.remove(intInCell);
					
				}
			}
			isChangeOccured.put(cellNo, false);
			for (Integer cellNoItr : changedCellNos) {
				eliminateRedundantValues(getRowNo(cellNoItr),getColNo(cellNoItr));
			}
		}
	}
	
	public static int getRowNo(int cellNo) {
		return cellNo/SUDOKU_INTXINT;
	}
	
	public static int getColNo(int cellNo) {
		return cellNo%SUDOKU_INTXINT;
	}
	
	public static int getGridNo(int row,int column) {
		return (row/SUDOKU_INT)*SUDOKU_INT + column/SUDOKU_INT;
	}
	
	public static String getFileContents(File file) {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null) {
				builder.append(line);
				builder.append(NEW_LINE);
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
	private static void readFileForInitializeSudoku() {
		//String testCaseFileStr = "test1.txt";
		//String initSoarFileNameStr = "";
		
		String testCaseFileFullStr = TEST_CASE_AND_HEADERS_FOOTERS_FOLDER + TEST_CASE_FILE_NAME;
		File testCaseFile = new File(testCaseFileFullStr);
		try (BufferedReader br = new BufferedReader(new FileReader(testCaseFile))) {
		    String line;
		    int row = 0;
		    while ((line = br.readLine()) != null) {
		    	StringTokenizer st = new StringTokenizer(line);
		    	int col = 0;
				while (st.hasMoreTokens()) {
					String nextToken = st.nextToken();
					try {
						int parseInt = Integer.parseInt(nextToken);
						fillMapWithPossibilities(parseInt, row, col);
					} catch (NumberFormatException e) {
						fillMapWithPossibilities(NO_INT, row, col);
					}
					
					col++;
				}
				row++;
				
		    }
		    System.out.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Integer getCellNo(int row,int col) {
		return row*SUDOKU_INTXINT + col;
	}
	
	public static void fillMapWithPossibilities(int val,int row,int col) {
		Set<Integer> values = new LinkedHashSet<Integer>();
		if(val == NO_INT) {
			for (int i=1; i <= SUDOKU_INTXINT ; i++) {
				values.add(i);
			}
		} else {
			values.add(val);
		}
		Integer cellNo = getCellNo(row, col);
		possibleValues.put(cellNo, values);
		isChangeOccured.put(cellNo, true);
	}
	
	public static enum Region {
		ROW("row"),
		COLUMN("col"),
		GRID("grid");
		
		public String name;

		private Region(String name) {
			this.name = name;
		}
		
	}
	
	public static String getFileNameWithoutExtension(String fileName) {
		int lastIndexOf = fileName.lastIndexOf(".");
		if(lastIndexOf != -1) {
			return fileName.substring(0, lastIndexOf);
		}
		return fileName;
	}
}
