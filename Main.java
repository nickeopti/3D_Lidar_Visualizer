package pkg3dlidar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @author Nicklas Boserup
 */
public class Main extends Application {

    private Slider v_x, v_z, focal, scaling, x, y, z;
    private ResizableCanvas canvas;
    Point[] points;
    double radius = 1;

    @Override
    public void start(Stage primaryStage) throws IOException {
        points = getPoints();
        
        BorderPane root = new BorderPane();
        
        HBox settings = new HBox(10);
        settings.setPadding(new Insets(10));
        v_x = new Slider(-Math.PI/2d, Math.PI/2d, 0);
        v_z = new Slider(-Math.PI*4, Math.PI*4, 0);
        focal = new Slider(5, 250, 200);
        scaling = new Slider(0.1, 20, 1);

        x = new Slider(-2000, 2000, 0);
        y = new Slider(-2000, 2000, 0);
        z = new Slider(-2000, 2000, 0);
        
        settings.getChildren().addAll(v_x, v_z, focal, scaling, x, y, z);
        HBox.setHgrow(v_x, Priority.ALWAYS);
        HBox.setHgrow(v_z, Priority.ALWAYS);
        HBox.setHgrow(focal, Priority.ALWAYS);
        HBox.setHgrow(scaling, Priority.ALWAYS);
        HBox.setHgrow(x, Priority.ALWAYS);
        HBox.setHgrow(y, Priority.ALWAYS);
        HBox.setHgrow(z, Priority.ALWAYS);
        
        canvas = new ResizableCanvas() {
            Point[] projectedPoints;

            @Override
            public void setProjectedPoints(Point... points) {
                projectedPoints = points;
            }

            @Override
            public void draw() {
                if (projectedPoints == null)
                    return;

                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, getWidth(), getHeight());

                for (int i = projectedPoints.length-1; i > 0; --i) {
                    Point p = projectedPoints[i];
                    if (p.z < 0)
                        continue;
                    gc.setFill(Color.hsb(p.z/2000d*360, 1, 1));
                    double scale = scaling.getValue();
                    gc.fillOval(
                            p.x*scale - radius/2 + getWidth()/2,
                            p.y*scale - radius/2 + getHeight()*0.75,
                            radius, radius);
                }
            }
        };
        draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
        canvas.setOnScroll(me -> {
            if (!me.isShiftDown() && !me.isControlDown()) {
                v_x.setValue(v_x.getValue() + me.getDeltaY() / 720d);
                v_z.setValue(v_z.getValue() - me.getDeltaX() / 720d);
            } else if(me.isShiftDown() && !me.isControlDown()) {
                focal.setValue(focal.getValue() + me.getDeltaX() / 100d);
                y.setValue(y.getValue() - me.getDeltaY() / 10d);
            } else if (!me.isShiftDown() && me.isControlDown()) {
                x.setValue(x.getValue() + me.getDeltaY()*Math.sin(v_z.getValue()) / 10d - me.getDeltaX()*Math.cos(v_z.getValue()) / 10d);
                z.setValue(z.getValue() - me.getDeltaY()*Math.cos(v_z.getValue()) / 10d - me.getDeltaX()*Math.sin(v_z.getValue()) / 10d);

            }
            draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
        });

        Pane p = new Pane(canvas);
        canvas.widthProperty().bind(p.widthProperty());
        canvas.heightProperty().bind(p.heightProperty());
        root.setCenter(p);
        root.setBottom(settings);
        root.setStyle("-fx-base: rgb(50,50,50); -fx-focus-color: transparent;");
        
        Scene scene = new Scene(root, 900, 500);



        Transition move1 = cameraTransition(-0.38888888888888895, 1.2222222222222225, 202.00000000000003, 454.8901054188032, -156.0, -190.85747921887472,
                -0.1819074379060071, 1.283185307179604, 250.0, -67.99154384377512, -144.0, 10.087008724062809);
        Transition move2 = cameraTransition(-0.38888888888888895, 1.2222222222222225, 202.00000000000003, -706.7777125403236, -156.0, 231.30822369654925,
                -0.38888888888888895, 1.2222222222222225, 202.00000000000003, 454.8901054188032, -156.0, -190.85747921887472);
        Transition move3 = cameraTransition(-0.8333333333333336, 1.2222222222222225, 202.00000000000003, -590.2349864667545, -512.0, 188.95503020017992,
                -0.38888888888888895, 1.2222222222222225, 202.00000000000003, -706.7777125403236, -156.0, 231.30822369654925);
        Transition move4 = cameraTransition(-1.5707963267948966, -0.38888888888888895, 250.0, 67.79158071525913, -1252.0, 65.97306792418387,
                -0.8333333333333336, 1.2222222222222225, 202.00000000000003, -590.2349864667545, -512.0, 188.95503020017992);
        Transition move5 = cameraTransition(-0.1819074379060071, 7.56637061435919, 250.0, -67.99154384377512, -144.0, 10.087008724062809,
                -1.5707963267948966, -0.38888888888888895, 250.0, 67.79158071525913, -1252.0, 65.97306792418387, 20);

        Transition seq = new SequentialTransition(move1, move2, move3, move4, move5);
        seq.setCycleCount(-1);

