package com.david;

import datamodule.ToDoData;
import datamodule.ToDoItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {


    private List<ToDoItem> todoItems;
    @FXML
    private ListView<ToDoItem> toDoListView;
    @FXML
    private TextArea itemDetails;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu contextMenu;
    @FXML
    private ToggleButton filterToggleButton;
    private FilteredList<ToDoItem> filteredList;
    private Predicate<ToDoItem> wantAllItems;
    private Predicate<ToDoItem> wantTodayItems;

    public void initialize() {

        contextMenu=new ContextMenu();
        MenuItem deleteMenuItem=new MenuItem("Delete");
        MenuItem editMenuItem=new MenuItem("Edit");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ToDoItem item=toDoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ToDoItem item=toDoListView.getSelectionModel().getSelectedItem();
                editItem(item);
            }
        });

        contextMenu.getItems().addAll(deleteMenuItem);
        contextMenu.getItems().addAll(editMenuItem);
        toDoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observableValue, ToDoItem toDoItem, ToDoItem newValue) {
                if (newValue != null) {
                    ToDoItem item = toDoListView.getSelectionModel().getSelectedItem();
                    itemDetails.setText(item.getDetails());
                    deadlineLabel.setText(item.getDeadline().toString());
                }
            }
        });

        wantAllItems=new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem toDoItem) {
                return true;
            }
        };

        wantTodayItems=new Predicate<ToDoItem>(){
            @Override
            public boolean test(ToDoItem toDoItem) {
                return (toDoItem.getDeadline().equals(LocalDate.now()));
            }
        };

        filteredList=new FilteredList<ToDoItem>(ToDoData.getInstance().getToDoItems(), new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem toDoItem) {
                return true;
            }
        });
        SortedList<ToDoItem> sortedList=new SortedList<ToDoItem>(filteredList, new Comparator<ToDoItem>() {
            @Override
            public int compare(ToDoItem toDoItem, ToDoItem t1) {
                return toDoItem.getDeadline().compareTo(t1.getDeadline());
            }
        });

        toDoListView.setItems(filteredList);
        toDoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        toDoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>() {
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> toDoItemListView) {
                ListCell<ToDoItem> cell=new ListCell<>() {
                    @Override
                    protected void updateItem(ToDoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        }else{
                            setText(item.getShortDescription());
                            if(item.getDeadline().isBefore(LocalDate.now().plusDays(1))){
                                setTextFill(Color.RED);
                            }else if(item.getDeadline().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.PURPLE);
                            }
                        }

                    }
                };
                cell.emptyProperty().addListener(
                        (obs,wasEmpty,isNowEmpty)->{
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            }else{
                                cell.setContextMenu(contextMenu);
                            }
                        }
                );
                return cell;
            }
        });

    }

    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("Dialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Could't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("Okay was pressed");
            DialogController controller = fxmlLoader.getController();
            ToDoItem newItem = controller.processResult();
            toDoListView.getSelectionModel().select(newItem);
        }
    }

    public void deleteItem(ToDoItem item){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete ToDo Item");
        alert.setHeaderText("Delete item "+item.getShortDescription());
        alert.setContentText("Are you sure ,press OK to confirm");
        Optional<ButtonType> result=alert.showAndWait();
        if(result.isPresent()&& result.get()==ButtonType.OK){
            ToDoData.getInstance().deleteItem(item);
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent){
        ToDoItem selectedItem=toDoListView.getSelectionModel().getSelectedItem();
        if(selectedItem!=null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                deleteItem(selectedItem);
            }
        }
    }

    public void editItem(ToDoItem item){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Change Item");
        alert.setHeaderText("Change "+item.getShortDescription());

        Optional<ButtonType> result= alert.showAndWait();

        if(result.isPresent()&&result.get()==ButtonType.OK) {
            Dialog<ButtonType> dialog=new Dialog<>();
            dialog.initOwner(mainBorderPane.getScene().getWindow());
            FXMLLoader fxmlLoader=new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("Dialog.fxml"));
            try{
                dialog.getDialogPane().setContent(fxmlLoader.load());
            }catch(IOException e){
                System.out.println("System couldn't load the dialog");
                return;
            }
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            Optional<ButtonType> dialogResult=dialog.showAndWait();
            if(dialogResult.isPresent()&& dialogResult.get()==ButtonType.OK){
                DialogController controller=fxmlLoader.getController();
                controller.editdItem(item);
            }
        }

    }

    @FXML
    public void handleFilterButton(){
        ToDoItem selectedItem=toDoListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(wantTodayItems);
            if(filteredList.isEmpty()){
                itemDetails.clear();
                deadlineLabel.setText("");
            }else if(filteredList.contains(selectedItem)){
                toDoListView.getSelectionModel().select(selectedItem);
            }else{
                toDoListView.getSelectionModel().selectFirst();
            }
        }else{
            filteredList.setPredicate(wantAllItems);
            toDoListView.getSelectionModel().select(selectedItem);
        }
    }
}

