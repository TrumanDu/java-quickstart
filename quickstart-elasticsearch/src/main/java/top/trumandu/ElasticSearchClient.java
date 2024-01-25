package top.trumandu;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.Sniffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Truman.P.Du
 * @date 2024/01/13
 */
@SuppressWarnings("unused")
@Component
public class ElasticSearchClient implements Closeable {
    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticSearchClient.class);
    @Value("${es.host}")
    private String esHost;
    private RestClient restClient;
    private ElasticsearchClient esClient;
    private Sniffer sniffer = null;

    @PostConstruct
    private void init() {
        restClient = RestClient
                .builder(HttpHost.create(esHost))
                .build();
        sniffer = Sniffer.builder(restClient).build();
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        esClient = new ElasticsearchClient(transport);
    }


    public void index(String index, JSONObject document) {
        try {
            Reader input = new StringReader(document.toJSONString());
            IndexRequest<JsonData> request = IndexRequest.of(i -> i
                    .index(index)
                    .withJson(input)
            );
            IndexResponse response = esClient.index(request);
        } catch (Exception e) {
            LOGGER.warn("esClient index has failed. message:{}", document, e);
        }
    }


    @PreDestroy
    @Override
    public void close() throws IOException {
        if (sniffer != null) {
            sniffer.close();
        }
        if (restClient != null) {
            restClient.close();
        }
        LOGGER.info("close es client.");
    }
}
