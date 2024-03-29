package global;

import bufmgr.*;
import diskmgr.*;
import catalog.*;

import java.io.IOException;

public class SystemDefs {
    public static BufMgr JavabaseBM;
    public static bigDB JavabaseDB;
    public static Catalog JavabaseCatalog;

    public static String JavabaseDBName;
    public static String JavabaseLogName;
    public static boolean MINIBASE_RESTART_FLAG = false;
    public static String MINIBASE_DBNAME;

    public SystemDefs() {};

    public SystemDefs(String dbname, int num_pgs, int bufpoolsize,
                      String replacement_policy) {
        int logsize;

        String real_logname = new String(dbname);
        String real_dbname = new String(dbname);

        if (num_pgs == 0) {
            logsize = 500;
        } else {
            logsize = 3 * num_pgs;
        }

        if (replacement_policy == null) {
            replacement_policy = new String("Clock");
        }

        init(real_dbname, real_logname, num_pgs, logsize,
                bufpoolsize, replacement_policy);
    }


    public void init(String dbname, String logname,
                     int num_pgs, int maxlogsize,
                     int bufpoolsize, String replacement_policy) {

        boolean status = true;
        JavabaseBM = null;
        JavabaseDB = null;
        JavabaseDBName = null;
        JavabaseLogName = null;
        JavabaseCatalog = null;

        try {
            JavabaseBM = new BufMgr(bufpoolsize, replacement_policy);
            JavabaseDB = new bigDB();

        } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }

        JavabaseDBName = new String(dbname);
        JavabaseLogName = new String(logname);
        MINIBASE_DBNAME = new String(JavabaseDBName);

        // create or open the DB

        if ((MINIBASE_RESTART_FLAG) || (num_pgs == 0)) {//open an existing database
            try {
                JavabaseDB.openBigDB(dbname);
            } catch (Exception e) {
                System.err.println("" + e);
                e.printStackTrace();
                Runtime.getRuntime().exit(1);
            }
        } else {
            try {
                JavabaseDB.openBigDB(dbname, num_pgs);
                JavabaseBM.flushAllPages();
            } catch (Exception e) {
                System.err.println("" + e);
                e.printStackTrace();
                Runtime.getRuntime().exit(1);
            }
        }
    }

//    public void setNumBuffers(int numBuf) throws Exception {
//        for (int i = 0; i < numBuffers; ++i) {
//            frmeTable[i].pin_cnt = 0;
//        }
//        flushAllPages();
//        hashTable.clearHashTable();
//        this.numBuffers = numBuf;
//        frmeTable = new FrameDesc[numBuffers];
//        for (int i = 0; i < numBuffers; i++)  // initialize frameTable
//            frmeTable[i] = new FrameDesc();
//        bufPool = new byte[numBuffers][MAX_SPACE];
//        replacer = new Clock(this);
//        replacer.setBufferManager(this);
//    }


    public void changeNumberOfBuffers(int num_pgs, String replacement_policy)throws PageUnpinnedException,
            PagePinnedException, PageNotFoundException, HashOperationException, BufMgrException, IOException {
        JavabaseBM.flushAllPagesForcibly();
        JavabaseBM = new BufMgr(num_pgs, replacement_policy);
    }
}
