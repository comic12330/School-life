package com.example.school_life;

import static org.junit.Assert.assertEquals;

import com.example.school_life.util.AlarmTimeCalculator;
import com.example.school_life.util.SectionTimes;

import org.junit.Test;

import java.util.Calendar;

/** 提醒触发时刻计算测试 */
public class AlarmTimeCalculatorTest {

    private Calendar at(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    /** 学期起始周：2026-10-19（周一） */
    private long termStart() {
        return at(2026, 10, 19, 0, 0).getTimeInMillis();
    }

    /** 第 5 节 14:00 -> 840 分钟 */
    private int section() {
        return SectionTimes.startMinuteOfDay(5);
    }

    @Test
    public void nextTrigger_sameDayBeforeClass() {
        long term = termStart();
        // 周二（10-20）10:00，第 5 节 14:00 的课提前 30 分钟 -> 当天 13:30
        long now = at(2026, 10, 20, 10, 0).getTimeInMillis();
        long expected = at(2026, 10, 20, 13, 30).getTimeInMillis();
        assertEquals(expected, AlarmTimeCalculator.nextTrigger(
                now, term, 2, section(), 30, 1, 16, 0));
    }

    @Test
    public void nextTrigger_afterClassGoesToNextWeek() {
        long term = termStart();
        // 周二 14:00（已上课）-> 下一周（10-27）13:30
        long now = at(2026, 10, 20, 14, 0).getTimeInMillis();
        long expected = at(2026, 10, 27, 13, 30).getTimeInMillis();
        assertEquals(expected, AlarmTimeCalculator.nextTrigger(
                now, term, 2, section(), 30, 1, 16, 0));
    }

    @Test
    public void nextTrigger_oddWeekCourseSkipsEvenWeek() {
        long term = termStart();
        // 单周课（weekType=1），当前是第 2 周（10-26 起为偶数周）
        // 周三 08:00 的课 -> 下一个单周 11-04 07:30（第 3 周，10-28 属第 2 周不排）
        long now = at(2026, 10, 26, 12, 0).getTimeInMillis();
        long expected = at(2026, 11, 4, 7, 30).getTimeInMillis();
        assertEquals(expected, AlarmTimeCalculator.nextTrigger(
                now, term, 3, SectionTimes.startMinuteOfDay(1), 30, 1, 16, 1));
    }

    @Test
    public void nextTrigger_outOfRangeReturnsMinusOne() {
        long term = termStart();
        // 课程已结束（endWeek=2），当前第 3 周 -> -1
        long now = at(2026, 11, 2, 8, 0).getTimeInMillis();
        assertEquals(-1, AlarmTimeCalculator.nextTrigger(
                now, term, 2, section(), 30, 1, 2, 0));
    }
}
