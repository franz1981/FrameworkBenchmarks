package io.quarkus.benchmark.repository;

import io.netty.util.concurrent.FastThreadLocal;
import io.vertx.mutiny.sqlclient.SqlClient;

class PgClients {
    private final FastThreadLocal<SqlClient> sqlClient = new FastThreadLocal<>();
    private PgClientFactory pgClientFactory;

    // for ArC
    public PgClients() {
    }

    public PgClients(final PgClientFactory pgClientFactory) {
        this.pgClientFactory = pgClientFactory;
    }

    SqlClient getClient() {
        SqlClient ret = sqlClient.get();
        if (ret == null) {
            ret = pgClientFactory.sqlClient(1);
            sqlClient.set(ret);
        }
        return ret;
    }
}