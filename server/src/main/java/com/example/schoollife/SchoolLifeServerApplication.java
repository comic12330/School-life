package com.example.schoollife;

import com.example.schoollife.entity.CampusCard;
import com.example.schoollife.entity.CardTransaction;
import com.example.schoollife.entity.Course;
import com.example.schoollife.entity.Grade;
import com.example.schoollife.entity.User;
import com.example.schoollife.repository.CampusCardRepository;
import com.example.schoollife.repository.CardTransactionRepository;
import com.example.schoollife.repository.CourseRepository;
import com.example.schoollife.repository.GradeRepository;
import com.example.schoollife.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.Arrays;

@SpringBootApplication
public class SchoolLifeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolLifeServerApplication.class, args);
    }

    /** 首次启动灌入演示种子数据（各表 count 为 0 才插，重启不重复） */
    @Bean
    public CommandLineRunner seed(UserRepository userRepo, CampusCardRepository cardRepo,
                                  CardTransactionRepository txRepo, GradeRepository gradeRepo,
                                  CourseRepository courseRepo) {
        return args -> {
            long now = System.currentTimeMillis();
            long day = 24L * 3600 * 1000;

            if (userRepo.count() == 0) {
                User a = newUser("2023051234", "张同学", "2023级", "计算机科学与技术学院", "软件工程", "软工2301班");
                User b = newUser("2023051235", "李同学", "2023级", "计算机科学与技术学院", "软件工程", "软工2301班");
                userRepo.saveAll(Arrays.asList(a, b));
            }

            if (cardRepo.count() == 0) {
                CampusCard ca = newCard("2023051234", "10001234", "张同学", "68.50", false, now);
                CampusCard cb = newCard("2023051235", "10001235", "李同学", "12.00", false, now);
                cardRepo.saveAll(Arrays.asList(ca, cb));
            }

            if (txRepo.count() == 0) {
                txRepo.saveAll(Arrays.asList(
                        tx("2023051234", "-12.00", "消费", "第一食堂 午餐", now - 1 * day),
                        tx("2023051234", "-8.50", "消费", "教育超市 文具", now - 2 * day),
                        tx("2023051234", "50.00", "充值", "网上银行", now - 3 * day),
                        tx("2023051234", "-15.00", "消费", "第二食堂 晚餐", now - 4 * day),
                        tx("2023051234", "-6.00", "消费", "图书馆 打印", now - 5 * day)));
            }

            if (gradeRepo.count() == 0) {
                gradeRepo.saveAll(Arrays.asList(
                        grade("2023051234", "2025-2026-1", "数据结构", "4.0", "92"),
                        grade("2023051234", "2025-2026-1", "高等数学(下)", "5.0", "88"),
                        grade("2023051234", "2025-2026-1", "Java程序设计", "4.0", "85"),
                        grade("2023051234", "2025-2026-1", "大学英语(三)", "3.0", "76"),
                        grade("2023051234", "2025-2026-1", "计算机组成原理", "4.0", "71"),
                        grade("2023051234", "2025-2026-1", "大学体育(三)", "1.0", "83"),
                        grade("2023051235", "2025-2026-1", "高等数学(下)", "5.0", "60"),
                        grade("2023051235", "2025-2026-1", "大学英语(三)", "3.0", "55")));
            }

            if (courseRepo.count() == 0) {
                courseRepo.saveAll(Arrays.asList(
                        course("2023051234", "数据结构", "王老师", "教一201", 1, 1, 2, 1, 16, 0, false, 30),
                        course("2023051234", "高等数学(下)", "刘老师", "教二305", 1, 3, 4, 1, 16, 0, false, 30),
                        course("2023051234", "Java程序设计", "张老师", "实验楼402", 2, 5, 6, 1, 16, 0, true, 30),
                        course("2023051234", "大学英语(三)", "陈老师", "外语楼102", 3, 1, 2, 1, 16, 1, false, 30),
                        course("2023051234", "计算机组成原理", "赵老师", "教三210", 3, 3, 4, 1, 16, 2, false, 30),
                        course("2023051234", "形势与政策", "李老师", "教一101", 4, 11, 12, 1, 16, 0, false, 30),
                        course("2023051234", "大学体育(三)", "孙老师", "体育馆", 5, 7, 8, 1, 16, 0, false, 30),
                        course("2023051235", "高等数学(下)", "刘老师", "教二305", 1, 3, 4, 1, 16, 0, false, 30),
                        course("2023051235", "大学英语(三)", "陈老师", "外语楼102", 3, 1, 2, 1, 16, 1, false, 30)));
            }
        };
    }

    private User newUser(String no, String name, String grade, String college, String major, String clazz) {
        User u = new User();
        u.setStudentNo(no);
        u.setPassword("123456");
        u.setName(name);
        u.setGrade(grade);
        u.setCollege(college);
        u.setMajor(major);
        u.setClazz(clazz);
        return u;
    }

    private CampusCard newCard(String no, String cardNo, String name, String balance, boolean lost, long time) {
        CampusCard c = new CampusCard();
        c.setStudentNo(no);
        c.setCardNo(cardNo);
        c.setName(name);
        c.setBalance(new BigDecimal(balance));
        c.setLost(lost);
        c.setUpdatedAt(time);
        return c;
    }

    private CardTransaction tx(String no, String amount, String type, String place, long time) {
        CardTransaction t = new CardTransaction();
        t.setStudentNo(no);
        t.setAmount(new BigDecimal(amount));
        t.setType(type);
        t.setPlace(place);
        t.setTime(time);
        return t;
    }

    private Grade grade(String no, String semester, String course, String credit, String score) {
        Grade g = new Grade();
        g.setStudentNo(no);
        g.setSemester(semester);
        g.setCourseName(course);
        g.setCredit(new BigDecimal(credit));
        g.setScore(new BigDecimal(score));
        return g;
    }

    private Course course(String no, String name, String teacher, String room, int day,
                          int start, int end, int sw, int ew, int weekType, boolean remind, int minutes) {
        Course c = new Course();
        c.setStudentNo(no);
        c.setName(name);
        c.setTeacher(teacher);
        c.setRoom(room);
        c.setDayOfWeek(day);
        c.setStartSection(start);
        c.setEndSection(end);
        c.setStartWeek(sw);
        c.setEndWeek(ew);
        c.setWeekType(weekType);
        c.setRemind(remind);
        c.setRemindMinutes(minutes);
        return c;
    }
}
