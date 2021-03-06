/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.tuscany.sca.diagram.artifacts;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Structure of a "Outermost layer" : not a SCA artifact
 *
 */
public class Layer extends Artifact {

    public Element addElement(Document document, String svgNs, int x, int y, int height, int width) {

        this.setHeight(height);
        this.setWidth(width);
        this.setxCoordinate(x);
        this.setyCoordinate(y);

        Element rectangle = document.createElementNS(svgNs, "rect");
        rectangle.setAttributeNS(null, "x", x + "");
        rectangle.setAttributeNS(null, "y", y + "");
        rectangle.setAttributeNS(null, "rx", getRoundCorner());
        rectangle.setAttributeNS(null, "ry", getRoundCorner());
        rectangle.setAttributeNS(null, "width", width + "");
        rectangle.setAttributeNS(null, "height", height + "");
//        rectangle.setAttributeNS(null, "fill", "#E5E5D0");
//        rectangle.setAttributeNS(null, "stroke", "#919191");
//        rectangle.setAttributeNS(null, "alignment-baseline", "middle");
        rectangle.setAttributeNS(null, "class", "layer");

        return rectangle;
    }

    public Element addElement(Document document, String svgNs, int x, int y, int height, int width, String fillColor) {

        Element rect = addElement(document, svgNs, x, y, height, width);
        rect.setAttributeNS(null, "fill", fillColor);

        return rect;
    }

}
