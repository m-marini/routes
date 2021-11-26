/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.routes.processor;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static java.lang.Math.round;
import static java.lang.Math.toRadians;
import static org.mmarini.yaml.Utils.fromFile;
import static org.mmarini.yaml.Utils.iter2Stream;

public class ModelProcessor {
    static final Logger logger = LoggerFactory.getLogger(ModelProcessor.class);

    private static AffineTransform createRotation(JsonNode rotNode) {
        assert rotNode.isObject();
        double x = rotNode.path("x").asDouble(0);
        double y = rotNode.path("y").asDouble(0);
        double angle = rotNode.path("angle").asDouble(0);
        return AffineTransform.getRotateInstance(toRadians(angle), x, y);
    }

    static AffineTransform createTransform(JsonNode process) {
        assert process.isArray();
        ArrayNode steps = (ArrayNode) process;
        AffineTransform s = iter2Stream(steps.elements())
                .map(step -> {
                    assert step.isObject();
                    JsonNode rotNode = step.path("rotate");
                    if (!rotNode.isMissingNode()) {
                        return createRotation(rotNode);
                    }
                    JsonNode translateNode = step.path("translate");
                    assert !translateNode.isMissingNode();
                    return createTranslation(translateNode);
                })
                .reduce((a, b) -> {
                    b.concatenate(a);
                    return b;
                })
                .orElseThrow();
        return s;
    }

    private static AffineTransform createTranslation(JsonNode translateNode) {
        assert translateNode.isObject();
        double x = translateNode.path("x").asDouble(0);
        double y = translateNode.path("y").asDouble(0);
        return AffineTransform.getTranslateInstance(x, y);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        assert args.length == 3;
        logger.info("Loading process {} ...", args[2]);
        JsonNode process = fromFile(args[2]);
        AffineTransform trans = createTransform(process);
        logger.info("Loading model {} ...", args[0]);
        JsonNode model = fromFile(args[0]);
        model = transform(model, trans);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        logger.info("Writting model {} ...", args[1]);
        mapper.writeValue(new FileWriter(args[1]), model);
        logger.info("Completed.");
    }

    static JsonNode transform(JsonNode model, AffineTransform trans) {
        transformNodes(model.at(JsonPointer.valueOf("/sites")), trans);
        transformNodes(model.at(JsonPointer.valueOf("/nodes")), trans);
        return model;
    }

    private static void transformNodes(JsonNode nodes, AffineTransform trans) {
        assert nodes.isObject();
        iter2Stream(nodes.fields())
                .map(Map.Entry::getValue)
                .forEach(node -> {
                    assert node.isObject();
                    double x = node.path("x").asDouble(0);
                    double y = node.path("y").asDouble(0);
                    ObjectNode n = (ObjectNode) node;
                    Point2D.Double ptSrc = new Point2D.Double(x, y);
                    trans.transform(ptSrc, ptSrc);
                    n.put("x", (double) round(ptSrc.getX()));
                    n.put("y", (double) round(ptSrc.getY()));
                });
    }
}
