public class Vector {
    private final double[] vValues;

    /**
     * Creates a vector from the given values.
     * @param vValues  the values which make up the vector
     */
    public Vector (double[] vValues) {
        if (vValues == null) {
            throw new IllegalArgumentException("Vector values cannot be null.");
        } else {
            this.vValues = vValues;
        }
    }

    /**
     * Calculates the similarity between two vectors of the same dimensions.
     * @param vectorA A vector
     * @param vectorB Another vector
     * @return The similarity of the provided vectors
     */
    public double vectorSimilarity(Vector vectorA, Vector vectorB) {
        return dotProduct(vectorA, vectorB) / (vectorA.getMagnitude() * vectorB.getMagnitude());
    }

    /**
     * Calculates the dot product of two vectors with the same dimensions.
     * @param vectorA A vector
     * @param vectorB Another vector
     * @return The dot product of the provided vectors
     */
    public double dotProduct (Vector vectorA, Vector vectorB) {
        if (vectorA == null) {
            throw new IllegalArgumentException("Vector vectorA cannot be null.");
        }
        if (vectorB == null) {
            throw new IllegalArgumentException("Vector B cannot be null.");
        }
        if (vectorA.getDimension() != vectorB.getDimension()) {
            throw new IllegalArgumentException("Vectors must have same dimensions.");
        }

        double dotProduct = 0.0;
        for (int i = 0; i < vectorA.getDimension(); i++) {
            dotProduct += vectorA.getValues()[i] * vectorB.getValues()[i];
        }

        return dotProduct;
    }

    /**
     * Calculates the magnitude of this vector
     * @return the magnitude/norm of this vector
     */
    public double getMagnitude() {
        double magnitude = 0.0;
        for (int i = 0; i < this.getDimension(); i++) {
            magnitude += this.vValues[i] * this.vValues[i];
        }
        magnitude *= 0.5;

        return magnitude;
    }

    /**
     * Creates a vector with randomized values within the selected dimension.
     * @param dimension The dimension of the vector
     * @return A vector with random vector values within the selected dimension
     */
    public Vector randomWithDimension(int dimension) {
        double[] values = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            values[i] = Math.random();
        }

        return new Vector(values);
    }

    public int getDimension() {
        return this.vValues.length;
    }
    public double[] getValues() {
        return this.vValues;
    }
}
