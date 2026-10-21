package com.example.school_life.net.model;

/** 课程（weekType：0=每周 1=单周 2=双周） */
public class Course {
    public Long id;
    public String studentNo;
    public String name;
    public String teacher;
    public String room;
    public Integer dayOfWeek;
    public Integer startSection;
    public Integer endSection;
    public Integer startWeek;
    public Integer endWeek;
    public Integer weekType;
    public Boolean remind;
    public Integer remindMinutes;
}
