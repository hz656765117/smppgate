package com.hz.smsgate.base.je;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public enum BDBStoredMapFactoryImpl implements StoredMapFactory<Long, Object> {


    /**
     *
     */
    INS;
    private final ConcurrentHashMap<String, QueueEnvironment> envMap = new ConcurrentHashMap<String, QueueEnvironment>();

    private final ConcurrentHashMap<String, StoredMap<Long, Object>> storedMaps = new ConcurrentHashMap<String, StoredMap<Long, Object>>();
    private final ConcurrentHashMap<String, StoredSortedMap<Long, Object>> sortedstoredMap = new ConcurrentHashMap<String, StoredSortedMap<Long, Object>>();
    private final ConcurrentHashMap<String, BlockingQueue<Object>> queueMap = new ConcurrentHashMap<String, BlockingQueue<Object>>();
    private static Logger LOGGER = LoggerFactory.getLogger(BDBStoredMapFactoryImpl.class);
    @Override
    public synchronized Map<Long, Object> buildMap(String storedpath, String name) {
        QueueEnvironment env = buildBDB(storedpath);
        SerialBinding<Long> ObjectKeyBinding = new SerialBinding<Long>(env.getStoredClassCatalog(),
                Long.class);
        SerialBinding<Object> ObjectValueBinding = new SerialBinding<Object>(env.getStoredClassCatalog(),
                Object.class);
        Database db = env.buildDatabase(name);

        String keyName = new StringBuilder().append(storedpath).append(name).toString();
        StoredMap<Long, Object> map = storedMaps.get(keyName);
        if (map == null) {
            StoredMap<Long, Object> tmpMap = new StoredMap<Long, Object>(db,
                    ObjectKeyBinding,
                    ObjectValueBinding,
                    true);
            StoredMap<Long, Object> old = storedMaps.putIfAbsent(keyName, tmpMap);
            return old == null ? tmpMap : old;
        }
        return map;
    }

    @Override
    public BlockingQueue<Object> getQueue(String storedpath, String name) {
        String keyName = new StringBuilder().append(storedpath).append(name).toString();
        BlockingQueue<Object> queue = queueMap.get(keyName);
        if (queue == null) {
            StoredSortedMap<Long, Object> sortedStoredmap = buildStoredSortedMap(storedpath,
                    "Trans_" + name);
            BlockingQueue<Object> newqueue = new BdbQueueMap<Object>(sortedStoredmap);
            BlockingQueue<Object> oldqueue = queueMap.putIfAbsent(keyName, newqueue);
            return oldqueue == null ? newqueue : oldqueue;
        }
        return queue;
    }

    private StoredSortedMap<Long, Object> buildStoredSortedMap(String storedpath, String name) {
        QueueEnvironment env = buildBDB(storedpath);
        SerialBinding<Long> ObjectKeyBinding = new SerialBinding<Long>(env.getStoredClassCatalog(),
                Long.class);
        SerialBinding<Object> ObjectValueBinding = new SerialBinding<Object>(env.getStoredClassCatalog(),
                Object.class);
        Database db = env.buildDatabase(name);
        String keyName = new StringBuilder().append(storedpath).append(name).toString();

        StoredSortedMap<Long, Object> soredMap = sortedstoredMap.get(keyName);

        if (soredMap == null) {
            soredMap = new StoredSortedMap<Long, Object>(db,
                    (EntryBinding<Long>) ObjectKeyBinding,
                    (EntryBinding<Object>) ObjectValueBinding,
                    true);

            StoredSortedMap<Long, Object> old = sortedstoredMap.putIfAbsent(keyName, soredMap);
            return old == null ? soredMap : old;
        }

        return soredMap;
    }

    private QueueEnvironment buildBDB(String basename) {
        String pathName;
        basename = basename == null ? "" : basename;
        if (PropertiesUtils.globalBDBBaseHome.endsWith("/")) {
            pathName = PropertiesUtils.globalBDBBaseHome + basename;
        } else {
            pathName = PropertiesUtils.globalBDBBaseHome + "/" + basename;
        }

        File file = new File(pathName);
        if (!file.exists()) {
            boolean succ = file.mkdirs();

            if (!succ) {
                LOGGER.error("create Directory {} failed. ",pathName);
                return null;
            }

        }

        if (!file.isDirectory()) {
            LOGGER.error("file  {} is not a Directory ",pathName);
            return null;
        }
        LOGGER.info("init BDBPath : {}",pathName);
        QueueEnvironment env = envMap.get(pathName);

        if (env == null) {
            env = new QueueEnvironment().buildEnvironment(pathName).buildStoredClassCatalog();
            QueueEnvironment oldenv = envMap.putIfAbsent(pathName, env);
            return oldenv == null ? env : oldenv;
        }
        return env;
    }

    private class QueueEnvironment {
        private Environment environment;
        private DatabaseConfig dbConfig;
        private Database classCatalogDB;
        private StoredClassCatalog storedClassCatalog;
        private ConcurrentHashMap<String, Database> dbMap = new ConcurrentHashMap<String, Database>();

        public QueueEnvironment buildEnvironment(String pathHome) {

            File home = new File(pathHome);
            // 获取BDB的配置文件

            EnvironmentConfig environmentConfig = new EnvironmentConfig(PropertiesUtils.getJeProperties());
            environmentConfig.setAllowCreate(true);
            environmentConfig.setTransactional(true);
            environment = new Environment(home, environmentConfig);
            dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(true);
            cleanLogSchedule();
            return this;
        }

        public QueueEnvironment buildStoredClassCatalog() {
            return buildStoredClassCatalog("classCatalog");
        }

        public QueueEnvironment buildStoredClassCatalog(String Name) {
            classCatalogDB = environment.openDatabase(null, Name, dbConfig);
            storedClassCatalog = new StoredClassCatalog(classCatalogDB);
            return this;
        }

        public Database buildDatabase(String queueName) {
            Database queueDB = dbMap.get(queueName);
            if (queueDB == null) {
                queueDB = environment.openDatabase(null, queueName, dbConfig);
                Database olddb = dbMap.putIfAbsent(queueName, queueDB);
                return olddb == null ? queueDB : olddb;
            }
            return queueDB;
        }

        public void clearLog() {
            environment.cleanLog();
        }

        public void close() {
            environment.cleanLog();
            classCatalogDB.close();
            closeAllQueue();
            environment.close();
        }

        private synchronized void closeAllQueue() {
            for (Entry<String, Database> entry : dbMap.entrySet()) {
                entry.getValue().close();
            }
        }

        public StoredClassCatalog getStoredClassCatalog() {
            return storedClassCatalog;
        }

        private Properties loadFrompropertiesFile(File file) {
            Properties tmpProperties = new Properties();
            InputStream in = null;
            try {
                in = FileUtils.openInputStream(file);
                tmpProperties.load(in);
            }
            catch (Exception ex) {
                LOGGER.error("load je.properties error.",ex);
            }
            finally {
                IOUtils.closeQuietly(in);
            }
            return tmpProperties;
        }

        /**
         * 定时清除BDB的Log
         *
         */
        private void cleanLogSchedule() {
            Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    clearLog();
                }
            }, 60, 60, TimeUnit.SECONDS);
        }

    }

}
