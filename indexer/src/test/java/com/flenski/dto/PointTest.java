package com.flenski.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PointTest {

    @Test
    public void testToJson() {
        Vector v = new Vector(new float[]{1.0f, 2.0f}, new int[]{0, 1});
        v.setName("sparse");
        Point p = new Point();
        p.setId(123);
        p.setVectors(v);
        String json = p.toJson();
        assertEquals("{\"vector\":\"{\\\"sparse\\\":{\\\"indices\\\":[0,1],\\\"values\\\":[1.0,2.0]}}\",\"id\":123}", json);
    }
}
