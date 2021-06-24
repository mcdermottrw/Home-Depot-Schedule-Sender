import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class Manager extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /* Initializes the program and the user interface */
    public void start(Stage stage) throws IOException {
        // Create a properties object and attempt to load config.properties into it
        Properties properties = new Properties();
        try {
            // A FileInputStream is required for properties.load()
            FileInputStream fileInputStream = new FileInputStream("res/config.properties");
            properties.load(fileInputStream);
        }
        catch(FileNotFoundException ex) {
            // If the file does not exist, just create it
            File propertiesFile = new File("res\\config.properties");
            propertiesFile.createNewFile();

            // Get the FileInputStream for the file so that it can be loaded using properties.load()
            FileInputStream fileInputStream = new FileInputStream("res/config.properties");
            properties.load(fileInputStream);

            // Set the properties that are required by the application
            properties.setProperty("username", "");
            properties.setProperty("password", "");
            properties.setProperty("accountSID", "");
            properties.setProperty("authToken", "");
            properties.setProperty("twilioPhoneNumber", "");
            properties.setProperty("phoneNumbers", "");

            // Save the file using FileOutputStream
            FileOutputStream fileOutputStream = new FileOutputStream("res/config.properties");
            properties.store(fileOutputStream, "File created");
        }

        // Create a LoadingInterface, adjust stage settings, and show the scene
        LoadingInterface loadingInterface = new LoadingInterface("Loading...");
        Scene scene = new Scene(loadingInterface);
        stage.setScene(scene);
        stage.setTitle("HD Schedule Scraper");
        stage.sizeToScene();
        stage.setResizable(false);
        stage.getIcons().add(new Image("file:res/icon.png"));
        stage.show();

        // Creates a user object, which makes it easier to access fields within the properties file, and validates all
        // of the data found within the properties file
        User user = new User(properties);
        user.validateProperties(stage, false);
    }

}
