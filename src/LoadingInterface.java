import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/*
 * An interface that lets the user know that their content/next interface is loading
 */
public class LoadingInterface extends Interface {

    public LoadingInterface(String message) {
        // Attempt to grab the loading icon "gear.gif" using FileInputStream
        Image imgLoading = null;
        try {
            FileInputStream fileInputStream = new FileInputStream("res\\gear.gif");
            imgLoading = new Image(fileInputStream);
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        // Place the GIF inside of an ImageView so that it can be placed inside the interface
        ImageView imvLoading = new ImageView(imgLoading);

        // A label to let the user know that their content is loading
        Label lblLoading = new Label(message);

        // // Add all of the elements to the root interface
        getChildren().addAll(
                imvLoading,
                lblLoading
        );
    }
}
