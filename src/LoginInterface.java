import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginInterface extends Interface {

    private Stage stage;
    private User user;

    // Some UI elements are fields so they can be accessed throughout the class
    private TextField txtUsername;
    private PasswordField txtPassword;
    private TextField txtAccountSID;
    private TextField txtAuthToken;
    private TextField txtTwilioNumber;
    private Label lblError;

    /* Constructor to build the interface */
    public LoginInterface(Stage stage, User user) {
        // Assigns the passed parameters to their appropriate fields
        this.stage = stage;
        this.user = user;

        // A simple title label to indicate that the user is on the login page
        Label lblLogin = new Label("Login");
        lblLogin.getStyleClass().add("login-label");

        // A label to indicate that the text field below it is for the username
        Label lblUsername = new Label("Home Depot username");
        lblUsername.getStyleClass().add("field-label");

        // A text field for users to input their username
        txtUsername = new TextField();

        // A VBox to contain both the username label and text field
        // If we simply added these elements to the root VBox, the label could not be aligned left
        VBox vbxUsername = new VBox(lblUsername, txtUsername);
        vbxUsername.setMaxWidth(250);

        // A label to indicate that the text field below it is for the password
        Label lblPassword = new Label("Home Depot password");
        lblPassword.getStyleClass().add("field-label");

        // A text field for users to input their password
        // PasswordField hides any text typed into it like a normal password field would
        txtPassword = new PasswordField();

        // A VBox to contain both the password label and text field
        VBox vbxPassword = new VBox(lblPassword, txtPassword);
        vbxPassword.setMaxWidth(250);

        // A label to indicate that the text field below it is for the Twilio Account SID
        Label lblAccountSID = new Label("Twilio account SID");
        lblAccountSID.getStyleClass().add("field-label");

        // A text field for users to input their account SID
        txtAccountSID = new TextField();

        // A VBox to contain the account SID label and text field
        VBox vbxAccountSID = new VBox(lblAccountSID, txtAccountSID);
        vbxAccountSID.setMaxWidth(250);

        // A label to indicate that the text field below it is for the Twilio Auth Token
        Label lblAuthToken = new Label("Twilio auth token");
        lblAuthToken.getStyleClass().add("field-label");

        // A text field for users to input their auth token
        txtAuthToken = new TextField();

        // A VBox to contain the auth token label and text field
        VBox vbxAuthToken = new VBox(lblAuthToken, txtAuthToken);
        vbxAuthToken.setMaxWidth(250);

        // A label to indicate that the text field below it is for the Twilio Auth Token
        Label lblTwilioNumber = new Label("Twilio phone number");
        lblTwilioNumber.getStyleClass().add("field-label");

        // A text field for users to input their auth token
        txtTwilioNumber = new TextField();

        // A VBox to contain the auth token label and text field
        VBox vbxTwilioNumber = new VBox(lblTwilioNumber, txtTwilioNumber);
        vbxTwilioNumber.setMaxWidth(250);

        // A Region to add extra spacing above the login button
        Region rgnSpacing = new Region();
        rgnSpacing.setPadding(new Insets(0, 0, 5, 0));

        // A Button to submit the form
        Button btnLogin = new Button("Login");
        btnLogin.setPrefSize(100, 30);
        btnLogin.setOnAction(e -> {
            // Send the login data that the user just submitted to the User object. Doing so will set the fields for
            // the object and save the data to the properties file. Once this is done, the data will be validated and
            // the user will receive an error message if there are any issues.
            setLoginData();
            user.validateProperties(stage, true);
        });

        // An error Label to indicate if there is a problem logging in
        lblError = new Label();
        lblError.getStyleClass().add("error-label");

        // Add all of the elements to the root VBox
        getChildren().addAll(
                lblLogin,
                vbxUsername,
                vbxPassword,
                vbxAccountSID,
                vbxAuthToken,
                vbxTwilioNumber,
                rgnSpacing,
                btnLogin,
                lblError
        );
    }

    /* Sets all of the login data from the form in the User object/properties object/properties file */
    public void setLoginData() {
        // Retrieve all entries from the form
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String accountSID = txtAccountSID.getText();
        String authToken = txtAuthToken.getText();
        String twilioPhoneNumber = txtTwilioNumber.getText().replaceAll("[-.+() ]", "");

        // Set all of the entries in the User object. When this is done, they will also be saved to the properties file.
        user.setHomeDepotUsername(username);
        user.setHomeDepotPassword(password);
        user.setTwilioAccountSID(accountSID);
        user.setTwilioAuthToken(authToken);
        user.setTwilioPhoneNumber(twilioPhoneNumber);

        // Switch to ConfigureInterface
        Scene scene = new Scene(new ConfigureInterface(stage, user));
        stage.setScene(scene);
    }

    /* Method to set the error label in case a problem occurs when logging in */
    public void setErrorLabel(String error) {
        lblError.setText(error);
    }

}
