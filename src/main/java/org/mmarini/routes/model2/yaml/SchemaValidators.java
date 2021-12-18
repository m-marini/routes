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

package org.mmarini.routes.model2.yaml;

import org.mmarini.yaml.schema.Validator;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static org.mmarini.yaml.schema.Validator.*;

public class SchemaValidators {

    private static final Validator DEFAULTS = objectProperties(Map.of(
            "frequence", nonNegativeNumber(),
            "speedLimit", nonNegativeNumber(),
            "priority", integer()
    ));

    private static final Validator EDGE = objectPropertiesRequired(Map.of(
            "start", string(),
            "end", string(),
            "speedLimit", nonNegativeNumber(),
            "priority", integer()
    ), List.of(
            "start", "end"
    ));

    private static final Validator NODE = objectPropertiesRequired(Map.of(
            "x", Validator.number(),
            "y", Validator.number()
    ), List.of("x", "y"));

    public static final Validator MODULE = objectPropertiesRequired(Map.of(
                    "defaults", defaults(),
                    "nodes", objectAdditionalProperties(node()),
                    "edges", arrayItems(edge())
            ), List.of(
                    "nodes",
                    "edges"
            )
    );
    private static final Validator WEIGHT = objectPropertiesRequired(Map.of(
                    "departure", string(),
                    "destination", string(),
                    "weight", nonNegativeNumber()
            ), List.of(
                    "departure",
                    "destination"
            )
    );

    private static final String VERSION_PATTERN = "(\\d+)\\.(\\d+)";
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 0;
    static final String BASE_VERSION = MAJOR_VERSION + "." + MINOR_VERSION;
    private static final Validator VERSION = locator -> root -> {
        String ver = locator.getNode(root).asText(BASE_VERSION);
        Matcher matcher = Pattern.compile(VERSION_PATTERN).matcher(ver);
        matcher.matches();
        int major = parseInt(matcher.group(1));
        int minor = parseInt(matcher.group(2));
        assertFor(major == MAJOR_VERSION && minor <= MINOR_VERSION,
                locator, "must be compatible with %s (%s)", BASE_VERSION, ver);
    };

    private static final Validator ROUTE = objectPropertiesRequired(Map.of(
            "sites", objectAdditionalProperties(node()),
            "nodes", objectAdditionalProperties(node()),
            "edges", arrayItems(edge()),
            "paths", arrayItems(weight()),
            "defaults", defaults(),
            "maxVehicles", nonNegativeInteger(),
            "version", string(
                    pattern(VERSION_PATTERN),
                    VERSION
            )
    ), List.of(
            "sites",
            "nodes",
            "edges",
            "paths"
    ));

    /**
     *
     */
    static Validator defaults() {
        return DEFAULTS;
    }

    /**
     *
     */
    static Validator edge() {
        return EDGE;
    }

    /**
     *
     */
    public static Validator module() {
        return MODULE;
    }

    /**
     *
     */
    static Validator node() {
        return NODE;
    }

    /**
     * @return
     */
    static Validator route() {
        return ROUTE;
    }

    /**
     * @return
     */
    static Validator weight() {
        return WEIGHT;
    }
}
