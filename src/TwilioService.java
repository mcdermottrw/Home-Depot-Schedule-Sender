import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.rest.verify.v2.Service;

public class TwilioService {

    String twilioPhoneNumber;
    String accountSID;
    String authToken;

    /* Constructor receives the required Twilio phone number, Account SID, and Auth Token then initializes Twilio */
    public TwilioService(String twilioPhoneNumber, String accountSID, String authToken) {
        this.accountSID = accountSID;
        this.authToken = authToken;
        this.twilioPhoneNumber = twilioPhoneNumber;

        Twilio.init(accountSID, authToken);
    }

    /* Sends a specific, passed message to whichever phone number was passed */
    public void sendSMS(String phoneNumber, String message) {
        Message m = Message.creator(
                new PhoneNumber(phoneNumber), // To
                new PhoneNumber(twilioPhoneNumber), // From
                message) // Text to send
        .create();
    }

    /* */
    public boolean areCredentialsValid() {
        try {
            Service service = Service.creator("Validate Credentials").create();
        }
        catch(ApiException ex) {
            return false;
        }
        return true;
    }

}
