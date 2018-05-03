package pkg3dlidar;

/**
 * @author Nicklas Boserup
 */
public class Matrix {

    private final double[][] matrix;
    
    public Matrix(int rows, int columns) {
        matrix = new double[rows][columns];
    }
    
    public Matrix(double[]... rows) {
        this(rows.length, rows[0].length); // Each row includes row[0].length columns
        for (int row = 0; row < rows.length; row++) {
            matrix[row] = rows[row];
        }
    }

    public int getNumberOfRows() {
        return matrix.length;
    }
    
    public int getNumberOfColumns() {
        return matrix[0].length;
    }
    
    public double getValue(int row, int column) {
        return matrix[row][column];
    }
    
    public double[] getRow(int row) {
        return matrix[row];
    }
    
    public double[] getColumn(int column) {
        double[] columnArray = new double[getNumberOfRows()];
        for (int row = 0; row < getNumberOfRows(); row++) {
            columnArray[row] = matrix[row][column];
        }
        return columnArray;
    }
    
    public Matrix transform() {
        Matrix result = new Matrix(getNumberOfColumns(), getNumberOfRows());
        
        for (int row = 0; row < getNumberOfRows(); row++) {
            for (int column = 0; column < getNumberOfColumns(); column++) {
                result.matrix[column][row] = matrix[row][column];
            }
        }
        
        return result;
    }
    
    public Matrix add(Matrix toAdd) {
        if (getNumberOfRows() != toAdd.getNumberOfRows() || getNumberOfColumns() != toAdd.getNumberOfColumns())
            throw new IllegalArgumentException("The two matrices must have the same number of dimensions");
        Matrix result = new Matrix(getNumberOfRows(), getNumberOfColumns());
        
        for (int row = 0; row < getNumberOfRows(); row++) {
            for (int column = 0; column < getNumberOfColumns(); column++) {
                result.matrix[row][column] = matrix[row][column] + toAdd.matrix[row][column];
            }
        }
        return result;
    }
    
    public Matrix multiplyScalar(double scalar) {
        Matrix result = new Matrix(getNumberOfRows(), getNumberOfColumns());
        
        for (int row = 0; row < getNumberOfRows(); row++) {
            for (int column = 0; column < getNumberOfColumns(); column++) {
                result.matrix[row][column] = matrix[row][column] * scalar;
            }
        }
        return result;
    }
    
    public double dotProduct(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length || vector1.length == 0)
            throw new IllegalArgumentException("The two vectors must have the same (non-zero) number of members");
        double dot = 0;
        for (int i = 0; i < vector1.length; i++)
            dot += vector1[i] * vector2[i];
        
        return dot;
    }
    
    public Matrix multiplyMatrix(Matrix toMultiply) {
        if (getNumberOfColumns()!= toMultiply.getNumberOfRows())
            throw new IllegalArgumentException("The matrix must have the same number of rows as toMultiply's number of columns");
        Matrix result = new Matrix(getNumberOfRows(), toMultiply.getNumberOfColumns());
        
        for (int row = 0; row < getNumberOfRows(); row++) {
            for (int column = 0; column < toMultiply.getNumberOfColumns(); column++) {
                result.matrix[row][column] = dotProduct(getRow(row), toMultiply.getColumn(column));
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        String s = "";
        
        for (int row = 0; row < getNumberOfRows(); row++) {
            s += row == 0 ? "⸢" : row == matrix.length-1 ? "⸤" : "|";
            for (int column = 0; column < getNumberOfColumns(); column++) {
                s += "\t" + matrix[row][column] + "\t";
            }
            s += row == 0 ? "⸣\n" : row == matrix.length-1 ? "⸥" : "|\n";
        }
        return s;
    }
    
}
