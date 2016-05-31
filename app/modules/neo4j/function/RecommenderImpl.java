/*
 * Copyright (C) 2016 Thunderbit team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package modules.neo4j.function;

import com.google.inject.Inject;
import modules.neo4j.domain.Tag;
import modules.neo4j.session.Neo4JSessionProvider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RecommenderImpl implements Recommender {
    @Inject
    public Neo4JSessionProvider sessionProvider;

    /**
     * {@inheritDoc}
     */
    public List<Iterable<Tag>> recommends(String... tagsNames) {
        String querySuffix = " RETURN recommended ORDER BY r.weight DESC";
        List<String> queries = new LinkedList<>();
        for (String tname : tagsNames) {
            String query = createOneQuery(tname) + querySuffix;
            System.out.println("Recommender query: " + query);
            queries.add(query);
        }
        List<Iterable<Tag>> results = new LinkedList<>();
        queries.forEach(q -> results.add(sendQuery(q)));
        return toMaximumFive(results);

    }

    /**
     * Create a query
     */
    private String createOneQuery(String tname) {
        return "MATCH (:Tag {name:\"" + tname + "\"})-[r:RELATED_TO]-(recommended)";
    }

    /**
     * Executes the query and returns the result
     */
    private Iterable<Tag> sendQuery(String q) {
        return sessionProvider.getSession().query(Tag.class, q, Collections.EMPTY_MAP);
    }

    private List<Iterable<Tag>> toMaximumFive(List<Iterable<Tag>> results) {
        List<Iterable<Tag>> r = new LinkedList<>();
        results.stream().forEach((it) -> {
            List<Tag> tempList = (List) it;
            if (tempList.size() > 5) {
                r.add(listToIterable(tempList.subList(0, 5)));
            } else {
                r.add(it);
            }
        });
        return r;
    }

    private Iterable<Tag> listToIterable(List<Tag> l) {
        return (Iterable<Tag>) l;
    }
}
