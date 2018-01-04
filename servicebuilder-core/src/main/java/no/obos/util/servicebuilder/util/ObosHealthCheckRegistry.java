package no.obos.util.servicebuilder.util;

import javax.sql.DataSource;

//TODO: Remove, temporary placeholder
public class ObosHealthCheckRegistry {
    public static void registerPingCheck(String s, String s1) {

    }

    public static void registerElasticSearchClusterCheck(String s, String clusterName, String indexname, Object cluster) {

    }

    public static void registerDataSourceCheck(String s, DataSource dataSource, String validationQuery) {

    }

    public static void registerActiveMqCheck(String s, String url, String queue, int maxQueueEntries, int queueEntriesGrace, String user, String password) {
    }

    public static void registerActiveMqCheck(String s, String url, String queueError, String user, String password) {

    }
}
