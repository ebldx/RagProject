import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VectorTest {
    private final double[] vValueA = {3.0, 4.0, 0.0};
    private final double[] vValueB = {4.0, -3.0, 0.0};
    private final double[] vValueC = {0.0, 3.0, 4.0};
    private final double[] vValueD = {6.0, 0.0, 8.0};
    private final double[] vValueE = {0.0, 15.0, 20.0};
    private final double[] vValueF = {-3.5, 4.5, 0.5};
    private final double[] vValueG = {0.0, 0.0, 0.0};

    @Test
    void dotProductTest() {
        Vector vectorA = new Vector(vValueA);
        Vector vectorB = new Vector(vValueB);
        Vector vectorC = new Vector(vValueC);
        Vector vectorD = new Vector(vValueD);
        Vector vectorE = new Vector(vValueE);
        Vector vectorF = new Vector(vValueF);
        Vector vectorG = new Vector(vValueG);
        assertEquals(0, Vector.dotProduct(vectorA, vectorB));
        assertEquals(12, Vector.dotProduct(vectorA, vectorC));
        assertEquals(-9, Vector.dotProduct(vectorB, vectorC));
        assertEquals(160, Vector.dotProduct(vectorD, vectorE));
        assertEquals(77.5, Vector.dotProduct(vectorE, vectorF));
        assertEquals(0, Vector.dotProduct(vectorG, vectorG));
    }

    @Test
    void getMagnitudeTest() {
        Vector vectorA = new Vector(vValueA);
        Vector vectorD = new Vector(vValueD);
        Vector vectorE = new Vector(vValueE);
        Vector vectorF = new Vector(vValueF);
        Vector vectorG = new Vector(vValueG);
        assertEquals(5, vectorA.getMagnitude());
        assertEquals(10, vectorD.getMagnitude());
        assertEquals(25, vectorE.getMagnitude());
        assertEquals(0, vectorG.getMagnitude());
    }

    @Test
    void vectorSimilarityTest() {
        Vector vectorA = new Vector(vValueA);
        Vector vectorB = new Vector(vValueB);
        Vector vectorC = new Vector(vValueC);
        Vector vectorD = new Vector(vValueD);
        Vector vectorE = new Vector(vValueE);
        assertEquals(0, Vector.vectorSimilarity(vectorA, vectorB));
        assertEquals(0.48, Vector.vectorSimilarity(vectorA, vectorC));
        assertEquals(-0.36, Vector.vectorSimilarity(vectorB, vectorC));
        assertEquals(0.64, Vector.vectorSimilarity(vectorD, vectorE));
    }
}
