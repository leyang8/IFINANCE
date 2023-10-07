package se2203b.assignments.ifinance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.sql.*;

public class AccountCategoryAdapter {
    Connection connection;
    public AccountCategoryAdapter (Connection connection, Boolean reset) throws SQLException {
        this.connection = connection;
        Statement stmt = connection.createStatement();
        if (reset) {

            try {
                // Remove tables if database tables have been created.
                // This will throw an exception if the tables do not exist
                stmt.execute("DROP TABLE AccountCategories");
            } catch (SQLException ex) {
                // No need to report an error.
                // The table simply did not exist.
            }
        }
            try {
                // Create the table
                stmt.execute("CREATE TABLE AccountCategories ("
                        + "categoryName VARCHAR(50) NOT NULL PRIMARY KEY , "
                        + "categoryType VARCHAR(50) NOT NULL)");
                populateSamples();
            } catch (SQLException ex) {
                // No need to report an error.
                // The table exists and may have some data.
            }
        }


    private void populateSamples() throws SQLException {
        // Create four initial categories
        AccountCategory Assets = new AccountCategory("Assets","Debit");
        AccountCategory Liabilities = new AccountCategory("Liabilities","Credit");
        AccountCategory Income = new AccountCategory("Income","Credit");
        AccountCategory Expenses = new AccountCategory("Expenses","Debit");

        // Insert into table
        insertCategory(Assets);
        insertCategory(Liabilities);
        insertCategory(Income);
        insertCategory(Expenses);

    }

    public void insertCategory(AccountCategory accountCategory) throws SQLException {
        String insertSqlStatement = "INSERT INTO AccountCategories (categoryName, categoryType) " +
                "VALUES (?, ?)";
        PreparedStatement stmt = connection.prepareStatement(insertSqlStatement);
        stmt.setString(1,accountCategory.getName());
        stmt.setString(2, accountCategory.getType());
        stmt.executeUpdate();
    }

    public AccountCategory getAccountCategory(String categoryName) throws SQLException{
        AccountCategory accountCategory = new AccountCategory();
        String getSqlStatement = "SELECT * FROM AccountCategories WHERE categoryName = ?";
        PreparedStatement getStatement = connection.prepareStatement(getSqlStatement);
        getStatement.setString(1,categoryName);
        ResultSet result = getStatement.executeQuery();
        while (result.next()){
            accountCategory.setName(result.getString("categoryName"));
            accountCategory.setType(result.getString("categoryType"));
        }
        return accountCategory;
    }

    public TreeItem<String> getCategoryItem(String categoryName) throws SQLException {
        TreeItem<String> categoryItem = new TreeItem<>();
        // Create a string with a SELECT statement
        String sqlStatement = "SELECT * FROM AccountCategories WHERE categoryName = ?";
        PreparedStatement statement = connection.prepareStatement(sqlStatement);
        statement.setString(1,categoryName);

        // Execute the statement and return the result
        ResultSet result = statement.executeQuery();
        // Loop the entire rows of rs and set the string values of list
        while (result.next()){
            categoryItem.setValue(result.getString("categoryName"));
        }
        return categoryItem;
    }

}
