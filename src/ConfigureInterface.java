import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigureInterface extends Interface {

    private Stage stage;
    private User user;

    // Some UI elements are fields so they can be accessed throughout the class
    private ListView<String> lstNumbers;
    private TextField txtPhoneNumber;
    private Label lblError;

    public ConfigureInterface(Stage stage, User user) {
        // Assign the passed Properties object to the appropriate field
        this.stage = stage;
        this.user = user;

        // A Label to display which user is logged in at the top of the screen
        Label lblUsername = new Label(user.getHomeDepotUsername());
        lblUsername.getStyleClass().add("header-label");

        // Region to separate the username and logout button in the top container
        Region rgnSpacing = new Region();
        HBox.setHgrow(rgnSpacing, Priority.ALWAYS); // HGrow must be set for the element in order for it to separate

        // A Button to allow the user to logout and return to LoginInterface
        Button btnLogout = new Button("Logout");
        btnLogout.getStyleClass().add("logout-button");
        btnLogout.setPrefWidth(75);
        btnLogout.setOnAction(event -> logout());

        // An HBox to contain lblUsername, btnLogout, and the region that separates them
        HBox hbxHeader = new HBox(lblUsername, rgnSpacing, btnLogout);
        hbxHeader.getStyleClass().add("configuration-hbox");

        Label lblPhoneNumber = new Label("Phone #: " + user.twilioPhoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1)-$2-$3"));
        lblPhoneNumber.getStyleClass().add("phone-number-label");

        // Label to indicate that the ListView below contains recipient phone numbers
        Label lblRecipientPhoneNumbers = new Label("Recipient phone numbers (right click to remove)");
        lblRecipientPhoneNumbers.getStyleClass().add("field-label");

        // Create a ListView to contain all phone numbers
        // Set up removePhoneNumber() function to be executed upon right click
        // Execute updateListView() to add phone all of the phone numbers
        lstNumbers = new ListView<>();
        lstNumbers.setPrefHeight(75);
        lstNumbers.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                removePhoneNumber();
            }
        });
        updateListView();

        // A VBox to hold the recipient phone numbers ListView and it's Label
        VBox vbxRecipientPhoneNumbers = new VBox(lblRecipientPhoneNumbers, lstNumbers);

        // A title Label to indicate that the TextField below it is used to add a phone number
        Label lblAddPhoneNumber = new Label("Add phone number");
        lblAddPhoneNumber.setPadding(new Insets(0, 0, 0, 5));
        lblAddPhoneNumber.getStyleClass().add("field-label");

        // Text field to allow the user to add a phone number/recipient
        txtPhoneNumber = new TextField();

        // A Button to confirm addition of a new phone number found in the TextField
        // Set the addPhoneNumber() function to execute when the Button is clicked
        Button btnAdd = new Button("Add");
        btnAdd.setPrefWidth(75);
        btnAdd.getStyleClass().add("add-button");
        btnAdd.setOnAction(e -> {
            addPhoneNumber();
        });

        // HBox for phone number TextField and it's Button
        HBox hbxAddPhoneNumber = new HBox(txtPhoneNumber, btnAdd);
        HBox.setHgrow(txtPhoneNumber, Priority.ALWAYS);
        hbxAddPhoneNumber.getStyleClass().add("configuration-hbox");

        // VBox for hbxAddPhoneNumber and it's Label
        VBox vbxAddPhoneNumber = new VBox(lblAddPhoneNumber, hbxAddPhoneNumber);
        vbxAddPhoneNumber.setStyle("-fx-padding: 0 0 25 0");

        // Button to indicate that the user is finished adding phone numbers and is ready to send
        // Executes submitNumbers() when the Button is clicked
        Button btnFinish = new Button("Finish");
        btnFinish.setPrefSize(125, 30);
        btnFinish.setOnAction(e -> {
            finish();
        });

        // An error Label to indicate if there is a problem logging in
        lblError = new Label();
        lblError.getStyleClass().add("error-label");

        // Add all of the elements to the root container
        getChildren().addAll(
                hbxHeader,
                lblPhoneNumber,
                vbxRecipientPhoneNumbers,
                vbxAddPhoneNumber,
                btnFinish,
                lblError
        );
    }

    /* Logs the user out by clearing all fields in the User object and properties file, then redirecting to LoginInterface */
    private void logout() {
        // Clear all data from the properties and user object
        user.setHomeDepotUsername("");
        user.setHomeDepotPassword("");
        user.setTwilioAccountSID("");
        user.setTwilioAuthToken("");
        user.setTwilioPhoneNumber("");
        user.setRecipientPhoneNumbers(new String[]{""});

        // Switch to the LoginInterface scene
        Scene scene = new Scene(new LoginInterface(stage, user));
        stage.setScene(scene);
    }

    /* Adds a new phone number to the ListView and properties file */
    private void addPhoneNumber() {
        // Clear the error message in case it has already been set
        setErrorLabel("");

        // Get the phone number from the TextField
        String phoneNumber = txtPhoneNumber.getText();

        // Remove all possible characters from the phone number and make sure it is a 10 digit number
        phoneNumber = phoneNumber.replaceAll("[-.+() ]", "");
        if (!phoneNumber.matches("\\d{10}")) {
            setErrorLabel("Phone numbers should be 10 digits in length in the form\n###-###-####");
            return;
        }

        // Retrieve the list of numbers and check for any duplicates. If there are, send an error message and return.
        String[] recipientPhoneNumbers = user.getRecipientPhoneNumbers();
        for (String entry : recipientPhoneNumbers) {
            if (entry.equals(phoneNumber)) {
                setErrorLabel("This phone number is already on the list");
                return;
            }
        }

        // Since there are no duplicates, the number can be added to the list.
        // First, check if the list is empty
        if (recipientPhoneNumbers[0].equals("") || recipientPhoneNumbers == null) {
            recipientPhoneNumbers[0] = phoneNumber;
        }
        // If the list is not empty, we will add the phone number to the array
        else {
            int oldLength = recipientPhoneNumbers.length;

            // First, add all entries from the old array into the new one
            String[] temp = new String[oldLength + 1];
            for (int i = 0; i < oldLength; i++) {
                temp[i] = recipientPhoneNumbers[i];
            }
            // Then place the new phone number inside of the last slot of the new array
            temp[oldLength] = phoneNumber;
            recipientPhoneNumbers = temp;
        }

        // Set the field in the user object, ultimately saving it to the properties file as well
        user.setRecipientPhoneNumbers(recipientPhoneNumbers);

        // Clear the TextField and update the ListView to reflect the changes
        txtPhoneNumber.clear();
        updateListView();
    }

    /* Removes the chosen phone number from the ListView that contains all recipient phone numbers */
    private void removePhoneNumber() {
        // Retrieve the chosen phone number from the ListView
        String chosenNumber = lstNumbers.getSelectionModel().getSelectedItem();

        // Remove all of the extra characters that are added to the numbers when they are put into the ListView
        chosenNumber = chosenNumber.replaceAll("[-.+() ]", "");

        // Retrieve the numbers from the User object
        String[] temp = user.getRecipientPhoneNumbers();

        // Turn the native array into an ArrayList and remove the chosenNumber
        List<String> allNumbersList = new ArrayList<>(Arrays.asList(temp));
        allNumbersList.remove(chosenNumber);

        // Recreate the native array so that it can be sent to the User object
        String[] recipientPhoneNumbers = new String[allNumbersList.size()];
        for (int i = 0; i < recipientPhoneNumbers.length; i++) {
            recipientPhoneNumbers[i] = allNumbersList.get(i);
        }
        user.setRecipientPhoneNumbers(recipientPhoneNumbers);

        // Update the ListView
        updateListView();
    }

    /* This method should be called whenever the ListView needs to be updated */
    private void updateListView() {
        // Start by clearing out the ListView
        lstNumbers.getItems().clear();

        // Get the list of recipient phone numbers from the User object
        String[] phoneNumbers = user.getRecipientPhoneNumbers();

        // If there are no phone numbers yet, return and leave the ListView empty
        if (phoneNumbers.length == 0) {
            return;
        }

        // Add each phone number, one by one
        for (String phoneNumber : phoneNumbers) {
            lstNumbers.getItems().add(phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1)-$2-$3"));
        }
    }

    /* Switches to the CountdownInterface when the user clicks the Finish button */
    private void finish() {
        CountdownInterface countdownInterface = new CountdownInterface(stage, user);
        stage.setScene(new Scene(countdownInterface));
    }

    /* Method to set the error label in case a problem occurs when logging in */
    public void setErrorLabel(String error) {
        lblError.setText(error);
    }

}
