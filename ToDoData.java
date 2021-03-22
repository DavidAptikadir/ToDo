package datamodule;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;


public class ToDoData {
    private static ToDoData instance= new ToDoData();
    private static String filename="ToDoListItems.txt";

    private ObservableList<ToDoItem> toDoItems;
    private DateTimeFormatter formatter;

    public void addToDoItem(ToDoItem item)
    {
        toDoItems.add(item);
    }

    public void deleteItem(ToDoItem item){
        toDoItems.remove(item);
    }

    public void editItem(String shortDescription,String details,LocalDate deadline,ToDoItem item){
        if(shortDescription!=null){
            item.setShortDescription(shortDescription);
        }
        if(details!=null){
            item.setDetails(details);
        }
        if(deadline!=null){
            item.setDeadline(deadline);
        }
    }

    public static ToDoData getInstance()
    {
        return  instance;
    }

    private ToDoData()
    {
        formatter= DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public ObservableList<ToDoItem> getToDoItems() {
        return toDoItems;
    }

    public void loadToDoItems() throws IOException{
        toDoItems= FXCollections.observableArrayList();
        Path path= Paths.get(filename);
        BufferedReader br= Files.newBufferedReader(path);


        String input;

        try{
            while((input=br.readLine())!=null){
                String[] itemPieces=input.split("\t");

                String shortDescription=itemPieces[0];
                String details=itemPieces[1];
                String dataString=itemPieces[2];

                LocalDate date=LocalDate.parse(dataString, formatter);
                ToDoItem todoItem=new ToDoItem(shortDescription,details,date);
                toDoItems.add(todoItem);
            }
        }finally {
            if(br!=null)
            {
                br.close();
            }
        }
    }

    public void storeTodoItems() throws IOException{
        Path path=Paths.get(filename);
        BufferedWriter bw=Files.newBufferedWriter(path);

        try{
            Iterator<ToDoItem> iter=toDoItems.iterator();
            while(iter.hasNext())
            {
                ToDoItem item=iter.next();
                bw.write(String.format("%s\t%s\t%s",
                        item.getShortDescription(),
                        item.getDetails(),
                        item.getDeadline().format(formatter)));
                bw.newLine();
            }
        }finally {
            if(bw!=null)
            {
                bw.close();
            }
        }
    }

}
