import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jasypt.util.text.AES256TextEncryptor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/*
 * A class built to hold the Properties object and disperse each entry from it into a single field making the data
 * easier to access throughout the application. The class holds methods to set, save, get, and validate data from the
 * Properties object/file in a more efficient manner.
 */
public class User {

    String homeDepotUsername;
    String homeDepotPassword; // Encrypted in the file
    String twilioAccountSID;
    String twilioAuthToken;
    String twilioPhoneNumber;
    String[] recipientPhoneNumbers;

    private Properties properties;

    /* Sets the properties field for use throughout the class */
    public User(Properties properties) {
        // This object instance holds the properties from the properties file, but the file must still be saved
        // whenever any properties are changed on the object
        this.properties = properties;

        // Put all of the fields from the properties instance into class fields
        this.homeDepotUsername = properties.getProperty("username");
        this.homeDepotPassword = properties.getProperty("password");
        this.twilioAccountSID = properties.getProperty("accountSID");
        this.twilioAuthToken = properties.getProperty("authToken");
        this.twilioPhoneNumber = properties.getProperty("twilioPhoneNumber");
        this.recipientPhoneNumbers = properties.getProperty("phoneNumbers").split(",");
    }

    /* Retrieves the user's Home Depot username */
    public String getHomeDepotUsername() {
        return homeDepotUsername;
    }

    /* Sets the user's Home Depot username for the field and properties object, then saves the properties file */
    public void setHomeDepotUsername(String username) {
        // Set the field
        homeDepotUsername = username;

        // Set property object field and then save the file
        properties.setProperty("username", username);
        savePropertiesFile();
    }

    /* Retrieves the decrypted users Home Depot password */
    public String getHomeDepotPassword() {
        return decryptPassword(homeDepotPassword);
    }

    /* Sets the user's Home Depot password for the field and properties object, then saves the properties file */
    public void setHomeDepotPassword(String password) {
        // Set the field
        homeDepotPassword = encryptPassword(password);

        // Set property object field and then save the file
        properties.setProperty("password", encryptPassword(password));
        savePropertiesFile();
    }

    /* Retrieves the users Twilio account SID */
    public String getTwilioAccountSID() {
        return twilioAccountSID;
    }

    /* Sets the user's Twilio account SID for the field and properties object, then saves the properties file */
    public void setTwilioAccountSID(String accountSID) {
        // Set the field
        twilioAccountSID = accountSID;

        // Set property object field and then save the file
        properties.setProperty("accountSID", accountSID);
        savePropertiesFile();
    }

    /* Retrieves the users Twilio auth token */
    public String getTwilioAuthToken() {
        return twilioAuthToken;
    }

    /* Sets the user's Twilio auth token for the field and properties object, then saves the properties file */
    public void setTwilioAuthToken(String authToken) {
        // Set the field
        twilioAuthToken = authToken;

        // Set property object field and then save the file
        properties.setProperty("authToken", authToken);
        savePropertiesFile();
    }

    /* Retrieves the users Twilio phone number */
    public String getTwilioPhoneNumber() {
        return twilioPhoneNumber;
    }

    /* Sets the user's Twilio phone number for the field and properties object, then saves the properties file */
    public void setTwilioPhoneNumber(String phoneNumber) {
        // Set the field
        twilioPhoneNumber = phoneNumber;

        // Set the property object field and then save the file
        properties.setProperty("twilioPhoneNumber", phoneNumber);
        savePropertiesFile();
    }

    /* Retrieves the users recipient phone numbers */
    public String[] getRecipientPhoneNumbers() {
        return recipientPhoneNumbers;
    }

    /* Sets the user's recipient phone numbers for the field and properties object, then saves the properties file */
    public void setRecipientPhoneNumbers(String[] phoneNumbers) {
        // Set the field
        recipientPhoneNumbers = phoneNumbers;

        // If the array that was sent is empty, set the property as a blank String "" and return
        if (phoneNumbers.length == 0) {
            properties.setProperty("phoneNumbers", "");
            savePropertiesFile();
            return;
        }

        // Must be turned into a String before saving to the properties file
        String phoneNumbersString = phoneNumbers[0];
        for (int i = 1; i < phoneNumbers.length; i++) {
            phoneNumbersString += ("," + phoneNumbers[i]);
        }

        // Set the property object field and then save the file
        properties.setProperty("phoneNumbers", phoneNumbersString);
        savePropertiesFile();
    }

    /* Encrypts any text that is passed to it using a hardcoded salt and the Jasypt library */
    // In the future, I hope to replace the hardcoded salt with something a bit more secure
    public String encryptPassword(String password) {
        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword("kfg5jld7hthrj98ioe4tofhi43jdp");
        return encryptor.encrypt(password);
    }

    /* Decrypts any text that is passed to it using a hardcoded salt and the Jasypt library */
    public String decryptPassword(String encryptedPassword) {
        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword("kfg5jld7hthrj98ioe4tofhi43jdp");
        return encryptor.decrypt(encryptedPassword);
    }

