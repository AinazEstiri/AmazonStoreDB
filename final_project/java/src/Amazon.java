/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Amazon {

   private static String current_userID;
   private static String current_username;
   private static String current_userLat;
   private static String current_userLong;
   private static String current_userType;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Amazon store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Amazon

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      System.out.println("_________________");
      System.out.println("START OF OUTPUT |");
      for (int j = 1; j <= 32; j++){System.out.print("_");}
      System.out.println("_");
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();

      clearScreen(38-rowCount);
      for (int j = 1; j <= 33; j++){System.out.print("_");}
      System.out.println();
      System.out.print("END OF OUTPUT | ");
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
  */


   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count number of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   public static void clearScreen(int x){for (int i = 1; i <= x; i++){System.out.println();}}
   public static void clearScreen(){clearScreen(150);}

   public static boolean valid_number(String num_in_question, boolean integer){

      boolean valid_number = true;
      String NUMBERS = "0123456789.";
      int dot_count = 0;

      for (char c : num_in_question.toCharArray()) {
         if (NUMBERS.indexOf(c) == -1){valid_number = false;}
         if (c == '.'){dot_count = dot_count + 1;}
      }

      if (integer){return (valid_number && dot_count == 0);}

      return (valid_number && (dot_count <= 1) && (dot_count != num_in_question.length()));
   }

   public static boolean valid_storeID(Amazon esql, String store){
      try{
         
         if (!valid_number(store, true)){return false;}

         List<List<String>> storeIDs = esql.executeQueryAndReturnResult("SELECT storeID FROM Store WHERE storeID = " + store);

         for (List<String> iList : storeIDs) {
            if (store.equals(iList.get(0).trim())){return true;}
        }
        return false;

      }catch(Exception e){
         System.err.println (e.getMessage());
         return false;
      }
   }

   public static boolean valid_warehouseID(Amazon esql, String warehouse){
      try{
         
         if (!valid_number(warehouse, true)){return false;}

         List<List<String>> storeIDs = esql.executeQueryAndReturnResult("SELECT WarehouseID FROM Warehouse WHERE WarehouseID = " + warehouse);

         for (List<String> iList : storeIDs) {
            if (warehouse.equals(iList.get(0).trim())){return true;}
        }
        return false;

      }catch(Exception e){
         System.err.println (e.getMessage());
         return false;
      }
   }

   public static boolean valid_product(Amazon esql, String store, String pname){
      try{
         List<List<String>> pnames = esql.executeQueryAndReturnResult("SELECT Product.productName FROM Product WHERE Product.storeID = " + store + 
                                                                        " AND Product.productName = \'" + pname + "\'");

         for (List<String> iList : pnames) {
            if (pname.equals(iList.get(0).trim())){return true;}
         }
        return false;

      }catch(Exception e){
         System.err.println (e.getMessage());
         return false;
      }
   }

   public static boolean valid_username(Amazon esql, String username){
      try{
         List<List<String>> usernames = esql.executeQueryAndReturnResult("SELECT Users.name FROM Users WHERE Users.name = \'" + username + "\'");
         for (List<String> iList : usernames) {
            if (username.equals(iList.get(0).trim())){return true;}
         }
        return false;

      }catch(Exception e){
         System.err.println (e.getMessage());
         return false;
      }
   }

   public static boolean valid_password(Amazon esql, String username, String password){
      try{
         List<List<String>> passwords = esql.executeQueryAndReturnResult("SELECT Users.password FROM Users WHERE Users.name = \'" + username + 
                                                                                             "\' AND Users.password = \'" + password + "\'");
         for (List<String> iList : passwords) {
            if (password.equals(iList.get(0).trim())){return true;}
         }
        return false;

      }catch(Exception e){
         System.err.println (e.getMessage());
         return false;
      }
   }

   public static boolean valid_num_units(Amazon esql, String number_of_units, String storeID, String productName){
      try{

         Float num_units_r = Float.parseFloat(number_of_units);
         
         String units_available = esql.executeQueryAndReturnResult("SELECT numberOfUnits FROM Product WHERE productName = \'" + productName + 
                                                                                    "\' AND storeID = " + storeID).get(0).get(0);                                                       
         Float num_units_a = Float.parseFloat(units_available);
         
         if (num_units_a < num_units_r){return false;}
         else{return true;}

      }catch(Exception e){
         System.err.println (e.getMessage());
         return false;
      }


   }

   public static void spaces(int x){for (int i = 0; i < x; i+=1){System.out.print(" ");}}
   public static void dashes(int x){for (int i = 0; i < x; i+=1){System.out.print("-");}}

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Amazon.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Amazon esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance();
         // instantiate the Amazon object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Amazon (dbname, dbport, user, "");

         boolean keepon = true;

         while(keepon) {

            clearScreen();

            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                
                dashes(15);
                System.out.println("\nMAIN MENU");
                dashes(15);
                System.out.println("\nUser Name: " + authorisedUser);
                System.out.println("User ID: " + esql.current_userID + "; User Type: " + esql.current_userType);
                
                dashes(30); dashes(30); dashes(30); dashes(30); dashes(10);

                System.out.print("\n| Customer Functions (All Users):");
                spaces(20);
                System.out.print("| Manager Only Functions:");
                spaces(24);
                System.out.println("| Admin Only Functions:");

                dashes(30); dashes(30); dashes(30); dashes(30); dashes(10);

                System.out.print("\n1. View Stores within 30 miles");
                spaces(23);
                System.out.print("7. View Stores you Manage");
                spaces(24);
                System.out.println("17. View Users");

                System.out.print("2. View Product List");
                spaces(33);
                System.out.print("8. Update Product");
                spaces(32);
                System.out.println("18. Edit Users");

                System.out.print("3. Place an Order");
                spaces(36);
                System.out.print("9. View 5 recent Product Updates Info");
                spaces(12);
                System.out.println("19. View Products");

                System.out.print("4. View 5 Recent Orders");
                spaces(30);
                System.out.print("10. View 5 Popular Items");
                spaces(25);
                System.out.println("20. Edit Products");

                System.out.print("5. View 10 Favorite Products");
                spaces(25);
                System.out.print("11. View 5 Popular Customers");
                spaces(21);
                System.out.println("21. View Everything");

                System.out.print("6. View Store Information");
                spaces(28);
                System.out.print("12. Place Product Supply Request");
                spaces(17);
                System.out.println("22. Delete store");

                spaces(53);
                System.out.print("13. View Supply Requests");
                spaces(25);
                System.out.println("23. Delete Warehouse");

                spaces(53);
                System.out.println("14. View Store Order Information");

                spaces(53);
                System.out.println("15. View Store Customers");

                spaces(53);
                System.out.print("16. View Top Spenders");
                spaces(28);
                System.out.println("99. Delete Account");

                System.out.println("\n0. Log Out");
                

                System.out.println();
                switch (readChoice()){
                   case 1: viewStores(esql); break;
                   case 2: viewProducts(esql); break;
                   case 3: placeOrder(esql); break;
                   case 4: viewRecentOrders(esql); break;
                   case 5: viewFavoriteProducts(esql); break;
                   case 6: viewStoreInformation(esql); break;
                   case 7: managerViewStores(esql); break;
                   case 8: updateProduct(esql); break;
                   case 9: viewRecentUpdates(esql); break;
                   case 10: viewPopularProducts(esql); break;
                   case 11: viewPopularCustomers(esql); break;
                   case 12: placeProductSupplyRequests(esql); break;
                   case 13: viewSupplyRequests(esql); break;
                   case 14: viewOrderInformation(esql); break;
                   case 15: viewStoreCustomers(esql); break;
                   case 16: viewTopSpenders(esql); break;
                   case 17: adminViewUsers(esql); break;
                   case 18: adminEditUsers(esql); break;
                   case 19: adminViewProducts(esql); break;
                   case 20: adminEditProducts(esql); break;
                   case 21: viewEverything(esql); break;
                   case 22: deleteStore(esql); break;
                   case 23: deleteWarehouse(esql); break;
                   case 0: usermenu = false; break;
                   case 99: usermenu = !deleteAccount(esql); break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      clearScreen();
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();

         // check if this is the correct password
         if (valid_username(esql, name)){
            System.out.println("\n---------------------\n| Username Already Taken! |\n\n---------------------\n"); 
            Thread.sleep(3000);
            return;}

         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude (between 0.0 and 100.0): ");   //enter lat value between [0.0, 100.0]
         String latitude = in.readLine();
         
         if (!valid_number(latitude, false)){
            System.out.println("\n---------------------\n| Invalid Latitude! |\n\n---------------------\n"); 
            Thread.sleep(3000);
            return;}

         float fl_latitude = Float.parseFloat(latitude);

         // check if this is the correct password
         if ((fl_latitude < 0) || (fl_latitude > 100)){
            System.out.println("\n---------------------\n| Invalid Latitude! |\n\n---------------------\n"); 
            Thread.sleep(3000);
            return;}

         System.out.print("\tEnter longitude (between 0.0 and 100.0): ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();

         if (!valid_number(longitude, false)){
            System.out.println("\n---------------------\n| Invalid Longitude! |\n\n---------------------\n"); 
            Thread.sleep(3000);
            return;}

         float fl_longitude = Float.parseFloat(longitude);
 
         // check if this is the correct password
         if ((fl_longitude < 0) || (fl_longitude > 100)){
            System.out.println("\n---------------------\n| Invalid Longitude! |\n\n---------------------\n"); 
            Thread.sleep(3000);
            return;}

         String type="customer";

			String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();

         // check if this is an actual user name
         if (!valid_username(esql, name)){
            System.out.println("\n---------------------\n| Invalid User Name! |\n\n---------------------\n"); 
            Thread.sleep(3000);
            return null;}

         System.out.print("\tEnter password: ");
         String password = in.readLine();

         // check if this is the correct password
         if (!valid_password(esql, name, password)){
            System.out.println("\n---------------------\n| Wrong Password! |\n\n---------------------\n"); 
            Thread.sleep(3000);
            return null;}

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         int userNum = esql.executeQuery(query);

         esql.current_username = name;

         query = String.format("SELECT userID FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         esql.current_userID = esql.executeQueryAndReturnResult(query).get(0).get(0);
          
         esql.current_userLat = esql.executeQueryAndReturnResult(
            String.format("SELECT latitude FROM USERS WHERE name = '%s' AND password = '%s'", name, password)
            ).get(0).get(0);

         esql.current_userLong = esql.executeQueryAndReturnResult(
            String.format("SELECT longitude FROM USERS WHERE name = '%s' AND password = '%s'", name, password)
            ).get(0).get(0);

         esql.current_userType = esql.executeQueryAndReturnResult(
            String.format("SELECT type FROM USERS WHERE name = '%s' AND password = '%s'", name, password)
            ).get(0).get(0).trim();

	 if (userNum > 0)
		return name;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static boolean deleteAccount(Amazon esql){
      try{

         // managers cannot perform this function
         if (esql.current_userType.equals("manager")){
            System.out.println("Sorry, managers cannot perform this function.\n\n");
            return false;
         }

         System.out.print("\tEnter password again to delete account: ");
         String password = in.readLine();

         // check if this is the correct password
         if (!valid_password(esql, esql.current_username, password)){
            System.out.println("\n---------------------\n| Wrong Password! |\n\n---------------------\n"); 
            Thread.sleep(3000);
            return false;}
         
         String query = "DELETE FROM Users WHERE Users.userID = " + esql.current_userID;
         esql.executeUpdate(query);
         return true;

      }catch(Exception e){
         System.err.println (e.getMessage());
  	   } 
      return false;

   }

   public static void viewStores(Amazon esql) {
   	try{
         
         String query = "SELECT Store.storeID, Store.longitude, Store.latitude "+
                        "FROM Store "+
                        "WHERE SQRT(POWER((Store.longitude - " + esql.current_userLong + 
                           "), 2) + POWER((Store.latitude - " + esql.current_userLat + "), 2)) < 30";

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
  	   } 
   
   }
   public static void viewProducts(Amazon esql) {
   	try{

         System.out.print("\tEnter Store ID: ");
         String storeID = in.readLine();

         // check if this is an actual store
         if (!valid_storeID(esql, storeID)){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}

         String query = "SELECT Product.productName, Product.numberOfUnits, Product.pricePerUnit "+
		                  "FROM Product "+
		                  "WHERE Product.storeID = " + storeID;

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   
   }

   public static void placeOrder(Amazon esql) {
   	try{
	      System.out.print("\tEnter storeID: ");
         String storeID = in.readLine();

         // check if this is an actual store
         if (!valid_storeID(esql, storeID)){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}

         String storeLat = esql.executeQueryAndReturnResult(
                     "SELECT Store.latitude "+
                     "FROM Store "+
                     "WHERE Store.storeID = " + storeID
         ).get(0).get(0);

         String storeLong = esql.executeQueryAndReturnResult(
                     "SELECT Store.longitude "+
                     "FROM Store "+
                     "WHERE Store.storeID = " + storeID
         ).get(0).get(0);

         double sLat = Double.parseDouble(storeLat);
         double sLong = Double.parseDouble(storeLong);

         double uLat = Double.parseDouble(esql.current_userLat);
         double uLong = Double.parseDouble(esql.current_userLong);

         double distance = esql.calculateDistance(sLat, sLong, uLat, uLong);

         if (distance > 30) {
            System.out.println("Sorry, you have to be within 30 miles of the store. \nSelect Option 1 to see stores within 30 miles.\n\n");
            return;
         }

	      System.out.print("\tEnter product name: ");
         String productName = in.readLine();

         // check if this is an actual product in the store
         if (!valid_product(esql, storeID, productName)){
            System.out.println("\n---------------------\n| Invalid Product Name! |\n\n---------------------\n"); 
            return;}

	      System.out.print("\tEnter number of units: ");
         String numUnits = in.readLine();

         if (!valid_number(numUnits, true)){
            System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
            return;}
         
         // check if there is enough units available
         if (!valid_num_units(esql, numUnits, storeID, productName)){
            System.out.println("\n---------------------\n| Sorry, not enough units available. |\n\n---------------------\n"); 
            return;}

         String price = esql.executeQueryAndReturnResult(
                     "SELECT (Product.pricePerUnit * " + numUnits + ") " +
                     "FROM Product " +
                     "WHERE Product.storeID = " + storeID + " AND Product.productName = \'" + productName + "\'"
         ).get(0).get(0);

         System.out.print("\nThis costs $" + price + ". \nEnter yes to confirm: ");
         String response_y = in.readLine();

         if (! (response_y.equals("yes") || response_y.equals("Yes") || response_y.equals("y") || response_y.equals("Y"))){return;}
         
         String query = "INSERT INTO Orders (customerID, storeID, productName, unitsOrdered, orderTime) " +
		 	               "VALUES (" + esql.current_userID + ", " + storeID + 
                        ", \'" + productName + "\', " + numUnits + ", CURRENT_TIMESTAMP)";

         esql.executeUpdate(query);

         // query = "UPDATE Product SET numberOfUnits = numberOfUnits - " + numUnits + " " +
         //         "WHERE Product.productName = \'" + productName + "\' AND Product.storeID = " + storeID;

         // esql.executeUpdate(query);

      }catch(Exception e){
         System.err.println (e.getMessage());
      } 
   }
   
   public static void viewRecentOrders(Amazon esql) {
      try{
         String query = "SELECT Orders.orderNumber, Orders.productName, Orders.unitsOrdered, Orders.storeId, Orders.orderTime " +
		                  "FROM Orders "+
		                  "WHERE Orders.customerID = " + esql.current_userID + " " +
                        "ORDER BY Orders.orderTime DESC " +
                        "LIMIT 5";

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }

   }

   public static void viewFavoriteProducts(Amazon esql){
      try{

         System.out.print("\tEnter StoreID (enter \'-\' for all): ");
         String storeID = in.readLine();

         String query = "SELECT productName, SUM(Orders.unitsOrdered) as Total_Units_Ordered";

         if (!storeID.equals("-")){
            // check if this is an actual store
            if (!valid_storeID(esql, storeID)){
               System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
               return;}
            query+= ", storeID ";
            }
         else{query+= " ";}

		   query+= "FROM Orders "+
		           "WHERE customerID = " + esql.current_userID + " ";

         if (!storeID.equals("-")){query+= "AND storeID = " + storeID + " ";}

         query+= "GROUP BY productName";

         if (!storeID.equals("-")){query+= ", storeID ";}
         else{query+=" ";}

         query+= "ORDER BY Total_Units_Ordered DESC " +
                 "LIMIT 10";

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }

   }

   public static void viewStoreInformation(Amazon esql){
      try{

         System.out.print("\tEnter StoreID (enter \'-\' for all): ");
         String storeID = in.readLine();

         // valid storeID check
         if ((!valid_storeID(esql, storeID)) && (!storeID.equals("-"))){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}
            
         String query = "SELECT Store.storeID, Store.longitude, Store.latitude, Store.dateEstablished, Users.name as manager_name ";

		   query+= "FROM Store, Users "+
		           "WHERE Store.managerID = Users.userID";

         if (!storeID.equals("-")){query+= " AND Store.storeID = " + storeID;}

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }

   }

   //__MANAGER_ONLY_FUNCTIONS__________________________________________________________________________________________________________________________________________

   public static void managerViewStores(Amazon esql){
      // only managers can perform this function
      if (!esql.current_userType.equals("manager")){
         System.out.println("Sorry, only managers can perform this function.\n\n");
         return;
      }
      try{
         
         String query = "SELECT Store.storeID, Store.dateEstablished, "+
                        "COUNT(DISTINCT Product.productName) as number_of_products, COUNT(DISTINCT Orders.orderNumber) as number_of_orders, "+
                        "SUM(Product.numberOfUnits * Product.pricePerUnit) as total_order_income " +
                        "FROM Product, Orders, Store " +
                        "WHERE Store.managerID = " + esql.current_userID + " " +
                        "AND Product.storeID = Store.storeID AND Orders.storeID = Store.storeID " +
                        "GROUP BY Store.storeID, Store.dateEstablished " +
                        "ORDER BY number_of_orders";


         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }

   }

   public static void updateProduct(Amazon esql) {

      // only managers can perform this function
      if (!esql.current_userType.equals("manager")){
         System.out.println("Sorry, only managers can perform this function.\n\n");
         return;
      }

      try{
         
         System.out.print("\tEnter storeID: ");
         String storeID = in.readLine();
         
         // check if this is an actual store
         if (!valid_storeID(esql, storeID)){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}

         String managerID = esql.executeQueryAndReturnResult(
               "SELECT Store.managerID FROM Store WHERE Store.storeID = " + storeID
         ).get(0).get(0);

         if (!esql.current_userID.equals(managerID)){
            System.out.println("Sorry, you are not this store's manager.\n\n");
            return;
         }

         System.out.print("\tEnter product name: ");
         String productName = in.readLine();

         // check if the product is valid
         if (!valid_product(esql, storeID, productName)){
            System.out.println("\n-------------------------\n| Invalid Product Name! |\n\n-------------------------\n"); 
            return;}

         System.out.print("\tNew number of units (enter \"-\" if no change): ");
         String new_numUnits = in.readLine();

         if ((!new_numUnits.equals("-")) && (!valid_number(new_numUnits, true))){
               System.out.println("\n-------------------------\n| Invalid Number! |\n\n-------------------------\n"); 
               return;}

         System.out.print("\tNew price per unit (enter \"-\" if no change): ");
         String new_priceperunit = in.readLine();

         if ((!new_priceperunit.equals("-")) && (!valid_number(new_priceperunit, false))){
            System.out.println("\n-------------------------\n| Invalid Number! |\n\n-------------------------\n"); 
            return;}
         
         String query;
         if (new_numUnits.equals("-") && new_priceperunit.equals("-")){
            System.out.println("No changes.");
            return;
         }
         else if (new_numUnits.equals("-")){
            query = "UPDATE Product SET pricePerUnit = " + new_priceperunit +
                    " WHERE storeID = " + storeID + " AND productName = \'" + productName + "\'";
         }
         else if (new_priceperunit.equals("-")){
            query = "UPDATE Product SET numberOfUnits = " + new_numUnits +
                    " WHERE storeID = " + storeID + " AND productName = \'" + productName + "\'";
         }
         else{
            query = "UPDATE Product SET numberOfUnits = " + new_numUnits + ", pricePerUnit = " + new_priceperunit +
                    " WHERE storeID = " + storeID + " AND productName = \'" + productName + "\'";
         }

         esql.executeUpdate(query);

         String query2 = String.format("INSERT INTO ProductUpdates (managerID, storeID, productName, updatedOn) VALUES (%s, %s, '%s', %s)", 
                                    esql.current_userID, storeID, productName, "CURRENT_TIMESTAMP");

         esql.executeUpdate(query2);

         System.out.println("Successfully updated product information.\n\n");

      }catch(Exception e){
         System.err.println (e.getMessage());
      }

   }

   public static void viewRecentUpdates(Amazon esql) {

      // only managers can perform this function
      if (!esql.current_userType.equals("manager")){
         System.out.println("Sorry, only managers can perform this function.\n\n");
         return;
      }

      try{
         String query = "SELECT * " +
		                  "FROM ProductUpdates "+
		                  "WHERE ProductUpdates.managerID = " + esql.current_userID +
                        "ORDER BY ProductUpdates.updatedOn DESC " +
                        "LIMIT 5";

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void viewPopularProducts(Amazon esql) {

      // only managers can perform this function
      if (!esql.current_userType.equals("manager")){
         System.out.println("Sorry, only managers can perform this function.\n\n");
         return;
      }

      try{
         System.out.print("\tEnter storeID: ");
         String storeID = in.readLine();

         // valid storeID check
         if (!valid_storeID(esql, storeID)){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}


         String managerID = esql.executeQueryAndReturnResult(
               "SELECT Store.managerID FROM Store WHERE Store.storeID = " + storeID
         ).get(0).get(0);

         if (!esql.current_userID.equals(managerID)){
            System.out.println("Sorry, you are not this store's manager.\n\n");
            return;
         }

         String query = "SELECT Orders.productName, COUNT(*) " +
		                  "FROM Orders "+
                        "WHERE Orders.storeID = " + storeID +
                        "GROUP BY Orders.productName " +
                        "ORDER BY COUNT(*) DESC " +
                        "LIMIT 5";

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void viewPopularCustomers(Amazon esql) {

      // only managers can perform this function
      if (!esql.current_userType.equals("manager")){
         System.out.println("Sorry, only managers can perform this function.\n\n");
         return;
      }

      try{
         System.out.print("\tEnter storeID: ");
         String storeID = in.readLine();

         // valid storeID check
         if (!valid_storeID(esql, storeID)){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}

         String managerID = esql.executeQueryAndReturnResult(
               "SELECT Store.managerID FROM Store WHERE Store.storeID = " + storeID
         ).get(0).get(0);

         if (!esql.current_userID.equals(managerID)){
            System.out.println("Sorry, you are not this store's manager.\n\n");
            return;
         }

         String query = "SELECT Orders.customerID, Users.name, COUNT(*) " +
		                  "FROM Orders, Users "+
                        "WHERE Users.userID = Orders.customerID AND Orders.storeID = " + storeID +
                        "GROUP BY Orders.customerID, Users.name " +
                        "ORDER BY COUNT(*) DESC " +
                        "LIMIT 5";

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void placeProductSupplyRequests(Amazon esql) {

      // only managers can perform this function
      if (!esql.current_userType.equals("manager")){
         System.out.println("Sorry, only managers can perform this function.\n\n");
         return;
      }

      try{
         System.out.print("\tEnter storeID: ");
         String storeID = in.readLine();

         // valid storeID check
         if (!valid_storeID(esql, storeID)){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}
 
         String managerID = esql.executeQueryAndReturnResult(
               "SELECT Store.managerID FROM Store WHERE Store.storeID = " + storeID
         ).get(0).get(0);

         if (!esql.current_userID.equals(managerID)){
            System.out.println("Sorry, you are not this store's manager.\n\n");
            return;
         }

         System.out.print("\tWarehouse ID:");
         String warehouseID = in.readLine();

         if (!valid_warehouseID(esql, warehouseID)){
            System.out.println("\n---------------------\n| Invalid Warehouse ID! |\n\n---------------------\n"); 
            return;}

         System.out.print("\tEnter product name: ");
         String productName = in.readLine();

         // valid product name check
         if (!valid_product(esql, storeID, productName)){
            System.out.println("\n---------------------\n| Invalid Product Name! |\n\n---------------------\n"); 
            return;}

         System.out.print("\tUnits Requested: ");
         String unitsRequested = in.readLine();

         // valid number check
         if (!valid_number(unitsRequested, true)){
            System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
            return;}

         String query = String.format("INSERT INTO ProductSupplyRequests (storeID, managerID, warehouseID, productName, unitsRequested) "+
                                    "VALUES (%s, %s, %s, '%s', %s)", 
                                       storeID, managerID, warehouseID, productName, unitsRequested);

         esql.executeUpdate(query);

         String old_numUnits = esql.executeQueryAndReturnResult(
               "SELECT Product.numberOfUnits FROM Product "+
               "WHERE Product.productName = \'" + productName + "\' AND Product.storeID = " + storeID
         ).get(0).get(0);

         // String query2 = "UPDATE Product SET numberOfUnits = (" + old_numUnits + " + " + unitsRequested +
         //            ") WHERE storeID = " + storeID + " AND productName = \'" + productName + "\'";

         // esql.executeUpdate(query2);
         

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void viewSupplyRequests(Amazon esql) {
      try{

         // only managers can perform this function
         if (!esql.current_userType.equals("manager")){
            System.out.println("Sorry, only managers can perform this function.\n\n");
            return;
         }

            System.out.print("\tStore ID (enter \'-\' for all stores you manage):");
            String storeID = in.readLine();

            // valid storeID check
            if ((!valid_storeID(esql, storeID)) && (!storeID.equals("-"))){
               System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
               return;}

            System.out.print("\tHow many results to display (enter \'-\' for all):");
            String recent_s = in.readLine();
            boolean recent = !recent_s.equals("-");

            if (!recent_s.equals("-")){
               if (!valid_number(recent_s, true)){
                  System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
                  return;}

               Float fl_num = Float.parseFloat(recent_s);
               if (fl_num < 0){
                  System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
                  return;}
            }
            
            String query;
            
            query = "SELECT * FROM ProductSupplyRequests WHERE managerID = " + esql.current_userID;

            if (!storeID.equals("-")){query += " AND storeID = " + storeID;}
            // if (recent) {query += " ORDER BY requestNumber DESC";}
            query += " ORDER BY requestNumber DESC";
            
            int rowCount = esql.executeQueryAndPrintResult(query);
            System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void viewOrderInformation(Amazon esql) {
      try{

         // only managers can perform this function
         if (!esql.current_userType.equals("manager")){
            System.out.println("Sorry, only managers can perform this function.\n\n");
            return;
         }

         System.out.print("\tStore ID (enter \'-\' for all stores you manage):");
         String storeID = in.readLine();

         // valid storeID check
         if ((!valid_storeID(esql, storeID)) && (!storeID.equals("-"))){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}
         
         String query;
         
         if (storeID.equals("-")){
            query = "SELECT * FROM Orders WHERE Orders.storeID IN "+
                           "(SELECT Store.storeID FROM Store WHERE Store.managerID = " + esql.current_userID + ")";
         }
         else{

            // can only view this store if the current user is the store's manager
            String managerID = esql.executeQueryAndReturnResult(
               "SELECT Store.managerID FROM Store WHERE Store.storeID = " + storeID
            ).get(0).get(0);

            if (!esql.current_userID.equals(managerID)){
               System.out.println("Sorry, you are not this store's manager.\n\n");
               return;
            }
            query = "SELECT * FROM Orders WHERE Orders.storeID = " + storeID;
         }

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void viewStoreCustomers(Amazon esql){
      try{

         // only managers can perform this function
         if (!esql.current_userType.equals("manager")){
            System.out.println("Sorry, only managers can perform this function.\n\n");
            return;
         }

         System.out.print("\tStore ID (enter \'-\' for all stores you manage):");
         String storeID = in.readLine();

         // valid storeID check
         if ((!valid_storeID(esql, storeID)) && (!storeID.equals("-"))){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}
         
         String query;
         
         if (storeID.equals("-")){
            query = "SELECT Users.userID, Users.name, Users.type, COUNT(*) as number_of_orders "+
                    "FROM Store, Users, Orders "+
                    "WHERE Users.userID = Orders.customerID AND Orders.storeID = Store.storeID "+
                    "AND Store.managerID = " + esql.current_userID +
                    " GROUP BY Users.userID, Users.name, Users.type";
         }
         else{

            // can only view this store if the current user is the store's manager
            String managerID = esql.executeQueryAndReturnResult(
               "SELECT Store.managerID FROM Store WHERE Store.storeID = " + storeID
            ).get(0).get(0);

            if (!esql.current_userID.equals(managerID)){
               System.out.println("Sorry, you are not this store's manager.\n\n");
               return;
            }

            query = "SELECT Users.userID, Users.name, Users.type, COUNT(*) as number_of_orders " +
                    "FROM Users, Orders WHERE Users.userID = Orders.customerID AND Orders.storeID = " + storeID +
                     " GROUP BY Users.userID, Users.name, Users.type";
         }

         query += " ORDER BY number_of_orders DESC";

         System.out.print("\tHow many results do you want to see? (enter \'-\' for all):");
         String input = in.readLine();

         if (!input.equals("-")){query+= " LIMIT " + input;}

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void viewTopSpenders(Amazon esql){
      try{

         // only managers can perform this function
         if (!esql.current_userType.equals("manager")){
            System.out.println("Sorry, only managers can perform this function.\n\n");
            return;
         }

         System.out.print("\tStore ID (enter \'-\' for all stores you manage):");
         String storeID = in.readLine();

         // valid storeID check
         if ((!valid_storeID(esql, storeID)) && (!storeID.equals("-"))){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}
         
         String query;
         
         if (storeID.equals("-")){
            query = "SELECT Users.userID, Users.name, Users.type, SUM(Orders.unitsOrdered * Product.pricePerUnit) as total_spending "+
                    "FROM Store, Users, Orders, Product " +
                    "WHERE Users.userID = Orders.customerID AND Orders.storeID = Store.storeID AND Orders.storeID = Product.storeID "+
                    "AND Orders.productName = Product.productName AND Store.managerID = " + esql.current_userID + " " +
                    "GROUP BY Users.userID, Users.name, Users.type";
         }
         else{

            // can only view this store if the current user is the store's manager
            String managerID = esql.executeQueryAndReturnResult(
               "SELECT Store.managerID FROM Store WHERE Store.storeID = " + storeID
            ).get(0).get(0);

            if (!esql.current_userID.equals(managerID)){
               System.out.println("Sorry, you are not this store's manager.\n\n");
               return;
            }

            query = "SELECT Users.userID, Users.name, Users.type, SUM(Orders.unitsOrdered * Product.pricePerUnit) as total_spending " +
                    "FROM Users, Orders, Product WHERE Users.userID = Orders.customerID AND Orders.storeID = Product.storeID " +
                    "AND Orders.productName = Product.productName AND Orders.storeID = " + storeID + " " +
                    "GROUP BY Users.userID, Users.name, Users.type";
         }

         query += " ORDER BY total_spending DESC";

         System.out.print("\tHow many results do you want to see? (enter \'-\' for all):");
         String input = in.readLine();

         if (!input.equals("-")){
            if (!valid_number(input, true)){
               System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
               return;}

            query+= " LIMIT " + input;
            }

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   

   //__ADMIN_ONLY_FUNCTIONS__________________________________________________________________________________________________________________________________________

   public static void adminViewUsers(Amazon esql) {
      try{
      
         // only admin can use this function
         if (!esql.current_userType.equals("admin")){
            System.out.println("Sorry, only administrators can perform this function.\n\n");
            return;
         }

         String query = "SELECT Users.name, Users.password, Users.userID, Users.type, Users.latitude, Users.longitude FROM Users";
         
         System.out.print("\tEnter first userID to display (enter \'-\' for beginning of list): ");
         String first = in.readLine();

         // check if this is a valid userID
         if (!(first.equals("-"))){
            if (valid_number(first, true)){
               System.out.println("\n---------------------\n| Invalid User ID! |\n\n---------------------\n"); 
               return;}}

         System.out.print("\tEnter last userID to display (enter \'-\' for end of list): ");
         String last = in.readLine();

         // check if this is a valid userID
         if (!(last.equals("-"))){
            if (valid_number(last, true)){
               System.out.println("\n---------------------\n| Invalid User ID! |\n\n---------------------\n"); 
               return;}}

         if (! ( first.equals("-") && last.equals("-") ) ){
            if (! ( first.equals("-") || last.equals("-") ) ){
               query+=" WHERE Users.userID >= " + first;
               query+=" AND Users.userID <= " + last;
            }
            else if (!first.equals("-")){query+=" WHERE Users.userID >= " + first;}
            else{query+= " WHERE Users.userID <= " + last;}
         }

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void adminEditUsers(Amazon esql){
      try{

         // only admin can use this function
         if (!esql.current_userType.equals("admin")){
            System.out.println("Sorry, only administrators can perform this function.\n\n");
            return;
         }

         System.out.print("\tWhich User do you want to edit? Input UserID:");
         String userID = in.readLine();

         if (!valid_number(userID, true)){
            System.out.println("\n-----------------------------\n| Invalid Number! |\n\n-----------------------------\n"); 
            return;}

         esql.executeQueryAndPrintResult("SELECT * FROM Users WHERE Users.userID = " + userID);
         System.out.println();

         System.out.println("\tWhich attribute(s) do you want to edit?");
         System.out.println("0. Go Back\n1. Name\n2. Password\n3. Location\n4. User Type\n5. Edit Multiple Attributes");
         System.out.print("Enter a number: ");
         String response = in.readLine();
         if (response.equals("0")){clearScreen(); return;}

         String query = "UPDATE Users SET ";
                        
         String response2;
         if (response.equals("1")){
            
            System.out.print("Enter new name: ");
            response2 = in.readLine();

            // check if this is username has already been taken
            if (valid_username(esql, response2)){
               System.out.println("\n-----------------------------\n| User Name already taken! |\n\n-----------------------------\n"); 
               return;}

            query+= "name = \'" + response2 + "\'"; 

         }
         else if (response.equals("2")){
            
            System.out.print("Enter new password: ");
            response2 = in.readLine();

            query+= "password = \'" + response2 + "\'"; 
         }
         else if (response.equals("3")){
            
            System.out.print("Enter new location (latitude then longitude): ");

            response2 = in.readLine();

            if (!valid_number(response2, false)){
               System.out.println("\n---------------------\n| Invalid Latitude! |\n\n---------------------\n"); 
               return;}

            float float_loc = Float.parseFloat(response2);

            // check if this is a valid latitude
            if ((float_loc < 0) || (float_loc > 100)){
               System.out.println("\n---------------------\n| Invalid Latitude! |\n\n---------------------\n"); 
               return;}

            query+= "latitude = " + response2; 

            response2 = in.readLine();
            
            if (!valid_number(response2, false)){
               System.out.println("\n---------------------\n| Invalid Latitude! |\n\n---------------------\n"); 
               return;}

            float_loc = Float.parseFloat(response2);

            // check if this is a valid longitude
            if ((float_loc < 0) || (float_loc > 100)){
               System.out.println("\n---------------------\n| Invalid Longitude! |\n\n---------------------\n"); 
               return;}

            query+= ", longitude = " + response2; 
         }
         else if (response.equals("4")){
            
            System.out.print("Enter new user type: ");
            response2 = in.readLine();

            if (! ( (response2.equals("customer")) || (response2.equals("manager")) || (response2.equals("admin")) ) ){
               System.out.println("\n-----------------------------\n| Invalid User Type! |\n\n-----------------------------\n"); 
               return;}

            query+= "type = \'" + response2 + "\'"; 
         }
         else if (response.equals("5")){
            
            String addQuery = "_$_&_@#";

            System.out.print("Enter new name (enter \'-\' to keep the same): ");
            response2 = in.readLine();

            if (!response2.equals("-")){
               
               // check if this is username has already been taken
               if (valid_username(esql, response2)){
                  System.out.println("\n---------------------\n| User Name already taken! |\n\n-----------------------------\n"); 
                  return;}

               if (addQuery.equals("_$_&_@#")){addQuery = "name = \'" + response2 + "\'";}
               else{addQuery+= ", " + "name = \'" + response2 + "\'";}
            }

            System.out.print("Enter new password (enter \'-\' to keep the same): ");
            response2 = in.readLine();

            if (!response2.equals("-")){
               if (addQuery.equals("_$_&_@#")){addQuery = "password = \'" + response2 + "\'";}
               else{addQuery+= ", " + "password = \'" + response2 + "\'";}
            }

            System.out.print("Enter new user type (enter \'-\' to keep the same): ");
            response2 = in.readLine();

            if (!response2.equals("-")){
               if (! ( (response2.equals("customer")) || (response2.equals("manager")) || (response2.equals("admin")) ) ){
                  System.out.println("\n-----------------------------\n| Invalid User Type! |\n\n-----------------------------\n"); 
                     return;}

               if (addQuery.equals("_$_&_@#")){addQuery = "type = \'" + response2 + "\'";}
               else{addQuery+= ", " + "type = \'" + response2 + "\'";}
            }

            System.out.print("Enter new location (latitude then longitude, enter \'-\' to keep the same): ");
            response2 = in.readLine();

            if (!response2.equals("-")){

               if (!valid_number(response2, false)){
               System.out.println("\n---------------------\n| Invalid Latitude! |\n\n---------------------\n"); 
               return;}

               float float_lat = Float.parseFloat(response2);

               // check if this is the correct password
               if ((float_lat < 0) || (float_lat > 100)){
                  System.out.println("\n---------------------\n| Invalid Latitude! |\n\n---------------------\n"); 
                  return;}

               if (addQuery.equals("_$_&_@#")){addQuery = "latitude = " + response2;}
               else{addQuery+= ", " + "latitude = " + response2;}
            }

            response2 = in.readLine();

            if (!response2.equals("-")){

               if (!valid_number(response2, false)){
               System.out.println("\n---------------------\n| Invalid Latitude! |\n\n---------------------\n"); 
               return;}

               float float_long = Float.parseFloat(response2);

               // check if this is the correct password
               if ((float_long < 0) || (float_long > 100)){
                  System.out.println("\n---------------------\n| Invalid Latitude! |\n\n---------------------\n"); 
                  return;}

               if (addQuery.equals("_$_&_@#")){addQuery = "longitude = " + response2;}
               else{addQuery+= ", " + "longitude = " + response2;}
            }

            if (!addQuery.equals("_$_&_@#")){query+=addQuery;}

         }
         else{System.out.println("Unknown input!"); return;}

         query+= " WHERE userID = " + userID;
         //System.out.println(query);
         esql.executeUpdate(query);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void adminViewProducts(Amazon esql){
      try{

         // only admin can use this function
         if (!esql.current_userType.equals("admin")){
            System.out.println("Sorry, only administrators can perform this function.\n\n");
            return;
         }

         System.out.print("Enter storeID (enter \'-\' for all): ");
         String storeID = in.readLine();

         // valid storeID check
         if ((!valid_storeID(esql, storeID)) && (!storeID.equals("-"))){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}

         String query = "SELECT * FROM Product";

         if (!storeID.equals("-")){query += " WHERE Product.storeID = " + storeID;}

         System.out.println("Order by?");
         System.out.println("0. No Order\n1. Number of Units Ascending\n2. Number of Units Descending\n3. Price Per Unit Ascending\n4. Price Per Unit Descending");
         System.out.print("Enter a number: ");
         String order_by = in.readLine();

         if (order_by.equals("1")){query+=" ORDER BY Product.numberOfUnits";}
         else if (order_by.equals("2")){query+=" ORDER BY Product.numberOfUnits DESC";}
         else if (order_by.equals("3")){query+=" ORDER BY Product.pricePerUnit";}
         else if (order_by.equals("4")){query+=" ORDER BY Product.pricePerUnit DESC";}

         System.out.print("How many results? (enter \'-\' for all): ");
         String num_results = in.readLine();

         if (!num_results.equals("-")){query+= " LIMIT " + num_results;}

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void adminEditProducts(Amazon esql){
      try{

         // only admin can use this function
         if (!esql.current_userType.equals("admin")){
            System.out.println("Sorry, only administrators can perform this function.\n\n");
            return;
         }

         System.out.print("Enter a storeID (enter \'-\' for all): ");
         String storeID = in.readLine();

         // valid storeID check
         if ((!valid_storeID(esql, storeID)) && (!storeID.equals("-"))){
            System.out.println("\n---------------------\n| Invalid Store ID! |\n\n---------------------\n"); 
            return;}

         System.out.print("Enter a product name (enter \'-\' for all): ");
         String productName = in.readLine();


         // valid product check
         if (!productName.equals("-")){
            if (!valid_product(esql, storeID, productName)){
               System.out.println("\n---------------------\n| Invalid Product Name! |\n\n---------------------\n"); 
               return;}}

         System.out.print("Set new number of units (enter \'-\' for no change): ");
         String num_units = in.readLine();

         // valid number check
         if (!num_units.equals("-")){
            if (!valid_number(num_units, true)){
               System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
               return;}

            float float1_num = Float.parseFloat(num_units);

            // no negative numbers here
            if (float1_num < 0){
               System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
               return;}
            }

         System.out.print("Set new price per unit (enter \'-\' for no change): ");
         String price_per = in.readLine();

         // valid number check
         if (!price_per.equals("-")){
            if (!valid_number(price_per, false)){
               System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
               return;}

            float float2_num = Float.parseFloat(price_per);

            // no negative numbers here
            if (float2_num < 0){
               System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
               return;}
            }

         if (num_units.equals("-") && price_per.equals("-")){System.out.println("No change."); return;}

         String query = "UPDATE Product SET ";
         
         if (num_units.equals("-")){query+= "pricePerUnit = " + price_per;}
         else if (price_per.equals("-")) {query+= "numberOfUnits = " + num_units;}
         else {query+= "numberOfUnits = " + num_units + ", pricePerUnit = " + price_per;}

         if (!(storeID.equals("-") && productName.equals("-"))){
            query += " WHERE ";

            if (!(storeID.equals("-") || productName.equals("-"))){query += "productName = \'" + productName + "\' AND storeID = " + storeID;}
            else if (storeID.equals("-")){query += "productName = \'" + productName + "\'";}
            else if (productName.equals("-")){query += "storeID = " + storeID;}
            else{return;}
         }

         esql.executeUpdate(query);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static String inputRangeQueryOnAttribute(String query, String attribute){

      try{
         System.out.print("\tEnter first " + attribute + " to display (enter \'-\' for beginning of list): ");
         String first = in.readLine();

         if ((!first.equals("-")) && (!valid_number(first, true))){
            System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
            return null;}

         System.out.print("\tEnter last " + attribute + " to display (enter \'-\' for end of list): ");
         String last = in.readLine();

         if ((!last.equals("-")) && (!valid_number(last, true))){
            System.out.println("\n---------------------\n| Invalid Number! |\n\n---------------------\n"); 
            return null;}

         if (! ( first.equals("-") && last.equals("-") ) ){
            if (! ( first.equals("-") || last.equals("-") ) ){
               query+=" WHERE " + attribute + " >= " + first;
               query+=" AND " + attribute + " <= " + last;
         }
            else if (!first.equals("-")){query+=" WHERE " + attribute + " >= " + first;}
            else{query+= " WHERE " + attribute + " <= " + last;}
         }
      }catch(Exception e){
         System.err.println (e.getMessage());
      }

      return query;
   }

   public static void viewEverything(Amazon esql){

      // only admin can use this function
      if (!esql.current_userType.equals("admin")){
         System.out.println("Sorry, only administrators can perform this function.\n\n");
         return;
      }

      try{

         System.out.println("\tWhich relation do you want to view?:");
         System.out.println("0.Go Back\n1. Users\n2. Stores\n3. Products\n4. Orders\n5. Warehouses\n6. Supply Requests\n7. Product Updates\n");
         System.out.print("Enter a number: ");
         String response = in.readLine();

         String query = "SELECT * FROM ";

         if (response.equals("0")){return;}
         else if(response.equals("1")){query+= "Users"; query = inputRangeQueryOnAttribute(query, "userID");}
         else if(response.equals("2")){query+= "Store"; query = inputRangeQueryOnAttribute(query, "storeID");}
         else if(response.equals("3")){
            query+= "Product";
            System.out.println("\tSelect an attribute to select by:");
            System.out.println("1. Store ID\n2. Price Per Unit\n3. Number of Units");
            System.out.print("Enter a number:");
            String num = in.readLine();
            String attribute;

            if (num.equals("1")){attribute = "storeID";}
            else if (num.equals("2")){attribute = "pricePerUnit";}
            else if (num.equals("3")){attribute = "numberOfUnits";}
            else {System.out.println("Unknown input!"); return;}
            query = inputRangeQueryOnAttribute(query, attribute);
         }
         else if(response.equals("4")){query+= "Orders"; query = inputRangeQueryOnAttribute(query, "orderNumber");}
         else if(response.equals("5")){query+= "Warehouse"; query = inputRangeQueryOnAttribute(query, "WarehouseID");}
         else if(response.equals("6")){query+= "ProductSupplyRequests"; query = inputRangeQueryOnAttribute(query, "requestNumber");}
         else if(response.equals("7")){query+= "ProductUpdates"; query = inputRangeQueryOnAttribute(query, "updateNumber");}
         else{System.out.println("Unknown input!"); return;}   


         if (query == null){
            System.out.println("\n---------------------\n| Invalid Input! |\n\n---------------------\n"); 
            return;}

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void deleteStore(Amazon esql) {
      try{
      
         // only admin can use this function
         if (!esql.current_userType.equals("admin")){
            System.out.println("Sorry, only administrators can perform this function.\n\n");
            return;
         }

         System.out.print("\tEnter storeID to delete: ");
         String storeID = in.readLine();

         // check if this is a valid userID
         if (!valid_storeID(esql, storeID)){
            System.out.println("\n---------------------\n| Invalid StoreID! |\n\n---------------------\n"); 
            return;}

         String query = "DELETE FROM Store WHERE Store.storeID = " + storeID;

         esql.executeUpdate(query);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void deleteWarehouse(Amazon esql) {
      try{
      
         // only admin can use this function
         if (!esql.current_userType.equals("admin")){
            System.out.println("Sorry, only administrators can perform this function.\n\n");
            return;
         }

         System.out.print("\tEnter WarehouseID to delete: ");
         String warehouseID = in.readLine();

         // check if this is a valid userID
         if (!valid_warehouseID(esql, warehouseID)){
            System.out.println("\n---------------------\n| Invalid WarehouseID! |\n\n---------------------\n"); 
            return;}

         String query = "DELETE FROM Warehouse WHERE Warehouse.WarehouseID = " + warehouseID;

         esql.executeUpdate(query);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }



}//end Amazon

