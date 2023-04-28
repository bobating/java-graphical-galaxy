package GalaxyGen;

import java.util.Arrays;
import java.util.Random;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Galaxy extends Application {

    private final int width = 800;
    private final int height = 850;
    private final Random random = new Random();
    private int numStars = 3000;
    private int numEndpoints = 5;
    private double[] xCoords = new double[numEndpoints];
    private double[] yCoords = new double[numEndpoints];
    private double[] xStarCoords;
    private double[] yStarCoords;
    private boolean[] isEndpointAnimating = new boolean[numEndpoints];

    private Timeline endpointAnimation;
    private boolean isEndpointAnimationPlaying = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        primaryStage.setTitle("Star Flex");
        TextField numStarsField = new TextField(Integer.toString(numStars));
        numStarsField.setPrefWidth(60);
        Label numStarsLabel = new Label("Number of stars:");
        numStarsLabel.getStyleClass().add("label");
        Button randomizeStarsButton = new Button("Randomize");
        randomizeStarsButton.getStyleClass().add("main-button");
        randomizeStarsButton.setOnAction(event -> {
            try {
                int num = Integer.parseInt(numStarsField.getText());
                randomizeStarPoints(num);
                drawRandomizedStarPoints(gc);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format");
            }
        });
        TextField numEndpointsField = new TextField(Integer.toString(numEndpoints));
        numEndpointsField.setPrefWidth(60);
        Label numEndpointsLabel = new Label("Number of endpoints:");
        numEndpointsLabel.getStyleClass().add("label");
        Button randomizeEndpointsButton = new Button("Randomize");
        randomizeEndpointsButton.getStyleClass().add("main-button");
        randomizeEndpointsButton.setOnAction(event -> {
            try {
                int num = Integer.parseInt(numEndpointsField.getText());
                randomizeLineEndpoints(num, gc);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format");
            }
        });
        randomizeStarPoints(numStars);
        drawRandomizedStarPoints(gc);
        randomizeLineEndpoints(numEndpoints, gc);
        drawRandomizedLineEndpoints(gc);

        // Generate star
        Circle star = generateStar();
        gc.setFill(Color.RED);
        gc.fillOval(
                star.getCenterX() - star.getRadius(),
                star.getCenterY() - star.getRadius(),
                star.getRadius() * 2,
                star.getRadius() * 2
        );

        // Create new canvas for star animation
        Canvas starCanvas = new Canvas(width, height);
        GraphicsContext starGC = starCanvas.getGraphicsContext2D();

        // Calculate star positions with some randomness
        double[] sizes = new double[500];
        double[] xStarCoords = new double[500];
        double[] yStarCoords = new double[500];
        Random random = new Random();
        for (int i = 0; i < 500; i++) {
            Paint fill = gc.getFill();
            double size = 1;
            if (fill instanceof RadialGradient) {
                size = ((RadialGradient) fill).getRadius(); // get current size
            }
            if (size < 1) size = 1;
            sizes[i] = random.nextInt(5) + 1; // random size between 1 and 8
            xStarCoords[i] = random.nextDouble() * gc.getCanvas().getWidth();
            yStarCoords[i] = random.nextDouble() * gc.getCanvas().getHeight();
        }

// Set up star animation
        Timeline starAnimation = new Timeline(
                new KeyFrame(
                        Duration.seconds(0.0003),
                        event -> {
                            starGC.clearRect(0, 0, starCanvas.getWidth(), starCanvas.getHeight()); // clear the canvas
                            for (int i = 0; i < 500; i++) {
                                starGC.setFill(Color.BLUE);
                                if (random.nextDouble() < 0.7) { // 70% chance of random color
                                    Color randomColor = Color.color(
                                            random.nextDouble(),
                                            random.nextDouble(),
                                            random.nextDouble()
                                    );
                                    starGC.setFill(randomColor);
                                } else {
                                    starGC.setFill(Color.WHITE);
                                }
                                starGC.fillOval(xStarCoords[i], yStarCoords[i], sizes[i], sizes[i]);
                                xStarCoords[i] += random.nextDouble() * 0.25 - 0.125; // add some randomness
                                yStarCoords[i] += random.nextDouble() * 0.25 - 0.125;
                            }
                        }
                )
        );
        starAnimation.setCycleCount((int) (10000 / 0.003)); // execute for 1000 frames (about 30 seconds)
        starAnimation.play();

        endpointAnimation = new Timeline(
                new KeyFrame(Duration.millis(16), event -> {
                    updateEndpointCoordinates(endpointAnimation.getCurrentTime().toMillis(), gc);
                })
        );
        endpointAnimation.setCycleCount(Timeline.INDEFINITE);

        // Create a group node to hold both the original canvas and the animated stars
        Group group = new Group();
        group.getChildren().addAll(canvas, starCanvas);

        // Create an HBox to hold the buttons
        HBox buttonBox = new HBox();
        buttonBox.getStyleClass().add("button-box");
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 0, 10, 0));
        buttonBox.setBackground(
                new Background(
                        new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)
                )
        ); // Set the background color to transparent
        buttonBox
                .getChildren()
                .addAll(
                        numStarsLabel,
                        numStarsField,
                        randomizeStarsButton,
                        numEndpointsLabel,
                        numEndpointsField,
                        randomizeEndpointsButton
                );

        // Set up the scene with the group node as the root
        BorderPane root = new BorderPane();
        root.setCenter(group);
        root.setBottom(buttonBox);
        root.setPadding(new Insets(70, 0, 0, 0));
        root.setMinHeight(height + buttonBox.getHeight());
        BorderPane.setAlignment(buttonBox, Pos.BOTTOM_CENTER); // Set the position of the button box to be at the bottom
        Scene scene = new Scene(root, width, height);
        scene
                .getStylesheets()
                .add("file:///C:/Users/chris/IdeaProjects/untitled/Resources/style.css");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Add the star animation to hover on top of the original canvas
        group.getChildren().add(star);



    }

    private Circle generateStar() {
        Circle star = new Circle(2, Color.WHITE);
        RadialGradient gradient = new RadialGradient(
                0,
                0,
                0.5,
                0.5,
                1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE),
                new Stop(1, Color.BLACK)
        );
        star.setFill(gradient);
        star.setEffect(new GaussianBlur(Math.random() * 3));
        star.setTranslateX(Math.random() * width);
        star.setTranslateY(Math.random() * height);

        return star;
    }

    private void drawRandomizedLineEndpoints(GraphicsContext gc) {
        gc.setStroke(Color.PINK);
        gc.beginPath();
        gc.moveTo(xCoords[0], yCoords[0]);
        for (int i = 1; i < numEndpoints; i++) {
            gc.lineTo(xCoords[i], yCoords[i]);
        }
        gc.lineTo(xCoords[0], yCoords[0]); // Draw line to first endpoint again to close the shape
        gc.stroke();
    }

    private void drawRandomizedStarPoints(GraphicsContext gc) {
        double size = 0.0;
                gc.setFill(
                new RadialGradient(
                        0.3,
                        0.6,
                        0.5,
                        0.5,
                        0.76,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(20, 20, 20)),
                        new Stop(0.6, Color.BLACK)
                )
        );
        gc.fillRect(0, 0, width, height);
        Random random = new Random();
        for (int i = 0; i < numStars; i++) {
            double x = xStarCoords[i];
            double y = yStarCoords[i];
            if (random.nextDouble() < 0.003) { // 1% of big star
               size = random.nextDouble() * 30;
            } else {
                size = random.nextDouble() * 3 + 0; // generate random size between 1 and 5
            }
            if (random.nextDouble() < 0.4) { // 40% chance of random color
                Color randomColor = Color.color(
                        random.nextDouble(),
                        random.nextDouble(),
                        random.nextDouble()
                );
                gc.setFill(randomColor);
            } else {
                gc.setFill(Color.WHITE);
            }
            gc.fillOval(x, y, size, size);
        }
    }

    private void randomizeStarPoints(int num) {
        numStars = num;
        xStarCoords = new double[numStars];
        yStarCoords = new double[numStars];
        Random rand = new Random();
        for (int i = 0; i < numStars; i++) {
            xStarCoords[i] = rand.nextDouble() * width;
            yStarCoords[i] = rand.nextDouble() * height;
        }
    }

    private void randomizeLineEndpoints(int num, GraphicsContext gc) {
        // Clear the canvas
        gc.clearRect(0, 0, width, height);

        // Generate new endpoints
        numEndpoints = num + 1; // Add 1 to include the starting point
        xCoords = new double[numEndpoints];
        yCoords = new double[numEndpoints];
        Random rand = new Random();
        for (int i = 0; i < numEndpoints; i++) {
            if (i < numEndpoints - 1) {
                xCoords[i] = rand.nextDouble() * width;
                yCoords[i] = rand.nextDouble() * height;
            } else {
                // Make sure the last endpoint has the same coordinates as the first endpoint
                xCoords[i] = xCoords[0];
                yCoords[i] = yCoords[0];
            }
        }

        // Initialize the animating array
        isEndpointAnimating = new boolean[numEndpoints];
        Arrays.fill(isEndpointAnimating, false);

        // Draw the new set of stars and lines
        drawRandomizedStarPoints(gc);
        drawLines(gc);

        // Stop the endpoint animation if it is currently playing
        if (isEndpointAnimationPlaying) {
            endpointAnimation.stop();
            isEndpointAnimationPlaying = false;
        }
    }

    private double getTotalLineLength() {
        double length = 0;
        for (int i = 0; i < numEndpoints - 1; i++) {
            double x1 = xCoords[i];
            double y1 = yCoords[i];
            double x2 = xCoords[i + 1];
            double y2 = yCoords[i + 1];
            length += Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }
        return length;
    }

    private void updateEndpointCoordinates(double elapsedTime, GraphicsContext gc) {
        double totalLength = getTotalLineLength();
        double currentLength = 0;
        for (int i = 0; i < numEndpoints - 1; i++) {
            double x1 = xCoords[i];
            double y1 = yCoords[i];
            double x2 = xCoords[i + 1];
            double y2 = yCoords[i + 1];
            double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            if (!isEndpointAnimating[i] && currentLength + length >= elapsedTime) {
                // Start animating this endpoint
                double t = (elapsedTime - currentLength) / length;
                xCoords[i] += t * (x2 - x1);
                yCoords[i] += t * (y2 - y1);
                isEndpointAnimating[i] = true;
            }
            currentLength += length;
        }
        drawLines(gc);
    }
    private void drawLines(GraphicsContext gc) {
        if (random.nextDouble() < 0.5) { // 50% chance of random color
            Color randomColor = Color.color(
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble()
            );
            gc.setStroke(randomColor);
        } else {
            gc.setStroke(Color.PINK);
        }

        double initialWidth = 0.0; // initial line width
        double finalWidth = 10.0; // final line width
        double duration = 1.5; // duration of animation in seconds

        DoubleProperty currentWidth = new SimpleDoubleProperty(initialWidth); // current line width
        int frameCount = (int) (duration * 120); // Number of frames in the animation

        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);

        for (int i = 0; i <= frameCount; i++) {
            double t = (double) i / frameCount;
            double interpolatedWidth = initialWidth + t * (finalWidth - initialWidth);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(duration * t), event -> {
                currentWidth.set(interpolatedWidth); // update currentWidth property
                drawLinesPath(gc, currentWidth.get());
            });

            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }
    private void drawLinesPath(GraphicsContext gc, double lineWidth) {
        gc.setLineWidth(lineWidth);
        gc.beginPath();
        gc.moveTo(xCoords[0], yCoords[0]);
        for (int i = 1; i < numEndpoints; i++) {
            gc.lineTo(xCoords[i], yCoords[i]);
        }
        gc.lineTo(xCoords[0], yCoords[0]); // Draw line to first endpoint again to close the shape
        gc.stroke();
    }

}
