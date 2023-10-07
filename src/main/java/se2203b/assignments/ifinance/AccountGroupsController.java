package se2203b.assignments.ifinance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AccountGroupsController implements Initializable {
    public TreeView<String> groupPresentingTree;

    private final ObservableList<String> data = FXCollections.observableArrayList();
    public Button exitButton;
    public Button saveButton;
    public TextField groupNameInputTextField;
    public MenuItem addMenu;
    public MenuItem changeMenu;
    public MenuItem deleteMenu;
    private String newGroupName;
    private AccountCategoryAdapter accountCategoryAdapter;
    private GroupAdapter groupAdapter;
    public ContextMenu contextMenu = new ContextMenu();
    public TreeItem<String> selectedItem;
    private final Group newGroup = new Group();
    private String currentUser;
    private String currentState;

    public void setAdapters(AccountCategoryAdapter accountCategoryAdapter, GroupAdapter groupAdapter,String userName) throws SQLException {
        this.accountCategoryAdapter = accountCategoryAdapter;
        this.groupAdapter = groupAdapter;
        currentUser = userName;
        buildTreeData();
    }

    public void buildTreeData() throws SQLException {
        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        groupPresentingTree.setRoot(root);
        groupPresentingTree.setShowRoot(false);

        root.getChildren().add(groupAdapter.finalList(currentUser,"Assets",0));
        root.getChildren().add(groupAdapter.finalList(currentUser,"Liabilities",0));
        root.getChildren().add(groupAdapter.finalList(currentUser,"Income",0));
        root.getChildren().add(groupAdapter.finalList(currentUser,"Expenses",0));

    }

    public void exitButtonListener(ActionEvent actionEvent) {
        // Get current stage reference
        Stage stage = (Stage) exitButton.getScene().getWindow();
        // Close stage
        stage.close();
    }

    public void groupNameInputTextFieldListener(KeyEvent keyEvent) {
        newGroupName = groupNameInputTextField.getText();
    }

    public void saveButtonListener(ActionEvent actionEvent) throws SQLException {
        Group parent = new Group();
        if (currentState.equals("add")){
            //get new groupID
            int newID = groupAdapter.getMax() + 1;
            //set new groupID and group Name
            newGroup.setID(newID);

            if (selectedItem.getValue().equals(accountCategoryAdapter.getCategoryItem(selectedItem.getValue()).getValue())){
                newGroup.setParent(parent);
                newGroup.setElement(accountCategoryAdapter.getAccountCategory(selectedItem.getValue()));
            }else {
                parent = groupAdapter.getGroup(selectedItem.getValue(),currentUser);
                newGroup.setParent(parent);

                AccountCategory element = parent.getElement();
                newGroup.setElement(element);
            }
            newGroup.setName(newGroupName);
            try {
                //insert into database
                groupAdapter.insertGroup(newGroup,currentUser);
                //insert into treeView
                selectedItem.getChildren().add(new TreeItem<>(newGroupName));
            }catch (SQLException exception){
                displayAlert("Unavailable New Group Name!");
            }

        }else if (currentState.equals("change")){
            Group changeGroup = groupAdapter.getGroup(selectedItem.getValue(),currentUser);
            changeGroup.setName(newGroupName);

            try {
                groupAdapter.modifyGroup(changeGroup,currentUser);
                selectedItem.setValue(newGroupName);
            }catch (SQLException exception){
                displayAlert("Unavailable New Group Name!");
            }
        }
        groupNameInputTextField.setText("");
        groupNameInputTextField.setDisable(true);
        saveButton.setDisable(true);
    }

    public void setMenuItemDisable(MouseEvent mouseEvent) throws SQLException {
        //get selected item
        selectedItem = groupPresentingTree.getSelectionModel().getSelectedItem();
        //if the selected item is category, disable change and delete
        if (selectedItem.getValue().equals(accountCategoryAdapter.getCategoryItem(selectedItem.getValue()).getValue())){
            addMenu.setDisable(false);
            changeMenu.setDisable(true);
            deleteMenu.setDisable(true);
            // if the selected item has children(not leaf), disable delete
        }else if (!selectedItem.isLeaf()){
            addMenu.setDisable(false);
            changeMenu.setDisable(false);
            deleteMenu.setDisable(true);
        }else {
            addMenu.setDisable(false);
            changeMenu.setDisable(false);
            deleteMenu.setDisable(false);
        }
    }

    public void addMenuListener(ActionEvent actionEvent){
        currentState = "add";
        groupNameInputTextField.setDisable(false);
        saveButton.setDisable(false);
        groupNameInputTextField.requestFocus();
        contextMenu.hide();
    }

    public void changeMenuListener(ActionEvent actionEvent){
        currentState = "change";
        groupNameInputTextField.setDisable(false);
        saveButton.setDisable(false);
        groupNameInputTextField.setText(selectedItem.getValue());
        groupNameInputTextField.requestFocus();
        contextMenu.hide();
    }

    public void deleteMenuListener(ActionEvent actionEvent) throws SQLException {
        contextMenu.hide();
        Group deleteGroup = groupAdapter.getGroup(selectedItem.getValue(),currentUser);
        groupAdapter.deleteGroup(deleteGroup,currentUser);
        selectedItem.getParent().getChildren().remove(selectedItem);
    }
    private void displayAlert(String msg) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Alert.fxml"));
            Parent ERROR = loader.load();
            AlertController controller = (AlertController) loader.getController();

            Scene scene = new Scene(ERROR);
            Stage stage = new Stage();
            stage.setScene(scene);

            stage.getIcons().add(new Image("file:src/main/resources/se2203b/assignments/ifinance/UWOlogo.png"));
            controller.setAlertText(msg);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException ex1) {

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}
}
