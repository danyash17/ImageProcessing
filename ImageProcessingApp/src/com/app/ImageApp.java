package com.app;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.layout.BorderPane;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * This application provides a GUI for image processing.\n" +
 *
 * @author William Edison
 * @version 5.00 January 2017
 * <p>
 * An image file can be loaded for processing by using the Main Menu Open option.
 * <p>
 * Processed image files can be saved in various formats (bmp, jpg, png, gif) using
 * the Main Menu Save Image option.
 * <p>
 * Image files can be transformed by selecting one of the buttons on the right.
 * -  Buttons labeled IP execute transformations from Image Processor class.
 * -  Buttons labeled FX execute transformations provided by Java FX.
 * <p>
 * The image scene can be:
 * -  Rotated using Y axis (vertical) right click drag mouse movement
 * -  Zoomed in or out using the keypad + or - keys or the mouse roller button
 * -  Moved around in the display window using the up, down, left, or right arrow keys
 * -  Reset to the default settings using the 'r' or 'R' key\n");
 */

public class ImageApp extends Application {

    private Stage stage;
    private Scene scene;
    private SubScene subScene;
    private Group subSceneRoot;
    private BorderPane border;

    private StackPane pane;
    private Image image;
    private ImageView imageView;
    private ImageProcessor ip;
    private int sceneWidth = 1100;
    private int sceneHeight = 700;
    final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final double SCROLL_FACTOR = .01;
    private static final double SCROLL_DELTA = 10.0;

    private VBox vBox;
    private int fileNameIndex;

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private double originX = 0.0;
    private double originY = 0.0;
    private double rotate = 0.0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Creates and initiates the Scene
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        buildScene();
        stage.setTitle("Image Application");
        stage.setScene(scene);
        stage.show();
    }


    /**
     * Builds the Scene containing the image display, menu bar, and vertical option bar
     */
    public void buildScene() {
        // Use a border pane as the root for scene
        border = new BorderPane();
        border.setTop(addMenuBar());
        vBox = addVBox();
        ScrollPane pane = new ScrollPane(vBox);
        pane.setFitToWidth(true);
        border.setRight(pane);
        scene = new Scene(border, sceneWidth, sceneHeight);
        buildSubScene(scene.getWidth(), scene.getHeight());

        // Set up zoom
        zoomProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable arg0) {
                imageView.setFitWidth(zoomProperty.get() * 4);
                imageView.setFitHeight(zoomProperty.get() * 3);
            }
        });

        pane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if (event.getDeltaY() > 0) {
                    zoomProperty.set(zoomProperty.get() * 1.1);
                } else if (event.getDeltaY() < 0) {
                    zoomProperty.set(zoomProperty.get() / 1.1);
                }
            }
        });

        handleKeyboard(scene);
        handleMouse(scene);
    }

    /**
     * Builds the SubScene containing the image display
     */
    public void buildSubScene(double ssWidth, double ssHeight) {
        pane = new StackPane();
        subScene = new SubScene(pane, ssWidth, ssHeight);
        subScene.setFill(BACKGROUND_COLOR);
        border.setCenter(pane);
    }
/*
    	subSceneRoot = new Group();
    	subScene = new SubScene(subSceneRoot, ssWidth, ssHeight);
        subScene.setFill(BACKGROUND_COLOR);

        pane = new Pane();
        pane.getChildren().addAll(subScene);

        subScene.widthProperty().bind(
                pane.widthProperty());
        subScene.heightProperty().bind(
                pane.heightProperty());
        border.setCenter(pane);
	}
*/

    /**
     * Sets up keyboard handlers for various key triggered events.
     *
     * @param scene Draw3D Scene Group
     */
    private void handleKeyboard(Scene scene) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