        scene.setOnKeyPressed(ke -> {
            switch (ke.getCode()) {
                case P:
                    System.out.println(v_x.getValue() + ", " + v_z.getValue() + ", " + focal.getValue() + ", " + x.getValue() + ", " + y.getValue() + ", " + z.getValue());
                    break;
                case R: case SPACE: case PAGE_DOWN: case PAGE_UP:
                    if (ke.isControlDown() || ke.isShiftDown() || ke.getCode() == KeyCode.PAGE_UP) {
                        seq.playFromStart();
                        break;
                    }
                    if (seq.getStatus() == Animation.Status.RUNNING)
                        seq.pause();
                    else
                        seq.play();
                    break;
                case PLUS: case ADD: case F5:
                    radius++;
                    draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
                    break;
                case MINUS: case SUBTRACT: case PERIOD:
                    if (radius > 1)
                        radius--;
                    draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
                    break;
            }
        });

        primaryStage.setTitle("3D Lidarscanning - Eksamensprojekt");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public void draw(ResizableCanvas canvas, double v_x, double v_z, double focal, double x, double y, double z) {
        Task<Point[]> calculatePoints = new Task<Point[]>() {
            @Override
            protected Point[] call() throws Exception {
                Point[] projectedPoints = Camera.getProjectedPoints(focal, Camera.getRotatedPoints(v_x, 0, v_z, Camera.getTranslatedPoints(x, y, z, points)));
                Arrays.sort(projectedPoints, Comparator.comparingDouble(p -> p.z));
                return projectedPoints;
            }
        };
        calculatePoints.setOnSucceeded(e -> {
            canvas.setProjectedPoints(calculatePoints.getValue());
            canvas.draw();
        });
        new Thread(calculatePoints).start();
    }

    public Point[] getPoints() throws IOException {
        File toRead = new File( System.getProperty("user.home") + "/Documents/3D_Torvet_28_04_2.log");
        List<String> data = Files.readAllLines(toRead.toPath());
        
        ArrayList<Point> points = new ArrayList<>();
        
        for (int n = 0; n < data.size(); n++) {
            int distance = Integer.parseInt(data.get(n));
            if (distance == 1)
                continue;
            distance += 5;
            
            double v_x = -(n % 400) * 2*Math.PI / 400d;
            double v_z = (n/400) * Math.PI / 180d;
            
            double[] r_x0 = {1, 0, 0};
            double[] r_x1 = {0, Math.cos(v_x), -Math.sin(v_x)};
            double[] r_x2 = {0, Math.sin(v_x), Math.cos(v_x)};
            Matrix r_x = new Matrix(r_x0, r_x1, r_x2);

            double[] r_y0 = {Math.cos(v_x), 0, Math.sin(v_x)};
            double[] r_y1 = {0, 1, 0};
            double[] r_y2 = {-Math.sin(v_x), 0, Math.cos(v_x)};
            Matrix r_y = new Matrix(r_y0, r_y1, r_y2);
            
            double[] r_z0 = {Math.cos(v_z), -Math.sin(v_z), 0};
            double[] r_z1 = {Math.sin(v_z), Math.cos(v_z), 0};
            double[] r_z2 = {0, 0, 1};
            Matrix r_z = new Matrix(r_z0, r_z1, r_z2);

            Matrix r = r_y.multiplyMatrix(r_z);
            
            double[] vector = {distance, 0, 0};
            Matrix rotatedVector = r.multiplyMatrix(new Matrix(vector).transform());
            points.add(new Point(rotatedVector.getValue(0, 0), rotatedVector.getValue(1, 0), rotatedVector.getValue(2, 0)));
        }
        
        return points.toArray(new Point[0]);
    }

    public Transition cameraTransition(double new_v_x, double new_v_z, double new_focal, double new_x, double new_y, double new_z,
                                       double old_v_x, double old_v_z, double old_focal, double old_x, double old_y, double old_z) {
        return cameraTransition(new_v_x, new_v_z, new_focal, new_x, new_y, new_z, old_v_x, old_v_z, old_focal, old_x, old_y, old_z, 5);
    }

    public Transition cameraTransition(double new_v_x, double new_v_z, double new_focal, double new_x, double new_y, double new_z,
                                       double old_v_x, double old_v_z, double old_focal, double old_x, double old_y, double old_z,
                                       double duration) {
        Transition move = new Transition() {
            {
                setCycleDuration(Duration.seconds(duration));
                setCycleCount(1);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                v_x.setValue((new_v_x - old_v_x) * frac + old_v_x);
                v_z.setValue((new_v_z - old_v_z) * frac + old_v_z);
                focal.setValue((new_focal - old_focal) * frac + old_focal);
                x.setValue((new_x - old_x) * frac + old_x);
                y.setValue((new_y - old_y) * frac + old_y);
                z.setValue((new_z - old_z) * frac + old_z);
                draw(canvas, v_x.getValue(), v_z.getValue(), focal.getValue(), x.getValue(), y.getValue(), z.getValue());
            }
        };

        return move;
    }

    public abstract class ResizableCanvas extends Canvas {

        public abstract void setProjectedPoints(Point... points);

        public abstract void draw();
        
        public ResizableCanvas() {
            widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> draw());
            heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> draw());
        }
        
        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) {
            return getWidth();
        }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }

    }

}
