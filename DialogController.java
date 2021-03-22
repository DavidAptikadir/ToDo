package com.david;

import datamodule.ToDoData;
import datamodule.ToDoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField shortDescription;
    @FXML
    private TextArea itemDetails;
    @FXML
    private DatePicker deadline;

    public ToDoItem processResult(){
        String description=shortDescription.getText().trim();
        String details=itemDetails.getText();
        LocalDate deadlineValue=deadline.getValue();

        ToDoItem newItem=new ToDoItem(description,details,deadlineValue);
        ToDoData.getInstance().addToDoItem(newItem);

        return newItem;
    }

    public void editdItem(ToDoItem item){
        String description=shortDescription.getText().trim();
        String details=itemDetails.getText();
        LocalDate deadlineValue=deadline.getValue();

        ToDoData.getInstance().editItem(description,details,deadlineValue,item);
    }
}
