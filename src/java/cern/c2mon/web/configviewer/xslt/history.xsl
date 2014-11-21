<?xml version="1.0" encoding="UTF-8"?>
  <!--
    This XSLT transformation is used for transforming HistoryTag XML into
    HTML representation
  -->
  <xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
  <!--  link variables  -->
  <xsl:variable name="base_url">../</xsl:variable>
  <xsl:variable name="alarm_url">alarmviewer/</xsl:variable>
  <xsl:variable name="command_url">commandviewer/</xsl:variable>
  <xsl:variable name="datatag_url">tagviewer/</xsl:variable>
  <xsl:variable name="history_url">historyviewer/</xsl:variable>		
  <xsl:variable name="trend_viewer_url">trendviewer/</xsl:variable>		
  <xsl:variable name="history_xml_url">historyviewer/xml/</xsl:variable>		
  <xsl:variable name="process_xml_url">process/xml/</xsl:variable>
  <xsl:variable name="alarm_xml_url">alarmviewer/xml/</xsl:variable>
  <xsl:variable name="tag_value_xml_url">tagvalue/xml/</xsl:variable>	
  <xsl:variable name="tag_config_xml_url">tagconfig/xml/</xsl:variable>		
  
  <xsl:variable name="help_alarm_url">http://oraweb.cern.ch/pls/timw3/helpalarm.AlarmForm?p_alarmid=</xsl:variable>
  <xsl:variable name="help_point_url">https://oraweb.cern.ch/pls/timw3/helpalarm.AlarmList?p_pointid1=</xsl:variable>
  
  <xsl:variable name="trend_base_url">
    <xsl:value-of select="$base_url" />
    <xsl:value-of select="$trend_viewer_url" />
  </xsl:variable>
  
  <xsl:variable name="trend_parameter">
    <xsl:value-of select="/*/@trendURL" />
  </xsl:variable>

  <xsl:template match="p">
    <xsl:copy-of select="." />
  </xsl:template>

  <xsl:template match="/">

  <style type="text/css">
    tr.ok
    {
    color : #000000;
    background : #FFFFFF;
    }

    tr.nexist
    {
    color : #FFFFFF;
    background : #990099;
    }

    tr.invalid
    {
    color : #000000;
    background : #66FFFF;
    }
    
    table {
      font-size: 12px;
    }
  </style>

  <div class="row">
    <div class="page-header">
      <h2>
        Data Tag History
        (
        <xsl:value-of select="/*/@id" />
        )
        <xsl:value-of select="/*/@historyDescription" />

        <div class="pull-right">
          <a href="../" class="btn btn-large btn-default">
            <span class="glyphicon glyphicon-home"></span>
            Home
          </a>
          <a href="{$base_url}{$history_xml_url}{/*/@id}{$trend_parameter}" class="btn btn-large btn-default">History XML >>
          </a>
          <a href="{$trend_base_url}{/*/@id}{$trend_parameter}" class="btn btn-large btn-default"> Trend >>
          </a>
          <a href="{$base_url}{$datatag_url}{/*/@id}" class="btn btn-large btn-default">View Tag>>
          </a>
          <a href="{$help_point_url}{/*/@id}" class="btn btn-large btn-danger">View Help Alarm >>
          </a>
        </div>
      </h2>
    </div>
  </div>

  <div class="row">
    <table class="table table-striped table-bordered">
      <thead>
        <tr>
          <th>Server Timestamp</th>
          <th>Value</th>
          <th>Quality description</th>
          <th>Value Description</th>
          <th>Source Timestamp</th>
          <th>Mode</th>
        </tr>
      </thead>

      <tbody>
        <xsl:for-each select="history/HistoryTag">

          <!-- Used to display a light-blue line in case of a datatag with invalid quality -->
          <xsl:variable name="quality_status">
            <xsl:choose>
              <xsl:when test="dataTagQuality/invalidQualityStates/entry">
                invalid
              </xsl:when>
              <xsl:otherwise>
                ok
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <tr class="{$quality_status}">
            <td>
              <xsl:value-of select="serverTimestamp" />
            </td>
            <td>
              <xsl:value-of select="value" />
            </td>

            <td>
              <xsl:choose>
                <xsl:when test="dataTagQuality/invalidQualityStates/entry">
                  <xsl:for-each select="dataTagQuality/invalidQualityStates/entry">
                    <p>
                      <xsl:value-of select="tagQualityStatus" />
                      -
                      <xsl:value-of select="string" />
                    </p>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <p>
                    OK
                  </p>
                </xsl:otherwise>
              </xsl:choose>
            </td>

            <td>
              <xsl:value-of select="description" />
            </td>
            <td>
              <xsl:value-of select="sourceTimestamp" />
            </td>
            <td>
              <xsl:value-of select="mode" />
            </td>
          </tr>
        </xsl:for-each>

      </tbody>
    </table>
  </div>
</xsl:template>
</xsl:stylesheet>