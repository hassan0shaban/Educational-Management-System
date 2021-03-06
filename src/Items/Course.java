package Items;

import static DataBase.MyConnection.con;
import Persons.Student;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Integer.parseInt;
import static java.lang.System.err;
import static java.lang.System.out;
import java.sql.PreparedStatement;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Course {

    private String name, doctorName;
    private int code, doctorId, studentId, TAid;
    private Connection con;
    private BufferedReader in;

    public Course(int code) {
        in = new BufferedReader(new InputStreamReader(System.in));
        con = con();
        this.code = code;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setTAid(int TAid) {
        this.TAid = TAid;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public void viewCourse() {
        Map<String, Boolean> courseStudents = new HashMap();
        Map<String, Boolean> courseTAs = new HashMap();

        var query = "select S.name from student_course C JOIN student S ON S.id = C.sid where C.ccode = ?;";
        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setInt(1, code);
            var rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("S.name");
                courseStudents.put(name, true);
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }

        query = "select  D.name from course C JOIN doctor D ON C.did = D.id where code = ?;";
        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setInt(1, code);
            var rs = ps.executeQuery();
            while (rs.next()) {
                doctorName = rs.getString("name");
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }

        query = "select T.name from TA T JOIN TA_course C ON C.tid = T.id where ccode = ?;";
        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setInt(1, code);
            var rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("T.name");
                courseTAs.put(name, true);
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }

        query = "select name from course where code = ?;";
        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setInt(1, code);
            var rs = ps.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }

        out.println("-------------------------------------------------------------------Course name : " + name + " ---------------");
        out.println("-------------------------------------------------------------------Course code : " + code + " ---------------");
        out.println("-------------------------------------------------------------------Course doctor : " + doctorName + " ---------------");
        if (!courseTAs.isEmpty()) {
            out.print("-------------------------------------------------------------------Course TAs : ");
            for (var ct : courseTAs.keySet()) {
                out.print(ct + "  , ");
            }
            out.println("");
        }

        if (!courseStudents.isEmpty()) {
            out.print("-------------------------------------------------------------------Course students : ");
            for (var cs : courseStudents.keySet()) {
                out.print(cs + ", ");
            }
            out.println("");
        }
    }

    public void markReport() throws IOException {
        Map<Student, Boolean> students = new HashMap();

        var query = "SELECT  S.name, S.id, C.midmark, C.finalmark, C.totalmark , C.bonus, C.yearmark FROM student_course C JOIN student S ON S.id = C.sid where C.ccode = ?;";
        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setInt(1, code);
            var rs = ps.executeQuery();
            while (rs.next()) {
                var s = new Student(rs.getInt("id"));
                var midMark = rs.getInt("midmark");
                var finalMark = rs.getInt("finalmark");
                var yearMark = rs.getInt("yearmark");
                var bonus = rs.getInt("bonus");
                var totalMark = (midMark == -1 ? 0 : midMark) + (finalMark == -1 ? 0 : finalMark) + (yearMark == -1 ? 0 : yearMark) + bonus;

                s.setName(rs.getString("name"));
                s.setMidGrade(midMark);
                s.setFinalGrade(finalMark);
                s.setYearDoingGrade(yearMark);
                s.setBonusGrade(bonus);
                s.setTotalGrade(totalMark);

                students.put(s, true);
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
        if (!students.isEmpty()) {
            for (Student s : students.keySet()) {
                out.println("Student name: " + (s.getName()) + " ------ "
                        + "Student id: " + s.getId() + " ------ "
                        + "Mid exame mark: " + (s.getMidGrade() == -1 ? "unknown" : s.getMidGrade()) + " ------ "
                        + "coursework mark: " + ((s.getYearDoingGrade()) == -1 ? "unknown" : s.getYearDoingGrade()) + " ------ "
                        + "bonus marks: " + (s.getBonusGrade() == -1 ? "unknown" : s.getBonusGrade()) + " ------ "
                        + "Final exam mark: " + (s.getFinalGrade() == -1 ? "unknown" : s.getFinalGrade()) + " ------ "
                        + "Total mark: " + (s.getTotalGrade() == -1 ? "unknown" : s.getTotalGrade()));
            }
            markActions();
        } else {
            err.println("-------------------------------------------------------------------NO students was registerd in this course------------------------------");
        }

    }

    private void doctorAssignments() {
        Map<String, Boolean> assignmentName = new HashMap();
        Map<String, Boolean> assignmentCode = new HashMap();

        var query = "select  A.acode, C.acode from assignments A JOIN course_assignment C ON A.acode = C.acode where ccode = ?;";

        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setString(1, code + "");
            var rs = ps.executeQuery();
            while (rs.next()) {
                String code = rs.getString("acode");
                String name = rs.getString("aname");
                assignmentCode.put(code, true);
                assignmentName.put(name, true);
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
        for (var i = 0; !assignmentName.isEmpty() && !assignmentCode.isEmpty(); i++) {
            out.println("-------------------------------------------------------------------Assignment name : " + assignmentName.get(i) + " , " + "Assignment code : " + assignmentCode.get(i) + " ---------------");
        }
    }

    public void createAssignment() throws IOException {
        String code, name, mark, questions;
        int Code, Mark;

        while (true) {
            out.println("-------------------------------------------------------------------Please enter assignment code---------------");
            code = in.readLine();
            if (code.matches("^\\d+$")) {
                Code = parseInt(code);
                if (Code == 0) {
                    return;
                } else if (!checkAssignmentCode(Code)) {
                    err.println("-------------------------------------------------------------------This Assignment code is already exists---------------");
                } else {
                    break;
                }
            } else {
                err.println("-------------------------------------------------------------------INVALID CODE-------------------------------------------------------------------");
            }
        }

        out.println(
                "----------------Please enter assignment name---------------");
        name = in.readLine();

        out.println(
                "----------------Please enter the assignment questions manually---------------");
        questions = in.readLine();

        while (true) {
            out.println("-------------------------------------------------------------------Please enter the assignment mark---------------");
            mark = in.readLine();
            Mark = parseInt(mark);
            if (code.matches("^\\d+$")) {
                Code = parseInt(code);
                if (Code == 0) {
                    return;
                } else if (!checkAssignmentCode(Code)) {
                    err.println("----------------This Assignment code is already exists enter another code or 0 to go back---------------");
                } else {
                    break;
                }
            } else {
                err.println("-------------------------------------------------------------------INVALID CODE-------------------------------------------------------------------");
            }
        }

        var query = "insert  into assignment (ccode,code,grade,name,question) values (?,?,?,?,?);";

        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setInt(1, this.code);
            ps.setInt(2, Code);
            ps.setInt(3, Mark);
            ps.setString(4, name);
            ps.setString(5, questions);
            ps.execute();
            out.println("-------------------------------------------------------------------SUCCESSFULLY CREATED---------------");
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
    }

    private void markActions() throws IOException {
        out.println("\n-------------------------------------------------------------------To put bonus for all students enter 1---------------");
        out.println("-------------------------------------------------------------------To put bonus for some student enter 2---------------");
        out.println("-------------------------------------------------------------------To go back enter 0---------------");

        OUTER:
        while (true) {
            var choice = in.readLine();
            switch (choice) {
                case "0":
                    return;
                case "1":
                    putBonusForAll();
                    break OUTER;
                case "2":
                    putBonusForStudent();
                    break OUTER;
                default:
                    err.println("-------------------------------------------------------------------Please enter correct input---------------");
                    break;
            }
        }
    }

    private void putBonusForAll() throws IOException {
        out.println("-------------------------------------------------------------------Please enter the bouns value---------------");
        String value;
        while (true) {
            value = in.readLine();
            if (value.matches("^\\d+$")) {
                var Value = parseInt(value);
                if (Value == 0) {
                    return;
                } else {
                    break;
                }
            } else {
                err.println("-------------------------------------------------------------------INVALID VALUE---------------");
            }
        }

        var query = "update student_course set bonus = (bonus+?) where ccode = ?;";
        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setInt(1, parseInt(value));
            ps.setInt(2, code);
            ps.executeUpdate();
        } catch (SQLException ex) {
            err.println(ex);
        }
    }

    private void putBonusForStudent() throws IOException {
        out.println("-------------------------------------------------------------------Please enter the student id---------------");
        String id;
        while (true) {
            id = in.readLine();
            if (id.matches("^\\d+$")) {
                var ID = parseInt(id);
                if (ID == 0) {
                    return;
                } else {
                    break;
                }
            } else {
                err.println("-------------------------------------------------------------------INVALID ID---------------");
            }
        }

        out.println("-------------------------------------------------------------------Please enter the bouns value---------------");
        String value;
        while (true) {
            value = in.readLine();
            if (value.matches("^\\d+$")) {
                break;
            } else {
                err.println("-------------------------------------------------------------------INVALID VALUE---------------");
            }
        }

        var query = "update student_course set bonus = (bonus+?) where ccode = ?;";
        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setInt(1, parseInt(value));
            ps.setInt(2, code);
            ps.executeUpdate();
        } catch (SQLException ex) {
            err.println(ex);
        }

    }

    public Map<Assignment, Boolean> listAssignments() {
        Map<Assignment, Boolean> assignments = new HashMap();

        var query = "select * from assignment where ccode = ?;";

        try {
            PreparedStatement ps;
            ps = con().prepareStatement(query);
            ps.setInt(1, code);
            var rs = ps.executeQuery();
            while (rs.next()) {
                var assignment = new Assignment(rs.getInt("code"));
                assignment.setName(rs.getString("name"));
                assignment.setGrade(rs.getInt("grade"));
                assignments.put(assignment, true);
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
        if (!assignments.isEmpty()) {
            for (Assignment a : assignments.keySet()) {
                out.println("-------------------------------------------------------------------Assignment name : " + a.getName() + " , "
                        + "Assignment code : " + a.getCode() + " , "
                        + "Assignment mark : " + a.getGrade());
            }
        } else {
            err.println("-------------------------------------------------------------------There is no assignments to view ---------------");
        }
        return assignments;
    }

    public Assignment viewAssignment() throws IOException {
        var assignments = listAssignments();
        if (!assignments.isEmpty()) {
            while (true) {
                out.println("-------------------------------------------------------------------Enter the assignment code---------------");
                var code = in.readLine();
                if (code.matches("^\\d+$")) {
                    var Code = parseInt(code);
                    if (Code == 0) {
                        return null;
                    } else if (!checkAssignmentCode(Code)) {
                        var a = new Assignment(parseInt(code));
                        a.viewAssignment();
                        return a;
                    } else {
                        err.println("-------------------------------------------------------------------This course code is not corrected---------------");
                    }
                } else {
                    err.println("-------------------------------------------------------------------INVALID code-------------------------------------------------------------------");
                }
            }
        }
        return null;
    }

    public void addStudent() throws IOException {
        var students = listAllStudents();
        if (!students.isEmpty()) {
            while (true) {
                out.println("-------------------------------------------------------------------Enter the student id-------------------------------------------------------------------");
                var id = in.readLine();
                if (id.matches("^\\d+$")) {
                    Integer Id = parseInt(id);
                    if (Id == 0) {
                        return;
                    } else if (!students.containsKey(Id)) {
                        err.println("-------------------------------------------------------------------this id is incorrect-------------------------------------------------------------------");
                    } else if (insertStudent(Id)) {
                        out.println("-------------------------------------------------------------------SUCCESSFULLY ADDED-------------------------------------------------------------------");
                        break;
                    } else {
                        out.println("-------------------------------------------------------------------This student is already registered-------------------------------------------------------------------");
                    }
                } else {
                    err.println("-------------------------------------------------------------------INVALID ID-------------------------------------------------------------------");
                }
            }
        }
    }

    public void removeStduent() throws IOException {
        var students = listStudents();

        if (students.isEmpty()) {
            err.println("-------------------------------------------------------------------There is no students registered in this course ---------------");
        } else {
            while (true) {
                out.println("-------------------------------------------------------------------Enter the student id-------------------------------------------------------------------");
                var id = in.readLine();
                if (id.matches("^\\d+$")) {
                    var Id = parseInt(id);
                    Id = parseInt(id);
                    if (Id == 0) {
                        return;
                    } else if (!students.containsKey(Id)) {
                        err.println("-------------------------------------------------------------------this id is incorrect-------------------------------------------------------------------");
                    } else {
                        removeStudent(Id);
                        break;
                    }
                } else {
                    err.println("-------------------------------------------------------------------INVALID ID-------------------------------------------------------------------");
                }
            }
        }
    }

    public void addTA() throws IOException {
        var TAs = listAllTAs();

        if (TAs.isEmpty()) {
            err.println("-------------------------------------------------------------------There is no TAs in the site to add ---------------");
        } else {
            while (true) {
                out.println("-------------------------------------------------------------------Enter the TA id-------------------------------------------------------------------");
                var id = in.readLine();
                if (id.matches("^\\d+$")) {
                    var Id = parseInt(id);
                    if (Id == 0) {
                        return;
                    } else if (!TAs.containsKey(Id)) {
                        err.println("-------------------------------------------------------------------INCORRECT ID-------------------------------------------------------------------");
                    } else if (addTA(Id)) {
                        out.println("-------------------------------------------------------------------SUCCESSFULLY ADDED-------------------------------------------------------------------");
                        return;
                    }
                } else {
                    err.println("-------------------------------------------------------------------INVALID ID-------------------------------------------------------------------");
                }
            }
        }
    }

    public void removeTA() throws IOException {
        var TAs = listTAs();

        if (TAs.isEmpty()) {
            err.println("-------------------------------------------------------------------There is no TAs are teaching in the course ---------------");
        } else {
            while (true) {
                out.println("-------------------------------------------------------------------Enter the TA id-------------------------------------------------------------------");
                var id = in.readLine();
                if (id.matches("^\\d+$")) {
                    var Id = parseInt(id);
                    if (Id == 0) {
                        return;
                    } else {
                        removeTA(Id);
                        return;
                    }
                } else {
                    err.println("-------------------------------------------------------------------INVALID ID-------------------------------------------------------------------");
                }
            }
        }
    }

    private boolean checkAssignmentCode(int acode) {
        PreparedStatement ps = null;
        var query = "select code from assignment where code = ?;";
        try {
            ps = con().prepareStatement(query);
            ps.setInt(1, acode);
            if (ps.executeQuery().next()) {
                return false;
            }
        } catch (SQLException e) {
            err.println(e);
        }
        return true;
    }

    private Map<Integer, Boolean> listAllStudents() {
        Map<Integer, Boolean> students = new HashMap();

        var query = "select name,id from student;";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            var rs = ps.executeQuery();
            while (rs.next()) {
                var s = rs.getInt("id");
                out.println("-------------------------------------------------------------------Student id: " + s
                        + " , Student name: " + rs.getString("name") + " ---------------");
                students.put(s, true);
            }
        } catch (SQLException ex) {
            out.println(ex.getMessage());
        }

        return students;
    }

    public Boolean insertStudent(int id) {
        Boolean isInserted = false;
        var query = "select sid from student_course where sid = ? and ccode = ?";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setInt(2, code);
            isInserted = ps.executeQuery().next();
        } catch (SQLException ex) {
            err.println(ex.getMessage());

        }
        if (!isInserted) {
            query = "insert into student_course (sid,ccode) values (?,?);";
            try {
                PreparedStatement ps;
                ps = con.prepareStatement(query);
                ps.setInt(1, id);
                ps.setInt(2, code);
                ps.execute();
                return true;
            } catch (SQLException ex) {
                err.println(ex.getMessage());
                return false;
            }
        }
        return false;
    }

    private Map<Integer, Boolean> listStudents() {
        Map<Integer, Boolean> students = new HashMap();

        var query = "select S.id, S.name from student_course C join student S on C.sid = S.id where C.ccode = ?;";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            ps.setInt(1, code);
            var rs = ps.executeQuery();
            while (rs.next()) {
                var s = rs.getInt("id");
                out.println("-------------------------------------------------------------------Student id: " + s
                        + " , Student name: " + rs.getString("name") + " ---------------");
                students.put(s, true);
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
        return students;
    }

    private void removeStudent(int id) {
        var query = "delete from student_course where ccode = ? and sid = ?";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            ps.setInt(1, code);
            ps.setInt(2, id);
            ps.execute();
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
    }

    private Map<Integer, Boolean> listAllTAs() {
        Map<Integer, Boolean> TAs = new HashMap();
        var query = "select name,id from TA;";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            var rs = ps.executeQuery();
            while (rs.next()) {
                var t = rs.getInt("id");
                out.println("-------------------------------------------------------------------TA id: " + t + " , "
                        + "TA name: " + rs.getString("name") + " ---------------");
                TAs.put(t, true);
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
        return TAs;
    }

    private void insertTA(int id) {
        var query = "insert into TA_course (tid,ccode) values(?,?);";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setInt(2, code);
            ps.execute();
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
    }

    private Map<Integer, Boolean> listTAs() {
        Map<Integer, Boolean> TAs = new HashMap();

        var query = "select T.id, T.name from TA_course C join TA T on C.tid = T.id where C.ccode = ?;";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            ps.setInt(1, code);
            var rs = ps.executeQuery();
            while (rs.next()) {
                var s = rs.getInt("id");
                out.println("-------------------------------------------------------------------TA id: " + s
                        + " , TA name: " + rs.getString("name") + " ---------------");
                TAs.put(s, true);
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
        return TAs;
    }

    private Boolean addTA(int id) {
        var query = "insert into TA_course Values (?,?);";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setInt(2, code);
            ps.execute();
            return true;
        } catch (SQLException ex) {
            err.println(ex.getMessage());
            return false;
        }

    }

    private void removeTA(int id) {
        var query = "delete from TA_course where ccode = ? and tid = ?";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            ps.setInt(1, code);
            ps.setInt(2, id);
            ps.execute();
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
    }

    public void studentGradeReport(int id) {
        var query = "select S.id, S.name, C.code,C.name, A.yearmark,A.midmark,A.finalmark,A.bonus,totalmark from Student S join student_course A on S.id = A.sid join course C on C.code = A.ccode where C.code = ? and S.id = ?;";
        try {
            PreparedStatement ps;
            ps = con.prepareStatement(query);
            ps.setInt(1, code);
            ps.setInt(2, id);
            var rs = ps.executeQuery();
            while (rs.next()) {
                out.println("-------------------------------------------------------------------Coures name : " + rs.getString("C.name") + " , Course code : " + rs.getString("C.code") + "-------------------------------------------------------------------");
                out.println("-------------------------------------------------------------------Student name : " + rs.getString("S.name") + " , Student id : " + rs.getString("S.id") + "-------------------------------------------------------------------");
                var yearMark = rs.getInt("A.yearmark");
                var midMark = rs.getInt("A.midmark");
                var finalrMark = rs.getInt("A.finalmark");
                var bonusMark = rs.getInt("A.bonus");
                var totalMark = rs.getInt("A.totalmark");
                out.println("-------------------------------------------------------------------Workcourse Mark : " + (yearMark == -1 ? "unknown" : yearMark) + "-------------------------------------------------------------------");
                out.println("-------------------------------------------------------------------MidTerm Exam Mark : " + (midMark == -1 ? "unknown" : midMark) + "-------------------------------------------------------------------");
                out.println("-------------------------------------------------------------------Final Exam Mark : " + (finalrMark == -1 ? "unknown" : finalrMark) + "-------------------------------------------------------------------");
                out.println("-------------------------------------------------------------------Bonus Marks : " + (bonusMark == -1 ? "unknown" : bonusMark) + "-------------------------------------------------------------------");
                out.println("-------------------------------------------------------------------Total Mark : " + (totalMark == -1 ? "unknown" : totalMark) + "-------------------------------------------------------------------");
            }
        } catch (SQLException ex) {
            err.println(ex.getMessage());
        }
    }

    private boolean checkTAId(int id) {
        Boolean result = false;
        var query = "select id from TA where id = ?";
        try {
            var ps = con.prepareStatement(query);
            ps.setInt(1, id);
            result = ps.executeQuery().next();
        } catch (SQLException e) {
            err.println(e);
        }

        query = "select tid from TA_course where tid = ? and ccode = ?";
        try {
            var ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.setInt(2, code);
            return result && !ps.executeQuery().next();
        } catch (SQLException e) {
            err.println(e);
        }
        return false;
    }
}
