package ext.addcolumn;

import com.lcs.wc.db.FlexObject;
import com.ptc.core.lwc.server.addColumns.DataType;
import wt.dataservice.DSProperties;
import wt.tools.sql.SQLCommandTool;
import wt.type.TypeManaged;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

public class AddColRestUtilHelper {

    /**
     * Helps to search PCX based on flex Type, attribute name and value
     *
     * @param flexTypeStr
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Collection<FlexObject> getColumnDiff(String flexTypeStr,String impExp)
            throws Exception {
    	System.out.println("*****Running getColumnDiff for "+ impExp);
        Class flexTypeClass = getClass(flexTypeStr);
        Class schemaColumns = Class.forName("com.ptc.core.lwc.server.addColumns.SchemaColumns");
        Method scMethodFileDir = schemaColumns.getDeclaredMethod("getAddColumnSchemaFileDir", (Class[])null);
        scMethodFileDir.setAccessible(true);
        String schemanFileLocation = (String)scMethodFileDir.invoke(schemaColumns, (Object[])null);
        String fileLocation = schemanFileLocation + File.separator + "AddColumnsSchema.xml";
        File localFile = new File(fileLocation);
        Class[] classType = new Class[]{Class.class, File.class};
        Object[] objects = new Object[]{flexTypeClass, localFile};
        Method scMethodSchCol = schemaColumns.getDeclaredMethod("newSchemaColumns", classType);
        scMethodSchCol.setAccessible(true);
        scMethodSchCol.invoke(schemaColumns, objects);
        Class dbColumns = null;
        Object dbColumnsInst = null;

        Class[] introClassInst;
        Object[] localColumns;

        try {
            Connection flexTypeInfo = SQLCommandTool.getConnection(DSProperties.DB_SCHEMA_USER, DSProperties.DB_PASSWORD);
            dbColumns = Class.forName("com.ptc.core.lwc.server.addColumns.DatabaseColumns");
            introClassInst = new Class[]{Class.class, Connection.class, Boolean.TYPE};
            localColumns = new Object[]{flexTypeClass, flexTypeInfo, Boolean.valueOf(true)};
            Method dataType = dbColumns.getDeclaredMethod("newDatabaseColumns", introClassInst);
            dataType.setAccessible(true);
            dbColumnsInst = dataType.invoke(dbColumns, localColumns);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String flexTypeName = flexTypeClass.getName() + "TypeInfo";

        Class[] row;
        Object[] column;
        Method newIntrospectionColumnsMethod;
        Class introspectionColumnsClass;
        try {
            introspectionColumnsClass = Class.forName("com.ptc.core.lwc.server.addColumns.IntrospectionColumns");
            row = new Class[]{String.class};
            column = new Object[]{flexTypeName};
            newIntrospectionColumnsMethod = introspectionColumnsClass.getDeclaredMethod("newIntrospectionColumns", row);
            newIntrospectionColumnsMethod.setAccessible(true);
            newIntrospectionColumnsMethod.invoke(introspectionColumnsClass, column);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Method typeInfoColumnsMethod;
        Class typeInfoColumnsClass;
        try {
            typeInfoColumnsClass = Class.forName("com.ptc.core.lwc.server.addColumns.TypeInfoColumns");
            row = new Class[]{String.class};
            column = new Object[]{flexTypeName};
            typeInfoColumnsMethod = typeInfoColumnsClass.getDeclaredMethod("newTypeInfoColumns", row);
            typeInfoColumnsMethod.setAccessible(true);
            typeInfoColumnsMethod.invoke(typeInfoColumnsClass, column);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DataType[] flexColumns = DataType.values();
        ArrayList addColumnsList = new ArrayList();
        FlexObject addCol = null;
        int bfrCnt=0,unUsedCnt=0,totalCnt=0,colDiffCount=0;

        for(int currentCol = 0; currentCol < flexColumns.length; ++currentCol) {
            Class columnClass = Class.forName("com.ptc.core.lwc.server.addColumns.Columns");
            DataType dType = flexColumns[currentCol];
            Class[] classArg = new Class[]{DataType.class};
            Object[] objectArg = new Object[]{dType};
            Method getColCountMethod = columnClass.getDeclaredMethod("getColumnCount", classArg);
            getColCountMethod.setAccessible(true);
            Method getUnallocatedColumnCountMethod = dbColumns.getDeclaredMethod("getUnallocatedColumnCount", classArg);
            getUnallocatedColumnCountMethod.setAccessible(true);

            System.out.println("flexColumns--------: " + flexColumns[currentCol] + " ***** " + getColCountMethod.invoke(dbColumnsInst, objectArg) + " .. " + getUnallocatedColumnCountMethod.invoke(dbColumnsInst, objectArg));

            Object totalCol = getColCountMethod.invoke(dbColumnsInst, objectArg);
            Object unUsedCol = getUnallocatedColumnCountMethod.invoke(dbColumnsInst, objectArg);
            unUsedCnt=Integer.parseInt(unUsedCol.toString());
            totalCnt=Integer.parseInt(totalCol.toString());            
            /*if("Export".equals(impExp)){
            	colDiffCount=totalCnt - unUsedCnt;
            }else if("Import".equals(impExp)){
            	if(totalCnt>=2)
				colDiffCount =totalCnt-2;
				else
				colDiffCount = totalCnt;
            }
             */
            colDiffCount=totalCnt;// - unUsedCnt;

            String type = flexColumns[currentCol].name();

            System.out.println(type);
            System.out.println(flexColumns[currentCol].toString());
            System.out.println(flexColumns[currentCol].getTypeInfoPropertyPrefix());
            System.out.println("-----" + DataType.valueOf(flexColumns[currentCol].name()));
            System.out.println(DataType.BOOLEAN.toString());

            addCol = new FlexObject();

            if ( type.equals("STRING")) type = "String";
            if ( type.equals("LONG")) type = "Long";
            if ( type.equals("DOUBLE")) type = "Double";
            if ( type.equals("BOOLEAN")) type = "Boolean";
            if ( type.equals("TIMESTAMP")) type = "Timestamp";
            if ( type.equals("OBJECT_REF")) type = "ObjectReference";
            if ( type.equals("VERSION_REF")) type = "VersionReference";
            if ( type.equals("INLINE_BLOB")) type = "InlineBLOB";

            addCol.setData(type,String.valueOf(colDiffCount) );
            addColumnsList.add (addCol);
        }
        return addColumnsList;
    }

    private static Class getClass(String className) throws ClassNotFoundException {
        Class typeClass = Class.forName(className);
        return TypeManaged.class.isAssignableFrom(typeClass)?typeClass:null;
    }
}

