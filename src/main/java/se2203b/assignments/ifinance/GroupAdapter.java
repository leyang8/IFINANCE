package se2203b.assignments.ifinance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.sql.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class GroupAdapter {
    Connection connection;
    AccountCategoryAdapter categoryAdapter;
    String currentUser;

    public GroupAdapter (Connection connection,String userName) throws SQLException {
        this.connection = connection;
        this.currentUser = userName;
        Statement stmt = connection.createStatement();
        try {
            String sqlStatement = "SELECT * FROM APP.ACCOUNTGROUPS";
            stmt.executeQuery(sqlStatement);

        }catch (SQLException e) {
            stmt.execute("CREATE TABLE AccountGroups ("
                    + "userName VARCHAR(15) NOT NULL, "
                    + "groupID INT NOT NULL PRIMARY KEY , "
                    + "groupName VARCHAR(50) NOT NULL UNIQUE , "
                    + "groupParent INT , "
                    + "groupElement VARCHAR(50) , "
                    + "FOREIGN KEY (groupParent) REFERENCES AccountGroups(groupID), "
                    + "FOREIGN KEY (groupElement) REFERENCES AccountCategories(categoryName)"
                    + ")");
            populateSamples();
        }
    }

    public void setCategoryAdapter(AccountCategoryAdapter adapter){
        categoryAdapter = adapter;
    }
    private void populateSamples() throws SQLException {
        //create four groupElement
        AccountCategoryAdapter categoryAdapter = new AccountCategoryAdapter(connection,false);
        AccountCategory Assets = categoryAdapter.getAccountCategory("Assets");
        AccountCategory Liabilities = categoryAdapter.getAccountCategory("Liabilities");
        AccountCategory Income = categoryAdapter.getAccountCategory("Income");
        AccountCategory Expenses = categoryAdapter.getAccountCategory("Expenses");

        //create empty parent and insert into table as ID 0
        Group emptyParent = new Group();
        insertGroup(emptyParent,currentUser);

        Group FixedAssets = new Group(1,"Fixed assets",emptyParent,Assets);
        insertGroup(FixedAssets,currentUser);
        Group Investments = new Group(2,"Investments",emptyParent,Assets);
        insertGroup(Investments,currentUser);
        Group Branch = new Group(3,"Branch/divisions",emptyParent,Assets);
        insertGroup(Branch,currentUser);
        Group Cash = new Group(4,"Cash in hand",emptyParent,Assets);
        insertGroup(Cash,currentUser);
        Group BankAccounts = new Group(5,"Bank accounts",emptyParent,Assets);
        insertGroup(BankAccounts,currentUser);
        Group Deposits = new Group(6,"Deposits (assets)",emptyParent,Assets);
        insertGroup(Deposits,currentUser);
        Group Advances = new Group(7,"Advances (assets)",emptyParent,Assets);
        insertGroup(Advances,currentUser);
        Group CapitalAccount = new Group(8, "Capital account",emptyParent,Liabilities);
        insertGroup(CapitalAccount,currentUser);
        Group LongTermLoans = new Group(9, "Long term loans",emptyParent,Liabilities);
        insertGroup(LongTermLoans,currentUser);
        Group CurrentLiabilities = new Group(10,"Current liabilities",emptyParent,Liabilities);
        insertGroup(CurrentLiabilities,currentUser);
        Group ReservesAndSurplus = new Group(11, "Reserves and surplus",emptyParent,Liabilities);
        insertGroup(ReservesAndSurplus,currentUser);
        Group SalesAccount = new Group(12,"Sales account",emptyParent,Income);
        insertGroup(SalesAccount,currentUser);
        Group PurchaseAccount = new Group(13, "Purchase account",emptyParent,Expenses);
        insertGroup(PurchaseAccount,currentUser);
        Group ExpensesDirect = new Group(14,"Expenses (direct)",emptyParent,Expenses);
        insertGroup(ExpensesDirect,currentUser);
        Group ExpensesIndirect = new Group(15,"Expenses (indirect)",emptyParent,Expenses);
        insertGroup(ExpensesIndirect,currentUser);
        Group SecuredLoans = new Group(16,"Secured loans",LongTermLoans,Liabilities);
        insertGroup(SecuredLoans,currentUser);
        Group UnsecuredLoans = new Group(17,"Unsecured loans",LongTermLoans,Liabilities);
        insertGroup(UnsecuredLoans,currentUser);
        Group DutiesTaxesPayable = new Group(18, "Duties taxes payable",CurrentLiabilities,Liabilities);
        insertGroup(DutiesTaxesPayable,currentUser);
        Group Provisions = new Group(19,"Provisions",CurrentLiabilities,Liabilities);
        insertGroup(Provisions,currentUser);
        Group SundryCreditors = new Group(20,"Sundry creditors",CurrentLiabilities,Liabilities);
        insertGroup(SundryCreditors,currentUser);
        Group BankOdAndLimits = new Group(21,"Bank od & limits",CurrentLiabilities,Liabilities);
        insertGroup(BankOdAndLimits,currentUser);
    }
    public int getMax() throws SQLException {
        int num = 0;

        // Add your work code here for Task #3
        Statement statement = connection.createStatement();
        String sqlStatement = "SELECT MAX(groupID) FROM AccountGroups";
        ResultSet result = statement.executeQuery(sqlStatement);
        while (result.next()){
            num += result.getInt(1);
        }
        return num;
    }

    public Group getGroup(String groupName,String userName) throws SQLException{

        Group wantedGroup = new Group();
        String getSqlStatement = "SELECT * from AccountGroups WHERE groupName = ? AND userName = ?";
        PreparedStatement getStatement = connection.prepareStatement(getSqlStatement);
        getStatement.setString(1,groupName);
        getStatement.setString(2,userName);

        ResultSet result = getStatement.executeQuery();
        Group groupParent = new Group();

        while (result.next()){

            //set id and name
            wantedGroup.setID(result.getInt("groupID"));
            wantedGroup.setName(result.getString("groupName"));

            //find groupParent
            if(result.getInt("groupParent") != 0) {
                String findParentSqlStatement = "SELECT * from AccountGroups WHERE groupID = ? AND userName = ? ";
                PreparedStatement parentStatement = connection.prepareStatement(findParentSqlStatement);
                parentStatement.setInt(1,result.getInt("groupParent"));
                parentStatement.setString(2,userName);
                ResultSet parentResult = parentStatement.executeQuery();
                parentResult.next();
                groupParent = getGroup(parentResult.getString("groupName"),userName);


            }
            wantedGroup.setParent(groupParent);
            //find groupElement
            AccountCategory groupElement = categoryAdapter.getAccountCategory(result.getString("groupElement"));
            wantedGroup.setElement(groupElement);
        }
        return wantedGroup;
    }

    public void insertGroup(Group group,String userName) throws SQLException {
        String insertSqlStatement = "INSERT INTO AccountGroups (userName, groupID, groupName, groupParent, groupElement) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(insertSqlStatement);
        stmt.setString(1,userName);
        stmt.setInt(2,group.getID());
        stmt.setString(3, group.getName());

        //if inserting zero group, set parent and element column as null
        try {
            stmt.setInt(4,group.getParent().getID());
            stmt.setString(5,group.getElement().getName());
        }catch (NullPointerException e){
            stmt.setNull(4, java.sql.Types.INTEGER);
            stmt.setNull(5,java.sql.Types.INTEGER);
        }
        stmt.executeUpdate();
    }
    public void modifyGroup(Group group,String userName) throws SQLException{
        String sqlModifyStatement = "UPDATE AccountGroups "+
                "SET groupName = ?"+
                "WHERE "+
                "userName = ? AND groupID = ?";
        PreparedStatement statement = connection.prepareStatement(sqlModifyStatement);
        statement.setString(1,group.getName());
        statement.setString(2,userName);
        statement.setInt(3,group.getID());
        statement.executeUpdate();
    }

    public void deleteGroup(Group group, String userName) throws SQLException{
        String sqlStatement = "DELETE FROM AccountGroups "+
                "WHERE groupID = ? AND UserName = ?";
        PreparedStatement statement = connection.prepareStatement(sqlStatement);
        statement.setInt(1,group.getID());
        statement.setString(2,userName);
        statement.executeUpdate();
    }


    public TreeItem<String> finalList (String userName, String category, int parent) throws SQLException{
        //get the category tree item
        AccountCategoryAdapter categoryAdapter = new AccountCategoryAdapter(connection,false);
        TreeItem<String> categoryItem = categoryAdapter.getCategoryItem(category);

        return addGroupNamesList(categoryItem,userName,category,parent);
    }
    public TreeItem<String> addGroupNamesList(TreeItem<String> rootNode, String userName, String category, int parent) throws SQLException {

        //first level group, retrieved by category name
        // Create a string with a SELECT statement
        String sqlStatement = "SELECT * FROM AccountGroups WHERE userName = ? And groupElement = ? AND groupParent = ?";
        PreparedStatement statement = connection.prepareStatement(sqlStatement);
        statement.setString(1,userName);
        statement.setString(2,category);
        statement.setInt(3,parent);

        // Execute the statement and return the result
        ResultSet result = statement.executeQuery();

        // Loop the entire rows of rs and set the string values of list
        while (result.next()){
            TreeItem<String> level1Item = new TreeItem<>(result.getString("groupName"));
            rootNode.getChildren().add(level1Item);
            addGroupNamesList(level1Item, userName, category, result.getInt("groupID"));
        }
        return rootNode;
    }
}

