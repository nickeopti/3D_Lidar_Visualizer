package pkg3dlidar;

import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LiveImport {

    private static int n = -1;

    public static void liveImport(int spr, ObservableList<Main.Point> pointList) {
        Thread t = new Thread(() -> {
            Runtime runtime = Runtime.getRuntime();
            try {
                //Process process = runtime.exec("pio serialports monitor -b 115200 --raw | tee 3D-scanning.log");

                String[] cmd = {
                        "/bin/sh",
                        "-c",
                        //"ls /etc | grep release"
                        "java -jar /home/nicke/NetBeansProjects/Handy/dist/Handy.jar | tee ~/Documents/3D-test.log"
                };
                Process process = runtime.exec(cmd);
                //Process process = runtime.exec("java -jar /home/nicke/NetBeansProjects/Handy/dist/Handy.jar | tee ~/Documents/3D-test.log");

                ArrayList<Main.Point> temp = new ArrayList<>();

                BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String input;
                while ((input = stdIn.readLine()) != null) {
                    n++;
                    int distance;
                    try {
                        distance = Integer.parseInt(input);
                    } catch (NumberFormatException nfe) {
                        continue;
                    }

                    if (distance <= 6)
                        continue;

                    temp.add(rotatePoint(spr, n, distance));

                    if (temp.size() == spr / 8) {
                        final Main.Point[] ps = temp.toArray(new Main.Point[temp.size()]);
                        temp.clear();
                        Platform.runLater(() -> pointList.addAll(ps));
                    }
                    //Platform.runLater(() -> pointList.add(rotatePoint(spr, n, distance)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    public static Main.Point rotatePoint(int spr, int n, int distance) {
        double v_x = -(n % spr) * 2*Math.PI / spr;
        double v_z = (n/spr) * Math.PI / 180d;

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

        return new Main.Point(
                rotatedVector.getValue(0, 0),
                rotatedVector.getValue(1, 0),
                rotatedVector.getValue(2, 0));
    }

}
