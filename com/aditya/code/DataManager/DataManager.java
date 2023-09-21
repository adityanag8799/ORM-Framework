package com.aditya.code.DataManager;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject;
import java.io.*;
import javax.tools.DiagnosticCollector;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.io.File.*;
import java.util.*;
import java.sql.*;
import com.google.gson.*;
import javax.annotation.processing.Processor;
import java.lang.reflect.*;
import javafx.util.Pair;
import com.aditya.code.annotations.*;
public class DataManager
{
static DataManager dm=null;
public Pair<String,String> updatePrimaryKeyPair;
public Pair<String,String> deleteKeyPair;
public PreparedStatement ps;
public String insertStatement;
public String tableNameForSelect;
public StringBuilder insert;
public StringBuilder update;
public StringBuilder deleteStatement;
public StringBuilder updateWhere;
public StringBuilder sb;
public StringBuilder valuesb;
public String jdbcDriver="";
public String connectionUrl="";
public String username="";
public boolean flag=false;;
public String password="";
public String selectTableName="";
Connection connection=null;
public String packageStructure;
public String jarFileName;
HashMap<String,Info> mainMap;
ArrayList<String> viewList;
HashMap<String,Pair<String,String>> methodMap;
Gson gson=null;
String sqlStatement="";
String newPackage;
private static void compileJavaFiles(String sourceDirectory, String outputDirectory) 
{
JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
try 
{
fileManager.setLocation(StandardLocation.CLASS_OUTPUT,Arrays.asList(new File(outputDirectory)));
List<File> sourceFiles = getSourceFiles(sourceDirectory);
Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, getCompilationOptions(), null, compilationUnits);
boolean success = task.call();
if(success) 
{
System.out.println("Compilation was successful.");
} 
else 
{
for (javax.tools.Diagnostic<? extends JavaFileObject> diagnostic : diagnosticCollector.getDiagnostics()) {
System.err.format("Error on line %d in %s%n",diagnostic.getLineNumber(),diagnostic.getSource().toUri());
System.err.println(diagnostic.getMessage(null));
}
}
} 
catch (IOException e) 
{
e.printStackTrace();
} 
finally
{
try 
{
fileManager.close();
} 
catch (IOException e) 
{
e.printStackTrace();
}
}
}
private static List<File> getSourceFiles(String sourceDirectory) 
{
List<File> sourceFiles = new ArrayList<>();
populateSourceFiles(new File(sourceDirectory), sourceFiles);
return sourceFiles;
}    
private static void populateSourceFiles(File directory, List<File> sourceFiles) 
{
if (directory.isDirectory()) 
{
File[] files = directory.listFiles();
if (files != null) 
{
for (File file : files) 
{
if (file.isDirectory()) 
{
populateSourceFiles(file, sourceFiles);
} 
else if (file.getName().endsWith(".java")) 
{
sourceFiles.add(file);
}
}
}
}
}
private static Iterable<String> getCompilationOptions() 
{
return Arrays.asList("-classpath", "C:/javaprojects/orm/lib/*;C:/javaprojects/orm;.");
}
private static Iterable<? extends JavaFileObject> getSourceFileObjects(StandardJavaFileManager fileManager, List<File> sourceFiles) 
{
return fileManager.getJavaFileObjectsFromFiles(sourceFiles);
}
private static void createJar(String directory, String jarFile) throws DataManagerException
{
try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream("dist"+File.separator+jarFile))) 
{
File sourceDirectory = new File(directory);
addFilesToJar(sourceDirectory, sourceDirectory, jarOutputStream);
System.out.println("JAR file created successfully.");
}catch (Exception e) 
{
throw new DataManagerException("Error creating JAR file: " + e.getMessage());
}
}
private static void addFilesToJar(File rootDir, File sourceDir, JarOutputStream jarOutputStream) throws DataManagerException
{
try
{
for (File file : sourceDir.listFiles()) 
{
if (file.isDirectory()) 
{
addFilesToJar(rootDir, file, jarOutputStream);
} 
else 
{
String entryName = rootDir.toURI().relativize(file.toURI()).getPath();
JarEntry jarEntry = new JarEntry(entryName);
jarOutputStream.putNextEntry(jarEntry);
Files.copy(file.toPath(), jarOutputStream);
jarOutputStream.closeEntry();
}
}
}catch(Exception e)
{
System.out.println(e);
}
}
private static void deleteDirectory(File directory) 
{
File[] files = directory.listFiles();
if (files != null) 
{
for (File file : files) 
{                
if (file.isDirectory()) 
{
deleteDirectory(file);
} 
else 
{
file.delete();
}
}
}
directory.delete();
}
public void init() throws DataManagerException
{
try
{
this.ps=null;
FileWriter writer=null;
this.gson=new Gson();
this.viewList=new ArrayList<>();
String currentDirectory = System.getProperty("user.dir");
String fileName=currentDirectory+"/conf.json";
JsonObject jsonObject=gson.fromJson(new FileReader(fileName),JsonObject.class);
this.jdbcDriver=jsonObject.get("jdbc-driver").getAsString();
if(this.jdbcDriver==null) throw new DataManagerException("Define jdbc-driver in conf.json");
this.connectionUrl=jsonObject.get("connection-url").getAsString();
if(this.connectionUrl==null) throw new DataManagerException("Define connection-url in conf.json");
this.username=jsonObject.get("username").getAsString();
if(this.username==null) throw new DataManagerException("Define username in conf.json");
this.password=jsonObject.get("password").getAsString();
if(this.password==null) throw new DataManagerException("Define password in conf.json");
this.packageStructure=jsonObject.get("package").getAsString();
if(this.packageStructure==null) throw new DataManagerException("Please provide package structure in conf.json");
this.jarFileName=jsonObject.get("jar-file").getAsString();
if(this.jarFileName==null) throw new DataManagerException("Please provide jar file name in conf.json");
this.newPackage=this.packageStructure.replace(".","/");
File directory=new File(currentDirectory+"/src/"+newPackage+"/pojo");
File dist=new File(currentDirectory+"/dist");
if(dist.exists()) deleteDirectory(dist);
dist.mkdirs();
if(directory.exists()) 
{
deleteDirectory(directory);
}
directory.mkdirs();
Class.forName(jdbcDriver);
this.connection=DriverManager.getConnection(connectionUrl,username,password);
DatabaseMetaData meta=this.connection.getMetaData();
ResultSet rs = meta.getTables(null, null, "%", new String[] { "TABLE" });
ResultSet viewResultSet = meta.getTables(null, null, null, new String[]{"VIEW"});
while(viewResultSet.next()) 
{
String viewName = viewResultSet.getString("TABLE_NAME");
String viewCatalog = viewResultSet.getString("TABLE_CAT");
String viewSchema = viewResultSet.getString("TABLE_SCHEM");
String viewType = viewResultSet.getString("TABLE_TYPE");
if (viewType.equalsIgnoreCase("VIEW")) 
{
this.viewList.add(viewName.toLowerCase());
}
}
while(rs.next())
{
HashMap<String,String> hm=new HashMap<String,String>();
String columnName="";
String typeName="";
String camelCase="";
StringBuilder newColumnName=null;
boolean cap=false;
HashMap<String,Info> tempMap=new HashMap<String,Info>();
methodMap=new HashMap<String,Pair<String,String>>();
ArrayList<Pair<String,String>> methodList=new ArrayList<Pair<String,String>>();
ArrayList<Pair<String,String>> updateMethodList=new ArrayList<Pair<String,String>>();
String tableName=rs.getString("TABLE_NAME");
String newTableName=tableName.substring(0,1).toUpperCase()+tableName.substring(1);
File file=new File(currentDirectory+"/src/"+newPackage+"/pojo/"+newTableName+".java");
if(file.exists()) file.delete();
writer=new FileWriter(file);
writer.write("package "+this.packageStructure+".pojo"+";\n");
writer.write("import java.util.Date;\n");
writer.write("import com.aditya.code.annotations.*;\n");
writer.write("@Table(name=\""+tableName+"\")\n");
writer.write("public class "+newTableName+"\n");
writer.write("{\n");
ResultSet rsColumns = meta.getColumns(null, null, tableName, null);
int size = 0;
if(rsColumns.last()) 
{
size = rsColumns.getRow();
rsColumns.beforeFirst();
}
this.insert=new StringBuilder();
this.insert.append("insert into "+tableName+" (");
this.update=new StringBuilder();
this.deleteStatement=new StringBuilder();
this.updateWhere=new StringBuilder();
this.updateWhere.append(" where ");
this.update.append("update "+tableName+" set ");
this.deleteStatement.append("delete from "+tableName+" where ");
Info info=new Info();
while(rsColumns.next())
{
columnName=rsColumns.getString("COLUMN_NAME");
//this.insert.append(columnName+",");
newColumnName=new StringBuilder();
cap=false;
for(int i=0;i<columnName.length();i++)
{
char c=columnName.charAt(i);
if(c=='_') cap=true;
else
{
if(cap)
{
newColumnName.append(Character.toUpperCase(c));
cap=false;
}
else
{
newColumnName.append(c);
}
}
}
camelCase=newColumnName.toString();
info.setColumnName(camelCase);
writer.write("@Column(name=\""+columnName+"\")\n");
String autoIncrement=rsColumns.getString("IS_AUTOINCREMENT");
ResultSet foreignKeyRs=meta.getImportedKeys(null,null,tableName);
while(foreignKeyRs.next())
{
if(foreignKeyRs.getString("FKCOLUMN_NAME").equals(columnName))
{
String parent=foreignKeyRs.getString("PKTABLE_NAME");
String col=foreignKeyRs.getString("PKCOLUMN_NAME");
writer.write("@ForeignKey(parent=\""+parent+"\",column=\""+col+"\")\n");
info.setIsForeignKey(true);
}
}
ResultSet primaryKeyRs=meta.getPrimaryKeys(null,null,tableName);
while(primaryKeyRs.next())
{
if(primaryKeyRs.getString("COLUMN_NAME").equals(columnName)) 
{
writer.write("@PrimaryKey\n");
info.setIsPrimaryKey(true);
}
else
{
info.setIsPrimaryKey(false);
}
}
if(autoIncrement.equals("YES")) 
{
size--;
info.setIsAutoIncrement(true);
writer.write("@AutoIncrement\n");
}
else 
{
this.insert.append(columnName+",");
info.setIsAutoIncrement(false);
}
typeName=rsColumns.getString("TYPE_NAME");
info.setTypeName(typeName);
tempMap.put(columnName,info);
if(typeName.equals("INT")) typeName="int";
else if(typeName.equals("VARCHAR")) typeName="String";
else if(typeName.equals("DATE")) typeName="Date";
writer.write(typeName+" "+camelCase+";\n");
Pair<String,String> methodPair=new Pair<String,String>(typeName,newColumnName.toString());
this.methodMap.put(columnName,methodPair);
if(info.getIsPrimaryKey()==true)
{
this.deleteStatement.append(columnName+"=?");
this.updateWhere.append(columnName+"=?");
String methodName="get"+newColumnName;
Pair<String,String> p=new Pair<>(typeName,methodName);
this.updatePrimaryKeyPair=p;
this.deleteKeyPair=p;
}
if(info.getIsAutoIncrement()==false)
{
String methodName="get"+newColumnName;
Pair<String,String> p=new Pair<>(typeName,methodName);
methodList.add(p);
}
if(info.getIsAutoIncrement()==false && info.getIsPrimaryKey()==false)
{
this.update.append(columnName+"=?,");
String methodName="get"+newColumnName;
Pair<String,String> p=new Pair<>(typeName,methodName);
updateMethodList.add(p);
}
hm.put(camelCase,typeName);
}
info.setMethodMap(this.methodMap);
updateMethodList.add(this.updatePrimaryKeyPair);
int li=this.insert.lastIndexOf(",");
this.insert.deleteCharAt(li);
this.insert.append(")values(");
li=this.update.lastIndexOf(",");
this.update.deleteCharAt(li);
this.update.append(updateWhere);
for(int i=0;i<size;i++) 
{
this.insert.append("?,");
}
li=this.insert.lastIndexOf(",");
this.insert.deleteCharAt(li);
this.insert.append(")");
info.setInsertStatement(this.insert);
info.setUpdateStatement(this.update);
info.setMethodList(methodList);
info.setUpdateMethodList(updateMethodList);
info.setDeleteStatement(this.deleteStatement);
info.setDeleteKeyPair(this.deleteKeyPair);
for(Map.Entry<String,String> entry:hm.entrySet())
{
writer.write("public void set"+entry.getKey()+"("+entry.getValue()+" "+entry.getKey()+")\n");
writer.write("{\n");
writer.write("this."+entry.getKey()+"="+entry.getKey()+";\n");
writer.write("}\n"); 
writer.write("public "+entry.getValue()+" get"+entry.getKey()+"()\n");
writer.write("{\n"); 
writer.write("return this."+entry.getKey()+";\n");
writer.write("}\n");
}
writer.write("}\n");
writer.close();
this.mainMap.put(tableName,info);
}
String sourceDirectory = "src/"+this.newPackage+"/pojo/";
String outputDirectory = "dist/";
String jarFile = this.jarFileName;
compileJavaFiles(sourceDirectory, outputDirectory);
createJar(outputDirectory+this.newPackage+"/pojo", jarFile);
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}

}
public DataManager() throws DataManagerException
{
try
{
this.sb=new StringBuilder();
this.valuesb=new StringBuilder();
this.sqlStatement="";
this.connection=null;
this.flag=false;
this.mainMap=new HashMap<String,Info>();
init();
JsonObject jsonObject=gson.fromJson(new FileReader("conf.json"),JsonObject.class);
this.jdbcDriver=jsonObject.get("jdbc-driver").getAsString();
this.connectionUrl=jsonObject.get("connection-url").getAsString();
this.username=jsonObject.get("username").getAsString();
this.password=jsonObject.get("password").getAsString();
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}
}
public static DataManager getDataManager() throws DataManagerException
{
dm=new DataManager();
return dm;
}
public void begin() throws DataManagerException
{
try
{
this.connection=DriverManager.getConnection(this.connectionUrl,username,password);
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}
}
public void end() throws DataManagerException
{
try
{
this.connection.close();
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}
}
public DataManager save(Object object) throws DataManagerException
{
try
{
this.ps=null;
this.flag=false;
String cname=object.getClass().getSimpleName();
Class c=object.getClass();
for(Map.Entry<String,Info> m:mainMap.entrySet())
{
if(m.getKey().equalsIgnoreCase(cname)) 
{
Info i=m.getValue();
String statement=i.getInsertStatement().toString();
this.ps=this.connection.prepareStatement(statement);
ArrayList<Pair<String,String>> al=i.getMethodList();
int idx=1;
for(int j=0;j<al.size();j++)
{
Pair<String,String> pp=al.get(j);
Method method=c.getMethod(pp.getValue());
if(pp.getKey().equals("int")) 
{
int result=(int)method.invoke(object);
this.ps.setInt(idx,result);
idx++;
}
else if(pp.getKey().equals("String")) 
{
String result=(String)method.invoke(object);
this.ps.setString(idx,result);
idx++;
}
else if(pp.getKey().equals("Date")) 
{
Object result=method.invoke(object);
java.util.Date date=(java.util.Date)result;
java.sql.Date sqlDate=new java.sql.Date(date.getTime());
this.ps.setDate(idx,sqlDate);
idx++;
}
}
}
}
String tname=object.getClass().getSimpleName();
for(Map.Entry<String,Info> e:mainMap.entrySet())
{
if(tname.equals(e.getKey()))
{
Info i=e.getValue();
HashMap<String,Pair<String,String>> h=i.getMethodMap();
for(Map.Entry<String,Pair<String,String>> g:h.entrySet())
{
if(i.getIsForeignKey()==true)
{
Class cc=Class.forName(object.getClass().getName());
Field[] fields=cc.getDeclaredFields();
for(Field field:fields)
{
ForeignKey fk=(ForeignKey)field.getAnnotation(ForeignKey.class);
if(fk!=null)
{
String fkpt=fk.parent();
String fkc=fk.column();
DatabaseMetaData meta=this.connection.getMetaData();
ResultSet frs=meta.getColumns(null,null,fkpt,fkc);
if(frs.next()==false) throw new DataManagerException("Parent table has no key");
}
}
}
}
}
}
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}
return this.dm;
}
public DataManager update(Object object) throws DataManagerException
{
try
{
this.ps=null;
this.flag=false;
String cname=object.getClass().getSimpleName();
Class c=object.getClass();
for(Map.Entry<String,Info> m:mainMap.entrySet())
{
if(m.getKey().equalsIgnoreCase(cname))
{
Info i=m.getValue();
String statement=i.getUpdateStatement().toString();
this.ps=this.connection.prepareStatement(statement);
ArrayList<Pair<String,String>> al=i.getUpdateMethodList();
int idx=1;
for(int j=0;j<al.size();j++)
{
Pair<String,String> pp=al.get(j);
Method method=c.getMethod(pp.getValue());
if(pp.getKey().equals("int"))
{
int result=(int)method.invoke(object);
this.ps.setInt(idx,result);
idx++;
}
else if(pp.getKey().equals("String"))
{
String result=(String)method.invoke(object);
this.ps.setString(idx,result);
idx++;
}
else if(pp.getKey().equals("Date"))
{
Object result=method.invoke(object);
java.util.Date date=(java.util.Date)result;
java.sql.Date sqlDate=new java.sql.Date(date.getTime());
this.ps.setDate(idx,sqlDate);
idx++;
}
}
}
}
int primaryKeyFlag=0;
String tname=object.getClass().getSimpleName();
for(Map.Entry<String,Info> e:mainMap.entrySet())
{
if(tname.equals(e.getKey()))
{
Info i=e.getValue();
if(i.getIsPrimaryKey()==true) primaryKeyFlag=1;
HashMap<String,Pair<String,String>> h=i.getMethodMap();
for(Map.Entry<String,Pair<String,String>> g:h.entrySet())
{
String methodName="get"+g.getValue().getValue();
Method method=object.getClass().getMethod(methodName);
Object result=method.invoke(object);
if(i.getIsForeignKey()==true)
{
Class cc=i.getInfoClass();
Field[] fields=cc.getDeclaredFields();
String fkpt=null;
String fkc=null;
for(Field field:fields)
{
ForeignKey fk=(ForeignKey)field.getAnnotation(ForeignKey.class);
if(fk!=null)
{
fkpt=fk.parent();
fkc=fk.column();
}
}
String checkQuery=null;
if(i.getTypeName().equals("INT"))
{
int res=(int)result;
checkQuery="Select * from "+fkpt+" where "+fkc+"="+res;
}
if(i.getTypeName().equals("VARCHAR"))
{
String res=(String)result;
checkQuery="Select * from "+fkpt+" where "+fkc+"="+"\""+res+"\"";
}
Statement st=this.connection.createStatement();
ResultSet chkrs=st.executeQuery(checkQuery);
if(chkrs.next()==false) throw new DataManagerException("Value is not exist in parent table");
}
}
}
}
if(primaryKeyFlag==0) throw new DataManagerException("Primary key is not present");
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}
return this.dm;
}
public DataManager delete(Class c,Object object) throws DataManagerException
{
try
{
this.ps=null;
this.flag=false;
String cname=c.getSimpleName();
Class cc=object.getClass();
Object o=c.newInstance();
for(Map.Entry<String,Info> m:mainMap.entrySet())
{
if(m.getKey().equalsIgnoreCase(cname))
{
Info i=m.getValue();
String statement=i.getDeleteStatement().toString();
this.ps=this.connection.prepareStatement(statement);
Pair<String,String> pp=i.getDeleteKeyPair();
Method method=c.getMethod(pp.getValue());
if(pp.getKey().equals("int"))
{
int result=(int)object;
this.ps.setInt(1,result);
}
else if(pp.getKey().equals("String"))
{
String result=(String)object;
this.ps.setString(1,result);
}
else if(pp.getKey().equals("Date"))
{
java.util.Date date=(java.util.Date)object;
java.sql.Date sqlDate=new java.sql.Date(date.getTime());
this.ps.setDate(1,sqlDate);
}
}
}
String tname=c.getName();
Field[] fields=c.getDeclaredFields();
String pkcname=null;
for(Field f:fields)
{
PrimaryKey pk=(PrimaryKey)f.getAnnotation(PrimaryKey.class);
if(pk!=null)
{
pkcname=f.getName();
}
}
for(Map.Entry<String,Info> e:mainMap.entrySet())
{
if(c.getSimpleName().equalsIgnoreCase(e.getKey()))
{
Info i=e.getValue();
HashMap<String,Pair<String,String>> h=i.getMethodMap();
for(Map.Entry<String,Pair<String,String>> g:h.entrySet())
{
if(g.getKey().equals(pkcname))
{
DatabaseMetaData meta=this.connection.getMetaData();
ResultSet chkrs=meta.getExportedKeys(null,null,tname);
while(chkrs.next())
{
String childTable=chkrs.getString("FKTABLE_NAME");
String childTableForeignKey=chkrs.getString("FKCOLUMN_NAME");
Statement st=this.connection.createStatement();
if(i.getTypeName().equals("INT"))
{
int res=(int)o;
String query="Select * from "+childTable+" where "+childTableForeignKey+"="+res;
ResultSet rs=st.executeQuery(query);
if(rs.next()) throw new DataManagerException("can't perform the act similar key is foreign key in other table");
}
else if(i.getTypeName().equals("VARCHAR"))
{
String res=(String)o;
String query="Select * from "+childTable+" where "+childTableForeignKey+"="+res;
ResultSet rs=st.executeQuery(query);
if(rs.next()) throw new DataManagerException("can't perform the act similar key is foreign key in other table");
}
}
}
}
}
}
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}
return this.dm;
}
public DataManager view(Class c,String viewName) throws DataManagerException
{
try
{
if(this.viewList.indexOf(viewName.toLowerCase())==-1) throw new DataManagerException("View is not present");
this.flag=true;
this.selectTableName=c.getName();
this.sb.append("select * from "+viewName);
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}
return this.dm;
}
public DataManager query(Class c) throws DataManagerException
{
try
{
this.flag=true;
String tname=c.getSimpleName();
this.tableNameForSelect=tname;
this.selectTableName=c.getName();
this.sb.append("select * from "+tname);
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}
return this.dm;
}
public DataManager where(String s) throws DataManagerException
{
for(Map.Entry<String,Info> e:mainMap.entrySet())
{
if(this.tableNameForSelect.equalsIgnoreCase(e.getKey()))
{
Info i=e.getValue();
HashMap<String,Pair<String,String>> h=i.getMethodMap();
for(Map.Entry<String,Pair<String,String>> g:h.entrySet())
{
if(g.getValue().getValue().equals(s))
{
this.sb.append(" where "+g.getKey());
break;
}
}
}
}
return this.dm;
}
public DataManager and(String s) throws DataManagerException
{
for(Map.Entry<String,Info> e:mainMap.entrySet())
{
if(this.tableNameForSelect.equalsIgnoreCase(e.getKey()))
{
Info i=e.getValue();
HashMap<String,Pair<String,String>> h=i.getMethodMap();
for(Map.Entry<String,Pair<String,String>> g:h.entrySet())
{
if(g.getValue().getValue().equals(s))
{
this.sb.append(" and "+g.getKey());
break;
}
}
}
}
return this.dm;
}
public DataManager or(String s) throws DataManagerException
{
for(Map.Entry<String,Info> e:mainMap.entrySet())
{
if(this.tableNameForSelect.equalsIgnoreCase(e.getKey()))
{
Info i=e.getValue();
HashMap<String,Pair<String,String>> h=i.getMethodMap();
for(Map.Entry<String,Pair<String,String>> g:h.entrySet())
{
if(g.getValue().getValue().equals(s))
{
this.sb.append(" or "+g.getKey());
break;
}
}
}
}
return this.dm;
}
public DataManager gt(Object o) throws DataManagerException
{
if(o instanceof Integer)
{
int res=(int)o;
this.sb.append(">"+res+" ");
}
if(o instanceof String)
{
String res=(String)o;
this.sb.append(">"+"\""+res+"\"");
}
return this.dm;
}
public DataManager lt(Object o) throws DataManagerException
{
if(o instanceof Integer)
{
int res=(int)o;
this.sb.append("<"+res+" ");
}
if(o instanceof String)
{
String res=(String)o;
this.sb.append("<"+"\""+res+"\"");
}
return this.dm;
}
public DataManager ge(Object o) throws DataManagerException
{
if(o instanceof Integer)
{
int res=(int)o;
this.sb.append(">="+res+" ");
}
if(o instanceof String)
{
String res=(String)o;
this.sb.append(">="+"\""+res+"\"");
}
return this.dm;
}
public DataManager le(Object o) throws DataManagerException
{
if(o instanceof Integer)
{
int res=(int)o;
this.sb.append("<="+res+" ");
}
if(o instanceof String)
{
String res=(String)o;
this.sb.append("<="+"\""+res+"\"");
}
return this.dm;
}
public DataManager ne(Object o) throws DataManagerException
{
if(o instanceof Integer)
{
int res=(int)o;
this.sb.append("!="+res+" ");
}
if(o instanceof String)
{
String res=(String)o;
this.sb.append("!="+"\""+res+"\"");
}
return this.dm;
}
public Object fire() throws DataManagerException
{
try
{
if(this.flag==false)
{
this.ps.executeUpdate();
}
else
{
List<Object> list=new ArrayList<Object>();
this.sqlStatement=this.sb.toString();
Class c=Class.forName(this.selectTableName);
PreparedStatement st=this.connection.prepareStatement(this.sqlStatement);
ResultSet rs=st.executeQuery();
while(rs.next())
{
for(Map.Entry<String,Info> e:mainMap.entrySet())
{
if(c.getSimpleName().equalsIgnoreCase(e.getKey()))
{
Info i=e.getValue();
Object oo=c.newInstance();
HashMap<String,Pair<String,String>> h=i.getMethodMap();
for(Map.Entry<String,Pair<String,String>> g:h.entrySet())
{
Pair p=g.getValue();
if(p.getKey().equals("int"))
{
int res=rs.getInt(g.getKey());
String methodName="set"+p.getValue();
Method method=c.getMethod(methodName,int.class);
method.invoke(oo,res);
}
if(p.getKey().equals("String"))
{
String res=rs.getString(g.getKey());
String methodName="set"+p.getValue();
Method method=c.getMethod(methodName,String.class);
method.invoke(oo,res);
}
if(p.getKey().equals("Date"))
{
java.sql.Date date=rs.getDate(g.getKey());
java.util.Date res=new java.util.Date(date.getTime());
String methodName="set"+p.getValue();
Method method=c.getMethod(methodName,java.util.Date.class);
method.invoke(oo,res);
}
}
list.add(oo);
}
}
}
return list;
}
}catch(Exception e)
{
throw new DataManagerException(e.getMessage());
}
return null;
}
}
