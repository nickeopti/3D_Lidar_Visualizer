package pkg3dlidar;

public class Camera {

    public static Point[] getTranslatedPoints(double x, double y, double z, Point... points) {
        Point[] translatedPoints = new Point[points.length];
        for (int i = 0; i < translatedPoints.length; i++) {
            translatedPoints[i] = new Point(points[i].x + x, points[i].y + y, points[i].z + z);
        }
        return translatedPoints;
    }

    public static Point[] getRotatedPoints(double v_x, double v_y, double v_z, Point... points) {
        double[] r_x0 = {1, 0, 0};
        double[] r_x1 = {0, Math.cos(v_x), -Math.sin(v_x)};
        double[] r_x2 = {0, Math.sin(v_x), Math.cos(v_x)};
        Matrix r_x = new Matrix(r_x0, r_x1, r_x2);

        double[] r_y0 = {Math.cos(v_z), 0, Math.sin(v_z)};
        double[] r_y1 = {0, 1, 0};
        double[] r_y2 = {-Math.sin(v_z), 0, Math.cos(v_z)};
        Matrix r_y = new Matrix(r_y0, r_y1, r_y2);

        double[] r_z0 = {Math.cos(v_z), -Math.sin(v_z), 0};
        double[] r_z1 = {Math.sin(v_z), Math.cos(v_z), 0};
        double[] r_z2 = {0, 0, 1};
        Matrix r_z = new Matrix(r_z0, r_z1, r_z2);

        Matrix r = r_x.multiplyMatrix(r_y);

        Point[] rotatedPoints = new Point[points.length];
        for (int i = 0; i < rotatedPoints.length; i++) {
            double[] vector = {points[i].x, points[i].y, points[i].z};
            Matrix rotatedVector = r.multiplyMatrix(new Matrix(vector).transform());
            rotatedPoints[i] = new Point(rotatedVector.getValue(0, 0), rotatedVector.getValue(1, 0), rotatedVector.getValue(2, 0));
        }

        return rotatedPoints;
    }

    public static Point[] getProjectedPoints(double focalLength, Point... points) {
        Point[] projectedPoints = new Point[points.length];
        for (int i = 0; i < projectedPoints.length; i++) {
            projectedPoints[i] = new Point(
                    -focalLength / (points[i].z) * points[i].x,
                    -focalLength / (points[i].z) * points[i].y,
                    points[i].z);
        }

        return projectedPoints;
    }

}