    /* Saves the properties file after changes are made to it */
    private void savePropertiesFile() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("res/config.properties");
            properties.store(fileOutputStream, "");
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * Validates all of the data found within the properties file
     * 1. Check for empty fields
     * 2. Validate Twilio phone number format
     * 3. Attempt to login to Home Depot
     * 4. Attempt to connect to Twilio API
     * 5. Check if any recipient phone numbers are listed yet
     * 6. Check if any recipient phone number that are listed are actually valid in format
     *
     * Since JavaFX applications are designed to run on a single thread, we must create and execute a new thread in
     * in order to keep the user interface from stalling.
     *
     * The boolean "attemptedLogin" should be set to true if the user just attempted to login to the application. If
     * attemptedLogin is set to true, the user will receive an error message for empty fields. If the user just loaded
     * the application, they obviously would not need that error message so attemptedLogin should be set to false
     */
    public void validateProperties(Stage stage, boolean attemptedLogin) {
        // Provide the user with a loading screen while the function is working
        LoadingInterface loadingInterface = new LoadingInterface("Validating...");
        stage.setScene(new Scene(loadingInterface));

        Thread thread = new Thread(() -> {
            // Create a new instance of each interface in case the user needs to be sent to one or another
            // CountdownInterface is instantiated later on so the thread inside of it does not begin execution
            LoginInterface loginInterface = new LoginInterface(stage, this);
            ConfigureInterface configureInterface = new ConfigureInterface(stage, this);

            // First, make sure all fields in the properties file are not blank
            // (adding all of the fields to the array just makes checking each of them a little more readable)
            String[] fields = new String[] {
                homeDepotUsername, homeDepotPassword, twilioAccountSID, twilioAuthToken, twilioPhoneNumber,
            };

            for (String field : fields) {
                if (field.equals("")) {
                    Platform.runLater(() -> {
                        // If any of the fields are empty, send the user back to the LoginInterface
                        stage.setScene(new Scene(loginInterface));

                        // This if statement is only executed if the user just attempted to login to the application
                        // This is necessary since the program also uses this function upon loading and sending an error
                        // message the first time they load up the application would be a little odd
                        if (attemptedLogin) {
                            loginInterface.setErrorLabel("You must fill out all fields in order to continue");
                        }
                    });
                    return;
                }
            }

            // Validate that the phone number entered is in the correct format
            if (!twilioPhoneNumber.matches("\\d{10}")) {
                Platform.runLater(() -> {
                    stage.setScene(new Scene(loginInterface));
                    loginInterface.setErrorLabel("Phone numbers should be 10 digits in length in the form\n###-###-####");
                });
                return;
            }

            // Validate the Home Depot username and password by checking if they are capable of logging into
            // Home Depot's website
            ScheduleScraper scheduleScraper = new ScheduleScraper(homeDepotUsername, decryptPassword(homeDepotPassword));
            if (!scheduleScraper.isLoginSuccessful()) {
                Platform.runLater(() -> {
                    // If logging in to Home Depot's site is not successful, send the user back to the LoginInterface
                    // with an error message letting them know
                    stage.setScene(new Scene(loginInterface));
                    loginInterface.setErrorLabel("Your Home Depot credentials are not valid\nPlease try again");
                });
                return;
            }

            // Validate the user's Twilio credentials by attempting to connect to the API. In this instance, it is not
            // necessary to pass the provided phone number in since this check will not account for that. Unfortunately,
            // there does not seem to be any way to know if the user entered a valid Twilio phone number until the
            // texts are sent out.
            TwilioService twilioService = new TwilioService("Not Needed Here", twilioAccountSID, twilioAuthToken);
            if (!twilioService.areCredentialsValid()) {
                Platform.runLater(() -> {
                    // If the Twilio credentials are not valid, send the user back to LoginInterface
                    stage.setScene(new Scene(loginInterface));
                    loginInterface.setErrorLabel("Your Twilio credentials are not valid\nPlease try again");
                });
                return;
            }

            // Check to see if any recipient phone numbers have been set up
            if (recipientPhoneNumbers[0].equals("")) {
                Platform.runLater(() -> {
                    // If there are no phone numbers listed, take the user to the configuration page so that they
                    // may add some to the list
                    stage.setScene(new Scene(configureInterface));
                });
                return;
            }

            // Validate any phone numbers that are found in the list by making sure that they are 10 digits in length
            for (String phoneNumber : recipientPhoneNumbers) {
                if (!phoneNumber.matches("\\d{10}")) {
                    Platform.runLater(() -> {
                        stage.setScene(new Scene(configureInterface));

                        configureInterface.setErrorLabel(
                                "Phone numbers should be 10 digits in length in the form\n###-###-####"
                        );
                    });
                    return;
                }
            }

            // If the file passes all validation, send the user to the countdown page
            Platform.runLater(() -> {
                CountdownInterface countdownInterface = new CountdownInterface(stage, this);
                stage.setScene(new Scene(countdownInterface));
            });

        });
        thread.setDaemon(true);
        thread.start();
    }

}
