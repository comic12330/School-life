package com.example.school_life.util;

/** GPA 绩点换算与计算（纯逻辑，方便单元测试） */
public class GpaCalculator {

    /** 百分制成绩 -> 绩点 */
    public static double gradePoint(double score) {
        if (score >= 90) return 4.0;
        if (score >= 85) return 3.7;
        if (score >= 82) return 3.3;
        if (score >= 78) return 3.0;
        if (score >= 75) return 2.7;
        if (score >= 72) return 2.3;
        if (score >= 68) return 2.0;
        if (score >= 64) return 1.5;
        if (score >= 60) return 1.0;
        return 0.0;
    }

    /** GPA = Σ(学分×绩点) / Σ学分，保留 2 位小数 */
    public static double gpa(double[] credits, double[] scores) {
        double sum = 0;
        double creditSum = 0;
        for (int i = 0; i < credits.length; i++) {
            sum += credits[i] * gradePoint(scores[i]);
            creditSum += credits[i];
        }
        if (creditSum == 0) return 0;
        return Math.round(sum / creditSum * 100) / 100.0;
    }

    private GpaCalculator() {
    }
}
