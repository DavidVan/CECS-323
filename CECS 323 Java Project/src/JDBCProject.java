
import java.sql.*;
import java.util.Scanner;

public class JDBCProject {

    static String USER;
    static String PASS;
    static String DBNAME;
    static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    static String DB_URL = "jdbc:derby://localhost:1527/";
    static Connection conn;
    static Statement stmt;

    public static void main(String[] args) {
        // Get credentials
        Scanner in = new Scanner(System.in);
        System.out.print("Name of the database (not the user account): ");
        DBNAME = in.nextLine();
        System.out.print("Database user name: ");
        USER = in.nextLine();
        System.out.print("Database password: ");
        PASS = in.nextLine();
        DB_URL = DB_URL + DBNAME + ";user=" + USER + ";password=" + PASS;
        conn = null;
        stmt = null;
        while(true) {
            printMenu();
            Scanner getChoice = new Scanner(System.in);
            int choice = getChoice.nextInt();
            System.out.println(choice);
            while (choice < 1 || choice > 6) {
                System.out.println("Invalid choice!");
                printMenu();
                choice = getChoice.nextInt();
            }

            if (choice == 1) {
                listAllAlbums();
            }
            else if (choice == 2) {
                listDataSpecific();
            }
            else if (choice == 3) {
                insertTitle();
            }
            else if (choice == 4) {
                insertStudioAndUpdateAll();
            }
            else if (choice == 5) {
                removeAlbum();
            }
            else if (choice == 6) {
                System.exit(0);
            }
        }
    }

    public static void printMenu() {
        System.out.println("Menu\n"
                + "1. List all album titles\n"
                + "2. List data for a specific album\n"
                + "3. Insert a new album\n"
                + "4. Insert a new studio and update all albums published by one"
                + "studio to be published by the new studio\n"
                + "5. Remove an album\n"
                + "6. Quit");
        System.out.print("Please select an option: ");
    }

    public static String dispNull(String input) {
        //because of short circuiting, if it's null, it never checks the length.
        if (input == null || input.length() == 0) {
            return "N/A";
        }
        else {
            return input;
        }
    }
    
    public static void listAllAlbums() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM Albums";
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Album Titles:");
            while (rs.next()) {
                //Retrieve by column name
                String albumTitle = rs.getString("AlbumTitle");
                //Display values
                System.out.println(albumTitle);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
        catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            }
            catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void listDataSpecific() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            String sql;
            String displayFormat = "%-60s%-50s%-100s%-60s%-25s%-45s%-60s%-25s%-60s%-20s%-3s\n";
            
            System.out.print("From which album do you want to view data? ");
            Scanner getAlbum = new Scanner(System.in);
            String albumChoice = getAlbum.nextLine();
            
            sql = "SELECT * FROM Albums NATURAL JOIN RecordingStudios NATURAL JOIN RecordingGroups WHERE AlbumTitle = '" + albumChoice + "'";
            ResultSet rs = stmt.executeQuery(sql);
            
            while (!rs.next()) {
                System.out.println("Invalid entry!");
                System.out.print("From which album do you want to view data? ");
                albumChoice = getAlbum.nextLine();
                sql = "SELECT * FROM Albums NATURAL JOIN RecordingStudios NATURAL JOIN RecordingGroups WHERE AlbumTitle = '" + albumChoice + "'";
                rs = stmt.executeQuery(sql);
            }
            
            // We got a valid ResultSet, so let's move the pointer back!
            rs = stmt.executeQuery(sql);
           
            System.out.printf(displayFormat, "Album Title", "Studio Name", "Studio Address", "Studio Owner", "Studio Phone", "Group Name", "Lead Singer", "Year Formed", "Genre", "Date Recorded", "Length");

            while (rs.next()) {
                //Retrieve by column name
                String albumTitle = rs.getString("AlbumTitle");
                String studioName = rs.getString("StudioName");
                String studioAddress = rs.getString("StudioAddress");
                String studioOwner = rs.getString("StudioOwner");
                String studioPhone = rs.getString("StudioPhone");
                String groupName = rs.getString("GroupName");
                String leadSinger = rs.getString("LeadSinger");
                String yearFormed = rs.getString("YearFormed");
                String genre = rs.getString("Genre");
                String dateRecorded = rs.getString("DateRecorded");
                String length = rs.getString("Length");

                //Display values
                System.out.printf(displayFormat, albumTitle, studioName, studioAddress, studioOwner, studioPhone, groupName, leadSinger, yearFormed, genre, dateRecorded, length);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
        catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            }
            catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void insertTitle() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            String sql;
            String displayFormat = "%-60s%-50s%-100s%-60s%-25s%-45s%-60s%-25s%-60s%-20s%-3s\n";
            
            System.out.print("What is the title of the album? ");
            Scanner getAlbum = new Scanner(System.in);
            String albumName = getAlbum.nextLine();
            
            System.out.print("Which Studio's name? ");
            Scanner getStudio = new Scanner(System.in);
            String studioChoice = getStudio.nextLine();
            
            sql = "SELECT StudioName FROM RecordingStudios";
            ResultSet rs = stmt.executeQuery(sql);
            
