package com.aditya.code.DataManager;
import javafx.util.Pair;
import java.util.*;
public class Info
{
public String columnName;
public String typeName;
public boolean isAutoIncrement;
public boolean isForeignKey;
public boolean isPrimaryKey;
public Class c;
public StringBuilder insertStatement;
public StringBuilder updateStatement;
public StringBuilder deleteStatement;

public HashMap<String,Pair<String,String> > methodMap;
public ArrayList<Pair<String,String>> methodList;
public ArrayList<Pair<String,String>> updateMethodList;
public Pair<String,String> deleteKeyPair;
public Info()
{
this.columnName="";
this.typeName="";
this.isAutoIncrement=false;
this.isForeignKey=false;
this.isPrimaryKey=false;
this.c=null;
this.insertStatement=null;
this.updateStatement=null;
this.methodMap=null;
this.methodList=null;
this.updateMethodList=null;
this.deleteStatement=null;
this.deleteKeyPair=null;
}
public void setColumnName(String columnName)
{
this.columnName=columnName;
}
public String getColumnName()
{
return this.columnName;
}
public void setTypeName(String typeName)
{
this.typeName=typeName;
}
public String getTypeName()
{
return this.typeName;
}
public void setIsAutoIncrement(boolean isAutoIncrement)
{
this.isAutoIncrement=isAutoIncrement;
}
public boolean getIsAutoIncrement()
{
return this.isAutoIncrement;
}
public void setIsForeignKey(boolean isForeignKey)
{
this.isForeignKey=isForeignKey;
}
public boolean getIsForeignKey()
{
return this.isForeignKey;
}
public void setIsPrimaryKey(boolean isPrimaryKey)
{
this.isPrimaryKey=isPrimaryKey;
}
public boolean getIsPrimaryKey()
{
return this.isPrimaryKey;
}
public void setInfoClass(Class c)
{
this.c=c;
}
public Class getInfoClass()
{
return this.c;
}
public void setInsertStatement(StringBuilder insertStatement)
{
this.insertStatement=insertStatement;
}
public StringBuilder getInsertStatement()
{
return this.insertStatement;
}
public void setMethodMap(HashMap<String,Pair<String,String>> methodMap)
{
this.methodMap=methodMap;
}
public HashMap<String,Pair<String,String>> getMethodMap()
{
return this.methodMap;
}
public void setMethodList(ArrayList<Pair<String,String>> methodList)
{
this.methodList=methodList;
}
public ArrayList<Pair<String,String>> getMethodList()
{
return this.methodList;
}
public void setUpdateStatement(StringBuilder updateStatement)
{
this.updateStatement=updateStatement;
}
public StringBuilder getUpdateStatement()
{
return this.updateStatement;
}
public void setUpdateMethodList(ArrayList<Pair<String,String>> updateMethodList)
{
this.updateMethodList=updateMethodList;
}
public ArrayList<Pair<String,String>> getUpdateMethodList()
{
return this.updateMethodList;
}
public void setDeleteStatement(StringBuilder deleteStatement)
{
this.deleteStatement=deleteStatement;
}
public StringBuilder getDeleteStatement()
{
return this.deleteStatement;
}
public void setDeleteKeyPair(Pair<String,String> deleteKeyPair)
{
this.deleteKeyPair=deleteKeyPair;
}
public Pair<String,String> getDeleteKeyPair()
{
return this.deleteKeyPair;
}
}