//            	System.out.println("Key Event Code= " + event.getCode() + ", viewNumber=" + viewNumber);

                switch (event.getCode()) {

                    case R:
                        reset();
                        break;
                    case D:
                        originX = imageView.getX();
                        imageView.setX(originX + 10);
                        break;
                    case A:
                        originX = imageView.getX();
                        imageView.setX(originX - 10);
                        break;
                    case W:
                        originY = imageView.getY();
                        imageView.setY(originY - 10);
                        break;
                    case S:
                        originY = imageView.getY();
                        imageView.setY(originY + 10);
                        break;
                    case ADD:
                        imageView.setScaleX(imageView.getScaleX() + SCROLL_DELTA * SCROLL_FACTOR);
                        imageView.setScaleY(imageView.getScaleY() + SCROLL_DELTA * SCROLL_FACTOR);
                        break;
                    case SUBTRACT:
                        imageView.setScaleX(imageView.getScaleX() - SCROLL_DELTA * SCROLL_FACTOR);
                        imageView.setScaleY(imageView.getScaleY() - SCROLL_DELTA * SCROLL_FACTOR);
                        break;
                    default:
                        System.out.println("Undefined key");
                        break;
                }
            }
        });
    }

    /**
     * Sets up mouse handlers for various mouse triggered events.
     *
     * @param scene Draw3D Scene Group
     */
    private void handleMouse(Scene scene) {
        scene.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent se) {
                imageView.setScaleX(imageView.getScaleX() + se.getDeltaY() * SCROLL_FACTOR);
                imageView.setScaleY(imageView.getScaleY() + se.getDeltaY() * SCROLL_FACTOR);
            }
        });

        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });

        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = -(mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);
                rotate = rotate + mouseDeltaY;

                if (me.isPrimaryButtonDown()) {
                    imageView.setRotate(rotate);
                }
            }
        });

    }

    /**
     * Resets rotation, zoom, and location values to defaults.
     */
    private void reset() {
        originX = 0.0;
        originY = 0.0;
        rotate = 0.0;
        imageView.setRotate(rotate);
        imageView.setX(originX);
        imageView.setY(originY);
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
    }

    /**
     * Creates a VBox with a column of buttons for selecting image transformations
     * and for showing the current image file name.
     *
     * @return
     */
    private VBox addVBox() {

        Button btnOrig = new Button("Original");
        Button btnInvert = new Button("Invert");
        TextField addConst = new TextField();
        addConst.setPromptText("+const");
        Button btnAddConst = new Button("Add const");
        TextField subsConst = new TextField();
        subsConst.setPromptText("-const");
        Button btnSubsConst = new Button("Substract const");
        Button btnLog = new Button("Natural logarithm");
        TextField powConst = new TextField();
        powConst.setPromptText("^const");
        Button btnPow = new Button("Power");
        Button btnHist = new Button("Histogram");
        Button btnHistEq = new Button("Equalized Histogram");
        Button btnOrigHist = new Button("Original Histogram");
        Button btnGray = new Button("Gray");
        Button btnLincontrast = new Button("Linear contrast");
        Button btnBitSlice = new Button("Bit slice");
        Button btnOtsuGt = new Button("Otsu GT");
        Button btnHistGt = new Button("Hist GT");
        Button btnGradGt = new Button("Gradient GT");
        TextField bitSlice = new TextField();
        bitSlice.setPromptText("bitplane №");

        btnOrig.setMaxWidth(Double.MAX_VALUE);
        btnInvert.setMaxWidth(Double.MAX_VALUE);
        btnAddConst.setMaxWidth(Double.MAX_VALUE);
        btnSubsConst.setMaxWidth(Double.MAX_VALUE);
        btnPow.setMaxWidth(Double.MAX_VALUE);
        btnLog.setMaxWidth(Double.MAX_VALUE);
        btnHist.setMaxWidth(Double.MAX_VALUE);
        btnHistEq.setMaxWidth(Double.MAX_VALUE);
        btnGray.setMaxWidth(Double.MAX_VALUE);
        btnOrigHist.setMaxWidth(Double.MAX_VALUE);
        btnLincontrast.setMaxWidth(Double.MAX_VALUE);
        btnBitSlice.setMaxWidth(Double.MAX_VALUE);
        btnOtsuGt.setMaxWidth(Double.MAX_VALUE);
        btnHistGt.setMaxWidth(Double.MAX_VALUE);
        btnGradGt.setMaxWidth(Double.MAX_VALUE);

        VBox vbButtons = new VBox();
        vbButtons.setSpacing(10);
        vbButtons.setPadding(new Insets(10, 20, 10, 20));
        vbButtons.getChildren().addAll(btnOrig, btnInvert, addConst,
                btnAddConst, subsConst, btnSubsConst,
                btnLog, powConst, btnPow,
                btnOrigHist, btnHist, btnHistEq,
                btnGray, btnLincontrast, bitSlice,
                btnBitSlice, btnOtsuGt, btnHistGt, btnGradGt);

        btnOrig.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                imageView.setImage(image);
            }
        });

        btnInvert.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                imageView.setImage(ip.invert());
            }
        });

        btnAddConst.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                if (addConst.getText() != null) {
                    imageView.setImage(ip.addConst((Integer.parseInt(addConst.getText()))));
                }
            }
        });

        btnSubsConst.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                if (subsConst.getText() != null) {
                    imageView.setImage(ip.substractConst((Integer.parseInt(subsConst.getText()))));
                }
            }
        });

        btnLog.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                imageView.setImage(ip.log());
            }
        });

        btnPow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                if (powConst.getText() != null) {
                    imageView.setImage(ip.pow((Integer.parseInt(powConst.getText()))));
                }
            }
        });

        btnOrigHist.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Stage newStage = new Stage();
                LineChart aHist = ip.hist();
                Scene newScene = new Scene(aHist, sceneWidth, sceneHeight);
                newStage.setScene(newScene);
                newStage.show();
            }
        });

        btnHist.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Stage newStage = new Stage();
                LineChart aHist = ip.hist(ip.wImage);
                Scene newScene = new Scene(aHist, sceneWidth, sceneHeight);
                newStage.setScene(newScene);
                newStage.show();
            }
        });

        btnHistEq.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                WritableImage writableImage = ip.equalize();
                imageView.setEffect(null);
                imageView.setImage(writableImage);
                Stage newStage = new Stage();
                LineChart aHist = ip.hist(writableImage);
                Scene newScene = new Scene(aHist, sceneWidth, sceneHeight);
                newStage.setScene(newScene);
                newStage.show();
            }
        });

        btnGray.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                imageView.setImage(ip.gray());
            }
        });

        btnLincontrast.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                imageView.setImage(ip.linearContrast());
            }
        });

        btnBitSlice.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                int plane = Integer.parseInt(bitSlice.getText());
                imageView.setImage(ip.bitSlice(plane));
            }
        });

        btnOtsuGt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                imageView.setImage(ip.otsuGlobalThreshold());
            }
        });

        btnHistGt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                imageView.setImage(ip.histThreshold());
            }
        });

        btnGradGt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imageView.setEffect(null);
                imageView.setImage(ip.gradThreshold());
            }
        });

        Label fileNameLabel = new Label(" ");
        fileNameLabel.setFont(Font.font("Regular", 14));
        vbButtons.getChildren().add(fileNameLabel);
        fileNameIndex = vbButtons.getChildren().indexOf(fileNameLabel);

        return vbButtons;
    }

    /**
     * Creates MenuBar items and defines associated actions.
     *
     * @return MenuBar menuBar
     */
    private MenuBar addMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: lightblue");
        Menu mainMenu = new Menu("Main");

        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String fileURL = null;
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Data File");
                String currentDirectory = findDefaultDirectory();
                fileChooser.setInitialDirectory(new File(currentDirectory));
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.bmp", "*.png", "*.jpg", "*.gif"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    try {
                        fileURL = file.toURI().toURL().toString();
                        image = new Image(fileURL, false);
                    } catch (MalformedURLException ex) {
                        //error
                    }
                    Label fileNameLabel = new Label(file.getName());
                    fileNameLabel.setFont(Font.font("Regular", 14));
                    vBox.getChildren().remove(fileNameIndex);
                    vBox.getChildren().add(fileNameLabel);

                    double ih = image.getHeight();
                    double iw = image.getWidth();
                    imageView = new ImageView();
                    imageView.setImage(image);

                    double ssWidth = scene.getWidth() - 300;
                    double ssHeight = ssWidth * ih / iw;

                    subSceneRoot = new Group();
                    subScene = new SubScene(subSceneRoot, ssWidth, ssHeight);
                    pane = new StackPane();
                    pane.getChildren().add(subScene);

                    imageView.setPreserveRatio(true);
                    imageView.fitWidthProperty().bind(pane.widthProperty());
                    imageView.fitHeightProperty().bind(pane.heightProperty());

                    subSceneRoot.getChildren().add(imageView);

                    border.setCenter(pane);

                    originX = 0.0;
                    originY = 0.0;
                    imageView.setX(originX);
                    imageView.setY(originY);
                    imageView.setScaleX(1.0);
                    imageView.setScaleY(1.0);
                    rotate = 0.0;
                    imageView.setRotate(rotate);
                    ip = new ImageProcessor(image);
                }
            }
        });

        Menu saveSceneMenu = new Menu("Save Image");
        MenuItem bmpItem = new MenuItem("bmp");
        MenuItem jpgItem = new MenuItem("jpg");
        MenuItem pngItem = new MenuItem("png");
        MenuItem gifItem = new MenuItem("gif");
        saveSceneMenu.getItems().addAll(bmpItem, jpgItem, pngItem, gifItem);

        bmpItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                saveImage("bmp");
            }
        });

        jpgItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                saveImage("jpg");
            }
        });

        pngItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                saveImage("png");
            }
        });

        gifItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                saveImage("gif");
            }
        });

        mainMenu.getItems().addAll(openItem, saveSceneMenu);

        Menu helpMenu = new Menu("Help");
        MenuItem helpItem = new MenuItem("Show Help Text");
        helpItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                pane.getChildren().add(addHelpBox());
            }
        });
        helpMenu.getItems().addAll(helpItem);

        menuBar.getMenus().addAll(mainMenu, helpMenu);

        return menuBar;
    }

    /**
     * Finds the default file directory for the selection dialogue.
     *
     * @return String currentDirectory
     */
    private String findDefaultDirectory() {
        String currentDirectory = null;
        String userDir = System.getProperty("user.dir");
        currentDirectory = userDir + "\\src\\Resources";
        File dir = new File(currentDirectory);
        if (!dir.exists()) {
            currentDirectory = userDir + "\\Resources";
            dir = new File(currentDirectory);
            if (!dir.exists()) {
                currentDirectory = userDir;
            }
        }
        return currentDirectory;
    }

    /**
     * Writes the current subScene to an image file in the specified format.
     *
     * @param format with values bmp, jpg, png, gif
     */
    private void saveImage(String format) {
//    	System.out.println("Save Scene " + format);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image " + format);
        String currentDirectory = findDefaultDirectory();
        fileChooser.setInitialDirectory(new File(currentDirectory));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image File", "*." + format));
        File file = fileChooser.showSaveDialog(stage);
//      System.out.println("Save SubScene File= " + file);
        if (file != null) {
            WritableImage wImage = subScene.snapshot(new SnapshotParameters(), null);

            if (format == "png" || format == "gif") {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(wImage, null), format, file);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            } else if (format == "bmp" || format == "jpg") {
                BufferedImage image = SwingFXUtils.fromFXImage(wImage, null);  // Get buffered image.
                BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE); // Remove alpha-channel from buffered image.
                Graphics2D graphics = imageRGB.createGraphics();
                graphics.drawImage(image, 0, 0, null);
                try {
                    ImageIO.write(imageRGB, format, file);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
                graphics.dispose();
            }
        }
    }

    /**
     * Shows help text.
     */
    private VBox addHelpBox() {
        if (imageView != null) {
            imageView.setEffect(null);
            imageView.setImage(null);
            reset();
        }
        Label fileNameLabel = new Label(" ");
        fileNameLabel.setFont(Font.font("Regular", 14));
        vBox.getChildren().remove(fileNameIndex);
        vBox.getChildren().add(fileNameLabel);

        Label helpLabel = new Label(
                " *  This application provides a GUI for image processing.\n" +
                        " *\n" +
                        " *  An image file can be loaded for processing by using the Main Menu Open option.\n" +
                        " *\n" +
                        " *  Processed image files can be saved in various formats (bmp, jpg, png, gif) using\n" +
                        " *  the Main Menu Save Image option.\n" +
                        " *\n" +
                        " *  Image files can be transformed by selecting one of the buttons on the right.\n" +
                        " *    -  Buttons labeled IP execute transformations from the ImageProcessor class.\n" +
                        " *    -  Buttons labeled FX execute transformations provided by Java FX.\n" +
                        " *\n" +
                        " *  The image scene can be:\n" +
                        " *    -  Rotated using Y axis (vertical) right click drag mouse movement\n" +
                        " *    -  Zoomed in or out using the keypad + or - keys or the mouse roller button\n" +
                        " *    -  Moved around in the display window using the up, down, left, or right arrow keys\n" +
                        " *    -  Reset to the default settings using the 'r' or 'R' key\n");

        helpLabel.setFont(Font.font("Regular", 16));
        VBox helpBox = new VBox(6, helpLabel);
        return helpBox;
    }
}