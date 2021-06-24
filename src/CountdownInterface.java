import com.twilio.exception.ApiException;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CountdownInterface extends Interface {

    Stage stage;
    User user;

    boolean endCountdown;

    /* This constructor is used to build the interface */
    public CountdownInterface(Stage stage, User user) {
        // Assign the passed parameters to the appropriate fields
        this.stage = stage;
        this.user = user;

        // Label to indicate which user is logged in
        Label lblUsername = new Label(user.getHomeDepotUsername());
        lblUsername.getStyleClass().add("username-label");

        // Label to indicate how long it will be until the schedule is sent out
        Label lblCounter = new Label();
        lblCounter.getStyleClass().add("counter-label");

        // Thread to control the countdown timer and execute sendSchedules() when it hits 0
        Thread countdown = new Thread(() -> {
            // Start the timer at 30 seconds
            int seconds = 30;

            // Set the initial time in the counter label
            lblCounter.setText(String.valueOf(seconds));

            // While loop to continue counting down until endCountdown is set to false
            endCountdown = false;
            while (!endCountdown) {
                // Pauses the thread for one second before updating the counter
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                // Decrement the seconds by one and sets the counter to the new value
                seconds--;
                int finalSeconds = seconds;
                Platform.runLater(() -> {
                    lblCounter.setText(String.valueOf(finalSeconds));
                });

                // Check if the timer has hit 0 yet
                // If so, end the loop controlling the countdown and execute sendSchedules()
                if (seconds == 0) {
                    endCountdown = true;
                    sendSchedules();
                }
            }
        });
        countdown.setDaemon(true); // Ends the thread if the program ends
        countdown.start();

        // Button to skip the timer
        Button btnSendSchedules = new Button("Send Schedules");
        btnSendSchedules.getStyleClass().add("send-button");
        btnSendSchedules.setPrefSize(125, 30);
        btnSendSchedules.setOnAction(e -> {
            // End the countdown thread before executing the method
            endCountdown = true;
            sendSchedules();
        });

        // Button to cancel the timer and go the the configure page
        Button btnConfiguration = new Button("Configure");
        btnConfiguration.getStyleClass().add("configure-button");
        btnConfiguration.setPrefSize(125, 30);
        btnConfiguration.setOnAction(e -> {
            // If the user decides to go back to the ConfigurationInterface, set endCountdown to true so the countdown
            // stops and set the scene to ConfigurationInterface
            endCountdown = true;
            ConfigureInterface configureInterface = new ConfigureInterface(stage, user);
            stage.setScene(new Scene(configureInterface));
        });

        // Add all elements to the root container
        getChildren().addAll(
            lblUsername,
            new Label("Your schedule will be sent out in"),
            lblCounter,
            new Label("To " + user.getRecipientPhoneNumbers().length + " recipients"),
            btnSendSchedules,
            btnConfiguration
        );
    }

    /* Sends schedules to the phone numbers that the user added in the ConfigureInterface page */
    private void sendSchedules() {
        // Switch to LoadingInterface first for a smoother experience
        LoadingInterface loadingInterface = new LoadingInterface("Sending...");
        stage.setScene(new Scene(loadingInterface));

        // The process of sending the schedule must be contained in a separate thread so the LoadingInterface does
        // not stall
        Thread thread = new Thread(() -> {
            // Create a TwilioService instance to send SMS with
            // The users Twilio phone number, account SID, and auth token are all required to create this object
            String twilioNumber = user.getTwilioPhoneNumber();
            String accountSID = user.getTwilioAccountSID();
            String authToken = user.getTwilioAuthToken();
            TwilioService twilioService = new TwilioService(twilioNumber, accountSID, authToken);

            // Create a ScheduleScraper object to retrieve and format the user's schedule
            // The users Home Depot username and password are required to create this object
            String username = user.getHomeDepotUsername();
            String password = user.getHomeDepotPassword();
            ScheduleScraper scheduleScraper = new ScheduleScraper(username, password);

            // Loop through each phone number, sending a schedule to each one :)
            //
            // If the Twilio phone number is found to be invalid, the user will be sent back to the LoginInterface with an
            // error message
            try {
                for (String phoneNumber : user.getRecipientPhoneNumbers()) {
                    twilioService.sendSMS(phoneNumber, scheduleScraper.getScheduleAsString());
                }
            }
            catch(ApiException ex) {
                LoginInterface loginInterface = new LoginInterface(stage, user);
                loginInterface.setErrorLabel(
                        "Your Twilio phone number is not valid.\n" +
                                "Make sure you are using the phone number you purchased from Twilio"
                );
                stage.setScene(new Scene(loginInterface));
            }

            // Exit the program now that the schedules have been sent
            System.exit(0);
        });
        thread.setDaemon(true);
        thread.start();

    }
}
