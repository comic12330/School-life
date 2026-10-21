package com.example.school_life;

import static org.junit.Assert.assertEquals;

import com.example.school_life.util.GpaCalculator;

import org.junit.Test;

/** GPA 绩点换算与加权计算测试 */
public class GpaCalculatorTest {

    @Test
    public void gradePoint_boundaries() {
        assertEquals(4.0, GpaCalculator.gradePoint(90), 0.001);
        assertEquals(4.0, GpaCalculator.gradePoint(100), 0.001);
        assertEquals(3.7, GpaCalculator.gradePoint(85), 0.001);
        assertEquals(3.3, GpaCalculator.gradePoint(82), 0.001);
        assertEquals(3.0, GpaCalculator.gradePoint(78), 0.001);
        assertEquals(2.7, GpaCalculator.gradePoint(75), 0.001);
        assertEquals(2.3, GpaCalculator.gradePoint(72), 0.001);
        assertEquals(2.0, GpaCalculator.gradePoint(68), 0.001);
        assertEquals(1.5, GpaCalculator.gradePoint(64), 0.001);
        assertEquals(1.0, GpaCalculator.gradePoint(60), 0.001);
        assertEquals(0.0, GpaCalculator.gradePoint(59), 0.001);
    }

    @Test
    public void gpa_seedData() {
        // 种子数据：92/88/85/76/71/83，学分 4/5/4/3/4/1 → 68.7/21 = 3.27
        double[] credits = {4, 5, 4, 3, 4, 1};
        double[] scores = {92, 88, 85, 76, 71, 83};
        assertEquals(3.27, GpaCalculator.gpa(credits, scores), 0.001);
    }

    @Test
    public void gpa_empty() {
        assertEquals(0.0, GpaCalculator.gpa(new double[0], new double[0]), 0.001);
    }
}
