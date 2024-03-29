package BigT;

import global.MID;
import heap.Heapfile;

import java.util.ArrayList;
import java.util.Comparator;

public class rowJoin {
    private String columnName;
    private int amtOfMem;
    private bigt rightBigT, resultantBigT;
    private Stream leftStream, rightStream;
    private Heapfile leftHeapFile;
    private Heapfile rightHeapFile;
    private String LEFT_HEAP = "leftTempHeap";
    private String RIGHT_HEAP = "rightTempHeap";
    private String outBigTName;
    private String rightBigTName;
    private String leftName;

    private String JoinType;

    public rowJoin(int amt_of_mem, Stream leftStream, String RightBigTName, String ColumnName, String outBigTName, String leftName, String JoinType) throws Exception {

        this.JoinType = JoinType;
        this.columnName = ColumnName;
        this.amtOfMem = amt_of_mem;
        this.rightBigTName = RightBigTName;
        this.rightBigT = new bigt(RightBigTName);
        this.leftStream = leftStream;
        // Left stream should be filtered on column
        this.rightStream = this.rightBigT.openStream(rightBigTName, 1, "*", columnName, "*", amt_of_mem);
        this.leftHeapFile = new Heapfile(LEFT_HEAP);
        this.rightHeapFile = new Heapfile(RIGHT_HEAP);
        this.outBigTName = outBigTName;
        this.leftName = leftName;

        if(this.JoinType.equalsIgnoreCase("1")) {

            System.out.println("............You have chosen NestedLoop Join...............");
            System.out.println("............Implementing NestedLoop Join...............");
            NestedLoopJoin();

        }else{
            System.out.println("............You have chosen IndexNestedLoopJoin...............");
            System.out.println("............Implementing IndexNestedLoopJoin...............");
            IndexNestedLoopJoin();
        }
    }


    public void NestedLoopJoin() throws Exception {

        Heapfile heapfile = new Heapfile(this.outBigTName+"_1");
        if(heapfile.getRecCntMap() != 0)//if heapfile has records in it
        {
            heapfile.deleteFileMap();//then delete that heapfile
        }

            try {

                ArrayList<Map> outerRelation = new ArrayList<>();
                ArrayList<Map> innerRelation = new ArrayList<>();

                Map map3 = leftStream.getNext();
                while (map3 != null) {
                    Map newMap = new Map(map3); // Create a new map object with the same entries as map3
                    outerRelation.add(newMap); // Add the new map object to the list
                    map3 = leftStream.getNext(); // Get the next map from the stream
                }
                leftStream.closestream();

                Map map4 = rightStream.getNext();
                while (map4 != null) {
                    Map newMap = new Map(map4); // Create a new map object with the same entries as map3
                    innerRelation.add(newMap);
                    map4 = rightStream.getNext();
                }

                rightStream.closestream();

                if( outerRelation.isEmpty() || innerRelation.isEmpty()) {

                    bigt table = new bigt(this.outBigTName, 1);//create new
                }
                else {

                    bigt table = new bigt(this.outBigTName, 1);

                    for (Map outerRow : outerRelation) {

                        for (Map innerRow : innerRelation) {

                            if (outerRow.getValue().equals(innerRow.getValue()) && outerRow.getTimeStamp() != innerRow.getTimeStamp()) {
                                Map map1 = new Map();
                                map1.setDefaultHdr();
                                map1.setRowLabel(outerRow.getRowLabel() + ":" + innerRow.getRowLabel());
                                map1.setColumnLabel(outerRow.getColumnLabel());
                                map1.setTimeStamp(outerRow.getTimeStamp());
                                map1.setValue(outerRow.getValue());

                                Map map2 = new Map();
                                map2.setDefaultHdr();
                                map2.setRowLabel(outerRow.getRowLabel() + ":" + innerRow.getRowLabel());
                                map2.setColumnLabel(outerRow.getColumnLabel());
                                map2.setTimeStamp(innerRow.getTimeStamp());
                                map2.setValue(outerRow.getValue());

                                MID mid1 = table.insertMap(map1, 1);

                                MID mid2 = table.insertMap(map2, 1);
                                table.insertIndex(mid2, map2, 0);


                            } else if (outerRow.getValue().equals(innerRow.getValue()) && outerRow.getTimeStamp() == innerRow.getTimeStamp()) {

                                Map map1 = new Map();
                                map1.setDefaultHdr();
                                map1.setRowLabel(outerRow.getRowLabel() + ":" + innerRow.getRowLabel());
                                map1.setColumnLabel(outerRow.getColumnLabel());
                                map1.setTimeStamp(outerRow.getTimeStamp());
                                map1.setValue(outerRow.getValue());

                                MID mid1 = table.insertMap(map1, 1);
                                table.insertIndex(mid1, map1, 0);


                            }
                        }
                    }

                    int noDuplicateRecordCount = table.deleteDuplicateRecords();

                }


            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    public void IndexNestedLoopJoin() throws Exception {

        Heapfile heapfile = new Heapfile(this.outBigTName+"_1");
        if(heapfile.getRecCntMap() != 0)//if heapfile has records in it
        {
            heapfile.deleteFileMap();//then delete that heapfile
        }

        try {

            ArrayList<Map> outerRelation = new ArrayList<>();

            Map outerRow = leftStream.getNext();
            while (outerRow != null) {
                outerRelation.add(outerRow);
                outerRow = leftStream.getNext();
            }
            leftStream.closestream();

            bigt table = new bigt(this.outBigTName, 1);

            for (Map outer : outerRelation) {

                bigt rightTable = new bigt(this.rightBigTName, 1);
                Stream rightStream = rightTable.openStream(this.rightBigTName, 1, "*", this.columnName, outer.getValue(), this.amtOfMem);

                Map innerRow = rightStream.getNext();
                while (innerRow != null) {

                    if (outer.getTimeStamp() != innerRow.getTimeStamp()) {
                        Map map1 = new Map();
                        map1.setDefaultHdr();
                        map1.setRowLabel(outer.getRowLabel() + ":" + innerRow.getRowLabel());
                        map1.setColumnLabel(outer.getColumnLabel());
                        map1.setTimeStamp(outer.getTimeStamp());
                        map1.setValue(outer.getValue());

                        Map map2 = new Map();
                        map2.setDefaultHdr();
                        map2.setRowLabel(outer.getRowLabel() + ":" + innerRow.getRowLabel());
                        map2.setColumnLabel(outer.getColumnLabel());
                        map2.setTimeStamp(innerRow.getTimeStamp());
                        map2.setValue(outer.getValue());

                        MID mid1 = table.insertMap(map1, 1);

                        MID mid2 = table.insertMap(map2, 1);
                        table.insertIndex(mid2, map2, 0);
                    }

                    innerRow = rightStream.getNext();
                }

                rightStream.closestream();
            }

            int noDuplicateRecordCount = table.deleteDuplicateRecords();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
