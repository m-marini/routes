<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xsl:template match="/rts:routes/default">
            <xsl:text>
default:</xsl:text>
        <xsl:if test="speedLimit">
            <xsl:text>
    speedLimit: </xsl:text>
            <xsl:value-of select="speedLimit"/>
        </xsl:if>

        <xsl:if test="frequence">
            <xsl:text>
    frequence: </xsl:text>
            <xsl:value-of select="frequence"/>
        </xsl:if>

        <xsl:if test="priority">
            <xsl:text>
    priority: </xsl:text>
            <xsl:value-of select="priority"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/rts:routes/site">
        <xsl:text>
    </xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text>:
        x: </xsl:text>
        <xsl:value-of select="x"/>
        <xsl:text>
        y: </xsl:text>
        <xsl:value-of select="y"/>
    </xsl:template>

    <xsl:template match="/rts:routes/path">
        <xsl:text>
-   departure: </xsl:text>
        <xsl:value-of select="departure"/>
        <xsl:text>
    destination: </xsl:text>
        <xsl:value-of select="destination"/>
        <xsl:if test="weight">
            <xsl:text>
    weight: </xsl:text>
            <xsl:value-of select="weight"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/rts:routes/node">
        <xsl:text>
    </xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text>:
        x: </xsl:text>
        <xsl:value-of select="x"/>
        <xsl:text>
        y: </xsl:text>
        <xsl:value-of select="y"/>
    </xsl:template>

    <xsl:template match="/rts:routes/edge">
        <xsl:text>
-   start: </xsl:text>
        <xsl:value-of select="start"/>
        <xsl:text>
    end: </xsl:text>
        <xsl:value-of select="end"/>
        <xsl:if test="speedLimit">
            <xsl:text>
    speedLimit: </xsl:text>
            <xsl:value-of select="speedLimit"/>
        </xsl:if>
        <xsl:if test="priority">
            <xsl:text>
    priority: </xsl:text>
            <xsl:value-of select="priority"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/">
        <xsl:text>---</xsl:text>
        <xsl:apply-templates select="/rts:routes/default" />
        <xsl:text>
sites:</xsl:text>
        <xsl:apply-templates select="/rts:routes/site" />
        <xsl:text>
paths:</xsl:text>
        <xsl:apply-templates select="/rts:routes/path" />
        <xsl:text>
nodes:</xsl:text>
        <xsl:apply-templates select="/rts:routes/node" />
        <xsl:text>
edges:</xsl:text>
        <xsl:apply-templates select="/rts:routes/edge" />
        <xsl:text>
</xsl:text>
    </xsl:template>

</xsl:stylesheet>