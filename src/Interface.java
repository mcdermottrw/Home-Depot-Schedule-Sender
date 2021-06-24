import javafx.scene.layout.VBox;

/*
 * A superclass for all other interfaces within the application
 * Sets certain parameters that all interfaces should contain so that code does not need to be repeated there
 */
public class Interface extends VBox {

    public Interface() {
        // Assign a stylesheet to this VBox and it's children
        getStylesheets().add("file:res/style.css");

        // Assign a style class to this VBox and set it's size
        getStyleClass().add("interface");
        setPrefSize(350, 450);
    }

}
