package no.obos.util.template.controllers;

import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.es.Indexer;
import no.obos.util.servicebuilder.es.Searcher;
import no.obos.util.template.model.Template;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilders;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class TemplateControllerElasticsearch implements TemplateController {

    @Inject
    Searcher<Template> searcher;
    @Inject
    Indexer<Template> indexer;
    int nextInt = 1;


    private void index(List<Template> toIndex) {
        String schema;
        try {
            schema = jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    .startObject("id")
                    .field("type", "integer")
                    .field("index", true)
                    .endObject()
                    .startObject("date")
                    .field("type", "date")
                    .field("index", true)
                    .endObject()
                    .startObject("name")
                    .field("type", "text")
                    .field("index", true)
                    .endObject()
                    .startObject("myDouble")
                    .field("type", "float")
                    .field("index", true)
                    .endObject()
                    .endObject()
                    .endObject().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        indexer.index(schema, toIndex, template -> String.valueOf(template.id));
    }

    @Override
    public List<Template> getAll() {
        try {
            return searcher.query(QueryBuilders.rangeQuery("id").from("0"));
        } catch (IndexNotFoundException e) {
            return Lists.newArrayList();
        }

    }

    @Override
    public Template get(int id) {
        List<Template> result = searcher.query(QueryBuilders.queryStringQuery(String.valueOf(id)).field("id"));
        if (result.isEmpty()) {
            throw new NotFoundException("No " + Template.class.getSimpleName() + " for id " + id);
        }
        return result.get(0);
    }

    @Override
    public Template create(Template toIndex) {
        int id = nextInt++;
        Template withId = toIndex.toBuilder().id(id).build();
        index(Lists.newArrayList(withId));
        return withId;
    }

    @Override
    public void update(int id, Template payload) {
        if (searcher.query(QueryBuilders.queryStringQuery(String.valueOf(id)).field("id")).isEmpty()) {
            throw new NotFoundException("No " + Template.class.getSimpleName() + " for id " + id);
        }
        Template withId = payload.toBuilder().id(id).build();
        index(Lists.newArrayList(withId));
    }

    @Override
    public void delete(int i) {
        indexer.delete(String.valueOf(i));
    }
}
