import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class ScheduleScraper {

    private ArrayList<String> scheduleArray;
    private boolean loginSuccessful = true;

    public ScheduleScraper(String username, String password) {
        this.scheduleArray = new ArrayList<>();

        // Define URL for login page
        String loginPageUrl = "https://hdapps.homedepot.com/LaborMgtTools/WFMEssLauncher";

        // Fetch the login form that we will be logging into
        Connection.Response loginForm = null;
        try {
            loginForm = Jsoup.connect(loginPageUrl)
                    .referrer("https://homedepot.com/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:50.0)")
                    .timeout(10 * 1000)
                    .followRedirects(true)
                    .method(Connection.Method.GET)
                    .execute();
        }
        catch (IOException ex) {
            System.err.println("An IOException occurred when attempting to fetch the login form in ScheduleScraper()");
        }

        // Get cookies from the login form
        Map<String, String> loginCookies = loginForm.cookies();

        // Create a map of parameters that are needed to login
        Map<String, String> parameters = new HashMap<>();
        parameters.put("language",  "en_US");
        parameters.put("successUrl", "https://hdapps.homedepot.com/LaborMgtTools/WFMEssLauncher?language=en_US");
        parameters.put("callingProgram", "lmslLaborMgtTools");
        parameters.put("uiLoc", "ext");
        parameters.put("appendSessionID", "false");
        parameters.put("myLocation", "Store");
        parameters.put("storeUser", "true");
        parameters.put("j_userbelongsto", "Store");
        parameters.put("j_storenumber", "1942");
        parameters.put("j_username", username);
        parameters.put("j_password", password);
        parameters.put("action", "Go");

        // Define the action URL (the URL that the parameters are sent to)
        String loginRequestUrl = "https://hdapps.homedepot.com/MYTHDPassport/rs/clientUI/thdLoginRequest";

        // Login and retrieve the page post-login
        Connection.Response schedulePage = null;
        try {
            schedulePage = Jsoup.connect(loginRequestUrl)
                    .referrer(loginPageUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:50.0)")
                    .timeout(10 * 20000)
                    .data(parameters)
                    .cookies(loginCookies)
                    .followRedirects(true)
                    .method(Connection.Method.POST)
                    .execute();
        }
        catch (IOException ex) {
            System.err.println("An IOException occurred when attempting to fetch the schedule page in ScheduleScraper()");
        }

        // Parse the page that is retrieved and filter out what I don't need via Document.select()
        Document doc = null;
        Elements links = null;
        try {
            doc = schedulePage.parse();
            links = doc.select("ul li span");
        }
        catch (IOException ex) {
            System.err.println("An IOException occurred when attempting parse the schedule document in ScheduleScraper()");
        }

        // Indicate that an error occurred if the title of the page is not "My Schedule"
        String title = doc.select("title").toString();
        if (!title.contains("My Schedule")) {
            loginSuccessful = false;
        }

        // This Queue will cycle through days of the week in the appropriate order
        Queue<String> weekdays = new LinkedList<>();
        weekdays.add("Monday");
        weekdays.add("Tuesday");
        weekdays.add("Wednesday");
        weekdays.add("Thursday");
        weekdays.add("Friday");
        weekdays.add("Saturday");
        weekdays.add("Sunday");

        for (int i = 0; i < links.size(); i++) {
            // Get indexed element, convert it to a String and remove the HTML tags from it.
            String element = links.get(i).toString();
            element = Jsoup.parse(element).text(); // Jsoup.parse(x).text() removes HTML tags

            // Get element above indexed element, convert it to a String and remove the HTML tags from it.
            String nextElement = "";
            try {
                nextElement = links.get(i + 1).toString();
                nextElement = Jsoup.parse(nextElement).text();
            } catch (IndexOutOfBoundsException ignored) {
                // IndexOutOfBoundsException occurs at the end of the list, when i+1 is not possible
            }

            // Check if the initial element is a date or not by checking whether the String
            // contains a "/" character. If it is a date, pull the appropriate weekday from the Queue
            // to be ready for use. Add the weekday back to the queue so that they can continue to keep cycling
            // Only do this if weekday contains a weekday though
            String weekday = "";
            if (element.contains("/")) {
                weekday = weekdays.poll();
                weekdays.add(weekday);
            }

            if (nextElement.contains("-")) {
                scheduleArray.add(weekday + " - " + element + "\n" + nextElement + "\n\n" );
            }
        }
    }

    /* Returns a formatted version of the scraped work schedule */
    public String getScheduleAsString() {
        String schedule = "Ryan's Work Schedule\n\n";
        
        for (String s : scheduleArray) {
            schedule += s;
        }
        
        return schedule;
    }

    /*  */
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

}
