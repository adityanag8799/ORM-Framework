import java.util.Date;
import java.text.*;
import java.util.*;
import com.blue.code.annotations.*;
import com.blue.code.DataManager.*;
import com.blue.code.pojo.*;
class testpsp
{
public static void main(String gg[])
{
try
{
DataManager dm=DataManager.getDataManager();
dm.begin();
//Course c=new Course();
//c.settitle("Python");
//dm.save(c);
Student s=new Student();
s.setfirstName("Virat");
s.setlastName("Kishan");
s.setgender("M");
s.setrollNumber(1010);
s.setcourseCode(2);
s.setaadharCardNumber("98ffffgggg");
String dateString="1997-11-10";
try
{
DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
Date date=df.parse(dateString);
s.setdateOfBirth(date);
}catch(Exception e)
{
System.out.println(e);
}
dm.save(s).fire();
//dm.delete(Student.class,1008).fire();
//Object o=dm.query(Student.class).fire();
//Object o=dm.view(Student.class,"getRecordGreaterThan1001").fire();
/*Object o=dm.query(Student.class).where("rollNumber").gt("1001").fire();
System.out.println("done");
List<Object> list=(List<Object>)o;
for(Object oo:list)
{
Student st=(Student)oo;
System.out.println(st.getrollNumber());
System.out.println(st.getfirstName());
System.out.println(st.getlastName());
System.out.println(st.getaadharCardNumber());
System.out.println(st.getgender());
System.out.println(st.getcourseCode());
System.out.println(st.getdateOfBirth().toString());
System.out.println("-----------------------");
}*/
dm.end();
}catch(Exception e)
{
System.out.println(e);
}
}
}