            boolean foundStudio = false;
            
            while (rs.next()) {
                String studioName = rs.getString("StudioName");
                if (studioName.equals(studioChoice)) {
                    foundStudio = true;
                    break;
                }
            }
            
            while (!foundStudio) {
                System.out.println("That studio does not exist! Please enter a valid group!");
                System.out.println("Here is a list of valid groups: ");
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String studioName = rs.getString("StudioName");
                    System.out.println(studioName);
                }
                System.out.print("Enter a studio: ");
                studioChoice = getStudio.nextLine();
                
                rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String studioName = rs.getString("StudioName");
                    if (studioName.equals(studioChoice)) {
                        foundStudio = true;
                        break;
                    }
                }
            }
            
            System.out.print("Which Group's name? ");
            Scanner getGroup = new Scanner(System.in);
            String groupChoice = getGroup.nextLine();
            
            sql = "SELECT GroupName FROM RecordingGroups";
            rs = stmt.executeQuery(sql);
            
            boolean foundGroup = false;
            
            while (rs.next()) {
                String groupName = rs.getString("GroupName");
                if (groupName.equals(groupChoice)) {
                    foundGroup = true;
                    break;
                }
            }
            
            while (!foundGroup) {
                System.out.println("That group does not exist! Please enter a valid group!");
                System.out.println("Here is a list of valid groups: ");
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String groupName = rs.getString("GroupName");
                    System.out.println(groupName);
                }
                System.out.print("Enter a group: ");
                groupChoice = getGroup.nextLine();
                
                rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    String groupName = rs.getString("GroupName");
                    if (groupName.equals(groupChoice)) {
                        foundGroup = true;
                        break;
                    }
                }
            }
            
            System.out.print("When was it recorded? Please enter in yyyy-mm-dd format! ");
            Scanner getDate = new Scanner(System.in);
            String dateRecorded = getDate.nextLine();
            
            System.out.print("How long is it, in minutes? ");
            Scanner getLength = new Scanner(System.in);
            int length = getLength.nextInt();
            rs = stmt.executeQuery(sql);
            
            sql = "INSERT INTO Albums(AlbumTitle, StudioName, GroupName, DateRecorded, \"Length\") VALUES ('" + albumName + "', '" + studioChoice + "', '" + groupChoice + "', '" + dateRecorded + "', " + length + ")";
            stmt.execute(sql);
            
            System.out.println("Added album!");
            
            rs.close();
            stmt.close();
            conn.close();
        }
        catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
        catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            }
            catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void insertStudioAndUpdateAll() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            String sql;
            String displayFormat = "%-60s%-50s%-100s%-60s%-25s%-45s%-60s%-25s%-60s%-20s%-3s\n";
            
            System.out.print("What is the name of the new studio? ");
            Scanner getStudio = new Scanner(System.in);
            String studioName = getStudio.nextLine();
            
            System.out.print("What is the name of the studio you're replacing? ");
            Scanner getStudioReplacing = new Scanner(System.in);
            String studioNameReplacing = getStudioReplacing.nextLine();
            
            System.out.print("What is the address of the new studio? ");
            Scanner getStudioAddress = new Scanner(System.in);
            String studioAddress = getStudioAddress.nextLine();
            
            System.out.print("Who owns the new studio? ");
            Scanner getStudioOwner = new Scanner(System.in);
            String studioOwner = getStudioOwner.nextLine();
            
            System.out.print("What is the owner's phone number? ");
            Scanner getStudioPhone = new Scanner(System.in);
            String studioPhone = getStudioPhone.nextLine();
            
            sql = "INSERT INTO RecordingStudios(StudioName, StudioAddress, StudioOwner, StudioPhone) VALUES ('" + studioName + "', '" + studioAddress + "', '" + studioOwner + "', '" + studioPhone + "')";
            stmt.execute(sql);
            
            System.out.println("Added studio!");
            
            sql = "UPDATE Albums SET StudioName = '" + studioName + "' WHERE StudioName = '" + studioNameReplacing + "'";
            stmt.execute(sql);
            
            System.out.println("Albums from the new studio: ");
            
            sql = "SELECT AlbumTitle FROM Albums WHERE StudioName = '" + studioName + "'";
            
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                //Retrieve by column name
                String albumTitle = rs.getString("AlbumTitle");
                //Display values
                System.out.println(albumTitle);
            }
            
            rs.close();
            stmt.close();
            conn.close();
        }
        catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
        catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            }
            catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
    public static void removeAlbum() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            String sql;
            String displayFormat = "%-60s%-50s%-100s%-60s%-25s%-45s%-60s%-25s%-60s%-20s%-3s\n";
            
            System.out.print("What is the title of the album you want to remove? ");
            Scanner getAlbum = new Scanner(System.in);
            String albumName = getAlbum.nextLine();
            
            sql = "DELETE FROM Albums WHERE AlbumTitle = '" + albumName + "'";
            stmt.execute(sql);
            
            System.out.println("Removed!");
            
            stmt.close();
            conn.close();
        }
        catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        }
        catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            }
            catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
    
}
