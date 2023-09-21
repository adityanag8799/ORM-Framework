The ORM (Object-Relational Mapping) Framework is a powerful tool designed to simplify database interactions for Java developers. With this framework, developers can seamlessly work with databases without the need to write SQL statements. 
This documentation will guide you through the setup and usage of this framework, showcasing its features and capabilities.

**Prerequisites:**
Before getting started with the ORM Framework, ensure that you have set the JAVA_HOME environment variable in your system's path,
especially if you are using Linux or macOS.

**Getting Started:**
To begin using the ORM Framework, follow these steps:

**1) Create a Configuration File:**

Create a conf.json file in your working directory with the following configuration parameters:
{
  "jdbc-driver": "com.mysql.cj.jdbc.Driver",
  "connection-url": "jdbc:mysql://localhost:3306/tmdbschool",
  "username": "tmwebrockuser",
  "password": "tmwebrockuser",
  "package": "com.code",
  "jar-file": "pojo.jar"
}

**2) Generate POJO Classes:**

Run the provided code in test.java located in the orm folder. This code will generate POJO (Plain Old Java Object) classes for all the tables specified in the conf.json file. These classes will be placed in the src folder, and compiled POJO classes will be stored in a JAR file in the dist folder, with the JAR file name defined in conf.json.

import java.text.*;
import java.util.*;
import com.annotations.*;
import com.DataManager.*;

public class testpsp {
  public static void main(String gg[]) {
    try {
      DataManager dm = DataManager.getDataManager();
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}

**3) Compile and Run:**

Compile the generated code using the following command:

javac -classpath c:\javaprojects\orm\lib\*;c:\javaprojects\orm;. *.java;

**4) Run the compiled code with:**

java -classpath c:\javaprojects\orm\lib\*;c:\javaprojects\orm;. testpsp; 

5)**Basic Operations:**

**Save Data:**

To save a record in the Student table, follow this example:

DataManager dm = DataManager.getDataManager();
dm.begin();
Student s = new Student();
s.setFirstName("Ishan");
s.setLastName("Kishan");
s.setGender("M");
s.setRollNumber(1009);
s.setCourseCode(2);
s.setAadharCardNumber("98fffff");
String dateString = "1997-11-10";
try {
  DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
  Date date = df.parse(dateString);
  s.setDateOfBirth(date);
} catch (Exception e) {
  System.out.println(e);
}
dm.save(s).fire();
dm.end();


**Update Data:**
To update data, simply use the update method:

dm.update(s).fire();
Delete Data:
To delete data, specify the primary key while calling the delete method:
java
Copy code
dm.begin();
dm.delete(Student.class, 1008).fire();
dm.end();
Retrieve Data:
To select data, use the query method:
java
Copy code
dm.begin();
Object o = dm.query(Student.class).fire();
List<Object> list = (List<Object>) o;
for (Object oo : list) {
  Student st = (Student) oo;
  // Access and print the desired fields here
}
dm.end();


**Working with Joins:**

To work with joins, create views in the database and use the view function to retrieve data from them.

dm.begin();
Object o = dm.view(Student.class, "getRecordGreaterThan1001").fire();
List<Object> list = (List<Object>) o;
for (Object oo : list) {
  Student st = (Student) oo;
  // Access and print the desired fields here
}
dm.end();

**Advanced Querying:**

The ORM Framework supports advanced querying with operators such as and, or, gt, le, eq, and ne. Here's an example:

dm.begin();
Object o = dm.query(Student.class).where("rollNumber").gt("1001").fire();
List<Object> list = (List<Object>) o;
for (Object oo : list) {
  Student st = (Student) oo;
  // Access and print the desired fields here
}
dm.end();

Thank you for using the ORM Framework. We hope this documentation helps you efficiently manage your database operations.
If you have any questions or need further assistance, please don't hesitate to reach out.
