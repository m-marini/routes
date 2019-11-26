<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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