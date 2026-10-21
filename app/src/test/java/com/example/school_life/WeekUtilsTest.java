package com.example.school_life;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.school_life.net.model.Course;
import com.example.school_life.util.WeekUtils;

import org.junit.Test;

import java.util.Calendar;

/** 周次与单双周判断测试 */
public class WeekUtilsTest {

    private Calendar at(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    /** 2026-10-19 是周一 */
    private long monday() {
        return at(2026, 10, 19, 0, 0).getTimeInMillis();
    }

    @Test
    public void mondayStart_returnsMondayMidnight() {
        // 周三 15:30 -> 所在周周一 00:00（10-19）
        long result = WeekUtils.mondayStart(at(2026, 10, 21, 15, 30).getTimeInMillis());
        assertEquals(monday(), result);
        // 周日同样归到本周一
        long sunday = WeekUtils.mondayStart(at(2026, 10, 25, 8, 0).getTimeInMillis());
        assertEquals(monday(), sunday);
    }

    @Test
    public void currentWeek_countsFromTermStart() {
        long term = monday();
        // 同一周内 -> 第 1 周
        assertEquals(1, WeekUtils.currentWeek(term, at(2026, 10, 21, 12, 0).getTimeInMillis()));
        // 2026-11-02 是第 3 周的周一
        assertEquals(3, WeekUtils.currentWeek(term, at(2026, 11, 2, 8, 0).getTimeInMillis()));
        // 早于学期开始按第 1 周
        assertEquals(1, WeekUtils.currentWeek(term, at(2026, 10, 1, 8, 0).getTimeInMillis()));
    }

    private Course course(int weekType, int startWeek, int endWeek) {
        Course c = new Course();
        c.startWeek = startWeek;
        c.endWeek = endWeek;
        c.weekType = weekType;
        return c;
    }

    @Test
    public void visible_weekTypeRules() {
        // 每周课：范围内都显示
        assertTrue(WeekUtils.visible(course(0, 1, 16), 3));
        assertTrue(WeekUtils.visible(course(0, 1, 16), 4));
        // 单周课：奇数周显示
        assertTrue(WeekUtils.visible(course(1, 1, 16), 3));
        assertFalse(WeekUtils.visible(course(1, 1, 16), 4));
        // 双周课：偶数周显示
        assertFalse(WeekUtils.visible(course(2, 1, 16), 3));
        assertTrue(WeekUtils.visible(course(2, 1, 16), 4));
        // 范围外不显示
        assertFalse(WeekUtils.visible(course(0, 1, 16), 17));
        assertFalse(WeekUtils.visible(course(0, 5, 16), 1));
    }
}
