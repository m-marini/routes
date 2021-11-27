<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
  ~
  ~ Permission is hereby granted, free of charge, to any person
  ~ obtaining a copy of this software and associated documentation
  ~ files (the "Software"), to deal in the Software without
  ~ restriction, including without limitation the rights to use,
  ~ copy, modify, merge, publish, distribute, sublicense, and/or sell
  ~ copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following
  ~ conditions:
  ~
  ~ The above copyright notice and this permission notice shall be
  ~ included in all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  ~ EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  ~ OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  ~ NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  ~ HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  ~ WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  ~ OTHER DEALINGS IN THE SOFTWARE.
  ~
  ~    END OF TERMS AND CONDITIONS
  ~
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xsl:template match="/rts:routes/node">
        <xsl:text disable-output-escaping="yes">
    </xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text disable-output-escaping="yes">:
        x: </xsl:text>
        <xsl:value-of select="x"/>
        <xsl:text disable-output-escaping="yes">
        y: </xsl:text>
        <xsl:value-of select="y"/>
    </xsl:template>

    <xsl:template match="priority">
        <xsl:text disable-output-escaping="yes">
        priority: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="speedLimit">
        <xsl:text disable-output-escaping="yes">
        speedLimit: </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="/rts:routes/edge">
        <xsl:text disable-output-escaping="yes">
    -   start: </xsl:text>
        <xsl:value-of select="start"/>
        <xsl:text disable-output-escaping="yes">
        end: </xsl:text>
        <xsl:value-of select="end"/>
        <xsl:apply-templates select="priority"/>
        <xsl:apply-templates select="speedLimit"/>
    </xsl:template>

    <xsl:template match="/">
        <xsl:text disable-output-escaping="yes">---
speedLimit: </xsl:text>
        <xsl:value-of select="rts:routes/default/speedLimit"/>
        <xsl:text disable-output-escaping="yes">
nodes:</xsl:text>
        <xsl:apply-templates select="/rts:routes/node"/>
        <xsl:text disable-output-escaping="yes">
edges:</xsl:text>
        <xsl:apply-templates select="/rts:routes/edge"/>
        <xsl:text disable-output-escaping="yes">
</xsl:text>
    </xsl:template>

</xsl:stylesheet>